package com.ever365.rest;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ever365.mongo.MongoDataSource;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CookieService {
	private MongoDataSource dataSource;
	
    public static final String ARG_TICKET = "365ticket";
	
	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Cookie setCookieTicket(HttpServletResponse resp ) {
    	Cookie cookie = new Cookie(ARG_TICKET, UUID.randomUUID().toString());
    	cookie.setMaxAge(60*24*60*60);
    	cookie.setPath("/");
    	resp.addCookie(cookie);
    	return cookie;
	}

	  
	public String getCookieTicket(HttpServletRequest httpReq) {
		Cookie[] cookies = httpReq.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(ARG_TICKET)) {
					return cookie.getValue();
				}
			}
		}
		return httpReq.getParameter(ARG_TICKET);
	}

	public void removeCookieTicket(HttpServletRequest request, HttpServletResponse response) {
		String ticket = getCookieTicket(request);
    	if (ticket!=null) {
    		DBCollection cookiesCol = dataSource.getCollection("cookies");
        	cookiesCol.remove(new BasicDBObject("ticket", ticket));
    	}
    	
    	Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cookie.setValue("");
				cookie.setMaxAge(0);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}
	}
	
	public String getCurrentUser(HttpServletRequest request) {
		String ticket = getCookieTicket(request);
		if (ticket==null) return null;
		
		DBCollection cookiesCol = dataSource.getCollection("cookies");
		DBObject ticDoc = cookiesCol.findOne(new BasicDBObject("ticket", ticket));
		if (ticDoc==null) {
			return null;
		} else {
			return (String)ticDoc.get("user");
		}
	}
	
	
	public String getCurrentAccessToken(HttpServletRequest request) {
		String ticket = getCookieTicket(request);
		if (ticket==null) return null;
		
		DBCollection cookiesCol = dataSource.getCollection("cookies");
		DBObject ticDoc = cookiesCol.findOne(new BasicDBObject("ticket", ticket));
		if (ticDoc==null) {
			return null;
		} else {
			return (String)ticDoc.get("at");
		}
	}
	
	
	/**
	 * 为登陆的用户保存cookie和用户名称对应关系
	 * @param request
	 * @param response
	 * @param username
	 */
	public void bindUserCookie(HttpServletRequest request,
			HttpServletResponse response, String username, String at) {
		String ticket = getCookieTicket(request);
		DBCollection cookiesCol = dataSource.getCollection("cookies");
		if (ticket != null) {
			DBObject ticDoc = cookiesCol.findOne(new BasicDBObject("ticket", ticket));
			if (ticDoc == null) {
				cookiesCol.insert(BasicDBObjectBuilder.start()
						.add("user", username).add("ticket", ticket)
						.add("remote", request.getRemoteAddr())
						.add("created", new Date())
						.add("at", at).get());
			} else {
				ticDoc.put("user", username);
				ticDoc.put("at", at);
				cookiesCol.update(new BasicDBObject("ticket", ticket), ticDoc);
			}
		} else {
			Cookie newCookie = setCookieTicket(response);
			ticket = newCookie.getValue();
			cookiesCol.insert(BasicDBObjectBuilder.start()
					.add("user", username).add("ticket", ticket)
					.add("remote", WebContext.getRemoteAddr(request))
					.add("agent", request.getHeader("User-Agent"))
					.add("at", at)
					.add("created", new Date()).get());
		}
	}
	
}
