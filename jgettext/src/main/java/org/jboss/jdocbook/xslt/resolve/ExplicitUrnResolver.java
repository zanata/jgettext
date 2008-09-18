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
import javax.xml.transform.stream.StreamSource;

import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.jdocbook.util.ResourceHelper;


/**
 * Resolves an explicit <tt>urn:docbook:stylesheet</tt> URN against the standard
 * DocBook stylesheets.
 *
 * @author Steve Ebersole
 */
public class ExplicitUrnResolver extends BasicUrnResolver {
	private final String name;

	public ExplicitUrnResolver(String name, String stylesheetResource) throws XSLTException {
		super( "urn:docbook:stylesheet", createSource( name, stylesheetResource ) );
		this.name = name;
	}

	private static Source createSource(String name, String stylesheetResource) throws XSLTException {
		URL stylesheet = ResourceHelper.requireResource( stylesheetResource );
		try {
			return new StreamSource( stylesheet.openStream(), stylesheet.toExternalForm() );
		}
		catch ( IOException e ) {
			throw new XSLTException( "could not locate DocBook stylesheet [" + name + "]", e );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + " [" + name + "]";
	}
}