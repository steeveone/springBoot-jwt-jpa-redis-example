package com.example.restservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restservice.component.JwtProvider;
import com.example.restservice.dto.AuthenticationDTO;
import com.example.restservice.dto.JwtToken;
import com.example.restservice.entity.User;
import com.example.restservice.service.AuthenticationService;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/public/authentication")
public class AuthenticationController {
	   @Autowired
	   private AuthenticationService authenticationService;
	   private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	   
	    @PostMapping("login")
	    public JwtToken login(@RequestBody AuthenticationDTO authDto) {
	    	JwtToken tkn = new JwtToken();
	    	logger.trace("login for [ " + authDto.getEmail() + " ]");
	    	tkn.setJwttoken(this.authenticationService.login(authDto.getEmail(), authDto.getPassword()));
	    	return tkn;
	    }
	    
	    @GetMapping("rememberMe")
	    public JwtToken rememberMe()
	    {
	    	User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	JwtToken tkn = new JwtToken();
	    	tkn.setJwttoken(this.authenticationService.rememberMe(user));
	    	return tkn;
	    }
	    
	    @GetMapping("renew")
	    public JwtToken renew(@RequestHeader("Authorization") String token )
	    {
	    	String jwtTkn = token.replace(JwtProvider.prefix, "");
	    	JwtToken tkn = new JwtToken();
	    	logger.trace("renew token [ " + tkn + " ]");
	    	tkn.setJwttoken(this.authenticationService.renew(jwtTkn));
	    	return tkn;
	    }
	    @GetMapping("logout")
	    public void logout(@RequestHeader("Authorization") String token)
	    {
	    	User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	final String email = user.getEmail();
	    	String jwtTkn = token.replace(JwtProvider.prefix, "");
	    	logger.trace("logging out for [ " + email + " ] token [ " + jwtTkn + " ]");
	    	this.authenticationService.logout(email, jwtTkn);
	    }
	    
	    @PostMapping("logout")
	    public void logout(@RequestHeader("Authorization") String token, @RequestBody String rememberMe)
	    {
	    	User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	final String email = user.getEmail();
	    	String jwtTkn = token.replace(JwtProvider.prefix, "");
	    	logger.trace("logging out for [ " + email + " ] token [ " + jwtTkn + " ]");
	    	this.authenticationService.logout(email, jwtTkn);
	    	this.authenticationService.logout(email, rememberMe);
	    }
}
