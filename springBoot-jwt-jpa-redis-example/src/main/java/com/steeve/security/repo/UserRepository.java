package com.steeve.security.repo;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import com.steeve.security.entity.User;

@Repository
public interface UserRepository extends ListCrudRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
}