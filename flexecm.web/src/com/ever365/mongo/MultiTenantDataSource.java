package com.ever365.mongo;

import com.ever365.ecm.authority.AuthenticationUtil;
import com.ever365.ecm.entity.EntityDAOImpl;
import com.ever365.ecm.permission.AclDAOImpl;
import com.ever365.ecm.repo.RepositoryDAOImpl;
import com.mongodb.DBCollection;

public class MultiTenantDataSource implements MongoDataSource {

	private MongoDataSource wrapped;
	@Override
	public DBCollection getCollection(String name) {
		if (AuthenticationUtil.getTenant()==null || "".equals(AuthenticationUtil.getTenant()) ) {
			return wrapped.getCollection(name);
		} else {
			return wrapped.getCollection(AuthenticationUtil.getTenant() + "." + name);
		}
	}

	@Override
	public DBCollection getCollection(String dbName, String collName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clean() {
		if (AuthenticationUtil.getTenant()==null || "".equals(AuthenticationUtil.getTenant()) ) {
			wrapped.clean();
		} else {
			wrapped.getCollection(AuthenticationUtil.getTenant() + "." + EntityDAOImpl.ENTITIES).drop();
			wrapped.getCollection(AuthenticationUtil.getTenant() + "." + RepositoryDAOImpl.REPOSITORIES).drop();
			wrapped.getCollection(AuthenticationUtil.getTenant() + "." + AclDAOImpl.ACE).drop();
			wrapped.getCollection(AuthenticationUtil.getTenant() + "." + AclDAOImpl.GROUP).drop();
		}
	}

}
