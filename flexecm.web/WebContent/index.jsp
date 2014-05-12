<%@page import="com.ever365.utils.StringUtils"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="com.ever365.ecm.service.servlet.LoginServlet"%>
<%@page import="com.ever365.ecm.authority.PersonService"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.springframework.web.context.ContextLoaderListener"%>
<%@page import="com.ever365.ecm.service.PublicService"%>
<html>
<head>
<meta charset="UTF-8">
<meta name="baidu-site-verification" content="e9546b516f21a9610998edc067b3c6b5"/>

<title>slide8快阅读-创造和分享您的展示</title>
<%
response.setContentType("text/html;charset=UTF-8");

PublicService publicService = (PublicService)ContextLoaderListener.getCurrentWebApplicationContext().getBean("rest.public");
Object user = session.getAttribute(LoginServlet.SESSION_USER);

List<Map<String, Object>> splash =  publicService.getList("展示");
List<Map<String, Object>> recList =  publicService.getList("首页推荐");
List<Map<String, Object>> hotList =  publicService.getList("热门");
List<Map<String, Object>> recentList =  publicService.getList("最新资源");
%>
<script src="http://libs.baidu.com/jquery/2.0.0/jquery.min.js"></script>
<script type="text/javascript" language="javascript" src="js/util.js"></script>
<script type="text/javascript" language="javascript" src="js/index.js"></script>
<link rel="StyleSheet" href="css/index.css"  type="text/css">

<script type="text/javascript">
	var person = null; 
	<%if (session.getAttribute(LoginServlet.SESSION_USER)!=null) { %>
	person = "<%=session.getAttribute(LoginServlet.SESSION_USER)%>";
	<%}%>
</script>
</head>
<body>

<div class="header">

	<div class="nav"><a href="javascript:myHome();">我的内容库</a></div>
	
	<div class="login-wrapper">
		<strong class="status"><i class="user icon"></i><span> 帐户</span></strong>
		<span class="nav-dropdown">
			<strong class="guest" onclick="loginDialog();"><i class="icon login"></i> <span>登录</span></strong>
			<strong class="guest" onclick="registerDialog();"><i class="icon register"></i> 注册</strong>
			<strong class="logon" style="display:none;" onclick="logout();"><i class="icon login"></i> 退出登录</strong>
		</span>
	</div>
</div>

<div class="main">
	<div class="logo row">
		<img src="../img/slide8.png">
  
	</div>
	<div class="list row">
	<%
		if (splash.size()>=1) {
			Map<String, Object> splashOne = (Map<String, Object>)splash.get(0);
	%>		
		<div class="article slide">
			<a href="/view.jsp?seq=<%=splashOne.get("seq")%>" target="_blank">
				<img class="article-image" src="/pub/file/image?id=<%=splashOne.get("tn")%>">
				<h1 class="article-title"><%=StringUtils.getFileName((String)splashOne.get("name")) %></h1>
				
				<ul class="meta">
          			<li>
          				<span class="publisher"><%=splashOne.get("creator") %></span>• 
          				<time class="time-formated"><%=splashOne.get("modified") %></time>
          			</li>
        		</ul>
			</a>
		</div>
	<%		
		}
	%>
		
		<div class="recommend">
			<h2>
				推荐
			</h2>
			
			<%
				Integer count = 0;
				for(Object o : recList) {
					if (count>=4) {
						break;
					}
					Map<String, Object> recommend = (Map<String, Object>)o;
			%>
					<div class="view">
						<a  href="/view.jsp?seq=<%=recommend.get("seq")%>" target="_blank">
						<img class="article-image" src="/pub/file/image?id=<%=recommend.get("tn")%>">
						<h1 class="article-title"><%=StringUtils.getFileName((String)recommend.get("name")) %></h1>
						</a>
					</div>			
			<%
				}
			%>
		</div>
		
	</div>

	<div class="sep-title">
		<i class="recent icon"></i>最新展示
	</div>
	<div class="pager row">
			<%
				for(Object o : recentList) {
					Map<String, Object> recent = (Map<String, Object>)o;
			%>
				
					<div class="article">
						<a href="/view.jsp?seq=<%=recent.get("seq")%>" target="_blank">
						
							<img class="article-image" src="/pub/file/image?id=<%=recent.get("tn")%>">
							<h1 class="article-title"><%=StringUtils.getFileName((String)recent.get("name")) %></h1>
							
							<ul class="meta">
			          			<li>
			          				<span class="publisher"><%=recent.get("creator") %></span>• 
			          				<time class="time-formated"><%=recent.get("modified") %></time>
			          			</li>
			        		</ul>
						</a>
					</div>
						
			<%
				}
			%>
			
		<div class="article more">
			<a href="javascript:more();">
				
				<h1>+获取更多</h1>
			</a>
		</div>
		
		
		<div class="article template" style="display: none;">
			<a class="seq" href="/view.jsp?seq=19" target="_blank">
			
				<img class="article-image" src="/pub/file/image?id=53547ff0e4b0c07f638d8fd8" style="width: 338.6666666666667px; margin-top: 0px;">
				<h1 class="article-title">时间管理技能（布衣公子作品）@teliss</h1>
				
				<ul class="meta">
          			<li>
          				<span class="publisher">admin</span>• 
          				<time class="time-formated">1 分钟前</time>
          			</li>
        		</ul>
			</a>
		</div>
		
		
	</div>
	
	<div class="aside">
		<div class="title">
			<a>
				<h2>
					<i class="hottest"></i>热门文件
				</h2>
			</a>
		</div>
		
		<%
				for(Object o : hotList) {
					Map<String, Object> hot = (Map<String, Object>)o;
			%>
				
				<div>
					<a class="top" target="_blank" >
						<strong><%=hot.get("name") %></strong>
						<em>23人评论</em>
					</a>
				</div>
			<%
				}
			%>
		
		
	</div>
</div>


<div id="modal" class="">
    <div class="modal-wrapper" id="login">
      <h2>
      	 <span>登录</span> 
      	<div class="dialog-handle"><a href="javascript:closeDialog();" class="diag-close">关闭</a></div>
      </h2>
      
      <div class="body">
        <div class="msg">
          <span class="news-tip-info">没有帐号？<a class="goto-signup" href="javascript:registerDialog();">现在注册 <i class="link-arrow"></i></a></span>
        </div>
        
		<div>
			<div class="item">
			  <span class="label">用户名</span>
			  <input type="text" name="log" id="user_login" class="input" value="" size="20">
			</div>
			<div class="item">
			  <span class="label">密码</span>
			  <input type="password" name="pwd" id="user_pass" class="input" value="" size="20">
			</div>
			<div class="item" style="margin-left: 80px;">
			  <a class="button blue"  href="javascript:login();">登录</a>
			  <a id="logon-result"></a>
			</div>
		</div>
      </div>
    </div>
    
    <div class="modal-wrapper" id="register">
      <h2>
      	 <span>用户注册</span> 
      	<div class="dialog-handle"><a href="javascript:closeDialog();" class="diag-close">关闭</a></div>
      </h2>
      
      <div class="body">
		<div>
			<div class="item">
			  <span class="label">用户名</span>
			  <input type="text" id="reg_user" class="input" value="" size="20">
			</div>
			<div class="item">
			  <span class="label">密码</span>
			  <input type="password" id="reg_user_pass" class="input" value="" size="20">
			</div>
			<div class="item">
			  <span class="label">确认密码</span>
			  <input type="password" id="reg_user_pass_cfm" class="input" value="" size="20">
			</div>
			<div class="item">
			  <span class="label">电子邮件</span>
			  <input type="text"id="reg_email" class="input" value="" size="20">
			</div>
			<div class="item " style="margin-left: 80px;">
			  <a class="button blue">注册</a> <a id="reg-result"></a>
			</div>
		</div>
      </div>
    </div>
    
    
    <div class="modal-bg"></div>
  </div>
  


</body>
</html>
