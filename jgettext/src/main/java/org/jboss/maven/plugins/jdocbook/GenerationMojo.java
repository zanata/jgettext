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
import java.util.List;
import java.util.Locale;

import org.apache.maven.artifact.Artifact;
import org.jboss.jdocbook.profile.ProfilerFactory;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.impl.RendererFactory;
import org.jboss.jdocbook.util.DocBookProfilingStrategy;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * This mojo's responsibility within the plugin/packaging is actually performing 
 * the DocBook transformations.  At the highest level, it takes the source and
 * process it via the specified DocBook XSLT to produce output.
 *
 * @goal generate
 * @phase compile
 * @requiresDependencyResolution
 *
 * @author Steve Ebersole
 */
public class GenerationMojo extends AbstractDocBookMojo {

	protected void process(FormatPlan[] plans) throws XSLTException, RenderingException {
		if ( !sourceDirectory.exists() ) {
			getLog().info( "sourceDirectory [" + sourceDirectory.getAbsolutePath() + "] did not exist" );
			return;
		}

		if ( !workDirectory.exists() ) {
			workDirectory.mkdirs();
		}

		if ( options.getDocbookVersion() == null ) {
			processArtifacts(
					new ArtifactProcessor() {
						public void process(Artifact artifact) {
							if ( "net.sf.docbook".equals( artifact.getGroupId() ) &&
									"docbook".equals( artifact.getArtifactId() ) ) {
								getLog().debug( "Found docbook version : " + artifact.getVersion() );
								if ( options.getDocbookVersion() != null ) {
									getLog().warn( "found multiple docbook versions" );
								}
								options.setDocbookVersion( artifact.getVersion() );
							}
						}
					}
			);
		}

		RendererFactory rendererFactory = new RendererFactory( options );
		Locale requestedLocale = getRequestedLocale();

		List<PublishingSource> sources = getPublishingSources();
		for ( PublishingSource source : sources ) {
			if ( requestedLocale != null && !requestedLocale.equals( source.getLocale() ) ) {
				getLog().info( "skipping non-requested lang [" + stringify( source.getLocale() ) + "]" );
				continue;
			}

			File sourceFile = source.resolveDocumentFile();
			if ( !sourceFile.exists() ) {
				getLog().info( "Source document [" + sourceFile.getAbsolutePath() + "] did not exist; skipping" );
				continue;
			}

			File publishingDirectory = source.resolvePublishingDirectory();
			if ( !publishingDirectory.exists() ) {
				publishingDirectory.mkdirs();
			}

			final String lang = stringify( source.getLocale() );
			options.getTransformerParameters().setProperty( "l10n.gentext.language", lang );
			options.getTransformerParameters().setProperty( "profile.lang", lang );

			boolean hasBeenProfiled = false;
			for ( FormatPlan plan : plans ) {
				if ( plan.getProfiling() == DocBookProfilingStrategy.TWO_PASS && !hasBeenProfiled ) {
					hasBeenProfiled = true;
					File profileOutputDir = new File( new File( workDirectory, "profile" ), lang );
					sourceFile = new ProfilerFactory( profileOutputDir, options ).buildProfiler().applyProfiling( sourceFile );
				}
				rendererFactory.buildRenderer( plan.getName() ).render( sourceFile, plan, publishingDirectory, stagingDirectory );
			}
		}
	}

}
