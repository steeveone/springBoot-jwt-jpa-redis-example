package com.example.restservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/public/redis")
public class RedisTestController {

	@Autowired
    private StringRedisTemplate redisTemplate;
	
	@PostMapping("add")
	public void add(@RequestBody AddDto dto)
	{
		redisTemplate.opsForList().leftPush(dto.subject, dto.message);
	}
	@PostMapping("get")
	public List<String> getForSubject(@RequestBody String subject)
	{
		long range = redisTemplate.opsForList().size(subject)-1;
		List<String > strings = redisTemplate.opsForList().range(subject, 0, range);
		return strings;
	}
	@DeleteMapping("delete")
	public void delete(@RequestBody AddDto dto)
	{
		Long index = redisTemplate.opsForList().indexOf(dto.subject, dto.message);
		if (index != null )
		{
			redisTemplate.opsForList().remove(dto.subject, 1, dto.message);
		}
	}
}

class AddDto {
	public String subject;
	public String message;
}