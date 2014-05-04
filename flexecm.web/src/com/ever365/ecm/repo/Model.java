package com.ever365.ecm.repo;

/**
 * System defined namespaces, types and properties
 * @author Liu Han
 */
public interface Model {
	
	public static final String SYSTEM_NAMESPACE = "s";
	public static final String SYSTEM_FILE_NAMESPACE = "f";
	public static final String SYSTEM_CONTENT_NAMESPACE = "cm";
	
	
	public static final QName PROP_SEQ = QName.createQName(SYSTEM_NAMESPACE, "seq");
	public static final QName PROP_ACL = QName.createQName(SYSTEM_NAMESPACE, "acl");
	public static final QName PROP_ACL_INHERIT = QName.createQName(SYSTEM_NAMESPACE, "inherit_acl");
	public static final QName PROP_UUID = QName.createQName(SYSTEM_NAMESPACE, "uid");
	public static final QName PROP_PARENT_ID = QName.createQName(SYSTEM_NAMESPACE, "pid");
	public static final QName PROP_NAME = QName.createQName(SYSTEM_NAMESPACE, "name");
	public static final QName PROP_REPO = QName.createQName(SYSTEM_NAMESPACE, "repo");
	public static final QName PROP_TYPE = QName.createQName(SYSTEM_NAMESPACE, "type");
	public static final QName PROP_ASSOC_TYPE = QName.createQName(SYSTEM_NAMESPACE, "assoc"); 
	public static final QName PROP_ASPECTS = QName.createQName(SYSTEM_NAMESPACE, "aspects");
	
	public static final QName PROP_CREATOR = QName.createQName(SYSTEM_NAMESPACE, "creator");
	public static final QName PROP_CREATED = QName.createQName(SYSTEM_NAMESPACE, "created");
	public static final QName PROP_AUTHOR = QName.createQName(SYSTEM_NAMESPACE, "author");
	public static final QName PROP_MODIFIER = QName.createQName(SYSTEM_NAMESPACE, "modifier");
	public static final QName PROP_MODIFIED = QName.createQName(SYSTEM_NAMESPACE, "modified");
	public static final QName PROP_OWNER = QName.createQName(SYSTEM_NAMESPACE, "owner");
		
	public static final QName PROP_COPYED_FROM = QName.createQName(SYSTEM_NAMESPACE, "copyfrom");
	
	public static final QName PROP_PERM_READER = QName.createQName(SYSTEM_NAMESPACE, "reader");
	public static final QName PROP_PERM_EDITOR = QName.createQName(SYSTEM_NAMESPACE, "editor");
	public static final QName PROP_PERM_COORDINATOR = QName.createQName(SYSTEM_NAMESPACE, "coordinator");
	
	
	
	
	public static final QName SYS_CONTAINS = QName.createQName(SYSTEM_NAMESPACE, "contains");
	public static final QName FS_CONTAINS = QName.createQName(SYSTEM_NAMESPACE, "contains");
	public static final QName FS_RENDERS = QName.createQName(SYSTEM_FILE_NAMESPACE, "render");
	

	public static final QName TYPE_FOLDER = QName.createQName(SYSTEM_NAMESPACE, "folder");
	public static final QName TYPE_FILE = QName.createQName(SYSTEM_NAMESPACE, "file");
	
	public static final QName TYPE_CONTENT_PERSON = QName.createQName(SYSTEM_NAMESPACE, "person");
	
	public static final QName PROP_FILE_URL = QName.createQName(SYSTEM_NAMESPACE, "url");
	public static final QName PROP_FILE_THUMBNAIL = QName.createQName(SYSTEM_NAMESPACE, "tn");
	public static final QName PROP_FILE_THUMBNAIL_SIZE = QName.createQName(SYSTEM_NAMESPACE, "tnsize");
	public static final QName PROP_FILE_ICON = QName.createQName(SYSTEM_NAMESPACE, "icon");
	
	
	public static final QName PROP_FILE_PAN_URLS = QName.createQName(SYSTEM_NAMESPACE, "pans");
	public static final QName PROP_FILE_URL_BAIDU = QName.createQName(SYSTEM_NAMESPACE, "baidu");
	public static final QName PROP_FILE_URL_115 = QName.createQName(SYSTEM_NAMESPACE, "y115");
	public static final QName PROP_FILE_URL_360 = QName.createQName(SYSTEM_NAMESPACE, "y360");
	
	public static final QName PROP_FILE_SIZE = QName.createQName(SYSTEM_NAMESPACE, "size");
	public static final QName PROP_FILE_EXT = QName.createQName(SYSTEM_NAMESPACE, "ext");
	
	public static final QName PROP_FILE_TOTAL = QName.createQName(SYSTEM_NAMESPACE, "fit");
	public static final QName PROP_FOLDER_TOTAL = QName.createQName(SYSTEM_NAMESPACE, "fot");
	public static final QName PROP_QUOTA = QName.createQName(SYSTEM_NAMESPACE, "quota");
	
	
	public static final QName PROP_DESC = QName.createQName(SYSTEM_NAMESPACE, "desc");
	public static final QName PROP_TITLE = QName.createQName(SYSTEM_NAMESPACE, "title");
	
	public static final QName PROP_ORIGIN_PATH = QName.createQName(SYSTEM_NAMESPACE, "opath");
	public static final QName PROP_ORIGIN_NAME = QName.createQName(SYSTEM_NAMESPACE, "oname");
	public static final QName PROP_RECOVERED = QName.createQName(SYSTEM_NAMESPACE, "recovered");
	
	public static final QName PROP_PASSWORD = QName.createQName(SYSTEM_NAMESPACE, "pwd");
	public static final QName PROP_EMAIL = QName.createQName(SYSTEM_NAMESPACE, "email");
	public static final QName PROP_PERSON_REPO = QName.createQName(SYSTEM_NAMESPACE, "prepo");
	
	public static final QName PROP_IS_ROOT = QName.createQName(SYSTEM_NAMESPACE, "root");
	
	public static final QName FACETED = QName.createQName(SYSTEM_NAMESPACE, "faceted");
	public static final QName PUBLISHED = QName.createQName(SYSTEM_NAMESPACE, "pub");
	public static final QName PUBLISHED_SEQ = QName.createQName(SYSTEM_NAMESPACE, "pubseq");
	
	public static final QName PROP_ADMIN_EMAIL = QName.createQName(SYSTEM_NAMESPACE, "adminemail");
	public static final QName PROP_ADMIN_EMAIL_OK = QName.createQName(SYSTEM_NAMESPACE, "adminemailok");
	public static final QName PROP_ADMIN_SMTP = QName.createQName(SYSTEM_NAMESPACE, "smtp");
	public static final QName PROP_ADMIN_PORT = QName.createQName(SYSTEM_NAMESPACE, "smtpport");
	public static final QName PROP_ADMIN_SMTP_PASS = QName.createQName(SYSTEM_NAMESPACE, "smtppass");
	public static final QName PROP_DEFAULT_STORE = QName.createQName(SYSTEM_NAMESPACE, "defaultstore");
	
	public static final QName PROP_KEEP_USRS = QName.createQName(SYSTEM_NAMESPACE, "keep");
	public static final QName PROP_READ_COUNT = QName.createQName(SYSTEM_NAMESPACE, "read");
	
	public static final QName ASSOC_FRIEND = QName.createQName(SYSTEM_NAMESPACE, "friend");
	
	
	
	
}

