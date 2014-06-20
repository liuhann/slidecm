package com.ever365.utils;


public class UUID {
	public final static String generate() {
		return java.util.UUID.randomUUID().toString();
	}
	/*
	genPass: function() {
		var x="123456789poiuytrewqasdfghjklmnbvcxzQWERTYUIPLKJHGFDSAZXCVBNM";
	 	var tmp="";
	 	var ran = Math.random();
	 	for(var i=0;i<10;i++) {
	 		ran *=10;
			tmp += x.charAt(Math.ceil(ran)%x.length);
	 	}
	 	return tmp;
	}
	
	*/
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",  
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",  
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",  
        "6", "7", "8", "9", "a", "b", "c", "d", "e", "f",  
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",  
        "t", "u", "v", "w", "x", "y", "z"};  

	
	public static String generateShortUuid() {  
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = generate().replace("-", "");
		for (int i = 0; i < 3; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		shortBuffer.append(new Double(Math.random()*1000000).intValue());
		return shortBuffer.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(UUID.generateShortUuid());
	}
}
