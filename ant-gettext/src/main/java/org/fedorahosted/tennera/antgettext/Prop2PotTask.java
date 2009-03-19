/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.fedorahosted.openprops.Properties;

/**
 * Converts Java Properties files into gettext template files (POT).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public class Prop2PotTask extends Prop2GettextTask
{

	@Override
	FileNameMapper defaultMapper() 
	{
		// use default filename mapping if unset:
		GlobPatternMapper globMap = new GlobPatternMapper();
		globMap.setFrom("*.properties"); //$NON-NLS-1$
		globMap.setTo("*.pot"); //$NON-NLS-1$
		return globMap;
	}
	
	@Override
	FileSelector[] getSelectors() 
	{
		return null;
	}
   
   @Override
   void processFile(String propFilename) throws IOException
   {
            File propFile = new File(srcDir, propFilename);
            String[] outFile = mapper.mapFileName(propFilename);
            if (outFile == null || outFile.length == 0)
            {
            	log("Skipping "+propFilename+": filename mapped to null", Project.MSG_VERBOSE);
            	return;
            }
            File potFile = new File(dstDir, outFile[0]); // FIXME support multiple output mappings
            if(potFile.lastModified() > propFile.lastModified())
            {
            	log("Skipping " + propFilename + ": " + potFile.getPath()
							+ " is up to date", Project.MSG_VERBOSE);
            	return;
            }
            Properties props = new Properties();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(propFile));
            try
            {
            	props.load(in);
            }
            finally
            { 
            	in.close();
            }
            log("Generating "+potFile+" from "+propFile, Project.MSG_VERBOSE);
            generatePO(propFile, props, null, null, potFile);
   }
}
