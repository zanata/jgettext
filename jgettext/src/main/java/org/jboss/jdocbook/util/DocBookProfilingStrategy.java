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
package org.jboss.jdocbook.util;

/**
 * Enumeration of the different strategies defined by the DocBook reference manual for applying profiling.
 *
 * @author Steve Ebersole
 */
public class DocBookProfilingStrategy {
	public static final DocBookProfilingStrategy NONE = new DocBookProfilingStrategy( "none" );
	public static final DocBookProfilingStrategy SINGLE_PASS = new DocBookProfilingStrategy( "single_pass" );
	public static final DocBookProfilingStrategy TWO_PASS = new DocBookProfilingStrategy( "two_pass" );

	private final String name;

	public DocBookProfilingStrategy(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static DocBookProfilingStrategy parse(String text) {
		if ( SINGLE_PASS.name.equalsIgnoreCase( text ) ) {
			return SINGLE_PASS;
		}
		else if ( TWO_PASS.name.equalsIgnoreCase( text ) ) {
			return TWO_PASS;
		}
		else {
			// default...
			return NONE;
		}
	}
}
