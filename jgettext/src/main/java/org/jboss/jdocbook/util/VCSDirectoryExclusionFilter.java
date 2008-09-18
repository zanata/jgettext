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
package org.jboss.jdocbook.util;

import java.io.FileFilter;
import java.io.File;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * A {@link FileFilter} which excludes VCS metadata directories...
 *
 * @author Steve Ebersole
 */
public class VCSDirectoryExclusionFilter implements FileFilter {
	/**
	 * {@inheritDoc}
	 */
	public boolean accept(File path) {
		if ( path.isDirectory() ) {
			if ( isVCSDirectory( path ) ) {
				return false;
			}
		}
		return true;
	}

	public static boolean isVCSDirectory(File path) {
		final String absolutePath = path.getAbsolutePath();
		for ( int i = 0, X = DirectoryScanner.DEFAULTEXCLUDES.length; i < X; i++ ) {
			if ( SelectorUtils.matchPath( DirectoryScanner.DEFAULTEXCLUDES[i], absolutePath, true ) ) {
				// do not accept file on match against an exclude
				return true;
			}
		}
		return false;
	}
}
