/*
 * Copyright (c) 2007, Red Hat Middleware, LLC. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, v. 2.1. This program is distributed in the
 * hope that it will be useful, but WITHOUT A WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License, v.2.1 along with this
 * distribution; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Red Hat Author(s): Steve Ebersole
 */
package org.jboss.jdocbook.i18n.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jboss.jdocbook.Action;
import org.jboss.jdocbook.Options;
import org.jboss.jdocbook.i18n.actions.process.Executor;
import org.jboss.jdocbook.util.I18nUtils;
import org.jboss.jdocbook.util.XIncludeHelper;
import org.jboss.jdocbook.util.FileUtils;

/**
 * Action to render the translators work back into XML in preparation for XSLT processing.
 * <p/>
 * NOTE : The processing here is based on po2xml from the poxml library.
 *
 * @author Steve Ebersole
 */
public class TranslateXmlAction implements Action {
	private final File masterFile;
	private final File poDirectory;
	private final File targetDirectory;
	private final Options options;

	public TranslateXmlAction(File masterFile, File poDirectory, File targetDirectory, Options options) {
		this.masterFile = masterFile;
		this.poDirectory = poDirectory;
		this.targetDirectory = targetDirectory;
		this.options = options;
	}

	public void perform() {
		po2xml( masterFile, poDirectory, targetDirectory );
	}

	protected void po2xml(File masterFile, File translationDirectory, File translatedWorkDirectory) {
		options.getLog().trace( "starting translation [" + masterFile + "]" );
		if ( !masterFile.exists() ) {
			options.getLog().info( "skipping translation; master file did not exist : {0}", masterFile );
			return;
		}

		final String poFileName = I18nUtils.determinePoFileName( masterFile );
		final File poFile = new File( translationDirectory, poFileName );
		if ( !poFile.exists() ) {
			throw new ActionException( "Unable to locate PO file for [" + masterFile.getName() + "] in [" + translationDirectory.getName() + "]" );
		}
		final File translatedFile = new File( translatedWorkDirectory, masterFile.getName() );
		generateTranslatedXML( masterFile, poFile, translatedFile );

		// Note : recursion below accounts for inclusions within inclusions
		final File sourceBasedir = masterFile.getParentFile();
		for ( File inclusion : XIncludeHelper.locateInclusions( masterFile ) ) {
			options.getLog().trace( "starting translation of inclusion [" + inclusion + "]" );
			final String relativity = FileUtils.determineRelativity( inclusion, sourceBasedir );
			options.getLog().trace( "determined relativity : " + relativity );
			final File relativeTranslationDir = ( relativity == null )
					? translationDirectory
					: new File( translationDirectory, relativity );
			final File relativeWorkDir = ( relativity == null )
					? translatedWorkDirectory
					: new File( translatedWorkDirectory, relativity );
			po2xml( inclusion, relativeTranslationDir, relativeWorkDir );
		}
	}

	private void generateTranslatedXML(File sourceFile, File poFile, File translatedFile) {
		if ( !sourceFile.exists() ) {
			options.getLog().trace( "skipping translation; source file did not exist : {0}", sourceFile );
			return;
		}
		if ( !poFile.exists() ) {
			options.getLog().trace( "skipping translation; PO file did not exist : {0}", poFile );
			return;
		}

		if ( translatedFile.exists()
				&& translatedFile.lastModified() >= sourceFile.lastModified()
				&& translatedFile.lastModified() >= poFile.lastModified() ) {
			options.getLog().trace( "skipping translation; up-to-date : {0}", translatedFile );
			return;
		}

		translatedFile.getParentFile().mkdirs();
		final String cmd = "po2xml " + FileUtils.resolveFullPathName( sourceFile ) + " " + FileUtils.resolveFullPathName( poFile );
		try {
			final FileOutputStream xmlStream = new FileOutputStream( translatedFile );
			try {
				options.getLog().trace( "<execute>" + cmd + "</execution>" );
				Executor.execute( cmd, xmlStream );
			}
			finally {
				try {
					xmlStream.flush();
					xmlStream.close();
				}
				catch ( IOException ignore ) {
					// intentionally empty...
				}
			}
		}
		catch ( IOException e  ) {
			throw new ActionException( "unable to open output stream for translated XML file [" + translatedFile + "]" );
		}
	}
}
