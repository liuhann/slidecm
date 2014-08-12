package com.ever365.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoaderListener;

import com.ever365.rest.AuthenticationUtil;
import com.ever365.rest.CookieService;


/**
 * Servlet implementation class OAuthServlet
 */
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private CookieService cookieService;
	private AuthorityService authorityService;
	public static final String SESSION_REDIRECT = "oauth_redirect";
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OAuthServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    private Map<String, OAuthProvider> providers = new HashMap<String, OAuthProvider>();
    
	@Override
	public void init() throws ServletException {
		super.init();
		Object o = ContextLoaderListener.getCurrentWebApplicationContext().getBean("rest.cookie");
		if (o!=null) {
			cookieService = (CookieService) o;
		}
		authorityService = (AuthorityService) ContextLoaderListener.getCurrentWebApplicationContext().getBean("rest.authority");
		
		providers.put("/top", new TopOAuthProvider());
		providers.put("/weibo", (OAuthProvider)ContextLoaderListener.getCurrentWebApplicationContext().getBean("oauth.weibo"));
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String servletPath = getServicePath(request);
		try {
			Map<String, Object> detail = authorityService.validate(servletPath, request.getParameter("code"));
			
			if (detail==null || detail.get(OAuthProvider.USERID)==null) {
				response.sendRedirect("/?oauth fail");
				return ;
			}
			
			String userId = (String)detail.get(OAuthProvider.USERID);
			String realName = (String)detail.get(OAuthProvider.REAL_NAME);
			
			request.getSession().setAttribute(AuthenticationUtil.SESSION_CURRENT_USER, userId);
			request.getSession().setAttribute(AuthenticationUtil.SESSION_CURRENT_USER_RN, realName);
			
			AuthenticationUtil.setCurrentUser(userId);
			AuthenticationUtil.setRealName(realName);
			
			if (cookieService!=null) {
				cookieService.bindUserCookie(request, response, userId);
			}
		} catch (Throwable t) {
			System.out.println(t.getLocalizedMessage());
		}
		
		if (request.getSession().getAttribute(SESSION_REDIRECT)!=null) {
			Object redirect = request.getSession().getAttribute(SESSION_REDIRECT);
			request.getSession().removeAttribute(SESSION_REDIRECT);
			response.sendRedirect(redirect.toString());
		} else {
			response.sendRedirect("/");
		}
		//response.sendRedirect("/");
	}
	
	public String getServicePath(HttpServletRequest request)
			throws UnsupportedEncodingException {
		String strPath = URLDecoder.decode(request.getRequestURI(), "UTF-8");
		String servletPath = request.getServletPath();
		
		int rootPos = strPath.indexOf(servletPath);
		if ( rootPos != -1)
			strPath = strPath.substring( rootPos + servletPath.length());
		return strPath;
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("redirect")!=null) {
			request.getSession().setAttribute(SESSION_REDIRECT, request.getParameter("redirect"));
		}
	}

}
