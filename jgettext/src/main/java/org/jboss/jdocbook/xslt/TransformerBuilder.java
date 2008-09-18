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
package org.jboss.jdocbook.xslt;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import com.icl.saxon.Controller;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.util.NoOpWriter;
import org.jboss.jdocbook.util.ResourceHelper;
import org.jboss.jdocbook.xslt.resolve.CurrentVersionResolver;
import org.jboss.jdocbook.xslt.resolve.ExplicitUrnResolver;
import org.jboss.jdocbook.xslt.resolve.RelativeJarUriResolver;
import org.jboss.jdocbook.xslt.resolve.ResolverChain;
import org.jboss.jdocbook.xslt.resolve.VersionResolver;
import org.jboss.jdocbook.Options;

/**
 * A builder of {@link javax.xml.transform.Transformer} instances, configurable
 * to return either SAXON or XALAN based transformers.
 *
 * @author Steve Ebersole
 */
public class TransformerBuilder {
	private final Options options;

	public TransformerBuilder(Options options) {
		this.options = options;
	}

	public Transformer buildStandardTransformer(URL xslt) {
		URIResolver uriResolver = buildStandardUriResolver();
		return buildTransformer( xslt, uriResolver );
	}

	public Transformer buildStandardTransformer(String xsltResource) {
		URIResolver uriResolver = buildStandardUriResolver();
		return buildTransformer( ResourceHelper.requireResource( xsltResource ), uriResolver );
	}

	public Transformer buildTransformer(FormatPlan formatPlan, URL customStylesheet) throws XSLTException {
		URIResolver uriResolver = buildUriResolver( formatPlan );
		URL xsltStylesheet = customStylesheet == null
				? ResourceHelper.requireResource( formatPlan.getStylesheetResource() )
				: customStylesheet;
		return buildTransformer( xsltStylesheet, uriResolver );
	}

	protected Transformer buildTransformer(URL xslt, URIResolver uriResolver) throws XSLTException {
		javax.xml.transform.TransformerFactory transformerFactory = options.resolveXmlTransformerType().getSAXTransformerFactory();
		transformerFactory.setURIResolver( uriResolver );

		Transformer transformer;
		try {
			Source source = new StreamSource( xslt.openStream(), xslt.toExternalForm() );
			transformer = transformerFactory.newTransformer( source );
		}
		catch ( IOException e ) {
			throw new XSLTException( "problem opening stylesheet", e );
		}
		catch ( TransformerConfigurationException e ) {
			throw new XSLTException( "unable to build transformer [" + e.getLocationAsString() + "] : " + e.getMessage(), e );
		}

		configureTransformer( transformer, uriResolver, options.getTransformerParameters() );

		return transformer;

	}

	public void configureTransformer(Transformer transformer, FormatPlan formatPlan) {
		configureTransformer( transformer, buildUriResolver( formatPlan ), options.getTransformerParameters() );
	}

	public static void configureTransformer(Transformer transformer, URIResolver uriResolver, Properties transformerParameters) {
		if ( transformer instanceof Controller ) {
			Controller controller = ( Controller ) transformer;
			try {
				controller.makeMessageEmitter();
				controller.getMessageEmitter().setWriter( new NoOpWriter() );
			}
			catch ( TransformerException te ) {
				// intentionally empty
			}
		}

		transformer.setURIResolver( uriResolver );
		transformer.setParameter( "fop.extensions", "0" );
		transformer.setParameter( "fop1.extensions", "1" );

		if ( transformerParameters == null ) {
			return;
		}
		for ( Map.Entry<Object, Object> entry : transformerParameters.entrySet() ) {
			transformer.setParameter( ( String ) entry.getKey(), entry.getValue() );
		}
	}

	public ResolverChain buildStandardUriResolver() {
		ResolverChain resolverChain = new ResolverChain();
		applyStandardResolvers( resolverChain );
		return resolverChain;
	}

	public ResolverChain buildUriResolver(FormatPlan formatPlan) throws XSLTException {
		return buildUriResolver( formatPlan.getName(), formatPlan.getCorrespondingDocBookStylesheetResource() );
	}

	public ResolverChain buildUriResolver(String formatName, String docBookstyleSheet) throws XSLTException {
		ResolverChain resolverChain = new ResolverChain( new ExplicitUrnResolver( formatName, docBookstyleSheet ) );
		applyStandardResolvers( resolverChain );
		return resolverChain;
	}

	private void applyStandardResolvers(ResolverChain resolverChain) {
		resolverChain.addResolver( new CurrentVersionResolver() );
		if ( options.getDocbookVersion() != null ) {
			resolverChain.addResolver( new VersionResolver( options.getDocbookVersion() ) );
		}
		resolverChain.addResolver( new RelativeJarUriResolver() );
		resolverChain.addResolver( options.getCatalogResolver() );
	}
}