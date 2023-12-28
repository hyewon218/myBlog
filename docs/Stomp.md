# Stomp
STOMP 는 Simple Text Oriented Messaging Protocol 의 약자로 메시지 전송을 위한 프로토콜이다. 기본적인 Websocket 과 가장 크게 다른 점은 기존의 Websocket 만을 사용한 통신은 발신자와 수신자를 Spring 단에서 직접 관리를 해야만 했다.

그러나 STOMP 는 다르다. stomp 는 pub/sub 기반으로 동작하기 때문에 메시지의 송신, 수신에 대한 처리를 명확하게 정의 할 수 있다. 즉 추가적으로 코드 작업할 필요 없이 @MessagingMapping 같은 어노테이션을 사용해서 메시지 발행 시 엔드포인트만 조정해줌으로써 훨씬 쉽게 메시지 전송/수신이 가능하다.

->메시지 브로커를 활용하여 pub/sub 방식으로 클라이언트와 서버가 쉽게 메시지를 주고 받을 수 있다.

## STOMP in Spring Boot

STOMP를 사용하기 위해서는 여러가지 설정이 필요하다.

1. Gradle 추가
    - 여기서 WebSocket과 함께 STOMP 관련 라이브러리도 함께 받아와진다.
2. Config 추가 및 설정
    - Config에서 Soket 연결, SUBSCRIBE 연결 설정, PUBLISH 설정을 해주어야 한다.
3. Message 컨트롤러 생성
    - Config에서 설정해준 URI로 요청이 메세지 요청이 오면 해당 컨트롤러로 매핑이 된다.


## Gradle 설정
```java
// websocket
implementation 'org.springframework.boot:spring-boot-starter-websocket'
// STOMP
implementation group: 'org.webjars', name: 'stomp-websocket', version: '2.3.3-1'
```

## Config 설정
```java
@EnableWebSocketMessageBroker
@Configuration
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp/chat") // 여기로 웹소켓 생성
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 발행하는 요청 url -> 메시지를 보낼 때
        registry.setApplicationDestinationPrefixes("/pub"); // 구독자 -> 서버(메세지보낼때)
        // 메시지를 구독하는 요청 url -> 메시지를 받을 때
        registry.enableSimpleBroker("/sub"); // 브로커 -> 구독자들(메세지받을때)
    }
}
```
- `@EnableWebSocketMessageBroker :` 메시지 브로커가 지원하는 ‘WebSocket 메시지 처리’를 활성화한다.
- `registerStompEndpoints()` : 기존의 WebSocket 설정과 마찬가지로 HandShake와 통신을 담당할 EndPoint를 지정한다. <br>
클라이언트에서 서버로 WebSocket 연결을 하고 싶을 때,`/stomp/chat`으로 요청을 보내도록 하였다.
- `setAllowedOriginPatterns` : cors 설정
- `configureMessageBroker()` : 메모리 기반의 Simple Message Broker를 활성화한다. <br>
메시지 브로커는`/sub`으로 시작하는 주소의 Subscriber들에게 메시지를 전달하는 역할을 한다. <br>
이때, 클라이언트가 서버로 메시지 보낼 때 붙여야 하는 prefix를 `/pub`로 지정하였다.
    - `setApplicationDestinationPrefixes`:
        - 메세지를 보낼 때, 관련 경로를 설정해주는 함수이다.
        - client에서`SEND`요청을 처리한다.
        - 클라이언트가 메세지를 보낼 때, 경로 앞에 `/pub`가 붙어있으면 Broker로 보내진다.
          ![B4DA5CC8-B3BB-4EE6-8A7D-44D68540CCC0_4_5005_c](https://github.com/JihyeChu/PetNexus/assets/126750615/8993e906-bf98-48d7-bb46-d6fed3c900a2)
    - `enableSimpleBroker`:
        - 메세지를 받을 때, **경로를 설정**해주는 함수이다.
        - 내장 브로커를 사용하겠다는 설정이다.
        -  `/sub`가 api에 prefix로 붙은 경우, messageBroker가 해당 경로를 가로채 처리한다.
        - 해당 경로(`/sub`)로`SimpleBroker`를 등록한다.`SimpleBroker`는 해당하는 경로를 구독하는 client에게 메시지를 전달하는 간단한 작업을 수행한다.
          ![41C287C2-573F-4DE6-B65E-7AFD85B288E5_4_5005_c](https://github.com/JihyeChu/PetNexus/assets/126750615/f598a54c-4ef8-4efe-b9ab-ed0caa3361ff)


## Message Controller 생성
```java
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {
    private final SimpMessagingTemplate template; //특정 Broker 로 메세지를 전달
    private final ChatService chatService;

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/enter 에 메세지가 오면 동작
    @MessageMapping(value = "/chat/enter")
    public void enter(ChatRequestDto message){ // 채팅방 입장
        message.setMessage(message.getWriter() + "님이 채팅방에 참여하였습니다.");
        template.convertAndSend("/sub/chat/" + message.getRoomId(), message);
    }

    // /pub/chat/message 에 메세지가 오면 동작
    @MessageMapping(value = "/chat/message")
    public void message(ChatRequestDto message){
        ChatResponseDto savedMessage = chatService.saveMessage(message);
        template.convertAndSend("/sub/chat/" + savedMessage.getRoomId(), savedMessage);
    }
```
- `@Controller`에서는 `/pub` desination prefix를 제외한 경로 `/chat/message`를 `@MessageMapping`하면 된다.

- `/chat/message` : Config에서 setApplicationDestinationPrefixes()를 통해 prefix를 `/pub` 으로 설정 해주었기 때문에 경로가 한번 더 수정되어 `/pub/chat/message`로 바뀐다.
- `convertAndSend` : `/sub`을 Config에서 설정해주었다. 그래서 Message Broker가 해당 send를 캐치하고 해당 토픽을 구독하는 모든 사람에게 메세지를 보내게 된다.
    - 예를 들어 **roomId** 2번을 구독하고 있다면 `/sub/chat/2`을 구독하고 있는 유저들에게 모두 보낸다.
      ![41C287C2-573F-4DE6-B65E-7AFD85B288E5_4_5005_c](https://github.com/JihyeChu/PetNexus/assets/126750615/d0408c64-45d0-4b2a-b85a-158b0a7b6cd7)



---
## 테스트 결과

postman은 웹소켓과 Socket.io는 지원하지만 STOMP 테스트를 지원하지 않아 apic 이라는 별도의 프로그램을 사용하여 로컬 환경에서 웹소켓 테스트를 할 수 있었습니다.
![F525A2F7-C0D8-4B62-A935-7A7D19141B29](https://github.com/JihyeChu/PetNexus/assets/126750615/00e80158-13d9-431f-ba38-cf6f9c0d58b5)
![386BD649-7D6C-49D6-AB61-575F21F73BC1](https://github.com/JihyeChu/PetNexus/assets/126750615/d5dbce58-f45c-49e1-b7c3-d36437693e6f)

1. http://localhost:8080/stomp/chat - 웹소켓 엔드포인트 설정
2. Subscription URL : /sub/chat/2  - 구독 URL 설정
3. Destination Queue : /pub/chat/message - 메세지 발송 URL 설정
4. 메세지 DB 저장 후 sub/chat/2 를 구독한 사람들에게 메세지가 보여진다.