package com.sparta.myblog.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.service.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaChatroomConsumer {

    private final ChatServiceImpl chatService;

    // 저장
    // @KafkaListener : topic, groupId, containerFactory 세 개의 값을 통해 '카프카로부터' 값을 가져올 수 있다.
    @KafkaListener(
        topics = "chatroom",
        groupId = "${spring.kafka.consumer.chatroom.rdb-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG + ":earliest"},
        containerFactory = "kafkaListenerContainerFactoryChatRDB"
    ) // containerFactory 는 config 파일에서 설정한 bean
    public void createChatInRDBConsumerGroup(ChatMessageDto chatMessageDto) {
        log.info("createChatInRDBConsumerGroup");
        chatService.saveMessage(chatMessageDto.getRoomId(), chatMessageDto);
    }

    // 메세지 보냄
    @KafkaListener(
        topics = "chatroom",
        groupId = "${spring.kafka.consumer.chatroom.redis-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG + ":earliest"},
        containerFactory = "kafkaListenerContainerFactoryChatRedis"
    )
    public void redisPublishConsumerGroup(ChatMessageDto chatMessageDto) {
        log.info("redisPublishChatConsumerGroup");
        try {
            // redis pub
            chatService.sendChatMessage(chatMessageDto.getRoomId(), chatMessageDto);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}