package com.ever365.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.RestService;
import com.ever365.utils.WebUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class WeiboOAuthProvider implements OAuthProvider {
	
	Logger logger = Logger. getLogger(WeiboOAuthProvider.class.getName());
	private MongoDataSource dataSource;
	
	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public WeiboOAuthProvider() {
		super();
	}

	@Override
	public String getCode() {
		return CODE;
	}

	@Override
	public Map<String, Object> authorize(String code) {
		 String url="https://api.weibo.com/oauth2/access_token";
		 String cliend_id = "2444138001";
		 String client_secret = "76543c609da27dde95d32d9494c6f039";
		
		 String at = null;
		 String uid = null;
		 try {
			 Map<String, Object> params = new HashMap<String, Object>();
			 params.put("client_id", cliend_id);
			 params.put("client_secret", client_secret);
			 params.put("grant_type", "authorization_code");
			 params.put("code", code);
			 params.put("redirect_uri", "http://shengo.duapp.com/oauth/weibo");
			 JSONObject json = WebUtils.doPost(url, params);
			 
			 if (json!=null && json.getString("access_token")!=null) {
				 at = json.getString("access_token");
				 uid = json.getString("uid");
				 return refreshWeiboInfo(uid, at);
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return null;
	}
	
	public void cancelValidate() {
		DBObject one = dataSource.getCollection("wbuers").findOne(new BasicDBObject("uid", AuthenticationUtil.getCurrentUser()));
		if (one!=null) {
			String at = one.get("at").toString();
			WebUtils.doGet("https://api.weibo.com/oauth2/revokeoauth2?access_token=" + at);
		}
	}
	
	@Override
	public String getName() {
		return "weibo";
	}
	
	@RestService(method="GET", uri="/weibo/info")
	public Map<String, Object> getWeiboInfo() {
		DBObject one = dataSource.getCollection("wbuers").findOne(new BasicDBObject("uid", AuthenticationUtil.getCurrentUser()));
		if (one!=null) {
			one.removeField("at");
			return one.toMap();
		} else {
			return Collections.EMPTY_MAP;
		}
	}
	
	public Map<String, Object> getFullWeiboInfo() {
		DBObject one = dataSource.getCollection("wbuers").findOne(new BasicDBObject("uid", AuthenticationUtil.getCurrentUser()));
		if (one!=null) {
			return one.toMap();
		} else {
			return null;
		}
	}
	
	@RestService(method="GET", uri="/weibo/refresh")
	public Map<String, Object> refreshWeiboInfo() {
		DBObject one = dataSource.getCollection("wbuers").findOne(new BasicDBObject("uid", AuthenticationUtil.getCurrentUser()));
		if (one!=null) {
			String at = one.get("at").toString();
			String uid = one.get("uid").toString();
			
			try {
				return refreshWeiboInfo(uid, at);
			} catch (JSONException e) {
				return null;
			}
			
		} else {
			return null;
		}
	}
	
	public Map<String, Object> refreshWeiboInfo(String uid, String at) throws JSONException {
		System.out.println("https://api.weibo.com/2/users/show.json?uid=" + uid
				+ "&access_token=" + at);
		
		JSONObject showJson = WebUtils.doGet("https://api.weibo.com/2/users/show.json?uid=" + uid
				+ "&access_token=" + at);
		if (showJson!=null) {
			String sn = showJson.getString("screen_name");
			String avatar_large = showJson.getString("avatar_large");
			
			DBObject dbo = BasicDBObjectBuilder.start()
					 .append(OAuthProvider.USERID, uid).append(OAuthProvider.ACCESS_TOKEN, at)
					 .append(OAuthProvider.REAL_NAME, sn).append("al", avatar_large).get();
			
			if (dataSource!=null) {
				 dataSource.getCollection("wbuers").update(new BasicDBObject("uid", uid), dbo, true, false);
			}
			return dbo.toMap();
		} else {
			return null;
		}
	}
	
	
}
