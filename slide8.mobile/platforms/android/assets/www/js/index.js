
var slide8Host = 'http://slide8.duapp.com';
var wh = 0;
var ww = 0;
var currentView = "splash";


$(document).ready(function() {
	document.addEventListener("deviceready", onDeviceReady, false);
	document.addEventListener("backbutton", onBackPressed, false);
});

function onBackPressed() {
	if (currentView=="slide") {
		currentView="index";
		window.plugins.orientationLock.lock("portrait");
		window.addEventListener("orientationchange", function() {
			$(".index").show();
			$(".slider").hide();
		});
	} else {
		navigator.app.exitApp();
	}
}

function onDeviceReady() {
	window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);

	console.log("deviceready  ");
	uiInit();
	console.log("UI Init OK  ");
	if ( navigator.connection.type ==Connection.NONE) {
		
	} else {
		loadIndexData();
	}
}

function uiInit() {
	wh = Math.floor($(window).height());
	ww = Math.floor($(window).width());
	$(".wrapper").css("width", ww);
	//$(".wrapper").css("height", wh);
	
	$(".loading").hide();
	$(".config").hide();
	
	var basicHeight = wh/20;
	
	/*
	$(".index h1.title").css("height", basicHeight);
	$(".index h1.title img.logo").css("margin-top", -(80-basicHeight)/2);
	
	$(".index h1").css("height", basicHeight);
	$(".index h1").css("line-height", (basicHeight-2) + "px");
	$("h1").css("font-size", basicHeight-20);
	$("body").css("font-size", basicHeight-25);
	*/
	
	$(".splash img").css("width", ww);
	$(".splash img").css("height", ww*3/4);
}

function checkConnection() {
    var networkState = navigator.connection.type;

    var states = {};
    states[Connection.UNKNOWN]  = 'Unknown connection';
    states[Connection.ETHERNET] = 'Ethernet connection';
    states[Connection.WIFI]     = 'WiFi connection';
    states[Connection.CELL_2G]  = 'Cell 2G connection';
    states[Connection.CELL_3G]  = 'Cell 3G connection';
    states[Connection.CELL_4G]  = 'Cell 4G connection';
    states[Connection.CELL]     = 'Cell generic connection';
    states[Connection.NONE]     = 'No network connection';
    return states[networkState];
}

function loadLocalData() {
	$(".index").show();
	$(".index .splash").hide();
}


function loadIndexData() {
	console.log("Loadding index Data.....");
	
	$.getJSON(slide8Host + "/pub/list/all", {}, function(indexData) {
		$(".index").show();
		initIndex(indexData);
	}).fail(function() {
		console.log("Fail Loadding index Data!!");
	});
}

var currentOpening = null;
function initIndex(indexData) {
	console.log("indexData Loaded");
	/*
	$(".article img.small").css("width",ww/2 );
	$(".article img.small").css("height",ww*3/8 );
	*/
	
	if (indexData.splash.length>0) {
		$(".index .splash").show();
		var article = indexData.splash[0];
		var meta  = extractArticleMeta(article);
		$(".index .splash .article").data("article", article);
		$(".index .splash .article")
		$(".index .splash .article-title").html(getFileName(article.name));
		$(".index .splash").find("span.pages").html(meta.p + "页");
		var splashImg = $(".splash .article-image");
		
		loadImg(splashImg, article.name, article.tn, 0);
		
		$(".splash .article").attachEvent(function(t) {
			var article = $(t).data("article");
			currentOpening = article;
			loadFullImg(article);
		});
	}
	if (indexData.recent.length>0) {
		for ( var i = 0; i < indexData.recent.length; i++) {
			var div = initArticle(indexData.recent[i]);
			$(".recommend").append(div);
		}
	}
}

function fullFileDownloaded(article, fileEntry) {
	if (currentOpening==article) {
		console.log("打开展示文件 " + article.name);
		window.plugins.orientationLock.lock("landscape");
		window.addEventListener("orientationchange", function() {
			var meta = extractArticleMeta(currentOpening);
			openSlide(fileEntry.toURL(), meta.w, meta.h, meta.p);
		});
	}
}

function initArticle(article) {
	var div = $("div.article.template").clone();
	
	div.data("article", article);
	div.attr("id", "atc-" + article.id);
	loadImg(div.find("img.small"), article.name, article.tn, 0);
	
	div.attachEvent(function(t) {
		var article = $(this).data("article");
	});
	
	var meta  = extractArticleMeta(article);
	
	var imageWidth = Math.floor((ww-20)/2);
	var imageHeight = Math.floor(imageWidth * 3/4);
	div.show();
	div.removeClass("template");
	div.css("width", imageWidth);
	div.css("height", imageHeight);
	div.find(".article-title").html(getFileName(article.name));
	div.find("span.pages").html(meta.p + "页");
	
	
	div.find("img.small").css("width",imageWidth);
	div.find("img.small").css("height",imageHeight);
	
	
	div.attachEvent(function(t) {
		var article = $(t).data("article");
		currentOpening = article;
		loadFullImg(article);
	});
	return div;
}

function extractArticleMeta(article) {
	var p = 0;
	var w = 0;
	var h = 0;
	for ( var i = 0; i < article.tags.length; i++) {
		if (article.tags[i].indexOf("高度")==0) {
			h = parseInt(article.tags[i].substring(2));
			continue;
		}
		if (article.tags[i].indexOf("宽度")==0) {
			w = parseInt(article.tags[i].substring(2));
			continue;
		}
		if (article.tags[i].indexOf("p")==0) {
			p = parseInt(article.tags[i].substring(1));
			continue;
		}
	}
	
	return {
		"p": p,
		"w": w,
		"h": h
	};
}
function openSlide(fileUrl, w, h, p) {
	
	currentView = "slide";
	wh = $(window).height();
	ww =$(window).width();
	console.log("screen : " + screen.width + "x" + screen.height + "   window:" + ww + "x" + wh);
	
	if (wh>ww) {  //执行时屏幕可能并没有切换到横屏。  因此先按横屏方式处理
		if(ww==screen.width) {
			var barHeight = screen.height - wh;
			ww -= barHeight;
			wh = screen.height;
		}
		var temp = wh;
		wh = ww;
		ww = temp;
	}
	
	console.log("打开展示 " + fileUrl + "  w=" + w + "  h= " + h + "  p=" + p);
	$(".index").hide();
	$(".slider").show();
	
	$(".slider .operations").show();

	$(".slider .next, .slider .prev").css("height", wh/2);
	$(".slider .next, .slider .prev").css("width", ww);
	$(".slider .next, .slider .prev").css("left", 0);
	$(".slider .next").css("bottom", 0);
	$(".slider .prev").css("top", 0);
	
	$(".slider").data("meta", {
		width: w,
		height: h,
		pic:p
	});
	$(".slider").data("slideUrl", fileUrl);
	page=0;
	slideNext();
	$(".slider .next").attachEvent(function() {
		/*
		if (parseInt($(".slider .next").css("opacity"))!=0) {
			$(".slider .next,.slider .prev").css("opacity", 0);
			return;
		}*/
		if (page>p) return;
		page++;
		slideNext(0.5);
	});
	
	$(".slider .prev").attachEvent(function() {/*
		if (parseInt($(".slider .next").css("opacity"))!=0) {
			$(".slider .next,.slider .prev").css("opacity", 0);
			return;
		}*/
		if (page<=0) return; 
		page--;
		slideNext(2);
	});
}

function slideNext(scaleOrigin) {
	//$(".slider .rub").remove();
	var pageDiv = $(".slider .backend");
	var meta = $(".slider").data("meta");
	var img = $(".slider").data("slideUrl");
	
	$(".slider .operations").html((page+1) + "/" + $(".slider").data("meta").pic);
	if (wh>ww) {
		var iw = ww;
		var ih = Math.floor(ww/meta.width * meta.height);
		pageDiv.css("top", (wh-ih)/2);
		pageDiv.css("left", 0);
	} else {
		var ih = wh;
		var iw = Math.floor(ih/meta.height * meta.width);
		pageDiv.css("top", "0px");
		pageDiv.css("left", (ww-iw)/2);
		if (iw > ww) {
			iw = ww;
			ih = Math.floor(ww/meta.width * meta.height);
			pageDiv.css("top", (wh-ih)/2);
			pageDiv.css("left", 0);
		}
	}
	pageDiv.css("width", iw);
	pageDiv.css("height", ih);
	pageDiv.css("background-size" , iw + "px " + ih * meta.pic + "px" );
	pageDiv.css("background-image", "url(" + img + ")");
	pageDiv.css("opacity", "0");
	pageDiv.css("-webkit-transition", "translate3d(0,0,0)");
	pageDiv.css("position", "absolute");
	pageDiv.css("background-position",  "0px -" + page * ih + "px");
	
	$(".slider .front").transition({opacity:0}).removeClass("front").addClass("backend");
	pageDiv.css("scale",scaleOrigin).transition({opacity:1, scale:1}).addClass("front").removeClass("backend");
}

/***
 * 本地文件操作相关处理
 */
function loadImg(img, name, uid, size) {
	console.log("loadding img  " + name + "   " + uid);
	img.attr("name", name);
	if (slide8Dir!=null) {
		slide8Dir.getDirectory("thumb", {create: true, exclusive: false}, function(thumbDirEntry) {
			//进入thumb文件夹
			thumbDirEntry.getFile(name, {create: true, exclusive: false},  function(thumbFile) {
				//获取图片文件
				thumbFile.file(function(fileObj) {
					console.log("get File Info : " + fileObj.name +  "  " + fileObj.size + "   " + fileObj.type);
					if (fileObj.size>0) { //图片文件比较大 认为已经下载过了
						console.log("using exist thumb file url: " + thumbFile.toURL());
						//$("img[name='" +name + "']").attr("src", thumbFile.toURL());
						
						$("img[name='" +name + "']").attr("src", thumbFile.toNativeURL());
					} else {
						console.log("transfer thumb from server  ");
						var ft = new FileTransfer();
						ft.download(
								"http://slide8.duapp.com/pub/file/image?id=" + uid,
								thumbFile.toURL(),
								function(entry) {
									console.log("File transfered " + entry.toURL());
									$("img[name='" +name + "']").attr("src", entry.toURL());
								},
								function(error) {
									console.log("File transfered  Error " + error.source + "  " + error.target);
									$("img[name='" +name + "']").attr("src", "img/thumbnail.png");
								},
								true,
								{}
						);
					}
				});
			});
		});
	}
}

var downloadList = [];

function loadFullImg(article) {
	console.log("loadding Full Image  " + article.name);
	
	if (slide8Dir!=null) {
		slide8Dir.getFile(article.name, {create:true, exclusive: false}, function(imgFile) {
			imgFile.file(function(fileObj) {
				console.log("get File Info : " + fileObj.name +  "  " + fileObj.size + "   " + fileObj.type);
				if (fileObj.size==article.size) {
					console.log("文件在SD卡已存在: " + article.name);
					fullFileDownloaded(article, imgFile);
				} else {
					console.log("请求下载: " + article.name);
					var ft = new FileTransfer();
					
					ft.onprogress = function(progressEvent) {
					    if (progressEvent.lengthComputable) {
					    	$("#atc-" + article.id).find("span.status").html(formatFileSize(progressEvent.loaded)  + " /"  +  formatFileSize(progressEvent.total));
					    	console.log("下载进度： " + formatFileSize(progressEvent.loaded)  + " /"  +  formatFileSize(progressEvent.total));
					    } else {
					    	$("#atc-" + article.id).find("span.status").html("下载中");
					    }
					};
					
					ft.download("http://slide8.duapp.com/pub/file/download?id=" + article.id,
							imgFile.toURL(),
							function(entry) {
								console.log("文件下载完成： " + entry.toURL());
								fullFileDownloaded(article,entry);
							}, 
							function(entry) {
								console.log("文件下载异常： " + error.source + "  " + error.target);
							},
							true,
							{}
					)
				}
				
			});
		});
	} else {
		
	}
}

function loadSlideImg() {
	
}

var slide8Dir = null;
function gotFS(fileSystem) {
	fileSystem.root.getDirectory("slide8", {create: true, exclusive: false}, function(dirEntry) {
		slide8Dir = dirEntry;
	}, fail);
}

function inSlide8Entry(dirEntry) {
	console.log("in slide8 folder " + dirEntry.fullPath);

	dirEntry.getFile("wulala.png", {create: true, exclusive: false},  function(file) {
		var fileTransfer = new FileTransfer();
		console.log("file got: "+ file.toURL());
		fileTransfer.download(
				"http://slide8.duapp.com/pub/file/download?id=53560946e4b04a81bf9c8ae4",
				file.toURL(),
				function(entry) {
					console.log("download complete: " + entry.fullPath);
					openSlide(entry.toURL(), 802, 660, 10);
				},
				function(error) {
					console.log("download error source " + error.source);
					console.log("download error target " + error.target);
					console.log("download error code " + error.code);
				},
				true,
				{}
		);
	}, fail);
	/*
	dirEntry.getFile("呜啦啦.png",  {create: true, exclusive: false}, function(pngFile) {
		
	}, fail);
	*/
}

function fail(error) {
	alert("fail  " + error.code);
    console.log(error.code);
}

function getFileName (name) {
	var pos = name.lastIndexOf(".");
	if(pos>-1) {
		return name.substring(0,pos);
	} else {
		return name;
	}
}

function formatFileSize(n) {
	if (n > 1073741824) {
		return Math.round(n / 1073741824, 1) + " G";
	}
	if (n > 1048576) {
		return Math.round(n / 1048576, 1) + " M";
	}
	if (n > 1024) {
		return Math.round(n / 1024, 1) + " K";
	}
	return n + "B";
}

var isMobile =/Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);
var isTouchDevice = isMobile;

$.fn.attachEvent = function(cb) {
	attachEvent($(this), cb);
};

function attachEvent(src, cb) {
	$(src).unbind();
	if (isTouchDevice) {
		$(src).bind("touchstart", function() {
			$(this).addClass("pressed");
		});
		$(src).bind("touchend",  function() {
			$(this).removeClass("pressed");
			//$(this).data("moved", false);
			if($(this).data("moved")!=true) {
				cb($(this));
			} else {
				$(this).data("moved", false);
			}
		});
		$(src).on('touchmove',function (e){
			$(this).data("moved", true);
			$(this).removeClass("pressed");
		});
	} else {
		$(src).bind("mousedown", function() {
			$(this).addClass("pressed");
		});
		
		$(src).bind("mouseup", function() {
			$(this).removeClass("pressed");
			//$(this).siblings().removeClass("current");
			//$(this).addClass("current");
			cb($(this));
		});
	}
}