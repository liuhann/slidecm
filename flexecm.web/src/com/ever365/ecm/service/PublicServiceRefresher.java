package com.ever365.ecm.service;

import java.io.InputStream;
import java.util.List;

import com.ever365.ecm.authority.AuthenticationUtil;
import com.ever365.ecm.authority.PersonService;
import com.ever365.ecm.entity.Entity;
import com.ever365.ecm.repo.Model;
import com.ever365.ecm.service.listener.RepositoryListener;

/**
 * 只有当前用户为admin才能运行的监听器
 * @author Han
 */

public class PublicServiceRefresher implements RepositoryListener {

	private PublicService publicService;
	
	private Boolean enabled;
	
	public PublicService getPublicService() {
		return publicService;
	}

	public void setPublicService(PublicService publicService) {
		this.publicService = publicService;
	}

	@Override
	public void beforeFileUpload(String repository, String path, String name,
			InputStream is, long size) {
		
	}

	@Override
	public void onFileUploaded(Entity parent, Entity entity) {
		if (listContains(parent.getPropertyList(Model.PROP_KEEP_USRS), PersonService.ADMIN)) {
			publicService.clearData(parent.getName());
		}
	}
	
	public boolean listContains(List<String> list, String target) {
		if (list==null) return false;
		return list.contains(target);
	}
	
	@Override
	public void onFolderCreated(Entity parent, Entity entity) {
		
	}

	@Override
	public void onMoved(Entity srcEntity, Entity targetEntity) {
		if (listContains(targetEntity.getPropertyList(Model.PROP_KEEP_USRS), PersonService.ADMIN)) {
			publicService.clearData(targetEntity.getName());
		} 
	}

	@Override
	public void onCopied(Entity srcEntity, Entity targetEntity) {
		if (listContains(targetEntity.getPropertyList(Model.PROP_KEEP_USRS), PersonService.ADMIN)) {
			publicService.clearData(targetEntity.getName());
		}
	}

	@Override
	public void onDeleted(Entity entity) {
		publicService.clearData(null);
	}

	@Override
	public void onRecovered(Entity entity) {
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnabled(boolean e) {
		this.enabled = e;
	}

	@Override
	public boolean enabled() {
		return enabled && AuthenticationUtil.isAdmin();
	}

}
