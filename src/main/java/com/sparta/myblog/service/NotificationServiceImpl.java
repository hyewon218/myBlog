package com.sparta.myblog.service;

import com.sparta.myblog.dto.NotificationArgsDto;
import com.sparta.myblog.dto.NotificationDto;
import com.sparta.myblog.entity.Notification;
import com.sparta.myblog.entity.NotificationArgs;
import com.sparta.myblog.entity.NotificationType;
import com.sparta.myblog.entity.SseEventName;
import com.sparta.myblog.entity.SseRepositoryKeyRule;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.exception.ErrorCode;
import com.sparta.myblog.exception.NoEntityException;
import com.sparta.myblog.exception.SseException;
import com.sparta.myblog.repository.NotificationRepository;
import com.sparta.myblog.repository.SSERepository;
import com.sparta.myblog.repository.UserRepository;
import com.sparta.myblog.utils.CustomTimeUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NotificationServiceImpl {

    @Value("${sse.timeout}")
    private String sseTimeout;
    private static final String UNDER_SCORE = "_";
    private static final String CONNECTED = "CONNECTED";
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SSERepository sseRepository;

    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Slice<NotificationDto> sendAlarmSliceAndIsReadToTrue(Pageable pageable,
        String username) {
        Long userId = getUserByUsernameOrException(username).getId();
        Slice<Notification> alarmSlices = notificationRepository.findSliceByCondition(pageable,
            userId);
        List<Notification> notifications = alarmSlices.getContent();

        //update 쿼리 여러번 나가는지 확인 해봐야 함.
        notifications.forEach(Notification::read);
        return new SliceImpl<>(notifications.stream()
            .map(notification ->
                NotificationDto.builder()
                    .notificationId(notification.getApiId())
                    .text(notification.getNotificationType().getAlarmContent())
                    .notificationArgsDto(NotificationArgsDto.builder()
                        .postId(notification.getNotificationArgs().getPostId())
                        .commentId(notification.getNotificationArgs().getCommentId())
                        .callingMemberId(
                            notification.getNotificationArgs().getCallingMemberNickname())
                        .build())
                    .build())
            .collect(Collectors.toList()),
            alarmSlices.getPageable(), alarmSlices.hasNext());
    }

    public void send(Long alarmReceiverId, NotificationType notificationType,
        NotificationArgs notificationArgs, SseEventName sseEventName) {
        User user = userRepository.findById(alarmReceiverId).orElseThrow(() ->
            new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        Notification notification = Notification.of(notificationType, notificationArgs, user);

        notificationRepository.save(notification);

        redisTemplate.convertAndSend(sseEventName.getValue(),
            getRedisPubMessage(alarmReceiverId, sseEventName));

    }

    public SseEmitter subscribe(String username, String lastEventId) {
        Long userId = getUserByUsernameOrException(username).getId();
        LocalDateTime now = CustomTimeUtils.nowWithoutNano();
        SseEmitter sse = new SseEmitter(Long.parseLong(sseTimeout));

        String key = new SseRepositoryKeyRule(userId, SseEventName.NOTIFICATION_LIST,
            now).toCompleteKeyWhichSpecifyOnlyOneValue();

        sse.onCompletion(() -> {
            log.info("onCompletion callback");

            sseRepository.remove(key);
        });
        sse.onTimeout(() -> {
            log.info("onTimeout callback");
            //만료 시 Repository 에서 삭제 되어야 함
            sse.complete();
        });

        sseRepository.put(key, sse);

        try {
            sse.send(SseEmitter.event()
                .name(CONNECTED)
                .id(getEventId(userId, now, SseEventName.NOTIFICATION_LIST))
                .data("subscribe"));

        } catch (IOException exception) {
            sseRepository.remove(key);

            log.info("SSE Exception: {}", exception.getMessage());

            throw new SseException(ErrorCode.SSE_SEND_ERROR);
        }

        // 중간에 연결이 끊겨서 다시 연결할 때, lastEventId를 통해 기존의 받지못한 이벤트를 받을 수 있도록 할 수 있음.
        // 현재 로직 상에서는 어처피 한번의 알림이나 새로고침을 받으면 알림 list 를 paging 해서 가져오기 때문에
        // 수신 못한 응답을 다시 보내는 게 불필요하고 효율을 떨어뜨릴 수 있음.
        return sse;
    }

    /**
     * 특정 유저의 특정 sse 이벤트에 대한 id를 생성한다. 위 조건으로 여러개 정의 될 경우 now 로 구분한다.
     */
    private String getEventId(Long userId, LocalDateTime now, SseEventName eventName) {
        return userId + UNDER_SCORE + eventName.getValue() + UNDER_SCORE + now;
    }

    /**
     * redis pub 시 userId와 sseEventName 을 합쳐서 보낸다.
     */
    private String getRedisPubMessage(Long userId, SseEventName sseEventName) {
        return userId + UNDER_SCORE + sseEventName.getValue();
    }

    private User getUserByUsernameOrException(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new NoEntityException(
                ErrorCode.ENTITY_NOT_FOUND));
    }
}
