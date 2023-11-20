package com.example.restservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.restservice.component.JwtProvider;
import com.example.restservice.entity.User;
import com.example.restservice.entity.UserRole;
import com.example.restservice.repo.UserRepository;
import com.example.restservice.service.JwtRedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class AuthorizationFilter extends BasicAuthenticationFilter
{
	private final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
	private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final JwtRedisService redisService;
    public AuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, JwtRedisService redisService) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.mapper = new ObjectMapper();
        this.redisService = redisService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    	
        final String header = request.getHeader(JwtProvider.headerParam);
        logger.trace("required [ " + request.getServletPath() + " ] header " + JwtProvider.headerParam +" [ " + header + " ]");
        if (header != null && header.startsWith(JwtProvider.prefix)) {
        	final String token = header.replace(JwtProvider.prefix, "");
        	try {
            	final DecodedJWT decoded = JwtProvider.verifyJwt(token);
            	if (decoded.getClaim("rememberMe").equals("true"))
            	{
            		if (request.getServletPath()!= "/public/authentication/renew")
            		{
            			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                		return;
            		}
            	}
            	final ObjectNode userNode = this.mapper.readValue(decoded.getClaim("user").asString(), ObjectNode.class);
            	final User user = this.mapper.convertValue(userNode, User.class);
            	if (redisService.checkToken(user.getEmail(), token))
            	{
            		logger.trace("decoded token matches with REDIS, checking against db");
            		this.userRepository.findById(user.getId()).ifPresent(entity -> {
            			Set<UserRole> roles = entity.getRoles();
            			List<SimpleGrantedAuthority> list = new ArrayList<SimpleGrantedAuthority>();
            			if (roles != null)
            			{
            				for (UserRole r: roles)
            				{
            					SimpleGrantedAuthority s = new SimpleGrantedAuthority(r.name());
            					list.add(s);
            				}
            			}
            			logger.trace("user [ " + entity.getEmail() + " ] authenticated");
            			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, list));
            			MDC.put("user", entity.getEmail());
            		});
            	}
            	else
            	{
            		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            		return;
            	}
            } catch (SignatureVerificationException ex)
            {
            	logger.debug("token [ " + token + " ] threw " + ex.getClass().getName() + " [ " + ex.getMessage()+ " ]");
            }
            catch (TokenExpiredException ex)
            {
            	logger.debug("token [ " + token + " ] is expired");
            	Base64.Decoder decoder = Base64.getUrlDecoder();
            	String[] parts = token.split("\\.");
            	ObjectMapper mapper = new ObjectMapper();
            	JsonNode root = mapper.readTree(decoder.decode(parts[1]));
            	String subject = root.get("sub").asText();
            	logger.trace("removing token for user [ " + subject + " ] [ " + token + " ]");
            	redisService.removeToken(subject, token);
            	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            	return;
            }
        	catch (JWTDecodeException ex)
        	{
        		logger.debug("token [ " + token + " ] threw " + ex.getClass().getName() + " [ " + ex.getMessage()+ " ]");
        	}
        }
        chain.doFilter(request, response);
        MDC.clear();
    }
}
