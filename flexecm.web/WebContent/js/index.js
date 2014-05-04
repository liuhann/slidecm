var moreI = 10;

$(document).ready(function() {
	$(".login-wrapper").hover(function() {
		$(".nav-dropdown").slideDown('fast');
	}, function() {
		$(".nav-dropdown").slideUp('fast');
	});
	
	if (person) {
		userLogon(person);
	}
	$(".recommend .view .article-title").hide();
	
	$(".recommend .view").hover(function() {
		$(this).find(".article-title").fadeIn("fast");
	}, function() {
		$(this).find(".article-title").fadeOut("fast");
	});

	$(".time-formated").each(function() {
		$(this).html(Utils.formatTime(parseInt($(this).html())));
	});
	
	$(".pager div.article").hover(function() {
		$(this).find("img").css("margin-top", "-40px");
	}, function() {
		$(this).find("img").css("margin-top", "0px");
	});
	
	var mainWidth = parseInt($(".main").css("width"));
	
	$(".pager .article").css("width",  mainWidth/3-10);
	if (mainWidth/3-10>320) {
		$(".pager .article img.article-image").css("width",  mainWidth/3-10);
	}
	viewPageInit();
});

function viewPageInit() {
	
}


function more() {
	$.getJSON("/pub/list/more", {
		type:encodeURI("最新资源"),
		skip: moreI,
		limit: 10
	}, function(data) {
		
		if (data.length==0) {
			$(".article.more").remove();
			return;
		}
		moreI += 10;
		for ( var i = 0; i < data.length; i++) {
			var ppt = data[i];
			var article = $(".article.template").clone();
			article.removeClass("template");
			
			article.find("a.seq").css("href", "/view.jsp?seq=" + ppt.seq);
			article.find("img.article-image").css("src", "/pub/file/image?id=" + ppt.tn);
			article.find("h1.article-title").html(Utils.getFileName(ppt.name));
			article.find(".publisher").html(ppt.creator);
			article.find(".time-formated").html(Utils.formatTime(ppt.modified));
			
			$(".article.more").before(article);
			article.show();
		}
		
		var mainWidth = parseInt($(".main").css("width"));
		
		$(".pager .article").css("width",  mainWidth/3-10);
		if (mainWidth/3-10>320) {
			$(".pager .article img.article-image").css("width",  mainWidth/3-10);
		}
		
	});
}


function myHome() {
	if (person) {
		location.href = "/web/home.html";
	} else {
		loginDialog();
	}
}

function closeDialog() {
	$("#modal").hide();
}

function loginDialog() {
	$("#modal").show();
	$("#login").show();
	$("#register").hide();
}

function registerDialog() {
	$("#modal").show();
	$("#login").hide();
	$("#register").show();
}

function login() {
	if ($("#user_login").val()=="") {
		$("#logon-result").html("用户名不能为空"); return;
	}
	if ($("#user_pass").val()=="") {
		$("#logon-result").html("密码不能为空"); return;
	}
	
	$.post("/login", {
		'name': $("#user_login").val(),
		'password': $("#user_pass").val()
	},  function() {
		person = $("#user_login").val();
		userLogon(person);
		closeDialog();
	}).fail( function() {
		$("#logon-result").html("用户名或密码错误");
	});
}


function register() {
	var user = $("#reg_user").val();
	var password = $("#reg_user_pass").val();
	var cfm = $("#reg_user_pass_cfm").val();
	var email = $("#reg_email").val();
	
	if (user.length<6) {
		
	}
}

function logout() {
	location.href = "/logout";
}


function userLogon(name) {
	$(".nav-dropdown .guest").hide();
	$(".nav-dropdown .logon").show();
	$(".login-wrapper .status span").html(name);
}

function isEmail(str){
	var reg = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/;
	return reg.test(str);
}


function onUpper() {
	var o = $(".slideview").data("opened");
	if (o==null) {
		closeCover();
	} else {
		if (o==0) return;
		o --;
		$(".slideview").data("opened", o);
		$(".slideview .img").css("background-position-y" , -parseInt($(".slideview .img").css("height")) * o);
	}
}

function onDowner() {
	var o = $(".slideview").data("opened");
	if (o==null) {
		closeCover();
	} else {
		if (o>=	$(".slideview").data("page")-1) return;
		o ++;
		$(".slideview").data("opened", o);
		$(".slideview .img").css("background-position-y" , -parseInt($(".slideview .img").css("height")) * o);
	}
}

function closeCover() {
	var width = 800;
	var height = 600;
	var page = 19999; 
	if (ja!=null) {
		for ( var i = 0; i < ja.length; i++) {
			if (ja[i].indexOf("宽度")==0) {
				width = parseInt(ja[i].substring(2));
			}
			if (ja[i].indexOf("高度")==0) {
				height = parseInt(ja[i].substring(2));
			}
			if (ja[i].indexOf("p")==0) {
				page = parseInt(ja[i].substring(1));
			}
		}
	}
	
	$(".slideview").css("width", width);
	$(".slideview .img").css("width", width);
	$(".slideview .img").css("height", height);
	
	$(".slideview").data("opened", 0);
	$(".slideview").data("page", page);
	
	$(".slideview .upper,.slideview .downer").css("background", "transparent");
	$(".slideview .upper,.slideview .downer").html("");
	$(".slideview .upper,.slideview .downer").css("border-bottom", "none");
	$(".slideview .upper,.slideview .downer").css("border-top", "none");
}