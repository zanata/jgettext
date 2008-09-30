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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jboss.jgettext.Catalog;
import org.jboss.jgettext.Message;

/**
 * CatalogWriter implementation
 *
 * @author Steve Ebersole
 */
public class CatalogWriter {
	protected final Catalog catalog;

	public CatalogWriter(Catalog catalog) {
		this.catalog = catalog;
	}

	public void writeTo(File file) throws IOException {
		writeTo( new MessageProcessor( catalog.locateHeader(), new FileWriter( file ) ) );
	}

	void writeTo(MessageProcessor processor) {
		final Message existingHeader = catalog.locateHeader();
		if ( existingHeader == null ) {
			processor.writeMessage( generateHeader() );
		}
		else {
			processor.writeMessage( existingHeader );
		}

		catalog.processMessages( processor );
	}

	private Message generateHeader() {
		Message header = new Message();
		header.setMsgid( "" );
		header.setMsgstr( "" );
		header.addComment( "SOME DESCRIPTIVE TITLE." );
		header.addComment( "FIRST AUTHOR <EMAIL@ADDRESS>, YEAR." );
		header.addComment( "" );
		header.markFuzzy();

		return header;
	}
}
