package com.ever365.ecm.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.baidu.inf.iis.bcs.utils.Mimetypes;
import com.ever365.ecm.authority.AuthenticationUtil;
import com.ever365.ecm.clipboard.ClipboardDAO;
import com.ever365.ecm.content.ContentDAO;
import com.ever365.ecm.content.ContentData;
import com.ever365.ecm.content.ContentStore;
import com.ever365.ecm.content.ContentStoreDAO;
import com.ever365.ecm.entity.Entity;
import com.ever365.ecm.entity.EntityDAO;
import com.ever365.ecm.repo.Model;
import com.ever365.ecm.repo.QName;
import com.ever365.ecm.repo.Repository;
import com.ever365.ecm.repo.RepositoryDAO;
import com.ever365.ecm.service.listener.RepositoryListener;
import com.ever365.mongo.AutoIncrementingHelper;
import com.ever365.rest.HttpStatus;
import com.ever365.rest.HttpStatusException;
import com.ever365.rest.RestParam;
import com.ever365.rest.RestService;
import com.ever365.utils.HTMLParser;
import com.ever365.utils.MapUtils;
import com.ever365.utils.StringUtils;
import com.ever365.utils.UUID;

/**
 * Repository and Content basic operations
 * @author han
 */

public class RepositoryService {

	private static final String PUBLIC_SEQ = "publicseq";

	public static final String IMG_TEMP_REPO = "img://temp";

	private static final String LIST = "list";

	private static final String ROOT = "root";

	private static final String _ID = "_id";

	public static final String CURRENT_USER = "currentUser";

	public static final String NO_EXT = "nv";

	private static final String NAME = "name";

	private static Logger logger = Logger.getLogger(RepositoryService.class.getName());
	
	private EntityDAO entityDAO;
	private ContentDAO contentDAO;
	private RepositoryDAO repositoryDAO;
	private ClipboardDAO clipboardDAO;
	private AutoIncrementingHelper incrementingHelper;
	
	public void setClipboardDAO(ClipboardDAO clipboardDAO) {
		this.clipboardDAO = clipboardDAO;
	}
	public void setRepositoryDAO(RepositoryDAO repositoryDAO) {
		this.repositoryDAO = repositoryDAO;
	}
	
	private ContentStoreDAO contentStoreDAO;
	
	private List<RepositoryListener> listeners; 
	
	public void setContentStoreDAO(ContentStoreDAO contentStoreDAO) {
		this.contentStoreDAO = contentStoreDAO;
	}
	public void setContentDAO(ContentDAO contentDAO) {
		this.contentDAO = contentDAO;
	}
	public void setListeners(List<RepositoryListener> listeners) {
		this.listeners = listeners;
	}
	
	
	
	public void setIncrementingHelper(AutoIncrementingHelper incrementingHelper) {
		this.incrementingHelper = incrementingHelper;
		incrementingHelper.initIncreasor(PUBLIC_SEQ);
	}
	public void setEntityDAO(EntityDAO entityDAO) {
		this.entityDAO = entityDAO;
	}
	
	public EntityDAO getEntityDAO() {
		return entityDAO;
	}

	public void removeAspect(String repo, String aspect) {
		
	}

	public void removeRepository(String repository) {
		// TODO Auto-generated method stub
	}
	
	@RestService(uri="/repository/list/all", method="GET", runAsAdmin=true)
	public List<Map<String, Object>> getAllRepositories() {
		List<Repository> allrepo = repositoryDAO.getRepositories();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (Repository repository : allrepo) {
			Map<String, Object> m = getRepositoryInfo(repository);
			result.add(m);   
		}
		return result;
	}
	
	@RestService(uri="/repository/public/list", method="GET")
	public List<Map<String, Object>> getRepositories() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		/*
		List<Repository> ownedRepositories = repositoryDAO.getRepositoriesByOwner(AuthenticationUtil.getCurrentUser());
		for (Repository repository : ownedRepositories) {
			Map<String, Object> m = getRepositoryInfo(repository);
			result.add(m);
		}
		*/
		List<Repository> list = repositoryDAO.getRepositories(Repository.PROTOCOL_PUB);
		
		for (Repository repository : list) {
			Map<String, Object> m = getRepositoryInfo(repository);
			result.add(m);
		}
		return result;
	}
	
	public Map<String, Object> getRepositoryInfo(Repository repository) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", repository.toString());
		
		if (repository.getDesc()!=null) {
			m.put("name", repository.getDesc());
		} else {
			m.put("name", repository.getIdentifier());
		}
		
		m.put("owner", repository.getOwner());
		m.put(ROOT, getEntityInfo(repository.getRootEntity(), 5));
		m.put("trash", repository.getTrashEntity().getId());
		return m;
	}
	
	@RestService(uri="/repository/add", method="POST")
	public void addRepository(@RestParam(value="repository")String repository,
			@RestParam(value="desc")String desc
			) {
		repositoryDAO.addRepository(repository, AuthenticationUtil.getCurrentUser(), desc, null);
		Repository repo = repositoryDAO.getRepository(repository, false);
	}
	
	@RestService(uri="/repository/tree/list", method="GET")
	public List<Map<String, Object>> getTreeNode(@RestParam(value="pid")String id) {
		if ("_user_root".equals(id)) {
			return getChildContainer(null, 0, -1);
		} else if ("_public_root".equals(id)) {
			List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
			
			List<Repository> repos = repositoryDAO.getRepositories(Repository.PROTOCOL_PUB);
			
			for (Repository repository : repos) {
				Map<String, Object> infos = new HashMap<String, Object>();
				infos.put(_ID, repository.getRootEntity().getId());
				infos.put("name", repository.getDesc());
				result.add(infos);
			}
			return result;
		} else if ("_shared_root".equals(id)) {
			List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> shareToMeList = getSharesToMe();
			
			for (Entity entity : getSharesToMeEntities()) {
				Map<String, Object> infos = new HashMap<String, Object>();
				infos.put(_ID, entity.getId());
				infos.put("name", entity.getName());
				result.add(infos);
			}
			return result;
		} else {
			return getChildContainer(id, 0, -1);
		}
	}
	
	
	@RestService(uri="/repository/entity/childcontainer", method="GET")
	public List<Map<String, Object>> getChildContainer(@RestParam(value="pid")String id, @RestParam(value="skip")int skip, @RestParam(value="limit")int limit) {
		
		if (id==null) {
			Repository userRepo = getUserRepo();
			id = userRepo.getRootEntity().getId();
		}
		
		List<Entity> children = entityDAO.listChildByType(id, Model.TYPE_FOLDER.toString(), skip, limit);
		List<Map<String, Object>> converted = new ArrayList<Map<String,Object>>();
		for (Entity entity : children) {
			Map<String, Object> rm = entity.toMap();
			converted.add(rm);
		}
		return converted;
	}
	
	@RestService(uri="/repository/entity/list", method="GET")
	public Map<String, Object> getChildren(@RestParam(value="repository")String repository,
			@RestParam(value="id")String id, @RestParam(value="skip")int skip, @RestParam(value="limit")int limit) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String desc = null;
		result.put(CURRENT_USER, AuthenticationUtil.getCurrentUser());
		if (id==null || id.equals("")) {
			Repository userRepo = getUserRepo();
			if (userRepo==null) throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			id = userRepo.getRootEntity().getId();
			desc = userRepo.getDesc();
			result.put(NAME, userRepo.getDesc());
		} else if (id.equals("keep")) {
			result.put(NAME, "收藏");
			result.put(_ID, "keep");
			result.put(ROOT, true);
			result.put(LIST, getKeeps());
			return result;
		} else if (id.equals("trash")) {
			result.put(_ID, "trash");
			result.put(NAME, "回收站");
			result.put(ROOT, true);
			result.put(LIST, getTrashedEntities(getUserRepo().toString()));
			return result;
		} else if (id.equals("shared")) {
			result.put(_ID, "shared");
			result.put(NAME, "分享");
			result.put(ROOT, true);
			result.put(LIST, getSharesToMe());
			return result;
		}
 		
		Entity current = entityDAO.getEntityById(id);
		result.putAll(current.toMap());
		
		if (desc!=null) {
			result.put(NAME, desc);
		}
		
		List<Entity> children = entityDAO.listChild(current, skip, limit);
		
		
		List<Map<String, Object>> converted = new ArrayList<Map<String,Object>>();
		for (Entity entity : children) {
			Map<String, Object> rm = entity.toMap();
			rm.put("acl", entity.getAcl());
			converted.add(rm);
		}
		result.put(LIST, converted);
		return result;
	}
	
	public Repository getUserRepo() {
		return repositoryDAO.getRepository("usr://" + AuthenticationUtil.getCurrentUser(), false);
	}
	
	public String getParentId(String path, Repository repo) {
		String parentEntityId = null;
		
		if (path!=null) {
			if (path.equals("/") || path.equals("") || path.equals("null")) return repo.getRootEntity().getId();
			
			if (path.startsWith("/")) {
				Entity parentEntity = entityDAO.getEntityByPath(repo.getRootEntity(), path);
				if (parentEntity==null) {
					throw new HttpStatusException(HttpStatus.NOT_FOUND);
				}
				parentEntityId = parentEntity.getId();
			} else {
				/* since we are using mongodb, the extra check of parent is ignored.*/
				parentEntityId = path;
			}
		}
		return parentEntityId;
	}

	@RestService(uri="/file/upload", method="POST", multipart=true)
	public Map<String, Object> uploadFile(@RestParam(value="id") String parentEntityId, @RestParam(value="name")String name,
			@RestParam(value="file")InputStream is, @RestParam(value="size")Long size
			) {
		
		Entity parentEntity = checkParent(parentEntityId);
		
		Repository repo = repositoryDAO.getRepository(parentEntity.getRepository().toString(), false);
		
		Entity entity = createEmptyFile(parentEntityId, name, repo);
		
		putStremToFile(is, size, repo, entity);
		
		for (RepositoryListener listener : listeners) {
			if (listener.enabled()) {
				listener.onFileUploaded(parentEntity, entity);
			}
		}
		return entity.toMap();
	}
	
	@RestService(uri="/file/thumbnail/attach", method="POST", multipart=true)
	public String uploadImage(@RestParam(value="id")String entityid,
			@RestParam(value="file")InputStream is, @RestParam(value="size")Long size
			) {
		Entity entity = getEntityDAO().getEntityById(entityid);
		if (entity==null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
		
		ContentStore contentStore = contentStoreDAO.getContentStore(entity.getRepository().toString());
		
		String url = contentStore.putContent(is, size);
		
		String contentDataId = contentDAO.createContentData(entity.getRepository().toString(), url, 
				Mimetypes.getInstance().getMimetype("png"), size, "UTF-8");
		
		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		contentProperties.put(Model.PROP_FILE_THUMBNAIL, contentDataId);
		contentProperties.put(Model.PROP_FILE_THUMBNAIL_SIZE, size);
		entityDAO.updateEntityProperties(entity, contentProperties);
		return contentDataId;
	}
	
	@RestService(uri="/file/thumbnail/remove", method="POST", multipart=true)
	public void removeFileThumbNail(
			@RestParam(value="id")String entityid
	) {
		Entity entity = getEntityDAO().getEntityById(entityid);
		String thumbnailId = entity.getPropertyStr(Model.PROP_FILE_THUMBNAIL);
		String iconId = entity.getPropertyStr(Model.PROP_FILE_ICON);
		
		if (thumbnailId!=null) {
			contentDAO.deleteContentData(thumbnailId);
		}
		if (iconId!=null) {
			contentDAO.deleteContentData(iconId);
		}

		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		contentProperties.put(Model.PROP_FILE_THUMBNAIL, null);
		contentProperties.put(Model.PROP_FILE_ICON, null);
		
		entityDAO.updateEntityProperties(entity, contentProperties);
	}
	
	@RestService(uri="/file/thumbnail/crop", method="POST", multipart=true)
	public String cropFileThumbNail(
			@RestParam(value="eid")String entityid,
			@RestParam(value="id")String fileid,
			@RestParam(value="x")Integer x,
			@RestParam(value="y")Integer y,
			@RestParam(value="w")Integer w,
			@RestParam(value="h")Integer h,
			@RestParam(value="zw")Integer zw,
			@RestParam(value="zh")Integer zh
	) {
		ContentData data = getContentData(fileid);
		javaxt.io.Image image = new javaxt.io.Image(data.getInputStream());
		image.crop(x, y, w, h);
		image.resize(zw, zh);
		byte[] raw = image.getByteArray();
		
		Entity entity = getEntityDAO().getEntityById(entityid);
		
		ContentStore contentStore = contentStoreDAO.getContentStore(entity.getRepository().toString());
		
		String contentUrl = contentStore.putContent(new ByteArrayInputStream(raw), new Long(raw.length));
		
		String contentData = contentDAO.createContentData(entity.getRepository().toString(), contentUrl, "image/jpeg", raw.length, null);
		
		image.crop((zw-zh)/2, 0, zh, zh);
		image.resize(80, 80);
		byte[] icon = image.getByteArray();
		String iconUrl = contentStore.putContent(new ByteArrayInputStream(icon), new Long(icon.length));
		String iconContentData = contentDAO.createContentData(entity.getRepository().toString(), iconUrl, "image/jpeg", icon.length, null);
		
		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		contentProperties.put(Model.PROP_FILE_THUMBNAIL, contentData);
		contentProperties.put(Model.PROP_FILE_ICON, iconContentData);
		
		entityDAO.updateEntityProperties(entity, contentProperties);
		return	contentData;
	}
	
	
	@RestService(uri="/file/image", method="GET", authenticated=false, cached=true)
	public ContentData getFileThumbNail(
			@RestParam(value="id")String contentDataId
	) {
		ContentData contentData = contentDAO.getContentData(contentDataId);
		ContentStore contentStore = contentStoreDAO.getContentStore(contentData.getRepo());
		
		InputStream is = contentStore.getContentData(contentData.getContentUrl());
		
		if (is!=null) {
			contentData.setFileName("thumbnail.png");
			contentData.setInputStream(is);
			return contentData;
		} else {
			throw new HttpStatusException(HttpStatus.INSUFFICIENT_STORAGE);
		}
	}
	
	
	public void putStremToFile(InputStream is, Long size, Repository repo,
			Entity entity) {
		
		ContentStore contentStore = contentStoreDAO.getContentStore(repo.toString());
		
		String url = contentStore.putContent(is, size);
		
		String contentDataId = contentDAO.createContentData(repo.toString(), url, Mimetypes.getInstance().getMimetype(entity.getPropertyStr(Model.PROP_FILE_EXT)), size, "UTF-8");
		
		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		contentProperties.put(Model.PROP_FILE_URL, contentDataId);
		contentProperties.put(Model.PROP_FILE_SIZE, size);
		
		entityDAO.updateEntityProperties(entity, contentProperties);
	}
	
	public Entity createEmptyFile(String parentEntityId, String name,
			Repository repo) {
		String childNodeName = name;
		
		Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
		if (childNodeName.lastIndexOf(".")>-1) {
			specialProperties.put(Model.PROP_FILE_EXT, childNodeName.substring(childNodeName.lastIndexOf(".")+1));
		} else {
			specialProperties.put(Model.PROP_FILE_EXT, NO_EXT);
		}
		Entity entity = entityDAO.addEntity(repo, parentEntityId, Model.FS_CONTAINS, 
				null, Model.TYPE_FILE, childNodeName, null, specialProperties);
		return entity;
	}
	
	
	public Entity checkParent(String parentEntityId) {
		if (parentEntityId==null) {
			throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		
		Entity parentEntity = entityDAO.getEntityById(parentEntityId);
		
		if (parentEntity==null) {
			throw new HttpStatusException(HttpStatus.PRECONDITION_FAILED);
		}
		return parentEntity;
	}
	
	@RestService(uri="/file/yun", method="GET")
	public Map<String, Object> analysisPanFile(@RestParam(value="url") String url) {
		if (url.indexOf("pan.baidu.com")>-1) {
			return HTMLParser.parseBaiduPan(url);
		} else if (url.indexOf("115.com")>-1) {
			return HTMLParser.parse115(url);
		} else {
			return new HashMap<String, Object>(0);
		}
	}
	
	@RestService(uri="/file/yun", method="POST")
	public Map<String, Object> addPanFile(@RestParam(value="id") String parentEntityId, @RestParam(value="url") String url,
			@RestParam(value="name") String name,
			@RestParam(value="size") String size) {
		Entity parentEntity = checkParent(parentEntityId);
		
		Entity entity = createEmptyFile(parentEntityId, name, parentEntity.getRepository());
		
		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		
		ArrayList<String> pans = new ArrayList<String>(1);
		pans.add(url);
		
		contentProperties.put(Model.PROP_FILE_PAN_URLS, pans);
		contentProperties.put(Model.PROP_FILE_SIZE, size);
		
		entityDAO.updateEntityProperties(entity, contentProperties);
		return entityDAO.getEntityById(entity.getId()).toMap();
	}
	

	@RestService(uri="/file/attach", method="POST")
	public Map<String, Object> attachExtraPanFile(@RestParam(value="id") String currentEntityId, @RestParam(value="url") String url,
			@RestParam(value="name") String name,
			@RestParam(value="size") String size) {
		Entity entity = entityDAO.getEntityById(currentEntityId);
		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		List<String> urls = entity.getPropertyList(Model.PROP_FILE_PAN_URLS);
		if (urls==null) {
			urls = new ArrayList<String>(1);
		}
		urls.add(url);
		contentProperties.put(Model.PROP_FILE_PAN_URLS, (Serializable) urls);
		entityDAO.updateEntityProperties(entity, contentProperties);
		return entityDAO.getEntityById(currentEntityId).toMap();
	}
	
	@RestService(uri="/file/deletepan", method="POST")
	public Map<String, Object> deleteExtraPanFile(@RestParam(value="id") String currentEntityId, @RestParam(value="url") String url) {
		Entity entity = entityDAO.getEntityById(currentEntityId);
		Map<QName, Serializable> contentProperties = new HashMap<QName, Serializable>();
		List<String> urls = entity.getPropertyList(Model.PROP_FILE_PAN_URLS);
		
		if (urls==null) return entity.toMap();
		
		urls.remove(url);
		contentProperties.put(Model.PROP_FILE_PAN_URLS, (Serializable) urls);
		entityDAO.updateEntityProperties(entity, contentProperties);
		return entityDAO.getEntityById(currentEntityId).toMap();
	}
	
	public Repository getRepository(String repository) {
		Repository repo = repositoryDAO.getRepository(repository, false);
		if (repo==null) {
			throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		return repo;
	}
	
	@RestService(uri="/folder/create", method="POST")
	public Map<String, Object> addFolder(
			@RestParam(value="id") String parentEntityId, @RestParam(value="name")String name,
			@RestParam(value="desc")String desc, @RestParam(value="title")String title
			) {
		Entity parentEntity = checkParent(parentEntityId);
		
		String uuid = UUID.generate();
		String childNodeName = name;
		
		Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
		specialProperties.put(Model.PROP_FOLDER_TOTAL, 0L);
		specialProperties.put(Model.PROP_FILE_TOTAL, 0L);
		specialProperties.put(Model.PROP_FILE_SIZE, 0L);
		if (desc!=null) {
			specialProperties.put(Model.PROP_DESC, desc);
			specialProperties.put(Model.PROP_TITLE, title);
		}
		
		Entity entity = entityDAO.addEntity(parentEntity.getRepository(), parentEntityId, Model.FS_CONTAINS, 
				uuid, Model.TYPE_FOLDER, childNodeName, null, specialProperties);
		
		for (RepositoryListener listener : listeners) {
			if (listener.enabled()) {
				listener.onFolderCreated(parentEntity, entity);
			}
		}
		return entity.toMap();
	}
	
	
	
	@RestService(uri="/file/moveToTrash", method="POST")
	public void moveToTrash(
			@RestParam(value="files", required=true) List<String> files) {
		
		for (String id : files) {
			Entity entity = entityDAO.getEntityById(id);
			if (entity==null) continue;
			
			if (entity.getProperty(Model.PROP_ORIGIN_NAME)!=null) {
				continue;
			}
			
			Repository repo = repositoryDAO.getRepository(entity.getRepository().toString(), false);
			
			Map<QName, Serializable> map = new HashMap<QName, Serializable>();
			
			String randomName = UUID.generate();
			map.put(Model.PROP_ORIGIN_NAME, entity.getName());
			map.put(Model.PROP_ORIGIN_PATH, entity.getParentId());
			map.put(Model.PROP_NAME, randomName);
			map.put(Model.PROP_MODIFIED, System.currentTimeMillis());
			map.put(Model.PROP_MODIFIER, AuthenticationUtil.getCurrentUser());
			entityDAO.move(entity, repo.getTrashEntity(), map);
		}
	}
	
	@RestService(uri="/entity/trash/list", method="GET")
	public List<Map<String, Object>> getTrashedEntities(@RestParam(value="repo") String repo) {
		if (repo==null) {
			throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		
		Repository repository = repositoryDAO.getRepository(repo, false);
		
		List<Entity> children = entityDAO.listChild(repository.getTrashEntity(), 0, 10000);
		
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		
		for (Entity entity : children) {
			Map<String, Object> rm = entity.toMap();
			result.add(rm);
		}
		return result;
	}
	
	@RestService(uri="/entity/remove", method="POST")
	public void removeEntity(
			@RestParam(value="id") String ids) {
		if (ids==null) {
			throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		String[] idlist = ids.split(",");
		for (String id : idlist) {
			Entity entity = entityDAO.getEntityById(id);
			
			if (entity==null) {
				continue;
			}
			entityDAO.deleteEntity(entity);
		}
	}
	
	@RestService(uri="/file/move", method="POST")
	public void move(
			@RestParam(value="srcPath",required=true) List<String> srcPaths,
			@RestParam(value="targetPath")String targetPath) {
		Entity target = getNotNullEntity(targetPath);
		
		for (String path : srcPaths) {
			Entity src = entityDAO.getEntityById(path);
			if (src == null) continue;
			entityDAO.move(src, target, null);
			for (RepositoryListener listener : this.listeners) {
				if (listener.enabled()) {
					listener.onMoved(src, target);
				}
			}
		}
	}
	
	/**
	 * Get the entity and make sure it is not null(If null, exception will be thrown)
	 * @param targetPath
	 * @return
	 */
	public Entity getNotNullEntity(String targetPath) {
		Entity target = null;
		
		if (targetPath==null) {
			Repository userRepo = getUserRepo();
			if (userRepo==null) throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE);
			target = userRepo.getRootEntity();
		} else {
			target = entityDAO.getEntityById(targetPath);
		}
		
		if (target==null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
		return target;
	}
	
	@RestService(uri="/file/rename", method="POST")
	public void rename(
			@RestParam(value="src",required=true) String src,
			@RestParam(value="newName",required=true) String newName) {
		Entity srcEntity = entityDAO.getEntityById(src);
		
		if (srcEntity==null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
		
		Map<QName, Serializable> props = new HashMap<QName, Serializable>();
		props.put(Model.PROP_NAME, newName);
		
		entityDAO.move(srcEntity, null, props);
	}
	


	@RestService(uri="/file/recover", method="POST")
	public void recover (
			@RestParam(value="ids", required=true) List<String> idlist) {
		if (idlist==null) {
			throw new HttpStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		
		for (String string : idlist) {
			Entity entity = entityDAO.getEntityById(string);
			if (entity==null) continue;
			recoverItem(entity);
		}
	}
	
	public void recoverItem(Entity entity) {
		if (entity==null) return;
		if (entity.getProperty(Model.PROP_ORIGIN_NAME)==null) return;
		
		Map<QName, Serializable> map = new HashMap<QName, Serializable>();
		
		Entity originalParent = entityDAO.getEntityById(entity.getPropertyStr(Model.PROP_ORIGIN_PATH));
		if (originalParent==null) throw new HttpStatusException(HttpStatus.PRECONDITION_FAILED);
		
		map.put(Model.PROP_ORIGIN_NAME, null);
		map.put(Model.PROP_ORIGIN_PATH, null);
		map.put(Model.PROP_RECOVERED, true);
		map.put(Model.PROP_NAME, entity.getPropertyStr(Model.PROP_ORIGIN_NAME));

		map.put(Model.PROP_MODIFIED, System.currentTimeMillis());
		map.put(Model.PROP_MODIFIER, AuthenticationUtil.getCurrentUser());
		
		entityDAO.move(entity, originalParent, map);
		
		for (RepositoryListener listener : this.listeners) {
			if (listener.enabled()) {
				listener.onRecovered(entity);
			}
		}
	}

	@RestService(uri="/file/recoverAll", method="POST")
	public void recoverAll(@RestParam(value="repo",required=true)String sr) {
		
		Repository repository = getUserRepo();
		
		if (repository!=null) {
			Entity trashEntity = repository.getTrashEntity();
			
			List<Entity> list = entityDAO.listChild(trashEntity, 0, -1);
			
			for (Entity entity : list) {
				try {
					recoverItem(entity);
				} catch (Exception e) {
					
				}
			}
		}
	}
	
	@RestService(uri="/trash/clean", method="POST")
	public void cleanTrash(@RestParam(value="repo")String sr) {
		
		Repository repository = getUserRepo();
		
		if (repository!=null) {
			Entity trashEntity = repository.getTrashEntity();
			
			List<Entity> list = entityDAO.listChild(trashEntity, 0, -1);
			
			for (Entity entity : list) {
				try {
					entityDAO.deleteEntity(entity);
				} catch (Exception e) {
					
				}
			}
		}
	}
	
	@RestService(uri="/share/add", method="POST")
	public void addPermission(@RestParam(value="id", required=true)List<String> idList,
			@RestParam(value="Reader")List<String> readers
			) {
		for (String id : idList) {
			Entity entity = entityDAO.getEntityById(id);
			if (entity==null) {
				return ;
			}
			Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
			
			if (entity.getProperty(Model.PROP_PERM_READER)!=null) {
				List<String> preader = (List<String>)entity.getProperty(Model.PROP_PERM_READER);
				readers.addAll(preader);
			} 
			specialProperties.put(Model.PROP_PERM_READER, (Serializable) readers);
			entityDAO.updateEntityProperties(entity, specialProperties);
		}
	}
	
	@RestService(uri="/share/update", method="POST")
	public void updatePermission(@RestParam(value="id", required=true)List<String> idList,@RestParam(value="inherit")Boolean inherit,
			@RestParam(value="Reader")List<String> readers,
			@RestParam(value="Editor")List<String> editors,
			@RestParam(value="Coordinator")List<String> coordinators) {
		for (String id : idList) {
			Entity entity = entityDAO.getEntityById(id);
			if (entity==null) {
				return ;
			}
			Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
			
			specialProperties.put(Model.PROP_PERM_READER, (Serializable) readers);
			specialProperties.put(Model.PROP_PERM_EDITOR, (Serializable) editors);
			specialProperties.put(Model.PROP_PERM_COORDINATOR, (Serializable) coordinators);
			specialProperties.put(Model.PROP_ACL_INHERIT, inherit);
			
			entityDAO.updateEntityProperties(entity, specialProperties);
		}
	}
	
	
	@RestService(uri="/share/mine", method="GET")
	public List<Map<String, Object>> getMyShares() {
		
		List<Map<String, Object>> shares = new ArrayList<Map<String,Object>>();
		/*
		Collection<Entity> sources = permissionService.getEntitiesBySource(AuthenticationUtil.getCurrentUser());
		
		
		for (Entity entity : sources) {
			Map<String, Object> info = getEntityInfo(entity, 5);
			info.put("permissions", getPermissions(entity.getId()));
			shares.add(info);
		}
		*/
		return shares;
	}
	
	public Map<String, Object> getEntityInfo(Entity entity, int limit) {
		Map<String, Object> info = entity.toMap();
		List<Entity> childrens = entityDAO.listChild(entity, 0, limit);
		
		LinkedList<Entity> ancestors = entityDAO.getAncestor(entity);
		
		LinkedList<String> paths = new LinkedList<String>();
		LinkedList<String> ids = new LinkedList<String>();
		
		for (Entity ancestor : ancestors) {
			paths.add(ancestor.getName());
			ids.add(ancestor.getId());
		}
		
		info.put("ancpath", paths);
		info.put("ancid", ids);
		List<Map<String, Object>> details = new ArrayList<Map<String,Object>>();
		for (Entity child : childrens) {
			details.add(child.toMap());
		}
		
		info.put(LIST, details);
		return info;
	}
	
	public List<Entity> getSharesToMeEntities() {
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put(Model.PROP_PERM_READER.getLocalName(), AuthenticationUtil.getCurrentUser());
		return entityDAO.filter(filters, null,null,null);
	}
	
	@RestService(uri="/share/received", method="GET")
	public List<Map<String, Object>> getSharesToMe() {
		List<Map<String, Object>> shares = new ArrayList<Map<String,Object>>();
		for (Entity entity : getSharesToMeEntities()) {
			shares.add(entity.toMap());
		}
		return shares;
	}
	
	@RestService(uri="/keep/set", method="POST")
	public void setKeep(@RestParam(value="id") String id, @RestParam(value="set") Boolean set) {
		
		Entity entity = entityDAO.getEntityById(id);
		Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
		ArrayList<String> keeps = null;
		
		if (entity.getProperty(Model.PROP_KEEP_USRS)==null) {
			keeps =  new ArrayList<String>();
		} else {
			keeps = (ArrayList<String>)entity.getProperty(Model.PROP_KEEP_USRS);
		}
		
		if (set) {
			keeps.add(AuthenticationUtil.getCurrentUser());
		} else {
			keeps.remove(AuthenticationUtil.getCurrentUser());
		}
		specialProperties.put(Model.PROP_KEEP_USRS, keeps);
		entityDAO.updateEntityProperties(entity, specialProperties);
	}
	
	@RestService(uri="/publish", method="POST", runAsAdmin=true)
	public void setPublished(@RestParam(value="id") String id) {
		
		Entity entity = entityDAO.getEntityById(id);
		if (entity == null || entity.getType().equals(Model.TYPE_FOLDER)) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST);
		}
		Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
		specialProperties.put(Model.PUBLISHED, true);
		specialProperties.put(Model.PUBLISHED_SEQ, incrementingHelper.getNextSequence(PUBLIC_SEQ));
		entityDAO.updateEntityProperties(entity, specialProperties);
	}
	
	
	@RestService(uri="/keep/list", method="GET")
	public List<Map<String, Object>> getKeeps() {
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put(Model.PROP_KEEP_USRS.getLocalName(), AuthenticationUtil.getCurrentUser());
		
		List<Entity> matched = entityDAO.filter(filters,null,null,null);
		
		for (Entity entity : matched) {
			if (entity.getOwner().equals(AuthenticationUtil.getCurrentUser())) {
				result.add(entity.toMap());
			}
		}
		return result;
	}

	
	/*
	@RestService(uri="/share/list", method="GET")
	public Map<String, Object> getPermissions(@RestParam(value="id", required=true)String id) {
		Entity entity = entityDAO.getEntityById(id);
		Map<String, Object> pinfo = new HashMap<String, Object>();
		List<ACE> permissions = permissionService.getAllSetPermissions(entity, true);
		
		pinfo.put("inh", permissionService.getInheritParentPermissions(entity));
		
		List<Map<String, Object>> inhlist = new ArrayList<Map<String,Object>>();
		
		List<Map<String, Object>> curlist = new ArrayList<Map<String,Object>>();
		
		for (ACE ace : permissions) {
			
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("auth", ace.getAuthority());
			info.put("permission", ace.getPermission());
			info.put("allow", ace.isAllow());
			info.put("entity", ace.getEntity());
			
			if (ace.getEntity().equals(entity.getId())) {
				curlist.add(info);
			} else {
				inhlist.add(info);
			}
		}
		
		pinfo.put("curlist", curlist);
		pinfo.put("inhlist", inhlist);
		return pinfo;
	}
	*/
	@RestService(uri="/file/clipboard/copyAll", method="POST")
	public void copyFromClipboard(@RestParam(value="target", required=true)String target) {
		List<Map<String, Object>> clips = clipboardDAO.list();
		
		for (Map<String, Object> map : clips) {
			
			try {
				
			} catch (HttpStatusException e) {
				//do nothing
			}
		}
	}
	
	@RestService(uri="/file/copy", method="POST")
	public void copy(@RestParam(value="srcs",required=true) List<String> srcPaths, @RestParam(value="target")String targetId
			) {
		Entity target = getNotNullEntity(targetId);
		for (String path : srcPaths) {
			Entity src = entityDAO.getEntityById(path);
			if (src == null) continue; 
			try {
				entityDAO.copy(src, target, null);
				for (RepositoryListener listener : this.listeners) {
					if (listener.enabled()) {
						listener.onCopied(src, target);
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
	@RestService(uri="/file/download", method="GET", authenticated=false)
	public ContentData getContentData(@RestParam(value="id", required=true)String id) {
		
		Entity entity = entityDAO.getEntityById(id);
		
		if (entity==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		
		ContentStore contentStore = contentStoreDAO.getContentStore(entity.getRepository().toString());
		
		String fileUrl = entity.getPropertyStr(Model.PROP_FILE_URL);
		
		ContentData contentData = contentDAO.getContentData(fileUrl);
		
		if (contentData==null) {
			throw new HttpStatusException(HttpStatus.FAILED_DEPENDENCY);
		}
		
		InputStream is = contentStore.getContentData(contentData.getContentUrl());
		
		if (is!=null) {
			contentData.setFileName(entity.getName());
			contentData.setInputStream(is);
			return contentData;
		} else {
			throw new HttpStatusException(HttpStatus.INSUFFICIENT_STORAGE);
		}
	}
	
	@RestService(uri="/file/filter", method="GET")
	public List<Map<String, Object>> filterFile(@RestParam(value="filter")String filter) {
		String[] filters = filter.split(";");
		Map<String, Object> query = new HashMap<String, Object>();
		
		query.put("type", "s:file");
		for (String filterItem : filters) {
			if (filterItem.startsWith("ext")) { //{ ext: { $in: [ 'doc', 'pdf' ] } } 
				String extValue = filterItem.substring(4);
				String[] extensions = extValue.split(",");
				List<String> extList = new ArrayList<String>();
				for (String ext : extensions) {
					extList.add(ext);
				}
				query.put("ext", MapUtils.newMap("$in", extList));
			}
			
			if (filterItem.startsWith("facet")) {
				 //{ faceted: "value"} } 
				query.put("faceted", filterItem.substring(6));
			}

			if (filterItem.startsWith("size>")) {
				//db.inventory.find( { qty: { $gte: 20 } } )
				MapUtils.putToMap(query, "size", "$gte", StringUtils.tofileSize(filterItem.substring(5)));
				//query.put("size", MapUtils.newMap("$gte", StringUtils.tofileSize(filterItem.substring(5))));
			}
			if (filterItem.startsWith("size<")) {
				//db.inventory.find( { qty: { $lte: 20 } } )
				MapUtils.putToMap(query, "size", "$lte", StringUtils.tofileSize(filterItem.substring(5)));
				//query.put("size", MapUtils.newMap("$lte", StringUtils.tofileSize(filterItem.substring(5))));
			}
		}
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		List<Entity> entities = getEntityDAO().filter(query,null,null,null);
		
		for (Entity entity : entities) {
			result.add(entity.toMap());
		}
		return result;
	}
	
	
	/*
	@RestService(uri="/file/view", method="GET")
	public ContentData viewFile(@RestParam(value="id")String id, @RestParam(value="width")String width) {
		Entity entity = entityDAO.getEntityById(id);
		
		if (entity==null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND);
		}
		ContentStore contentStore = contentStoreDAO.getContentStore(entity.getRepository().toString());
		QName qname = QName.createQName(Model.SYSTEM_CONTENT_NAMESPACE, "w" + width);
		
		String converted = entity.getPropertyStr(qname);
		if (converted==null) {
			String fileUrl = entity.getPropertyStr(Model.PROP_FILE_URL);
			ContentData contentData = contentDAO.getContentData(fileUrl);
			
			if (contentData==null) {
				throw new HttpStatusException(HttpStatus.FAILED_DEPENDENCY);
			}
			
			InputStream is = contentStore.getContentData(contentData.getContentUrl());
		
			File temp = null;
			try {
				Image image = ImageLoader.fromStream(is);
				
				temp = File.createTempFile(entity.getId() + "_w_" + System.currentTimeMillis(), ".tmp");
				
				image.getResizedToWidth(Integer.parseInt(width)).writeToFile(temp);
				
				String url = contentStore.putContent(new FileInputStream(temp), temp.length());
				
				converted = contentDAO.createContentData(url, Mimetypes.getInstance().getMimetype(entity.getPropertyStr(Model.PROP_FILE_EXT)), temp.length(), "UTF-8");

				Map<QName, Serializable> specialProperties = new HashMap<QName, Serializable>();
				
				specialProperties.put(qname, converted);
				
				entityDAO.updateEntityProperties(entity, specialProperties);
				
			} catch (IOException e) {
				throw new HttpStatusException(HttpStatus.FAILED_DEPENDENCY);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (temp!=null) {
					temp.delete();
				}
			}
		}
		ContentData contentData = contentDAO.getContentData(converted);
		InputStream is = contentStore.getContentData(contentData.getContentUrl());
		if (is!=null) {
			contentData.setFileName(entity.getName());
			contentData.setInputStream(is);
			return contentData;
		} else {
			throw new HttpStatusException(HttpStatus.INSUFFICIENT_STORAGE);
		}
	}
	*/
	
}
