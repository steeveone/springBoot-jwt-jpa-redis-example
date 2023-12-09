package com.steeve.security.component;

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtProvider
{
	 private final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
	 	
	 public static String issuer;
	 public static String secret;
	 public static String prefix;
	 public static String headerParam;
	 private  @Value("${jwt.jwtissuer}") String jwtissuer;

	public JwtProvider(Environment env)
	 {
		 JwtProvider.issuer = env.getProperty("jwt.issuer");
		 JwtProvider.secret = env.getProperty("jwt.secret");
	     JwtProvider.prefix = env.getProperty("jwt.prefix");
	     JwtProvider.headerParam = env.getProperty("jwt.param");
	     
	     if (JwtProvider.secret == null || JwtProvider.prefix == null || JwtProvider.headerParam == null) {
	            throw new BeanInitializationException("Cannot assign security properties. Check application.yml file.");
	     }
	 }
	 
	 public static String createJwt(String subject, Map<String, Object> payloadClaims)
	 {
		 JWTCreator.Builder builder = JWT.create().withSubject(subject).withIssuer(issuer);
		 final DateTime now = DateTime.now();
	     builder.withIssuedAt(now.toDate()).withExpiresAt(now.plusMinutes(5).toDate());

	     for (Map.Entry<String, Object> entry : payloadClaims.entrySet())
	     {
	            builder.withClaim(entry.getKey(), entry.getValue().toString());
	     }
	     return builder.sign(Algorithm.HMAC256(JwtProvider.secret));
	 }
	 public static String createRememberMeJwt(String subject, Map<String, Object> payloadClaims)
	 {
		 JWTCreator.Builder builder = JWT.create().withSubject(subject).withIssuer(issuer);
		 final DateTime now = DateTime.now();
	     builder.withIssuedAt(now.toDate()).withExpiresAt(now.plusDays(5).toDate());

	     for (Map.Entry<String, Object> entry : payloadClaims.entrySet())
	     {
	            builder.withClaim(entry.getKey(), entry.getValue().toString());
	     }
	     builder.withClaim("rememberMe", "true");
	     return builder.sign(Algorithm.HMAC256(JwtProvider.secret));
	 }
	 public static DecodedJWT verifyJwt(String jwt)
	 {
		 return JWT.require(Algorithm.HMAC256(JwtProvider.secret)).build().verify(jwt);
	 }
}
