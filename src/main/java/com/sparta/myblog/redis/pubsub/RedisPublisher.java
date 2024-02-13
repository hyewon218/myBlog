package com.sparta.myblog.redis.pubsub;

import com.sparta.myblog.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisChatroomTemplate;

    // 채팅
    // publish() 메소드는 외부로부터 ChannelTopic 을 받아와, 해당 채널로 메시지를 발행한다.
    // 메시지 발행에는 RedisTemplate 의 convertAndSend() 메소드가 사용된다.
    public void publish(ChannelTopic topic, ChatMessageDto chatMessageDto){

        log.info("채팅방 : " + topic.getTopic() + " Message : " + chatMessageDto.getMessage());

        // 해당 채널로 메시지를 발행(publish) -> 대기하고 있던 onMessage 가 해당 메세지를 받아 처리
        redisChatroomTemplate.convertAndSend(topic.getTopic(), chatMessageDto);

        log.info("redisTemplate : " + redisChatroomTemplate);
    }
}
