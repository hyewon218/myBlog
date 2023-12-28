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

    private final RedisTemplate<String, Object> redisTemplate;

    // 채팅
    public void publish(ChannelTopic topic, ChatMessageDto chatMessageDto){

        log.info("채팅방 : " + topic.getTopic() + " Message : " + chatMessageDto.getMessage());

        // 해당 채널로 메시지를 발행(publish) -> 대기하고 있던 onMessage 가 해당 메세지를 받아 처리
        redisTemplate.convertAndSend(topic.getTopic(), chatMessageDto);

        log.info("redisTemplate : " + redisTemplate);
    }
}
