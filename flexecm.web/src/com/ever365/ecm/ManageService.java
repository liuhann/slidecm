package com.ever365.ecm;

import org.bson.types.ObjectId;

import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.RestService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ManageService {
	
	private MongoDataSource dataSource;

	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@RestService(uri="/manage/trans", method="GET", authenticated=false)
	public void translateContent() {
		DBCursor cursor = dataSource.getCollection("entities").find();
		
		while (cursor.hasNext()) {
			DBObject dbo = cursor.next();
			
			if ("s:file".equals(dbo.get("type"))) {
				try {
					ObjectId oid = new ObjectId(dbo.get("tn").toString());
					DBObject contentdbo = dataSource.getCollection("contents").findOne(new BasicDBObject("_id", oid));
					dbo.put("otn", dbo.get("tn"));
					dbo.put("tn", contentdbo.get("url"));
				} catch (Exception e ){
				}

				try {
					ObjectId oid = new ObjectId(dbo.get("url").toString());
					DBObject contentdbo = dataSource.getCollection("contents").findOne(new BasicDBObject("_id", oid));
					dbo.put("ourl", dbo.get("url"));
					dbo.put("url", contentdbo.get("url"));
				} catch (Exception e ){
				}
				dataSource.getCollection("entities").update(new BasicDBObject("_id", dbo.get("_id")), dbo);
			}
		}
		
		
	}

}
