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
package org.jboss.maven.shared.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jboss.maven.shared.properties.CompositeMavenProjectProperties;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.InterpolationFilterReader;

/**
 * A delegate for handling {@link Resource} resolution.
 *
 * @author Steve Ebersole
 */
public class ResourceDelegate {
	public static final String[] DEFAULT_DEFAULT_INCLUDES = new String[] { "**/**" };

	private final File basedir;
	private final File baseTargetDirectory;
	private final String[] defaultIncludes;
	private final String[] defaultExcludes;
	private final Log log;

	private final Map filterProperties;

	/**
	 * Constructs a Delegate instance for handling Resource resolution.
	 *
	 * @param project The project currently being built.
	 * @param baseTargetDirectory The base target directory to which we should copy resources.
	 * @param log The log instance to use for logging.
	 */
	public ResourceDelegate(MavenProject project, File baseTargetDirectory, Log log) {
		this( project, baseTargetDirectory, DEFAULT_DEFAULT_INCLUDES, null, log );
	}

	/**
	 * Constructs a Delegate instance for handling Resource resolution.
	 *
	 * @param project The project currently being built.
	 * @param baseTargetDirectory The base target directory to which we should copy resources.
	 * @param defaultIncludes default patterns for resource copying inclusion.
	 * @param defaultExcludes default patterns for resource copying exclusion.
	 * @param log The log instance to use for logging.
	 */
	public ResourceDelegate(
			MavenProject project,
			File baseTargetDirectory,
			String[] defaultIncludes,
			String[] defaultExcludes,
			Log log) {
		this.basedir = project.getBasedir();
		this.baseTargetDirectory = baseTargetDirectory;
		this.defaultIncludes = defaultIncludes;
		this.defaultExcludes = defaultExcludes;
		this.log = log;
		this.filterProperties = new CompositeMavenProjectProperties( project );
	}

	public void process(Resource[] resources) throws ResourceException {
		for ( Resource resource : resources ) {
			process( resource );
		}
	}

	public void process(Resource resource) throws ResourceException {
		getLog().debug( "starting resource processing for : " + resource.getDirectory() );
		String[] fileNames = collectFileNames( resource );
		if ( fileNames == null ) {
			getLog().debug( "no matching files found" );
			return;
		}

		File destination = resource.getTargetPath() == null
				? baseTargetDirectory
				: new File( baseTargetDirectory, resource.getTargetPath() );
		if ( !destination.exists() ) {
			destination.mkdirs();
		}

		for ( String fileName : fileNames ) {
			if ( resource.isFiltering() ) {
				copyFilteredFile(
						new File( resource.getDirectory(), fileName ),
						new File( destination, fileName ),
						null,
						getFilterWrappers(),
						filterProperties
				);
			}
			else {
				copyFileIfModified(
						new File( resource.getDirectory(), fileName ),
						new File( destination, fileName )
				);
			}
		}
	}

	/**
	 * Given a resource, determine the matching file names which should be
	 * processed.
	 *
	 * @param resource The resource model.
	 * @return The collected matching file names.
	 */
	private String[] collectFileNames(Resource resource) {
		File resourceDirectory = new File( resource.getDirectory() );
		if ( !resourceDirectory.exists() && !resource.getDirectory().startsWith( "/" ) ) {
			resourceDirectory = new File( basedir, resource.getDirectory() );
			if ( !resourceDirectory.exists() ) {
				resourceDirectory = null;
			}
		}
		if ( resourceDirectory == null ) {
			throw new ResourceException( "could not locate specified resource directory" );
		}

		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir( resource.getDirectory() );
		scanner.setIncludes( determineIncludes( resource ) );
		scanner.setExcludes( determineExcludes( resource ) );
		scanner.addDefaultExcludes();
		scanner.scan();
		return scanner.getIncludedFiles();
	}

	@SuppressWarnings("unchecked")
	private String[] determineIncludes(Resource resource) {
		return toStringArray( resource.getIncludes(), defaultIncludes );
	}

	@SuppressWarnings("unchecked")
	private String[] determineExcludes(Resource resource) {
		return toStringArray( resource.getExcludes(), defaultExcludes );
	}


	private interface FilterWrapper {
		Reader getReader(Reader fileReader, Map filterProperties);
	}

	private FilterWrapper[] getFilterWrappers() {
		return new FilterWrapper[] {
				// support ${token}
				new FilterWrapper() {
					public Reader getReader(Reader fileReader, Map filterProperties) {
						return new InterpolationFilterReader(
								fileReader, filterProperties, "${", "}"
						);
					}
				},
				// support @token@
				new FilterWrapper() {
					public Reader getReader(Reader fileReader, Map filterProperties) {
						return new InterpolationFilterReader(
								fileReader, filterProperties, "@", "@"
						);
					}
				}
		};
	}

	private static void copyFilteredFile(
			File from,
			File to,
			String encoding,
			FilterWrapper[] wrappers,
			Map filterProperties) throws ResourceException {
		// buffer so it isn't reading a byte at a time!
		Reader fileReader = null;
		Writer fileWriter = null;
		try {
			// fix for MWAR-36, ensures that the parent dir are created first
			to.getParentFile().mkdirs();

			if ( encoding == null || encoding.length() < 1 ) {
				fileReader = new BufferedReader( new FileReader( from ) );
				fileWriter = new FileWriter( to );
			}
			else {
				FileInputStream instream = new FileInputStream( from );
				FileOutputStream outstream = new FileOutputStream( to );
				fileReader = new BufferedReader(
						new InputStreamReader( instream, encoding )
				);
				fileWriter = new OutputStreamWriter( outstream, encoding );
			}

			Reader reader = fileReader;
			for ( FilterWrapper wrapper : wrappers ) {
				reader = wrapper.getReader( reader, filterProperties );
			}

			IOUtil.copy( reader, fileWriter );
		}
		catch( IOException e ) {
			throw new ResourceException( e.getMessage(), e );
		}
		finally {
			IOUtil.close( fileReader );
			IOUtil.close( fileWriter );
		}
	}

	private static void copyFileIfModified(File source, File destination) throws ResourceException {
		if ( destination.lastModified() < source.lastModified() ) {
			try {
				FileUtils.copyFile( source.getCanonicalFile(), destination );
				destination.setLastModified( source.lastModified() );
			}
			catch ( IOException e ) {
				throw new ResourceException( e.getMessage(), e );
			}
		}
	}

	private Log getLog() {
		return log;
	}

	private static String[] toStringArray(List<String> list, String[] defaultArray) {
		if ( list == null || list.isEmpty() ) {
			return defaultArray;
		}
		else {
			return list.toArray( new String[ list.size() ] );
		}
	}
}
