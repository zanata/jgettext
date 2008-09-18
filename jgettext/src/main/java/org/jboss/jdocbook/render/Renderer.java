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
package org.jboss.jdocbook.render;

import java.io.File;

import org.jboss.jdocbook.render.format.FormatPlan;
import org.jboss.jdocbook.xslt.XSLTException;

/**
 * Renderer contract
 *
 * @author Steve Ebersole
 */
public interface Renderer {

	/**
	 * Performs the actual rendering or transforming of the DocBook sources into
	 * the respective output format.
	 *
	 * @param source The source DocBook file.
	 * @param plan The formatting plan.
	 * @param renderingDirectory The directory into which to render
	 * @param stagingDirectory The directory where images resources were staged
	 * @throws RenderingException Problem writing the output file(s).
	 * @throws XSLTException Problem performing XSL transformation.
	 */
	public void render(File source, FormatPlan plan, File renderingDirectory, File stagingDirectory) throws RenderingException, XSLTException;

	public File getAttachableBundle(File source);
}
