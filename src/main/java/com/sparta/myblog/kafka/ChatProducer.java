package com.sparta.myblog.kafka;

import com.sparta.myblog.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatProducer {

    private final KafkaTemplate<String, ChatMessageDto> kafkaChatTemplate;

    public void send(ChatMessageDto chatMessageDto) {
        kafkaChatTemplate.send("chat", chatMessageDto);
        log.debug("chat kafka produce");
    }
}