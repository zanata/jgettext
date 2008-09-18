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

import javax.xml.transform.sax.SAXTransformerFactory;

/**
 * Enumeration of supported XSLT transformers.
 *
 * @author Steve Ebersole
 */
public abstract class TransformerType {
	public static final TransformerType SAXON = new SaxonTransformerType();
	public static final TransformerType XALAN = new XalanTransformerType();

	private final String name;

	private SAXTransformerFactory factory;

	private TransformerType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public SAXTransformerFactory getSAXTransformerFactory() {
		if ( factory == null ) {
			factory = buildSAXTransformerFactory();
		}
		return factory;
	}

	protected abstract SAXTransformerFactory buildSAXTransformerFactory();

	public static TransformerType parse(String name) {
		if ( XALAN.name.equals( name ) ) {
			return XALAN;
		}
		else {
			// default
			return SAXON;
		}
	}

	/**
	 * The SAXON transformer type...
	 */
	public static class SaxonTransformerType extends TransformerType {
		public SaxonTransformerType() {
			super( "saxon" );
		}

		public SAXTransformerFactory buildSAXTransformerFactory() {
			return new com.icl.saxon.TransformerFactoryImpl();
		}
	}

	public static class XalanTransformerType extends TransformerType {
		public XalanTransformerType() {
			super( "xalan" );
		}

		public SAXTransformerFactory buildSAXTransformerFactory() {
			return new org.apache.xalan.processor.TransformerFactoryImpl();
		}
	}
}