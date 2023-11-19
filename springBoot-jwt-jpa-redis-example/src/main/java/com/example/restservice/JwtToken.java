package com.example.restservice;

public class JwtToken {
	private String jwttoken;

	public String getJwttoken() {
		return jwttoken;
	}

	public void setJwttoken(String jwttoken) {
		this.jwttoken = jwttoken;
	}
	
	public String toString()
	{
		return this.jwttoken;
	}
}
