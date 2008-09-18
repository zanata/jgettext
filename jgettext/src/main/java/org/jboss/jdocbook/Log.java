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
package org.jboss.jdocbook;

/**
 * A general 'log bridge' contract.
 *
 * @author Steve Ebersole
 */
public interface Log {
	public void trace(String message);
	public void trace(String message, Object... args);

	public void info(String message);
	public void info(String message, Object... args);
	public void info(String message, Throwable exception);
	public void info(String message, Throwable exception, Object... args);

	public void error(String message);
	public void error(String message, Object... args);
	public void error(String message, Throwable exception);
	public void error(String message, Throwable exception, Object... args);
}
