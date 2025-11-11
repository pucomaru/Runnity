package com.runnity.global.config;

import com.runnity.challenge.listener.ChallengeEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
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

    // Cache용: String 직렬화
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

    // Spring Cloud가 요구하는 기본 redisTemplate 빈
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        return stringRedisTemplate();
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
            ChallengeEventListener challengeEventListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisPubsubConnectionFactory());

        // challenge:*:ready 패턴 구독
        container.addMessageListener(challengeEventListener, new PatternTopic("challenge:*:ready"));
        // challenge:*:running 패턴 구독
        container.addMessageListener(challengeEventListener, new PatternTopic("challenge:*:running"));
        // challenge:*:done 패턴 구독
        container.addMessageListener(challengeEventListener, new PatternTopic("challenge:*:done"));

        return container;
    }
}

