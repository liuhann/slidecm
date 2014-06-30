package com.ever365.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.ever365.utils.WebUtils;


public class TopOAuthProvider implements OAuthProvider {

	private static final String TAOBAO_USER_NICK = "taobao_user_nick";
	
	@Override
	public String getCode() {
		return CODE;
	}

	@Override
	public Map<String, Object> authorize(String code) {
		String url="https://oauth.taobao.com/token";
		 Map<String,Object> props=new HashMap<String,Object>();
		 props.put("grant_type","authorization_code");
		 /*测试时，需把test参数换成自己应用对应的值*/
		 props.put("code",code);
		 props.put("client_id","21796075");
		 props.put("client_secret","f681d9f9210f16bcaa12f32e69407947");
		 props.put("redirect_uri","http://godbuy.ever365.com/oauth/top");
		 props.put("view","web");
  
		 JSONObject jso = WebUtils.doPost(url, props);
		 
		 Map<String, Object> map = WebUtils.jsonObjectToMap(jso);
		 
		 try {
			map.put(OAuthProvider.USERID, URLDecoder.decode(map.get(TAOBAO_USER_NICK).toString(), "UTF-8"));
			map.put(OAuthProvider.ACCESS_TOKEN, map.get("access_token"));
		} catch (UnsupportedEncodingException e) {
		}
		 return map;
	}

	@Override
	public String getName() {
		return "top";
	}
	
	
}
