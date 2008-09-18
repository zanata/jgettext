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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.xslt.XSLTException;
import org.jboss.maven.shared.resource.ResourceDelegate;

/**
 * This mojo's responsibility within the plugin/packaging is to process resources
 * defined by various inputs, moving them into a staging directory for use
 * during XSLT processing.  This is needed because the DocBook XSLT only allow
 * defining a single <tt>img.src.path</tt> value; FOP only allows a single
 * <tt>fontBaseDir</tt> value; etc.
 *
 * @goal resources
 * @phase process-resources
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class ResourceMojo extends AbstractDocBookMojo {

	protected void process(FormatPlan[] formattings) throws RenderingException, XSLTException {
		// allow project local style artifacts to override imported ones...
		processDependencySupportArtifacts( collectDocBookStyleDependentArtifacts() );
		processProjectResources();
	}

	// project local resources ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private void processProjectResources() throws RenderingException {
		if ( imageResource != null ) {
			new ResourceDelegate( project, new File( stagingDirectory, "images" ), getLog() ).process( imageResource );
		}
		if ( cssResource != null ) {
			new ResourceDelegate( project, new File( stagingDirectory, "css" ), getLog() ).process( cssResource );
		}
	}


	// dependency support resources ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private List<Artifact> collectDocBookStyleDependentArtifacts() {
		final ArrayList<Artifact> rtn = new ArrayList<Artifact>();
		processArtifacts(
				new ArtifactProcessor() {
					public void process(Artifact artifact) {
						if ( "jdocbook-style".equals( artifact.getType() ) ) {
							rtn.add( artifact );
						}
					}
				}
		);
		return rtn;
	}

	private void processDependencySupportArtifacts(List<Artifact> artifacts) throws RenderingException {
		for ( Artifact artifact : artifacts ) {
			processDependencySupportArtifact( artifact.getFile(), stagingDirectory );
		}
	}

	protected void processDependencySupportArtifact(File file, File target) throws RenderingException {
		getLog().info( "unpacking dependency resource [" + file.getAbsolutePath() + "] to staging-dir [" + target.getAbsolutePath() + "]" );
		try {
            target.mkdirs();
			UnArchiver unArchiver = archiverManager.getUnArchiver( "jar" );
            unArchiver.setSourceFile( file );
            unArchiver.setDestDirectory( target );
			unArchiver.extract();
		}
        catch ( NoSuchArchiverException e ) {
            throw new RenderingException( "Unknown archiver type", e );
        }
        catch ( ArchiverException e ) {
            throw new RenderingException( "Error unpacking file [" + file + "] to [" + target + "]", e );
        }
        catch ( IOException e ) {
            throw new RenderingException( "Error unpacking file [" + file + "] to [" + target + "]", e );
        }
    }
}
