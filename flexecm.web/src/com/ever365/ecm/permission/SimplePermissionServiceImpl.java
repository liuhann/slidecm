package com.ever365.ecm.permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ever365.ecm.entity.Entity;

public class SimplePermissionServiceImpl implements PermissionService {

	
	@Override
	public boolean isAdmin(String user) {
		return false;
	}

	@Override
	public void setInheritParentPermissions(Entity entity,
			boolean inheritParentPermissions) {

	}

	@Override
	public void setPermission(Entity entity, String authority,
			String permission, boolean allow) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ACE> getPermissions(Entity entity) {
		return null;
	}

	@Override
	public List<ACE> getPermissions(Entity entity, String auth) {
		return null;
	}

	@Override
	public List<ACE> getAllSetPermissions(Entity entity, boolean includeInherit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPermission(Entity entity, String permission) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasReadPermission(Entity entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearPermission(Entity entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deletePermission(Entity entity, String authority,
			String permission) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Entity> getEntitiesBySource(String authority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Entity> getEntitiesByTarget(String authority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getInheritParentPermissions(Entity entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, List<Access>> getPermissionGroups() {
		// TODO Auto-generated method stub
		return null;
	}

}
