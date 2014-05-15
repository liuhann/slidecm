
$(document).ready(function() {
	$("#sales-list").show();
	getSalelist();
});


function frmRequest() {
	$(".view").hide();
	$("#posters").show();
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
	
	$(".view").hide();
	$("#pendding").show();
	
	$.post("/service/seller/request ", o, function() {
		$("#requesting .msg").html("提交成功");
	});
}

function getSalelist() {
	$.getJSON("/service/seller/list", {},
		function(data) {
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
				if (sale.online) {
					if (d>now) {
						status = "上线，用户预约中";
					} else {
						status = "抢购完成";
					}
				} else {
					if (d>now) {
						status = "申请上线中";
					} else {
						status = "已失效";
					}
				}
				cloned.find(".status").html(status);
				
				cloned.data("sale", sale);
				cloned.find("a.modify").click(function() {
					var sale = $(this).pd("sale");
					$(".view").hide();
					
					$("#posters").show();
					$("#posters").data("sale", sale);
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

				
				$("#sales-list table").append(cloned);
			}
	});
}



function remove(id) {
}

function modify(id) {
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


