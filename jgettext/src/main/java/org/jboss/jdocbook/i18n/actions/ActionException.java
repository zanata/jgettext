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
package org.jboss.jdocbook.i18n.actions;

import org.jboss.jdocbook.JDocBookProcessException;

/**
 * Used to indicate an error performing an {@link org.jboss.jdocbook.Action}.
 *
 * @author Steve Ebersole
 */
public class ActionException extends JDocBookProcessException {
	private static final long serialVersionUID = 5590768273066409640L;

	public ActionException(String message) {
		super( message );
	}

	public ActionException(String message, Throwable cause) {
		super( message, cause );
	}
}
