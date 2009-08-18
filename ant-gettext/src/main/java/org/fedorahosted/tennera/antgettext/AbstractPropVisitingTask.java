package org.fedorahosted.tennera.antgettext;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.fedorahosted.openprops.Properties;


public abstract class AbstractPropVisitingTask extends AbstractPropReadingTask {

	String[] locales;
	protected static interface PropertiesVisitor
	{
		public void visit(String key, String englishString, String comment, String englishFile, int lineNumber);
	}

	public void setLocales(String locales) {
		this.locales = locales.split(","); //$NON-NLS-1$
	}

	FileSelector[] getSelectors() {
		if (locales != null)
			return new FileSelector[] {new BasePropertiesSelector(locales)};
		else
			return new FileSelector[0];
	}

	@Override
	public void execute() throws BuildException {
		try
		{
			try
			{
				initialise();
			}
			catch (FileNotFoundException e)
			{
				log("skipped: "+e.getMessage(), Project.MSG_WARN);
				return;
			}
			dstDir.mkdirs();
			DirectoryScanner ds = getPropDirectoryScanner();
			// use default includes if unset:
			if(!getImplicitFileSet().hasPatterns())
			{
				ds.setIncludes(new String[] {"**/*.properties"}); //$NON-NLS-1$
			}
			ds.setSelectors(getSelectors());
			ds.scan();
			String[] files = ds.getIncludedFiles();

			// for each of the base props files in srcdir:
			for (int i = 0; i < files.length; i++)
			{
				processFile(files[i]);
			}
			postExecute();
		}
		catch (DirMissingException e)
		{
			log("skipped: "+e.getMessage());
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}

	abstract void processFile(String propFilename) throws IOException; 

	abstract void postExecute() throws IOException;

	public AbstractPropVisitingTask() {
		super();
	}

	protected void visitProperties(String englishFile, Properties englishProps, PropertiesVisitor visitor)
			throws IOException {
			
					// this will be >0 if we are inside a NON-TRANSLATABLE block
					int nonTranslatable = 0;
				
			PROPERTIES: 
					for (String key : englishProps.stringPropertyNames())
					{
					   String englishString = englishProps.getProperty(key);
					   
					   // NB java.util.Properties throws away comments...
			
				       String comment;
				       if (includeAll) 
				       {
				          comment = englishProps.getComment(key);
				       } 
				       else 
				       {
				       	  String raw = englishProps.getRawComment(key);
			
				       	  if (raw != null && raw.length() != 0) // TODO treat "" like null
				          {
				             StringBuilder sb = new StringBuilder(raw.length());
				             String[] lines = raw.split(NEWLINE_REGEX);
				             for (int j = 0; j < lines.length; j++) 
				             {
				                String line = lines[j];
				                // See http://wiki.eclipse.org/Eclipse_Globalization_Guidelines#Non-translatable_Message_Strings
				                if (line.startsWith(START_MARKER))
				                {
				                   ++nonTranslatable;
				                } 
				                else if (line.startsWith(END_MARKER))
				                {
				                   --nonTranslatable;
				                   if (nonTranslatable < 0)
				                      throw new BuildException(
				                            "Found \"" + END_MARKER + "\" without matching \"" +
				                            START_MARKER + "\" near " +
				                            englishFile+":"+englishProps.getLineNumber(key));
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
				       if (shouldSkip(englishFile, key, englishString))
						   continue PROPERTIES;
					   
				       if (nonTranslatable == 0) 
				       {
				    	   visitor.visit(key, englishString, comment, englishFile, englishProps.getLineNumber(key));
				       }
				    }
					// TODO process raw footer comment NON-TRANSLATABLEs, then:
//					if (nonTranslatable != 0)
//					{
//						throw new BuildException("File "+englishFile+": \"" +
//								START_MARKER + "\" comments don't match \"" +
//								END_MARKER + "\" comments");
//					} 
				}

}