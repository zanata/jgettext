package org.camouflage.format.po;

import java.util.StringTokenizer;

public abstract class POHeaderUtil{
	
	public static void setLastTranslator(POEntry e, String translator){
		
	}

	public static void removeLastTranslator(POEntry e){
		
	}
	
	public static void setPlurals(POEntry e, int nplurals, String expression){
		
	}

	public static void clearField(POEntry e, String field){
		
	}
	
	public static void setField(POEntry e, String field, String content){
		
	}
	
	public static String getField(POEntry e, String field){
		StringTokenizer tok = new StringTokenizer(e.getMsgStr(), "\n");
		while(tok.hasMoreTokens()){
			String token = tok.nextToken();
			if(token.startsWith(field)){
				token = token.substring(field.length()+2);
				return token; 
			}
		}
		return null;
		
	}
	
	public static String getContentTypeCharset(POEntry e){
		return getField(e,FIELD_CONTENT_TYPE);
	}
	public static String getContentType(POEntry e){
		return null;
	}
	
	public static String getCharset(POEntry e){
		String content = getContentTypeCharset(e);
		if(content == null) return null;
		int index = content.indexOf("charset=");
		if(index != -1){
			content = content.substring(index+8).trim();
			return content;
		}
		return null;
	}
	
	public static final String FIELD_CONTENT_TYPE = "Content-Type"; 
}
