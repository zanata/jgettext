package org.fedorahosted.tennera.antgettext;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * Converts one or more po files (for a single gettext domain, 
 * but optionally multiple locales) into a tree of properties files.
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class Gettext2PropTask extends AbstractPropGettextTask 
{
	private File propSrcDir;
	
	public void setPropSrcDir(File propSrcDir) {
		this.propSrcDir = propSrcDir;
	}
	
	@Override
	protected void initialise() throws FileNotFoundException, RecognitionException,
			TokenStreamException, IOException {
		super.initialise();
		if (propSrcDir == null)
			propSrcDir = dstDir;
	}
	
	@Override
	protected DirectoryScanner getPropDirectoryScanner() {
		return this.getDirectoryScanner(propSrcDir);
	}
	
	@Override
	Catalog initPOCatalog(String locale) 
		throws FileNotFoundException, IOException, RecognitionException, TokenStreamException 
	{
		try
		{
			File poFile = new File(srcDir, locale+".po"); //$NON-NLS-1$
			ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
			parser.catalog();
			return parser.getCatalog();
		}
		catch (Exception e)
		{
			log(e.getMessage(), Project.MSG_WARN);
			return null;
		}
	}

	@Override
	void processFile(String propFilename) throws IOException 
	{
        File propFile = new File(propSrcDir, propFilename);
        String englishFile;
        try {
        	englishFile = FileUtils.getRelativePath(relativeBase, propFile);
        } catch (Exception e) {
        	throw new IOException(e);
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

		String propBasename = StringUtil.removeFileExtension(
				propFilename, ".properties"); //$NON-NLS-1$
		for (String locale : locales) 
		{
			Catalog cat = poCatalogs.get(locale);
			if (cat == null)
				continue;
			String propTransBasename = propBasename+"_"+locale+".properties"; //$NON-NLS-1$ //$NON-NLS-2$
			File propTransFile = new File(dstDir, propTransBasename);
			PropGenerator propGenerator = new PropGenerator(cat);
			visitProperties(englishFile, englishProps, propGenerator);
			Properties props = propGenerator.getProperties();
			if(props.size() != 0)
			{
				propTransFile.getParentFile().mkdirs();
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(propTransFile));
				try
				{
					props.store(out, null);
				}
				finally
				{
					out.close();
				}
			}
			else
			{
				propTransFile.delete();
			}
		}
	
	}
	
	static class PropGenerator implements PropertiesVisitor
	{
		private final Catalog cat;
		Properties properties = new Properties();
		
		public PropGenerator(Catalog cat) {
			this.cat = cat;
		}
		
		public Properties getProperties() {
			return properties;
		}
		
		@Override
		public void visit(String key, String englishString, String comment,
				String englishFile, int lineNumber) {
			String msgid = englishString;
			String msgctxt = key;
			Message message = cat.locateMessage(msgctxt, msgid);
			if (message != null && !message.isFuzzy())
				properties.setProperty(key, message.getMsgstr());
		}
	}
	
	@Override
	void postExecute() throws IOException {
		// nothing
	}
	
}
