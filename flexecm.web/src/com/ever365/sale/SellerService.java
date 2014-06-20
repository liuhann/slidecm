package com.ever365.sale;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.util.FileCopyUtils;

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
import com.ever365.utils.MapUtils;
import com.ever365.utils.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SellerService {
	
	private static final String COLL_BOOKS = "books";
	private static final String COLL_SALES = "sales";
	private static final String COLL_SELLER = "sellers";
	
	
	private static final String FIELD_BOOK_TIME = "t";
	private static final String FIELD_BOOK_USER = "u";
	private static final String FIELD_BOOK_SALE_ID = "m";
	
	private static final String FIELD_PRICE = "price";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_SELLER = "seller";
	private static final String FIELD_BOOK_CODE = "o";
	private static final String FIELD_COUNT = "count";
	private static final String FIELD_IS_ONLINE = "online";
	private static final String FIELD_ONLINE_SEQ = "seq";
	private static final String FIELD_CONTENT = "content";
	private static final String FIELD_ID = "_id";

	
	private static final String STRING_EMPTY = "";
	
	private MongoDataSource dataSource;
	private ContentStore contentStore;
	private AutoIncrementingHelper incrementingHelper;
	
	public void setIncrementingHelper(AutoIncrementingHelper incrementingHelper) {
		this.incrementingHelper = incrementingHelper;
		incrementingHelper.initIncreasor("sales");
	}

	public void setContentStore(ContentStore contentStore) {
		this.contentStore = contentStore;
	}

	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@RestService(method="POST", uri="/seller/register", authenticated=false)
	public RestResult register(@RestParam(required=true, value="name") String name,
			@RestParam(required=true, value="shop") String shop,
			@RestParam(required=true, value="pass") String pass,
			@RestParam(required=true, value="user") String user,
			@RestParam(required=true, value="phone") String phone,
			@RestParam(required=true, value="email") String email,
			@RestParam(required=true, value="other") String other
			) {
		DBCollection coll = dataSource.getCollection(COLL_SELLER);

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
	
	@RestService(method="POST", uri="/seller/login", authenticated=false)
	public RestResult login(@RestParam(required=true, value="name") String name,
			@RestParam(required=true, value="pass") String pass
			) {
		DBCollection coll = dataSource.getCollection(COLL_SELLER);

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
	
	@RestService(method="GET", uri="/seller/test/login", authenticated=false)
	public RestResult login() {
		RestResult rr = new RestResult();
		Map<String, Object> session = new HashMap<String, Object>();
		session.put(AuthenticationUtil.SESSION_CURRENT_USER, "test");
		rr.setSession(session);
		rr.setRedirect("/");
		return rr;
	}
	
	@RestService(method="GET", uri="/seller/logout")
	public RestResult logout() {
		RestResult rr = new RestResult();
		Map<String, Object> session = new HashMap<String, Object>();
		session.put(AuthenticationUtil.SESSION_CURRENT_USER, null);
		rr.setSession(session);
		rr.setRedirect("/");
		return rr;
	}
	
	@RestService(method="GET", uri="/seller/info")
	public Map<String, Object> getSellerInfo() {
		DBCollection coll = dataSource.getCollection(COLL_SELLER);
		DBObject exist = coll.findOne(new BasicDBObject("name",AuthenticationUtil.getCurrentUser()));
		if (exist==null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		} else {
			return exist.toMap();
		}
	}
	
	@RestService(method="POST", uri="/seller/request")
	public void request(
			@RestParam(required=false, value="id") String id,
			@RestParam(required=true, value=FIELD_TITLE) String title,
			@RestParam(required=true, value="subtitle") String subtitle,
			@RestParam(required=true, value=FIELD_COUNT) String count,
			@RestParam(required=true, value=FIELD_PRICE) String price,
			@RestParam(required=true, value="oprice") String oprice,
			@RestParam(required=true, value="time") String time,
			@RestParam(required=true, value="until") String until,
			@RestParam(required=false, value="preview") String preview,
			@RestParam(required=true, value=FIELD_CONTENT) String content
			) {
		try {
			DBCollection coll = dataSource.getCollection(COLL_SALES);
			DBObject dbo = new BasicDBObject();
			dbo.put(FIELD_SELLER, AuthenticationUtil.getCurrentUser());
			
			dbo.put(FIELD_TITLE, title);
			dbo.put("subtitle", subtitle);
			dbo.put(FIELD_COUNT, new Integer(count));
			dbo.put(FIELD_PRICE, new Integer(price));
			dbo.put("time", StringUtils.parseDate(time).getTime());
			dbo.put(FIELD_IS_ONLINE, false);
			dbo.put("oprice", new Integer(oprice));
			dbo.put("preview", preview);
			
			String contentId = UUID.randomUUID().toString();
			
			dbo.put(FIELD_CONTENT, contentId);
			
			if (until.equals(STRING_EMPTY)) {
				dbo.put("until", STRING_EMPTY);
			} else {
				dbo.put("until", StringUtils.parseDate(until).getTime());
			}
			
			try {
				contentStore.putContent(contentId, new ByteArrayInputStream(content.getBytes("utf-8")), "text/html", content.length());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			if (id==null) { 
				//初始化一个序列号的号码，就作为销售次序码
				dbo.put(FIELD_ONLINE_SEQ, incrementingHelper.getNextSequence("sales"));
				coll.insert(dbo);
			} else {
				coll.update(new BasicDBObject(FIELD_ID, new ObjectId(id)), dbo, true, false);
			}
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RestService(method="GET", uri="/seller/list")
	public List<Map<String, Object>> listRequesting() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_SELLER, AuthenticationUtil.getCurrentUser());
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find(dbo);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			result.add(o.toMap());
		}
		return result;
	}
	
	@RestService(method="GET", uri="/seller/sale")
	public Map<String, Object> getRequest(@RestParam(required=true, value="id") String id) {
		
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_ID, new ObjectId(id));
		DBObject e = coll.findOne(dbo);
		
		if (e==null) throw new HttpStatusException(HttpStatus.NOT_FOUND);
		
		Map m = e.toMap();
		m.put(FIELD_CONTENT, loadContent(e.get(FIELD_CONTENT).toString()));
		return m;
	}

	@RestService(method="GET", uri="/seller/sale/content")
	public String loadContent(@RestParam(required=true, value="id") String id) {
		StreamObject so = contentStore.getContentData(id);
		byte[] bytes;
		try {
			bytes = FileCopyUtils.copyToByteArray(so.getInputStream());
			return new String(bytes, "UTF-8");
		} catch (IOException e) {
			throw new HttpStatusException(HttpStatus.PRECONDITION_FAILED);
		}
	}
	
	@RestService(method="POST", uri="/seller/sale/drop")
	public void dropSale(@RestParam(required=true, value="id") String id) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_ID, new ObjectId(id));
		DBObject e = coll.findOne(dbo);
		if (!Boolean.TRUE.equals(e.get(FIELD_IS_ONLINE)) && AuthenticationUtil.getCurrentUser().equals(e.get(FIELD_SELLER))) {
			coll.remove(dbo);
		}
	}
	
	@RestService(uri="/preview/attach", method="POST", multipart=true)
	public String uploadPreview(@RestParam(value="file")InputStream is, @RestParam(value="size")Long size) {
		String uid = UUID.randomUUID().toString();
		contentStore.putContent(uid, is, "image/png", size);
		return uid;
	}
	
	@RestService(uri="/preview", method="GET", authenticated=false)
	public StreamObject getPreview(@RestParam(value="id") String id) {
		return contentStore.getContentData(id);
	}
	
	@RestService(method="GET", uri="/seller/sale/books")
	public Map<String, Object> getBooksList(@RestParam(required=true, value="id") Integer id, 
			@RestParam(required=true, value="skip") Integer skip,
			@RestParam(required=true, value="limit") Integer limit) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_ONLINE_SEQ, id);
		DBObject e = coll.findOne(dbo);
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (e==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		if (!AuthenticationUtil.getCurrentUser().equals(e.get(FIELD_SELLER)) && !AuthenticationUtil.isAdmin()) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		
		DBCursor cursor = dataSource.getCollection(COLL_BOOKS).find(new BasicDBObject(FIELD_BOOK_SALE_ID, id));
		
		result.put("total", cursor.count());
		cursor.skip(skip).limit(limit);
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		result.put("list", list);
		return result;
	}
	
	
	
	@RestService(uri="/seller/admin/request/list", method="GET", runAsAdmin=true)
	public List<Map<String, Object>> getRequesting() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		
		DBCursor cursor = coll.find(new BasicDBObject(FIELD_IS_ONLINE, false));
		List<Map<String, Object>> requesting = new ArrayList<Map<String,Object>>();
		while (cursor.hasNext()) {
			DBObject dbo = cursor.next();
			requesting.add(dbo.toMap());
		}
		return requesting;
	}
	
	@RestService(uri="/seller/admin/online/list", method="GET", runAsAdmin=true)
	public List<Map<String, Object>> getOnlines() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		
		DBObject query = new BasicDBObject();
		query.put(FIELD_IS_ONLINE, true);
		query.put("time", MapUtils.newMap("$gt", System.currentTimeMillis()));
		
		DBCursor cursor = coll.find(query);
		
		List<Map<String, Object>> requesting = new ArrayList<Map<String,Object>>();
		while (cursor.hasNext()) {
			DBObject dbo = cursor.next();
			requesting.add(dbo.toMap());
		}
		return requesting;
	}
	
	@RestService(uri="/seller/admin/finished/list", method="GET", runAsAdmin=true)
	public List<Map<String, Object>> getFinished() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		
		DBObject query = new BasicDBObject();
		query.put(FIELD_IS_ONLINE, true);
		query.put("time", MapUtils.newMap("$lt", System.currentTimeMillis()));
		
		DBCursor cursor = coll.find(query);
		
		List<Map<String, Object>> requesting = new ArrayList<Map<String,Object>>();
		while (cursor.hasNext()) {
			DBObject dbo = cursor.next();
			requesting.add(dbo.toMap());
		}
		return requesting;
	}

	@RestService(uri="/seller/admin/request/approve", method="POST", runAsAdmin=true)
	public void approve(@RestParam(value="id")String id, @RestParam(value="on")String on) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject sale = coll.findOne(new BasicDBObject(FIELD_ID, new ObjectId(id)));
		if ("1".equals(on)) {
			sale.put(FIELD_IS_ONLINE, true);
			sale.put(FIELD_ONLINE_SEQ, incrementingHelper.getNextSequence("sales"));
			incrementingHelper.initIncreasor("S" + sale.get(FIELD_ONLINE_SEQ));
		} else {
			sale.put(FIELD_IS_ONLINE, false);
			sale.removeField(FIELD_ONLINE_SEQ);
		}
		coll.update(new BasicDBObject(FIELD_ID, new ObjectId(id)), sale);
	}
	
	@RestService(uri="/sale", method="GET", authenticated=false)
	public Map<String, Object> getSale(@RestParam(value="id") Integer id) {
		try {
			Map m = getSaleBySeq(id);
			
			if (!m.get(FIELD_SELLER).equals(AuthenticationUtil.getCurrentUser())) {
				m.remove(FIELD_COUNT);
				m.remove(FIELD_ID);
			}
			if (AuthenticationUtil.getCurrentUser()!=null) {
				m.putAll(bookDetail(id));
			}
			return m;
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
	}

	public Map getSaleBySeq(Integer id) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_ONLINE_SEQ, id);
		DBObject e = coll.findOne(dbo);
		if (e==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		Map m = e.toMap();
		m.put(FIELD_CONTENT, loadContent(e.get(FIELD_CONTENT).toString()));
		return m;
	}

	@RestService(uri="/book", method="GET", authenticated=false) 
	public Map<String, Object> bookDetail(@RestParam(value="id") Integer id) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("cu", AuthenticationUtil.getCurrentUser());
		
		if (AuthenticationUtil.getCurrentUser()==null) return m;
		
		DBCollection bookColl = dataSource.getCollection(COLL_BOOKS);
		DBObject exsitQuery = BasicDBObjectBuilder.start(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser())
				.add(FIELD_BOOK_SALE_ID, id).get();

		DBObject one = bookColl.findOne(exsitQuery);
		if (one!=null) {
			m.putAll(one.toMap());
		}
		return m;
	}
	
	@RestService(uri="/book", method="POST", webcontext=true) 
	public Map<String, Object> book(@RestParam(value="id") Integer id) {
		DBCollection bookColl = dataSource.getCollection(COLL_BOOKS);
		DBObject exsitQuery = BasicDBObjectBuilder.start(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser())
				.add(FIELD_BOOK_SALE_ID, id).get();

		DBObject book = new BasicDBObject();
		book.put(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser());
		book.put(FIELD_BOOK_TIME, System.currentTimeMillis());
		book.put(FIELD_BOOK_SALE_ID, id);
		book.put("ip", WebContext.getRemoteAddr());
		Long n = incrementingHelper.getNextSequence("S" + id);
		
		Date now = new Date();
		book.put(FIELD_BOOK_CODE, new StringBuilder().append("M").append(1000+id).append(now.getDate()).append(1000+n).append(now.getTime()%1000).toString());
		bookColl.update(exsitQuery, book, true, false);
		return book.toMap();
	}
	
	@RestService(uri="/buy", method="POST", webcontext=true) 
	public Map<String, Object> buy(@RestParam(value="id") Integer id) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> book = bookDetail(id);
		if (book.get(FIELD_BOOK_CODE)==null) {
			result.put("error", 9);
			return result;
		}
		
		Map sale = getSaleBySeq(id);
		Integer count = (Integer)sale.get(FIELD_COUNT);
		Long current = incrementingHelper.getCurrentSequence("B" + id);
		if (current.intValue()>count) {
			result.put("error", 8);
			return result;
		}
		
		Long bn = incrementingHelper.getNextSequence("B" + id);
		
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_BOOK_SALE_ID, id);
		dbo.put(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser());
		dbo.put(FIELD_BOOK_TIME, System.currentTimeMillis());
		dbo.put("order", com.ever365.utils.UUID.generateShortUuid());
		
		DBCollection buyCollection = dataSource.getCollection("deals");
		result.putAll(dbo.toMap());
		
		buyCollection.insert(dbo);
		return result;
	}
}
