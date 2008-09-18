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

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

/**
 * Basic support for URIResolvers which map a URN unto a single replacement
 * {@link javax.xml.transform.Source}.
 *
 * @author Steve Ebersole
 */
public class BasicUrnResolver implements URIResolver {
	private final String urn;
	private final Source source;

	/**
	 * Constructs a {@link URIResolver} which maps occurences of the given <tt>urn</tt> onto the given
	 * <tt>source</tt>
	 *
	 * @param urn The urn to be replaced.
	 * @param source The value to return instead of the urn.
	 */
	public BasicUrnResolver(String urn, Source source) {
		this.urn = urn;
		this.source = source;
	}

	/**
	 * {@inheritDoc}
	 */
	public Source resolve(String href, String base) throws TransformerException {
		return urn.equals( href ) ? source : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + " [URN:" + urn + "]";
	}
}