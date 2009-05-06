package org.fedorahosted.tennera.antgettext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.util.FileNameMapper;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.HeaderUtil;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;

/**
 * Converts Java Properties files into gettext files (PO or POT).  
 * Each properties file becomes one PO file whose name is determined
 * by the FileNameMapper 'mapper'.
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public abstract class AbstractProp2PoPotTask extends AbstractPropReadingTask {
	private FileNameMapper mapper;
	public void add(FileNameMapper mapper)
	{
		if (this.mapper != null)
			throw new BuildException("mapper already set!");
		this.mapper = mapper;
	}
	public FileNameMapper getMapper() 
	{
		return mapper;
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

		// this will be >0 if we are inside a NON-TRANSLATABLE block
		int nonTranslatable = 0;
	
PROPERTIES: 
		for (String key : englishProps.stringPropertyNames())
		{
		   String englishString = englishProps.getProperty(key);
		   if (shouldSkip(englishFile.toString(), key, englishString))
			   continue PROPERTIES;
		   
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
	          message.addSourceReference(key);
	          message.addFormat("java-format"); //  FIXME check this //$NON-NLS-1$
	          message.setMsgctxt(key);
	          message.setMsgid(englishString);
	          if (transProps != null)
	          {
	        	  String translatedValue = transProps.getProperty(key);
	        	  message.setMsgstr(translatedValue != null ? translatedValue : ""); //$NON-NLS-1$
	          }
	          cat.addMessage(message);
	          ++messageCount;
	       }
	    }
		// FIXME include genComment in header ?
//        String genComment = toPoFile+" generated by "+getClass().getName()+" from "+englishFile+" and "+transFile;
		// TODO check that footerComment balances out nonTranslatable count 
		PoWriter writer = new PoWriter();
		Date potDate;
//		if(transFile != null)
//			potDate = new Date(transFile.lastModified());
//		else
			potDate = new Date(englishFile.lastModified());
		cat.addMessage(HeaderUtil.generateDefaultHeader(potDate));
		writer.write(cat, out);
	}
	finally
	{
	   out.close();
	}
	if(messageCount == 0)
		toPoFile.delete();
}

}