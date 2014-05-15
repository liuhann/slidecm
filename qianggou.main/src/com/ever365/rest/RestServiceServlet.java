package com.ever365.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Servlet implementation class RestServiceServlet
 */
public class RestServiceServlet extends HttpServlet {
	private static final String UTF_8 = "UTF-8";

	private static final String MULTIFIELD_ENDS = "[]";

	private static final long serialVersionUID = 1L;

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	private HttpServiceRegistry registry;
	Logger logger = Logger. getLogger(RestServiceServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestServiceServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init(ServletConfig config) throws ServletException {
		registry = (HttpServiceRegistry)ContextLoaderListener.getCurrentWebApplicationContext().getBean("http.registry");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String strPath = URLDecoder.decode(request.getRequestURI(), UTF_8);
		String servletPath = request.getServletPath();
		
		int rootPos = strPath.indexOf(servletPath);
		if ( rootPos != -1)
			strPath = strPath.substring( rootPos + servletPath.length());
		
		MethodInvocation handler = registry.getGet(strPath);
		if (handler==null) {
			response.setStatus(404);
			return;
		}

		Enumeration paramNames = request.getParameterNames();
		Map<String, Object> args = new HashMap<String, Object>();
		while (paramNames.hasMoreElements()) {
			String name = (String) paramNames.nextElement();
			args.put(name, URLDecoder.decode(request.getParameter(name), UTF_8));
		}
		
		try {
			Object result = handler.execute(args);
			render(request, response, result);
		} catch (Exception e) {
			if (e instanceof HttpStatusException) {
				response.sendError(((HttpStatusException)e).getCode(), ((HttpStatusException)e).getDescription());
			} else {
				try {
					Object result = handler.execute(args);
					render(request, response, result);
				} catch (Exception ex) {
					e.printStackTrace(response.getWriter());
					response.setStatus(500);
				}
			}
		} finally {
			doCleanUp();
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String strPath = URLDecoder.decode( request.getRequestURI(), UTF_8);
			//request.setCharacterEncoding("UTF-8"); 
			String servletPath = request.getServletPath();

			int rootPos = strPath.indexOf(servletPath);
			if ( rootPos != -1)
				strPath = strPath.substring( rootPos + servletPath.length());

			MethodInvocation handler = registry.getPost(strPath);
			
			if (handler==null) {
				response.setStatus(404);
				return;
			}

			Map<String, Object> args = new HashMap<String, Object>();

			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String name = (String) paramNames.nextElement();
				args.put(name, URLDecoder.decode(request.getParameter(name), UTF_8));
			}
		
 			Object result = handler.execute(args);
			if (result==null) {
				//logger.info("Request: " + request.getMethod() + "   " + request.getPathInfo() + "?" + request.getQueryString());
				response.setStatus(200);
			} else {
				render(request, response, result);
			}
		} catch (Exception e) {
			if (e instanceof HttpStatusException) {
				response.getWriter().println(extractError(e));
				response.sendError(((HttpStatusException)e).getCode(), ((HttpStatusException)e).getDescription());
			} else {
				e.printStackTrace();
				response.sendError(501);
				response.getWriter().println(extractError(e));
			}
		} finally {
			doCleanUp();
		}
	}

	public void doCleanUp() {

	}

	public String extractError(Exception e) {
		StackTraceElement[] trances = e.getStackTrace();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < trances.length; i++) {
			sb.append(trances[i].getClassName() + "  " + trances[i].getLineNumber() + "\n");
		}
		sb.append(e.getMessage());
		return sb.toString();
	}

	private void render(HttpServletRequest request, HttpServletResponse response, Object result)
			throws IOException {
		if (result==null) {
			response.setStatus(201);
			return;
		}
		
		if (result instanceof RestResult) {
			RestResult rr = (RestResult) result;
			
			if (rr.getSession()!=null) {
				HttpSession session = request.getSession();
				for (String key : rr.getSession().keySet()) {
					session.setAttribute(key, rr.getSession().get(key));
				}
			}
			
			if (rr.getRedirect()!=null) {
				response.sendRedirect(rr.getRedirect());
				return;
			}
			
			if (rr.getTextPlain()!=null) {
				response.setContentType(CONTENT_TYPE);
				PrintWriter pw = response.getWriter();
				pw.print(rr.getTextPlain());
				pw.close();
				return;
			}
			
			if (rr.getJson()!=null) {
				response.setContentType(CONTENT_TYPE);
				PrintWriter pw = response.getWriter();
				JSONObject jo = new JSONObject(rr.getJson());
				pw.print(jo.toString());
				pw.close();
				return;
			}
		} else {
			response.setContentType(CONTENT_TYPE);
			PrintWriter pw = response.getWriter();
			if (result instanceof Collection) {
				JSONArray ja = new JSONArray((Collection) result);
				pw.print(ja.toString());
			} else if (result instanceof Map){
				JSONObject jo = new JSONObject((Map) result);
				pw.print(jo.toString());
			} else {
				pw.print(result.toString());
			}
			pw.close();
		}
		
	}

}