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
package org.jboss.jdocbook.profile;

import java.io.File;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.jboss.jdocbook.Options;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.util.Constants;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * Implementation of the {@link Profiler} contract.
 *
 * @author Steve Ebersole
 */
class ProfilerImpl implements Profiler {
	private final File outputDirectory;
	private final Options options;

	ProfilerImpl(File outputDirectory, Options options) {
		this.outputDirectory = outputDirectory;
		this.options = options;
	}

	/**
	 * {@inheritDoc}
	 */
	public File applyProfiling(File sourceFile) {
		try {
			if ( !outputDirectory.exists() ) {
				outputDirectory.mkdirs();
			}
			File targetFile = new File( outputDirectory, sourceFile.getName() );
			options.getLog().info( "applying DocBook profiling [" + targetFile.getAbsolutePath() + "]" );

			Transformer xslt = options.getTransformerBuilder()
					.buildStandardTransformer( Constants.MAIN_PROFILE_XSL_RESOURCE );
			xslt.transform( buildSource( sourceFile ), buildResult( targetFile ) );
			return targetFile;
		}
		catch ( TransformerException e ) {
			throw new XSLTException( "error performing translation [" + e.getLocationAsString() + "] : " + e.getMessage(), e );
		}
	}

	private Source buildSource(File sourceFile) throws RenderingException {
		return FileUtils.createSAXSource( sourceFile, options.getCatalogResolver(), options.isXincludeSupported() );
	}

	protected Result buildResult(File targetFile) throws RenderingException, XSLTException {
		return new StreamResult( targetFile );
	}
}
