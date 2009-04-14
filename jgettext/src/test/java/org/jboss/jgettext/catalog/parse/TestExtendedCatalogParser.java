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
 *
 * Red Hat Author(s): Steve Ebersole
 */
package org.jboss.jgettext.catalog.parse;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.fedorahosted.tennera.jgettext.catalog.write.MessageProcessor;
import org.fedorahosted.tennera.jgettext.util.NoOpWriter;

/**
 * TestExtendedCatalogParser implementation
 *
 * @author Steve Ebersole
 */
public class TestExtendedCatalogParser extends TestCase {
	public void testBasic() throws Throwable {
		File poFile = new File( getClass().getResource( "/valid/sample.po" ).getFile() );
		ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
		parser.catalog();
		Catalog catalog = parser.getCatalog();
		LocalProcessor processor = new LocalProcessor();
		catalog.processMessages( processor );

		assertNotNull( catalog.locateHeader() );
		assertEquals( 7, processor.entryCount );
		assertEquals( 3, processor.obsoleteCount );
	}

	public void testObsoleteEntries() throws Throwable {
		File poFile = new File( getClass().getResource( "/valid/obsolete.po" ).getFile() );
		ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
		parser.catalog();
		Catalog catalog = parser.getCatalog();
		LocalProcessor processor = new LocalProcessor();
		catalog.processMessages( processor );

		assertNotNull( catalog.locateHeader() );
		assertEquals( 5, processor.entryCount );
		assertEquals( 4, processor.obsoleteCount ); // - header...
	}

	public void testPartialObsoleteEntries() throws Throwable {
		File poFile = new File( getClass().getResource( "/invalid/mixed_up_obsolete.po" ).getFile() );
		ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
		try {
			parser.catalog();
			fail( "was expecting exception" );
		}
		catch ( ParseException expected ) {
		}
	}

	public static class LocalProcessor extends MessageProcessor {
		private int entryCount;
		private int obsoleteCount;

		public LocalProcessor() {
			super( new NoOpWriter() );
		}

		protected void messageStart(Message message) throws IOException {
			entryCount++;
			if ( message.isObsolete() ) {
				obsoleteCount++;
			}
			writer.write( "-----------------------------------------------------------------------------------\n" );
		}

		protected void messageEnd(Message message) throws IOException {
			writer.write( "-----------------------------------------------------------------------------------\n" );
		}
	}
}
