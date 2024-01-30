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
    // 저장
    // @KafkaListener : topic, groupId, containerFactory 세 개의 값을 통해 '카프카로부터' 값을 가져올 수 있다.
    // topics 를 통해서 수신하고 싶은 토픽을 선택할 수 있다.
    // containerFactory 를 통해서 원하는 팩토리를 불러올 수 있다. (config 에서 생성했었다.)
    // 이러한 작업을 줄여주는 것이 바로 kafka connector 이다.
    @KafkaListener(
        topics = "${kafka.topic.alarm.name}", // topics 속성을 사용하여 리스너가 메시지를 소비해야 하는 Kafka 주제를 지정할 수 있다. 리스너는 지정된 주제를 자동으로 구독한다..
        groupId = "${kafka.consumer.alarm.rdb-group-id}", // groupId 속성은 리스너가 속한 소비자 그룹을 지정하는 데 사용된다. 동일한 그룹 ID를 가진 Kafka 소비자는 구독 주제의 메시지 처리 작업을 공유하여 병렬성과 로드 밸런싱을 제공한다.
        properties = {AUTO_OFFSET_RESET_CONFIG + ":earliest"},
        containerFactory = "kafkaListenerContainerFactoryRDB" // containerFactory 속성을 사용하면 기본 Kafka 메시지 리스너 컨테이너를 생성하는 데 사용되는 Bean의 이름을 지정할 수 있습니다. 이를 통해 동시성, 승인 모드, 오류 처리 등 컨테이너의 다양한 속성을 유연하게 사용자 지정할 수 있습니다.
    ) // containerFactory 는 config 파일에서 설정한 bean
    public void createAlarmInRDBConsumerGroup(NotificationEvent alarmEvent, Acknowledgment ack) {
        log.info("createAlarmInRDBConsumerGroup");
        notificationService.createNotification(alarmEvent.getUserId(), alarmEvent.getType(),
            alarmEvent.getArgs());
        ack.acknowledge();
    }

    @KafkaListener(
        topics = "${kafka.topic.alarm.name}",
        groupId = "${kafka.consumer.alarm.redis-group-id}",
        properties = {AUTO_OFFSET_RESET_CONFIG + ":earliest"},
        containerFactory = "kafkaListenerContainerFactoryRedis"
    )
    public void redisPublishConsumerGroup(NotificationEvent alarmEvent, Acknowledgment ack) {
        log.info("redisPublishConsumerGroup");
        // redis pub
        notificationService.send(alarmEvent.getUserId(), alarmEvent.getEventName());
        ack.acknowledge();
    }
}