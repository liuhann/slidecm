package com.ever365.mongo;

import java.util.Map;

import com.ever365.utils.MapUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Liu Han
 */
public class AutoIncrementingHelper {
	
	private MongoDataSource dataSource;
	
	private Map<String, Integer> incMap = MapUtils.newMap("seq", 1);
	
	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void initIncreasor(String name) {
		try {
			if (dataSource.getCollection("counters").findOne(new BasicDBObject("_id", name))==null) {
				DBObject dbo = new BasicDBObject();
				dbo.put("_id", name);
				dbo.put("seq", 2L);
				dataSource.getCollection("counters").insert(dbo);
			} 
		} catch (Exception e) {
			;;
		}
	}
	
	public Long getNextSequence(String name) {
		try {
			BasicDBObject query = new BasicDBObject("_id", name);
			DBObject update = new BasicDBObject();
			update.put("$inc", incMap);
			DBObject dbo = dataSource.getCollection("counters").findAndModify(query, update);
			return (Long) dbo.get("seq");
		} catch (Exception e ){
			initIncreasor(name);
			return 1L;
		}
	}
	
	public Long getCurrentSequence(String name) {
		BasicDBObject query = new BasicDBObject("_id", name);
		
		DBObject q = dataSource.getCollection("counters").findOne(query);
		if (q==null) {
			initIncreasor(name);
			return 1L;
		} else {
			return (Long) q.get("seq");
		}
	}
	
}
