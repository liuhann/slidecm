var cu = null;

var datePickerOptions = {
		lang: "ch",
		format: "Y-m-d H:i"
};

$(document).ready(function() {
	$.getJSON("/service/seller/info", {}, function(data) {
		cu = data;
		$("span.cu").html(cu.name);
		getRequesting();
		addImageWidge('btn-preview-upload', 'img-preview', 'posters');
		addImageWidge('repost-btn-preview-upload', 'repost-img-preview', 'present-edit');
		
		$("#time").datetimepicker(datePickerOptions);
		$("#until").datetimepicker(datePickerOptions);
		$("#repost-time").datetimepicker(datePickerOptions);
	}).fail(function() {
		
		//location.href = "/seller/login.html";
	});
});

function frmRequest() {
	$(".view").hide();
	$("#posters").show();
	initForm(null);
}

function frmRepost() {
	$(".view").hide();
	$("#present-edit").show();
}

function requestPresent() {
	pendding();
	
	$.post("/service/seller/present/add", {
		"title": $("#present-title").val(),
		"url": $("#present-url").val(),
		"per" :  $("#present-per").val(),
		"total": $("#present-total").val(),
		"desc": $("#present-desc").val(),
		"preview" : $("#present-edit").data("preview")
	}, function(data) {
		penddingOut();
	}).fail(function() {
		alert("未知错误");
		penddingOut();
	});
}

function getPresents() {
	pendding();
	$.getJSON("/service/seller/present/my", {},function(list){
		penddingOut();
		navOn("my-presents-list");
		
		$("#my-presents-list tr.item").remove();
		for ( var i = 0; i < list.length; i++) {
			var cloned = $("#my-presents-list table tr.template").clone();
			var present = list[i];
			cloned.removeClass("template").addClass("item");
			cloned.data("present", present);
			
			//cloned.find(".title").html(present.title);
			if (present.total>0) {
				cloned.find(".method").html("共赠送" + present.total + "个");	
			} else {
				cloned.find(".method").html("每" + present.per + "个转发送1个");
			}
			cloned.find(".desc").html(present.desc);
			
			cloned.find("a.remove").click(function() {
				var p = $(this).pd("present");
				$.post("/service/seller/present/remove", {
					"id": p._id
				}, function() {
					getPresents();
				});
			});
			
			$("#my-presents-list table").append(cloned);
		}
		
	});
}

function request() {
	if ($("#btn_request").hasClass("disabled")) {
		return;
	}
	$("span.msg.error").removeClass("error").html("");
	//首先进行有效性检查
	var o = {
			"title": encodeURI($("#title").val()),
			"subtitle": encodeURI($("#subtitle").val()),
			"count": $("#count").val(),
			"price": $("#price").val(),
			"oprice": $("#oprice").val(), 
			"url": encodeURI($("#sell-url").val()),
			"time": $("#time").val(),
			"until": $("#until").val(),
			"content": encodeURI(um.getContent())
	};
	
	var check = true;
	if (o.title=="") {
		$("#title").next("span.msg").html("标题不能为空").addClass("error");
		check = false;
	}
	if (!isPrice(o.count)) {
		$("#count").next("span.msg").html("数量必须为正整数").addClass("error");
		check = false;
	}
	if (!isPrice(o.price)) {
		$("#price").next("span.msg").html("价格必须为正整数").addClass("error");
		check = false;
	}
	if (!isPrice(o.oprice)) {
		$("#oprice").next("span.msg").html("原价必须为正整数").addClass("error");
		check = false;
	}
	if(!isUrl(o.url)) {
		$("#sell-url").next("span.msg").html("必须给出销售地址").addClass("error");
		check = false;
	}
	
	if (!check) return;
	
	if ($("#posters").data("sale")!=null) {
		o.id = $("#posters").data("sale")._id;
	}
	
	if ($("#posters").data("preview")!=null) {
		o.preview = $("#posters").data("preview");
	}
	
	//$(".view").hide();
	//$("#pendding").show();
	$("#btn_request").addClass("disabled");
	$.post("/service/seller/request ", o, function() {
		$("#btn_request").removeClass("disabled");
		getRequesting();
	}).fail(function() {
		$("#btn_request").removeClass("disabled");
	});
}

function getRequesting() {
	navOn("requesting");
	$("#sales-list").show();
	
	$("#sales-list tr.sale").remove();
	$.getJSON("/service/seller/request/list", {},
		function(data) {
			for ( var i = 0; i < data.length; i++) {
				var sale = data[i];
				var cloned = initSaleTR(sale);
				$("#sales-list table").append(cloned);
			}
			$("#c_qiang").html(data.length);
	});
}

function getOnlines() {
	navOn("online");
	$("#sales-list").show();
	
	$("#sales-list tr.sale").remove();
	$.getJSON("/service/seller/online/list", {},
		function(data) {
			for ( var i = 0; i < data.length; i++) {
				var sale = data[i];
				var cloned = initSaleTR(sale);
				$("#sales-list table").append(cloned);
			}
	});
}

function getFinished() {
	navOn("finished");
	$("#sales-list").show();
	
	$("#sales-list tr.sale").remove();
	$.getJSON("/service/seller/finished/list", {},
		function(data) {
			for ( var i = 0; i < data.length; i++) {
				var sale = data[i];
				var cloned = initSaleTR(sale);
				$("#sales-list table").append(cloned);
			}
	});
}

function initSaleTR(sale) {
	var cloned = $("#sales-list tr.template").clone();
	cloned.removeClass("template");
	cloned.addClass("sale");
	cloned.find(".name").html(sale.title);
	
	var d = new Date(sale.time);
	
	cloned.find(".time").html(formateTime(sale.time));
	cloned.find(".until").html(formateTime(sale.until));
	cloned.find(".price").html(sale.price);
	
	var status = "";
	var now = new Date();
	cloned.find("td.action a").hide();
	if (sale.online) {
		if (d>now) {
			status = "上线，接受预约中";
			cloned.find(".status").css("color", "green");
			cloned.find("td.action a.preview").show();
		} else {
			status = "抢购完成";
			cloned.find("td.action a.preview").show();
			cloned.find("td.action a.deals").show();
			cloned.find("td.action a.request").show();
		}
		cloned.find(".books").html(sale.books);
	} else {
		status = "申请上线中";
		cloned.find(".status").css("color", "#CC0000");
		cloned.find("td.action a.preview").show();
		cloned.find("td.action a.modify").show();
		cloned.find("td.action a.remove").show();
		cloned.find("a.modify").click(function() {
			var sale = $(this).pd("sale");
			initForm(sale);
		});
		cloned.find("a.remove").click(function() {
			var sale = $(this).pd("sale");
			if (!sale.online && confirm("确认删除抢购活动？ 删除后无法恢复！")) {
				$.post("/service/seller/sale/drop", {
					"id": sale._id
				}, function() {
					getRequesting();
				});
			}
		});
	}
	
	cloned.find(".status").html(status);
	cloned.data("sale", sale);
	
	cloned.find("a.deals").click(function() {
		var sale = $(this).pd("sale");
		window.open("deals.html?id=" + sale.seq);
	});
	
	cloned.find("a.preview").click(function() {
		var sale = $(this).pd("sale");
		if (sale.seq!=null) {
			window.open("/book.html?id="  + sale.seq);
		} else {
			window.open("/book.html?oid="  + sale._id);
		}
	});
	
	cloned.find("a.books").click(function() {
		var sale = $(this).pd("sale");
		window.open("books.html?id="  + sale.seq);
		return;
	});
	
	cloned.find("a.request").click(function() {
		var sale = $(this).pd("sale");
		$(".view").hide();
		$("#posters").show();
		sale._id=null;
		initForm(sale);
	});
	return cloned;
}

function config() {
	navOn("seller-config");
	$("#shopurl").val(cu.shop);
	$("#contact").val(cu.user);
	$("#mobile").val(cu.phone);
	$("#email").val(cu.email);
	$("#imcontact").val(cu.other);
}

function configcfm() {
	if ($("#btn-config-modify").hasClass("diabled"))  return;
	$("#btn-config-modify").addClass("disabled").html("提交中");
	$.post("/service/seller/info", {
		"name": cu.name,
		"shop": $("#shopurl").val(),
		"user": $("#contact").val(),
		"phone": $("#mobile").val(),
		"email" : $("#email").val(),
		"other": $("#imcontact").val()
	}, function(data) {
		alert("信息修改成功");
		$("#btn-config-modify").removeClass("disabled").html("确认修改");
		cu = JSON.parse(data);
	});
}

function modifypwd() {
	navOn("password-modify");
	$("#oldpwd").val("");
	$("#modifypwd").val("");
	$("#cfmmodifypwd").val("");
}

function passwordcfm() {
	if ($("#btn-config-pwd").hasClass("diabled"))  return;
	
	var pwd = $("#modifypwd").val();
	var cfmpwd = $("#cfmmodifypwd").val();
	
	if (pwd=="") {
		alert("密码不能为空"); return;
	}
	if (pwd!=cfmpwd) {
		alert("2次密码输入不一致"); return;
	}
	$("#btn-config-pwd").addClass("disabled").html("提交中");
	
	$.post("/service/seller/password", {
		"old": $("#oldpwd").val(),
		"new": pwd
	}, function(data) {
		if (data=="1") {
			alert("密码修改成功");
		} else {
			alert("密码修改失败");
		}
		$("#btn-config-pwd").removeClass("disabled").html("确认修改");
	});
}

function initForm(sale) {
	$(".view").hide();
	$("#posters").show();
	$("#posters").data("preview", null);
	$('#img-preview img').remove();
	$('#img-preview').html("");
	$("#posters").data("sale", null);

	if (sale!=null) {
		if (sale._id!=null) {
			$("#posters").data("sale", sale);
		}
		if (sale.preview) {
			 var img = $("<img src='/service/preview?id=" + sale.preview + "'>");
			 $('#img-preview').append(img);
			 $("#posters").data("preview", sale.preview);
		}
		
		$("#title").val(sale.title);
		$("#posters #subtitle").val(sale.subtitle);
		$("#posters #count").val(sale.count);
		$("#posters #price").val(sale.price);
		$("#posters #time").val(formateTime(sale.time));
		$("#posters #until").val(formateTime(sale.until));
		$("#posters #sell-url").val(sale.url);
		
		$.get("/service/seller/sale/content", {
			"id": sale.content
		}, function(data) {
			um.setContent(data);
		});
	} else {
		$("#title").val("");
		$("#posters #subtitle").val("");
		$("#posters #count").val("");
		$("#posters #price").val("");
		$("#posters #time").val("");
		$("#posters #until").val("");
		$("#posters #sell-url").val("");
		um.setContent("");
	}
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

function initUploader() {
	var imgUploader = new plupload.Uploader({
		runtimes : 'html5',
		browse_button : 'btn-preview-upload',
		max_file_size : '1mb',
		multi_selection: false,
		filters : [
		            {title : "图片文件", extensions : "jpg,png"},
		        ],
		url : '/service/preview/attach'
	});
	
	imgUploader.init();
	imgUploader.bind("FilesAdded", function(up, files) {
		$("#img-preview").html("图片上传中..");
		up.start();
	});
	
	imgUploader.bind('FileUploaded', function(up, file, tn) {
		$('#img-preview img').remove();
		 $('#img-preview').html("");
		 var img = $("<img src='/service/preview?id=" + tn.response + "'>");
		 $("#posters").data("preview", tn.response);
		 $('#img-preview').append(img);
	});
	
	imgUploader.bind('Error', function(up, err) {  // on file upload error
		if (err.code==-600) {
			$("#img-preview").html("图片大小不能超过1M");
		} else {
			$("#img-preview").html("上传失败，请重新选择");
		}
		$("#img-preview").css("color", "red");
	 });
}

function addImageWidge(btn, preview, idstores) {
	var imgUploader = new plupload.Uploader({
		runtimes : 'html5',
		browse_button : btn,
		max_file_size : '1mb',
		multi_selection: false,
		filters : [
		            {title : "图片文件", extensions : "jpg,png"},
		        ],
		url : '/service/preview/attach'
	});
	
	imgUploader.init();
	imgUploader.bind("FilesAdded", function(up, files) {
		$("#" + preview ).html("图片上传中..");
		up.start();
	});
	
	imgUploader.bind('FileUploaded', function(up, file, tn) {
		$('#' + preview + ' img').remove();
		 $('#' + preview).html("");
		 var img = $("<img src='/service/preview?id=" + tn.response + "'>");
		 $('#' + preview).append(img);
		 $("#" + idstores).data("preview", tn.response);
	});
	
	imgUploader.bind('Error', function(up, err) {  // on file upload error
		if (err.code==-600) {
			$("#" + preview ).html("图片大小不能超过1M");
		} else {
			$("#" + preview).html("上传失败，请试试重新上传");
		}
		$("#" + preview).css("color", "red");
	 });
}

function previewUrlChange() {
	if ($("#preview-url").val()!="" && isUrl($("#preview-url").val())) {
		$('#img-preview img').remove();
		$('#img-preview').html("");
		var img = $("<img src='" + $("#preview-url").val() + "'>");
		$('#img-preview').append(img);
	}
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

function isPrice(v) {
	var p = parseFloat(v);
	if (isNaN(p)) {
		return false;
	} 
	if (p<0) return false;
	return true;
}

function isUrl(v) {
	return (v.indexOf("http://")==0);
}

