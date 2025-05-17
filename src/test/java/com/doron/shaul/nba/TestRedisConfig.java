package com.doron.shaul.nba;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static com.doron.shaul.nba.NbaStatisticsIntegrationTest.redisContainer;

@TestConfiguration
class TestRedisConfig {
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisContainer.getHost());
        redisConfig.setPort(redisContainer.getMappedPort(6379));

        return new LettuceConnectionFactory(redisConfig);
    }
}
