package com.sparta.myblog.controller;

import com.sparta.myblog.dto.NotificationDto;
import com.sparta.myblog.service.NotificationServiceImpl;
import com.sparta.myblog.utils.CustomTimeUtils;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알람 관련 API", description = "알람 관련 API 입니다.")
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @PreAuthorize("hasAuthority('alarm.read')")
    @Operation(summary = "알람 sse 구독", description = "알람 sse 구독, sseEmitter객체 반환.")
    @GetMapping(value = "/alarm/subscribe", produces = "text/event-stream")
    public SseEmitter notificationSubscribe(
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user,
        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
        HttpServletResponse response) {

        //nginx 리버스 프록시에서 버퍼링 기능으로 인한 오동작 방지
        response.setHeader("X-Accel-Buffering", "no");

        LocalDateTime now = CustomTimeUtils.nowWithoutNano();
        return notificationService.subscribe(user.getUsername(), lastEventId);
    }

    @PreAuthorize("hasAuthority('alarm.read')")
    @Operation(summary = "알림 목록조회", description = "조회 시 날짜 기준 내림차순 정렬 다음 쿼리 파라미터 필요.  ?sort=createdDate,desc")
    @GetMapping("/alarms")
    public Slice<NotificationDto> readAllNotifications(
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user, Pageable pageable) {
        log.info("readAllAlarms{}", Thread.currentThread().getName());
        return notificationService.sendAlarmSliceAndIsReadToTrue(pageable, user.getUsername());
    }
}

