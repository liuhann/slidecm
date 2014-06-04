
$(document).ready(function() {
	getSalelist();
	initUploader();
});

function frmRequest() {
	$(".view").hide();
	$("#posters").show();
	initForm(null);
}

function request() {
	//首先进行有效性检查
	var o = {
			"title": $("#title").val(),
			"subtitle": $("#subtitle").val(),
			"count": $("#count").val(),
			"price": $("#price").val(),
			"time": $("#time").val(),
			"until": $("#until").val(),
			"content": encodeURI(um.getContent())
	};
	
	if ($("#posters").data("sale")!=null) {
		o.id = $("#posters").data("sale")._id;
	}
	
	if ($("#posters").data("preview")!=null) {
		o.preview = $("#posters").data("preview");
	}
	
	$(".view").hide();
	$("#pendding").show();
	
	$.post("/service/seller/request ", o, function() {
		getSalelist();
	});
}

function getSalelist() {
	$("div.content div.view").hide();
	$("#sales-list").show();
	navOn("list");
	
	$("#sales-list tr.sale").remove();
	$.getJSON("/service/seller/list", {},
		function(data) {
			var onlines = 0;
			var offline = 0;
			var finished = 0;
		
			for ( var i = 0; i < data.length; i++) {
				var sale = data[i];
				var cloned = $("#sales-list tr.template").clone();
				cloned.removeClass("template");
				cloned.addClass("sale");
				cloned.find(".name").html(sale.title);
				
				var d = new Date(sale.time);
				
				cloned.find(".time").html(formateTime(d.getTime()));
				cloned.find(".price").html(sale.price);
				
				var status = "";
				var now = new Date();
				cloned.find("td.action a").hide();
				if (sale.online) {
					if (d>now) {
						status = "上线，接受预约中";
						cloned.find(".status").css("color", "green");
						cloned.find("td.action a.preview").show();
						cloned.find("td.action a.books").show();
						onlines ++;
					} else {
						status = "抢购完成";
						finished ++;
					}
				} else {
					if (d>now) {
						status = "申请上线中";
						offline ++;
						cloned.find(".status").css("color", "#CC0000");
						cloned.find("td.action a.preview").show();
						cloned.find("td.action a.modify").show();
						cloned.find("td.action a.remove").show();
					} else {
						status = "已失效";
					}
				}
				cloned.find(".status").html(status);
				
				cloned.data("sale", sale);
				cloned.find("a.modify").click(function() {
					var sale = $(this).pd("sale");
					initForm(sale);
				});
				
				cloned.find("a.remove").click(function() {
					var sale = $(this).pd("sale");
					if (!sale.online) {
						$.post("/service/seller/drop", {
							"_id": sale._id
						}, function() {
							getSalelist();
						});
					}
				});
				
				cloned.find("a.preview").click(function() {
					var sale = $(this).pd("sale");
					window.open("/preview.jsp?id="  + sale._id);
				});
				
				$("#sales-list table").append(cloned);
			}
			$("#c_qiang").html(onlines);
	});
}

function initForm(sale) {
	
	$(".view").hide();
	$("#posters").show();
	$("#posters").data("sale", sale);
	$("#posters").data("preview", null);
	$('#img-preview img').remove();
	$('#img-preview').html("");
	
	if (sale!=null) {
		if (sale.preview) {
			 var img = $("<img src='/service/preview?id=" + sale.preview + "'>");
			 $('#img-preview').append(img);
		}
		
		$("#title").val(sale.title);
		$("#posters #subtitle").val(sale.subtitle);
		$("#posters #count").val(sale.count);
		$("#posters #price").val(sale.price);
		$("#posters #time").val(formateTime(sale.time));
		$("#posters #until").val(sale.until);
	
		$.get("/service/content", {
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
		um.setContent("");
	}
}


function remove(id) {
}

function modify(id) {
}

function navOn(cl) {
	$(".nav li.link").removeClass("on");
	$(".nav li." + cl).addClass("on");
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


