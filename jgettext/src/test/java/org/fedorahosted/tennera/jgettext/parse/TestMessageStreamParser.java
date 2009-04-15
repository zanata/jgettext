package org.fedorahosted.tennera.jgettext.parse;

import static org.junit.Assert.*;

import java.io.File;

import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.junit.Before;
import org.junit.Test;

public class TestMessageStreamParser {

	File poFile;
	
	@Before
	public void setup(){
		poFile = new File( getClass().getResource( "/valid/sample.po" ).getFile() );
	}
	
	@Test
	public void testIteratingThroughABasicFile() throws Throwable {
		Message message;
		MessageStreamParser parser = new MessageStreamParser(poFile);
		
		assertTrue(parser.hasNext());
		message = parser.next();
		assertTrue(message.isHeader());
		assertTrue(message.isFuzzy());
		
		assertTrue(parser.hasNext());
		message = parser.next();
		assertFalse(message.isHeader());
		assertFalse(message.isFuzzy());
		
		parser.next();
		parser.next();
		
		message = parser.next();
		assertTrue(message.isObsolete());
		
		parser.next();
		parser.next(); // last message
		
		assertFalse(parser.hasNext());
	}
	
}
