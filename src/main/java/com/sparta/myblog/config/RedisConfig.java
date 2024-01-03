package com.sparta.myblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sparta.myblog.redis.pubsub.RedisSubscriber;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching // 캐싱 기능을 활성화
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;
    private final RedisProperties redisProperties;
    private final RedisSubscriber redisSubscriber;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    /**
     * LettuceConnectionFactory 를 사용하여 RedisConnectionFactory 빈을 생성하여 반환
     *
     * @return RedisConnectionFactory 객체
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    // 메세지를 발송하기 위해서 RedisTemplate 정의
    /**
     * RedisTemplate 빈을 생성하여 반환
     *
     * @return RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() { // (3) pub
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        // 이 메소드를 빼면 실제 스프링에서 조회할 때는 값이 정상으로 보이지만
        // redis-cli 로 보면 key 값에 \xac\xed\x00\x05t\x00\x0 이런 값들이 붙는다.
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // GenericJackson2JsonRedisSerializer
        // 객체의 클래스 지정 없이 모든 Class Type 을 JSON 형태로 저장할 수 있는 Serializer
        // 여러 객체를 직렬화/역직렬화 사용할 수 있는
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        // redisTemplate.setKeySerializer(new StringRedisSerializer()); //Key: String
        return redisTemplate;
    }

    // pub/sub///////////////////////////////////////////////
    // redis 를 경청하고 있다가 메시지 발행(publish)이 오면 Listener 가 처리합니다.

    // 컨테이너 설정
    // container 빈으로 등록
    // MessageListenerContainer : JMS template 과 함께 스프링에서 JMS 메시징을 사용하는 핵심 컴포넌트
    // MDP(message-driven POJO)를 사용하여 비동기 메시지를 받는데 사용. 메시지의 수신관점에서 볼 떄 필요
    // MessageListener 를 생성하는 데 사용
    // receiver 등록 (메시지를 받아 처리하는 로직적인 부분)
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {  // (1)sub
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        // listenerAdapter 를 이용해서 Listener 지정
        // subscribe 할 topic 지정
        container.addMessageListener(listenerAdapter(), topic());

        return container;
    }

    // 리스너어댑터 설정
    // MessageListenerAdapter : 스프링에서 비동기 메시지를 지원하는 마지막 컴포넌트
    // 정해진 채널로 들어온 메시지를 처리할 action 을 정의.
    // MessageListenerAdapter 에서 subscriber 설정
    @Bean
    public MessageListenerAdapter listenerAdapter() { // (2)sub
        // delegate – the delegate object
        // defaultListenerMethod – method to call when a message comes
        return new MessageListenerAdapter(redisSubscriber, "onMessage");
    }

    // channelTopic() : redis 에서 pub/sub 할 채널을 지정해 줌
    @Bean
    public ChannelTopic topic() { // (4)
        return new ChannelTopic("chatroom");
    }


    ////cache////////////////////////////////////////////////////
    // Redis Cache 를 사용하기 위한 cache manager 등록
    // 커스텀 설정을 적용하기 위해 RedisCacheConfiguration 을 먼저 생성한다.
    // 이후 RedisCacheManager 를 생성할 때 cacheDefaults 의 인자로 configuration 을 주면 해당 설정이 적용된다.
    // RedisCacheConfiguration 설정
    // disableCachingNullValues - null 값이 캐싱될 수 없도록 설정한다. null 값 캐싱이 시도될 경우 에러를 발생시킨다.
    // entryTtl - 캐시의 TTL(Time To Live)를 설정한다. Duration class 로 설정할 수 있다.
    // serializeKeysWith - 캐시 Key 를 직렬화-역직렬화 하는데 사용하는 Pair 를 지정한다.
    // serializeValuesWith - 캐시 Value 를 직렬화-역직렬화 하는데 사용하는 Pair 를 지정한다. -> 가시성이 중요하지 않기 때문에 JdkSerializationRedisSerializer 사용
    // Value 는 다양한 자료구조가 올 수 있기 때문에 GenericJackson2JsonRedisSerializer 를 사용한다.

    // CacheManager 스프링 빈에 설정된 StringRedisSerializer 와 Jackson2JsonRedisSerializer 로 캐시 키는 문자열로 변경되어 저장되고, 캐시 데이터는 JSON 형식으로 변경되어 저장된다.
    // @param redisConnectionFactory Redis 와의 연결을 담당한다.
    @Bean(name = "cacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory cacheConnectionFactory) {
        GenericJackson2JsonRedisSerializer redisSerializer = getGenericJackson2JsonRedisSerializer();

        // Redis 캐시설정
        RedisCacheConfiguration redisConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
            .disableCachingNullValues()
            .entryTtl(Duration.ofMinutes(10)); // 캐시 지속 시간

        // 스프링에서 사용할 CacheManager 빈 생성 (spring-boot-starter-data-redis)
        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(cacheConnectionFactory)
            .cacheDefaults(redisConfiguration)
            .build();
    }

    private GenericJackson2JsonRedisSerializer getGenericJackson2JsonRedisSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator
            .builder()
            .allowIfSubType(Object.class)
            .build();

        // ObjectMapper 를 사용해서 LocalTimeDate 를 직렬화 하기 위해 매핑
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(
            objectMapper);
    }
}
