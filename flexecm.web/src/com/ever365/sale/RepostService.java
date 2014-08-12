package com.ever365.sale;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.ever365.auth.WeiboOAuthProvider;
import com.ever365.common.ContentStore;
import com.ever365.mongo.AutoIncrementingHelper;
import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.HttpStatus;
import com.ever365.rest.HttpStatusException;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestResult;
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
	
	private static final String COLL_PRESENTS = "gifts";

	private static final String SESSIONID = "sessionid";
	
	public static final Integer GIFT_NOT_APPROVED = 0;
	public static final Integer GIFT_APPROVED = 1;
	public static final Integer GIFT_POSTED = 2;
	public static final Integer GIFT_FINISHED = 3;

	Logger logger = Logger. getLogger(RepostService.class.getName());
	
	private MongoDataSource dataSource;
	private ContentStore contentStore;
	private AutoIncrementingHelper incrementingHelper;
	private WeiboOAuthProvider weiboOAuthProvider;
	
	public void setWeiboOAuthProvider(WeiboOAuthProvider weiboOAuthProvider) {
		this.weiboOAuthProvider = weiboOAuthProvider;
	}
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
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		coll.ensureIndex("seq");
		coll.ensureIndex("status");
	}
	
	@RestService(method="GET", uri="/user/info")
	public Map<String, Object> getUserInfos() {
		
		Map<String, Object> winfo = new HashMap<String, Object>();
		winfo.putAll(weiboOAuthProvider.getWeiboInfo());
		winfo.put("cu", AuthenticationUtil.getCurrentUser());
		
		return winfo;
	}
	
	/**
	 * status : 0 : 商品增加未有人发微博
	 * 	 		1 : 审核通过
	 *          2 : 已经转发
	 *          3  ： 抽奖结束   
	 */
	
	@RestService(method="POST", uri="/seller/present/add")
	public void request(
			@RestParam(required=true, value="url") String url,
			@RestParam(required=true, value="per") Integer per,
			@RestParam(required=true, value="total") Integer total,
			@RestParam(required=true, value="desc") String desc,
			@RestParam(required=true, value="dav") String dav,
			@RestParam(required=true, value="fans") Integer fans,
			@RestParam(required=false, value="preview") String preview
			) {
		try {
			DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
			DBObject dbo = new BasicDBObject();
			dbo.put("url", url);
			dbo.put("per", per);
			dbo.put("seller", weiboOAuthProvider.getWeiboInfo().get("rn"));
			dbo.put("sid", AuthenticationUtil.getCurrentUser());
			dbo.put("total", total);
			dbo.put("desc", desc);
			dbo.put("preview", preview);
			dbo.put("dav", dav);
			dbo.put("fans", fans);
			
			dbo.put("seq", incrementingHelper.getNextSequence("weibo"));
			dbo.put("status", GIFT_NOT_APPROVED);
			
			coll.insert(dbo);
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 商家获取自己提供的商品列表
	 * @return
	 */
	@RestService(method="GET", uri="/seller/present")
	public List<Map<String, Object>> getMyPresents() {
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		BasicDBObject query = new BasicDBObject("sid", AuthenticationUtil.getCurrentUser());
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		DBCursor cursor = coll.find(query);
		
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		return list;
	}
	
	/**
	 * 商家删除自己未获通过的商品
	 * @return
	 */
	@RestService(method="POST", uri="/seller/present/remove")
	public void removeMyPresent(@RestParam(value="id") String id) {
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		query.put("sid", AuthenticationUtil.getCurrentUser());
		query.put("status", GIFT_NOT_APPROVED);
		
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		coll.remove(query);
	}

	/**
	 * 供大V列举所有可转发的商品
	 * @return
	 */
	@RestService(method="GET", uri="/present/list")
	public List<Map<String, Object>> getPresents() {
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		BasicDBObject query = new BasicDBObject("status", GIFT_APPROVED);
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		DBCursor cursor = coll.find(query);
		
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		return list;
	}
	
	/**
	 * 正在转发抽奖中的商品
	 * @return
	 */
	@RestService(method="GET", uri="/present/today")
	public List<Map<String, Object>> getTodayPresent() {
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		BasicDBObject query = new BasicDBObject("status", GIFT_POSTED);
		
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
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		BasicDBObject query = new BasicDBObject();
		query.put("dav", AuthenticationUtil.getCurrentUser());
		
		DBCursor cursor = coll.find(query);
		List<Map<String, Object>> r = new ArrayList<Map<String,Object>>();
		while(cursor.hasNext()) {
			r.add(cursor.next().toMap());
		}
		return r;
	}
	
	@RestService(method="POST", uri="/dav/post", reqireAt=true,webcontext=true)
	public synchronized void postWeibo(@RestParam(value="id") String mid, @RestParam(value="msg")String msg) {
		
		DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
		
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(mid));
		query.put("status", GIFT_APPROVED);
		
		DBObject gift = coll.findOne(query);
		
		if (gift==null) throw new HttpStatusException(HttpStatus.LOCKED);
		
		Map<String, Object> params = new HashMap<String, Object>();
		if (gift.get("preview")!=null) {
			//logger.info("get content data " + gift.get("preview")  + "  using cs: " + contentStore);
			StreamObject cd = contentStore.getContentData((String)gift.get("preview"));
			params.put("pic", cd.getInputStream());
			params.put("size", cd.getSize());
		} else {
			throw new HttpStatusException(HttpStatus.PRECONDITION_FAILED);
		}
		
		try {
			params.put("access_token", weiboOAuthProvider.getFullWeiboInfo().get("at"));
			params.put("status", URLEncoder.encode(msg, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			
		}
		
		Map<String, Object> set = new HashMap<String, Object>();
		if (WebContext.isLocal()) {
			
		} else {
			logger.info(params + "  at" + params.get("access_token"));
			JSONObject result = WebUtils.doPost("https://upload.api.weibo.com/2/statuses/upload.json", params);
			
			logger.info(result.toString());
			
			if (!result.has("id")) {
				throw new HttpStatusException(HttpStatus.FORBIDDEN);
			}
			dataSource.getCollection("weibos").insert(new BasicDBObject(WebUtils.jsonObjectToMap(result)));
			try {
				set.put("wid", result.getLong("id"));
			} catch (JSONException e) {
				throw new HttpStatusException(HttpStatus.FORBIDDEN);
			}
		}
		set.put("msg", msg);
		set.put("status", GIFT_POSTED);
		set.put("dav", AuthenticationUtil.getCurrentUser());
		set.put("time", System.currentTimeMillis());
		BasicDBObject update = new BasicDBObject();
		update.put("$set", set);
		coll.update(query, update);
	}
	
	@RestService(method="GET", uri="/dav/i", webcontext=true, authenticated=false) 
	public Map<String, Object> getRepost(@RestParam(value="i") String seq) {
		
		Integer i = Integer.parseInt(seq);

		Map<String, Object> post = getPost(i);
		
		if(post==null) throw new HttpStatusException(HttpStatus.NOT_FOUND);
		
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.putAll(post);
		
			
		if (AuthenticationUtil.getCurrentUser()!=null) {
			DBCollection reposts = dataSource.getCollection("reposts");
			
			//首先查看是否用户已转发过 
			DBObject repost = reposts.findOne(BasicDBObjectBuilder.start().add("seq", i)
					.add("u", AuthenticationUtil.getCurrentUser()).get());
			if (repost!=null) {
				result.put("code", repost.get("code"));
			} else {//如果已经标记过， 则自动为用户预订。
				DBObject marked = reposts.findOne(new BasicDBObject(SESSIONID, WebContext.getSessionID()));
				if (marked!=null) {
					try {
						Map<String, Object> repostMap = doRepost(seq);
						result.put("code", repostMap.get("code"));
					} catch (Exception ex) {
						//
					}
					reposts.remove(new BasicDBObject(SESSIONID, WebContext.getSessionID()));
				}
			}
			result.put("cu", AuthenticationUtil.getCurrentUser());
		}
		
		result.put("weibos", getTodayPresent());
		return result;
		
	}
	
	
	private Map<Integer, Map> posts = new HashMap<Integer, Map>();
	
	@RestService(method="GET", uri="/dav/post/get", authenticated=false) 
	public Map<String, Object> getPost(@RestParam(value="i") Integer seq) {
		
		if (posts.get(seq)==null) {
			DBCollection coll = dataSource.getCollection(COLL_PRESENTS);
			BasicDBObject query = new BasicDBObject("seq", seq);
			DBObject gift = coll.findOne(query);
			if (gift!=null) {
				posts.put(seq, gift.toMap());
			}
		}
		return posts.get(seq);
	}
	
	
	@RestService(method="POST", uri="/dav/repost/book", authenticated=false, webcontext=true)
	public void markAsBook(@RestParam(value="i")String seq) {
		DBCollection coll = dataSource.getCollection("reposts");
		coll.insert(new BasicDBObject(SESSIONID, WebContext.getSessionID()));
	}
	
	
	@RestService(method="POST", uri="/dav/repost") 
	public Map<String, Object> doRepost(@RestParam(value="i") String seq) {
		Map<String, Object> post = getPost(new Integer(seq));
		if (post==null) 
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		
		
		DBCollection reposts = dataSource.getCollection("reposts");
		DBObject one = reposts.findOne(BasicDBObjectBuilder.start().add("seq", new Integer(seq)).add("u", AuthenticationUtil.getCurrentUser()).get());
		
		if (one!=null) {
			return one.toMap();
		}
		
		DBObject dbo = new BasicDBObject();
		
		dbo.put("seq", new Integer(seq));
		dbo.put("code", incrementingHelper.getNextSequence("repost-" + seq));
		dbo.put("u", AuthenticationUtil.getCurrentUser());
		dbo.put("t", System.currentTimeMillis());
		
		if (!WebContext.isLocal()) {
			if (post.get("wid")==null) throw new HttpStatusException(HttpStatus.BAD_REQUEST);
			Map<String, Object> params = new HashMap<String, Object>();
			
			params.put("access_token", weiboOAuthProvider.getFullWeiboInfo().get("at"));
			params.put("id", post.get("wid"));
			params.put("is_comment", 3);
			
			//String requestUrl = "https://api.weibo.com/2/statuses/repost.json" + 
			logger.info(params.toString());
			
			JSONObject result = WebUtils.doPost("https://api.weibo.com/2/statuses/repost.json", params);
			logger.info(result.toString());
			if (result.has("id")) {
				try {
					dbo.put("wid", result.getLong("id"));
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
	
	@RestService(uri="/repost/my", method="GET")
	public List<Map<String, Object>> getMyReposts() {
		DBCollection reposts = dataSource.getCollection("reposts");
		if (AuthenticationUtil.getCurrentUser()==null) return Collections.EMPTY_LIST;
		DBCursor cur = reposts.find(BasicDBObjectBuilder.start().add("u", AuthenticationUtil.getCurrentUser()).get());
		
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		while (cur.hasNext()) {
			DBObject o = cur.next();
			Map m = o.toMap();
			m.put("post", getPost((Integer)m.get("seq")));
			result.add(m);
		}
		return result;
	}
	
	@RestService(uri="/repost/gift", method="GET")
	public List<Map<String, Object>> getMyWinReposts() {
		DBCollection reposts = dataSource.getCollection("repostwins");
		if (AuthenticationUtil.getCurrentUser()==null) return Collections.EMPTY_LIST;
		DBCursor cur = reposts.find(BasicDBObjectBuilder.start().add("u", AuthenticationUtil.getCurrentUser()).get());
		
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		while (cur.hasNext()) {
			DBObject o = cur.next();
			Map m = o.toMap();
			m.put("post", getPost((Integer)m.get("seq")));
			result.add(m);
		}
		return result;
	}
	
	
	@RestService(uri="/repost/receiveLocation", method="GET")
	public Map<String, Object> getMyPostLocation() {
		DBCollection locations = dataSource.getCollection("location");
		
		DBObject post = locations.findOne(new BasicDBObject("u", AuthenticationUtil.getCurrentUser()));
		if (post==null) {
			return Collections.EMPTY_MAP;
		} else {
			return post.toMap();
		}
	}
	
	@RestService(uri="/repost/receiveLocation", method="POST")
	public void updateMyPostLocation(@RestParam(value="rn")String rn,
			@RestParam(value="detail")String detail,@RestParam(value="mobile")String mobile,
			@RestParam(value="phone")String phone) {
		DBCollection locations = dataSource.getCollection("location");

		DBObject post = new BasicDBObject();
		post.put("u", AuthenticationUtil.getCurrentUser());
		post.put("rn", rn);
		post.put("detail", detail);
		post.put("mobile", mobile);
		post.put("phone", phone);
		locations.update(new BasicDBObject("u", AuthenticationUtil.getCurrentUser()), post, true, false);
	}

	
	@RestService(uri="/weibo/cancel", method="GET")
	public RestResult cancelWeibo() {
		weiboOAuthProvider.cancelValidate();
		RestResult rr = new RestResult();
		Map<String, Object> session = new HashMap<String, Object>();
		session.put(AuthenticationUtil.SESSION_CURRENT_USER, null);
		rr.setSession(session);
		rr.setRedirect("/");
		return rr;
	}
	
	
}

