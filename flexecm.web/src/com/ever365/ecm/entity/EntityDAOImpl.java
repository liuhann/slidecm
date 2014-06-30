package com.ever365.ecm.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.ever365.common.ContentStore;
import com.ever365.ecm.repo.Model;
import com.ever365.ecm.repo.QName;
import com.ever365.ecm.repo.Repository;
import com.ever365.mongo.AutoIncrementingHelper;
import com.ever365.mongo.MongoDataSource;
import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.HttpStatus;
import com.ever365.rest.HttpStatusException;
import com.ever365.utils.MapUtils;
import com.ever365.utils.UUID;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author Liu Han 
 */
public class EntityDAOImpl implements EntityDAO {
	
	public static final String _ID = "_id";
	public static final String ENTITIES = "entities";
	public static final String ASSOC = "assoc";

	private static final String STRING_EMPTY = "";

	private MongoDataSource dataSource;
	private AutoIncrementingHelper autoIncrementingHelper;
	private ContentStore contentStore;
	
	public void setContentStore(ContentStore contentStore) {
		this.contentStore = contentStore;
	}

	public void setAutoIncrementingHelper(
			AutoIncrementingHelper autoIncrementingHelper) {
		this.autoIncrementingHelper = autoIncrementingHelper;
	}

	public void setDataSource(MongoDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void init() {
		try {
			autoIncrementingHelper.initIncreasor(ENTITIES);
			getEntityCollection().ensureIndex(Model.PROP_MODIFIED.getLocalName());
			getEntityCollection().ensureIndex(Model.PROP_PARENT_ID.getLocalName());
		} catch (Exception er) {
			;
		}
	}
	
	@Override
	public boolean exists(Entity entity) {
		return false;
	}
	
	class MongoDBOWrapper {
		private DBObject dbObject;

		public MongoDBOWrapper(DBObject dbObject) {
			super();
			this.dbObject = dbObject;
		}
		

		public Boolean getBoolean(QName qname) {
			String key = getQNameKey(qname);
			Object o = dbObject.get(key);
			
			if (o==null) return false;
			if (o instanceof Boolean) {
				return (Boolean) o;
			} else {
				return false;
			}
		}
		public String getString(String key) {
			Object o = dbObject.get(key);
			
			if (o==null) return null;
			if (o instanceof String) {
				return (String) o;
			} else {
				return o.toString();
			}
		}
		
		public String getString(QName qname) {
			String key = getQNameKey(qname);
			return getString(key);
		}
		
		public QName getQName(QName key) {
			String strKey = getQNameKey(key);
			return getQName(strKey);
		}


		public QName getQName(String strKey) {
			Object o = dbObject.get(strKey);
			if (o==null) return null;
			if (o instanceof String) {
				return QName.createQName(o);
			} else {
				throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		public Long getLong(QName qname) {
			Object o = dbObject.get(getQNameKey(qname));
			if (o==null) return 0L;
			if (o instanceof Long) {
				return (Long) o;
			} else if (o instanceof Integer) {
				return ((Integer)o).longValue();
			} else {
				throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		public ObjectId getId(QName qname) {
			return getId(getQName(qname));
		}
		
		public ObjectId getId(String qname) {
			Object o = dbObject.get(qname);
			if (o==null) return null;
			if (o instanceof ObjectId) {
				return (ObjectId) o;
			} else {
				return new ObjectId(o.toString());
			}
		}
	}

	public String getQNameKey(QName qname) {
		String key;
		if (qname.getNamespaceURI().equals(Model.SYSTEM_NAMESPACE)) {
			key = qname.getLocalName();
		} else {
			key = qname.toString();
		}
		return key;
	}
	
	@SuppressWarnings("unchecked")
	public Entity extractEntity(DBObject entitydbo) {
		MongoDBOWrapper wrapper = new MongoDBOWrapper(entitydbo);
		
		Entity entity = new Entity(new Repository(wrapper.getString(Model.PROP_REPO)), 
				entitydbo.get(_ID).toString());
		entity.setName(wrapper.getString(Model.PROP_NAME));
		entity.setType(wrapper.getQName(Model.PROP_TYPE));
		
		entity.setCreator(wrapper.getString(Model.PROP_CREATOR));
		entity.setModifier(wrapper.getString(Model.PROP_MODIFIER));
		
		entity.setCreated(wrapper.getLong(Model.PROP_CREATED));
		entity.setModified(wrapper.getLong(Model.PROP_MODIFIED));
		entity.setOwner(wrapper.getString(Model.PROP_OWNER));

		entity.setInheritAcl(wrapper.getBoolean(Model.PROP_ACL_INHERIT));
		entity.setAcl(wrapper.getBoolean(Model.PROP_ACL));
		
		entity.setParentId(wrapper.getString(Model.PROP_PARENT_ID));
		
		if (entity.getParentId()!=null) {
			entity.setAssocationType(wrapper.getQName(Model.PROP_ASSOC_TYPE));
		}
		try {
			entity.setSize(wrapper.getLong(Model.PROP_FILE_SIZE));
		} catch (Exception e) {
			entity.setSize(0L);
		}
		
		entity.setRawMap(entitydbo.toMap());
		return entity;
	}


	@Override
	public void deleteEntity(Entity entity) {
		if (entity.getType().equals(Model.TYPE_FOLDER)) {
			List<Entity> children = listChild(entity, 0, -1);
			for (Entity c : children) {
				deleteEntity(c);
			}
		}
		
		if (entity.getType().equals(Model.TYPE_FILE)) {
			if (entity.getProperty(Model.PROP_FILE_URL)!=null) {
				contentStore.deleteContent(entity.getPropertyStr(Model.PROP_FILE_URL));
			}
		}
		getEntityCollection().remove(new BasicDBObject("_id", new ObjectId(entity.getId())));
	}

	@Override
	public List<Entity> listChild(Entity entity,
			int skip, int count) {
		if (entity==null) throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		
		DBObject query = new BasicDBObject();
		query.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(entity.getId()));

		
		Map<String, Object> order = new HashMap<String, Object>(1);
		order.put(Model.PROP_MODIFIED.getLocalName(), -1);
		
		DBCursor cursor = getEntityCollection().find(query).sort(new BasicDBObject(order)).skip(skip);
		if (count>0) {
			cursor.limit(count);
		}
		
		List<Entity> result = new ArrayList<Entity>();
		while(cursor.hasNext()) {
			DBObject dbo = cursor.next();
			result.add(extractEntity(dbo));
		}
		cursor.close();
		return result;
	}
	
	@Override
	public List<Entity> listChildByType(String parentId, String type, int skip, int count) {
		if (parentId==null) 
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		
		DBCollection entityColl = getEntityCollection();
		
		DBObject query = new BasicDBObject();
		query.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(parentId));
		query.put(getQNameKey(Model.PROP_TYPE), type);
		
		DBCursor cursor = entityColl.find(query).skip(skip);
		if (count>0) {
			cursor.limit(count);
		}
		
		List<Entity> result = new ArrayList<Entity>();
		while(cursor.hasNext()) {
			DBObject dbo = cursor.next();
			result.add(extractEntity(dbo));
		}
		return result;
	}

	@Override
	public Entity addEntity(Repository repository, String parentNodeId,
			QName assocType, String uuid, QName nodeType,
			String childNodeName, Map<QName, Serializable> auditableProperties,
			Map<QName, Serializable> specialProperties) {
		if (repository==null || assocType==null || nodeType==null || childNodeName==null) {
			throw new IllegalArgumentException();
		}
		
		DBCollection entityColl = getEntityCollection();
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		
		if (parentNodeId!=null) {
			//If not root, will make a check of conflict
			DBObject exsitQuery = new BasicDBObject();
			exsitQuery.put(getQNameKey(Model.PROP_NAME), childNodeName);
			exsitQuery.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(parentNodeId));
			DBObject exsit = entityColl.findOne(exsitQuery);
			if (exsit!=null) {
				throw new HttpStatusException(HttpStatus.CONFLICT);
			}
		}
		
		properties.put(Model.PROP_REPO, repository.toString());
		if (parentNodeId!=null) {
			properties.put(Model.PROP_PARENT_ID, new ObjectId(parentNodeId));
		}
		
		properties.put(Model.PROP_TYPE, nodeType.toString());
		properties.put(Model.PROP_NAME, childNodeName);
		properties.put(Model.PROP_UUID, uuid);
		properties.put(Model.PROP_PERM_READER, (Serializable)Collections.emptyList());
		properties.put(Model.PROP_PERM_EDITOR, (Serializable)Collections.emptyList());
		properties.put(Model.PROP_PERM_COORDINATOR, (Serializable)Collections.emptyList());
		properties.put(Model.PROP_KEEP_USRS, (Serializable)Collections.emptyList());
		
		
		properties.put(Model.PROP_ACL_INHERIT, true);
		properties.put(Model.PROP_ASSOC_TYPE, assocType.toString());
		//properties.put(Model.PROP_SEQ, this.autoIncrementingHelper.getNextSequence(ENTITIES));
		
		Date d = new Date();
		properties.put(Model.PROP_CREATOR, AuthenticationUtil.getCurrentUser());
		properties.put(Model.PROP_CREATED, d.getTime());
		properties.put(Model.PROP_MODIFIER, AuthenticationUtil.getCurrentUser());
		properties.put(Model.PROP_MODIFIED, d.getTime());
		properties.put(Model.PROP_OWNER, AuthenticationUtil.getCurrentUser());
		
		
		if (auditableProperties!=null) {
			properties.putAll(auditableProperties);
		} 
		if (specialProperties!=null) {
			properties.putAll(specialProperties);
		}
		Map<String, Serializable> extraProperties = new HashMap<String, Serializable>(specialProperties.size());
		for (QName qname : specialProperties.keySet()) {
			extraProperties.put(qname.toString(), specialProperties.get(qname));
		}
		DBObject entitydbo = new BasicDBObject();
		
		for (QName key : properties.keySet()) {
			entitydbo.put(getQNameKey(key), properties.get(key));
		}
		
		entityColl.insert(entitydbo);
		Entity entity = extractEntity(entitydbo);
		return entity;
	}


	@Override
	public Entity getEntityByPath(Entity from, String path) {
		
		String[] paths = path.split("/");
		
		Entity entity = from;
		for (int i = 0; i < paths.length; i++) {
			if (STRING_EMPTY.equals(paths[i])) continue;
			entity = getChildByName(entity, paths[i]);
			if (entity==null) return null;
		}
		
		return entity;
	}

	@Override
	public Entity getEntityById(String Id) {
		
		DBCollection entityColl = getEntityCollection();
		
		DBObject query = new BasicDBObject();
		
		query.put(_ID, new ObjectId(Id));
		
		DBObject found = entityColl.findOne(query);
		if (found==null) {
			return null;
		} else {
			return extractEntity(found);
		}
	}

	@Override
	public Long getChildrenCount(String parentId) {
		if (parentId==null) 
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		DBCollection entityColl = getEntityCollection();
		DBObject query = new BasicDBObject();
		query.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(parentId));
		return entityColl.count(query);
	}

	@Override
	public List<String> getDescendants(String Id) {
		return null;
	}
	
	@Override
	public LinkedList<Entity> getAncestor(Entity entity) {
		LinkedList<Entity> result = new LinkedList<Entity>();
		
		String parentId = entity.getParentId();
		
		while(parentId!=null) {
			Entity parentEntity = getEntityById(parentId);
			
			if (parentEntity!=null) {
				result.add(parentEntity);
				parentId = parentEntity.getParentId();
			} else {
				parentId = null;
			}
		}
		return result;
	}

	public DBCollection getEntityCollection() {
		DBCollection entityColl = dataSource.getCollection(ENTITIES);
		return entityColl;
	}
	

	public DBCollection getAssocCollection() {
		DBCollection entityColl = dataSource.getCollection(ASSOC);
		return entityColl;
	}

	@Override
	public void updateEntityProperties(Entity entity,
			Map<QName, Serializable> specialProperties) {

		DBObject query = new BasicDBObject(_ID, new ObjectId(entity.getId()));
		
		DBObject updatedbo = new BasicDBObject();
		
		updatedbo.put("$set", MapUtils.newMap(Model.PROP_MODIFIED.getLocalName(), System.currentTimeMillis()));
		
		for (QName key : specialProperties.keySet()) {
			
			Serializable value = specialProperties.get(key);
			
			String dbokey = null;
			
			if (key.getNamespaceURI().equals(Model.SYSTEM_NAMESPACE)) {
				dbokey = key.getLocalName();
			} else {
				dbokey = key.toString();
			}
			
			if (value==null) {
				if (updatedbo.get("$unset")!=null) {
					Map map = (Map)updatedbo.get("$unset");
					map.put(dbokey, 1);
				} else {
					updatedbo.put("$unset", MapUtils.newMap(dbokey, 1));
				}
				continue;
			}
		
			if (value instanceof String) {
				if (((String) value).startsWith("+")) {
					if(updatedbo.get("$inc")==null) {
						updatedbo.put("$inc", MapUtils.newMap(dbokey, Long.parseLong(((String)value).substring(1))));
					} else {
						Map incs = (Map)updatedbo.get("$inc");
						incs.put(dbokey, Long.parseLong(((String)value).substring(1)));
					}
					continue;
				} 
			}
			/*
			 * if set multiple fields (in most cases), will call mongodb on this
			 * 
			 * {
			 * 	"$set" : {
			 * 		"field1": val1,
			 *      "field2": val2
			 *      .....
			 *   }
			 * }
			 */
			
			if (updatedbo.get("$set")!=null) {
				Map map = (Map)updatedbo.get("$set");
				map.put(dbokey, value);
			} else {
				updatedbo.put("$set", MapUtils.newMap(dbokey, value));
			}
		}
		getEntityCollection().update(query, updatedbo);
	}

	@Override
	public String getEntityPath(Entity entity) {
		LinkedList<Entity> anc = getAncestor(entity);
		
		String path = "";
		for (Entity e : anc) {
			if (e.getParentId()!=null) {
				
			}
		}
		return null;
	}
	
	@Override
	public void move(Entity src, Entity target, Map<QName, Serializable> props) {
		if (src==null) throw new IllegalArgumentException();
		
		String newName = null;
		if (props!=null) {
			newName = MapUtils.get(props, Model.PROP_NAME);
		}
		
		if (target==null) { // rename
			target = getEntityById(src.getParentId());
		}
		
		if (src.getRepository().equals(target.getRepository())) {
			//check movable
			if (src.equals(target)) return;

			if (src.getParentId().equals(target.getId())) { //move to self parent, only check rename
				if (newName==null) return; 
				if (newName!=null && newName.equals(src.getName())) return; 
			} 
			//check move to ancestor
			LinkedList<Entity> ancestors = getAncestor(target);
			if (ancestors.contains(src)) {
				//the src is the target ancestor 
				throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
			}
		}
		
		newName = (newName==null) ? src.getName(): newName;
		if (getChildByName(target, newName)!=null) {
			throw new HttpStatusException(HttpStatus.CONFLICT);
		}

		BasicDBObject updatedbo = new BasicDBObject();
		
		Map<String, Object> setMap = new HashMap<String, Object>();

		if (props!=null) {
			for (QName key : props.keySet()) {
				setMap.put(getQNameKey(key), props.get(key));
			}
		}
		
		setMap.put(getQNameKey(Model.PROP_MODIFIED), System.currentTimeMillis());
		setMap.put(getQNameKey(Model.PROP_MODIFIER), AuthenticationUtil.getCurrentUser());
		
		if (!src.getParentId().equals(target.getId())) {
			setMap.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(target.getId()));
			//setMap.put(getQNameKey(Model.PROP_SEQ), this.autoIncrementingHelper.getNextSequence(ENTITIES));
		}
		
		if (!src.getRepository().equals(target.getRepository())) {
			setMap.put(getQNameKey(Model.PROP_REPO), target.getRepository().toString());
		}
		
		updatedbo.put("$set", setMap);
		
		getEntityCollection().update(new BasicDBObject(_ID, new ObjectId(src.getId())), updatedbo);
	}

	@Override
	public Entity getChildByName(Entity entity, String childName) {
		
		DBObject query = new BasicDBObject();
		
		query.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(entity.getId()));
		query.put(getQNameKey(Model.PROP_NAME), childName);
		
		DBObject r = getEntityCollection().findOne(query);
		if (r!=null) {
			return extractEntity(r);
		}
		return null;
	}

	@Override
	public void copy(Entity src, Entity target, String newName) {
		if (src==null || target==null) throw new IllegalArgumentException();
		
		
		if (src.getRepository().equals(target.getRepository())) {
			//check copiable
			if (src.equals(target)) {
				throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
			}
			if (src.getParentId().equals(target.getId())) { //copy to same dir: must provide new Name
				if (newName==null) throw new HttpStatusException(HttpStatus.CONFLICT);
				if (newName!=null && newName.equals(src.getName())) throw new HttpStatusException(HttpStatus.CONFLICT); ;
			}
			
			LinkedList<Entity> ancestors = getAncestor(target);
			if (ancestors.contains(src)) {
				//the src is the target ancestor 
				throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
			}
			
			//check duplicated
			if (!src.getParentId().equals(target)) { //move
				newName = (newName==null) ? src.getName(): newName;
				if (getChildByName(target, newName)!=null) {
					throw new HttpStatusException(HttpStatus.CONFLICT);
				}
			} 
		}
		recursiveCopy(src, target, newName);
	}
	
	public Entity copyOneEntity(Entity src, Entity target, String newName) {
		Map<String, Object> map = src.toMap();
		map.remove("_id");
		
		map.remove(getQNameKey(Model.PROP_PERM_COORDINATOR));
		map.remove(getQNameKey(Model.PROP_PERM_READER));
		map.remove(getQNameKey(Model.PROP_PERM_EDITOR));
		map.remove(getQNameKey(Model.PROP_KEEP_USRS));
		
		DBObject entitydbo = new BasicDBObject();
		entitydbo.putAll(map);
		entitydbo.put(getQNameKey(Model.PROP_REPO), target.getRepository().toString());
		entitydbo.put(getQNameKey(Model.PROP_PARENT_ID), new ObjectId(target.getId()));
		entitydbo.put(getQNameKey(Model.PROP_NAME), newName==null?src.getName(): newName);
		entitydbo.put(getQNameKey(Model.PROP_OWNER), target.getOwner());
		entitydbo.put(getQNameKey(Model.PROP_UUID), com.ever365.utils.UUID.generate());
		entitydbo.put(getQNameKey(Model.PROP_ACL), null);
		entitydbo.put(getQNameKey(Model.PROP_ACL_INHERIT), true);
		
		entitydbo.put(getQNameKey(Model.PROP_CREATED), System.currentTimeMillis());
		entitydbo.put(getQNameKey(Model.PROP_CREATOR), AuthenticationUtil.getCurrentUser());
		entitydbo.put(getQNameKey(Model.PROP_MODIFIED), System.currentTimeMillis());
		entitydbo.put(getQNameKey(Model.PROP_MODIFIER), AuthenticationUtil.getCurrentUser());
		entitydbo.put(getQNameKey(Model.PROP_AUTHOR), src.getCreator());
		entitydbo.put(getQNameKey(Model.PROP_ASSOC_TYPE), Model.FS_CONTAINS.toString());
		entitydbo.put(getQNameKey(Model.PROP_COPYED_FROM), src.getId());
		
		if (src.getType().equals(Model.TYPE_FILE)) {
			String newUid = UUID.generate();
			contentStore.copyContent(src.getPropertyStr(Model.PROP_FILE_URL), newUid);
			entitydbo.put(getQNameKey(Model.PROP_FILE_URL), newUid);
		}
		getEntityCollection().insert(entitydbo);
		
		Entity entity = extractEntity(entitydbo);
		
		return entity;
	}
	
	public void recursiveCopy(Entity src, Entity target, String newName) {
		Entity newparent = copyOneEntity(src, target, newName);
		
		if (src.getType().equals(Model.TYPE_FOLDER)) {
			List<Entity> children = listChild(src, 0, -1);
			for (Entity entity : children) {
				recursiveCopy(entity, newparent, null);
			}
			
		}
	}

	@Override
	public List<Entity> filter(Map<String, Object> filters, Map<String, Object> order,Integer skip, Integer limit) {
		
		DBObject dbo = new BasicDBObject(filters);
		DBCursor cursor = getEntityCollection().find(dbo);
		if (order!=null) {
			cursor.sort(new BasicDBObject(order));
		}
		if (skip!=null) {
			cursor.skip(skip);
		}
		if (limit!=null) {
			cursor.limit(limit);
		}
		List<Entity> list = new ArrayList<Entity>();
		while (cursor.hasNext()) {
			DBObject e = cursor.next();
			list.add(extractEntity(e));
		}
		
		return list;
	}

	@Override
	public void addEntityAssoc(String sourceId, String targetId, QName assocType,
			String assocValue) {
		
		DBObject ups = new BasicDBObject();
		ups.put("src", new ObjectId(sourceId));
		ups.put("target", new ObjectId(targetId));
		ups.put("type", getQNameKey(assocType));
		
		DBObject dbo = new BasicDBObject();
		dbo.put("src", new ObjectId(sourceId));
		dbo.put("target", new ObjectId(targetId));
		dbo.put("type", getQNameKey(assocType));
		dbo.put("value", assocValue);
		getAssocCollection().update(ups, dbo, true, false);
	}
	
	@Override
	public int removeNodeAssoc(String sourceId, String targetId, QName assocType) {
		DBObject dbo = new BasicDBObject();
		dbo.put("src", new ObjectId(sourceId));
		if (targetId!=null) {
			dbo.put("target", new ObjectId(targetId));
		}
		if (assocType!=null) {
			dbo.put("type", getQNameKey(assocType));
		}
		return getAssocCollection().remove(dbo).getN();
	}
}
