/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;

/**
 * Verify that two trees of Java Properties files are equivalent.
 * Note that any extra .properties files in the dir2 subtree are ignored.
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public class VerifyPropTask extends MatchingTask
{
   private File dir1;
   private File dir2;
   private Mapper mapper;
   private boolean failOnNull = false;
   private boolean failFast = false;
   private boolean keysOnly = false;

   public void setDir1(File srcDir)
   {
      this.dir1 = srcDir;
   }
   
   public void addMapper(Mapper mapper) 
   {
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

   public void setDir2(File dstDir)
   {
      this.dir2 = dstDir;
   }
   
   public void setFailOnNull(boolean failOnSkip) 
   {
	   this.failOnNull = failOnSkip;
   }
   
   public void setFailFast(boolean failFast) 
   {
	   this.failFast = failFast;
   }
   
   public void setKeysOnly(boolean keysOnly) 
   {
       this.keysOnly = keysOnly;
   }

   @Override
   public void execute() throws BuildException
   {
      DirUtil.checkDir(dir1, "dir1", false); //$NON-NLS-1$
      DirUtil.checkDir(dir2, "dir2", false); //$NON-NLS-1$

      if (mapper == null)
      {
   		  add(new IdentityMapper());
      }
      try
      {
         DirectoryScanner ds = super.getDirectoryScanner(dir1);
         // use default includes if unset:
         if(!getImplicitFileSet().hasPatterns())
             ds.setIncludes(new String[] {"**/*.properties"}); //$NON-NLS-1$
         ds.scan();
         String[] files = ds.getIncludedFiles();

         for (int i = 0; i < files.length; i++)
         {
            String prop1Filename = files[i];
            File prop1File = new File(dir1, prop1Filename);
			String[] outFile = mapper.getImplementation().mapFileName(prop1Filename);
			if (outFile == null || outFile.length == 0)
			{
				if (failOnNull)
					throw new BuildException("Input filename "+prop1File+" mapped to null");
				log("Skipping "+prop1File+": filename mapped to null", Project.MSG_VERBOSE);
				continue;
			}
			String prop2Filename = outFile[0]; // FIXME support multiple output mappings?
			File prop2File = new File(dir2, prop2Filename);
            
            Properties props1 = new Properties();
            InputStream in1 = new FileInputStream(prop1File);
            try
            {
               props1.load(in1);
               Properties props2 = new Properties();
               InputStream in2 = new FileInputStream(prop2File);
               try
               {
                  props2.load(in2);
                  int errorCount = 0;
                  StringBuilder errors = new StringBuilder();
        		  errors.append(prop1File.getPath()+" is different from "+prop2File.getPath()+": ");
    			  errors.append(System.getProperty("line.separator")); //$NON-NLS-1$
      
                  for (Map.Entry<Object,Object> entry : props1.entrySet()) 
                  {
                	  String propName = (String) entry.getKey();
                	  String prop1Val = (String) entry.getValue();
                	  String prop2Val = (String) props2.remove(propName);
                	  if(!equivalent(prop1Val, prop2Val)) 
                	  {
                		  ++errorCount;
                		  recordError(errors, propName + " -> {" + prop1Val + ", " + prop2Val + "}");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                	  }
                  }
                  if(!props2.isEmpty())
                  {
            		  ++errorCount;
            		  String message = "second file contains extra keys: "+props2.keySet();
            		  recordError(errors, message);
                  }
                  
                  if(errorCount != 0)
                	  throw new BuildException(errors.toString());
               }
               finally
               {
                  in2.close();
               }
            }
            finally
            {
               in1.close();
            }
         }
         log("Verified .properties files in "+dir1+" against "+dir2, Project.MSG_VERBOSE);
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
   }

   private boolean equivalent(String prop1Val, String prop2Val) 
   {
	   if (keysOnly)
		   return prop2Val != null;
	   else
		   return prop1Val.equals(prop2Val);
   }

   private void recordError(StringBuilder errors, String message) 
   {
		errors.append(message);
		if (failFast) 
		{
			throw new BuildException(errors.toString());
		} 
		else 
		{
			// record all errMsgs, throw exception at the end
			errors.append(System.getProperty("line.separator")); //$NON-NLS-1$
		}
	}

}
