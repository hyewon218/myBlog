package com.sparta.myblog.config;

import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.kafka.NotificationEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrapAddress}")
    private String bootstrapServers;

    /**
     * In-Sync-Replica 에 모두 event 가 저장되었음이 확인 되어야 ack 신호를 보냄.
     * 가장 성능은 떨어지지만 event produce 를 보장할 수 있음.
     */
    @Value("${kafka.producer.acksConfig}")
    private String acksConfig;

    /*TODO : 알람과 채팅 분리?*/
    private Map<String, Object> producerFactoryConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.ACKS_CONFIG, acksConfig);
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // KEY 는 메세지를 보내면 토픽의 파티션이 지정될 때 쓰인다.
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }

    // 알람
    @Bean
    public ProducerFactory<String, NotificationEvent> producerNotificationFactory() {
        Map<String, Object> configProps = producerFactoryConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, NotificationEvent> kafkaNotificationTemplate() {
        return new KafkaTemplate<>(producerNotificationFactory());
    }


    // 채팅
    @Bean
    public ProducerFactory<String, ChatMessageDto> producerChatFactory() {
        Map<String, Object> configProps = producerFactoryConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public KafkaTemplate<String, ChatMessageDto> kafkaChatTemplate() {// kafka에 데이터를 주고 받기 위한 template을 생성해준다. kafka는 key,value 형식의 저장소임으로 key는 String, value는 ChatDto 를 사용한다.
        return new KafkaTemplate<>(producerChatFactory());// template을 실질적으로 생성해주는 producerFactory를 넣어준다.
    }
}