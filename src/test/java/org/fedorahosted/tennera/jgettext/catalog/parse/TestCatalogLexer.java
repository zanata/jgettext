package org.fedorahosted.tennera.jgettext.catalog.parse;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class TestCatalogLexer
{

    @Test
    public void testAscii() throws Exception
    {
        checkCharset("ascii.po", "ASCII");
    }

    @Test
    public void testBig5() throws Exception
    {
        checkCharset("big5.po", "BIG5");
    }

    @Test
    public void testCharset() throws Exception
    {
        checkCharset("charset.po", "ASCII");
    }

    @Test
    public void testUnspecified() throws Exception
    {
        checkCharset("unspecified.po", "UTF-8");
    }

    @Test
    public void testUtf8() throws Exception
    {
        checkCharset("utf8.po", "UTF-8");
    }

    private void checkCharset(String testfile, String expected)
            throws IOException, UnsupportedEncodingException
    {
        InputStream in1 = getClass().getResourceAsStream(testfile);
        String charset1 = CatalogLexer.readGettextCharset(in1);
        assertEquals(expected, charset1);
    }

}
