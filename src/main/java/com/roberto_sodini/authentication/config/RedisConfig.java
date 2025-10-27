package com.roberto_sodini.authentication.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // Configurazione per gestire meglio le connessioni
        template.setDefaultSerializer(template.getStringSerializer());
        
        log.info("[REDIS CONFIG] StringRedisTemplate configurato con successo");
        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        log.info("[REDIS CONFIG] RedisTemplate configurato con successo");
        return template;
    }
}
