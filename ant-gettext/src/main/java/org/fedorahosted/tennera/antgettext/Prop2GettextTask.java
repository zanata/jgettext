package org.fedorahosted.tennera.antgettext;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * Converts a directory tree of Java Properties files into 
 * one or more gettext files (PO/POT) (for a single gettext 
 * domain, but optionally multiple locales).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: $
 */
public class Prop2GettextTask extends AbstractPropGettextTask {
	boolean generatePO = true;
	boolean generatePOT = true;
	private Catalog potCatalog = new Catalog(true);
	
	public void setPO(boolean generatePO) 
	{
		this.generatePO = generatePO;
	}
	
	public void setPOT(boolean generatePOT) 
	{
		this.generatePOT = generatePOT;
	}

   @Override
   Catalog initPOCatalog(String locale) 
   		throws FileNotFoundException, IOException, RecognitionException, TokenStreamException 
   {
	   return new Catalog(false);
   }

   private void saveCat(Catalog cat, File poFile) throws IOException 
   {
	   BufferedWriter out = new BufferedWriter(new FileWriter(poFile));
	   try
	   {
		   PoWriter writer = new PoWriter();
		   writer.setGenerateHeader(true);
		   writer.write(cat, out);
	   }
	   finally
	   {
		   out.close();
	   }
   }

	void processFile(String propFilename) throws IOException
	{
//	   		log("processFile "+propFilename);
            File propFile = new File(srcDir, propFilename);
            
            String propBasename = StringUtil.removeFileExtension(
            		propFilename, ".properties"); //$NON-NLS-1$
            
            List<File> propTransFiles = new ArrayList<File>(locales.length);
            List<String> transLocales = new ArrayList<String>(locales.length);
            
            for (String locale : locales) 
            {
            	String propTransBasename = propBasename+"_"+locale+".properties"; //$NON-NLS-1$ //$NON-NLS-2$
				File propTransFile = new File(srcDir, propTransBasename);
            	if (propTransFile.exists()) 
            	{
	            	log(propTransFile+" added to list");
	            	propTransFiles.add(propTransFile);
	            	transLocales.add(locale);
				}
            	else
            	{
            		log(propTransFile+" does not exist");
            	}
			}
            if (propTransFiles.isEmpty() && !generatePOT) 
            {
            	// no need to load this properties file
            	return;
            }
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
            
            if (generatePOT)
            	visitProperties(englishFile, englishProps, new POGenerator(potCatalog, null));
//            	addToCatalog(englishFile, englishProps, null, potCat);
            
// indent from here            
    for (int k = 0; k < propTransFiles.size(); k++) 
    {
		File propTransFile = propTransFiles.get(k);
		String locale = transLocales.get(k);
        log("Reading messages from "+propFile+" and "+ propTransFile, Project.MSG_VERBOSE);
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
        Catalog poCat = poCatalogs.get(locale);
        visitProperties(englishFile, englishProps, new POGenerator(poCat, transProps));
//        addToCatalog(englishFile, englishProps, transProps, poCat);
    }
// indent to here            
   }
	
	static class POGenerator implements PropertiesVisitor
	{
		private final Catalog cat;
		private final Properties transProps;
		
		public POGenerator(Catalog cat, Properties transProps) {
			this.cat = cat;
			this.transProps = transProps;
		}

		@Override
		public void visit(String key, String englishString, String comment, String englishFile, int lineNumber) {
			String msgctxt = key;
			String msgid = englishString;

			Message message = cat.locateMessage(msgctxt, msgid);
			if(message != null)
			{
				if (transProps != null)
				{
					assert StringUtil.equals(message.getMsgstr(), transProps.getProperty(key));
				}
			}
			else
			{
				message = new Message();
				message.addFormat("java-format"); //  TODO check this //$NON-NLS-1$
				message.addSourceReference(msgctxt);
				message.setMsgid(msgid);
				message.setMsgctxt(msgctxt);
				if (transProps != null)
				{
					String msgstr = transProps.getProperty(key);
					if (msgstr != null)
						message.setMsgstr(msgstr);
				}
				cat.addMessage(message);
			}
			if (comment != null)
				message.addExtractedComment(comment);
			message.addSourceReference(englishFile, lineNumber);
		}
		
	}

	@Override
	void postExecute() throws IOException 
	{
		if (generatePO) 
		{
			for (String locale : locales) 
			{
				Catalog cat = poCatalogs.get(locale);
				File poFile = new File(dstDir, locale+".po"); //$NON-NLS-1$
				if (cat.isEmpty())
					poFile.delete();
				else
					saveCat(cat, poFile);
			}
		}
		if (generatePOT)
		{
			File potFile = new File(dstDir, dstDir.getName()+".pot"); //$NON-NLS-1$
			saveCat(potCatalog, potFile);
		}
	}

}
