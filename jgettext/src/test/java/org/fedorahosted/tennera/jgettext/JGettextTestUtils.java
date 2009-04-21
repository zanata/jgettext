package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;

public class JGettextTestUtils {

	public static void testRoundTrip(String message, File f) throws FileNotFoundException, ParseException, IOException{
		String output = roundtrip(f);
		String originalString = readToString(f); 
		assertEquals(message, originalString, output);
	}

	public static void testRoundTrip(File f) throws FileNotFoundException, ParseException, IOException{
		testRoundTrip(null, f);
	}

	
	public static String roundtrip(File original) throws FileNotFoundException, ParseException, IOException{
		PoParser poParser = new PoParser();
		PoWriter poWriter = new PoWriter();
		Catalog originalCatalog = poParser.parseCatalog(original);
		StringWriter outputWriter = new StringWriter();
		poWriter.write(originalCatalog, outputWriter);
		outputWriter.flush();
		return outputWriter.toString();
	}
	
	public static String readToString(File file) {

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
