package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.fail;

import java.io.File;

import org.fedorahosted.tennera.jgettext.catalog.parse.UnexpectedTokenException;
import org.junit.Test;


public class RoundTripTests {
	
	PoParser poParser;
	PoWriter poWriter;
	
	public RoundTripTests(){
		poParser = new PoParser();
		poWriter = new PoWriter();
	}
	
	@Test
	public void testRoundtrip1() throws Throwable{
		File original = getResource("/roundtrip/sample.po");
		TestUtils.testRoundTrip(original);
	}

	@Test
	public void testWordWrappingInMsgId() throws Throwable{
		File original = getResource("/roundtrip/msgid_wordwrap.po");
		TestUtils.testRoundTrip(original);
	}
	
	@Test
	public void testEmptyLineNote() throws Throwable{
		File original = getResource("/roundtrip/translate-toolkit/emptylines_notes.po");
		TestUtils.testRoundTrip(original);
	}
	
	@Test
	public void testMalformedObsoleteUnits() throws Throwable{
		File original = getResource("/roundtrip/translate-toolkit/malformed_obsoleteunits.po");
		try{
			TestUtils.testRoundTrip(original);
			fail("expected UnexpectedTokenException");
		}
		catch(UnexpectedTokenException e){
		}
	}
	
	@Test
	public void testMalformedUnits() throws Throwable{
		File original = getResource("/roundtrip/translate-toolkit/malformed_units.po");
		try{
			TestUtils.testRoundTrip(original);
			fail("expected UnexpectedTokenException");
		}
		catch(UnexpectedTokenException e){
		}
	}
	
	@Test
	public void testNonAsciiHeader() throws Throwable{
		File original = getResource("/roundtrip/translate-toolkit/nonascii_header.po");
		TestUtils.testRoundTrip(original);
	}
	
	@Test
	public void testMultilineContext() throws Throwable{
		File original = getResource("/roundtrip/translate-toolkit/multiline_context.po");
		TestUtils.testRoundTrip(original);
	}
	
	@Test
	public void testContentEndsWithEOL() throws Throwable{
		File original = getResource("/roundtrip/content_end_with_eol.po");
		TestUtils.testRoundTrip(original);
	}
	
	private File getResource(String file){
		return new File( getClass().getResource(file).getFile() );
	}
	
}
