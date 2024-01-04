package com.sparta.myblog.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

import com.sparta.myblog.service.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationServiceImpl notificationService;

    /**
     * offset을 최신으로 설정해야한다.
     * https://stackoverflow.com/questions/57163953/kafkalistener-consumerconfig-auto-offset-reset-doc-earliest-for-multiple-listene
     */
    @KafkaListener(topics = "${kafka.topic.alarm.name}", groupId = "${kafka.consumer.alarm.rdb-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG
            + ":earliest"}, containerFactory = "kafkaListenerContainerFactoryRDB")
    public void createAlarmInRDBConsumerGroup(NotificationEvent alarmEvent, Acknowledgment ack) {
        log.info("createAlarmInRDBConsumerGroup");
        notificationService.createNotification(alarmEvent.getUserId(), alarmEvent.getType(),
            alarmEvent.getArgs());
        ack.acknowledge();
    }

    @KafkaListener(topics = "${kafka.topic.alarm.name}", groupId = "${kafka.consumer.alarm.redis-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG
            + ":earliest"}, containerFactory = "kafkaListenerContainerFactoryRedis")
    public void redisPublishConsumerGroup(NotificationEvent alarmEvent, Acknowledgment ack) {
        log.info("redisPublishConsumerGroup");
        notificationService.send(alarmEvent.getUserId(), alarmEvent.getEventName());
        ack.acknowledge();
    }
}