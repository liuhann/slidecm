package com.ever365.rest;

import java.io.InputStream;
import java.util.Map;

public class RestResult {
	
	private Map<String, Object> session;
	private String redirect;

	public Map<String, Object> getSession() {
		return session;
	}
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
	public String getRedirect() {
		return redirect;
	}
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	
}
