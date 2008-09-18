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
package org.jboss.jdocbook.i18n.actions.process;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Responsible for performing system call executions and coordinating error checking, process I/O, etc.
 *
 * @author Steve Ebersole
 */
public class Executor {
	/**
	 * Shorthand for {@link #execute(String, java.io.OutputStream, java.io.OutputStream)} using
	 * {@link System#out} and {@link System#err} for the process's stdout and errout, respectively.
	 *
	 * @param cmd The command to execute.
	 */
	public static void execute(String cmd) {
		execute( cmd, System.out, System.err );
	}

	/**
	 * Shorthand for {@link #execute(String, java.io.OutputStream, java.io.OutputStream)} using
	 * {@link System#err} for the process's errout and the given <tt>out</tt> for its stdout.
	 *
	 * @param cmd The command to execute.
	 * @param out The stream to which to pipe the process's stdout.
	 */
	public static void execute(String cmd, OutputStream out) {
		execute( cmd, out, System.err );
	}

	/**
	 * Perform the given cmd as a System call, piping the {@link Process#getOutputStream() input} of the resulting
	 * process to our {@link System#in}, its {@link Process#getOutputStream() stdout} to the given <tt>out</tt> stream
	 * and its {@link Process#getOutputStream() errout} to the given <tt>err</tt> stream.
	 *
	 * @param cmd The command to execute.
	 * @param out The stream to which to pipe the process's stdout.
	 * @param err The stream to which to pipe the process's errout.
	 */
	public static void execute(String cmd, OutputStream out, OutputStream err) {
		ProcessOutputManager outputManager = null;
		try {
			Process cmdProcess = Runtime.getRuntime().exec( cmd );
			outputManager = new ProcessOutputManager( cmdProcess, out, err );
			try {
				cmdProcess.waitFor();
			}
			catch ( InterruptedException e ) {
				throw new ExecutionException( "unable to obtain appropriate runtime environment", e );
			}
			if ( cmdProcess.exitValue() != 0 ) {
				// assume problem...
				throw new ExecutionException( "there was a problem executing command; check output" );
			}
		}
		catch ( IOException e ) {
			throw new ExecutionException( "unable to obtain appropriate runtime environment", e );
		}
		finally {
			if ( outputManager != null ) {
				outputManager.stop();
			}
		}
	}
}
