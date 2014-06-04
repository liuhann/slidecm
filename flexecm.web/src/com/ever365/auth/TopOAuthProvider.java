package com.ever365.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
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
		 Map<String,String> props=new HashMap<String,String>();
		 props.put("grant_type","authorization_code");
		 /*测试时，需把test参数换成自己应用对应的值*/
		 props.put("code",code);
		 props.put("client_id","21796075");
		 props.put("client_secret","f681d9f9210f16bcaa12f32e69407947");
		 props.put("redirect_uri","http://godbuy.ever365.com/oauth/top");
		 props.put("view","web");
  
		 JSONObject jso = WebUtils.doPost(url, props);
		 
		 
		 Map<String, Object> map = WebUtils.jsonObjectToMap(jso);
		 
		 map.put(OAuthProvider.USERID, map.get(TAOBAO_USER_NICK));
		 return map;
	}

	@Override
	public String getName() {
		return "top";
	}
	
	
}
