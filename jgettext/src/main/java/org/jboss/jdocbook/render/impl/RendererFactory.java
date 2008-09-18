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

import org.jboss.jdocbook.Options;
import org.jboss.jdocbook.render.format.StandardDocBookFormatDescriptors;
import org.jboss.jdocbook.render.Renderer;

/**
 * A factory for building {@link Renderer} instances.
 *
 * @author Steve Ebersole
 */
public class RendererFactory {
	private final Options options;

	public RendererFactory(Options options) {
		this.options = options;
	}

	/**
	 * Build an appropriate renderer for the given <tt>formatName</tt>
	 *
	 * @param formatName The name of the formatting in which we want to perform rendering.
	 * @return The renderer.
	 */
	public Renderer buildRenderer(String formatName) {
		if ( formatName.equals( StandardDocBookFormatDescriptors.PDF.getName() ) ) {
			return new PDFRenderer( options );
		}
		else {
			return new BasicRenderer( options );
		}
	}
}
