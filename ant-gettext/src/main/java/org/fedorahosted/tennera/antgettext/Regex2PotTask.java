/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.jboss.jgettext.Catalog;
import org.jboss.jgettext.Message;
import org.jboss.jgettext.Occurence;
import org.jboss.jgettext.catalog.write.CatalogWriter;

/**
 * Extracts strings which match a regular expression into a gettext template file (POT).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class Regex2PotTask extends MatchingTask
{
   private static final String DEFAULT_REGEX = null;

   private BufferedWriter out;

   /**
    * @see #setRegex(String)
    */
   private String regex = DEFAULT_REGEX;

   private File srcDir;

   private File target;

   /**
    * @see #setUnescape(boolean)
    */
   private boolean unescape = true;

   private String pathPrefix;
   
   /**
    * Maps an English key to a set of matching source locations
    */
   private Map<String, Set<String>> mapKeyToLocationSet = new TreeMap<String, Set<String>>();


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

   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
   }

   public void setTarget(File target)
   {
      this.target = target;
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
   
   public void setPathPrefix(String prefix)
   {
      this.pathPrefix = prefix;
   }
   
   @Override
   public void execute() throws BuildException
   {
      DirUtil.checkDir(srcDir, "srcDir", false);
//      if (target == null)
//      {
//         throw new BuildException("target attribute must be set!");
//      }
      if (target != null && target.exists() && !target.isFile())
      {
         throw new BuildException("target exists but is not a file!");
      }

      try
      {
         if(target == null)
         {
            log("Extracting English strings from '" +srcDir+ "' to STDOUT");
            out = new BufferedWriter(new OutputStreamWriter(System.out));
         }
         else
         {
            log("Extracting English strings from '" +srcDir+ "' to '"+target+"'");
            out = new BufferedWriter(new FileWriter(target));
         }
         DirectoryScanner ds = super.getDirectoryScanner(srcDir);
         String[] files = ds.getIncludedFiles();

         for (int i = 0; i < files.length; i++)
         {
            processFileByConcatenatedLines(files[i]);
         }
         generatePot(out);
         out.close();
      }
      catch (IOException e)
      {
         throw new BuildException(e);
      }
   }

   /**
    * Scans a single file for regex matches, writing them to 'out'.  
    * @param filename
    * @throws IOException
    * @throws BuildException
    */
   private void processFileByConcatenatedLines(String filename) throws IOException, BuildException
   {
      log("processing " + filename, Project.MSG_VERBOSE);
      Pattern pat = Pattern.compile(regex);

      File f = new File(srcDir, filename);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      String line;
      List<Integer> lineStartList = new ArrayList<Integer>();
      StringBuilder contents = new StringBuilder();
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

   /**
    * Finds the (1-based) number of the line containing a character, 
    * specified by its character index from the beginning of the file.
    * @param lineStarts
    * @param charNo
    * @return
    */
   private static int findLineNumber(Integer[] lineStarts, int charNo)
   {
      // Find the largest index in the list less than or equal to charNo, and add 1.  
      int position = Arrays.binarySearch(lineStarts, charNo);
      if (position >= 0)
         return position + 1;
      else
         return -position - 1;
   }

   
   private void recordMatch(String filename, String key, int lineNo)
   {
      Set<String> set = mapKeyToLocationSet.get(key);
      if(set == null)
      {
         set = new TreeSet<String>();
         mapKeyToLocationSet.put(key, set);
      }
      set.add(filename+":"+lineNo); //$NON-NLS-1$
   }

   protected void generatePot(BufferedWriter out) throws IOException
   {
       Catalog cat = new Catalog(true);
       CatalogWriter writer = new CatalogWriter(cat);
       for (Map.Entry<String, Set<String>> mapEntry : mapKeyToLocationSet.entrySet())
       {
	   Message message = new Message();
//	   message.addExtractedComment(null);
	   message.addOccurence(new Occurence(locationSetToString(mapEntry.getValue())));
	   message.addFormat("java-format"); // FIXME check this
//	   message.setMsgctxt(null);
	   message.setMsgid(mapEntry.getKey());
	   cat.addMessage(message);
       }
       writer.writeTo(out);
       
//      PotWritingUtil.writePotHeader(out);
//      for (Map.Entry<String, Set<String>> mapEntry : mapKeyToLocationSet.entrySet())
//      {
//         PotWritingUtil.writePotEntry(
//               out, null, locationSetToString(mapEntry.getValue()), true, null, mapEntry.getKey());
       /*
           BufferedWriter out, 
           String extractedComment, 
           String locationReference, 
           boolean isJavaFormat, 
           String context, 
           String key) throws IOException
        */
//      }
   }
   
   private String locationSetToString(Set<String> locationSet)
   {
      StringBuilder sb = new StringBuilder();
//      sb.append("#:"); //$NON-NLS-1$
      for (String location : locationSet)
      {
         sb.append(pathPrefix);
         sb.append(location);
         // TODO don't use a space after last element
         sb.append(' ');
      }
      return sb.toString();
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
