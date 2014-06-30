var cu = null;

$(document).ready(function() {
	$.getJSON("/service/shengo/index", {}, 
			function(index) {
				$("#pendding").fadeOut('fast');
				if (index.cu) {
					$("#site-map div.item.login").hide();
					$("#site-map div.item.user").show();
					$("#site-map div.item.user .uname").html(index.cu).show();
					cu = index.cu;
				} else {
					$("#site-map div.item.login").show();
					$("#site-map div.item.user").hide();
				}
				
				$("#site-map div.item.seller").show();
				
				/*
				for(var i=0; i<index.ads.length; i++) {
					var sale = index.ads[i];
					
					var cloned = $("a.ads.template").clone().removeClass("template");
					
					cloned.find(".title").html(sale.title);
					cloned.find(".price").html("¥ " + sale.price);
					cloned.find(".time").html(formate_time(sale.time));
					
					cloned.find(".img img").attr("src", "/service/preview?id=" + sale.preview);
					
					$("#main .left").append(cloned);
				}
				*/
				for(var i=0; i<index.recents.length; i++) {
					var sale = index.recents[i];
					
					var cloned = $("#timeline li.item.template").clone().removeClass("template");
					cloned.find("div.bg").css("background-image", "url('/service/preview?id=" + sale.preview + "')");
					
					//cloned.find("img.bg").attr("src", "/service/preview?id=" + sale.preview);
					
					cloned.find(".time").html(humanity_time(sale.time));
					cloned.find(".title").html(sale.title);
					cloned.find(".price").html("¥" + sale.price);
					cloned.find("a.go").attr("href", "/book.html?id=" + sale.seq);
					
					cloned.hover(function() {
						$(this).find("div.bg").css("background-position", "0px -100px");
					}, function() {
						$(this).find("div.bg").css("background-position", "0px 0px");
					});
					
					$("#timeline").append(cloned);
				}
				
				
				for(var i=0; i<index.weibo.length; i++) {
					var cloned = $("#weipost .template").clone();
					var present = index.weibo[i];
					initWaterfall(cloned, present);
					$("#weipost .list").append(cloned);
				}
			}
	);

	
	$("#books-container").hoverDelay({
				hoverDuring: 300,
				outDuring: 500,
				hoverEvent: function(){
					$("#books-container").find("dd").show().addClass("hovered");
					if($("#books-container").data("list")==null) {
						$.getJSON("/service/books", {}, function(list) {
							$("#books-container").data("list", list);
							var total = 0;
							for ( var i = 0; i < list.length; i++) {
								var book  = list[i];
								if (book.sale.time<new Date().getTime()) continue;
								total ++;
								var cloned = $("#books-container li.sale.template").clone();
								cloned.removeClass("template");
								cloned.find(".title a").html(book.sale.title);
								cloned.find(".price").html(book.sale.price);
								cloned.find(".title a").attr("href", "/book.html?id=" + book.m);
								cloned.find(".time").html(humanity_time(book.sale.time));
								$("#books-container div.list ul").append(cloned);
							}
							
							$("#books-container .total").html(total);
						});
					}
				},
		    outEvent: function(){
		    	$($("#books-container")).find("dd").hide();
		    }
	});
	
	$("#deals-container").hoverDelay({
		hoverDuring: 300,
		outDuring: 500,
		hoverEvent: function(){
			$("#deals-container").find("dd").show();
			if($("#deals-container").data("list")==null) {
				$.getJSON("/service/deals", {}, function(list) {
					$("#deals-container").data("list", list);
					for ( var i = 0; i < list.length; i++) {
						var book  = list[i];
						var cloned = $("#deals-container li.sale.template").clone();
						cloned.removeClass("template");
						cloned.find(".title a").html(book.sale.title);
						cloned.find(".price").html(book.sale.price);
						cloned.find(".title a").attr("href", "/book.html?id=" + book.m);
						cloned.find(".time").html(book.order);
						$("#deals-container div.list ul").append(cloned);
					}
					$("#deals-container .total").html(list.length);
				});
			}
		},
	    outEvent: function(){
	    	$($("#deals-container")).find("dd").hide();
	    }
	});
});



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
	cloned.find("a.go").attr("href", "/dav/i.html?" + present.seq);
}


function formate_time(time) {
	var d = new Date(time);
	return d.getFullYear() + "-" + (d.getMonth()+1) + "-" + d.getDate() + " " + d.getHours() + ":" + ((d.getMinutes()<10)?("0" + d.getMinutes()): d.getMinutes()); 
}


function humanity_time(millsec) {
	var date = new Date(millsec),
	diff = ((date.getTime() - (new Date()).getTime()) / 1000),
	day_diff = Math.floor(diff / 86400),
	today = (date.getDate()== (new Date()).getDate());
	
	if (diff<0) {
		return "进行中";
	}  else  if (day_diff==0) {
		if (today) {
			if (diff<60*60) {
				return Math.floor(diff/60) + "分钟后";
			}
			if (diff<8*60*60) {
				return Math.floor(diff/(60*60)) + "小时后";
			}
			return "今天" + date.getHours() + ":" + ((date.getMinutes()<10)? ("0" + date.getMinutes()) : date.getMinutes());
		} else {
			return "明天" + date.getHours() + ":" + ((date.getMinutes()<10)? ("0" + date.getMinutes()) : date.getMinutes());
		}
	} else  {
		return (date.getMonth()+1) + "月" + date.getDate() + " 日" + date.getHours() + ":" + ((date.getMinutes()<10)?("0" + date.getMinutes()): date.getMinutes());
	} 
}



function loginDialog() {
	$("#dialog").show();
}

function closeDialog() {
	$("#dialog").hide();
}


function sellerDialog() {
	if (cu==null || cu.indexOf("@")>-1) {
		location.href = "/seller/login.html";
	} else {
		location.href = "/seller/index.html";
	}
}

(function($){
    $.fn.hoverDelay = function(options){
        var defaults = {
            hoverDuring: 500,
            outDuring: 500,
            hoverEvent: function(){
                $.noop();
            },
            outEvent: function(){
                $.noop();
            }
        };
        var sets = $.extend(defaults,options || {});
        var hoverTimer, outTimer;
        return $(this).each(function(){
            $(this).hover(function(){
                clearTimeout(outTimer);
                hoverTimer = setTimeout(sets.hoverEvent, sets.hoverDuring);
            },function(){
                clearTimeout(hoverTimer);
                outTimer = setTimeout(sets.outEvent, sets.outDuring);
            });
        });
    }
})(jQuery);
