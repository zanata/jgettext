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
package org.jboss.maven.plugins.jdocbook;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.jboss.jdocbook.JDocBookProcessException;
import org.codehaus.plexus.util.FileUtils;

/**
 * CleanPoMojo implementation
 *
 * @goal clean-po
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class CleanPoMojo extends AbstractDocBookMojo {
	protected void doExecute() throws JDocBookProcessException {
		Locale requestedLocale = getRequestedLocale();
		for ( I18nSource source : getI18nSources() ) {
			if ( requestedLocale != null && !requestedLocale.equals( source.getLocale() ) ) {
				continue;
			}
			final File poDirectory = source.resolvePoDirectory();
			if ( poDirectory.exists() ) {
				try {
					FileUtils.cleanDirectory( poDirectory );
				}
				catch ( IOException e ) {
					getLog().warn( "unable to cleanup POT directory [" + poDirectory + "]", e );
				}
			}
		}
	}
}
