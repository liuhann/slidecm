var cu = null;

$(document).ready(function() {
	current();
});

function current() {
	var num = parseInt(query_string(location.href));
	
	if (!isNaN(num)) {
		$.getJSON("/service/dav/i", {
			"i": num
		}, function(present) {
			cu = present.cu;
			var cloned  = $("#present");
		
			initWaterfall(cloned, present);
			
			for ( var i = 0; i < present.weibos.length; i++) {
				var other = present.weibos[i];
				
				var cloned = $("#recommend .template").clone().removeClass("template");
				cloned.find(".postmeta a").html(other.dav);
				cloned.find(".message").html(other.msg);
				cloned.find(".time").html(humTime(other.time));
				
				cloned.find("a.btnrepost").attr("href", "/dav/i.html?" + other.seq);
				
				$("#recommend ul").append(cloned);
			}
		});
	}
}

function initWaterfall(cloned, present) {
	cloned.removeClass("template").addClass("waterfall");
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
	
	if (present.code!=null) {
		cloned.find(".rcode .hint").html(present.code);
		cloned.find(".rcode").show();
		cloned.find(".repost").hide();
	}
	cloned.find(".bg").css("background","url('/service/preview?id=" + present.preview + "')");
}

function repost() {
	
	var num = parseInt(query_string(location.href));
	if (cu==null) {
		//首先预订 
		$.post("/service/dav/repost/book", {  
			"i": num
		}, function() {
			//标记跳回到本页 
			$.post("/oauth/weibo", {
				"redirect": location.href
			}, function() {
				//前往认证 
				location.href = "https://api.weibo.com/oauth2/authorize?client_id=2444138001&response_type=code&redirect_uri=http://shengo.duapp.com/oauth/weibo";
			});
		});
	} else {
		$.post("/service/dav/repost", {
			"i": num
		}, function(data) {
			var repost = JSON.parse(data);
			$("#present .rcode .hint").html(repost.code);
			$("#present .rcode").show();
			$("#present .repost").hide();
		});
	}
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

function humTime(millsec) {
	var date = new Date(millsec),
		diff = (((new Date()).getTime() - date.getTime()) / 1000),
		day_diff = Math.floor(diff / 86400);
			
	if ( isNaN(day_diff) || day_diff < 0 || day_diff >= 31 )
		return;
	if (day_diff>=31) {
		return date.getFullYear() + "年" + (date.getMonth()+1) + "月" + date.getDate() + "日";
	}
	return day_diff == 0 && (
			//diff < 60 && "刚刚" ||
			diff < 120 && "1 分钟前" ||
			diff < 3600 && Math.floor( diff / 60 ) + " 分钟前" ||
			diff < 7200 && "1 小时前" ||
			diff < 86400 && Math.floor( diff / 3600 ) + " 小时前") ||
		day_diff == 1 && ("昨天" + date.getHours() + ":" + (date.getMinutes()<10?("0"+date.getMinutes()):date.getMinutes())) ||
		day_diff < 7 && day_diff + " 天前" ||
		day_diff < 31 && Math.ceil( day_diff / 7 ) + " 周前";
}


function formateTime(d) {
	var t = new Date(d);
	return t.getFullYear() + "-" + (t.getMonth()+1) + "-" + t.getDate() + "  " + t.getHours() + ":" + ((t.getMinutes()<10)? ("0"+t.getMinutes()):t.getMinutes());
}

function query_string(url) {
	if (url.indexOf("?")>-1) {
		return url.substring(url.indexOf("?")+1); 
	} else {
		return "";
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
