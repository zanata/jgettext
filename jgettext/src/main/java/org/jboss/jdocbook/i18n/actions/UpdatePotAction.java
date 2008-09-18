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
 * Update the PortableObjectTemplate (POT) file(s) from a given master source file.
 * <p/>
 * Any XIncludes contained in the master source file are followed and processed as well.
 *
 * @author Steve Ebersole
 */
public class UpdatePotAction implements Action {
	private final File master;
	private final File templateDirectory;
	private final Options options;

	/**
	 * Construct an action ready to perform updates on the POT files pertaining to a given master DocBook source.
	 *
	 * @param master The DocBook source
	 * @param templateDirectory The directory where POT files are contained.
	 * @param options The user options
	 */
	public UpdatePotAction(File master, File templateDirectory, Options options) {
		this.templateDirectory = templateDirectory;
		this.master = master;
		this.options = options;
	}

	/**
	 * {@inheritDoc}
	 */
	public void perform() throws ActionException {
		xml2pot( master, templateDirectory );
	}

	void xml2pot(File masterFile, File templateDirectory) throws ActionException {
		if ( !masterFile.exists() ) {
			options.getLog().info( "skipping POT updates; master file did not exist : {0}", masterFile );
			return;
		}
		final File sourceBasedir = masterFile.getParentFile();
		final String potFileName = I18nUtils.determinePotFileName( masterFile );
		final File potFile = new File( templateDirectory, potFileName );
		updatePortableObjectTemplate( masterFile, potFile );

		// Note : recursion below accounts for inclusions within inclusions
		for ( File inclusion : XIncludeHelper.locateInclusions( masterFile ) ) {
			final String relativity = FileUtils.determineRelativity( inclusion, sourceBasedir );
			final File relativeTemplateDir = ( relativity == null ) ? templateDirectory : new File(
					templateDirectory,
					relativity
			);
			xml2pot( inclusion, relativeTemplateDir );
		}
	}

	private void updatePortableObjectTemplate(File sourceFile, File template) {
		if ( !sourceFile.exists() ) {
			options.getLog().trace( "skipping POT update; source file did not exist : {0}", sourceFile );
			return;
		}

		if ( template.exists() && template.lastModified() >= sourceFile.lastModified() ) {
			options.getLog().trace( "skipping POT update; up-to-date : {0}", template );
			return;
		}

		template.getParentFile().mkdirs();
// supposedly the RH DocBot stuff uses xml2pot...
//		executeXml2po( sourceFile, template );
		executeXml2pot( sourceFile, template );
	}

//	private void executeXml2po(File sourceFile, File template) {
//		final String cmd = "xml2po -o " + I18nUtils.resolveFullPathName( template ) + " " + I18nUtils.resolveFullPathName( sourceFile );
//		Executor.execute( cmd );
//	}

	private void executeXml2pot(File sourceFile, File template) {
		final String cmd = "xml2pot " + FileUtils.resolveFullPathName( sourceFile );

		try {
			final FileOutputStream xmlStream = new FileOutputStream( template );
			try {
				options.getLog().trace( "updating POT file {0}", template );
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
			throw new ActionException( "unable to open output stream for POT file [" + template + "]" );
		}
	}
}
