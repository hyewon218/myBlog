package com.sparta.myblog.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topic.alarm.name}")
    private String topicName;

    public void send(NotificationEvent notificationEvent) {
        kafkaTemplate.send(topicName, notificationEvent);
        log.debug("alarm kafka produce");
    }
}