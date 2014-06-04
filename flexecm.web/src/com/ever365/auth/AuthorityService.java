package com.ever365.auth;

import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class AuthorityService {
	
	private static final String COLL_AUTHORITIES = "authorities";
	private static final String P = "p";
	private static final String U = "u";
	public static final String ADMIN = "admin";
	private MongoDataSource mongoDataSource;

	public void setMongoDataSource(MongoDataSource mongoDataSource) {
		this.mongoDataSource = mongoDataSource;
		mongoDataSource.getCollection(COLL_AUTHORITIES).ensureIndex(U);
	}

	@RestService(method="GET", uri="/person/exist")
	public boolean personExists(@RestParam(value="name")String userName) {
		DBCollection acoll = getAuthorityCollection();
		return acoll.findOne(new BasicDBObject(U, userName)) != null;
	}

	public DBCollection getAuthorityCollection() {
		return mongoDataSource.getCollection(COLL_AUTHORITIES);
	}
	

	@RestService(method="GET", uri="/person/current")
	public String getCurrentPerson() {
		return AuthenticationUtil.getCurrentUser();
	}
	
	
	@RestService(method="POST", uri="/password/modify")
	public void modifyPassword(@RestParam(value="old",required=true)String old,
			@RestParam(value="new", required=true)String newpass) {
		
		if (checkPassword(AuthenticationUtil.getCurrentUser(), old)) {
			DBObject p  = new BasicDBObject();
			p.put(U, AuthenticationUtil.getCurrentUser());
			p.put(P, newpass);
			getAuthorityCollection().update(new BasicDBObject(U, AuthenticationUtil.getCurrentUser()), p);
		}
	}
	
	
	
	
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
	
	@RestService(method="POST", uri="/person/remove", runAsAdmin=true)
	public void removePerson(@RestParam(value="id")String userName) {
		getAuthorityCollection().remove(new BasicDBObject(U, userName));
	}

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
	
	public void addOAuthPerson(@RestParam(value="id")String id, @RestParam(value="from") String from, @RestParam(value="at") String access_token) {
		DBObject dbo = new BasicDBObject();
		dbo.put(U, id);
		dbo.put("from", from);
		dbo.put(P, access_token);
		getAuthorityCollection().insert(dbo);
	}
}
