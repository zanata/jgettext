/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;

/**
 * Extracts strings which match a regular expression into a gettext template file (POT).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class Regex2PotTask extends MatchExtractingTask
{
   private static final String DEFAULT_REGEX = null;

   /**
    * @see #setRegex(String)
    */
   private String regex = DEFAULT_REGEX;

   /**
    * @see #setUnescape(boolean)
    */
   private boolean unescape = true;

   /**
    * The regex should include one or more capture groups (in parentheses).  
    * Any matching groups will be extracted as POT keys.
    * 
    * @param regex
    */
   public void setRegex(String regex)
   {
      this.regex = regex;
   }

   /**
    * Whether to process Java-style escapes in the source files.
    * 
    * @param unescape
    */
   public void setUnescape(boolean unescape)
   {
      this.unescape = unescape;
   }
   
   void recordMatches(String filename, CharSequence contents, Integer[] lineStarts) 
   {
       Pattern pat = Pattern.compile(regex);
       Matcher matcher = pat.matcher(contents);

       while (matcher.find())
       {
	   try
	   {
	       for (int i=1; i<=matcher.groupCount(); i++)
	       {
		   String capture = matcher.group(i);
		   if(capture != null)
		   {
		       String key = unescapeJava(capture);
		       int charNo = matcher.start(i);
		       int lineNo = findLineNumber(lineStarts, charNo);
		       recordMatch(filename, key, lineNo);
		   }
	       }
	   }
	   catch (IndexOutOfBoundsException e)
	   {
	       throw new BuildException(e);
	   }
       }
   }

   private String unescapeJava(String key)
   {
      if (unescape)
      {
         // backslash-X is replaced by X.
         // thus backslash-backslash is replaced by backslash.
         // Note: we have to escape backslashes once for Java's parser, and again
         // for the regex parser.
         return key.replaceAll("\\\\(.)", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      else
      {
         return key;
      }
   }

}
