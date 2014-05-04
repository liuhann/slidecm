package com.ever365.ecm.service.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoaderListener;

import com.ever365.ecm.authority.PersonService;

public class LoginServlet extends HttpServlet {

	public static final String SESSION_USER = ".user";

	private static final long serialVersionUID = -8073403112922258905L;

	private PersonService personService;
	
	private String logonPage;
	private String loginPage;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		personService = (PersonService)ContextLoaderListener.getCurrentWebApplicationContext().getBean("rest.person");
		
		logonPage = config.getServletContext().getInitParameter("logonPage");
		loginPage = config.getServletContext().getInitParameter("loginPage");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		String name = req.getParameter("name");
		String password = req.getParameter("password");
		
		if (name==null || password==null) {
			resp.sendError(400);
			return;
		}
		try {
			boolean checked = personService.checkPassword(name, password);
			if (checked) {
				req.getSession().setAttribute(SESSION_USER, name);
				resp.getWriter().println(name);
				return;
			} else {
				resp.sendError(401);
				return;
			}
		} catch (Exception e) {
			resp.sendError(401);
			
			/*
			try {
				boolean checked = personService.checkPassword(name, password);
				if (checked) {
					req.getSession().setAttribute(SESSION_USER, name);
					
					if (name.equals(PersonService.ADMIN)) {
						resp.sendRedirect("/admin/home.html");
						return;
					}
					resp.sendRedirect(logonPage);
				} else {
					resp.sendRedirect(loginPage + "?" + URLEncoder.encode("用户名和密码不匹配"));
				}
			} catch (Exception ne) {
				resp.sendRedirect(loginPage + "?" + URLEncoder.encode("服务器错误"));
			}
			*/
		}
	
	}

	
	
}
