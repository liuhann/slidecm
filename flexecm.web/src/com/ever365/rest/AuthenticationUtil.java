package com.ever365.rest;

import java.util.ArrayList;
import java.util.List;

import com.ever365.auth.AuthorityService;

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
    
    
	public static List<String> getCurrentAuthorities()  {
		List<String> r = new ArrayList<String>();
		r.add(currentUser.get());
		return r;
	}
	
	public static boolean isAdmin() {
		if (AuthorityService.ADMIN.equals(currentUser.get())) {
			return true;
		} else {
			return false;
		}
	}
    
	public static void setCurrentUser(String user) {
		currentUser.set(user);
    }
	
	public static String getCurrentUser() {
    	return currentUser.get();
    }
  
    /**
     * Remove the current security information
     */
    public static void clearCurrentSecurityContext()
    {
    	currentUser.set(null);
    }
}
