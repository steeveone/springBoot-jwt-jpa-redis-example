package com.example.restservice.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.restservice.component.JwtProvider;

@Service
public class JwtRedisService {

	@Autowired
    private StringRedisTemplate redisTemplate;
	private final Logger logger = LoggerFactory.getLogger(JwtRedisService.class);
	
	public void addToken(String subject, String token)
	{
		logger.trace("add token for subject [ " +subject+ " ] [ " +token+ " ]");
		redisTemplate.opsForList().leftPush(subject, token);
	}
	
	public void removeToken(String subject, String token)
	{
		logger.trace("delete token for subject [ " +subject+ " ] [ " +token+ " ]");
		redisTemplate.opsForList().remove(subject, 1, token);
		if (redisTemplate.opsForList().size(subject).equals(0L))
		{
			logger.trace("remove subject [ " +subject+ " ] ");
			redisTemplate.delete(subject) ;
		}
	}
	
	public boolean checkToken(String subject, String token)
	{
		boolean valid = redisTemplate.opsForList().indexOf(subject, token) != null;
		logger.trace("check token for subject [ " +subject+ " ] [ " +token+ " ] return [ " +valid + " ]");
		return valid;
	}
	
	public void removeAllTokens(String subject)
	{
		logger.trace("deleting all tokens for subject [ " + subject + " ]");
		redisTemplate.delete(subject) ;
	}
	
	@Scheduled(fixedRate = 1000*30)
	public void evictExpiredTokens()
	{
		logger.info("evictExpiredTokens service");
		redisTemplate.keys("*").forEach(key->{
			long evicted = 0;
			logger.trace("found key [ " + key + " ]");
			long keySize = redisTemplate.opsForList().size(key);
			List<String> toRemove = new ArrayList<String>();
			for (long i = 0; i < keySize; i++)
			{
				String jwtToken = redisTemplate.opsForList().index(key, i);
				try
				{
					JwtProvider.verifyJwt(jwtToken);
					logger.trace("found a valid token");
				}
				catch (TokenExpiredException ex)
				{
					logger.trace("found a invalid token");
					//removeToken(key, jwtToken);
					toRemove.add(jwtToken);
					evicted++;
				}
			}
			toRemove.forEach(tkn->{
				removeToken(key, tkn);
			});
			logger.debug("evicted " + evicted + " sessions for user [ " + key + " ] ");
		});
	}
}
