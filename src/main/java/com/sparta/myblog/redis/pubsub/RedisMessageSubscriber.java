package com.sparta.myblog.redis.pubsub;

import com.sparta.myblog.entity.SseEventName;
import com.sparta.myblog.entity.SseRepositoryKeyRule;
import com.sparta.myblog.exception.ErrorCode;
import com.sparta.myblog.exception.SseException;
import com.sparta.myblog.repository.SSERepository;
import com.sparta.myblog.utils.CustomTimeUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisMessageSubscriber implements MessageListener {
    private static final String UNDER_SCORE = "_";
    private final SSERepository sseRepository;
    /**
     * 여러 서버에서 SSE 를 구현하기 위한 Redis Pub/Sub
     * subscribe 해두었던 topic 에 publish 가 일어나면 메서드가 호출된다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {

        log.info("Redis Pub/Sub message received: {}", message.toString());

        String[] strings = message.toString().split(UNDER_SCORE);

        Long userId = Long.parseLong(strings[0]);
        SseEventName eventName = SseEventName.getEnumFromValue(strings[1]);

        String keyPrefix = new SseRepositoryKeyRule(userId, eventName,
            null).toCompleteKeyWhichSpecifyOnlyOneValue();

        LocalDateTime now = CustomTimeUtils.nowWithoutNano();

        sseRepository.getKeyListByKeyPrefix(keyPrefix).forEach(key -> {
            SseEmitter emitter = sseRepository.get(key).get();
            try {
                emitter.send(SseEmitter.event()
                    .id(getEventId(userId, now, Objects.requireNonNull(eventName)))
                    .name(eventName.getValue())
                    .data(eventName.getValue()));
            } catch (IOException e) {
                sseRepository.remove(key);
                log.error("SSE send error", e);
                throw new SseException(ErrorCode.SSE_SEND_ERROR);
            }
        });
    }

    /**
     *  특정 유저의 특정 sse 이벤트에 대한 id를 생성한다.
     *  위 조건으로 여러개 정의 될 경우 now 로 구분한다.
     */
    private String getEventId(Long userId, LocalDateTime now, SseEventName eventName) {
        return userId + UNDER_SCORE + eventName.getValue() + UNDER_SCORE + now;
    }
}
