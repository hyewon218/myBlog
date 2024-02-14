package com.sparta.myblog.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaNotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaNotificationTemplate;

    public void send(NotificationEvent notificationEvent) {
        kafkaNotificationTemplate.send("alarm", notificationEvent); //(토픽, 생성할 값)
        log.debug("alarm kafka produce");
    }
}