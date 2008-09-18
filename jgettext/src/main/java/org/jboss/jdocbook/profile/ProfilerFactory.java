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
package org.jboss.jdocbook.profile;

import java.io.File;

import org.jboss.jdocbook.Options;

/**
 * A factory for {@link Profiler} instances.
 *
 * @author Steve Ebersole
 */
public class ProfilerFactory {

	private final File outputDirectory;
	private final Options options;

	/**
	 * Constructs a factory capable of producing {@link Profiler} instances writing to the given directory.
	 *
	 * @param outputDirectory The directory where profiling output should be written.
	 * @param options The options.
	 */
	public ProfilerFactory(File outputDirectory, Options options) {
		this.options = options;
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Builds a profiler.
	 *
	 * @return The profiler.
	 */
	public Profiler buildProfiler() {
		return new ProfilerImpl( outputDirectory, options );
	}

}
