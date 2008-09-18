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

import java.util.Properties;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

import org.codehaus.plexus.util.IOUtil;

/**
 * Utilities for dealing with @{link Properties} objects.
 * <p/>
 * Taken from maven-war-plugin, which apparently took it from
 * maven-resources-plugin... ;)
 *
 * @author Steve Ebersole
 */
public class PropertiesHelper {

	/**
	 * Disallow external instantiation of PropertiesHelper.
	 */
	private PropertiesHelper() {
	}

	/**
	 * Reads a property file, resolving all internal variables.
	 *
	 * @param propfile The property file to load
	 *
	 * @return the loaded and fully resolved Properties object
	 */
	public static Properties loadPropertyFile(File propfile) {
		if ( !propfile.exists() ) {
			throw new PropertiesException( "unable to locate spercified prop file [" + propfile.toString() + "]" );
		}

		Properties props = new Properties();
		if ( propfile.exists() ) {
			try {
				FileInputStream inStream = new FileInputStream( propfile );
				try {
					props.load( inStream );
				}
				finally {
					IOUtil.close( inStream );
				}
			}
			catch( IOException ioe ) {
				throw new PropertiesException( "unable to load properties file [" + propfile + "]" );
			}
		}

		for ( Enumeration n = props.propertyNames(); n.hasMoreElements(); ) {
			String k = ( String ) n.nextElement();
			props.setProperty( k, PropertiesHelper.getInterpolatedPropertyValue( k, props ) );
		}

		return props;
	}


	/**
	 * Retrieves a property value, replacing values like ${token}
	 * using the Properties to look them up.
	 * <p/>
	 * It will leave unresolved properties alone, trying for System
	 * properties, and implements reparsing (in the case that
	 * the value of a property contains a key), and will
	 * not loop endlessly on a pair like
	 * test = ${test}.
	 *
	 * @param key The key for which to find the corresponding value
	 * @param props The properties from which to find the value.
	 * @return The (possible interpolated) property value
	 */
	public static String getInterpolatedPropertyValue(String key, Properties props) {
		// This can also be done using InterpolationFilterReader,
		// but it requires reparsing the file over and over until
		// it doesn't change.

		String v = props.getProperty( key );
		String ret = "";
		int idx, idx2;

		while ( ( idx = v.indexOf( "${" ) ) >= 0 ) {
			// append prefix to result
			ret += v.substring( 0, idx );

			// strip prefix from original
			v = v.substring( idx + 2 );

			// if no matching } then bail
			if ( ( idx2 = v.indexOf( '}' ) ) < 0 ) {
				break;
			}

			// strip out the key and resolve it
			// resolve the key/value for the ${statement}
			String nk = v.substring( 0, idx2 );
			v = v.substring( idx2 + 1 );
			String nv = props.getProperty( nk );

			// try global environment..
			if ( nv == null ) {
				nv = System.getProperty( nk );
			}

			// if the key cannot be resolved,
			// leave it alone ( and don't parse again )
			// else prefix the original string with the
			// resolved property ( so it can be parsed further )
			// taking recursion into account.
			if ( nv == null || nv.equals( key ) ) {
				ret += "${" + nk + "}";
			}
			else {
				v = nv + v;
			}
		}
		return ret + v;
	}
}
