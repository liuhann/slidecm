package com.ever365.auth;

import java.util.Map;

public interface OAuthProvider {

	public static final String CODE = "code";
	
	public static final String USERID = "userid";
	
	String getCode();
	
	Map<String, Object> authorize(String code);
	
	String getName();
}