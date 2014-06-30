package com.ever365.sale;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.ever365.common.ContentStore;
import com.ever365.mongo.AutoIncrementingHelper;
import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.HttpStatus;
import com.ever365.rest.HttpStatusException;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestService;
import com.ever365.rest.StreamObject;
import com.ever365.rest.WebContext;
import com.ever365.utils.WebUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class RepostService {
	
	private static final String SESSIONID = "sessionid";

	Logger logger = Logger. getLogger(RepostService.class.getName());
	
	private MongoDataSource dataSource;
	private ContentStore contentStore;
	private AutoIncrementingHelper incrementingHelper;
	
	
	public void setContentStore(ContentStore contentStore) {
		this.contentStore = contentStore;
	}
	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setIncrementingHelper(AutoIncrementingHelper incrementingHelper) {
		this.incrementingHelper = incrementingHelper;
	}
	
	public void init() {
		DBCollection coll = dataSource.getCollection("presents");
		coll.ensureIndex("seq");
		coll.ensureIndex("status");
	}
	/**
	 * status : 0 : 商品增加未有人发微博
	 * 	 		1 : 已发微博
	 *          2 : 转发抽奖完成   
	 */
	
	@RestService(method="POST", uri="/seller/present/add")
	public void request(
			@RestParam(required=true, value="url") String url,
			@RestParam(required=true, value="per") Integer per,
			@RestParam(required=true, value="total") Integer total,
			@RestParam(required=true, value="desc") String desc,
			@RestParam(required=false, value="preview") String preview
			) {
		try {
			DBCollection coll = dataSource.getCollection("presents");
			DBObject dbo = new BasicDBObject();
			dbo.put("url", url);
			dbo.put("per", per);
			dbo.put("seller", AuthenticationUtil.getCurrentUser());
			dbo.put("total", total);
			dbo.put("desc", desc);
			dbo.put("preview", preview);
			dbo.put("seq", incrementingHelper.getNextSequence("weibo"));
			dbo.put("status", 0);
			
			coll.insert(dbo);
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RestService(method="GET", uri="/seller/present/my")
	public List<Map<String, Object>> getMyPreparings() {
		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject("seller", AuthenticationUtil.getCurrentUser());
		query.put("status", 0);
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		DBCursor cursor = coll.find(query);
		
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		return list;
	}
	
	@RestService(method="POST", uri="/seller/present/remove")
	public void removeMyPresent(@RestParam(value="id") String id) {
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		query.put("seller", AuthenticationUtil.getCurrentUser());

		
		DBCollection coll = dataSource.getCollection("presents");
		coll.remove(query);
	}
	
	
	@RestService(method="GET", uri="/present/list")
	public List<Map<String, Object>> getPreparings() {
		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject();
		query.put("status", 0);
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		DBCursor cursor = coll.find(query);
		
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		return list;
	}
	
	@RestService(method="GET", uri="/present/onlines")
	public List<Map<String, Object>> getPosts() {
		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject();
		query.put("status", 1);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		DBCursor cursor = coll.find(query);
		
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		return list;
	}
	
	
	@RestService(method="GET", uri="/dav/vcode", runAsAdmin=true)
	public String addV() {
		try {
			DBCollection coll = dataSource.getCollection("vcodes");
			String code = com.ever365.utils.UUID.generateShortUuid();
			coll.insert(new BasicDBObject("code", code));
			return code;
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RestService(method="POST", uri="/dav/add")
	public void addV(@RestParam(required=true, value="code") String code) {
		try {
			DBCollection coll = dataSource.getCollection("vcodes");
			DBObject one = coll.findOne(new BasicDBObject("code", code));
			
			if (one!=null) {
				DBCollection wcoll = dataSource.getCollection("davs");
				wcoll.insert(new BasicDBObject("name", AuthenticationUtil.getCurrentUser()));
				coll.remove(new BasicDBObject("code", code));
			}
			return;
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	

	@RestService(method="GET", uri="/dav/info")
	public Map<String, Object> getCurrentDAV() {
		DBCollection coll = dataSource.getCollection("davs");
		DBObject one = coll.findOne(new BasicDBObject("name", AuthenticationUtil.getCurrentUser()));
		if (one==null) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN);
		} else {
			return one.toMap();
		}
	}
	
	@RestService(method="GET", uri="/dav/mypost")
	public List<Map<String, Object>> getMyPosts() {
		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject();
		query.put("dav", AuthenticationUtil.getCurrentWeiboUser());
		
		DBCursor cursor = coll.find(query);
		List<Map<String, Object>> r = new ArrayList<Map<String,Object>>();
		while(cursor.hasNext()) {
			r.add(cursor.next().toMap());
		}
		return r;
	}
	
	@RestService(method="GET", uri="/dav/onlineposts")
	public List<Map<String, Object>> getAllOnlinePosts() {
		DBCollection coll = dataSource.getCollection("presents");
		
		BasicDBObject query = new BasicDBObject();
		query.put("status", 1);
		
		DBCursor cursor = coll.find(query);
		
		List<Map<String, Object>> r = new ArrayList<Map<String,Object>>();
		
		while(cursor.hasNext()) {
			r.add(cursor.next().toMap());
		}
		return r;
	}
	
	@RestService(method="POST", uri="/dav/post", reqireAt=true,webcontext=true)
	public synchronized void postWeibo(@RestParam(value="id") String mid, @RestParam(value="msg")String msg) {
		
		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(mid));
		query.put("status", 0);
		
		DBObject gift = coll.findOne(query);
		
		if (gift==null) throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		
		Map<String, Object> params = new HashMap<String, Object>();
		if (gift.get("preview")!=null) {
			StreamObject cd = contentStore.getContentData((String)gift.get("preview"));
			params.put("pic", cd.getInputStream());
			params.put("size", cd.getSize());
		} else {
			throw new HttpStatusException(HttpStatus.PRECONDITION_FAILED);
		}
		
		try {
			params.put("access_token", AuthenticationUtil.getCurrentAt());
			params.put("status", URLEncoder.encode(msg, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			
		}
		
		Map<String, Object> set = new HashMap<String, Object>();
		if (WebContext.isLocal()) {
			
		} else {
			JSONObject result = WebUtils.doPost("https://upload.api.weibo.com/2/statuses/upload.json", params);
			
			if (!result.has("id")) {
				throw new HttpStatusException(HttpStatus.FORBIDDEN);
			}
			dataSource.getCollection("weibos").insert(new BasicDBObject(WebUtils.jsonObjectToMap(result)));
			try {
				set.put("pic", result.getString("pic"));
				set.put("wid", result.getString("id"));
			} catch (JSONException e) {
				
			}
		}
		
		set.put("status", 1);
		set.put("dav", AuthenticationUtil.getCurrentWeiboUser());
		set.put("time", System.currentTimeMillis());
		BasicDBObject update = new BasicDBObject();
		update.put("$set", set);
		coll.update(query, update);
	}
	
	@RestService(method="GET", uri="/dav/i", webcontext=true, authenticated=false) 
	public Map<String, Object> getRepost(@RestParam(value="i") String seq) {
		
		Integer i = Integer.parseInt(seq);

		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject("seq", i);
		
		DBObject gift = coll.findOne(query);
		if (gift!=null) {
			Map m = gift.toMap();
			
			if (AuthenticationUtil.getCurrentWeiboUser()!=null) {
				DBCollection reposts = dataSource.getCollection("reposts");
				DBObject repost = reposts.findOne(BasicDBObjectBuilder.start().add("seq", i)
						.add("u", AuthenticationUtil.getCurrentWeiboUser()).get());
				if (repost!=null) {
					m.put("code", repost.get("code"));
				} else {//如果已经标记过， 则自动为用户预订。
					DBObject marked = reposts.findOne(new BasicDBObject(SESSIONID, WebContext.getSessionID()));
					if (marked!=null) {
						try {
							Map<String, Object> repostMap = doRepost(seq);
							m.put("code", repostMap.get("code"));
						} catch (Exception ex) {
							//
						}
						reposts.remove(new BasicDBObject(SESSIONID, WebContext.getSessionID()));
					}
				}
				m.put("cu", AuthenticationUtil.getCurrentWeiboUser());
			}
			
			m.put("weibos", getAllOnlinePosts());
			
			return m;
		} 
		throw new HttpStatusException(HttpStatus.NOT_FOUND);
	}
	
	@RestService(method="POST", uri="/dav/repost/book", authenticated=false,webcontext=true)
	public void markAsBook(@RestParam(value="i")String seq) {
		DBCollection coll = dataSource.getCollection("reposts");
		coll.insert(new BasicDBObject(SESSIONID, WebContext.getSessionID()));
	}
	
	
	@RestService(method="POST", uri="/dav/repost", reqireAt=true) 
	public Map<String, Object> doRepost(@RestParam(value="i") String seq) {
		
		DBCollection coll = dataSource.getCollection("presents");
		BasicDBObject query = new BasicDBObject("seq", new Integer(seq));
		DBObject gift = coll.findOne(query);
		
		if (gift==null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
		
		DBCollection reposts = dataSource.getCollection("reposts");
		
		DBObject one = reposts.findOne(BasicDBObjectBuilder.start().add("seq", new Integer(seq)).add("u", AuthenticationUtil.getCurrentWeiboUser()).get());
		
		if (one!=null) {
			return one.toMap();
		}
		
		DBObject dbo = new BasicDBObject();
		
		dbo.put("seq", new Integer(seq));
		dbo.put("code", incrementingHelper.getNextSequence("wr" + seq));
		dbo.put("u", AuthenticationUtil.getCurrentWeiboUser());
		dbo.put("t", System.currentTimeMillis());
		
		if (!WebContext.isLocal()) {
			if (gift.get("wid")==null) throw new HttpStatusException(HttpStatus.BAD_REQUEST);
			Map<String, Object> params = new HashMap<String, Object>();
			
			params.put("access_token", AuthenticationUtil.getCurrentAt());
			params.put("id", gift.get("wid"));
			params.put("is_comment", 3);
			
			JSONObject result = WebUtils.doPost("https://api.weibo.com/2/statuses/repost.json", params);
			
			if (result.has("id")) {
				try {
					dbo.put("wid", result.getString("id"));
				} catch (JSONException e) {
				}
			} else {
				throw new HttpStatusException(HttpStatus.FORBIDDEN);
			}
		} else {
			
		}
		reposts.insert(dbo);
		return dbo.toMap();
	}
	
}
