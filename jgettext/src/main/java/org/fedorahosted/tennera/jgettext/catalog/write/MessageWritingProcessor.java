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
package org.fedorahosted.tennera.jgettext.catalog.write;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.MessageProcessor;
import org.fedorahosted.tennera.jgettext.PoWriter;

/**
 * MessageProcessor implementation
 *
 * @author Steve Ebersole
 */
@Deprecated
public class MessageWritingProcessor implements MessageProcessor {
	protected final Writer writer;
	protected final Message header; // header handled specially...
	protected final PoWriter poWriter;

	public MessageWritingProcessor(Message header, Writer writer) {
		this.writer = writer;
		this.header = header;
		poWriter = new PoWriter();
	}

	public MessageWritingProcessor(Writer writer) {
		this( null, writer );
	}

	public MessageWritingProcessor() {
		this( new OutputStreamWriter( System.out ) );
	}

	public void processMessage(Message message) {
		if ( message == header ) {
			return;
		}
		try{
			poWriter.write(message, writer);
		}
		catch(IOException e){}
	}

	public void writeMessage(Message message) {
		try{
			poWriter.write(message, writer);
		}
		catch(IOException e){}
	}
}
