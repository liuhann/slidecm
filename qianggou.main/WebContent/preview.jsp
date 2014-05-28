<%@page import="com.ever365.common.StringUtils"%>
<%@page import="java.util.Date"%>
<%@ page language="java" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<%@page import="java.util.Map"%>
<%@page import="org.springframework.web.context.ContextLoaderListener"%>
<%@page import="com.ever365.qiang.SellerService"%>
<html>
<head>
<%
response.setContentType("text/html;charset=UTF-8");
String id = request.getParameter("id");

if (id==null) {
	response.sendError(400);
	return;
}

SellerService sellerService = (SellerService)ContextLoaderListener.getCurrentWebApplicationContext().getBean("rest.seller");

Map sale = sellerService.getRequest(id);

Long time = (Long)sale.get("time");
Date timed = new Date(time);
Date left = new Date((Long)sale.get("time") - System.currentTimeMillis());


%>
<title></title>

<link rel="StyleSheet" href="/css/common.css"  type="text/css">
</head>
<body>


<header>
	<div class="wrapper">
		<h1>--zxa</h1>
	</div>
</header>


<div class="wrapper preview">
	
	<div class="block ">
		<div class="splash">
			<img src="img/1.jpg" style="width:480px; height: 400px;">
		</div>
		
		<div class="des">
			<div class="title"><%=sale.get("title")%></div>
			<div class="subtitle"><%=sale.get("subtitle")%></div>
		    <div class="time-left">还剩 <%=left.getDate() %>天 <%=left.getHours() %>小时 <%=left.getMinutes() %>分</div>
			<div class="price-wrap">
			    <h3 class="price">
			        <span class="price-display"><em>￥</em><%=sale.get("price") %></span>
					<span class="sub-price">
						<span class="discount"></span>
			    		原价<span class="original">&nbsp;¥496</span>
					</span>
			    </h3>
			</div>
			
			<div class="box">
				<table>
					<tr>
						<td>
							<div class="grey">抢购时间</div>
							<div class="focus"><%=StringUtils.formateDate(timed)%></div>
						</td>
						<td>
							<div class="grey">预约人数</div>
							<div class="focus">2301</div>
						</td>
						<td>淘宝交易 成交返现</td>
					</tr>
				</table>
			
			</div>
			
			
			<div class="agree">
				<label for="agreement"><input type="checkbox" class="checkbox" id="agreement" checked="">我已阅读并同意</label> <a href="#agreecontent">预约及购买规则</a>
			</div>
			<div class="booki">
				<a class="btn">立刻预约</a>
			</div>
			
			
		</div>
		
	</div>

	<div class="block agreement" id="agreecontent">
		<div class="title">预约规则</div>
		<ol>
			<li>预约表示您获得了指定时间进行抢购的资格，不代表能购买成功</li>
			<li>抢购成功后请前往商户指定地址购买，通过抢购码来获得相应的优惠</li>
			<li>为保护用户的利益，打击黄牛，商家保留取消恶意抢购的权利</li>
			<li>在法律允许范围内本站保留对本次活动的最终解释权。</li>
		</ol>
	</div>
	
	<div class="block">
		<%=sale.get("content") %>
	</div>
	

</div>


</body>
</html>