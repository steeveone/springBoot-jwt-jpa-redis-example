package com.example.restservice.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.restservice.entity.User;
import com.example.restservice.repo.UserRepository;
import com.example.restservice.service.JwtRedisService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/private")
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
    private UserRepository userRepository;
	@Autowired
    private JwtRedisService redisService;
    @GetMapping("users")
    public List<User> getUsers() {
    	logger.info("listed users");
        return (List<User>) userRepository.findAll();
    }
    @PostMapping("users")
    void addUser(@RequestBody User user) {
    	logger.info("added user [ " +user.getEmail()+ " ]");
    	user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userRepository.save(user);
    }
    
    @DeleteMapping("users/{id}")
    void deleteUser(@PathVariable("id") Long id)
    {	
    	logger.info("deleted user id [ " + id + " ]");
    	userRepository.deleteById(id);
    }
    
    @PostMapping("/users/resetpwd/{id}")
    void resetPwd(@PathVariable("id") Long id, @RequestBody String password)
    {
    	logger.info("required password reset for user id [ " +id+ " ]" );
    	Optional<User> aUser = userRepository.findById(id);
    
    	if (aUser.isPresent())
    	{
    		aUser.get().setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
    		//aUser.get().setHasToLogout(true);
    		logger.trace("saving new password");
    		userRepository.save(aUser.get());
    		logger.trace("invalidating all tokens for user [ " + aUser.get().getEmail() + " ]");
    		redisService.removeAllTokens(aUser.get().getEmail());
    	}
    	else
    	{
    		logger.trace("user id [ " + id + " ] not present");
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    	}
    }
}