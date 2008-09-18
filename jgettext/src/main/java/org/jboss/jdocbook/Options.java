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
package org.jboss.jdocbook;

import java.util.Properties;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.jboss.jdocbook.util.TransformerType;
import org.jboss.jdocbook.xslt.TransformerBuilder;
import org.jboss.jdocbook.xslt.catalog.ExplicitCatalogManager;
import org.jboss.jdocbook.xslt.catalog.ImplicitCatalogManager;

/**
 * A (detachable) representation of the user configuration.
 *
 * @author Steve Ebersole
 */
public class Options {
    private boolean xincludeSupported;
	private String[] catalogs;
	private String xmlTransformerType;
	private Properties transformerParameters;
	private boolean useRelativeImageUris = true;
	private String docbookVersion;
	private char localeSeparator = '-';

	private CatalogResolver catalogResolver;
	private TransformerBuilder transformerBuilder;

	private Log log = new NoOpLog();

	protected Options() {
	}

	public Options(char localeSeparator) {
		this.localeSeparator = localeSeparator;
	}

	public Options(
			boolean xincludeSupported,
			String[] catalogs,
			String xmlTransformerType,
			Properties transformerParameters,
			boolean useRelativeImageUris,
			String docBookVersion,
			char localeSeparator) {
		this.xincludeSupported = xincludeSupported;
		this.catalogs = catalogs;
		this.xmlTransformerType = xmlTransformerType;
		this.transformerParameters = transformerParameters;
		this.useRelativeImageUris = useRelativeImageUris;
		this.docbookVersion = docBookVersion;
		this.localeSeparator = localeSeparator;
	}

	public boolean isXincludeSupported() {
		return xincludeSupported;
	}

	public String[] getCatalogs() {
		return catalogs;
	}

	public String getXmlTransformerType() {
		return xmlTransformerType;
	}

	public TransformerType resolveXmlTransformerType() {
		return TransformerType.parse( getXmlTransformerType() );
	}

	public Properties getTransformerParameters() {
		if ( transformerParameters == null ) {
			transformerParameters = new Properties();
		}
		return transformerParameters;
	}

	public boolean isUseRelativeImageUris() {
		return useRelativeImageUris;
	}

	public String getDocbookVersion() {
		return docbookVersion;
	}

	public void setDocbookVersion(String docbookVersion) {
		this.docbookVersion = docbookVersion;
	}

	public char getLocaleSeparator() {
		return localeSeparator;
	}

	public CatalogResolver getCatalogResolver() {
		if ( catalogResolver == null ) {
			CatalogManager catalogManager;
			if ( getCatalogs() == null || getCatalogs().length == 0 ) {
				catalogManager = new ImplicitCatalogManager();
			}
			else {
				catalogManager = new ExplicitCatalogManager( getCatalogs() );
			}
			catalogResolver = new CatalogResolver( catalogManager );
		}
		return catalogResolver;
	}

	public TransformerBuilder getTransformerBuilder() {
		if ( transformerBuilder == null ) {
			transformerBuilder = new TransformerBuilder( this );
		}
		return transformerBuilder;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public Log getLog() {
		return log;
	}

	private static class NoOpLog implements Log {

		public void trace(String message) {
		}

		public void trace(String message, Object... args) {
		}

		public void info(String message) {
		}

		public void info(String message, Object... args) {
		}

		public void info(String message, Throwable exception) {
		}

		public void info(String message, Throwable exception, Object... args) {
		}

		public void error(String message) {
		}

		public void error(String message, Object... args) {
		}

		public void error(String message, Throwable exception) {
		}

		public void error(String message, Throwable exception, Object... args) {
		}
	}
}