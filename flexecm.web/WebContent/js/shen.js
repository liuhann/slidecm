var cu = null;

$(document).ready(function() {
	$.get("/service/person/current", null, function(data) {
		if (data!="") {
			$("#site-map div.login").hide();
			$("#site-map div.user").html(data).show();
			cu = data;
		} else {
			$("#site-map div.login").show();
			$("#site-map div.user").hide();
		}
	});
});

function loginDialog() {
	$("#dialog").show();
}

function closeDialog() {
	$("#dialog").hide();
}


function sellerDialog() {
	if (cu==null) {
		alert("请先登陆");
	} else {
		location.href = "/seller/index.html";
	}
}