/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;
import java.io.FileInputStream;

import org.apache.tools.ant.BuildFileTest;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.Catalog.MessageProcessor;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;

@SuppressWarnings("nls")
public class BuildTest extends BuildFileTest {
    private int numHeaders = 0;
    /**
     * This helps Infinitest, since it doesn't know about the taskdefs inside build.xml 
     */
    static Class[] testedClasses = {
    		Prop2PoTask.class, 
    		Prop2PotTask.class, 
    		Pot2EnTask.class, 
    		Po2PropTask.class, 
    		XPath2PotTask.class
    };

    public BuildTest(String name) {
	super(name);
    }
    
	@Override
    protected void setUp() throws Exception {
	// work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
	System.getProperties().remove("basedir");
	configureProject("src/test/data/taskdefs/build.xml");
	executeTarget("prepare");
    }

    @Override
    protected void tearDown() throws Exception {
	executeTarget("cleanup");
    }
    
    private Catalog runPOGeneratingTest(String taskName, String poTargetName, int msgCount, BaseProcessor processor) throws Exception
    {
	executeTarget(taskName);
	System.out.println(this.getOutput());
	File targetFile = new File(poTargetName);
	assertTrue(targetFile.toString() + " should exist", targetFile.exists());
	ExtendedCatalogParser parser = new ExtendedCatalogParser(targetFile);
	parser.catalog();
	Catalog catalog = parser.getCatalog();
	catalog.setTemplate(poTargetName.endsWith(".pot"));
	catalog.processMessages(processor);
	assertEquals(1, numHeaders);
	assertEquals(msgCount, processor.numMessages);
	return catalog;
    }
    
    abstract class BaseProcessor implements MessageProcessor
    {
	int numMessages = 0;

//	@Override
	public void processMessage(Message entry) {
	    if (entry.isHeader()) {
		++numHeaders;
		return;
	    }
	    ++numMessages;
	    checkEntry(entry);
	}

	abstract void checkEntry(Message entry);
    }
    
    private final class EmptyMsgstrChecker extends BaseProcessor {
	// test that msgstr==""
	@Override
	void checkEntry(Message entry) {
	    String msgstr = entry.getMsgstr();
		assertTrue("msgstr(\""+msgstr+"\") should equal \"\"", 
		msgstr.equals(""));
	}
    }

    public void testBasic() throws Exception {
	runPOGeneratingTest("testBasic", 
		"src/test/data/taskdefs/pot2en_basic/messages.po", 
		1, new BaseProcessor() 
	{
	    // test that msgstr==msgid
	    @Override
	    void checkEntry(Message entry) {
		String msgstr = entry.getMsgstr();
		String msgid = entry.getMsgid();
		assertTrue("msgstr(\""+msgstr+"\") should equal msgid(\""+msgid+"\")", 
			msgstr.equals(msgid));
	    }
	});
    }

    public void testPseudo() throws Exception {
	runPOGeneratingTest("testPseudo", 
		"src/test/data/taskdefs/pot2en_pseudo/messages.po", 
		1, new BaseProcessor() 
	{
	    // test that msgstr==pseudolocalise(msgid)
	    @Override
	    void checkEntry(Message entry) {
		String msgstr = entry.getMsgstr();
		String msgid = entry.getMsgid();
		assertTrue("msgstr(\""+msgstr+"\") should equal pseudo(msgid(\""+msgid+"\"))", 
			msgstr.equals(StringUtil.pseudolocalise(msgid)));
	    }
	});
    }

    public void testProp2Pot() throws Exception {
	runPOGeneratingTest("testProp2Pot", 
		"src/test/data/taskdefs/prop2pot_pot/messages.pot", 
		4, new EmptyMsgstrChecker());
    }
    
    public void testProp2Po() throws Exception {
	runPOGeneratingTest("testProp2Po", 
		"src/test/data/taskdefs/prop2po_po/messages_es.po", 
		5, new BaseProcessor()
	{
		@Override
		void checkEntry(Message entry) {
			String msgstr = entry.getMsgstr();
			String msgid = entry.getMsgid();
			assertTrue("msgstr(\""+msgstr+"\") should not equal msgid(\""+msgid+"\")", !msgstr.equals(msgid));
			if (msgid.equals("another_key"))
				assertEquals("", msgstr);
		}
	}
	
	);
    }
    
    private Properties runPropGeneratingTest(String taskName, String propTargetName) throws Exception
    {
	executeTarget(taskName);
	System.out.println(this.getOutput());
	File targetFile = new File(propTargetName);
	assertTrue(targetFile.toString(), targetFile.exists());
	Properties props = new Properties();
	FileInputStream in = new FileInputStream(targetFile);
	props.load(in);
	in.close();
	return props;
    }
    
    public void testPo2Prop() throws Exception {
	Properties result = runPropGeneratingTest("testPo2Prop", 
		"src/test/data/taskdefs/po2prop_prop/messages_qps.properties");
	Properties expected = new Properties();
	expected.setProperty("msgctxt A", "msgstr A");
	assertEquals(expected, result);
    }
    
    public void testXpath2Pot() throws Exception {
	runPOGeneratingTest(getName(), 
		"src/test/data/taskdefs/xpath2pot_pot/meta.pot", 
		147, new EmptyMsgstrChecker());
    }

}
