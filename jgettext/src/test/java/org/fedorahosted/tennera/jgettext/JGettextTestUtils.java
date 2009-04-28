package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;

public class JGettextTestUtils {

	public static void testRoundTrip(String message, File f) throws FileNotFoundException, ParseException, IOException{
		String output = roundtrip(f);
		String msgCatOutput = readToStringFromMsgcat(output);
		String originalString = readToStringFromMsgcat(f); 
		assertEquals(message, originalString, msgCatOutput);
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
	
	public static String readToStringFromMsgcat(File file) {
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
    	String[] cmd_elements = {"/usr/bin/msgcat", file.getAbsolutePath()};
		try {
			Process prcess = Runtime.getRuntime().exec(cmd_elements);
			InputStream cmd_output = prcess.getInputStream();
			reader = new BufferedReader( new InputStreamReader(cmd_output)) ;
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(prcess.getErrorStream()));
            StringBuilder errorStr = new StringBuilder();
            line = null;
            while((line = errorReader.readLine()) != null) {
            	errorStr.append(line);
            	errorStr.append("\n");
            }
        	assertTrue("Error parsing input file:\n"+errorStr.toString(), errorStr.toString().isEmpty());
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
	
	public static String readToStringFromMsgcat(String input) {
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
    	String[] cmd_elements = {"/usr/bin/msgcat", "-"};
		try {
			Process prcess = Runtime.getRuntime().exec(cmd_elements);
			prcess.getOutputStream().write(input.getBytes());
			prcess.getOutputStream().close();
			
			InputStream cmd_output = prcess.getInputStream();
			reader = new BufferedReader( new InputStreamReader(cmd_output)) ;
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(prcess.getErrorStream()));
            StringBuilder errorStr = new StringBuilder();
            line = null;
            while((line = errorReader.readLine()) != null) {
            	errorStr.append(line);
            	errorStr.append("\n");
            }
        	assertTrue("Error parsing output file:\n"+errorStr.toString(), errorStr.toString().isEmpty());
            
            
            
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
