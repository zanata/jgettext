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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.jboss.jdocbook.Options;
import org.jboss.jdocbook.render.Renderer;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.util.FileUtils;
import org.jboss.jdocbook.util.ResourceHelper;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * The basic implementation of the {@link Renderer} contract.
 *
 * @author Steve Ebersole
 */
public class BasicRenderer implements Renderer {
	protected final Options options;

	/**
	 * Construct a renderer instance using the given <tt>options</tt>.
	 *
	 * @param options The options.
	 */
	public BasicRenderer(Options options) {
		this.options = options;
	}

	/**
	 * {@inheritDoc}
	 */
	public File getAttachableBundle(File source) {
		// todo : we need to figure out how we are going to handle attachments...
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(File sourceFile, FormatPlan formatPlan, File renderingDirectory, File stagingDirectory) throws RenderingException, XSLTException {
		File targetDirectory = new File( renderingDirectory, formatPlan.getName() );
		if ( ! targetDirectory.exists() ) {
			FileUtils.mkdir( targetDirectory.getAbsolutePath() );
		}

		if ( formatPlan.isImageCopyingRequired() ) {
			if ( stagingDirectory.exists() ) {
				File imageBase = new File( stagingDirectory, "images" );
				if ( imageBase.exists() ) {
					try {
						FileUtils.copyDirectoryStructure( imageBase, targetDirectory );
					}
					catch ( IOException e ) {
						throw new RenderingException( "unable to copy images", e );
					}
				}
				File cssBase = new File( stagingDirectory, "css" );
				if ( cssBase.exists() ) {
					try {
						FileUtils.copyDirectoryStructure( cssBase, targetDirectory );
					}
					catch ( IOException e ) {
						throw new RenderingException( "unable to copy css", e );
					}
				}
			}
		}

		File targetFile = new File( targetDirectory, deduceTargetFileName( sourceFile, formatPlan ) );
		if ( targetFile.exists() ) {
			targetFile.delete();
		}
		if ( !targetFile.exists() ) {
			try {
				targetFile.createNewFile();
			}
			catch ( IOException e ) {
				throw new RenderingException( "unable to create output file [" + targetFile.getAbsolutePath() + "]", e );
			}
		}

		performRendering( sourceFile, formatPlan, stagingDirectory, targetFile );
	}

	private void performRendering(File sourceFile, FormatPlan formatPlan, File stagingDirectory, File targetFile) {
		Transformer transformer = buildTransformer( targetFile, formatPlan, stagingDirectory );
		Source transformationSource = buildSource( sourceFile );
		Result transformationResult = buildResult( targetFile );
		try {
			transformer.transform( transformationSource, transformationResult );
		}
		catch ( TransformerException e ) {
			throw new XSLTException( "error performing translation [" + e.getMessageAndLocation() + "]", e );
		}
		finally {
			releaseResult( transformationResult );
		}
	}

	private String deduceTargetFileName(File source, FormatPlan formatPlan) {
		return formatPlan.getTargetNamingStrategy().determineTargetFileName( source );
	}

	protected Transformer buildTransformer(File targetFile, FormatPlan formatPlan, File stagingDirectory) throws RenderingException, XSLTException {
		final URL transformationStylesheet =  ResourceHelper.requireResource( formatPlan.getStylesheetResource() );
		Transformer transformer = options.getTransformerBuilder().buildTransformer( formatPlan, transformationStylesheet );
		if ( formatPlan.isImagePathSettingRequired() ) {
			try {
				String imgSrcPath = new File( stagingDirectory, "images" ).toURL().toString();
				if ( !imgSrcPath.endsWith( "/" ) ) {
					imgSrcPath += '/';
				}
				options.getLog().trace( "setting 'img.src.path' xslt parameter [" + imgSrcPath + "]" );
				transformer.setParameter( "img.src.path", imgSrcPath );
			}
			catch ( MalformedURLException e ) {
				throw new XSLTException( "unable to prepare 'img.src.path' xslt parameter", e );
			}
		}
		transformer.setParameter( "keep.relative.image.uris", options.isUseRelativeImageUris() ? "1" : "0" );
		if ( formatPlan.isDoingChunking() ) {
			String rootFilename = targetFile.getName();
			rootFilename = rootFilename.substring( 0, rootFilename.lastIndexOf( '.' ) );
			transformer.setParameter( "root.filename", rootFilename );
			transformer.setParameter( "base.dir", targetFile.getParent() + File.separator );
			transformer.setParameter( "manifest.in.base.dir", "1" );
		}
		return transformer;
	}

	private Source buildSource(File sourceFile) throws RenderingException {
		return FileUtils.createSAXSource( sourceFile, options.getCatalogResolver(), options.isXincludeSupported() );
    }

	protected Result buildResult(File targetFile) throws RenderingException, XSLTException {
		return new StreamResult( targetFile );
	}

	protected void releaseResult(Result transformationResult) {
		// typically nothing to do...
	}
}
