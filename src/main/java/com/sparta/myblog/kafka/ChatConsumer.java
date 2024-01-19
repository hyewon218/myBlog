package com.sparta.myblog.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatConsumer {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatServiceImpl chatService;

    // 저장
    @KafkaListener(topics = "${kafka.topic.chat.name}", groupId = "${kafka.consumer.chat.rdb-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG
            + ":earliest"}, containerFactory = "kafkaListenerContainerFactoryChatRDB")
    public void createChatInRDBConsumerGroup(ChatMessageDto chatMessageDto) {
        log.info("createAlarmInRDBConsumerGroup");
        chatService.saveMessage(chatMessageDto.getRoomId(), chatMessageDto);
    }

    // 메세지 보냄
    @KafkaListener(topics = "${kafka.topic.chat.name}", groupId = "${kafka.consumer.chat.redis-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG
            + ":earliest"}, containerFactory = "kafkaListenerContainerFactoryChatRedis")
    public void redisPublishConsumerGroup(ChatMessageDto chatMessageDto) {
        log.info("redisPublishChatConsumerGroup");
        try {
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessageDto.getRoomId(),
                chatMessageDto); // Websocket 구독자에게 채팅 메시지 Send
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}