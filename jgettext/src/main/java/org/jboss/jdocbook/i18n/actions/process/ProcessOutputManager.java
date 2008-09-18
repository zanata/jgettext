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

import java.io.OutputStream;

/**
 * ProcessOutputManager implementation
 *
 * @author Steve Ebersole
 */
public class ProcessOutputManager {
	private final StreamBridge outputBridge;
	private StreamBridge errorBridge;

	public ProcessOutputManager(Process process, OutputStream out, OutputStream err) {
		outputBridge = new StreamBridge( process.getInputStream(), out );
		errorBridge = new StreamBridge( process.getErrorStream(), err );

		Thread outputStreamThread = new Thread( outputBridge );
		outputStreamThread.setDaemon( true );
		outputStreamThread.start();

		Thread errorStreamThread = new Thread( errorBridge );
		errorStreamThread.setDaemon( true );
		errorStreamThread.start();
	}

	public void stop() {
		outputBridge.stop();
		errorBridge.stop();
	}
}
