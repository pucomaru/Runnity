package com.runnity.stream.global.config;

import com.runnity.stream.socket.util.ChallengeEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // =====================
    // Redis Cache
    // =====================
    @Value("${spring.data.redis.cache.host}")
    private String cacheHost;

    @Value("${spring.data.redis.cache.port}")
    private int cachePort;

    @Bean(name = "redisCacheConnectionFactory")
    public RedisConnectionFactory redisCacheConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(cacheHost, cachePort);
        return new LettuceConnectionFactory(config);
    }

    // 스케줄러 서버용: String 직렬화
    @Bean(name = "stringRedisTemplate")
    public RedisTemplate<String, String> stringRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisCacheConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    // BroadcastRedisUtil 등 StringRedisTemplate 타입 의존성 대응용 추가 빈
    @Bean
    public StringRedisTemplate stringRedisTemplateBean(RedisConnectionFactory redisCacheConnectionFactory) {
        return new StringRedisTemplate(redisCacheConnectionFactory);
    }

    // BroadcastRedisUtil용: Object 직렬화
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisCacheConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // =====================
    // Redis Pub/Sub (이벤트 발행/수신용)
    // =====================
    @Value("${spring.data.redis.pubsub.host}")
    private String pubsubHost;

    @Value("${spring.data.redis.pubsub.port}")
    private int pubsubPort;

    @Bean(name = "redisPubsubConnectionFactory")
    public RedisConnectionFactory redisPubsubConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(pubsubHost, pubsubPort);
        return new LettuceConnectionFactory(config);
    }

    // Pub/Sub 발행용: String 직렬화
    @Bean(name = "pubsubRedisTemplate")
    public RedisTemplate<String, String> pubsubRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisPubsubConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // Pub/Sub 수신용: MessageListenerContainer
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisPubsubConnectionFactory,
            ChallengeEventListener challengeEventListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisPubsubConnectionFactory);

        container.addMessageListener(challengeEventListener, new PatternTopic("challenge:*:ready"));
        container.addMessageListener(challengeEventListener, new PatternTopic("challenge:*:running"));
        container.addMessageListener(challengeEventListener, new PatternTopic("challenge:*:done"));

        return container;
    }
}

