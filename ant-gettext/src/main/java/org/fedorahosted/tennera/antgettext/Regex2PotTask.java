/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
   
   public Regex2PotTask() {
       // this may later be overridden by the ant attribute "format"
       setFormat("java-format");
   }

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
   
   Integer[] readByLines(BufferedReader reader, StringBuilder contents) throws IOException 
   {
	String line;
	List<Integer> lineStartList = new ArrayList<Integer>();
	{
	    int lineNo = 0;
	    int charNum = 0;
	    while ((line = reader.readLine()) != null)
	    {
		++lineNo;
		contents.append(line);
		contents.append('\n');
		lineStartList.add(charNum);
		charNum += line.length()+1; // plus 1 for the newline
	    }
	}
	Integer[] lineStarts = lineStartList.toArray(new Integer[0]);
	return lineStarts;
   }

   @Override
   protected void processFile(String filename, File f) throws IOException 
   {
       BufferedReader reader = new BufferedReader(new FileReader(f));

       StringBuilder contents = new StringBuilder();
       Integer[] lineStarts = readByLines(reader, contents);
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
		       recordMatch(filename, key, String.valueOf(lineNo));
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
