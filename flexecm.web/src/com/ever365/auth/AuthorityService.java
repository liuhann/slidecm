package com.ever365.auth;

import java.util.HashMap;
import java.util.Map;

import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.HttpStatus;
import com.ever365.rest.HttpStatusException;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestResult;
import com.ever365.rest.RestService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class AuthorityService {
	
	private static final String COLL_AUTHORITIES = "authorities";
	private static final String P = "p";
	private static final String U = "u";
	public static final String ADMIN = "admin";
	private String ap;
	private MongoDataSource dataSource;

	private Map<String, OAuthProvider> authProviders;
	
	public void setAuthProviders(Map<String, OAuthProvider> authProviders) {
		this.authProviders = authProviders;
	}

	public void setAp(String ap) {
		this.ap = ap;
	}

	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@RestService(method="GET", uri="/logout", authenticated=false)
	public RestResult logout(@RestParam(value="re") String redirect) {
		RestResult rr = new RestResult();
		Map<String, Object> session = new HashMap<String, Object>();
		session.put(AuthenticationUtil.SESSION_CURRENT_USER, null);
		rr.setSession(session);
		if (redirect==null) {
			rr.setRedirect("/");
		} else {
			rr.setRedirect(redirect);
		}
		return rr;
	}
	
	public Map<String, Object> validate(String from, String code) {
		OAuthProvider provider = authProviders.get(from);
		if (provider==null) {
			return null;
		} else {
			return provider.authorize(code);
		}
	}
	
	@Deprecated
	@RestService(method="GET", uri="/person/exist")
	public boolean personExists(@RestParam(value="name")String userName) {
		DBCollection acoll = getAuthorityCollection();
		return acoll.findOne(new BasicDBObject(U, userName)) != null;
	}

	public DBCollection getAuthorityCollection() {
		return dataSource.getCollection(COLL_AUTHORITIES);
	}

	@Deprecated
	@RestService(method="GET", uri="/person/current", authenticated=false)
	public String getCurrentPerson() {
		return AuthenticationUtil.getCurrentUser();
	}
	
	@Deprecated
	@RestService(method="POST", uri="/person/password")
	public void modifyPassword(@RestParam(value="old",required=true)String old,
			@RestParam(value="new", required=true)String newpass) {
		
		if (checkPassword(AuthenticationUtil.getCurrentUser(), old)) {
			DBObject p  = new BasicDBObject();
			p.put(U, AuthenticationUtil.getCurrentUser());
			p.put(P, newpass);
			getAuthorityCollection().update(new BasicDBObject(U, AuthenticationUtil.getCurrentUser()), p);
		}
	}
	
	@Deprecated
	@RestService(method="POST", uri="/person/add", runAsAdmin=true)
	public boolean createPerson(@RestParam(value="userId")String userName,
			@RestParam(value="password")String password) {
		if (personExists(userName)) return false;
		DBObject p  = new BasicDBObject();
		
		p.put(U, userName);
		p.put(P, password);
		getAuthorityCollection().insert(p);
		return true;
	}
	
	@Deprecated
	@RestService(method="POST", uri="/person/remove", runAsAdmin=true)
	public void removePerson(@RestParam(value="id")String userName) {
		getAuthorityCollection().remove(new BasicDBObject(U, userName));
	}

	@Deprecated
	@RestService(method="POST", uri="/person/checkpassword", authenticated=false)
	public boolean checkPassword(@RestParam(value="name")String userName,
			@RestParam(value="password")String password) {
		DBObject one = getAuthorityCollection().findOne(new BasicDBObject(U, userName));
		if (one==null) {
			return false;
		} 
		
		if (password.equals(one.get(P))) {
			return true;
		} else {
			return false;
		}
	}
	
	@Deprecated
	@RestService(method="POST", uri="/person/login", authenticated=false)
	public RestResult login(@RestParam(value="name")String userName,
			@RestParam(value="password")String password) {
		if (userName.equals(ADMIN) && password.equals(ap)) {
			RestResult rr = new RestResult();
			Map<String, Object> session = new HashMap<String, Object>();
			session.put(AuthenticationUtil.SESSION_CURRENT_USER, userName);
			rr.setSession(session);
			return rr;			
		}
		
		DBObject one = getAuthorityCollection().findOne(new BasicDBObject(U, userName));
		if (one==null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED);
		} else {
			if (password.equals(one.get(P))) {
				RestResult rr = new RestResult();
				Map<String, Object> session = new HashMap<String, Object>();
				session.put(AuthenticationUtil.SESSION_CURRENT_USER, userName);
				rr.setSession(session);
				return rr;
			} else {
				throw new HttpStatusException(HttpStatus.UNAUTHORIZED);
			}	
		}
	}
}
