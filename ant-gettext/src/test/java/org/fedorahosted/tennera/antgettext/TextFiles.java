/*
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

class TextFiles 
{
    private TextFiles() 
    {
    }
    
    public static final String REGEX_TO_SKIP = "\"POT-Creation-Date: .*\""; 
    
    public static void assertEqualDirectories(String expectedDir, String actualDir) throws IOException 
    {
	assertEqualDirectories(new File(expectedDir), new File(actualDir));
    }
    
    /**
     * Checks that all files/directories found in expectedDir are also 
     * in actualDir, using text-mode comparison.  Fails on the first 
     * different file.  Any lines which match the regex REGEX_TO_SKIP
     * will be replaced with the regex for purposes of comparison.
     * <p>
     * Note: extra files/dirs found in actualDir are ignored.  Also note 
     * that files are loaded completely into memory for comparison, 
     * which allows IDEs like Eclipse to show the differences nicely, 
     * but won't scale to multi-gigabyte files very well!
     * @param expectedDir
     * @param actualDir
     * @throws IOException
     */
    public static void assertEqualDirectories(File expectedDir, File actualDir) throws IOException
    {
	File[] expFiles = expectedDir.listFiles();
	for (File expectedFile : expFiles)
	{
	    File actualFile = new File(actualDir, expectedFile.getName());
	    if (shouldSkip(expectedFile))
		continue;
	    assertTrue("expected file matching "+expectedFile+": "+actualFile+" does not exist", actualFile.exists());
	    if (expectedFile.isDirectory())
	    {
		assertTrue("expected directory matching "+expectedFile+": "+actualFile+" is not a directory", actualFile.isDirectory());
		assertEqualDirectories(expectedFile, actualFile);
	    }
	    else
	    {
		assertEqualFiles(expectedFile, actualFile );
	    }
	}
    }

    public static void assertEqualFiles(File expectedFile, File actualFile) throws IOException
    {
	String expected = readLines(expectedFile);
	String actual = readLines(actualFile);
	assertEquals(
		"expected file matching "+expectedFile+": "+actualFile+" does not match", 
		expected, 
		actual);
    }

    private static String readLines(File file) 
    	throws FileNotFoundException, IOException 
    {
	StringBuilder exp = new StringBuilder();
	BufferedReader reader = new BufferedReader(new FileReader(file));
	try
	{
        	String line;
        	while ((line = reader.readLine()) != null)
        	{
        	    if (line.matches(REGEX_TO_SKIP))
        		exp.append(REGEX_TO_SKIP);
        	    else
        		exp.append(line);
        	    exp.append('\n');
        	}
        	return exp.toString();
	}
	finally
	{
	    reader.close();
	}
    }
    
    private static boolean shouldSkip(File f)
    {
	String name = f.getName();
	return (name.endsWith("~") || name.equals(".svn"));
    }

}
