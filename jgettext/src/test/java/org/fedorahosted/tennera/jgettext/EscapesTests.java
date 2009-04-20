package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Test;

public class EscapesTests {
	
	PoParser poParser;
	PoWriter poWriter;
	
	public EscapesTests(){
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
		File original = getResource("/valid/escapes_msg.po");
		testEscapes(original);
	}
	
	private void testEscapes(String message, File f) throws Throwable{
		String output = escapesProcess(f);
		String originalString = readToString(f); 
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
	
    private static String readToString(File file) {

    	BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
	    	reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	 
	        String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 
        return sb.toString();
    }


}
