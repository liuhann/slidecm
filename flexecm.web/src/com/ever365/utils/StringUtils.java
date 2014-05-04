package com.ever365.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class StringUtils {

	public static final Long tofileSize(String size) {
		try {
			if (size.endsWith("M")||size.endsWith("m")) {
				Float f = new Float(size.substring(0, size.length()-1)) * 1024 * 1024;
				return f.longValue();
			} else if (size.endsWith("K") || size.endsWith("k")) {
				Float f = new Float(size.substring(0, size.length()-1)) * 1024;
				return f.longValue();
			} else if (size.endsWith("G") || size.endsWith("g")) {
				Float f = new Float(size.substring(0, size.length()-1)) * 1024 * 1024 * 1024;
				return f.longValue();
			} else if (size.endsWith("B") || size.endsWith("b")) {
				Float f = new Float(size.substring(0, size.length()-1));
				return f.longValue();
			} else {
				return new Long(size);
			}
		} catch (Exception e) {
			return 0L;
		}
	}
	
	public static DecimalFormat df2  = new DecimalFormat("###.00"); 
	public static final String formateFileSize(Long size) {
		if (size > 1073741824) {
			return df2.format(size/1073741824) + "G"; 
		}
		if (size > 1048576) {
			return df2.format(size/1048576)  + " M";
		}
		if (size > 1024) {
			return df2.format(size/1024) + " K";
		}
		return size + "B";
	}
	
	/*
	SimpleDateFormat sFormat = new SimpleDateFormat("yyyy年M月dd日");
	public static final String formateTime(Long time) {
		if (size > 1073741824) {
			return df2.format(size/1073741824) + "G"; 
		}
		if (size > 1048576) {
			return df2.format(size/1048576)  + " M";
		}
		if (size > 1024) {
			return df2.format(size/1024) + " K";
		}
		return size + "B";
	}
	*/
	
	public static final String getFileName(String file) {
		if (file==null) return "";
		int dot = file.lastIndexOf(".");
		if (dot>-1) {
			return file.substring(0,dot);
		} else {
			return file;
		}
	}
	
	public static final String middle(String source, String start, String end) {
		int posx = source.indexOf(start);
		
		if (posx>-1) {
			posx += start.length();
		}
		
		int posy = source.indexOf(end, posx);
		
		if (posy==-1) {
			posy = source.length();
		}
		return source.substring(posx, posy);
	}
	
}
