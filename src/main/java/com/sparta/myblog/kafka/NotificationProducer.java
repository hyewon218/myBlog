package com.sparta.myblog.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void send(NotificationEvent notificationEvent) {
        kafkaTemplate.send("alarm", notificationEvent);
        log.debug("alarm kafka produce");
    }
}