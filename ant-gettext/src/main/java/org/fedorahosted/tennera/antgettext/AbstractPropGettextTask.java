package org.fedorahosted.tennera.antgettext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.fedorahosted.tennera.jgettext.Catalog;

import antlr.RecognitionException;
import antlr.TokenStreamException;

abstract class AbstractPropGettextTask extends AbstractPropVisitingTask {

	File relativeBase;
	protected final Map<String,Catalog> poCatalogs = new HashMap<String,Catalog>();

	public void setRelativeBase(File relativeBase) {
		this.relativeBase = relativeBase;
	}

	@Override
	protected void initialise() throws FileNotFoundException, RecognitionException, TokenStreamException, IOException {
		super.initialise();
		if (relativeBase == null)
			relativeBase = dstDir;
		if (locales == null)
			throw new BuildException("locales attribute must be set");
		for (String locale : locales) 
		{
			Catalog cat = initPOCatalog(locale);
			if(cat != null)
				poCatalogs.put(locale, cat);
		}

	}

	abstract Catalog initPOCatalog(String locale)
			throws FileNotFoundException, IOException, RecognitionException,
			TokenStreamException;

}