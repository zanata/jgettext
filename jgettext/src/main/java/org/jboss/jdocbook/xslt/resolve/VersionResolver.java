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

import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.jboss.jdocbook.util.ResourceHelper;

/**
 * Maps docbook-version based URIs to local classpath lookups.  These URIs are in the form
 * 'http://docbook.sourceforge.net/release/xsl/{version}'
 *
 * @author Steve Ebersole
 */
public class VersionResolver implements URIResolver {
	public static final String BASE_HREF = "http://docbook.sourceforge.net/release/xsl/";

	private final String version;
	private final String versionHref;

	/**
	 * Constructs a VersionResolver instance using the given <tt>version</tt>.
	 *
	 * @param version The version.
	 */
	public VersionResolver(String version) {
		this.version = version;
		this.versionHref = BASE_HREF + version;
	}

	/**
	 * {@inheritDoc}
	 */
	public Source resolve(String href, String base) throws TransformerException {
		if ( href.startsWith( versionHref ) ) {
			return resolve( href );
		}
		else if ( base.startsWith( versionHref ) ) {
			return resolve( base + "/" + href );
		}
		return null;
	}

	private Source resolve(String href) {
		String resource = href.substring( versionHref.length() );
		try {
			URL resourceURL = ResourceHelper.requireResource( resource );
			return new StreamSource( resourceURL.openStream(), resourceURL.toExternalForm() );
		}
		catch ( IllegalArgumentException e ) {
			return null;
		}
		catch ( IOException e ) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + " [version=" + version + "]";
	}
}