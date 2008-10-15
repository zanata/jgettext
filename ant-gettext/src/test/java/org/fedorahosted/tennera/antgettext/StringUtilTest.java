/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.fedorahosted.tennera.antgettext;

import junit.framework.TestCase;

public class StringUtilTest extends TestCase {

    public StringUtilTest(String name) {
	super(name);
    }

    public void testPseudolocalise() {
	String expected = "[--- Tⱨé ᕴմîçⱪ Ьяø𝍕ň ϝøẋ ʝմოｐš øⱱéя ŧⱨé ŀåżŷ đøց. THE RAIN IN SPAIN FALLS MAINLY IN THE PLAIN. ---]";
	String original = "The quick brown fox jumps over the lazy dog. THE RAIN IN SPAIN FALLS MAINLY IN THE PLAIN.";
	String actual = StringUtil.pseudolocalise(original);
	System.out.println(actual);
	assertEquals(expected, actual);
    }
    
    public void testChomp() throws Exception {
	assertEquals("", StringUtil.chomp(""));
	assertEquals("", StringUtil.chomp("\n"));
	assertEquals("abc", StringUtil.chomp("abc\r"));
	assertEquals("abc", StringUtil.chomp("abc\n"));
	assertEquals("abc", StringUtil.chomp("abc\r\n"));
	assertEquals("abc\n", StringUtil.chomp("abc\n\n"));
	assertEquals("abc\r\n", StringUtil.chomp("abc\r\n\r\n"));
    }
}
