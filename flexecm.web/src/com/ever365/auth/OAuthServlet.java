package com.ever365.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ever365.rest.AuthenticationUtil;


/**
 * Servlet implementation class OAuthServlet
 */
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
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
		
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String servletPath = request.getServletPath();
		
		OAuthProvider provider = providers.get(servletPath);
		
		Map<String, Object> detail = provider.authorize(request.getParameter(provider.getCode()));
		
		request.getSession().setAttribute(AuthenticationUtil.SESSION_CURRENT_USER, detail.get(OAuthProvider.USERID));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
