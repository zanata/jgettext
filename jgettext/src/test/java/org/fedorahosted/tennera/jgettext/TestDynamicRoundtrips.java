package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestDynamicRoundtrips extends TestSuite{
    
    public final static void generateTests(TestSuite ts) throws Throwable{
    	final Properties properties = new Properties();
    	try{
    		URL url = TestDynamicRoundtrips.class.getResource("/roundtripfiles.properties");
    		if(url == null) // skip tests if configuration file not found
    			return;
    		properties.load(
    				new BufferedInputStream(
    						new FileInputStream(
    								new File(url.getFile()))));
    	}
    	catch(IOException e){
    		fail("unable to load properties file");
    	}
    	final Enumeration<Object> keys = properties.keys();
    	while(keys.hasMoreElements()){
    		final String key = (String) keys.nextElement();
    		final File rootDir = new File(properties.getProperty(key));
    		TestSuite suite = new TestSuite(key);
    		ts.addTest(suite);
            TestCase test = new TestCase(key + " exists"){
                @Override
                public void runTest() {
            		assertTrue("No such file.", rootDir.exists());
            		assertTrue("File not a directory", rootDir.isDirectory());
                }
            };
            suite.addTest(test);
            
            
            if(!rootDir.exists() || !rootDir.isDirectory())
            	return;
            
            List<File> files = new LinkedList<File>();
    		getChildPoFilesRecursively(rootDir, files);
    		
    		for(int i=0;i<files.size();i++){
    			final File f = files.get(i);
                TestCase testCase = new TestCase(f.getAbsolutePath().substring(rootDir.getAbsolutePath().length())){
                    @Override
                    public void runTest() throws Throwable{
                    	JGettextTestUtils.testRoundTrip(null, f);
                    }
                };
                suite.addTest(testCase);
    		}
    	}
    }
    
    
    public static TestSuite suite() throws Throwable{
            TestSuite ts=new TestSuite();
            generateTests(ts);
            if(ts.countTestCases() == 0){
            	ts.addTest(new TestCase("bogus"){
            		@Override
            		protected void runTest() throws Throwable {
            		}
            	});
            }
            return ts;
    }    

    private static class PoAndDirFileFilter implements FileFilter{
    	//@Override
    	public boolean accept(File pathname) {
    		if(pathname.isDirectory()){
				return true;
			}
			else{
				if(pathname.getName().endsWith(".po") || pathname.getName().endsWith(".pot")){
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
