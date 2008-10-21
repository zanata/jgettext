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
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

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

   public void setDir1(File srcDir)
   {
      this.dir1 = srcDir;
   }
   
   public void setDir2(File dstDir)
   {
      this.dir2 = dstDir;
   }
   
   @Override
   public void execute() throws BuildException
   {
      DirUtil.checkDir(dir1, "dir1", false);
      DirUtil.checkDir(dir2, "dir2", false);

      try
      {
         DirectoryScanner ds = super.getDirectoryScanner(dir1);
         // use default includes if unset:
         if(!getImplicitFileSet().hasPatterns())
             ds.setIncludes(new String[] {"**/*.properties"});
         ds.scan();
         String[] files = ds.getIncludedFiles();

         for (int i = 0; i < files.length; i++)
         {
            String propFilename = files[i];
            File prop1File = new File(dir1, propFilename);
            File prop2File = new File(dir2, propFilename);
            Properties props1 = new Properties();
            BufferedReader in1 = new BufferedReader(new FileReader(prop1File));
            try
            {
               props1.load(in1);
               Properties props2 = new Properties();
               BufferedReader in2 = new BufferedReader(new FileReader(prop2File));
               try
               {
                  props2.load(in2);
      
                  if(!props1.equals(props2))
                     throw new BuildException(prop1File.getPath()+" is different from "+prop2File.getPath()+": file1="+props1+", file2="+props2);
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
         System.out.println("Verified .properties files in "+dir1+" against "+dir2);
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
   }

}
