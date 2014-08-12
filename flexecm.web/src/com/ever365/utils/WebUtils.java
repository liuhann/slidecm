package com.ever365.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ever365.rest.HttpStatusException;
import com.sun.corba.se.spi.orb.StringPair;

public class WebUtils {
	
	private static Log logger = LogFactory.getLog(WebUtils.class);
	
	public static JSONObject doGet(String requestUrl) {
		HttpClient httpClient = new HttpClient(new HttpClientParams(),new SimpleHttpConnectionManager(true));
		GetMethod getMethod = new GetMethod(requestUrl);
		
		JSONObject jsonObject = null;
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				logger.info("Method failed: request url:" +  requestUrl + "  status:" + getMethod.getStatusLine());
			}
			InputStream bodyis = getMethod.getResponseBodyAsStream();
			
			//byte[] responseBody = getMethod.getResponseBody();
			
			String result =  convertStreamToString(bodyis);
			
			try {
				jsonObject = new JSONObject(result);
			} catch (Exception e) {
				logger.debug("json invalid:" +  requestUrl + "  content: " + result);
			}
			
		} catch (HttpException e) {
			logger.debug("HttpException on get url" + requestUrl);
		} catch (IOException e) {
			logger.debug("IOException on get url" + requestUrl);
		} finally {
			getMethod.releaseConnection();
		}
		return jsonObject;
	}
	
	public static JSONObject doPost(String requestUrl, Map<String, Object> params) {
		HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager(true));
		PostMethod postMethod = new PostMethod(requestUrl);
		
		List parts=new ArrayList();
		
		boolean multipart = false;
		for (final String key : params.keySet()) {
			Object value = params.get(key);
			if (value instanceof InputStream) {
				if (params.get("size")==null) {
					logger.info("POST param with size==?");
					throw new HttpStatusException(com.ever365.rest.HttpStatus.BAD_REQUEST);
				}
				final long size = (Long)params.get("size");
				final InputStream stream = (InputStream) value;
				parts.add(new FilePart(key, new PartSource() {
					@Override
					public long getLength() {
						return size;
					}
					@Override
					public String getFileName() {
						return key;
					}
					@Override
					public InputStream createInputStream() throws IOException {
						return stream;
					}
					})
				);
				multipart = true;
			} else {
				parts.add(new StringPart(key,value.toString()));
				postMethod.setParameter(key, value.toString());
			}
		}
		if (multipart) {
			postMethod.getParams().clear();
			postMethod.setRequestEntity(new MultipartRequestEntity((Part[]) parts.toArray(new Part[parts.size()]), 
					postMethod.getParams()));
		} 
		
		JSONObject jsonObject = null;
		try {
			int statusCode = httpClient.executeMethod(postMethod);
			
			if (statusCode != HttpStatus.SC_OK) {
				 logger.info("Method failed: request url:" +  requestUrl + "  status:" + postMethod.getStatusLine());
			}
			byte[] responseBody = postMethod.getResponseBody();
			String result =  new String(responseBody, "utf-8");
			
			try {
				jsonObject = new JSONObject(result);
			} catch (Exception e) {
				logger.info("json invalid:" +  requestUrl + "  content: " + result);
			}
			
		} catch (HttpException e) {
			logger.debug("HttpException on get url" + requestUrl);
		} catch (IOException e) {
			logger.debug("IOException on get url" + requestUrl);
		} finally {
			postMethod.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0); 
		}
		return jsonObject;
	}

	
	 public static String convertStreamToString(InputStream is) {   
		 try {
			 BufferedReader reader = new BufferedReader(new InputStreamReader(is,
					 "UTF-8"));
			 StringBuilder sb = new StringBuilder();
			 String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "/n");
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		 return null;
	 }   

	 public static List<Object> jsonArrayToList(JSONArray jsonArray) {
			List<Object> ret = new ArrayList<Object>();
			Object value = null;
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				try {
					value = jsonArray.get(i);
				} catch (JSONException e) {
					System.out.println(" there are no value with the index in the JSONArray");
					e.printStackTrace();
					return null;
				}
				if (value instanceof JSONArray) {
					ret.add(jsonArrayToList((JSONArray) value));
				} else if (value instanceof JSONObject) {
					ret.add(jsonObjectToMap((JSONObject) value));
				} else {
					ret.add(value);
				}
			}

			return (ret.size() != 0) ? ret : null;
		}

		public static Map<String, Object> jsonObjectToMap(JSONObject jsonObject) {
			Map<String, Object> ret = new HashMap<String, Object>();
			Object value = null;
			String key = null;
			for (Iterator<?> keys = jsonObject.keys(); keys.hasNext();) {
				key = (String) keys.next();
				try {
					value = jsonObject.get(key);
					if (value.toString().equals("null")) {
						value = null;
					}
				} catch (JSONException e) {
					System.out.println("the key is not found in the JSONObject");
					e.printStackTrace();
					return null;
				}
				if (value instanceof JSONArray) {
					ret.put(key, jsonArrayToList((JSONArray) value));
				} else if (value instanceof JSONObject) {
					ret.put(key, jsonObjectToMap((JSONObject) value));
				} else {
					ret.put(key, value);
				}

			}

			return ret.size() != 0 ? ret : null;

		}

}
