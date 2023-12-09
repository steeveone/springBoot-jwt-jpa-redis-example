package com.steeve.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class JedisConfig {
	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
		return new JedisConnectionFactory(config);
	}
	
	@Bean
	StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory)
	{
	    StringRedisTemplate template = new StringRedisTemplate();
	    template.setConnectionFactory(redisConnectionFactory);
	    return template;
	}
}
