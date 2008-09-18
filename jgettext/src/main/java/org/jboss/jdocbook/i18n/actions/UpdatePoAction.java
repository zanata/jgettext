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
import java.util.Locale;

import org.jboss.jdocbook.Action;
import org.jboss.jdocbook.Options;
import org.jboss.jdocbook.i18n.actions.process.Executor;
import org.jboss.jdocbook.util.VCSDirectoryExclusionFilter;
import org.jboss.jdocbook.util.I18nUtils;
import org.jboss.jdocbook.util.FileUtils;

/**
 * Update translatable entries in a translation's PO files based on the state of the POT files.
 * <p/>
 * NOTE : The processing here is based on either the msgmerge or msginit commands from the GNU gettext package.
 *
 * @author Steve Ebersole
 */
public class UpdatePoAction implements Action {
	private final File potDirectory;
	private final File translationDirectory;
	private final Locale translationLocale;
	private final Options options;

	/**
	 * Construct an action ready to perform updates to the PO files for a given translation corresponding to the POT
	 * templates in the given POT-directory.
	 *
	 * @param potDirectory The directory containing the POT files.
	 * @param translationDirectory The directory containing the translation PO files.
	 * @param translationLocale The locale of the translation
	 * @param options The user options
	 */
	public UpdatePoAction(File potDirectory, File translationDirectory, Locale translationLocale, Options options) {
		this.potDirectory = potDirectory;
		this.translationDirectory = translationDirectory;
		this.translationLocale = translationLocale;
		this.options = options;
	}

	/**
	 * {@inheritDoc}
	 */
	public void perform() {
		msgmerge( potDirectory, translationDirectory, translationLocale );
	}

	/**
	 * Update the PO files contained in the given translation-directory based on their corresponding POT file
	 * from the template-directory.
	 *
	 * @param templateDirectory The directory containind POT files.
	 * @param translationDirectory The PO files directory.
	 * @param locale The translation locale.
	 */
	protected void msgmerge(File templateDirectory, File translationDirectory, Locale locale) {
		if ( !templateDirectory.exists() ) {
			options.getLog().info( "skipping PO updates; POT directory did not exist : {0}", potDirectory );
			return;
		}
		File[] files = templateDirectory.listFiles( new VCSDirectoryExclusionFilter() );
		for ( int i = 0, X = files.length; i < X; i++) {
			if ( files[i].isDirectory() ) {
				msgmerge(
						new File( templateDirectory, files[i].getName() ),
						new File( translationDirectory, files[i].getName() ),
						translationLocale
				);
			}
			else {
				if ( I18nUtils.isPotFile( files[i] ) ) {
					File translation = new File( translationDirectory, I18nUtils.determinePoFileName( files[i] ) );
					updateTranslation( files[i], translation, locale );
				}
			}
		}
	}

	private void updateTranslation(File template, File translation, Locale locale) {
		if ( !template.exists() ) {
			options.getLog().trace( "skipping PO updates; POT file did not exist : {0}", template );
			return;
		}

		if ( translation.lastModified() >= template.lastModified() ) {
			options.getLog().trace( "skipping PO updates; up-to-date : {0}", translation );
			return;
		}

		final String cmd;
		if ( translation.exists() ) {
			cmd = "msgmerge --quiet --backup=none --update " + FileUtils.resolveFullPathName( translation )
					+ " " + FileUtils.resolveFullPathName( template );
		}
		else {
			translation.getParentFile().mkdirs();
			cmd = "msginit --no-translator -l " + locale
					+ " -i " + FileUtils.resolveFullPathName( template )
					+ " -o " + FileUtils.resolveFullPathName( translation );
		}
		Executor.execute( cmd );
	}
}
