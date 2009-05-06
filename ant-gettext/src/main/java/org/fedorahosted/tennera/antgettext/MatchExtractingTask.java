/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;
import org.xml.sax.SAXException;

/**
 * Extracts strings which match some condition into a gettext template file (POT).
 * 
 * @author <a href="sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public abstract class MatchExtractingTask extends MatchingTask 
{
    private BufferedWriter out;
    private File srcDir;
    private File target;
    private String pathPrefix = "";
    private String format = "";
    
    /**
     * Maps an English key to a set of matching source locations
     */
    private Map<String, Set<String>> mapKeyToLocationSet = new TreeMap<String, Set<String>>();

    /**
     * Finds the (1-based) number of the line containing a character, 
     * specified by its character index from the beginning of the file.
     * @param lineStarts
     * @param charNo
     * @return
     */
    protected static int findLineNumber(Integer[] lineStarts, int charNo) 
    {
	// Find the largest index in the list less than or equal to charNo, and add 1.  
	int position = Arrays.binarySearch(lineStarts, charNo);
	if (position >= 0)
	    return position + 1;
	else
	    return -position - 1;
    }

    public void setSrcDir(File srcDir) 
    {
	this.srcDir = srcDir;
    }

    public void setTarget(File target) 
    {
	this.target = target;
    }

    public void setPathPrefix(String prefix) 
    {
	this.pathPrefix = prefix;
    }
    
    public void setFormat(String format) 
    {
	this.format = format;
    }

    @Override
    public void execute() throws BuildException 
    {
	DirUtil.checkDir(srcDir, "srcDir", false);
	//      if (target == null)
	//      {
	//         throw new BuildException("target attribute must be set!");
	//      }
	if (target != null && target.exists() && !target.isFile())
	{
	    throw new BuildException("target exists but is not a file!");
	}

	try
	{
	    DirectoryScanner ds = this.getDirectoryScanner(srcDir);
	    ds.scan();
	    String[] files = ds.getIncludedFiles();

	    log("Files to scan: "+files.length, Project.MSG_VERBOSE);
	    
	    for (int i = 0; i < files.length; i++)
	    {
		String filename = files[i];
		log("processing " + filename, Project.MSG_VERBOSE);
		File f = new File(srcDir, filename);
		processFile(filename, f);
	    }
	    if (mapKeyToLocationSet.isEmpty()) 
	    {
		log("No matching English strings found in '" +srcDir+ "'", Project.MSG_VERBOSE);
		return;
	    }
	    if (target == null)
	    {
		log("Extracting "+mapKeyToLocationSet.size()+" English strings from '" +srcDir+ "' to STDOUT", Project.MSG_VERBOSE);
		out = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
		generatePot(out);
	    }
	    else
	    {
		target.getParentFile().mkdirs();
		log("Extracting "+mapKeyToLocationSet.size()+" English strings from '" +srcDir+ "' to '"+target+"'", Project.MSG_VERBOSE);
		out = new BufferedWriter(new FileWriter(target));
		generatePot(out);
		out.close();
	    }
	}
	catch (IOException e)
	{
	    throw new BuildException(e);
	} catch (SAXException e) {
	    throw new BuildException(e);
	}
    }

    /**
     * Scans a single file for matches, recording them for later output.  
     * @param filename
     * @throws IOException
     * @throws SAXException 
     * @throws BuildException
     */
    protected abstract void processFile(String filename, File f) throws IOException, SAXException; 

    /**
     * Records a match for later output by generatePot
     * @param filename
     * @param key
     * @param location location within the specified file
     */
    protected void recordMatch(String filename, String key, String location) 
    {
	Set<String> set = mapKeyToLocationSet.get(key);
	if(set == null)
	{
	    set = new TreeSet<String>();
	    mapKeyToLocationSet.put(key, set);
	}
	set.add(filename+':'+location);
    }

    private void generatePot(BufferedWriter out) throws IOException 
    {
	Catalog cat = new Catalog(true);
	for (Map.Entry<String, Set<String>> mapEntry : mapKeyToLocationSet.entrySet())
	{
	    Message message = new Message();
	    //	   message.addExtractedComment(null);
	    Set<String> locations = mapEntry.getValue();
	    
	    for (String location : locations) 
	    {
		message.addSourceReference(pathPrefix+location);
	    }
	    
	    if (!format.equals(""))
		message.addFormat(format);
	    //	   message.setMsgctxt(null);
	    message.setMsgid(mapEntry.getKey());
	    cat.addMessage(message);
	}
	PoWriter writer = new PoWriter();
	writer.setGenerateHeader(true);
	writer.write(cat, out);
    }

}