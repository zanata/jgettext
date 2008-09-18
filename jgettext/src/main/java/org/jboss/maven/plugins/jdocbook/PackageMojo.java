/*
 * Copyright © 2007  Red Hat Middleware, LLC. All rights reserved.
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
package org.jboss.maven.plugins.jdocbook;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * This mojo's responsibility within the plugin/packaging is to bundle the
 * individual formats into deployable formats.  The desicion  Note that some formats (PDF, e.g.) are
 * already deployable.
 * <p/>
 * After bundling, each bundle is then attached to the project
 *
 * @goal bundle
 * @phase package
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class PackageMojo extends AbstractDocBookMojo {
	/**
	 * {@inheritDoc}
	 */
	protected void process(FormatPlan[] formatPlans) throws RenderingException, XSLTException {
		File projectArtifactFile = new File( project.getBuild().getOutputDirectory(), project.getBuild().getFinalName() + ".war" );
		JarArchiver archiver = new JarArchiver();
		archiver.setDestFile( projectArtifactFile );

//		RendererFactory rendererFactory = new RendererFactory( options );
		try {
			for ( PublishingSource source : getPublishingSources() ) {
				for ( FormatPlan formatPlan : formatPlans ) {
					archiver.addDirectory(
							new File( source.resolvePublishingDirectory(), formatPlan.getName() ),
							formatPlan.getName() + "/"
					);
//					if ( attach ) {
//						File bundle = rendererFactory.buildRenderer( formatPlan.getName() ).getAttachableBundle( )
//					}
				}
			}
			archiver.createArchive();
		}
		catch ( IOException e ) {
			throw new RenderingException( "Unable to create archive [" + projectArtifactFile.getAbsolutePath() + "]", e );
		}
		catch ( ArchiverException e ) {
			throw new RenderingException( "Unable to populate archive [" + projectArtifactFile.getAbsolutePath() + "]", e );
		}

		project.getArtifact().setFile( projectArtifactFile );
	}
}
