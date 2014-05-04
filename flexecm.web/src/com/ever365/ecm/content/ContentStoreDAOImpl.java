package com.ever365.ecm.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ever365.mongo.MongoDataSource;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author LiuHan
 */

public class ContentStoreDAOImpl implements ContentStoreDAO {

	public static final String STORE = "store";

	public Map<String, ContentStore> stores = new HashMap<String, ContentStore>(); 
	
	private ContentStoreFactory contentStoreFactory;

	private MongoDataSource dataSource;
	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setContentStoreFactory(ContentStoreFactory contentStoreFactory) {
		this.contentStoreFactory = contentStoreFactory;
	}

	@Override
	public ContentStore getContentStore(String name) {
		if (name==null) {
			return contentStoreFactory.getDefaultContentStore();
		} else {
			ContentStore contentStore = stores.get(name);
			if (contentStore!=null) return contentStore;
			
			
			//关于同步：有问题再说~~~~
			DBObject dbo = dataSource.getCollection(STORE).findOne(new BasicDBObject("name", name));
			
			if (dbo==null) {
				contentStore = contentStoreFactory.getDefaultContentStore();
				addContentStore(name, contentStore.getStoreUrl());
			} else {
				if (dbo.get("url")==null) {
					contentStore = contentStoreFactory.getDefaultContentStore();
				} else {
					String storeUrl = (String)dbo.get("url");
					contentStore = contentStoreFactory.initContentStore(storeUrl);
				}
			}
			stores.put(name, contentStore);
			return contentStore;
		}
	}

	@Override
	public void addContentStore(String name, String contentStoreUrl) {
		DBObject dbo = new BasicDBObject();
		dbo.put("name", name);
		dbo.put("url", contentStoreUrl);
		
		dataSource.getCollection(STORE).insert(dbo);
	}

	@Override
	public List<ContentStore> getAllContentStores() {
		return null;
	}

	@Override
	public void removeContentStore(String name) {

	}

}
