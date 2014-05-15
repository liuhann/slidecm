package com.ever365.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.FileCopyUtils;

import com.baidu.inf.iis.bcs.BaiduBCS;
import com.baidu.inf.iis.bcs.auth.BCSCredentials;
import com.baidu.inf.iis.bcs.model.DownloadObject;
import com.baidu.inf.iis.bcs.model.ObjectMetadata;
import com.baidu.inf.iis.bcs.request.GetObjectRequest;
import com.baidu.inf.iis.bcs.request.PutObjectRequest;
import com.baidu.inf.iis.bcs.response.BaiduBCSResponse;

/**
 * 根据ID读写文件流。  可以本地读写，也可以配置为百度云的地址
 * @author Administrator
 *
 */
public class ContentStore {

	private String appKey;
	private String appSecret;
	private String bcsHost;
	private String bucketName;

	private BaiduBCS baiduBCS;
	
	private String localPath;
	
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public void setBcsHost(String bcsHost) {
		this.bcsHost = bcsHost;
	}
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
	
	public InputStream getContentData(String uid) {
		if (localPath!=null) {
			File f = new File(localPath, uid);
			if (f.exists()) {
				try {
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			return null;
		} else {
			GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, uid);
			BaiduBCSResponse<DownloadObject> result = getBCS().getObject(getObjectRequest);
			return result.getResult().getContent();
		}
	}
	
	public String putContent(String uid, InputStream inputStream,
			String contentType, long size) {
		
		if (localPath!=null) {
			File f = new File(localPath, uid);
			
			try {
				if (!f.getParentFile().exists()) {
					f.getParentFile().mkdirs();
				}
				boolean created = f.createNewFile();
				if (!created) {
					throw new IOException("File can not be created");
				}
				FileCopyUtils.copy(inputStream, new FileOutputStream(f));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return uid;
		} else {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(size);
			String object = "/" + uid;
			
			BaiduBCS baiduBCS = getBCS();
			
			PutObjectRequest request = new PutObjectRequest(bucketName, object, inputStream, objectMetadata);
			ObjectMetadata result = baiduBCS.putObject(request).getResult();
			return object;
		}
	}
	public BaiduBCS getBCS() {
		if (baiduBCS==null) {
			synchronized (this) {
				if (baiduBCS==null) {
					BCSCredentials credentials = new BCSCredentials(appKey, appSecret);
					baiduBCS = new BaiduBCS(credentials, bcsHost);
					baiduBCS.setDefaultEncoding("UTF-8"); // Default UTF-8
				}
			}
		}
		return baiduBCS;
	}
	
}
