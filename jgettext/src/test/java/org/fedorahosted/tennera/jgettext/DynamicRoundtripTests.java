package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.junit.Test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DynamicRoundtripTests extends TestSuite{
    
    public final static void generateTests(TestSuite ts) throws Throwable{
    	final Properties properties = new Properties();
    	try{
    		properties.load(
    				new BufferedInputStream(
    						new FileInputStream(
    								new File(
    										DynamicRoundtripTests.class.getResource("/roundtripfiles.properties").getFile()))));
    	}
    	catch(IOException e){
    		fail("unable to load properties file");
    	}
    	final Enumeration<Object> keys = properties.keys();
    	while(keys.hasMoreElements()){
    		final String key = (String) keys.nextElement();
    		final File rootDir = new File(properties.getProperty(key));
    		
            TestCase test = new TestCase(key + " exists"){
                @Override
                public void runTest() {
            		assertTrue("No such file.", rootDir.exists());
            		assertTrue("File not a directory", rootDir.isDirectory());
                }
            };
            ts.addTest(test);
            
            if(!rootDir.exists() || !rootDir.isDirectory())
            	return;
            
            List<File> files = new LinkedList<File>();
    		getChildPoFilesRecursively(rootDir, files);
    		
    		for(int i=0;i<files.size();i++){
    			final File f = files.get(i);
                TestCase testCase = new TestCase(f.getAbsolutePath().substring(rootDir.getAbsolutePath().length())){
                    @Override
                    public void runTest() throws Throwable{
                    	testRoundTrip(null, f);
                    }
                };
                ts.addTest(testCase);
    		}
    	}
    }
    
    
	private static void testRoundTrip(String message, File f) throws FileNotFoundException, ParseException, IOException{
		String output = roundtrip(f);
		String originalString = readToString(f); 
		assertEquals(message, originalString, output);
	}

	
	private static String roundtrip(File original) throws FileNotFoundException, ParseException, IOException{
		PoParser poParser = new PoParser();
		PoWriter poWriter = new PoWriter();
		Catalog originalCatalog = poParser.parseCatalog(original);
		StringWriter outputWriter = new StringWriter();
		poWriter.write(originalCatalog, outputWriter);
		outputWriter.flush();
		return outputWriter.toString();
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
    
    public static TestSuite suite() throws Throwable{
            TestSuite ts=new TestSuite();
            generateTests(ts);
            return ts;
    }    

    private static class PoAndDirFileFilter implements FileFilter{
    	@Override
    	public boolean accept(File pathname) {
    		if(pathname.isDirectory()){
				return true;
			}
			else{
				if(pathname.getName().endsWith(".po")){
					return true;
				}
			}
			return false;
    	}    	
    }
    
    private static FileFilter ff = new PoAndDirFileFilter();
    
    private static void getChildPoFilesRecursively(File dir, List<File> files){
    	
    	File [] children = dir.listFiles(ff);
    	
    	for(File f: children){
    		if(f.isDirectory()){
    			getChildPoFilesRecursively(f, files);
    		}
    		else{
    			files.add(f);
    		}
    	}
    }
   
}
