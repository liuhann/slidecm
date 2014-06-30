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
	
	private static final String BOOK_COUNT_PREFIX = "B";
	private static final String DEALS_COUNT_PREFIX = "D";

	
	private static final String COLL_DEALS = "deals";
	private static final String COLL_BOOKS = "books";
	private static final String COLL_SALES = "sales";
	private static final String COLL_SELLER = "sellers";
	
	private static final String FIELD_BOOK_TIME = "t";
	private static final String FIELD_BOOK_USER = "u";
	private static final String FIELD_BOOK_SALE_ID = "m";
	
	private static final String FIELD_ONSALE_TIME = "time";
	private static final String FIELD_PRICE = "price";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_SELLER = "seller";
	private static final String FIELD_BOOK_CODE = "o";
	private static final String FIELD_COUNT = "count";
	private static final String FIELD_IS_ONLINE = "online";
	private static final String FIELD_SALE_ID = "seq";
	private static final String FIELD_CONTENT = "content";
	private static final String FIELD_ID = "_id";

	
	private static final String STRING_EMPTY = "";
	
	private MongoDataSource dataSource;
	private ContentStore contentStore;
	private AutoIncrementingHelper incrementingHelper;
	private RepostService repostService;
	
	public void setIncrementingHelper(AutoIncrementingHelper incrementingHelper) {
		this.incrementingHelper = incrementingHelper;
		incrementingHelper.initIncreasor("sales");
	}

	public void setRepostService(RepostService repostService) {
		this.repostService = repostService;
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
	
	@RestService(method="GET", uri="/seller/info")
	public Map<String, Object> getSellerInfo() {
		DBCollection coll = dataSource.getCollection(COLL_SELLER);

		DBObject exist = coll.findOne(new BasicDBObject("name",AuthenticationUtil.getCurrentUser()));
		if (exist==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		return exist.toMap();
	}
	
	@RestService(method="POST", uri="/seller/info")
	public Map setSellerInfo(@RestParam(required=true, value="name") String name,
			@RestParam(required=true, value="shop") String shop,
			@RestParam(required=true, value="user") String user,
			@RestParam(required=true, value="phone") String phone,
			@RestParam(required=true, value="email") String email,
			@RestParam(required=true, value="other") String other) {
		DBCollection coll = dataSource.getCollection(COLL_SELLER);

		DBObject dbo = coll.findOne(new BasicDBObject("name",AuthenticationUtil.getCurrentUser()));
		if (dbo==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		dbo.put("shop", shop);
		dbo.put("user", user);
		dbo.put("phone", phone);
		dbo.put("email", email);
		dbo.put("other", other);
		coll.update(new BasicDBObject("name",AuthenticationUtil.getCurrentUser()), dbo);
		return dbo.toMap();
	}
	
	@RestService(method="POST", uri="/seller/password")
	public Integer setSellerPwd(@RestParam(required=true, value="old") String oldpass,
			@RestParam(required=true, value="new") String newpass) {
		DBCollection coll = dataSource.getCollection(COLL_SELLER);

		DBObject dbo = coll.findOne(new BasicDBObject("name",AuthenticationUtil.getCurrentUser()));
		if (dbo==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		
		if (oldpass.equals(dbo.get("pass"))) {
			dbo.put("pass", newpass);
			coll.update(new BasicDBObject("name",AuthenticationUtil.getCurrentUser()), dbo);
			return 1;
		} else {
			return -1;
		}
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
	
	@RestService(method="POST", uri="/seller/request")
	public void request(
			@RestParam(required=false, value="id") String id,
			@RestParam(required=true, value="title") String title,
			@RestParam(required=false, value="subtitle") String subtitle,
			@RestParam(required=true, value="url") String url,
			@RestParam(required=true, value="count") String count,
			@RestParam(required=true, value="price") String price,
			@RestParam(required=true, value="oprice") String oprice,
			@RestParam(required=true, value=FIELD_ONSALE_TIME) String time,
			@RestParam(required=true, value="until") String until,
			@RestParam(required=false, value="preview") String preview,
			@RestParam(required=true, value="content") String content
			) {
		try {
			DBCollection coll = dataSource.getCollection(COLL_SALES);
			DBObject dbo = new BasicDBObject();
			dbo.put(FIELD_SELLER, AuthenticationUtil.getCurrentUser());
			
			dbo.put(FIELD_TITLE, title);
			dbo.put("subtitle", subtitle);
			dbo.put(FIELD_COUNT, new Integer(count));
			dbo.put("url", url);
			dbo.put(FIELD_PRICE, price);
			dbo.put(FIELD_ONSALE_TIME, StringUtils.parseDate(time).getTime());
			dbo.put(FIELD_IS_ONLINE, false);
			dbo.put("oprice", oprice);
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
				dbo.put(FIELD_SALE_ID, incrementingHelper.getNextSequence("sales"));
				coll.insert(dbo);
			} else {
				DBObject exsit = coll.findOne(new BasicDBObject(FIELD_ID, new ObjectId(id)));
				
				if (exsit==null) {
					throw new HttpStatusException(HttpStatus.BAD_REQUEST);
				}
				dbo.put(FIELD_SALE_ID, exsit.get(FIELD_SALE_ID));
				coll.update(new BasicDBObject(FIELD_ID, new ObjectId(id)), dbo, true, false);
			}
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RestService(method="GET", uri="/seller/request/list")
	public List<Map<String, Object>> getRequesting() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_SELLER, AuthenticationUtil.getCurrentUser());
		dbo.put(FIELD_IS_ONLINE, false);
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find(dbo);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			Map m = o.toMap();
			Long n = incrementingHelper.getCurrentSequence(BOOK_COUNT_PREFIX + m.get(FIELD_SALE_ID));
			m.put("books", n);
			result.add(m);
		}
		return result;
	}
	
	@RestService(method="GET", uri="/seller/online/list")
	public List<Map<String, Object>> getOnlines() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_SELLER, AuthenticationUtil.getCurrentUser());
		dbo.put(FIELD_IS_ONLINE, true);
		dbo.put(FIELD_ONSALE_TIME, MapUtils.newMap("$gt", System.currentTimeMillis()));
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find(dbo);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			Map m = o.toMap();
			Long n = incrementingHelper.getCurrentSequence(BOOK_COUNT_PREFIX + m.get(FIELD_SALE_ID));
			m.put("books", n);
			result.add(m);
		}
		return result;
	}

	@RestService(method="GET", uri="/seller/finished/list")
	public List<Map<String, Object>> getFinished() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_SELLER, AuthenticationUtil.getCurrentUser());
		dbo.put(FIELD_IS_ONLINE, true);
		dbo.put(FIELD_ONSALE_TIME, MapUtils.newMap("$lt", System.currentTimeMillis()));
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		 
		DBCursor cursor = coll.find(dbo);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			Map m = o.toMap();
			Long n = incrementingHelper.getCurrentSequence(BOOK_COUNT_PREFIX + m.get(FIELD_SALE_ID));
			m.put("books", n);
			result.add(m);
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
		Long n = incrementingHelper.getCurrentSequence(BOOK_COUNT_PREFIX + m.get(FIELD_SALE_ID));
		m.put("books", n);
		m.put(FIELD_CONTENT, getContent(e.get(FIELD_CONTENT).toString()));
		return m;
	}
	
	@RestService(uri="/admin/sale", method="GET", authenticated=true)
	public Map<String, Object> getFullSaleInfo(@RestParam(value="id") Integer id) {
		try {
			Map m = getSaleBySeq(id);
			if (!m.get(FIELD_SELLER).equals(AuthenticationUtil.getCurrentUser())) {
				m.remove(FIELD_COUNT);
				m.remove(FIELD_ID);
			}
			return m;
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
	}
	
	
	@RestService(method="GET", uri="/seller/sale/content")
	public String getContent(@RestParam(required=true, value="id") String id) {
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
	
	@RestService(method="GET", uri="/seller/books")
	public Map<String, Object> getBooksList(@RestParam(required=true, value="id") Integer id, 
			@RestParam(required=true, value="skip") Integer skip,
			@RestParam(required=true, value="limit") Integer limit) {
		checkOwnable(id);
		
		Map<String, Object> result = new HashMap<String, Object>();
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

	public void checkOwnable(Integer id) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_SALE_ID, id);
		DBObject e = coll.findOne(dbo);
		
		if (e==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		if (!AuthenticationUtil.getCurrentUser().equals(e.get(FIELD_SELLER)) && !AuthenticationUtil.isAdmin()) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
	}
	
	@RestService(method="GET", uri="/seller/deals")
	public List<Map<String, Object>> getDeals(@RestParam(required=true, value="id") Integer id) {
		checkOwnable(id);
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		DBCursor cursor = dataSource.getCollection(COLL_DEALS).find(new BasicDBObject(FIELD_BOOK_SALE_ID, id));
		
		while (cursor.hasNext()) {
			list.add(cursor.next().toMap());
		}
		return list;
	}
	

	@RestService(uri="/sale", method="GET", authenticated=false)
	public Map<String, Object> getSale(@RestParam(value="id") Integer id) {
		try {
			Map<String, Object> m = new HashMap<String, Object>();
			m.putAll(getSaleShortInfo(id));
			if (m==null) {
				throw new HttpStatusException(HttpStatus.NOT_FOUND);
			}
			if (AuthenticationUtil.getCurrentUser()!=null) {
				m.putAll(bookDetail(id));
			}
			
			if (((Long)m.get(FIELD_ONSALE_TIME)) < System.currentTimeMillis()) {
				if (getSaleCount(id)<0) {
					m.put("f", true);
				}
				m.put("d", getDealCode(id));
			}
			return m;
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
	}
	
	public Map<Integer, Map> saleShortCache = new HashMap<Integer, Map>();
	public Map<Integer, Integer> saleCountCache = new HashMap<Integer, Integer>();
	
	public Integer getSaleCount(Integer id) {
		if (saleCountCache.get(id)==null) {
			DBCollection coll = dataSource.getCollection(COLL_SALES);
			DBObject dbo = new BasicDBObject();
			dbo.put(FIELD_SALE_ID, id);
			DBObject e = coll.findOne(dbo);
			if (e==null) {
				return null;
			}
			Long bn = incrementingHelper.getCurrentSequence(DEALS_COUNT_PREFIX + id);
			
			if (bn.intValue()>=(Integer)e.get(FIELD_COUNT)) {
				saleCountCache.put(id, -1);
			} else {
				saleCountCache.put(id, (Integer)e.get(FIELD_COUNT));
			}
		}
		return saleCountCache.get(id);
	}
	
	public Map getSaleShortInfo(Integer id) {
		if (saleShortCache.get(id)==null) {
			DBCollection coll = dataSource.getCollection(COLL_SALES);
			DBObject dbo = new BasicDBObject();
			dbo.put(FIELD_SALE_ID, id);
			DBObject e = coll.findOne(dbo);
			if (e==null) {
				return null;
			}
			Map m = e.toMap();
			m.remove(FIELD_COUNT);
			m.remove(FIELD_ID);
			
			saleShortCache.put(id, m);
			Long bn = incrementingHelper.getCurrentSequence(DEALS_COUNT_PREFIX + id);
			
			if (bn.intValue()>=(Integer)e.get(FIELD_COUNT)) {
				saleCountCache.put(id, -1);
			} else {
				saleCountCache.put(id, (Integer)e.get(FIELD_COUNT));
			}
			
			return m;
		}
		return saleShortCache.get(id);
	}

	public Map getSaleBySeq(Integer id) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_SALE_ID, id);
		DBObject e = coll.findOne(dbo);
		if (e==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		Map m = e.toMap();
		m.put(FIELD_CONTENT, getContent(e.get(FIELD_CONTENT).toString()));
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
	
	@RestService(uri="/books", method="GET")
	public List<Map> getMyBooks() {
		List<Map> m = new ArrayList<Map>();
		
		DBCollection bookColl = dataSource.getCollection(COLL_BOOKS);
		
		DBObject query = new BasicDBObject();
		query.put(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser());

		DBCursor cursor = bookColl.find(query);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			Map t = o.toMap();
			t.put("sale", getSaleShortInfo((Integer)o.get(FIELD_BOOK_SALE_ID)));
			m.add(t);
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
		Long n = incrementingHelper.getNextSequence(BOOK_COUNT_PREFIX + id);
		
		Date now = new Date();
		book.put(FIELD_BOOK_CODE, new StringBuilder().append("M").append(1000+id).append(now.getDate()).append(1000+n).append(now.getTime()%1000).toString());
		bookColl.update(exsitQuery, book, true, false);
		return book.toMap();
	}
	
	@RestService(uri="/buy", method="POST", webcontext=true)
	public synchronized Map<String, Object> buy(@RestParam(value="id") Integer id) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		//检查是否卖完
		if (getSaleCount(id)==-1) {
			result.put("status", 410);
			return result;
		}
		
		Object time = getSaleShortInfo(id).get(FIELD_ONSALE_TIME);
		if (time!=null && ((Long)time) > System.currentTimeMillis()) {
			result.put("status", 400);
			return result;
		}
	
		Long bn = incrementingHelper.getNextSequence(DEALS_COUNT_PREFIX + id);
		DBCollection buyCollection = dataSource.getCollection(COLL_DEALS);
		
		//生成订单号
		DBObject dbo = new BasicDBObject();
		dbo.put(FIELD_BOOK_SALE_ID, id);
		dbo.put("bn", bn);
		dbo.put(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser());
		dbo.put(FIELD_BOOK_TIME, System.currentTimeMillis());
		dbo.put(FIELD_BOOK_CODE, com.ever365.utils.UUID.generateShortUuid());
		
		result.putAll(dbo.toMap());
		buyCollection.update(BasicDBObjectBuilder.start(FIELD_BOOK_SALE_ID, id).add(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser()).get(), dbo, true, false);
		result.put("status", 200);
		
		if (bn>=getSaleCount(id)) {
			saleCountCache.put(id, -1);
		}
		
		return result;
	}
	
	@RestService(uri="/deals", method="GET")
	public List<Map> getMyDeals() {
			List<Map> mydeals = new ArrayList<Map>();
		
		DBCollection bookColl = dataSource.getCollection(COLL_DEALS);
		DBObject query = new BasicDBObject();
		query.put(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser());

		DBCursor cursor = bookColl.find(query);
		
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			Map t = o.toMap();
			t.put("sale", getSaleShortInfo((Integer)o.get(FIELD_BOOK_SALE_ID)));
			mydeals.add(t);
		}
		return mydeals;
	}
	
	@RestService(uri="/deals/code", method="GET")
	public String getDealCode(@RestParam(value="id") Integer id) {
		DBCollection bookColl = dataSource.getCollection(COLL_DEALS);
		DBObject query = new BasicDBObject();
		query.put(FIELD_BOOK_USER, AuthenticationUtil.getCurrentUser());
		query.put(FIELD_BOOK_SALE_ID, id);
		
		DBObject one = bookColl.findOne(query);
		if (one==null) return null;
		return one.get(FIELD_BOOK_CODE).toString();
	}

	@RestService(uri="/seller/admin/request/list", method="GET", runAsAdmin=true)
	public List<Map<String, Object>> getAllRequesting() {
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
	public List<Map<String, Object>> getAllOnlines() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		
		DBObject query = new BasicDBObject();
		query.put(FIELD_IS_ONLINE, true);
		query.put(FIELD_ONSALE_TIME, MapUtils.newMap("$gt", System.currentTimeMillis()));
		
		DBCursor cursor = coll.find(query);
		
		List<Map<String, Object>> requesting = new ArrayList<Map<String,Object>>();
		while (cursor.hasNext()) {
			DBObject dbo = cursor.next();
			requesting.add(dbo.toMap());
		}
		return requesting;
	}
	
	@RestService(uri="/seller/admin/finished/list", method="GET", runAsAdmin=true)
	public List<Map<String, Object>> getAllFinished() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		
		DBObject query = new BasicDBObject();
		query.put(FIELD_IS_ONLINE, true);
		query.put(FIELD_ONSALE_TIME, MapUtils.newMap("$lt", System.currentTimeMillis()));
		
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
			sale.put(FIELD_SALE_ID, incrementingHelper.getNextSequence(COLL_SALES));
			incrementingHelper.initIncreasor(BOOK_COUNT_PREFIX + sale.get(FIELD_SALE_ID));
		} else {
			sale.put(FIELD_IS_ONLINE, false);
			sale.removeField(FIELD_SALE_ID);
		}
		coll.update(new BasicDBObject(FIELD_ID, new ObjectId(id)), sale);
	}
	
	@RestService(uri="/seller/admin/recommend", method="POST", runAsAdmin=true) 
	public void setRecommond(@RestParam(value="id") Integer id, @RestParam(value="set") String set) {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBObject query = new BasicDBObject(FIELD_SALE_ID, id);
		
		if (set.equals("1")) {
			DBObject update = new BasicDBObject("$set", MapUtils.newMap("pushed", true));
			coll.update(query, update);
		} else {
			DBObject update = new BasicDBObject("$set", MapUtils.newMap("pushed", null));
			coll.update(query, update);
		}
	}
	
	@RestService(uri="/shengo/youhui", method="POST", runAsAdmin=true)
	public void addYouHui(@RestParam(value="uri") String uri, @RestParam(value="price") String price, 
			@RestParam(value="title") String title, @RestParam(value="oprice") String oprice, @RestParam(value="sold") String sold,
			@RestParam(value="img") String img) {
		
	}
	
	@RestService(uri="/shengo/index", method="GET", authenticated=false)
	public Map<String, Object> getSaleIndex() {
		Map<String, Object> index = new HashMap<String, Object>();
		index.put("cu", AuthenticationUtil.getCurrentUser());
		index.put("t", System.currentTimeMillis());
		
		/*
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		DBCursor adscur = coll.find(new BasicDBObject("pushed",true));
		List<Map> ads = new ArrayList<Map>();
		
		while (adscur.hasNext()) {
			ads.add(getSaleShortInfo(((Long)adscur.next().get(FIELD_SALE_ID)).intValue()));
		}
		index.put("ads", ads);
		*/
		index.put("recents", getRecents());
		index.put("weibo", getWeibos());
		return index;
	}

	public List<Map<String, Object>> getWeibos() {
		return repostService.getAllOnlinePosts();
	}
	
	@RestService(uri="/sale/recent", method="GET", authenticated=false)
	public List<Map> getRecents() {
		DBCollection coll = dataSource.getCollection(COLL_SALES);
		
		DBObject query = new BasicDBObject();
		
		query.put(FIELD_ONSALE_TIME, MapUtils.newMap("$gt", System.currentTimeMillis()));
		query.put(FIELD_IS_ONLINE, true);
		
		DBCursor cursor = coll.find(query).sort(new BasicDBObject(FIELD_ONSALE_TIME, 1)).limit(20);
		
		List<Map> result = new ArrayList<Map>();
		while(cursor.hasNext()) {
			result.add(getSaleShortInfo(((Long)cursor.next().get(FIELD_SALE_ID)).intValue()));
		}
		return result;
	}
	
	
	
	
	
}
