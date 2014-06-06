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
import com.ever365.rest.RestServiceServlet;


/**
 * Servlet implementation class OAuthServlet
 */
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private CookieService cookieService;
	
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
		
		TopOAuthProvider topOAuthProvider = new TopOAuthProvider();
		providers.put("/top", topOAuthProvider);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String servletPath = getServicePath(request);
		
		OAuthProvider provider = providers.get(servletPath);
		
		Map<String, Object> detail = provider.authorize(request.getParameter(provider.getCode()));
		
		request.getSession().setAttribute(AuthenticationUtil.SESSION_CURRENT_USER, detail.get(OAuthProvider.USERID));
		
		if (cookieService!=null) {
			cookieService.bindUserCookie(request, response, detail.get(OAuthProvider.USERID).toString());
		}
		response.sendRedirect("/");
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
		// TODO Auto-generated method stub
	}

}
