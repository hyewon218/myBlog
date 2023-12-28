## 변경사항
• 채팅방 id String으로 수정 -> redis에 저장하려면 String으로 해줘야한다고 함



## 📍Redis Pub/Sub 란?
![66D3C3A3-420D-4FC8-B23B-51D02E3B2BF1](https://github.com/JihyeChu/PetNexus/assets/126750615/c1475ca6-a765-4c20-ae0d-e8c7996ce07f)
> Publish / Subscribe 란 특정한 주제(topic)에 대하여 해당 topic을 구독한 모두에게 메시지를 발행하는 통신 방법으로 채널을 구독한 수신자(클라이언트) 모두에게 메세지를 전송 하는것을 의미한다. 하나의 Client가 메세지를 Publish하면, 이 Topic에 연결되어 있는 다수의 클라이언트가 메세지를 받을 수 있는 구조이다.
Publish / Subscribe 구조에서 사용되는 Queue를 일반적으로 Topic이라고 한다.
그래서 레디스의 pub/sub 기능은 은 주로 채팅 기능이나, 푸시 알림등에 사용된다. 
다만 유의할점이 있는데, 이러한 redis의 pub/sub 시스템은 **매우 단순한 구조**로 되어있다는 것이다. 
Pub/Sub 시스템에서는 채널에 구독 신청을 한 모든 subscriber에게 메시지를 전달한다.  
그런데 메시지를 "던지는" 시스템이기 때문에, **메시지를 따로 보관하지도 않는다**. 
즉, 수신자(클라이언트)가 메세지를 받는 것을 보장하지 않아, 
**subscribe 대상이 하나도 없는 상황**에서 메시지를 publish해도 역시 사라진다. 
그래서 일반 메시지큐처럼 수신 확인을 하지 않는다.**(전송 보장을 하지 않음)**

### 그럼에도 사용하는 이유?
> 웹소켓을 이용할경우 추가적인 네트워크 통신이 필요하기에 레이턴시(딜레이)가 약간 생길수 있다. 반면 레디스는 In-Memory 기반이라 매우 바르게 메세지를 받을 수 있다. 따라서 현재 접속 중인 클라이언트에게 짧고 간단한 메시지를 빠르게 보내고 싶을 때, 그리고 전송된 메시지를 따로 저장하거나 수신확인이 필요 없을 때, 마지막으로 100% 전송 보장은 하지 않아도 되는 데이터를 보낼때 이용하면 괜찮다.

![E3D9ACB2-4D7C-49A6-9828-4CEBF719FE2A](https://github.com/JihyeChu/PetNexus/assets/126750615/d7e57aa9-71cf-4d64-a255-197a9d8e50eb)
> ### In-Memory 기반 Message Broker 문제점
> 사실 Spring에서 제공하는 STOMP를 활용하고도, 내장된 Simple Message Broker를 사용해 채팅 서버를 구현할 수 있다.
하지만 Simple Message Broker 같은 경우 스프링 부트 서버의 **내부 메모리에서 동작**하게 된다.
인메모리 기반 브로커를 사용했을 때의 문제점은 다음과 같다.
> - 서버가 down되거나 재시작을 하게되면 Message Broker(메시지 큐)에 있는 데이터들은 유실될 수 있다.
> - 다수의 서버일 경우 서버간 채팅방을 공유할 수 없게 되면서 다른 서버간에 있는 사용자와의 채팅이 불가능 해진다.

```
⭐️이러한 문제들을 해결하기 위해서는 Message Broker가 여러 서버에서 접근할 수 있도록 개선이 필요하다. 
즉, 외부 메시지 브로커를 연동한다면 인프라 비용은 좀 더 증가하겠지만 위 문제들을 해결할 수 있게 된다!
```


## Redis Pub/Sub 모델 사용하기
**Message 를 처리하기 위한 Listener**
Listener 에서는 전달 받은 Message 를 SimpMessageSendingOperations 를 이용해 현재 소켓에 연결돼 있는 사용자에게 메시지를 전파한다.
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 여러 서버에서 SSE 를 구현하기 위한 Redis Pub/Sub
     * Redis 메세지가 발행(publish)되면 대기하고 있던 onMessage 가 해당 메세지를 받아 처리
     * subscribe 해두었던 topic 에 publish 가 일어나면 메서드가 호출된다.
     */
    @Override
    public void onMessage(Message message,byte[] pattern) {
        log.info("Redis Pub/Sub message received: {}", message.toString());
        try{
            // redis 에서 발행된 데이터를 받아 deserialize
            String publishMessage = (String)redisTemplate.getStringSerializer().deserialize(message.getBody());
            log.info(publishMessage);
            // ChatRoom 객체로 매핑
            ChatMessageDto chatMessageDto = objectMapper.readValue(publishMessage, ChatMessageDto.class);
            //ChatRoom roomMessage = objectMapper.readValue(publishMessage, ChatRoom.class);
             log.info("Room - Message : {}", chatMessageDto.getRoomId());
            // WebSocket 구독자에게 채팅 메세지 Send
            messagingTemplate.convertAndSend("/sub/chat/" + chatMessageDto.getRoomId(), chatMessageDto);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
```
`MessageListenerAdapter` 객체를 이용해 Message 를 처리할 Listner 를 설정해준다.<br>
`RedisMessageListenerContainer` 객체를 이용해 Topic 과 Listner 를 연결해준다.<br>
Topic 에 Message 가 생성되면 `MessageListner` 가 해당 Message 를 처리한다.


## Redis 설정 하기
```java
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;
    private final RedisProperties redisProperties;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }

    //pub/sub///////////////////////////////////////////////
    // redis 를 경청하고 있다가 메시지 발행(publish)이 오면 Listener 가 처리합니다.
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        MessageListenerAdapter listenerAdapter,
        ChannelTopic channelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, channelTopic);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }
```

## Redis 에 저장 및 불러오기 위한 Repository 정의
`HashOperations` : Redis 에 Map 형태 (Key, Value) 로 데이터에 접근 및 저장을 하기 위한 객체<br> 
- Hashes :  **Value**가 **Map 자료구조와 같은 Key/Value 형태**가 됨<br>
    - 하나의 Key에 여러개의 필드를 갖는 구조가 됨


`MessageListenerContainer` : JMS(Java Message Service) template과 함께 스프링에서 JMS메시징을 사용하는 핵심 컴포넌트.<br> 
MDP(message-driven POJO)를 사용하여 비동기 메시지를 받는데 사용. <br>
메시지의 수신관점에서 볼 때 필요. MessageListener를 생성하는 데 사용

```java
@Log4j2
@RequiredArgsConstructor
@Repository
public class ChatRoomRedisRepository {

    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    private final RedisPublisher redisPublisher;
    // 구독 처리 서비스
    private final RedisSubscriber redisSubscriber;
    // Redis
    private final RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;
    // 채팅방(topic)에 발행되는 메시지를 처리할 Listener
    // topic 에 메시지 발행을 기다리는 Listener
    private final RedisMessageListenerContainer redisMessageListener;
    // topic 이름으로 topic 정보를 가져와 메시지를 발송할 수 있도록 Map 에 저장
    // 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보
    // 서버별로 채팅방에 매치되는 topic 정보를 Map 에 넣어 roomId로 찾을수 있도록 한다.
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        // topic 정보를 담을 Map 을 초기화
        topics = new HashMap<>();
    }

    // 모든 채팅방 조회
    public List<ChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    // 특정 채팅방 조회
    public ChatRoom findRoomById(String roomId) {
        return opsHashChatRoom.get(CHAT_ROOMS, roomId);
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash 에 저장한다.
    public void createChatRoom(ChatRoomRequestDto requestDto, User user) {
        ChatRoom chatRoom = requestDto.toEntity(user);
        String roomId = chatRoom.getId();
        
        opsHashChatRoom.put(CHAT_ROOMS, roomId, chatRoom);

        // 신규 Topic 을 생성하고 Listener 등록 및 Topic Map 에 저장
        ChannelTopic topic = new ChannelTopic(roomId);
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        topics.put(roomId, topic);
    }

    // 채팅방 입장 : redis 에 topic 을 만들고 pub/sub 통신을 하기 위해 리스너를 설정
    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
        }
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        topics.put(roomId, topic);
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    // 특정 Topic 에 메시지 발행
    public void pushMessage(String roomId, ChatMessageDto messageDto) {
        ChannelTopic topic = topics.get(roomId);
        redisPublisher.publish(topic,
            ChatMessageDto.builder()
                .sender(messageDto.getSender())
                .roomId(roomId)
                .message(messageDto.getMessage())
                .type(ChatType.TALK)
                .build());
    }

    // Topic 삭제 후 Listener 해제, Topic Map 에서 삭제
    public void deleteRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        redisMessageListener.removeMessageListener(redisSubscriber, topic);
        topics.remove(roomId);
    }
```

## 전달 받은 Message 를 Redis 로 보내기
```java
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final TradeChatRepository tradeChatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TradeChatRoomRepository tradeChatRoomRepository;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final RedisSubscriber redisSubscriber;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    // 메세지 삭제 - DB Scheduler 적용 필요

    // 채팅방에 메시지 발송
    @Override
    @Transactional
    public void sendChatMessage(String roomId, ChatMessageDto messageDto) {
        messageDto.setType(ChatType.TALK);
        saveMessage(roomId, messageDto);
        redisMessageListenerContainer.addMessageListener(redisSubscriber,
            chatRoomRedisRepository.getTopic(roomId));

        // Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
        chatRoomRedisRepository.pushMessage(roomId, messageDto);
    }
```

## Message 요청 처리하기
@MessageMapping 를 이용해 메시지 요청을 받기 위한 경로를 설정한다.
```java
@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/message 에 메세지가 오면 동작
    // 채팅방에 발행된 메시지는 서로 다른 서버에 공유하기 위해 redis 의 Topic 으로 발행
    @MessageMapping("chat/message/{roomId}") // 오픈채팅
    public ChatMessageDto message(@DestinationVariable String roomId, ChatMessageDto messageDto) {
        chatService.sendChatMessage(roomId, messageDto);
        return ChatMessageDto.builder()
            .roomId(roomId)
            .sender(messageDto.getSender())
            .message(messageDto.getMessage())
            .build();
    }
```



## 로직 설명

1. StompHandler
    1. 클라이언트 로직 : 쿠키에서 키값이 Authorization 인 것 찾아 헤더에 Authorization 토큰값 넣어주기
    2. 엔드포인트로 연결
    3. 클라이언트 stompClient.onConnect - *StompCommand*.CONNECT : 연결됨
    4. websocket 연결시 헤더의 토큰값(jwt) 유효성 검증
    5. 클라이언트 stompClient.subscribe - *StompCommand*.SUBSCRIBE : 채팅룸 구독요청
    6. header 정보에서 구독 destination 정보를 얻고, roomId를 추출
    7. 클라이언트 입장 메시지를 채팅방에 발송(redis publish) - html 에 보여주기
    8. Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
2.  ChatRoomRedisRepository<br>
    a. 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash 에 저장한다.
     ![C9D86211-1592-4837-B93B-D05B1470BE80_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/19defc02-9c97-489e-97e1-d3c02810353f)
    
    b. 채팅방 입장 : redis 에 topic 을 만들고 pub/sub 통신을 하기 위해 리스너를 설정
     ![2C9CEACE-92D2-4B95-97BF-1C021152D7F7](https://github.com/hyewon218/kim-jpa2/assets/126750615/88d70b63-3b6d-4b0e-a3c6-a3e1211961d3)

    c. 신규 Topic 을 생성하고 Listener 등록 및 Topic Map 에 저장
    - Topic Map : topic 이름으로 topic 정보를 가져와 메시지를 발송할 수 있도록 Map 에 저장, 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보, 서버별로 채팅방에 매치되는 topic 정보를 Map 에 넣어 roomId로 찾을수 있도록 한다.
    - addListener - 구독자,채팅방 (topic이 String이어야) 짝지어 등록
    - redisSubscriber - RedisSubscriber 클래스의 onMessage 로
      -  **여러 서버에서 SSE 를 구현하기 위한 Redis Pub/Sub Redis**
      -  메세지가 발행(publish)되면 대기하고 있던 `onMessage` 가 해당 메세지를 받아 처리
      -  subscribe 해두었던 topic 에 publish 가 일어나면 메서드가 호출된다.

3. RedisSubscriber
    1. 여러 서버에서 SSE 를 구현하기 위한 Redis Pub/SubRedis
    2. 메세지가 발행(publish)되면 대기하고 있던 onMessage 가 해당 메세지를 받아 처리
    3. subscribe 해두었던 topic 에 **publish** 가 일어나면 메서드가 호출된다. (*RedisPublisher → RedisSubscriber*)
    4. onMessage
       a. redis 에서 발행된 데이터를 받아 deserialize<br>
       b. ChatRoom 객체로 매핑<br>
       c. WebSocket 구독자에게 채팅 메세지 Send

4. 채팅 입력
    1. */pub/chat/message - ChatController의 message()*
    2. DB에 메세지 저장
    3. 채팅방에 발행된 메시지는 서로 다른 서버에 공유하기 위해 redis 의 Topic(채팅방 입장할 때 만들었던) 으로 발행
    4. Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
        1. *ChatRoomRedisRepository의 pushMessage()*
            1. *RedisPublisher의 publish()*
                1. Template.convertAndSend(*topic*.getTopic(), *chatMessageDto*);}
                    1. **roomId를 통해 얻은 topic에 메세지 전달**
                        1. *RedisSubscriber의 onMessage()*

## 채팅방에서의 pub/sub 컨셉
- 채팅방 생성 : pub / sub 구현을 위한 Topic이 생성됨
- 채팅방 입장 : Topic 구독
- 채팅방에서 메시지를 송수신 : 해당 Topic으로 메세지를 송신(pub), 메세지를 수신(sub)