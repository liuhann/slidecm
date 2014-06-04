package com.ever365.rest;

import java.io.InputStream;
import java.util.Map;

public class RestResult {
	
	private String textPlain;
	private Map<String, Object> session;
	private String redirect;
	
	private String fileName;
	private String mimeType;
	private InputStream fileStream;
	
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public InputStream getFileStream() {
		return fileStream;
	}
	public void setFileStream(InputStream fileStream) {
		this.fileStream = fileStream;
	}
	public String getTextPlain() {
		return textPlain;
	}
	public void setTextPlain(String textPlain) {
		this.textPlain = textPlain;
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
