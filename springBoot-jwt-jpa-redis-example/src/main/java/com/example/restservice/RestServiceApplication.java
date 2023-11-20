package com.example.restservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.restservice.entity.User;
import com.example.restservice.entity.UserRole;
import com.example.restservice.repo.UserRepository;

@SpringBootApplication
@EnableScheduling
public class RestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestServiceApplication.class, args);
	}
	@Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            Stream.of("John", "Julie", "Jennifer", "Helen", "Rachel").forEach(name -> {
                User user = new User(name, name.toLowerCase() + "@domain.com");
                user.setPassword("$2a$12$NvQAn8insrMifQSNBlhGRemi3745pKYhDsuNDUh7bgnwQllIwWh8C");
                user.setRoles(new HashSet<>(Arrays.asList(UserRole.USER)));
                userRepository.save(user);
            });
            userRepository.findAll().forEach(System.out::println);
            List<UserRole> roles = new ArrayList<UserRole>();
            roles.add(UserRole.ADMIN);
            roles.add(UserRole.USER);
            User a = userRepository.findById(1L).get();
            a.setRoles( new HashSet<>(roles));
            userRepository.save(a);
        };
    }
}
