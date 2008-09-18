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
package org.jboss.jgettext.catalog.write;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.jboss.jgettext.Catalog;
import org.jboss.jgettext.Message;
import org.jboss.jgettext.Occurence;

/**
 * MessageProcessor implementation
 *
 * @author Steve Ebersole
 */
public class MessageProcessor implements Catalog.MessageProcessor {
	protected final Writer writer;
	protected final Message header; // header handled specially...

	public MessageProcessor(Message header, Writer writer) {
		this.writer = writer;
		this.header = header;
	}

	public MessageProcessor(Writer writer) {
		this( null, writer );
	}

	public MessageProcessor() {
		this( new OutputStreamWriter( System.out ) );
	}

	public void processMessage(Message message) {
		if ( message == header ) {
			return;
		}

		try {
			messageStart( message );

			for ( String comment : message.getComments() ) {
				writer.write( "#" + comment ); // no space on purpose!!!
				writer.write( '\n' );
			}

			for ( String comment : message.getExtractedComments() ) {
				writer.write( "#. " + comment );
				writer.write( '\n' );
			}

			for ( Occurence occurence : message.getOccurences() ) {
				writer.write( "#: " + occurence.toString() );
				writer.write( '\n' );
			}

			if ( message.isFuzzy() ) {
				writer.write( "#, fuzzy" );
				writer.write( '\n' );
			}

			for ( String format : message.getFormats() ) {
				writer.write( "#, " + format );
				writer.write( '\n' );
			}

			if ( message.getPrevMsgctx() != null ) {
				writeMsgctxt( "#| ", message.getPrevMsgctx() );
			}

			if ( message.getPrevMsgid() != null ) {
				writeMsgid( "#| ", message.getPrevMsgid() );
			}

			if ( message.getPrevMsgidPlural() != null ) {
				writeMsgidPlural( "#| ", message.getPrevMsgidPlural() );
			}

			String prefix = message.isObsolete() ? "#~ " : "";
			if ( message.getMsgctxt() != null ) {
				writeMsgctxt( prefix, message.getMsgctxt() );
			}

			writeMsgid( prefix, message.getMsgid() );

			if ( message.getMsgidPlural() != null ) {
				writeMsgidPlural( prefix, message.getMsgidPlural() );
			}

			writeMsgstr( prefix, message.getMsgstr() );

			writeMsgstrPlurals( prefix, message.getMsgstrPlural() );

			messageEnd( message );

			writer.flush();
		}
		catch ( IOException e ) {
			throw new RuntimeException( "Problem writing message : " + e.getMessage(), e );
		}

	}

	protected void messageStart(Message message) throws IOException {
	}

	protected void messageEnd(Message message) throws IOException {
	}

	private void writeMsgctxt(String prefix, String ctxt) throws IOException {
		writer.write( prefix + "msgctxt \"" + ctxt + "\"");
		writer.write( '\n' );
	}

	private void writeMsgid(String prefix, String msgid) throws IOException {
		writer.write( prefix + "msgid \"" + msgid + "\"");
		writer.write( '\n' );
	}

	private void writeMsgidPlural(String prefix, String msgidPlural) throws IOException {
		writer.write( prefix + "msgid_plural \"" + msgidPlural + "\"");
		writer.write( '\n' );
	}

	private void writeMsgstr(String prefix, String msgstr) throws IOException {
		if ( msgstr == null ) {
			msgstr = "";
		}
		writer.write( prefix + "msgstr \"" + msgstr + "\"");
		writer.write( '\n' );
	}

	private void writeMsgstrPlurals(String prefix, List<String> msgstrPlurals) throws IOException {
		int i = 0;
		for ( String msgstr : msgstrPlurals ) {
			writer.write( prefix + "msgstr[" + i + "] \"" +  msgstr + "\"" );
			writer.write( '\n' );
			i++;
		}
	}
}
