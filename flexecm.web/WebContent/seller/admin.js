$(document).ready(function() {
	
	$.getJSON("/service/seller/info", {}, function(data) {
		if (data.name!="admin") {
			location.href = "/index.html";
			return;
		}
		getRequesting();
	
	}).fail(function() {
		location.href = "/index.html";
	});
});


function getRequesting() {
	$("#sales-list").show();
	$("#sales-list table td.sale").remove();
	$.getJSON("/service/seller/admin/request/list", {}, function(data) {
		
		for ( var i = 0; i < data.length; i++) {
			var sale = data[i];
			var cloned = $("#sales-list tr.template").clone();
			cloned.removeClass("template");
			cloned.addClass("sale");
			cloned.find(".name").html(sale.title);
			
			cloned.find(".time").html(formateTime(sale.time));
			if (sale.until!="") {
				cloned.find(".until").html(formateTime(sale.until));	
			}
			cloned.find(".price").html(sale.price);
			cloned.find(".seller").html(sale.seller);
			cloned.find(".count").html(sale.count);
			cloned.data("sale", sale);
			cloned.find("a.preview").click(function() {
				var sale = $(this).pd("sale");
				window.open("/book.html?oid="  + sale._id);
			});
			
			cloned.find("a.approve").click(function() {
				var sale = $(this).pd("sale");
				$.post("/service/seller/admin/request/approve", {
					"id" : sale._id,
					"on": "1"
				}, function () {
					getRequesting();
				});
				window.open("/book.html?oid="  + sale._id);
			});
			$("#sales-list table").append(cloned);
		}
	});
}

function getOnlines() {
	$("#sales-list").show();
	$("#sales-list table td.sale").remove();
	$.getJSON("/service/seller/admin/online/list", {}, function(data) {
		
		for ( var i = 0; i < data.length; i++) {
			var sale = data[i];
			var cloned =initCloned(sale);
			cloned.find("a.preview").click(function() {
				var sale = $(this).pd("sale");
				window.open("/book.html?oid="  + sale._id);
			});
		
			$("#sales-list table").append(cloned);
		}
	});
	
}

function initCloned(sale) {
	var cloned =  $("#sales-list tr.template").clone();
	cloned.removeClass("template");
	cloned.addClass("sale");
	cloned.find(".name").html(sale.title);
	
	cloned.find(".time").html(formateTime(sale.time));
	if (sale.until!="") {
		cloned.find(".until").html(formateTime(sale.until));	
	}
	cloned.find(".price").html(sale.price);
	cloned.find(".seller").html(sale.seller);
	cloned.find(".count").html(sale.count);
	cloned.data("sale", sale);
		
	return cloned;
}


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


