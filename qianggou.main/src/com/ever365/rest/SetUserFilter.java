package com.ever365.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Liu Han
 */

public class SetUserFilter implements Filter {

	private String loginPage = "/index.html";

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain fc) throws IOException, ServletException {
		
		HttpServletRequest httpreq = (HttpServletRequest) req;
		HttpServletResponse httpres = (HttpServletResponse) resp;
		
		Object user = httpreq.getSession().getAttribute(AuthenticationUtil.SESSION_CURRENT_USER);
		if (user==null) {
			httpres.sendRedirect(loginPage);
			return;
		} else {
			AuthenticationUtil.setCurrentUser((String)user);
		}
		fc.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		String lp = fc.getServletContext().getInitParameter("loginPage");
		if (lp!=null) {
			loginPage = lp;
		}
	}

}
