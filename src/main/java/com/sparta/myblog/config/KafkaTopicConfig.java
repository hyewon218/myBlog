package com.sparta.myblog.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

/**
     * broker 를 두개만 설정하였으므로 최소 Replication Factor 로 2를 설정하고
     * Partition 의 경우 Event 의 Consumer 인 WAS 를 2대까지만 실행되도록 해두었기 때문에 2로 설정함.
     * 이보다 Partition 을 크게 설정한다고 해서 Consume 속도가 빨라지지 않기 때문이다.*/

    @Bean
    public NewTopic newNotificationTopic() {
        return new NewTopic("alarm", 2, (short) 2);
    }

    @Bean
    public NewTopic newChatTopic() {
        return new NewTopic("chatroom", 2, (short) 2);
    }
}
