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
package org.jboss.jdocbook.render.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.jboss.jdocbook.Options;
import org.jboss.jdocbook.render.RenderingException;

/**
 * Implementation of the {@link org.jboss.jdocbook.render.Renderer} contract specifically for dealing with PDF generation.
 *
 * @author Steve Ebersole
 */
public class PDFRenderer extends BasicRenderer {
	public PDFRenderer(Options options) {
		super( options );
	}

	protected Result buildResult(File targetFile) throws RenderingException {
		return new ResultImpl( targetFile );
	}

	protected void releaseResult(Result transformationResult) {
		( ( ResultImpl ) transformationResult ).release();
	}

	private class ResultImpl extends SAXResult {
		private OutputStream outputStream;

		public ResultImpl(File targetFile) throws RenderingException {
			try {
				FopFactory fopFactory = FopFactory.newInstance();
//				fopFactory.getRendererFactory().addRendererMaker(
//						new PDFRendererMaker() {
//
//						}
//				);

				outputStream = new BufferedOutputStream( new FileOutputStream( targetFile ) );

				FOUserAgent fopUserAgent = fopFactory.newFOUserAgent();
				fopUserAgent.setProducer( "jDocBook Plugin for Maven" );
//				fopUserAgent.setRendererOverride(
//						new org.apache.fop.render.pdf.PDFRenderer() {
//							public void setupFontInfo(FontInfo inFontInfo) {
//
//							}
//						}
//				);

				Fop fop = fopFactory.newFop( MimeConstants.MIME_PDF, fopUserAgent, outputStream );
				setHandler( fop.getDefaultHandler() );
			}
			catch ( Throwable t ) {
				throw new RenderingException( "error building transformation result [" + targetFile.getAbsolutePath() + "]", t );
			}
		}

		private void release() {
			if ( outputStream == null ) {
				return;
			}
			try {
				outputStream.flush();
				outputStream.close();
			}
			catch ( IOException ignore ) {
			}
		}
	}
}
