package com.example.restservice;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String email;
    private String password;
    private Set<UserRole> roles;
    
    public User(){}
	public User(String name, String email){
		this.email = email;
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	
	@Column(nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
	
	 @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
	 @CollectionTable
	 @Enumerated(EnumType.STRING)
	 public Set<UserRole> getRoles() {
	    return roles;
	 }

	 public void setRoles(Set<UserRole> roles) {
	     this.roles = roles;
	 }
}