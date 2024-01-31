# 기존 알림 기능
## 문제점
현재는 알림이 생성되는 시점에 클라이언트가 이를 알 수 있는 방법이 없고 단지 서버의 상태만 변경됩니다.<br> 
따라서 클라이언트에서 조회 api를 호출해야만 알림이 갱신됩니다. 이를 자동적으로 사용자 화면에 반영될 수 있도록 하려고 합니다.
## 해결법
### Short Polling
- 클라이언트에서 **주기적으로 서버로 요청을 보내는** 방법입니다.
- 서버에 대한 부담이 크지 않고 요청 주기를 넉넉하게 잡아도 될 정도로 실시간성이 중요하지 않다면 고려해 볼 만한 방법입니다.

### Long Polling
- **요청을 보내고 서버에서 변경이 일어날 때까지 대기**하는 방법입니다.
- Short Polling에 비해서는 서버의 부담이 줄어들지만 **한번의 서버 변경마다 다시 연결을 재요청해야** 합니다.

위의 해결법들의 장단점을 고려하여 **SSE 방식**을 선택했습니다.<br>
polling방식의 경우 **클라이언트에서 주기적으로 요청을** 해야하기 때문에 요청량이 많아질 경우 비효율적으로 서버의 리소스 사용량이 증가합니다.<br>
알림 기능은 서버에서 클라이언트로 **비동기적**으로 통신을 할 수 있으면 되기 때문에 WebSocket을 선택하지 않기로 했습니다.

### Server-Sent Events
- 서버와 한번 연결을 맺고나면 일정 시간동안 서버에서 변경이 발생할 때마다 데이터를 전송받는 방법입니다.
- Long Polling과 달리 서버에서 변경이 발생해도 다시 재연결 할 필요가 없습니다.
- 최대 연결 수에 제한이 있습니다.

### WebSocket
- 서버와 연결을 유지하면서 양방향 통신을 유지할 수 있습니다.
- SSE에 비해서 구현과 학습에 비용이 클 수 있습니다.

## 선택
위의 해결법들의 장단점을 고려하여 SSE 방식을 선택했습니다.<br>
polling 방식의 경우 client에서 주기적으로 요청을 해야하기 때문에 요청량이 많아질 경우 비효율적으로 서버의 리소스 사용량이 증가합니다.<br>
알림 기능은 서버에서 클라이언트로 비동기적으로 통신을 할 수 있으면 되기 때문에 WebSocket을 선택하지 않기로 했습니다.

# ✔️ Server-Sent Events(SSE)
Server-Sent Events(이하 SSE)는 HTTP 스트리밍을 통해 서버에서 클라이언트로 **단방향의 Push Notification**을 전송할 수 있는 HTML5 표준 기술입니다.<br>
HTTP/1.1 프로토콜 사용시 브라우저에서 1개 도메인에 대해 생성할 수 있는 EventSteam의 최대 개수는 6개로 제한됩니다.<br> 
(HTTP/2 프로토콜 사용시에는 브라우저와 서버간의 조율로 최대 100개까지 유지가 가능합니다.) <br>
이벤트 데이터는 UTF-8 인코딩된 문자열만 지원합니다. 서버 사이드에서 이벤트 데이터를 담은 객체를 JSON으로 마샬링하여 전송하는 것이 가장 일반적입니다.<br>
현재 Internet Explorer을 제외한 모든 브라우저에서 지원합니다. JavaScript에서는 EventSource를 이용하여 연결 생성 및 전송된 이벤트에 대한 제어가 가능합니다.<br>
Spring Framework는 4.2(2015년)부터 SseEmitter 클래스를 제공하여 서버 사이드에서의 SSE 통신 구현이 가능해졌습니다.


- 전통적인 웹 애플리케이션이라면 클라이언트의 요청 단건에 대해 서버가 응답하는 방식이지만 <br>
SSE를 이용하면 별도의 복잡한 기술이 필요없이 HTTP 프로토콜을 기반으로 서버에서 클라이언트로 Real-Time Push Notification을 전송할 수 있다.<br>
클라이언트의 요청에 의해 한 번 연결이 맺어지면 서버가 원하는 시점에 클라이언트에게 원하는 메시지를 전송할 수 있다. <br>
이러한 특징 덕분에 최소의 오버헤드로 모니터링 시스템의 그래프 갱신, 채팅 및 메신저 등의 비지니스에 광범위하게 적용할 수 있다.
- HTTP/1.1 프로토콜 사용시 브라우저에서 1개 도메인에 대해 생성할 수 있는 EventSteam의 최대 개수는 6개로 제한된다. <br>
(HTTP/2 프로토콜 사용시에는 브라우저와 서버간의 조율로 최대 100개까지 유지가 가능하다.)
- 이벤트 데이터는 UTF-8 인코딩된 문자열만 지원한다. 서버 사이드에서 이벤트 데이터를 담은 객체를 JSON으로 마샬링하여 전송하는 것이 가장 일반적이고 무난하다.
- 현재 Internet Explorer을 제외한 모든 브라우저에서 지원한다. JavaScript에서는 EventSource를 이용하여 연결 생성 및 전송된 이벤트에 대한 제어가 가능하다.
- Spring Framework는 4.2(2015년)부터 SseEmitter 클래스를 제공하여 서버 사이드에서의 SSE 통신 구현이 가능해졌다.

## ⭐️ 기본적인 동작 흐름
### 1. 클라이언트에서 SSE 연결 요청을 보낸다.
```
   GET /connect HTTP/1.1
   Accept: text/event-stream
   Cache-Control: no-cache
```   
- 이벤트의 미디어 타입은 `text/event-stream`이 표준으로 정해져있습니다.<br> 
- 이벤트는 캐싱하지 않으며 지속적 연결을 사용해야 합니다(HTTP 1.1에서는 기본적으로 지속 연결을 사용합니다).

### 2. 서버에서는 클라이언트와 매핑되는 SSE 통신 객체를 만들고 연결 확정 응답을 보낸다.
```
   HTTP/1.1 200
   Content-Type: text/event-stream;charset=UTF-8
   Transfer-Encoding: chunked
```   
- 응답의 미디어 타입은 `text/event-stream` 입니다.<br>
이때 Transfer-Encoding 헤더의 값을 `chuncked`로 설정합니다.<br> 
서버는 동적으로 생성된 컨텐츠를 스트리밍하기 때문에 본문의 크기를 미리 알 수 없기 때문입니다.

### 3. 서버에서 이벤트가 발생하면 해당 객체를 통해 클라이언트로 데이터를 전달한다.
- 서버에서 SSE통신 객체를 유저마다 저장해놓고 해당 유저에게 응답을 보내야 할 경우 해당 객체를 활용하여 데이터를 전달합니다.

## 만료 시간
- SSE연결 시 서버에서 만료시간을 설정합니다.
- EventStream의 만료 시간을 너무 길게 설정하는 것은 서버 입장에서 좋은 방법이 아닙니다. <br>
운영 레벨에서 긴 수명을 가진 EventStream이 차지하는 커넥션과 쓰레드는 잠재적인 퍼포먼스 저하 요소가 될 수 있기 때문입니다. <br>
또한, 서버 앞단의 로드 밸런서도 최대 연결 시간 설정에 제한이 있기 때문에 비지니스 로직을 고려하여 적절한 만료 시간을 정해야 합니다.
- 만료가 도래하면 EventStream의 새로운 생성을 요청하기 때문에 큰 흐름에서 EventStream은 유지됩니다.


## ⭐️ 구현 과정
### 1. SSE 구독
- SSE 구독 api를 호출하면 서버에서 만료시간을 지정하여 **SSE 연결을 수립하고 연결 객체를 반환**합니다.
- **최초의 SSE 연결 이후에 바로 한번 SSEEvent를 생성해서 클라이언트에 보내주어야** 합니다.
- 만료 시간까지 아무런 데이터도 보내지 않으면 재연결 요청시 `503 Service Unavailable 에러`가 발생할 수 있습니다.<br>
- 따라서 처음 SSE 연결 시 더미 데이터를 전달해주는 것이 안전합니다.

```java
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알람 관련 API", description = "알람 관련 API 입니다.")
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @PreAuthorize("hasAuthority('alarm.read')")
    @Operation(summary = "알람 sse 구독", description = "알람 sse 구독, sseEmitter객체 반환.")
    @GetMapping(value = "/notification/subscribe", produces = "text/event-stream")
    public SseEmitter notificationSubscribe(
        @ApiParam(hidden = true) @AuthenticationPrincipal UserDetails user,
        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
        HttpServletResponse response) {

        //nginx 리버스 프록시에서 버퍼링 기능으로 인한 오동작 방지
        response.setHeader("X-Accel-Buffering", "no");

        LocalDateTime now = CustomTimeUtils.nowWithoutNano();
        return notificationService.subscribe(user.getUsername(), lastEventId);
    }
}
```
```java
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AlarmService {
    @Value("${sse.timeout}")
    private String sseTimeout;
    private static final String UNDER_SCORE = "_";
    private static final String CONNECTED = "CONNECTED";
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final SSEInMemoryRepository sseRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public SseEmitter subscribe(String username, String lastEventId, LocalDateTime now) {
        Long userId = getUserByUsernameOrException(username).getId();
        SseEmitter sse = new SseEmitter(Long.parseLong(sseTimeout));
        String key = new SseRepositoryKeyRule(userId, SseEventName.ALARM_LIST,
            now).toCompleteKeyWhichSpecifyOnlyOneValue();

        sse.onCompletion(() -> {
            log.info("onCompletion callback");
            sseRepository.remove(key);
        });
        sse.onTimeout(() -> {
            log.info("onTimeout callback");
            //만료 시 Repository에서 삭제 되어야함.
            sse.complete();
        });

        sseRepository.put(key, sse);
        try {
            sse.send(SseEmitter.event()
                .name(CONNECTED)
                .id(getEventId(userId, now, SseEventName.ALARM_LIST))
                .data("subscribe"));
        } catch (IOException exception) {
            sseRepository.remove(key);
            log.info("SSE Exception: {}", exception.getMessage());
            throw new SseException(ErrorCode.SSE_SEND_ERROR);
        }

        // 중간에 연결이 끊겨서 다시 연결할 때, lastEventId를 통해 기존의 받지못한 이벤트를 받을 수 있도록 할 수 있음.
        // 한번의 알림이나 새로고침을 받으면 알림을 slice로 가져오기 때문에
        // 수신 못한 응답을 다시 보내는 로직을 구현하지 않음.
        return sse;
    }

    /**
     *  특정 유저의 특정 sse 이벤트에 대한 id를 생성한다.
     *  위 조건으로 여러개 정의 될 경우 now 로 구분한다.
     * @param userId
     * @param now
     * @param eventName
     * @return
     */
    private String getEventId(Long userId, LocalDateTime now, SseEventName eventName) {
        return userId + UNDER_SCORE + eventName.getValue() + UNDER_SCORE + now;
    }

    /**
     * redis pub시 userId와 sseEventName을 합쳐서 보낸다.
     * @param userId
     * @param sseEventName
     * @return
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
```

### 2. SseEmitter 저장
#### Inmemory ConcurrentHashMap.
- **SSE 연결 시 생성된 객체를 통해 클라이언트에 응답**을 보낼 수 있으므로 **유저마다 이 객체를 조회할 수 있도록 저장**해야 합니다.
- 이 때 **동시성 문제**가 발생할 수 있기 때문에 일반 HashMap 이 아니라 `ConcurrentHashMap`을 사용합니다.
```java
@Slf4j
@Component
public class SSEInMemoryRepository implements SSERepository{
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
    public List<SseEmitter> getListByKeyPrefix(String keyPrefix){
        return sseEmitterMap.keySet().stream()
            .filter(key -> key.startsWith(keyPrefix))
            .map(sseEmitterMap::get)
            .collect(Collectors.toList());
    }
    @Override
    public List<String> getKeyListByKeyPrefix(String keyPrefix){
        return sseEmitterMap.keySet().stream()
            .filter(key -> key.startsWith(keyPrefix))
            .collect(Collectors.toList());
    }
    @Override
    public void remove(String key) {
        sseEmitterMap.remove(key);
    }
}
```


#### key 값 구조
```java
@RequiredArgsConstructor
@EqualsAndHashCode
public class SseRepositoryKeyRule {
    private static final String UNDER_SCORE = "_";

    private final Long userId;
    private final SseEventName sseEventName;
    private final LocalDateTime createdAt;

    /**
     * SSEInMemoryRepository에서 사용될
     * 특정 user에 대한 특정 브라우저,특정 SSEEventName에
     * 대한 SSEEmitter를 찾기 위한 key를 생성한다.
     * @return
     */
    public String toCompleteKeyWhichSpecifyOnlyOneValue() {

        String createdAtString = createdAt == null ? "" : createdAt.toString();
        return userId + UNDER_SCORE + sseEventName.getValue() + UNDER_SCORE + createdAtString;
    }
}
```
- 또한 HTTP 1.1의 경우에 하나의 브라우저에서 6개까지의 연결을 할 수 있기 때문에 한명의 유저가 여러개의 SSE연결을 할 수 있습니다.
- 따라서 Key값은 유저 정보와 생성정보를 포함하여 한명의 유저에 대해서 구분할 수 있도록 합니다.

### 3. 응답 보내기
- 응답을 보낼 경우 sseRepository에서 `SseEmitter`를 찾고 **id, name, data를 정하여 보냅니다**.
- id의 경우 보통 마지막으로 보내진 응답을 구분하여 비처 발송되지 못한 응답을 **재응답**해주는 데에 활용합니다.
- name은 `SseEvent`를 구분합니다.
- data는 전송 내용을 의미하며 주의할 점은 객체가 아니라 `String`형태로 보내야 합니다.
```java
			SseEmitter emitter = sseRepository.get(key).get();
            try {
                emitter.send(SseEmitter.event()
                    .id(getEventId(userId, now, eventName))
                    .name(eventName.getValue())
                    .data(eventName.getValue()));
            } catch (IOException e) {
                sseRepository.remove(key);
                log.error("SSE send error", e);
                throw new SseException(ErrorCode.SSE_SEND_ERROR);
            }			
```


### 4. 클라이언트
- `EventSource`객체를 생성하면 자동적으로 Connection이 수립됨
- 'connect' 라는 name이 SseEvent name과 같을 경우 event 수신
```
const sse = new EventSource("https://{domain}/connect");

sse.addEventListener('connect', (e) => {
	const { data: receivedConnectData } = e;
	console.log('connect event data: ',receivedConnectData);  // "connected!"
});
```

# Redis pub/sub을 활용하여 SSE Scale out시 문제 해결
[Redis pub/sub 정리](https://github.com/hyewon218/myBlog/blob/master/docs/RedisPubSub.md)