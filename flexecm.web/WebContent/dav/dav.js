var cu = null;

$(document).ready(function() {
	home();
});

function home() {
	$("#presents .waterfall").remove();
	
	$.getJSON("/service/present/list", {
	}, function(list) {
		for ( var i = 0; i < list.length; i++) {
			var present = list[i];
			var cloned = initWaterFall(present);
			$("#presents").append(cloned);
		}
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

function hidePost() {
	$("#postweibo").hide();
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
	cloned.find(".reqire.fans span").html(formatNumber(present.fans));
	
	cloned.find(".method").hide();
	if (present.total>0) {
		cloned.find(".method.total").show();
	} else {
		cloned.find(".method.per").show();
	}
	
	cloned.find(".bg").css("background-image","url('/service/preview?id=" + present.preview + "')");
	
	if (present.status==1) {
		cloned.find("a.btnrepost").click(function() {
			
			var present = $(this).pd("present");
			$("#presents").hide();
			$("#postweibo").show();
			var text = "福利来了，" + present.desc + "，马上转发此微博即有机会获得，" 
					+ ((present.total==0)?( "每" + present.per +"个转发者赠送一个"):("一共" + present.total + "个" )) + 
			",第三方平台抽取，点此地址进行转发：http://shengo.duapp.com/dav/i.html?" + present.seq;
			$("#weibo-text").html(text);
			checktext(text);
			$("#postweibo").data("present", present);
			$("#postweibo div.pic").css("background-image","url('/service/preview?id=" + present.preview + "')");
			
			$("#weibo-text").change(function() {
				var text = $("#weibo-text").val();
				checktext(text);
			});
			
			function checktext(text) {
				var remains = 140-text.length;
				if (remains>0) {
					$("#weibo-text-remains").html("还可输入" + remains + "字");
				} else {
					$("#weibo-text-remains").html("已超过" + (-remains) + "字");
				}
			}
		});
	} else if (present.status==2) {
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

function formatNumber(n) {
	if (n>10000) {
		return Math.floor(n/10000) + "万";
	} else {
		return n;
	}
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
