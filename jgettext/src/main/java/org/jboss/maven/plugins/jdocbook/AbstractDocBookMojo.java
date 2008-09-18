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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jboss.jdocbook.JDocBookProcessException;
import org.jboss.jdocbook.Log;
import org.jboss.jdocbook.render.RenderingException;
import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptors;
import org.jboss.jdocbook.util.LocaleUtils;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * Basic support for the various DocBook mojos in this packaging plugin.
 * Mainly, we are defining common configuration attributes of the packaging.
 * 
 * @author Steve Ebersole
 */
public abstract class AbstractDocBookMojo extends AbstractMojo {
	public static final String PLUGIN_NAME = "jdocbook";

	/**
	 * INTERNAL : The project being built
	 *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

	/**
	 * INTERNAL : The artifacts associated with the dependencies defined as part
	 * of the project to which we are being attached.
	 *
	 * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
	 */
	protected Set projectArtifacts;

	/**
	 * INTERNAL : The artifacts associated to the dependencies defined as part
	 * of our configuration within the project to which we are being attached.
	 *
	 * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
	 */
	protected List pluginArtifacts;

    /**
     * INTERNAL : used to get reference to environemtn Archiver/UnArchiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     * @readonly
     */
    protected ArchiverManager archiverManager;

	/**
	 * INTERNAL : used during packaging to attach produced artifacts
	 *
	 * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}"
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

	/**
	 * The name of the document (relative to sourceDirectory) which is the
	 * document to be rendered.
	 *
	 * @parameter
	*  @required
	 */
	protected String sourceDocumentName;

	/**
	 * The directory where the sources are located.
	 *
	 * @parameter expression="${basedir}/src/main/docbook"
	 */
	protected File sourceDirectory;

	/**
	 * A {@link Resource} describing project-local images.
	 *
	 * @parameter
	 */
	protected Resource imageResource;

	/**
	 * A {@link Resource} describing project-local css.
	 *
	 * @parameter
	 */
	protected Resource cssResource;

	/**
	 * The directory containing local fonts
	 *
	 * @parameter expression="${basedir}/src/main/fonts"
	 */
	protected File fontsDirectory;

	/**
	 * The directory where the output will be written.
	 *
	 * @parameter expression="${basedir}/target/docbook/publish"
	 */
	protected File publishDirectory;

	/**
	 * The directory where we can perform some staging staging occurs.  Mainly
	 * this is used for (1) image/css staging; (2) font staging.
	 *
	 * @parameter expression="${basedir}/target/docbook/staging"
	 * @required
	 * @readonly
	 */
	protected File stagingDirectory;

	/**
	 * A directory used for general transient work.
	 *
	 * @parameter expression="${basedir}/target/docbook/work"
	 * @required
	 * @readonly
	 */
	protected File workDirectory;

	/**
	 * The formats in which to perform rendering.
	 *
     * @parameter
	 * @required
	 */
	protected Format[] formats;

	/**
	 * Whether or not to perform the attching of the format
	 * outputs as classified attachments.
	 *
     * @parameter
	 */
	protected boolean attach = true;

	/**
	 * Configurable options
	 *
     * @parameter
	 */
	protected Options options;


	// translation-specific config setting ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * The locale of the master translation.
	 *
	 * @parameter default-value="en-US"
	 */
	protected String masterTranslation;

	/**
	 * The path (relative to the sourceDirectory) to the directory containing the master sources.  By default the
	 * master-translation's locale (en-US) will be used if such a directory exists <b>if translations are specified</b>.
	 * Users can explicitly specify such a relative path here.
	 *
	 * @parameter default-value=""
	 */
	protected String relativeMasterTranslationPath;

	/**
	 * The directory for POT translations files.
	 *
	 * @parameter expression="${basedir}/src/main/docbook/pot"
	 */
	protected File potDirectory;

	/**
	 * The locales of all non-master translations.
	 *
	 * @parameter
	 */
	protected String[] translations;

	/**
	 * The directory which contains the translations.  The assumed strategy here is that each translation would
	 * have a directory under the directory named here which would contain the PO sources for that particular
	 * language translation.  The default here is to use sourceDirectory itself.
	 *
	 * @parameter
	 */
	protected File translationBaseDirectory;

	private File resolvedMasterSourceDirectory;

	/**
	 * The override method to perform the actual processing of the
	 * mojo.
	 * 
	 * @param formatPlans The format plans
	 * @throws RenderingException Indicates problem performing rendering
	 * @throws XSLTException Indicates problem building or executing XSLT transformer
	 */
	protected void process(FormatPlan[] formatPlans) throws JDocBookProcessException {
	}

	protected void doExecute() throws JDocBookProcessException {
		process( determineFormatPlans() );
	}

	public final void execute() throws MojoExecutionException, MojoFailureException {
		if ( options == null ) {
			options = new Options();
		}
		options.setLog( new MavenLogBridge() );

		if ( translationBaseDirectory == null ) {
			translationBaseDirectory = sourceDirectory;
		}

		if ( translations == null ) {
			translations = new String[0];
		}

		if ( translations.length != 0 ) {
			if ( relativeMasterTranslationPath == null || "".equals( relativeMasterTranslationPath ) ) {
				// see if we have a directory named according to the master translation within the sourceDorectory...
				File test = new File( sourceDirectory, masterTranslation );
				if ( test.exists() ) {
					relativeMasterTranslationPath = masterTranslation;
				}
			}
		}
		resolvedMasterSourceDirectory = sourceDirectory;
		if ( relativeMasterTranslationPath != null && !"".equals( relativeMasterTranslationPath ) ) {
			resolvedMasterSourceDirectory = new File( resolvedMasterSourceDirectory, relativeMasterTranslationPath );
		}

		try {
			doExecute();
		}
		catch ( XSLTException e ) {
			throw new MojoExecutionException( "XSLT problem", e );
		}
		catch ( RenderingException e ) {
			throw new MojoExecutionException( "Rendering problem", e );
		}
	}

	private FormatPlan[] determineFormatPlans() {
		FormatPlan[] plans = new FormatPlan[ formats.length ];
		for ( int i = 0; i < formats.length; i++ ) {
			plans[i] = new FormatPlan( formats[i], StandardDocBookFormatDescriptors.getDescriptor( formats[i].getFormatName() ) );
		}
		return plans;
	}

	protected Locale getRequestedLocale() {
		String requestedLocaleStr = project.getProperties().getProperty( "jdocbook.lang" );
		Locale requestedLocale = requestedLocaleStr == null ? null : parseLocale( requestedLocaleStr );
		if ( requestedLocale != null ) {
			getLog().info( "requested processing limited to [" + stringify( requestedLocale ) + "] lang" ) ;
		}
		return requestedLocale;
	}

	protected MasterTranslationDescriptor getMasterTranslationDescriptor() {
		return new MasterTranslationDescriptor( parseLocale( masterTranslation ) );
	}

	protected List<PublishingSource> getPublishingSources() {
		ArrayList<PublishingSource> descriptors = new ArrayList<PublishingSource>();
		descriptors.add( getMasterTranslationDescriptor() );

		for ( String locale : translations ) {
			descriptors.add( new TranslationDescriptor( parseLocale( locale ) ) );
		}

		return descriptors;
	}

	protected List<I18nSource> getI18nSources() {
		ArrayList<I18nSource> descriptors = new ArrayList<I18nSource>();
		for ( String locale : translations ) {
			descriptors.add( new TranslationDescriptor( parseLocale( locale ) ) );
		}
		return descriptors;
	}

	protected static interface ArtifactProcessor {
		public void process(Artifact artifact);
	}

	protected void processArtifacts(ArtifactProcessor processor) {
		processProjectArtifacts( processor );
		processPluginArtifacts( processor );
	}

	protected void processProjectArtifacts(ArtifactProcessor processor) {
		processArtifacts( processor, projectArtifacts );
	}

	protected void processPluginArtifacts(ArtifactProcessor processor) {
		processArtifacts( processor, pluginArtifacts );
	}

	private void processArtifacts(ArtifactProcessor processor, Collection artifacts) {
		for ( Object artifact : artifacts ) {
			processor.process( ( Artifact ) artifact );
		}
	}

	protected Locale parseLocale(String locale) {
		return LocaleUtils.parse( locale, options.getLocaleSeparator() );
	}

	protected String stringify(Locale locale) {
		return LocaleUtils.render( locale, options.getLocaleSeparator() );
	}

	protected static interface PublishingSource {
		public Locale getLocale();
		public File resolveDocumentFile();
		public File resolvePublishingDirectory();
	}

	protected static interface I18nSource {
		public Locale getLocale();
		public File resolvePoDirectory();
		public File resolveTranslatedXmlDirectory();
	}

	protected class MasterTranslationDescriptor implements PublishingSource {
		private final Locale locale;

		public MasterTranslationDescriptor(Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return locale;
		}

		public File resolveDocumentFile() {
			return new File( resolvedMasterSourceDirectory, sourceDocumentName );
		}

		public File resolvePublishingDirectory() {
			return new File( publishDirectory, stringify( locale ) );
		}
	}

	protected class TranslationDescriptor implements PublishingSource, I18nSource {
		private final Locale locale;

		public TranslationDescriptor(Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return locale;
		}

		public File resolvePoDirectory() {
			return new File( translationBaseDirectory, stringify( locale ) );
		}

		public File resolveTranslatedXmlDirectory() {
			return new File( new File( workDirectory, "xml" ), stringify( locale ) );
		}

		public File resolveDocumentFile() {
			return new File( resolveTranslatedXmlDirectory(), sourceDocumentName );
		}

		public File resolvePublishingDirectory() {
			return new File( publishDirectory, stringify( locale ) );
		}
	}

	private class MavenLogBridge implements Log {
		public void trace(String message) {
			getLog().debug( message );
		}

		public void trace(String message, Object... args) {
			if ( getLog().isDebugEnabled() ) {
				getLog().debug( MessageFormat.format( message, args ) );
			}
		}

		public void info(String message) {
			getLog().info( message );
		}

		public void info(String message, Object... args) {
			if ( getLog().isInfoEnabled() ) {
				getLog().info( MessageFormat.format( message, args ) );
			}
		}

		public void info(String message, Throwable exception) {
			getLog().info( message, exception );
		}

		public void info(String message, Throwable exception, Object... args) {
			if ( getLog().isInfoEnabled() ) {
				getLog().info( MessageFormat.format( message, args ), exception );
			}
		}

		public void error(String message) {
			getLog().error( message );
		}

		public void error(String message, Object... args) {
			if ( getLog().isErrorEnabled() ) {
				getLog().error( MessageFormat.format( message, args ) );
			}
		}

		public void error(String message, Throwable exception) {
			getLog().error( message, exception );
		}

		public void error(String message, Throwable exception, Object... args) {
			if ( getLog().isErrorEnabled() ) {
				getLog().error( MessageFormat.format( message, args ), exception );
			}
		}
	}
}
