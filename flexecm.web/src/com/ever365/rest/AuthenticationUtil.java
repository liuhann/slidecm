package com.ever365.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread local utils which holds tenant and user 
 * @author LiuHan 
 */
public class AuthenticationUtil
{
	public static String EMPTY_TENANT = "default";
	public static String SYSTEM = "system";
	
	public static String SESSION_CURRENT_USER = ".cu";
	
	
    private static ThreadLocal<String> currentUser = new ThreadLocal<String>();
    private static ThreadLocal<String> tenant =  new ThreadLocal<String>();
    
    private static ThreadLocal<Boolean> runAsAdmin =  new ThreadLocal<Boolean>();
    
	public static void setRunAsAdmin() {
		runAsAdmin.set(true);
    }
	
	public static List<String> getCurrentAuthorities()  {
		List<String> r = new ArrayList<String>();
		r.add(currentUser.get());
		return r;
	}
	
	public static void unsetRunAsAdmin() {
		runAsAdmin.set(false);
    }

	public static boolean isAdmin() {
		return runAsAdmin.get();
	}
    
	public static void setCurrentUser(String user) {
		currentUser.set(user);
    }
	
	public static String getCurrentUser() {
    	return currentUser.get();
    }
  
	public static void setTenant(String t) {
		tenant.set(t);
    }
  
    /**
     * Remove the current security information
     */
    public static void clearCurrentSecurityContext()
    {
    	currentUser.set(null);
    	runAsAdmin.set(false);
    }
}
