package com.ever365.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.springframework.util.FileCopyUtils;

import com.baidu.inf.iis.bcs.BaiduBCS;
import com.baidu.inf.iis.bcs.auth.BCSCredentials;
import com.baidu.inf.iis.bcs.model.DownloadObject;
import com.baidu.inf.iis.bcs.model.ObjectMetadata;
import com.baidu.inf.iis.bcs.model.Resource;
import com.baidu.inf.iis.bcs.request.CopyObjectRequest;
import com.baidu.inf.iis.bcs.request.DeleteObjectRequest;
import com.baidu.inf.iis.bcs.request.GetObjectRequest;
import com.baidu.inf.iis.bcs.request.PutObjectRequest;
import com.baidu.inf.iis.bcs.response.BaiduBCSResponse;
import com.ever365.rest.StreamObject;

/**
 * 根据ID读写文件流。  可以本地读写，也可以配置为百度云的地址
 * @author Administrator
 *
 */
public class ContentStore {

	private static final String SLASH = "/";
	private String appKey;
	private String appSecret;
	private String bcsHost;
	private String bucketName;

	private BaiduBCS baiduBCS;
	
	private String localPath;
	private static Logger logger = Logger.getLogger(ContentStore.class.getName());
	
	
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
	
	public void copyContent(String uid, String newUid) {
		if (localPath!=null) {
			File f = new File(localPath, uid);
			if (f.exists()) {
				try {
					putContent(uid, new FileInputStream(f), null, f.length());
				} catch (FileNotFoundException e) {
					
				}
			}
		} else {
			CopyObjectRequest copyObjectRequest = new CopyObjectRequest(new Resource(bucketName, uid),
					new Resource(bucketName, newUid));
			getBCS().copyObject(copyObjectRequest);
		}
	}
	
	
	public void deleteContent(String uid) {
		if (localPath!=null) {
			File f = new File(localPath, uid);
			if (f.exists()) {
				f.delete();
			}
		} else {
			DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, uid);
			getBCS().deleteObject(deleteObjectRequest);
		}
	}
	
	public StreamObject getContentData(String uid) {
		StreamObject so = new StreamObject();
		if (localPath!=null) {
			File f = new File(localPath, uid);
			if (f.exists()) {
				try {
					so.setFileName(f.getName());
					so.setInputStream(new FileInputStream(f));
					so.setLastModified(f.lastModified());
					so.setSize(f.length());
					return so;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			return null;
		} else {
			if (!uid.startsWith(SLASH)) {
				uid = SLASH + uid;
			}
			GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, uid);
			BaiduBCSResponse<DownloadObject> result = getBCS().getObject(getObjectRequest);
			so.setInputStream(result.getResult().getContent());
			so.setLastModified(result.getResult().getObjectMetadata().getLastModified().getTime());
			so.setSize(result.getResult().getObjectMetadata().getContentLength());
			return so;
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
			String object = SLASH + uid;
			
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
