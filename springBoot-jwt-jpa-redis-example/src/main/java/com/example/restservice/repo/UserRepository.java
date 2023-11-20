package com.example.restservice.repo;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.restservice.entity.User;

@Repository
public interface UserRepository extends ListCrudRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
}