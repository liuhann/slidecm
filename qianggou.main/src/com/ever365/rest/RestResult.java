package com.ever365.rest;

import java.util.Map;

public class RestResult {
	
	private String textPlain;
	private Map<String, Object> json;
	private Map<String, Object> session;
	private String redirect;
	public String getTextPlain() {
		return textPlain;
	}
	public void setTextPlain(String textPlain) {
		this.textPlain = textPlain;
	}
	public Map<String, Object> getJson() {
		return json;
	}
	public void setJson(Map<String, Object> json) {
		this.json = json;
	}
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
