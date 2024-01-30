package com.sparta.myblog.repository;

import groovy.util.logging.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SSEInMemoryRepository implements SSERepository {

    // Web Application Server 가 redis pub 메시지를 수신하면 다음 로직을 수행합니다.
    // 먼저 인메모리 내의 ConcurrentHashMap 에서 pub 메시지 정보에 해당하는 내용으로 SseEmitter 객체를 찾을 수 있는지 확인합니다.
    // 이는 해당 WAS 가 SSE 응답을 보내야하는 클라이언트와 연결된 WAS 인지 확인하는 과정이기도 합니다.
    // SseEmitter를 하나라도 찾은 WAS 는 SSE 응답을 클라이언트에게 보내고
    // 이 응답을 통해 클라이언트는 스스로 요청(polling)하지 않고 알림 내역을 비동기적으로 응답받을 수 있습니다.

    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    @Override
    public void put(String key, SseEmitter sseEmitter) {
        sseEmitterMap.put(key, sseEmitter);
    }

    @Override
    public Optional<SseEmitter> get(String key) {
        return Optional.ofNullable(sseEmitterMap.get(key));
    }

    @Override
    public List<SseEmitter> getListByKeyPrefix(String keyPrefix) {
        return sseEmitterMap.keySet().stream()
            .filter(key -> key.startsWith(keyPrefix))
            .map(sseEmitterMap::get)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getKeyListByKeyPrefix(String keyPrefix) {
        return sseEmitterMap.keySet().stream()
            .filter(key -> key.startsWith(keyPrefix))
            .collect(Collectors.toList());
    }

    public void remove(String key) {
        sseEmitterMap.remove(key);
    }
}