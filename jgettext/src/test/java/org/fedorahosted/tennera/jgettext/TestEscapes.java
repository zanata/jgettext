package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Test;

public class TestEscapes {
	
	PoParser poParser;
	PoWriter poWriter;
	
	public TestEscapes(){
		poParser = new PoParser();
		poWriter = new PoWriter();
	}
	
	@Test
	public void testEscapesComment() throws Throwable{
		File original = getResource("/valid/escapes_comment.po");
		testEscapes(original);
	}
	
	@Test
	public void testCRInMsgidAndMsgStr() throws Throwable{
		File original = getResource("/valid/escapes_cr_in_msgid_and_msgstr.po");
		testEscapes(original);
	}
	
	private void testEscapes(String message, File f) throws Throwable{
		String output = escapesProcess(f);
		String originalString = JGettextTestUtils.readToString(f); 
		assertEquals(message, originalString, output);
	}
	
	private void testEscapes(File f) throws Throwable{
		testEscapes(null, f);
	}
	
	private String escapesProcess(File original) throws Throwable{
		Catalog originalCatalog = poParser.parseCatalog(original);
		StringWriter outputWriter = new StringWriter();
		poWriter.write(originalCatalog, outputWriter);
		outputWriter.flush();
		return outputWriter.toString();
	}
	
	private File getResource(String file){
		return new File( getClass().getResource(file).getFile() );
	}


}
