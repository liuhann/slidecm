package com.ever365.qiang;

import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class SellerService {
	private MongoDataSource dataSource;

	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@RestService(method="POST", uri="/sreg")
	public void register(@RestParam(required=true, value="name") String name,
			@RestParam(required=true, value="shop") String shop,
			@RestParam(required=true, value="user") String user,
			@RestParam(required=true, value="phone") String phone,
			@RestParam(required=true, value="email") String email,
			@RestParam(required=true, value="other") String other
			) {
		DBCollection coll = dataSource.getCollection("seller");
		
		DBObject dbo = new BasicDBObject();
		dbo.put("name", name);
		dbo.put("shop", shop);
		dbo.put("user", user);
		dbo.put("phone", phone);
		dbo.put("email", email);
		dbo.put("other", other);
		
		dbo.put("pass", false);

		coll.insert(dbo);
	}
	
	
}
