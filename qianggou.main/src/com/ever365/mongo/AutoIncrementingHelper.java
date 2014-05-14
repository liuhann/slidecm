package com.ever365.mongo;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Liu Han
 */
public class AutoIncrementingHelper {
	
	private MongoDataSource dataSource;
	private Long x = 1L;
	
	private Map<String, Integer> incMap = new HashMap<String, Integer>();
	
	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
		incMap.put("seq", 1);
	}

	public void initIncreasor(String name) {
		if (dataSource.getCollection("counters").findOne(new BasicDBObject("_id", name))==null) {
			DBObject dbo = new BasicDBObject();
			dbo.put("_id", name);
			dbo.put("seq", 0L);
			dataSource.getCollection("counters").insert(dbo);
			x = 0L;
		}
	}
	
	public Long getNextSequence(String name) {
		BasicDBObject query = new BasicDBObject("_id", name);
		DBObject update = new BasicDBObject();
		update.put("$inc", incMap);
		
		DBObject dbo = dataSource.getCollection("counters").findAndModify(query, update);
		return (Long) dbo.get("seq");
	}
	
}
