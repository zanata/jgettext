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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Bridges an input stream to an output stream.
 * <p/>
 * This is used in system call handling because the output of the created process is actually represented as an input
 * stream to our java process.  Generally we need to 'pipe' this to an output stream (the console for example).
 *
 * @author Steve Ebersole
 */
public class StreamBridge implements Runnable {
	private static final int DEF_BUFFER_SIZE = 512;

	private final InputStream inputStream;
	private final OutputStream outputStream;
    private final int bufferSize;

	private boolean stopped = false;

	public StreamBridge(InputStream inputStream, OutputStream outputStream) {
		this( inputStream, outputStream, DEF_BUFFER_SIZE );
	}

	public StreamBridge(InputStream inputStream, OutputStream outputStream, int bufferSize) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.bufferSize = bufferSize;
	}

	public void run() {
        final byte[] buf = new byte[bufferSize];
        int length;
        try {
            while ( true ) {
                length = inputStream.read( buf );
                if ( ( length <= 0 ) || stopped ) {
                    break;
                }
                outputStream.write( buf, 0, length );
				outputStream.flush();
            }
			outputStream.flush();
        }
		catch ( IOException ignore ) {
			// ???
		}
		finally {
            synchronized (this) {
                notifyAll();
            }
        }
	}

	synchronized void stop() {
        stopped = true;
        notifyAll();
    }

}
