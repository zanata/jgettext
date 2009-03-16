package org.fedorahosted.tennera.antgettext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.util.FileNameMapper;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.Occurence;
import org.fedorahosted.tennera.jgettext.catalog.write.CatalogWriter;

/**
 * Converts Java Properties files into gettext files (PO).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public abstract class Prop2GettextTask extends MatchingTask {
	File srcDir;
	File dstDir;
	FileNameMapper mapper;
	boolean includeAll;
	EmptyStringPolicy emptyStringPolicy = EmptyStringPolicy.WARNANDSKIP;

	// This should be pretty safe, at least for ISO-8859-1 files
	static final String NEWLINE_REGEX = "(\r\n|\r|\n)"; //$NON-NLS-1$

	public void setSrcDir(File srcDir)
	{
		this.srcDir = srcDir;
	}

	public void setDstDir(File dstDir)
	{
		this.dstDir = dstDir;
	}

	public void setIncludeAll(boolean includeAll) 
	{
		this.includeAll = includeAll;
	}

	public void setWhenEmptyString(String policy)
	{
		emptyStringPolicy = EmptyStringPolicy.valueOf(policy.toUpperCase());
	}

	public void add(FileNameMapper mapper)
	{
		if (this.mapper != null)
			throw new BuildException("mapper already set!");
		this.mapper = mapper;
	}

	abstract FileNameMapper defaultMapper(); 
	abstract FileSelector[] getSelectors();

	void checkArgs()
	{
		DirUtil.checkDir(srcDir, "srcDir", false); //$NON-NLS-1$
		DirUtil.checkDir(dstDir, "dstDir", true); //$NON-NLS-1$
		if (mapper == null)
		{
			// use default filename mapping if unset:
			mapper = defaultMapper();
		}

	}

   @Override
   public void execute() throws BuildException
   {
	   checkArgs();
	   try
	   {
         DirectoryScanner ds = super.getDirectoryScanner(srcDir);
         // use default includes if unset:
         if(!getImplicitFileSet().hasPatterns())
         {
             ds.setIncludes(new String[] {"**/*.properties"}); //$NON-NLS-1$
         }
         ds.setSelectors(getSelectors());
         ds.scan();
         String[] files = ds.getIncludedFiles();
         

         // for each of the props files in srcdir:
         for (int i = 0; i < files.length; i++)
         {
        	 processFile(files[i]);
         }
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
   }

	abstract void processFile(String propFilename) throws IOException;

//         FIXME use transProps
void generatePO(File englishFile, Properties englishProps, File transFile, Properties transProps, File toPoFile)
		throws IOException {
	int messageCount = 0;
	toPoFile.getParentFile().mkdirs();
	BufferedWriter out = new BufferedWriter(new FileWriter(toPoFile));
	try
	{
		Catalog cat = new Catalog(true);
		CatalogWriter writer = new CatalogWriter(cat);

		// this will be >0 if we are inside a NON-TRANSLATABLE block
		int nonTranslatable = 0;
	
PROPERTIES: 
		for (String key : englishProps.stringPropertyNames())
		{
		   String englishString = englishProps.getProperty(key);
		   
		   if (englishString.length() == 0)
		   {
			   String message = "Empty value for key "+key+" in file "+englishFile;
			   switch (emptyStringPolicy) {
			     case SKIP:
			       log(message, Project.MSG_DEBUG);
				   continue PROPERTIES;
			     case WARNANDSKIP:
	  		       log(message, Project.MSG_WARN);
	  			   continue PROPERTIES;
			     case INCLUDE:
	  		       log(message, Project.MSG_DEBUG);
			       break;
			     case WARNANDINCLUDE:
			       log(message, Project.MSG_WARN);
			       break;
			       // if anyone ever implements this, don't forget to 
			       // handle @@EMPTY@@ in pot2en and po2prop.  And
			       // *please* come up with a better sentinel value,
			       // but remember, translators may need to enter it as msgstr.
//        		     case REPLACE:
//        		       englishString = "@@EMPTY@@";
//        		       break;
			     case FAIL:
			    	 throw new BuildException(message);
				 default:
				   throw new RuntimeException("unhandled switch case "+emptyStringPolicy);
			   }
		   }
		   
		   // NB java.util.Properties throws away comments...

	       String comment;
	       if (includeAll) 
	       {
	          comment = englishProps.getComment(key);
	       } 
	       else 
	       {
	       	  String raw = englishProps.getRawComment(key);

	       	  if (raw != null && raw.length() != 0) // FIXME treat "" like null
	          {
	             StringBuilder sb = new StringBuilder(raw.length());
	             String[] lines = raw.split(NEWLINE_REGEX);
	             for (int j = 0; j < lines.length; j++) 
	             {
	                String line = lines[j];
	                // See http://wiki.eclipse.org/Eclipse_Globalization_Guidelines#Non-translatable_Message_Strings
	                if (line.startsWith("# START NON-TRANSLATABLE")) //$NON-NLS-1$ 
	                {
	                   ++nonTranslatable;
	                } 
	                else if (line.startsWith("# END NON-TRANSLATABLE")) //$NON-NLS-1$
	                {
	                   --nonTranslatable;
	                   if (nonTranslatable < 0)
	                      throw new BuildException(
	                            "Found '# END NON-TRANSLATABLE' " +
	                            "without matching " +
	                            "'# START NON-TRANSLATABLE': file="+englishFile+" key="+key);
	                } 
	                else if (nonTranslatable == 0) 
	                {
	                   sb.append(Properties.cookCommentLine(line));
	                   if (j+1 < lines.length)
	                      sb.append('\n');
	                }
	             }
	             comment = sb.toString();
	          } 
	          else 
	          {
	             comment = null;
	          }
	       }
	       if (nonTranslatable == 0) 
	       {
	          Message message = new Message();
	          if (comment != null)
	        	  message.addExtractedComment(comment);
	          message.addOccurence(new Occurence(key));
	          message.addFormat("java-format"); //  FIXME check this //$NON-NLS-1$
	          message.setMsgctxt(key);
	          message.setMsgid(englishString);
	          if (transProps != null)
	          {
	        	  message.setMsgstr(transProps.getProperty(key));
	          }
	          cat.addMessage(message);
	          ++messageCount;
	       }
	    }
		// FIXME include genComment in header ?
//        String genComment = toPoFile+" generated by "+getClass().getName()+" from "+englishFile+" and "+transFile;
		// TODO check that footerComment balances out nonTranslatable count 
		if(transFile != null)
			writer.writeTo(out, new Date(transFile.lastModified()));
		else
			writer.writeTo(out, new Date(englishFile.lastModified()));
	}
	finally
	{
	   out.close();
	}
	if(messageCount == 0)
		toPoFile.delete();
}

}