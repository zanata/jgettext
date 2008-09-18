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
package org.jboss.jdocbook.xslt.resolve;

import java.net.URL;
import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

/**
 * Responsible for resolving relative references from jar base urls.
 *
 * @author Steve Ebersole
 */
public class RelativeJarUriResolver implements URIResolver {
	/**
	 * {@inheritDoc}
	 */
	public Source resolve(String href, String base) throws TransformerException {
		// href need to be relative
		if ( href.indexOf( "://" ) > 0 || href.startsWith( "/" ) ) {
			return null;
		}

		// base would need to start with jar:
		if ( !base.startsWith( "jar:" ) ) {
			return null;
		}

		String fullHref = base.substring( 4, base.lastIndexOf( '/' ) + 1 ) + href;
		try {
			URL url = new URL( fullHref );
			return new StreamSource( url.openStream(), url.toExternalForm() );
		}
		catch ( Throwable t ) {
			return null;
		}
	}
}