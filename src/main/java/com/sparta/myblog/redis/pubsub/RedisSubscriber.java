package com.sparta.myblog.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.myblog.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// MessageListener 를 구현한 서비스 클래스
// MessageListener : Callback for processing received objects through Redis.
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 메세지를 수신하는 구독자 구현
     * 여러 서버에서 SSE 를 구현하기 위한 Redis Pub/Sub
     * Redis 메세지가 발행(publish)되면 대기하고 있던 onMessage 가 해당 메세지를 받아 처리
     * subscribe 해두었던 topic 에 publish 가 일어나면 메서드가 호출된다.
     * -> 최종적으로 RedisListener 를 구현한다. 실제로 메세지를 수신하게 되면 처리하는 로직이다.
     */
    @Override
    // onMessage() 메서드는 메시지를 구독(subscribe)했을 때 수행할 메서드
    public void onMessage(Message message, byte[] pattern) {

        log.info("Redis Pub/Sub message received: {}", message.toString());

        try{
            // redis 에서 발행된 데이터를 받아 deserialize
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());

            // ObjectMapper.readValue 를 사용해서 JSON 을 파싱해서 자바 객체(ChatMessageDto.Class)로 바꿔준다
            ChatMessageDto chatMessageDto = objectMapper.readValue(publishMessage, ChatMessageDto.class);

            log.info("Room - Message : {}", chatMessageDto.getMessage());

            // WebSocket 구독자에게 채팅 메세지 Send
            messagingTemplate.convertAndSend("/sub/chat/" + chatMessageDto.getRoomId(), chatMessageDto);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}