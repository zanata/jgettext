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

import java.util.List;
import java.util.Locale;

import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.i18n.actions.UpdatePoAction;

/**
 * UpdatePoMojo implementationslatable strings from the master translation source into the POT files.
 *
 * @goal update-po
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class UpdatePoMojo extends AbstractDocBookMojo {
	protected void doExecute() throws JDocBookProcessException {
		Locale translationLocale = getRequestedLocale();
		List<I18nSource> sources = getI18nSources();
		for ( I18nSource source : sources ) {
			if ( translationLocale == null || translationLocale.equals( source.getLocale() ) ) {
				getLog().info( "Updating PO file [" + stringify( source.getLocale() ) + "]" );
				new UpdatePoAction( potDirectory, source.resolvePoDirectory(), source.getLocale(), options ).perform();
			}
		}
	}
}
