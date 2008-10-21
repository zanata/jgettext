/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;

import org.apache.tools.ant.BuildFileTest;
import org.jboss.jgettext.Catalog;
import org.jboss.jgettext.Message;
import org.jboss.jgettext.Catalog.MessageProcessor;
import org.jboss.jgettext.catalog.parse.ExtendedCatalogParser;

public class Pot2EnTaskTest extends BuildFileTest {
    private int numHeaders = 0;

    public Pot2EnTaskTest(String name) {
	super(name);
    }
    
    @Override
//    public void configureProject(String filename) throws BuildException {
//	// work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
//	System.getProperties().remove("basedir");
//	File buildFile = new File(filename);
//	super.configureProject(buildFile.getPath());
//    }

    protected void setUp() throws Exception {
	// work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
	System.getProperties().remove("basedir");
	configureProject("src/test/data/taskdefs/pot2en.xml");
	executeTarget("prepare");
    }

    protected void tearDown() throws Exception {
	executeTarget("cleanup");
    }
    
    abstract class BaseProcessor implements MessageProcessor
    {
//	@Override
	public void processMessage(Message entry) {
	    if (entry.isHeader()) {
		++numHeaders;
		return;
	    }
	    checkEntry(entry);
	}

	abstract void checkEntry(Message entry);
    }
    
    private void runTest(String taskName, String targetName, MessageProcessor processor) throws Exception
    {
	executeTarget(taskName);
	System.out.println(this.getOutput());
	File targetFile = new File(targetName);
	assertTrue(targetFile.toString(), targetFile.exists());
	ExtendedCatalogParser parser = new ExtendedCatalogParser(targetFile);
	parser.catalog();
	Catalog catalog = parser.getCatalog();
	catalog.setTemplate(targetName.endsWith(".pot"));
	catalog.processMessages(processor);
	assertEquals(1, numHeaders);
    }

    public void testBasic() throws Exception {
	runTest("testBasic", 
		"src/test/data/taskdefs/pot2en_basic/messages.po", 
		new BaseProcessor() 
	{
	    // test that msgstr==msgid
	    @Override
	    void checkEntry(Message entry) {
		assertTrue("msgstr(\""+entry.getMsgstr()+"\") should equal msgid(\""+entry.getMsgid()+"\")", 
			entry.getMsgstr().equals(entry.getMsgid()));
	    }
	});
    }

    public void testPseudo() throws Exception {
	runTest("testPseudo", 
		"src/test/data/taskdefs/pot2en_pseudo/messages.po", 
		new BaseProcessor() 
	{
	    // test that msgstr==pseudolocalise(msgid)
	    @Override
	    void checkEntry(Message entry) {
		assertTrue("msgstr(\""+entry.getMsgstr()+"\") should equal pseudo(msgid(\""+entry.getMsgid()+"\"))", 
			entry.getMsgstr().equals(StringUtil.pseudolocalise(entry.getMsgid())));
	    }
	});
    }

    public void testProp2Pot() throws Exception {
	runTest("testProp2Pot", 
		"src/test/data/taskdefs/prop2pot_pot/messages.pot", 
		new BaseProcessor() 
	{
	    // test that msgstr==""
	    @Override
	    void checkEntry(Message entry) {
		assertTrue("msgstr(\""+entry.getMsgstr()+"\") should equal \"\"", 
			entry.getMsgstr().equals(""));
	    }
	});
    }

}
