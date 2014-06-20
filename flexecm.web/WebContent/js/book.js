var cu = null;

var countdown = false;
var onsale = 0;

var seed = 0;
var bottom = 0;

var currentSale = null;

$(document).ready(function() {
	var id = query_string("id");
	var oid = query_string("oid");
	if (id==null && oid==null) {
		//location.href = "/error/404.html";
		return;
	}
	var param = {};
	if (id==null) {
		param = {"oid": oid};
	} else {
		param = {"id": id};
	}
	
	$.getJSON("/service/sale", 
			param, function(sale) {
		currentSale = sale;
		$("#sale-info .title").html(sale.title);
		$("#sale-info .subtitle").html(sale.subtitle);
		$("#sale-info .bigprice").html(sale.price);
		$("#sale-info .original").html(sale.oprice);
		$("#sale-info .buydate").html(formate_time(sale.time));
		$("#content").html(sale.content);
		$(".splash img").attr("src", "/service/preview?id=" + sale.preview);
		if (sale.cu!=null) {
			$("#site-map .login").html(sale.cu);
		}
		
		var now = new Date();
		onsale = new Date(sale.time);
		
		var dura = onsale.getTime() - now.getTime();
		
		if (dura<0) {  
			if (dura<-30*60*1000 || sale.f==1) { //超过30分钟或者已卖完不再提供抢购  
				$("#btn_book").addClass("disabled");
				$("#btn_book").html("已售罄");
				$("#sale-info .time-left").html("已经抢购结束");
				$("#sale-info .time-left").css("background-color", "#888");
			} else {
				go();
			}
			return;
		}
	
		if (sale.seq==null) {
			$("#btn_book").addClass("disabled");
			$("#btn_book").html("审核后可预约");
			return;
		}
		
		if (sale.o) {
			$("#btn_book").addClass("disabled");
			$("#btn_book").html("已预约");
			$("#book_code").html("预约号 " + sale.o);
		} else if (now.getTime() > sale.until) {
			$("#btn_book").addClass("disabled");
			$("#btn_book").html("预约已结束");
		}
		
		if (in_8_hour(onsale) && sale.o) { //还有8小时并已预约则开始倒数
				timeCountDown();
		}
	}).fail(function() {
		location.href = "/404.html";
	});
});

function timeCountDown() {
	var remains = onsale.getTime() - new Date().getTime();
	if (remains<0) {
		go(remains);
	} else {
		$("#sale-info .time-left .day").html(time_dura_days(remains));
		$("#sale-info .time-left .hours").html(time_dura_hours(remains));
		$("#sale-info .time-left .minutes").html(time_dura_min(remains));
		$("#btn_book").html(time_dura_hours(remains) + "小时" +time_dura_min(remains) + "分" +time_dura_second(remains) + "秒后");
		setTimeout(timeCountDown, 1000);
	}
}

function go(dura) {
	if (dura<-30*60*1000) {
		$("#btn_book").addClass("disabled");
		$("#btn_book").html("已售罄");
		$("#sale-info .time-left").html("已经抢购结束");
		$("#sale-info .time-left").css("background-color", "#888");
	} else {
		$("#btn_book").removeClass("disabled");
		$("#btn_book").html("抢购");
		$("#btn_book").click(function() {
			buy();
		});
	}
}

function buy() {
	
}

function in_8_hour(d) {
	return (d.getTime() - new Date().getTime())<8*60*60*1000; 
}

function time_dura_days(dura) {
	return Math.floor(dura/(1000*60*60*24));
}
function time_dura_hours(dura) {
	return Math.floor((dura%(1000*60*60*24))/(1000*60*60));
}
function time_dura_min(dura) {
	return Math.floor((dura%(1000*60*60))/(1000*60));
}
function time_dura_second(dura) {
	return Math.floor((dura%(1000*60))/(1000));
}


function book() {
	if (currentSale ==null) {
		alert("请等待页面加载完成");
		return;
	}
	
	if (currentSale.seq==null) {
		alert("请通过审批后才能预约");
		return;
	}
	
	if ($("#btn_book").hasClass("disabled")) return;
	
	if($("#agreement").attr("checked")!="checked") {
		alert("请勾选阅读并同意 预约及购买规则");
		return;
	}
	var id = currentSale.seq;
	
	
	$("#btn_book").addClass("disabled");
	$("#btn_book").html("正在预约");
	$.post("/service/book", {
		"id": id
	}, function(data) {
		var book =$.parseJSON(data);
		$("#btn_book").html("已预约");
		$("#book_code").html("预约号 " + book.o);
	}).fail(function() {
		$("#btn_book").html("预约");
		$("#btn_book").removeClass("disabled");
		alert("当前服务器出错，请重试");
	});
}

function loginDialog() {
	$("#dialog").show();
}

function closeDialog() {
	$("#dialog").hide();
}

function formate_time(time) {
	var d = new Date(time);
	return d.getFullYear() + "-" + (d.getMonth()+1) + "-" + d.getDate() + " " + d.getHours() + ":" + ((d.getMinutes()<10)?("0" + d.getMinutes()): d.getMinutes()); 
}

function query_string(key) {
	return (document.location.search.match(new RegExp("(?:^\\?|&)"+key+"=(.*?)(?=&|$)"))||['',null])[1];
}
