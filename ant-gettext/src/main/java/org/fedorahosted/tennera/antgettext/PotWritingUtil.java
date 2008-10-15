/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class PotWritingUtil
{

   public static void writePotHeader(BufferedWriter out) throws IOException
   {
      out.write("# SOME DESCRIPTIVE TITLE.");out.newLine(); //$NON-NLS-1$
      out.write("# Copyright (C) YEAR THE PACKAGE'S COPYRIGHT HOLDER");out.newLine(); //$NON-NLS-1$
      out.write("# This file is distributed under the same license as the PACKAGE package.");out.newLine(); //$NON-NLS-1$
      out.write("# FIRST AUTHOR <EMAIL@ADDRESS>, YEAR.");out.newLine(); //$NON-NLS-1$
      out.write("#");out.newLine(); //$NON-NLS-1$
   
      out.write("msgid \"\"");out.newLine(); //$NON-NLS-1$
      out.write("msgstr \"\"");out.newLine(); //$NON-NLS-1$
      out.write("\"Project-Id-Version: PACKAGE VERSION\\n\"");out.newLine(); //$NON-NLS-1$
      out.write("\"Report-Msgid-Bugs-To: \\n\"");out.newLine(); //$NON-NLS-1$
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ"); //$NON-NLS-1$
      out.write("\"POT-Creation-Date: "+dateFormat.format(new Date())+"\\n\"");out.newLine(); //$NON-NLS-1$ //$NON-NLS-2$
      out.write("\"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n\"");out.newLine(); //$NON-NLS-1$
      out.write("\"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n\"");out.newLine(); //$NON-NLS-1$
      out.write("\"Language-Team: LANGUAGE <LL@li.org>\\n\"");out.newLine(); //$NON-NLS-1$
      out.write("\"MIME-Version: 1.0\\n\"");out.newLine(); //$NON-NLS-1$
      out.write("\"Content-Type: text/plain; charset=UTF-8\\n\"");out.newLine(); //$NON-NLS-1$
      out.write("\"Content-Transfer-Encoding: 8bit\\n\"");out.newLine();  //$NON-NLS-1$
   }

   /**
    * Outputs a single key to 'out' in POT format.
    * @param extractedComment TODO
    * 
    */
   public static void writePotEntry(
	   BufferedWriter out, 
	   String extractedComment, 
	   String locationReference, 
	   boolean isJavaFormat, 
	   String context, 
	   String key) throws IOException
   {
       if (extractedComment != null && extractedComment.length() != 0)
       {
	   String[] lines = extractedComment.split("\n");
	   for (String line : lines) {
	       out.write("#. ");
	       out.write(line);
	       out.newLine();
	   }
       }

       if (locationReference != null)
       {
	   out.write("#: ");
	   out.write(locationReference);
	   out.newLine();
       }

       if (isJavaFormat)
       {
	   out.write("#, java-format"); //$NON-NLS-1$
	   out.newLine();
       }

       if (context != null)
       {
	   out.write("msgctxt \""); //$NON-NLS-1$
	   out.write(escapePot(context));
	   out.write('"');
	   out.newLine();
       }

       out.write("msgid \""); //$NON-NLS-1$
       out.write(escapePot(key));
       out.write('"');
       out.newLine();

       out.write("msgstr \"\""); //$NON-NLS-1$
       out.newLine();
       out.newLine();
   }

   private static String escapePot(String key)
   {
      boolean multiline = false;
      StringBuilder sb = new StringBuilder(key.length());
      for(int i=0; i<key.length(); i++)
      {
         char ch = key.charAt(i);
         switch(ch)
         {
            // escape backslashes, quotes and newlines with '\'
            case '\\':
               // FIXME shouldn't this be sb.append("\\\\") ? 
               sb.append("\\"); //$NON-NLS-1$
               break;
            case '\"':
               sb.append("\\\""); //$NON-NLS-1$
               break;
            case '\t':
               sb.append("\\t"); //$NON-NLS-1$
               break;
            case '\n':
               sb.append("\\n\"\n\""); //$NON-NLS-1$
               multiline = true;
               break;
            default:
               sb.append(ch);
               break;
         }
      }
      if(multiline)
      {
         sb.insert(0, "\"\n\""); //$NON-NLS-1$
      }
      return sb.toString();
   }

}
