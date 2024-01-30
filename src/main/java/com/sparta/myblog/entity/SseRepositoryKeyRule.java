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

    // SSEInMemoryRepository 에서 사용될 특정 user 에 대한 특정 브라우저, 특정 SSEEventName 에 대한 SSEEmitter를 찾기 위한 key 를 생성한다.
    // 또한 HTTP 1.1의 경우에 하나의 브라우저에서 6개 까지의 연결을 할 수 있기 때문에 한명의 유저가 여러개의 SSE 연결을 할 수 있습니다.
    // 따라서 Key 값은 유저정보와 생성정보를 포함하여 한명의 유저에 대해서 구분할 수 있도록 합니다.
    public String toCompleteKeyWhichSpecifyOnlyOneValue() {
        return createdAt != null ? toKeyUserAndEventInfo() + UNDER_SCORE + createdAt.toString() :
            toKeyUserAndEventInfo();
    }

    public String toKeyUserAndEventInfo() {
        return userId + UNDER_SCORE + sseEventName.getValue();
    }
}