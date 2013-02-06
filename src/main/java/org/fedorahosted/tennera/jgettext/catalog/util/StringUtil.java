/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.jgettext.catalog.util;

import org.fedorahosted.tennera.jgettext.catalog.parse.UnexpectedTokenException;

@SuppressWarnings("nls")
public class StringUtil {
    private StringUtil() {
    }
    
    public static String addEscapes(String s) {
	s = s.replace("\r", "\\r");
	s = s.replace("\n", "\\n");
	return s = s.replace("\"", "\\\"");
    }

    public static String removeEscapes(String s) {
    	StringBuilder result = new StringBuilder();
    	
    	if(s.length() == 1){
    		// if it's a single character in the stream, only the backslash
    		// can cause problems
    		if (s.charAt(0) != '\\')
    			return s;
    		throw new UnexpectedTokenException("Unexpected token '\\' ",-1);
    	}
    	
    	char [] chars = s.toCharArray();

    	
    	for(int i =1;i<chars.length;i++) {
    		
    		char prev = chars[i-1];
    		char current = chars[i];
    		
    		if(prev == '\\') {
    			switch(current){
    			case '\\':
    				result.append('\\');
    				chars[i] = ' '; // to avoid double quoting
    				break;
    			case 'r':
    				result.append('\r');
    				break;
    			case 'n':
    				result.append('\n');
    				break;
    			case 't':
    				result.append('\t');
    				break;
    			case '\"':
    				result.append('\"');
    				break;
    			default:
    				throw new UnexpectedTokenException("Invalid escape sequence: " + prev + current, -1);
    			}
    		}
    		else{ // prev is not '\\'
    			if(i== 1) {
    				result.append(prev);
    			}
    			if(current != '\\'){
        			result.append(current);
    			}
    		}
    	}
    	return result.toString();
    }
    
	public static String quote(String s) {
    	if (s == null)
    		return "null";
    	else
    		return '\"'+s+'\"';
    }
}
