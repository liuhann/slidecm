package com.ever365.ecm.permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ever365.ecm.entity.Entity;

public interface PermissionService {

	boolean isAdmin(String user);
	void setInheritParentPermissions(Entity entity,
			boolean inheritParentPermissions);
	void setPermission(Entity entity, String authority,
			String permission, boolean allow); 
	/**
	 * Get the permission which take effect
	 * @param entity
	 * @return
	 */
	List<ACE> getPermissions(Entity entity);

	/**
	 * Get permission current user has
	 * @param entity
	 * @param auth
	 * @return
	 */
	List<ACE> getPermissions(Entity entity, String auth);

	List<ACE> getAllSetPermissions(Entity entity, boolean includeInherit);

	boolean hasPermission(Entity entity, String permission);

	boolean hasReadPermission(Entity entity);

	void clearPermission(Entity entity);

	void deletePermission(Entity entity, String authority, String permission);

	Collection<Entity> getEntitiesBySource(String authority);

	Collection<Entity> getEntitiesByTarget(String authority);

	boolean getInheritParentPermissions(Entity entity);

	Map<String, List<Access>> getPermissionGroups();

}