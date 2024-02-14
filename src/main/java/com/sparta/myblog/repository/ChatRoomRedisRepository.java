package com.sparta.myblog.repository;


import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.entity.ChatRoom;
import com.sparta.myblog.entity.ChatType;
import com.sparta.myblog.redis.pubsub.RedisChatroomPublisher;
import com.sparta.myblog.redis.pubsub.RedisChatroomSubscriber;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Repository;


@Log4j2
@RequiredArgsConstructor
@Repository
public class ChatRoomRedisRepository {

    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    private final RedisChatroomPublisher redisChatroomPublisher;
    // 구독 처리 서비스
    private final RedisChatroomSubscriber redisChatroomSubscriber;
    // Redis
    private final RedisTemplate<String, Object> redisChatroomTemplate;

    // TODO : redisTemplate 확인
    // @Resource 는 빈 이름을 통해 주입을 받는 어노테이션인데
    // 여기에 redisTemplate 빈 이름을 적어주고 원하는 Operations 를 타입으로 지정해주면 RestTemplate 을 거쳐서 가져올 필요없이 바로 주입받을 수 있다.
    // @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;
    // topic 이름으로 topic 정보를 가져와 메시지를 발송할 수 있도록 Map 에 저장
    // 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보
    // 서버별로 채팅방에 매치되는 topic 정보를 Map 에 넣어 roomId로 찾을수 있도록 한다.
    private Map<String, ChannelTopic> topics;
    // 채팅방(topic)에 발행되는 메시지를 처리할 Listener
    // topic 에 메시지 발행을 기다리는 Listener
    private final RedisMessageListenerContainer redisMessageListenerChatroomContainer;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisChatroomTemplate.opsForHash();
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

    // 채팅방 생성
    // 서버간 채팅방 공유를 위해 redis hash 에 저장한다.
    // redis 에 메세지 저장하기
    public void createChatRoom(ChatRoom chatRoom) {

        // ChatRoom 를 redis 에 저장하기 위하여 직렬화한다.
        redisChatroomTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatRoom.class));
        // redisChatroomTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(ChatRoom.class));

        String roomId = chatRoom.getId();

        // ⭐️ redis 의 hashes 자료구조
        // key : CHAT_ROOMS , filed : roomId, value : chatRoom
        opsHashChatRoom.put(CHAT_ROOMS, roomId, chatRoom);

        // 신규 Topic 을 생성하고 Listener 등록 및 Topic Map 에 저장
        ChannelTopic topic = new ChannelTopic(roomId);

        redisMessageListenerChatroomContainer.addMessageListener(redisChatroomSubscriber, topic);

        topics.put(roomId, topic);
    }

    // 채팅방 입장 (subscribe) : redis 에 topic 을 만들고 pub/sub 통신을 하기 위해 리스너를 설정
    public void enterChatRoom(String roomId) {

        ChannelTopic topic = topics.get(roomId);

        log.info("레디스 topic 확인 : "+ topic);

        if (topic == null) {
            topic = new ChannelTopic(roomId);
        }
        redisMessageListenerChatroomContainer.addMessageListener(redisChatroomSubscriber, topic);

        topics.put(roomId, topic);
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    // 특정 Topic 에 메시지 발행 (publish)
    public void pushMessage(String roomId, ChatMessageDto messageDto) {

        // roomId 를 통해 (생성, 입장 시 redis 에 저장된) topic 을 얻는다.
        ChannelTopic topic = topics.get(roomId);

        redisChatroomPublisher.publish(topic,
            ChatMessageDto.builder()
                .sender(messageDto.getSender())
                .roomId(roomId)
                .message(messageDto.getMessage())
                .type(ChatType.TALK)
                .build());

        log.info("레디스 서버 특정 Topic 에 메세지 전송 완료");
    }

    // Topic 삭제 후 Listener 해제, Topic Map 에서 삭제
    public void deleteRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        redisMessageListenerChatroomContainer.removeMessageListener(redisChatroomSubscriber, topic);
        topics.remove(roomId);
    }
}

