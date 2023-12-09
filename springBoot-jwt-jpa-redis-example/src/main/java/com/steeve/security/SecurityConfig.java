package com.steeve.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.steeve.security.entity.UserRole;
import com.steeve.security.repo.UserRepository;
import com.steeve.security.service.JwtRedisService;

import jakarta.servlet.DispatcherType;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	 @Autowired
	 private UserRepository userRepository;
	 @Autowired
	 private JwtRedisService redisService;
	 @Bean
	 PasswordEncoder passwordEncoder() {
		 return new BCryptPasswordEncoder();
	 }
	 

	 @Bean
	 AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
	 	return authenticationConfiguration.getAuthenticationManager();
	 }

	 @Bean
	 CorsConfigurationSource  corsConfigurationSource() {
	 	CorsConfiguration configuration = new CorsConfiguration();
	 	configuration.applyPermitDefaultValues();
	 	configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
	 	configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
	 	//configuration.setAllowedHeaders(Arrays.asList("*"));
	 	//configuration.setAllowCredentials(true);
	 	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	 	source.registerCorsConfiguration("/**", configuration);
	 	return source;
	 }
	 @Bean
	 MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
	     return new MvcRequestMatcher.Builder(introspector);
	 }
	 
	 @Bean
	 SecurityFilterChain filterChain(HttpSecurity http,  MvcRequestMatcher.Builder mvc) throws Exception {
		 http.cors((cors)->cors.configurationSource(corsConfigurationSource()));
		 http.csrf((csrf) -> csrf.disable());
		 http.sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		 
		 http.addFilterBefore(new AuthorizationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), this.userRepository, this.redisService), UsernamePasswordAuthenticationFilter.class);
		 
		 http.headers((request) -> request.frameOptions((req) -> {req.sameOrigin();}));
		 http.authorizeHttpRequests((request) ->request.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
				 										.requestMatchers(antMatcher("/h2console/**")).permitAll()
				 										.requestMatchers(antMatcher("/public/authentication/rememberMe")).authenticated()
				 										.requestMatchers(antMatcher("/public/authentication/renew")).authenticated()
				 										.requestMatchers(antMatcher("/public/authentication/logout")).authenticated()
				 										.requestMatchers(antMatcher("/public/**")).permitAll()
				 										.requestMatchers(antMatcher("/private/users/resetpwd/**")).hasAuthority(UserRole.ADMIN.name())
				 										.requestMatchers(antMatcher("/private/users/**")).hasAnyAuthority(UserRole.USER.name())
				 										.anyRequest().authenticated());
		 
	    return http.build();
	 } 
	 
}
