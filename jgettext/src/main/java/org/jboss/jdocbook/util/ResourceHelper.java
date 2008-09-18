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

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Simple helpers for locating and handling classpath and file URL resource
 * lookups.
 *
 * @author Steve Ebersole
 */
public class ResourceHelper {
	/**
	 * Locate said resource, throwing an exception if it could not be found.
	 *
	 * @param name The resource name.
	 * @return The resource's URL.
	 * @throws IllegalArgumentException If the resource could not be found.
	 */
	public static URL requireResource(String name) {
		URL resource = locateResource( name );
		if ( resource == null ) {
			throw new IllegalArgumentException( "could not locate resource [" + name + "]" );
		}
		return resource;
	}

	/**
	 * Locate said resource.
	 *
	 * @param name The resource name.
	 * @return The resource's URL.
	 */
	public static URL locateResource(String name) {
		if ( name.startsWith( "classpath:" ) ) {
			return locateClassPathResource( name.substring( 10 ) );
		}
		else if ( name.startsWith( "file:" ) ) {
			try {
				return new URL( name );
			}
			catch ( MalformedURLException e ) {
				throw new IllegalArgumentException( "malformed explicit file url [" + name + "]");
			}
		}
		else {
			// assume a classpath resource (backwards compatibility)
			return locateClassPathResource( name );
		}
	}

	private static URL locateClassPathResource(String name) {
		while ( name.startsWith( "/" ) ) {
			name = name.substring( 1 );
		}
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if ( loader == null ) {
			loader = ResourceHelper.class.getClassLoader();
		}
		URL result = loader.getResource( name );
		if ( result == null ) {
			result = loader.getResource( "/" + name );
		}
		return result;
	}
}