package com.sparta.myblog.config;

import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.kafka.NotificationEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetResetConfig;

    @Value("${spring.kafka.consumer.alarm.rdb-group-id}")
    private String rdbNotificationGroupId;

    @Value("${spring.kafka.consumer.alarm.redis-group-id}")
    private String redisNotificationGroupId;

    @Value("${spring.kafka.consumer.chat.rdb-group-id}")
    private String rdbChatGroupId;

    @Value("${spring.kafka.consumer.chat.redis-group-id}")
    private String redisChatGroupId;

    // 알람
    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationRDBConsumerFactory() { // Topic 에 접속하기 위한 정보를 가진 Factory 빈 생성
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, rdbNotificationGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(NotificationEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactoryRDB() { // Topic 에 변경사항이 존재하는 이벤트를 리스닝하는 빈
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>(); // @KafkaListener 가 붙은 메서드의 컨테이너를 빌드(생성)해준다.
        factory.setConsumerFactory(notificationRDBConsumerFactory()); // 이때, 설정정보가 담긴 ConsumerFactory 를 setting 할 수 있다.
        return factory;
    }

    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationRedisConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, redisNotificationGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(NotificationEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactoryRedis() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationRedisConsumerFactory());
        return factory;
    }


    // 채팅
    @Bean
    public ConsumerFactory<String, ChatMessageDto> chatRDBConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, rdbChatGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(ChatMessageDto.class));
    }

    @Bean
    // ConcurrentKafkaListenerContainerFactory : // 1개 이상의 consumerFactory 를 사용하는 multi thread
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactoryChatRDB() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatRDBConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ChatMessageDto> chatRedisConsumerFactory() {// 소비자 consumer 의 설정 값을 설정하여 cunsumerFactory를 생성한다. containerFactory() 메소드와 동일하다.
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, redisChatGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(ChatMessageDto.class));// 역직렬화 할 value 값이 object 이기 때문에 직접 주입해줘야 오류가 발생하지 않는다.
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactoryChatRedis() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatRedisConsumerFactory());
        return factory;
    }
}