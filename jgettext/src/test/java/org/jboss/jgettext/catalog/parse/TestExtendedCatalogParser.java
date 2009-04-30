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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.junit.Test;

/**
 * TestExtendedCatalogParser implementation
 *
 * @author Steve Ebersole
 */
public class TestExtendedCatalogParser{
	
	@Test
	public void testBasic() throws Throwable {
		File poFile = new File( getClass().getResource( "/valid/sample.po" ).getFile() );
		ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
		parser.catalog();
		Catalog catalog = parser.getCatalog();
		
		int entryCount = 0;
		int obsoleteCount = 0;
		for(Message m : catalog){
			entryCount++;
			if(m.isObsolete()) obsoleteCount++;
		}
		assertNotNull( catalog.locateHeader() );
		assertEquals( 7, entryCount );
		assertEquals( 3, obsoleteCount );
	}

	@Test
	public void testObsoleteEntries() throws Throwable {
		File poFile = new File( getClass().getResource( "/valid/obsolete.po" ).getFile() );
		ExtendedCatalogParser parser = new ExtendedCatalogParser( poFile );
		parser.catalog();
		Catalog catalog = parser.getCatalog();

		int entryCount = 0;
		int obsoleteCount = 0;
		for(Message m : catalog){
			entryCount++;
			if(m.isObsolete()) obsoleteCount++;
		}

		assertNotNull( catalog.locateHeader() );
		assertEquals( 5, entryCount );
		assertEquals( 4, obsoleteCount ); // - header...
	}

	@Test
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
}
