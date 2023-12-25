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
// 해당 채널로 메시지를 발행(publish)하는 Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    // 채팅
    public void publish(ChannelTopic topic, ChatMessageDto chatMessageDto){
        log.info("채팅방 : " + topic.getTopic() + " Message : " + chatMessageDto.getMessage());
        redisTemplate.convertAndSend(topic.getTopic(), chatMessageDto);
    }
}
