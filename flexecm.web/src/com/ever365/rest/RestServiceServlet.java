package com.ever365.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.ContextLoaderListener;


/**
 * Servlet implementation class RestServiceServlet
 */
public class RestServiceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String UTF_8 = "UTF-8";
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

		Object user = request.getSession().getAttribute(AuthenticationUtil.SESSION_CURRENT_USER);
		if (user!=null) {
			AuthenticationUtil.setCurrentUser((String)user);
		} else {
			AuthenticationUtil.setCurrentUser(null);
		}
		
		String strPath = URLDecoder.decode(request.getRequestURI(), "UTF-8");
		String servletPath = request.getServletPath();
		
		int rootPos = strPath.indexOf(servletPath);
		if ( rootPos != -1)
			strPath = strPath.substring( rootPos + servletPath.length());
		
		MethodInvocation handler = registry.getGet(strPath);
		if (handler==null) {
			response.setStatus(404);
			return;
		}
		if (handler.isAuthenticated() && AuthenticationUtil.getCurrentUser()==null) {
			response.setStatus(401);
			return;
		}
		if (handler.isRunAsAdmin() && !AuthenticationUtil.isAdmin()) {
			response.setStatus(403);
			return;
		}
		
		Enumeration paramNames = request.getParameterNames();
		Map<String, Object> args = new HashMap<String, Object>();
		while (paramNames.hasMoreElements()) {
			String name = (String) paramNames.nextElement();
			args.put(name, URLDecoder.decode(request.getParameter(name), "UTF-8"));
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
			String strPath = URLDecoder.decode( request.getRequestURI(), "UTF-8");
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
			if (handler.isAuthenticated() && AuthenticationUtil.getCurrentUser()==null) {
				response.setStatus(401);
				return;
			}
			if (handler.isRunAsAdmin() && !AuthenticationUtil.isAdmin()) {
				response.setStatus(403);
				return;
			}
			
			Map<String, Object> args = new HashMap<String, Object>();
			
			if (handler.isMultipart() && ServletFileUpload.isMultipartContent(request)) {
				//这是post的上传流请求
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				// Parse the request
				List<FileItem> items = upload.parseRequest(request);

				args = new HashMap<String, Object>();
				//boolean hasFile = false;
				for (FileItem item : items) {
					if (item.isFormField()) {
						args.put(item.getFieldName(), item.getString(UTF_8));
					} else {
						args.put(item.getFieldName(), item);
						args.put("size", item.getSize());
					}
				}
			} else {
				Enumeration paramNames = request.getParameterNames();
				while (paramNames.hasMoreElements()) {
					String name = (String) paramNames.nextElement();
					args.put(name, URLDecoder.decode(request.getParameter(name), UTF_8));
				}
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
		
		//logger.info("Request: " + request.getMethod() + "   " + request.getPathInfo() + "?" + request.getQueryString());
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
			}
			return;
		}
		
		if (result instanceof RestDownload) {
			handleFileDownload(request, response, (RestDownload)result);
			return;
		} 
		
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

	public void handleFileDownload(HttpServletRequest request,
			HttpServletResponse response, RestDownload result)
			throws UnsupportedEncodingException, IOException {
		
		long modifiedSince = request.getDateHeader("If-Modified-Since");
		
		if (modifiedSince>0L) {
			if (result.getLastModified()<=modifiedSince) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		} else {
			response.setDateHeader("Last-Modified", result.getLastModified());
			response.setHeader("Cache-Control", "must-revalidate");
			response.setHeader("ETag", "\"" + result.getLastModified() + "\"");
		}
		
		String userAgent = request.getHeader("User-Agent");
		if (result.getFileName()!=null) {
			String fileDwnName = new String(result.getFileName().getBytes("UTF-8"), "ISO-8859-1");
			if (userAgent!=null && userAgent.contains("MSIE")) {
				fileDwnName = new String(fileDwnName.getBytes("gb2312"), "ISO8859-1");
			}
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileDwnName + "\";");
		}
		
		response.setHeader("Accept-Ranges", "bytes");
		
		//断点下载处理
		String range = request.getHeader("Content-Range");
		if (range == null) {
			range = request.getHeader("Range");
		}
		if (range != null)  {
			
		}
		
		response.setContentType(result.getMimeType());
		int size = result.getSize();
		response.setHeader("Content-Range", "bytes 0-"
				+ Long.toString(size - 1L) + "/"
				+ Long.toString(size));
		response.setContentLength(size);
		response.setHeader("Content-Length", Integer.toString(size));
		
		FileCopyUtils.copy(result.getInputStream(), response.getOutputStream());
	}

}