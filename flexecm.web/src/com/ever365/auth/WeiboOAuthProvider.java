package com.ever365.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.ever365.utils.WebUtils;


public class WeiboOAuthProvider implements OAuthProvider {
	
	Logger logger = Logger. getLogger(WeiboOAuthProvider.class.getName());
	
	@Override
	public String getCode() {
		return CODE;
	}

	@Override
	public Map<String, Object> authorize(String code) {
		 String url="https://api.weibo.com/oauth2/access_token";
		 String cliend_id = "3339035727";
		 String client_secret = "01633e971fb6bfa47b1ecc89f725bd89";
		 
		 String requestUrl = url + "?client_id=" + cliend_id + "&client_secret=" + client_secret + "&grant_type=authorization_code"
				  + "&redirect_uri=http://www.ever365.com/oauth/weibo&code=" + code;
		 try {
			 Map<String, Object> params = new HashMap<String, Object>();
			 params.put("client_id", cliend_id);
			 params.put("client_secret", client_secret);
			 params.put("grant_type", "authorization_code");
			 params.put("code", code);
			 params.put("redirect_uri", "http://www.ever365.com/oauth/weibo");
			 JSONObject json = WebUtils.doPost(url, params);
			 logger.info(json.toString());
			 if (json!=null && json.getString("access_token")!=null) {
				 String at = json.getString("access_token");
				 String uid = json.getString("uid");
				 //JSONObject uidjson = WebUtils.doGet("https://api.weibo.com/2/account/get_uid.json?access_token=" + at);
				 //logger.info(uidjson.toString());
				 //if (uidjson!=null && uidjson.getString("uid")!=null) {
				 //String uid =  uidjson.getString("uid");
				JSONObject showJson = WebUtils.doGet("https://api.weibo.com/2/users/show.json?uid=" + uid
						+ "&access_token=" + at);
				logger.info(showJson.toString());
				if (showJson!=null) {
					Map<String, Object> map = WebUtils.jsonObjectToMap(showJson);
					map.put(OAuthProvider.USERID, map.get("screen_name"));
					map.put(OAuthProvider.ACCESS_TOKEN, at);
					return map;
				}
				 //}
				 
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return null;
	}

	@Override
	public String getName() {
		return "weibo";
	}
	
	
}
