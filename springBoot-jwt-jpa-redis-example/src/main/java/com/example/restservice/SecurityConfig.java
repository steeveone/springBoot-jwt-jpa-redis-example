package com.example.restservice;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;

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
	 /*
	 @Bean
	 SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		 http.cors().and().csrf().disable();
	     http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	     	.and()
	        .addFilterBefore(new AuthorizationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), this.userRepository), UsernamePasswordAuthenticationFilter.class).authorizeRequests()
	        //.antMatchers("/public/**").permitAll()
	        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
	        .requestMatchers(new AntPathRequestMatcher("/private/users/resetPwd/**")).hasAnyRole("ADMIN")
	        .expressionHandler(null)
	        .anyRequest().authenticated();
	    return http.build();
	 }
	*/

	 @Bean
	 CorsConfigurationSource  corsConfigurationSource() {
	 	CorsConfiguration configuration = new CorsConfiguration();
	 	configuration.applyPermitDefaultValues();
	 	configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
	 	configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE"));
	 	//configuration.setAllowedHeaders(Arrays.asList("*"));
	 	//configuration.setAllowCredentials(true);
	 	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	 	source.registerCorsConfiguration("/**", configuration);
	 	return source;
	 }

	 
	 @Bean
	 SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		 http.cors((cors)->cors.configurationSource(corsConfigurationSource()));
		 http.csrf((csrf) -> csrf.disable());
		 http.sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		 http.addFilterBefore(new AuthorizationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), this.userRepository, this.redisService), UsernamePasswordAuthenticationFilter.class);
		 
		 http.authorizeHttpRequests((request) -> request.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
				 										.requestMatchers("/public/authentication/renew").authenticated()
				 										.requestMatchers("/public/authentication/logout").authenticated()
				 										.requestMatchers("/public/**").permitAll()
				 										.requestMatchers("/private/users/resetpwd/**").hasAuthority(UserRole.ADMIN.name())
				 										.requestMatchers("/private/users/**").hasAnyAuthority(UserRole.USER.name())
				 										.anyRequest().authenticated());
		 
	    return http.build();
	 } 
}
