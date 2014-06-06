var cu = null;

$(document).ready(function() {
	var id = query_string("id");
	var oid = query_string("oid");
	if (id==null && oid==null) {
		location.href = "/error/404.html";
		return;
	}
	$.getJSON("/service/sale", {
		"id": id,
		"oid":oid
	}, function(sale) {
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
		if (sale.o) {
			$("#btn_book").addClass("disabled");
			$("#btn_book").html("已预约");
			$("#book_code").html("预约号 " + sale.o);
		}
	});
});

function book() {
	if ($("#btn_book").hasClass("disabled")) return;
	var id = query_string("id");
	
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
