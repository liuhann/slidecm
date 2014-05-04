package com.ever365.ecm.service.listener;

import java.io.InputStream;

import com.ever365.ecm.entity.Entity;

/**
 * Registered events for repository.
 * @author Liu Han
 */

public interface RepositoryListener {
	
	public void beforeFileUpload(String repository, String path, String name,
			InputStream is, long size);
	
	public void onFileUploaded(Entity parent, Entity entity);
	
	public void onFolderCreated(Entity parent, Entity entity);
	
	public void onMoved(Entity srcEntity, Entity targetParent);
	
	public void onCopied(Entity srcEntity, Entity targetParent);
	
	public void onDeleted(Entity entity);

	public void onRecovered(Entity entity);
	
	public String getName();
	
	public void setEnabled(boolean enable);
	
	public boolean enabled();
}
