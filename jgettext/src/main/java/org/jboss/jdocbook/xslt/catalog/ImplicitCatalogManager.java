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
package org.jboss.jdocbook.xslt.catalog;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * CatalogManager which resolves its catalogs internally via  classpath
 * resource lookups.  Its looks for resources named '/catalog.xml' on the
 * classpath.
 *
 * @author Steve Ebersole
 */
public class ImplicitCatalogManager extends AbstractCatalogManager {
	public ImplicitCatalogManager() {
		super( resolveCatalogNames() );
	}

	private static ArrayList<String> resolveCatalogNames() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if ( classLoader == null ) {
			classLoader = ImplicitCatalogManager.class.getClassLoader();
		}
		ArrayList<String> names = new ArrayList<String>();
        try {
            Enumeration enumeration = classLoader.getResources( "/catalog.xml" );
            while ( enumeration.hasMoreElements() ) {
				final URL resource = ( URL ) enumeration.nextElement();
				final String resourcePath = resource.toExternalForm();
				if ( resourcePath != null ) {
					names.add( resourcePath );
				}
            }
        }
		catch ( IOException ignore ) {
			// intentionally empty
		}
		return names;
	}
}