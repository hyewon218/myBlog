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

    @Value("${kafka.bootstrapAddress}")
    private String bootstrapServers;

    @Value("${kafka.consumer.autoOffsetResetConfig}")
    private String autoOffsetResetConfig;

    @Value("${kafka.consumer.alarm.rdb-group-id}")
    private String rdbGroupId;

    @Value("${kafka.consumer.alarm.redis-group-id}")
    private String redisGroupId;

    @Value("${kafka.consumer.chat.rdb-group-id}")
    private String rdbChatGroupId;

    @Value("${kafka.consumer.chat.redis-group-id}")
    private String redisChatGroupId;

    // 알람
    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationRDBConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, rdbGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(NotificationEvent.class));
    }

    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationRedisConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, redisGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(NotificationEvent.class));
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
    public ConsumerFactory<String, ChatMessageDto> chatRedisConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, redisChatGroupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(ChatMessageDto.class));
    }

    // 알람
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactoryRDB() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationRDBConsumerFactory());
        return factory;
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
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactoryChatRDB() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatRDBConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactoryChatRedis() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatRedisConsumerFactory());
        return factory;
    }
}