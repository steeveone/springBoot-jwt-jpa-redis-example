package com.example.restservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
@Service
public class AuthenticationService {
	private final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtRedisService redisService;

    
    public String login(String email, String password) {
    	logger.trace("called login for [ " +email+ " ]");
    	
        Optional<User> _user = this.userRepository.findByEmail(email);
        
        if (_user.isEmpty())
        {
        	logger.trace("user not found");
        	throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User user = _user.get();
        if (!this.passwordEncoder.matches(password, user.getPassword())) {
        	logger.trace("wrong password");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        ObjectNode userNode = new ObjectMapper().convertValue(user, ObjectNode.class);
        userNode.remove("password");
        Map<String, Object> claimMap = new HashMap<String, Object>(0);
        claimMap.put("user", userNode);
        String jwtToken =  JwtProvider.createJwt(email, claimMap);
        
        logger.trace("generated token, storing on REDIS");
        redisService.addToken(user.getEmail(), jwtToken);
        logger.debug("user [ " + user.getEmail() + " ] logged in");
        return jwtToken;
    }
    
    public String rememberMe(User user)
    {
    	ObjectNode userNode = new ObjectMapper().convertValue(user, ObjectNode.class);
    	userNode.remove("password");
        Map<String, Object> claimMap = new HashMap<String, Object>(0);
        claimMap.put("user", userNode);
        String jwtToken =  JwtProvider.createRememberMeJwt(user.getEmail(), claimMap);
        redisService.addToken(user.getEmail(), jwtToken);
        return jwtToken;
    }

    public String renew(String oldJwtToken)
    {
    	logger.trace("renewing token [ " + oldJwtToken + " ]");
    	try {
    		DecodedJWT decoded =  JwtProvider.verifyJwt(oldJwtToken);
    		logger.trace("decoded token");
    		String email = decoded.getSubject();
    		logger.trace("deleting old token from REDIS");
    		redisService.removeToken(email, oldJwtToken);
    		
    		Optional<User> _user = this.userRepository.findByEmail(email);
    	 
    		if (_user.isEmpty())
    		{
    			logger.debug("user [ " + email + " ] from token not found on DB");
    			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    		}
    		User user = _user.get();
    		ObjectNode userNode = new ObjectMapper().convertValue(user, ObjectNode.class);
    		userNode.remove("password");
    		Map<String, Object> claimMap = new HashMap<String, Object>(0);
    		claimMap.put("user", userNode);
    		logger.trace("creating new jwt token");
    		String jwtToken =  JwtProvider.createJwt(email, claimMap);
    		logger.trace("saving new jwt token to REDIS");
    		redisService.addToken(user.getEmail(), jwtToken);
    		logger.debug("user [ " + user.getEmail() + " ] did a token renew");
    		return jwtToken;
    	} catch (SignatureVerificationException ex) {
    		logger.trace("token decoding threw " + ex.getClass().getName() + " : " + ex.getMessage());
    		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    	}
    }
    
    public void logout(String subject, String jwtToken)
    {
    	logger.trace("deleting token from REDIS [ " + jwtToken + "]");
    	redisService.removeToken(subject, jwtToken);
    	logger.debug("logout token [ " + jwtToken + " ]");
    }
}
