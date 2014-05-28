package com.ever365.qiang;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.util.FileCopyUtils;

import com.ever365.common.ContentStore;
import com.ever365.common.StringUtils;
import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.HttpStatus;
import com.ever365.rest.HttpStatusException;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestResult;
import com.ever365.rest.RestService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SellerService {
	private static final String EMPTY = "";
	private MongoDataSource dataSource;
	private ContentStore contentStore;

	public void setContentStore(ContentStore contentStore) {
		this.contentStore = contentStore;
	}

	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@RestService(method="POST", uri="/register")
	public RestResult register(@RestParam(required=true, value="name") String name,
			@RestParam(required=true, value="shop") String shop,
			@RestParam(required=true, value="pass") String pass,
			@RestParam(required=true, value="user") String user,
			@RestParam(required=true, value="phone") String phone,
			@RestParam(required=true, value="email") String email,
			@RestParam(required=true, value="other") String other
			) {
		DBCollection coll = dataSource.getCollection("seller");

		DBObject exist = coll.findOne(new BasicDBObject("name",name));
		if (exist!=null) {
			throw new HttpStatusException(HttpStatus.CONFLICT);
		}
		DBObject dbo = new BasicDBObject();
		dbo.put("name", name);
		dbo.put("shop", shop);
		dbo.put("pass", pass);
		dbo.put("user", user);
		dbo.put("phone", phone);
		dbo.put("email", email);
		dbo.put("other", other);
		coll.insert(dbo);
		RestResult rr = new RestResult();
		Map<String, Object> session = new HashMap<String, Object>();
		session.put(AuthenticationUtil.SESSION_CURRENT_USER, name);
		rr.setSession(session);
		return rr;
	}
	
	@RestService(method="POST", uri="/login", authenticated=false)
	public RestResult login(@RestParam(required=true, value="name") String name,
			@RestParam(required=true, value="pass") String pass
			) {
		DBCollection coll = dataSource.getCollection("seller");

		DBObject exist = coll.findOne(new BasicDBObject("name",name));
		if (exist==null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
		
		if (pass.equals(exist.get("pass"))) {
			RestResult rr = new RestResult();
			Map<String, Object> session = new HashMap<String, Object>();
			session.put(AuthenticationUtil.SESSION_CURRENT_USER, name);
			rr.setSession(session);
			return rr;
		} else {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RestService(method="POST", uri="/logout")
	public RestResult logout() {
		RestResult rr = new RestResult();
		Map<String, Object> session = new HashMap<String, Object>();
		session.put(AuthenticationUtil.SESSION_CURRENT_USER, null);
		rr.setSession(session);
		return rr;
	}
	
	@RestService(method="POST", uri="/seller/request")
	public void request(
			@RestParam(required=false, value="id") String id,
			@RestParam(required=true, value="title") String title,
			@RestParam(required=true, value="subtitle") String subtitle,
			@RestParam(required=true, value="count") String count,
			@RestParam(required=true, value="price") String price,
			@RestParam(required=true, value="time") String time,
			@RestParam(required=true, value="until") String until,
			@RestParam(required=false, value="preview") String preview,
			@RestParam(required=true, value="content") String content
			) {
		DBCollection coll = dataSource.getCollection("sells");
		DBObject dbo = new BasicDBObject();
		dbo.put("seller", AuthenticationUtil.getCurrentUser());
		
		dbo.put("title", title);
		dbo.put("subtitle", subtitle);
		dbo.put("count", new Integer(count));
		dbo.put("price", new Integer(price));
		dbo.put("time", StringUtils.parseDate(time).getTime());
		dbo.put("online", false);
		dbo.put("preview", preview);
		
		String contentId = UUID.randomUUID().toString();
		
		dbo.put("content", contentId);
		
		if (until.equals(EMPTY)) {
			dbo.put("until", EMPTY);
		} else {
			dbo.put("until", StringUtils.parseDate(until).getTime());
		}
		
		try {
			contentStore.putContent(contentId, new ByteArrayInputStream(content.getBytes("utf-8")), "text/html", content.length());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if (id==null) {
			coll.insert(dbo);
		} else {
			coll.update(new BasicDBObject("_id", new ObjectId(id)), dbo, true, false);
		}
	}
	
	@RestService(method="GET", uri="/seller/list")
	public List<Map<String, Object>> listRequesting() {
		
		DBCollection coll = dataSource.getCollection("sells");
		DBObject dbo = new BasicDBObject();
		dbo.put("seller", AuthenticationUtil.getCurrentUser());
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find(dbo);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			result.add(o.toMap());
		}
		return result;
	}
	
	@RestService(method="GET", uri="/seller/request")
	public Map<String, Object> getRequest(@RestParam(required=true, value="id") String id) {
		
		DBCollection coll = dataSource.getCollection("sells");
		DBObject dbo = new BasicDBObject();
		dbo.put("_id", new ObjectId(id));
		DBObject e = coll.findOne(dbo);
		Map m = e.toMap();
		
		InputStream is = contentStore.getContentData(e.get("content").toString());
		byte[] bytes;
		try {
			bytes = FileCopyUtils.copyToByteArray(is);
			m.put("content", new String(bytes, "UTF-8"));
		} catch (IOException exception) {
		}
		return m;
	}
	
	
	@RestService(method="POST", uri="/seller/drop")
	public void dropSale(@RestParam(required=true, value="id") String id) {
		DBCollection coll = dataSource.getCollection("sells");
		DBObject dbo = new BasicDBObject();
		dbo.put("_id", new ObjectId(id));
		DBObject e = coll.findOne(dbo);
		if (Boolean.FALSE.equals(e.get("online")) && AuthenticationUtil.getCurrentUser().equals(e.get("seller"))) {
			coll.remove(dbo);
		}
	}
	
	
	
	
	@RestService(method="GET", uri="/sonline")
	public List<Map<String, Object>> listOnlines() {
		DBCollection coll = dataSource.getCollection("sells");
		DBObject dbo = new BasicDBObject();
		dbo.put("seller", AuthenticationUtil.getCurrentUser());
		dbo.put("online", true);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find();
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			result.add(o.toMap());
		}
		return result;
	}
	
	
	@RestService(method="GET", uri="/sfinish")
	public List<Map<String, Object>> listfinished() {
		DBCollection coll = dataSource.getCollection("finished");
		DBObject dbo = new BasicDBObject();
		dbo.put("seller", AuthenticationUtil.getCurrentUser());
		dbo.put("online", true);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find();
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			result.add(o.toMap());
		}
		return result;
	}

	@RestService(method="GET", uri="/sdetail")
	public Map<String, Object> getDetail(@RestParam(required=true, value="id") String id) {
		DBCollection coll = dataSource.getCollection("sells");
		return coll.findOne(new BasicDBObject("_id", new ObjectId(id))).toMap();
	}
	
	
	@RestService(method="GET", uri="/content")
	public String loadContent(@RestParam(required=true, value="id") String id) {
		InputStream is = contentStore.getContentData(id);
		byte[] bytes;
		try {
			bytes = FileCopyUtils.copyToByteArray(is);
			return new String(bytes, "UTF-8");
		} catch (IOException e) {
			throw new HttpStatusException(HttpStatus.PRECONDITION_FAILED);
		}
	}
	
	@RestService(uri="/preview/attach", method="POST", multipart=true)
	public String uploadPreview(@RestParam(value="file")InputStream is, @RestParam(value="size")Long size) {
		String uid = UUID.randomUUID().toString();
		contentStore.putContent(uid, is, "image/png", size);
		return uid;
	}
	
	@RestService(uri="/preview", method="GET", multipart=true)
	public InputStream uploadPreview(@RestParam(value="id") String id) {
		return contentStore.getContentData(id);
	}
}
