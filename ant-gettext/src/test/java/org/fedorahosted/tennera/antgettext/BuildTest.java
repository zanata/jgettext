/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.fedorahosted.openprops.Properties;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.MessageProcessor;
import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;

@SuppressWarnings("nls")
public class BuildTest extends BuildFileTest {
    private int numHeaders = 0;
    /**
     * This helps Infinitest, since it doesn't know about the taskdefs inside build.xml 
     */
    @SuppressWarnings("unchecked")
    static Class[] testedClasses = {
    	Gettext2PropTask.class,
    	Po2PropTask.class, 
    	Pot2EnTask.class, 
    	Prop2PoTask.class, 
    	Prop2PotTask.class,
    	Prop2GettextTask.class,
    	Regex2PotTask.class,
    	XPath2PotTask.class
    };

    public BuildTest(String name) {
	super(name);
    }
    
	@Override
    protected void setUp() throws Exception {
	// work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
	System.getProperties().remove("basedir");
	configureProject("src/test/resources/taskdefs/build.xml");
	executeTarget("prepare");
    }

    @Override
    protected void tearDown() throws Exception {
	executeTarget("cleanup");
    }
    
    private Catalog runPOGeneratingTest(String taskName, 
    		String poTargetName, int msgCount, 
    		BaseProcessor processor) throws Exception
	{
    	return runPOGeneratingTest(taskName, poTargetName, msgCount, processor, true);
	}
    private Catalog runPOGeneratingTest(String taskName, 
    		String poTargetName, int msgCount, 
    		BaseProcessor processor, boolean expectHeader) throws Exception
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
	if (expectHeader)
		assertEquals(1, numHeaders);
	else
		assertEquals(0, numHeaders);
	assertEquals(msgCount, processor.numMessages);
	return catalog;
    }
    
    abstract class BaseProcessor implements MessageProcessor
    {
	int numMessages = 0;

	@Override
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
	runPOGeneratingTest(getName(), 
		"src/test/resources/taskdefs/pot2en_basic/messages.po", 
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
	runPOGeneratingTest(getName(), 
		"src/test/resources/taskdefs/pot2en_pseudo/messages.po", 
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
	runPOGeneratingTest(getName(), 
		"src/test/resources/taskdefs/prop2pot_pot/messages.pot", 
		4, new EmptyMsgstrChecker());
    }
    
    public void testProp2Po() throws Exception {
//	runPOGeneratingTest(getName(), 
//		"src/test/resources/taskdefs/prop2po_po/messages_es.po", 
//		5, new BaseProcessor()
//	{
//		@Override
//		void checkEntry(Message entry) {
//			String msgstr = entry.getMsgstr();
//			String msgid = entry.getMsgid();
//			// this is a weak test
//			assertTrue("msgstr(\""+msgstr+"\") should not equal msgid(\""+msgid+"\")", !msgstr.equals(msgid));
//			if (msgid.equals("another_key"))
//				assertEquals("", msgstr);
//		}
//	}
//	
//	);
	executeTarget(getName());
	TextFiles.assertEqualDirectories(
		"src/test/resources/taskdefs/prop2po-expected", 
		"src/test/resources/taskdefs/prop2po_po");
    }
    
    public void testRegex2Pot1() throws Exception {
    	List<String> expected = Arrays.asList("some more text",
    			"some reused text",
    			"some text");
    	runPOTGeneratingTest(expected, "src/test/resources/taskdefs/regex2pot1_pot/regex.pot");
    }
    
    public void testRegex2Pot2() throws Exception {
    	List<String> expected = Arrays.asList("ABC {0}{1}{2}", "Project Logo");

    	runPOTGeneratingTest(expected, "src/test/resources/taskdefs/regex2pot2_pot/regex.pot");
    }

    private void runPOTGeneratingTest(List<String> expected, String potFilename) throws Exception 
    {
		final List<String> msgidList = new ArrayList<String>();
	runPOGeneratingTest(getName(), 
		potFilename, 
		expected.size(), new BaseProcessor()
	{
		@Override
		void checkEntry(Message entry) {
			assertEquals("", entry.getMsgstr());
			msgidList.add(entry.getMsgid());
		}
	}
	
	);
	assertEquals(expected, msgidList);
    }
    
    public void testGettext2Prop() throws Exception {
    	executeTarget(getName());
		assertEquals("_about_", 
				loadProps("src/test/resources/taskdefs/gettext2prop_prop/messages1_dummy.properties")
				.getProperty("ABOUT_BUTTON"));
		assertEquals("_cancel_", 
				loadProps("src/test/resources/taskdefs/gettext2prop_prop/messages2_dummy.properties")
				.getProperty("CANCEL_BUTTON"));
		assertEquals(null, 
			loadProps("src/test/resources/taskdefs/gettext2prop_prop/messages1_dummy.properties")
			.getProperty("UNTRANSLATED"));
    }
    
    public void testProp2Gettext() throws Exception {
//      <gettext2prop srcDir="prop2gettext" dstDir="prop2gettext_po" />
    	executeTarget(getName());
//    	fail("must test contents of po and pot files");
    	TextFiles.assertEqualDirectories(
    		"src/test/resources/taskdefs/prop2gettext-expected", 
    		"src/test/resources/taskdefs/prop2gettext_po");
	}
    
    public void testVerifyProp1() throws Exception {
		executeTarget(getName());
		// just testing that there are no exceptions
	}
    
    public void testVerifyProp2() throws Exception {
		try {
			executeTarget(getName());
			fail("verify should have failed");
		} catch (BuildException e) {
			// expected
		}
	}
    
    
    
    private Properties runPropGeneratingTest(String taskName, String propTargetName) throws Exception
    {
	executeTarget(taskName);
	System.out.println(this.getOutput());
	Properties props = loadProps(propTargetName);
	return props;
    }

	private Properties loadProps(String filename) throws FileNotFoundException,
			IOException {
		File propFile = new File(filename);
		assertTrue(propFile.toString(), propFile.exists());
		Properties props = new Properties();
		FileInputStream in = new FileInputStream(propFile);
		props.load(in);
		in.close();
		return props;
	}
    
    public void testPo2Prop() throws Exception {
	Properties result = runPropGeneratingTest(getName(), 
		"src/test/resources/taskdefs/po2prop_prop/messages_qps.properties");
	Properties expected = new Properties();
	expected.setProperty("msgctxt A", "msgstr A");
	assertEquals(expected, result);
    }
    
    public void testXpath2Pot() throws Exception {
	runPOGeneratingTest(getName(),
		"src/test/resources/taskdefs/xpath2pot_pot/meta.pot", 
		147, new EmptyMsgstrChecker(), true);
    }
    
}
