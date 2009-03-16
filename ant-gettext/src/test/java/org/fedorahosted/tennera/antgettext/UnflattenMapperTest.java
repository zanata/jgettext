package org.fedorahosted.tennera.antgettext;

import org.apache.tools.ant.util.FileNameMapper;

import junit.framework.TestCase;


/**
 * Note: this mapping is undocumented and likely to change, 
 * so you probably shouldn't use it.
 * @author sflaniga
 *
 */
public class UnflattenMapperTest extends TestCase 
{
	
	FileNameMapper mapper = new UnflattenMapper();
	
	private void testMap(String expected, String from) 
	{
		String[] result = mapper.mapFileName(from);
		assertEquals(1, result.length);
		assertEquals(expected, result[0]);
	}
	
	
	public void testMapFileNameDefault() 
	{
		String from = "module/org.plugin-org.plugin.messages/locale.po";
		String expected = "module/org.plugin/org/plugin/messages_locale.properties";
		testMap(expected, from);
	}

//	public void testMapFileNameAlternative() 
//	{
//	// module/(locale)/(plugin)-(resource).po
//		mapper.setFrom("^[^/\\\\]+[/\\\\]([^/\\\\]+)[/\\\\]([^-]+)-(.*)[.]po$"); //$NON-NLS-1$
//		String from = "module/locale/org.plugin-org.plugin.messages.po";
//		String expected = "module/org.plugin/org/plugin/messages_locale.properties";
//		testMap(expected, from);
//	}

}
