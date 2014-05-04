package com.ever365.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HTMLParser {

	public static final String FROM = "from";
	public static final String SIZE = "size";
	public static final String TITLE = "title";


	public static void main(String[] args) {
		Map<String, Object> m = parseBaiduPan("http://pan.baidu.com/s/1eQghKAI");
		System.out.println(m.get(TITLE) + "   " + m.get(SIZE));
	}
	
	public static Map<String, Object> parse115(String url) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Document doc = Jsoup.connect(url).get();
			Elements p = doc.select("div.spree-info");
			
			result.put(TITLE, p.select("span").attr(TITLE));
			
			result.put(SIZE, StringUtils.tofileSize(
					StringUtils.middle(p.select("em").html(),"：", "B")
					));
			result.put(FROM, "115");
			return result;
		} catch (IOException e) {
		} catch (Exception e) {
		}
		return result;
	}
	
	public static Map<String, Object> parseBaiduPan(String url) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Document doc = Jsoup.connect(url).get();
			result.put(TITLE, doc.select("h2.b-fl").attr(TITLE));
			result.put(SIZE, StringUtils.tofileSize(
					StringUtils.middle(doc.select("#downFileButtom b").last().html(),"(", ")")
					));
			result.put(FROM, "baidu");
		} catch (IOException e) {
		} catch (Exception e) {
		}
		return result;
	}
	
	
}
