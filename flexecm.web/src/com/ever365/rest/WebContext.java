package com.ever365.rest;

import javax.servlet.http.HttpServletRequest;

public class WebContext {
	
	private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
	private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
	private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
	private static final String UNKNOWN = "unknown";
	private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	
	private static ThreadLocal<String> remoteAddr = new ThreadLocal<String>();
	private static ThreadLocal<String> sessionId = new ThreadLocal<String>();
	
	private static ThreadLocal<Boolean> local = new ThreadLocal<Boolean>(); 

	public static String getSessionID() {
		return sessionId.get();
	}
	
	public static void setSessionId(String id) {
		sessionId.set(id);
	}
	
	public static boolean isLocal() {
		return local.get();
	}
	
	public static void setLocal(Boolean l) {
		local.set(l);
	}
	
	public static String getRemoteAddr() {
		return remoteAddr.get();
	}
	
	public static void setRemoteAddr(String addr) {
		remoteAddr.set(addr);
	}
	
	public static String getRemoteAddr(HttpServletRequest request) {
		String ip = request.getHeader(X_FORWARDED_FOR);
		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader(PROXY_CLIENT_IP);
		}
		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader(WL_PROXY_CLIENT_IP);
		}
		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader(HTTP_CLIENT_IP);
		}
		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader(HTTP_X_FORWARDED_FOR);
		}
		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	
	public static void clear() {
		remoteAddr.set(null);
		sessionId.set(null);
	}
	
	
	
}
