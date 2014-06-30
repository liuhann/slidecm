var cu = null;

$(document).ready(function() {
	home();
});

function home() {
	navOn("presents");
	$("#presents .waterfall").remove();
	
	$.getJSON("/service/dav/info", {}, function(data) {
		$(".view").hide();
		$("#presents").show();
		cu = data;
		$("span.cu").html(cu.name);
		
		$.getJSON("/service/present/list", {
		}, function(list) {
			for ( var i = 0; i < list.length; i++) {
				var present = list[i];
				var cloned = initWaterFall(present);
				$("#presents").append(cloned);
			}
		});
	}).fail(function() {
		$(".view").hide();
		$("#invite").show();
	});
}

function postWeibo() {
	var p = $("#postweibo").data("present");
	$.post("/service/dav/post", {
		"id": p._id,
		"msg": $("#weibo-text").val()
	}, function() {
		getReposts();
	});
}


function getReposts() {
	navOn("reposts");
	$("#presents .waterfall").remove();
	$("#presents").show();
	$.getJSON("/service/dav/mypost", {},function(list) {
		for ( var i = 0; i < list.length; i++) {
			var present = list[i];
			var cloned = initWaterFall(present);
			$("#presents").append(cloned);
		}
	});
}

function initWaterFall(present) {
	var cloned = $("#presents .template").clone().removeClass("template").addClass("waterfall");
	cloned.data("present", present);
	cloned.find(".desc").html(present.desc);
	cloned.find(".from .seller").html(present.seller);
	
	cloned.find(".method.per span").html(present.per);
	cloned.find(".method.total span").html(present.total);
	cloned.find(".method").hide();
	if (present.total>0) {
		cloned.find(".method.total").show();
	} else {
		cloned.find(".method.per").show();
	}
	
	cloned.find(".bg").css("background","url('/service/preview?id=" + present.preview + "')");
	
	if (present.status==0) {
		cloned.find("a.btnrepost").click(function() {
			var present = $(this).pd("present");
			$("#postweibo").show();
			$("#weibo-text").html("福利来了，" + present.desc + "，马上转发，将有可能获得哦。赠送数量：" 
					+ ((present.total==0)?( "每" + present.per +"个转发者赠送一个"):(present.total )) + 
			"点击第三方平台转发有效哦 http://www.ever365.com/dav/i.html?" + present.seq);
			$("#postweibo").data("present", present);
			$("#postweibo div.pic").css("background","url('/service/preview?id=" + present.preview + "')");
		});
	} else if (present.status==1) {
		
		cloned.find("a.btnrepost").html("查看");
		
		cloned.find("a.btnrepost").click(function() {
			location.href = "/dav/i.html?" + present.seq;
		});
	}
	
	return cloned;
}

function davAdd() {
	$.post("/service/dav/add", {
		"code" : $("#invite-code").val()
	}, function() {
		home();
	});
}

function navOn(cl) {
	$(".view").hide();
	$("#" + cl).show();
	$(".nav li.link").removeClass("on");
	$(".nav li[for='" + cl + "']").addClass("on");
	$(".nav li." + cl).addClass("on");
}

function pendding() {
	$(".view").hide();
	$("#pendding").show();
}

function penddingOut() {
	$("#pendding").hide();
}

/***
 * 通用函数 
 */

function formateTime(d) {
	var t = new Date(d);
	return t.getFullYear() + "-" + (t.getMonth()+1) + "-" + t.getDate() + "  " + t.getHours() + ":" + ((t.getMinutes()<10)? ("0"+t.getMinutes()):t.getMinutes());
}

$.fn.pd = function(name) {
	var current = $(this);
	var data = current.data(name);
	while(current!=null && data==null) {
		current = current.parent();
		data = current.data(name);
	}
	return data;
};
