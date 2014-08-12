package com.ever365.auth;

import java.util.Map;

public interface OAuthProvider {

	public static final String CODE = "code";
	
	public static final String ACCESS_TOKEN = "at";
	
	public static final String USERID = "uid";
	public static final String REAL_NAME = "rn";
	
	String getCode();
	
	Map<String, Object> authorize(String code);
	
	String getName();
}
