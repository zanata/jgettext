/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.jgettext.catalog.util;

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
	s = s.replace("\\r", "\r");
	s = s.replace("\\n", "\n");
	return s = s.replace("\\\"", "\"");
    }
    
	public static String quote(String s) {
    	if (s == null)
    		return "null";
    	else
    		return '\"'+s+'\"';
    }
}
