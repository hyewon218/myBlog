package com.sparta.myblog.entity;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SseRepositoryKeyRule {

    private static final String UNDER_SCORE = "_";

    private final Long userId;
    private final SseEventName sseEventName;
    private final LocalDateTime createdAt;

    /**
     * SSEInMemoryRepository 에서 사용될 특정 user 에 대한 특정 브라우저, 특정 SSEEventName 에 대한 SSEEmitter를 찾기 위한 key 를 생성한다.
     *
     */
    public String toCompleteKeyWhichSpecifyOnlyOneValue() {

        return createdAt != null ? toKeyUserAndEventInfo() + UNDER_SCORE + createdAt.toString() :
            toKeyUserAndEventInfo();
    }

    /**
     * SSEInMemoryRepository 에서 사용될 특정 user, 특정 SSEEventName 에 대한 모든 SSEEmitter를 찾기 위한 key 를 생성한다.
     *
     */
    public String toKeyUserAndEventInfo() {
        return userId + UNDER_SCORE + sseEventName.getValue();
    }
}