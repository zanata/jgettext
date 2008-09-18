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
package org.jboss.maven.shared.properties;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.File;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.introspection.ReflectionValueExtractor;

/**
 * Presents an consolidated, aggregated view of project properties.
 * <p/>
 * Aggregation occurs across {@link System#getProperties()},
 * {@link org.apache.maven.project.MavenProject#getProperties()} and
 * via bean-propetties-style reflection on {@link org.apache.maven.project.MavenProject}.
 *
 * @author Steve Ebersole
 */
public class CompositeMavenProjectProperties extends AbstractMap {
	private final MavenProject project;
	private final Map values;

	@SuppressWarnings("unchecked")
	public CompositeMavenProjectProperties(MavenProject project) {
		this.project = project;
		values = new HashMap( System.getProperties() );
		values.putAll( project.getProperties() );
		for ( Object o : project.getBuild().getFilters() ) {
			String filtersfile = ( String ) o;
			values.putAll( PropertiesHelper.loadPropertyFile( new File( filtersfile ) ) );
		}
	}

	public synchronized Object get(Object key) {
		// try the local value map first...
		Object value = values.get( key );

		// then try reflection on the project bean properties...
		if ( value == null ) {
			try {
				value = ReflectionValueExtractor.evaluate( String.valueOf( key ), project );
			}
			catch( Throwable ignore ) {
				// intentionally empty...
			}
		}
		return value;
	}

	public Set entrySet() {
		throw new UnsupportedOperationException( "iterating MavenProject properties is not supported" );
	}
}
