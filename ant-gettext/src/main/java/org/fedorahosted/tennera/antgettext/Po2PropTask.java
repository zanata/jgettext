/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.Occurence;
import org.fedorahosted.tennera.jgettext.Catalog.MessageProcessor;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;

/**
 * 
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public class Po2PropTask extends MatchingTask
{
   private static final boolean INCLUDE_PROCESSING_COMMENT = false;
   // TODO disable this once multiline comments are fixed in prop2po
   private static final boolean INCLUDE_MESSAGE_COMMENTS = true;
   private File srcDir;
   private File dstDir;
   private Mapper mapper;
   private String locale = null;
   private boolean failOnNull = false;

   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
   }

   public void addMapper(Mapper mapper) {
       if (this.mapper != null)
           throw new BuildException("mapper already set!");
       this.mapper = mapper;
   }
   
   
   public void add(FileNameMapper filenameMapper)
   {
	   Mapper mapper = new Mapper(getProject());
	   mapper.add(filenameMapper);
	   addMapper(mapper);
   }
   
//   public Mapper createMapper() throws BuildException {
//       if (mapperElement != null) {
//           throw new BuildException(ERROR_MULTIPLE_MAPPERS,
//                                    getLocation());
//       }
//       mapperElement = new Mapper(getProject());
//       return mapperElement;
//   }
   
   public void setDstDir(File dstDir)
   {
      this.dstDir = dstDir;
   }
   
   public void setLocale(String locale)
   {
       this.locale = locale;
   }
   
   public void setFailOnNull(boolean failOnSkip) {
	   this.failOnNull = failOnSkip;
   }

   @Override
   public void execute() throws BuildException
   {
      DirUtil.checkDir(srcDir, "srcDir", false); //$NON-NLS-1$
      DirUtil.checkDir(dstDir, "dstDir", true); //$NON-NLS-1$

      String localeSuffix;
      if (locale == null || locale.length() == 0)
    	  localeSuffix = ""; //$NON-NLS-1$
      else
    	  localeSuffix = "_" + locale; //$NON-NLS-1$
      if (mapper == null)
      {
    	  // use default filename mapping if unset:
    	  GlobPatternMapper globMap = new GlobPatternMapper();
    	  globMap.setFrom("*.po"); //$NON-NLS-1$
    	  globMap.setTo("*"+localeSuffix+".properties"); //$NON-NLS-1$ //$NON-NLS-2$
    	  add(globMap);
      }
      try
      {
         DirectoryScanner ds = super.getDirectoryScanner(srcDir);
         // use default includes if unset:
         if(!getImplicitFileSet().hasPatterns())
             ds.setIncludes(new String[] {"**/*.po"}); //$NON-NLS-1$
         ds.scan();
         String[] files = ds.getIncludedFiles();

         for (int i = 0; i < files.length; i++)
         {
            String poFilename = files[i];
            File poFile = new File(srcDir, poFilename);
            
            String[] outFile = mapper.getImplementation().mapFileName(poFilename);
            if (outFile == null || outFile.length == 0)
            {
            	if (failOnNull)
            		throw new BuildException("Input filename "+poFilename+" mapped to null");
            	log("Skipping "+poFilename+": filename mapped to null", Project.MSG_VERBOSE);
            	continue;
            }
            String propFilename = outFile[0]; // FIXME support multiple output mappings
            File propFile = new File(dstDir, propFilename);
            if(propFile.lastModified() > poFile.lastModified())
            {
            	log("Skipping "+poFilename+": "+propFilename +" is up to date", Project.MSG_VERBOSE);
            	continue;
            }
            
            final Properties props = new Properties();
            log("Generating "+propFile+" from "+poFile, Project.MSG_VERBOSE);

            propFile.getParentFile().mkdirs();
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(propFile));
            try
            {
//               POUnmarshaller unmarshaller = POFactory.createUnmarshaller();
//               POFile file = unmarshaller.unmarshall(poFile);
//               for (POEntry entry : file.getEntries())
		ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
		parser.catalog();
		Catalog catalog = parser.getCatalog();
		MessageProcessor processor = new MessageProcessor() 
		{
		    public void processMessage(Message entry) {
			String ctxt = entry.getMsgctxt();
			// NB entries which don't have (a) ctxt or (b) reference will be ignored
			Collection<String> poComments = entry.getExtractedComments();
			StringBuilder sb = new StringBuilder();
			for (String comm : poComments) 
			{
			    sb.append(comm).append("\n"); //$NON-NLS-1$
			}
			String poComment = sb.toString();
			//System.err.println("Processing "+entry.toString());
			if (ctxt == null)
			{
			    // TODO gettext tools and the POUnmarshaller do not preserve whitespace in references 
			    // so we should use the original en properties as a template
			    if(entry.getMsgid().length() != 0)
			    {
				for (Occurence occ : entry.getOccurences())
				{
				    String ref = occ.toString();
				    props.setProperty(ref, entry.getMsgstr());
				    if (poComment.length() != 0 && INCLUDE_MESSAGE_COMMENTS)
					props.setComment(ref, poComment);
				}
			    }
			}
			else
			{
			    props.setProperty(ctxt, entry.getMsgstr());
			    if (poComment.length() != 0 && INCLUDE_MESSAGE_COMMENTS)
				props.setComment(ctxt, poComment);
			}
		    }
		};
		catalog.processMessages( processor);

		String comment = null;
		if (INCLUDE_PROCESSING_COMMENT) {
		    comment = propFilename+" generated by "+Po2PropTask.class.getName()+" from "+poFilename;
		}
		props.store(out, comment);
//               props.store(System.out, comment);
            }
            finally
            {
               out.close();
            }
         }
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
   }

}
