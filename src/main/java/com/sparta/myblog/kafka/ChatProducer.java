package com.sparta.myblog.kafka;

import com.sparta.myblog.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatProducer {

    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

    @Value("${kafka.topic.chat.name}")
    private String topicName;

    public void send(ChatMessageDto chatMessageDto) {
        kafkaTemplate.send(topicName, chatMessageDto);
        log.debug("chat kafka produce");
    }
}