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
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.fedorahosted.openprops.Properties;

/**
 * Converts Java Properties files into gettext files (PO).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public class Prop2PoTask extends Prop2GettextTask
{
	private String[] locales;
	File srcTransDir;

	public void setLocales(String locales)
	{
		this.locales = locales.split(","); //$NON-NLS-1$
	}

	public void setSrcTransDir(File srcTransDir)
	{
		this.srcTransDir = srcTransDir;
	}

	@Override
	FileNameMapper defaultMapper() 
	{
		GlobPatternMapper globMap = new GlobPatternMapper();
		globMap.setFrom("*.properties"); //$NON-NLS-1$
		globMap.setTo("*.po"); //$NON-NLS-1$
		return globMap;
	}
	
	private class BasePropertiesSelector implements FileSelector
	{
		@Override
		public boolean isSelected(File basedir, String filename, File file)
				throws BuildException 
		{
			for (String loc : locales) 
			{
				if (filename.endsWith("_"+loc+".properties")) { //$NON-NLS-1$ //$NON-NLS-2$
//					log("skipping translated property file for now: "+filename);
					return false;
				}
			}
			return true;
		}
	}

	@Override
	FileSelector[] getSelectors() 
	{
		return new FileSelector[] {new BasePropertiesSelector()};
	}

	@Override
	void checkArgs() 
	{
		super.checkArgs();
		if (locales == null)
			throw new BuildException("locales attribute must be set!");
		if(srcTransDir == null)
			srcTransDir = srcDir;
	}

   @Override
   void processFile(String propFilename) throws IOException
   {
//	   		log("processFile "+propFilename);
            File propFile = new File(srcDir, propFilename);
            
            String propBasename = StringUtil.removeFileExtension(
            		propFilename, ".properties");
            
            List<File> propTransFiles = new ArrayList<File>(locales.length);
            List<File> poFiles = new ArrayList<File>(locales.length);
            
            for (String locale : locales) 
            {
            	String propTransBasename = propBasename+"_"+locale+".properties"; //$NON-NLS-1$ //$NON-NLS-2$
				File propTransFile = new File(srcTransDir, propTransBasename);
            	if (propTransFile.exists()) 
            	{
		            String[] outFile = mapper.mapFileName(propTransBasename);
		            if (outFile == null || outFile.length == 0)
		            {
		            	log("Skipping "+propTransFile+": filename mapped to null", Project.MSG_VERBOSE);
		            	return;
		            }
		            File poFile = new File(dstDir, outFile[0]); // FIXME support multiple output mappings
		            if (poFile.lastModified() > propFile.lastModified() && 
		            		poFile.lastModified() > propTransFile.lastModified())
		            {
		            	log("Skipping " + propTransFile.getName() + ": " + poFile.getPath()
									+ " is up to date", Project.MSG_VERBOSE);
		            }
		            else
		            {
		            	log(propTransFile+" added to list");
		            	propTransFiles.add(propTransFile);
		            	poFiles.add(poFile);
		            }
				}
            	else
            	{
            		log(propTransFile+" does not exist");
            	}
			}
            if (propTransFiles.isEmpty()) 
            {
//            	log("no new translations for "+propFilename);
            	// there are no translations, or all the POs are up to date
            	return;
            }
            Properties englishProps = new Properties();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(propFile));
            try
            {
            	englishProps.load(in);
            }
            finally
            { 
            	in.close();
            }
            
// indent from here            
    for (int k = 0; k < propTransFiles.size(); k++) 
    {
		File propTransFile = propTransFiles.get(k);
		File poFile = poFiles.get(k);
        log("Generating "+poFile+" from "+propFile+" and "+ propTransFile, Project.MSG_VERBOSE);
        Properties transProps = new Properties();
        BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(propTransFile));
        try
        {
        	transProps.load(in2);
        }
        finally
        { 
        	in2.close();
        }
        generatePO(propFile, englishProps, propTransFile, transProps, poFile);
    }
// indent to here            
   }

}
