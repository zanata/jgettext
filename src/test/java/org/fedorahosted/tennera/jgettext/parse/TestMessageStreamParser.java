package org.fedorahosted.tennera.jgettext.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.junit.Before;
import org.junit.Test;

public class TestMessageStreamParser {

    File poFile;

    @Before
    public void setup() {
        poFile = new File(getClass().getResource("/valid/sample.po").getFile());
    }

    @Test
    public void testIteratingThroughABasicFile() throws Throwable {
        Message message;
        MessageStreamParser parser = new MessageStreamParser(poFile);

        assertTrue(parser.hasNext());
        message = parser.next();
        assertTrue(message.isHeader());
        assertTrue(message.isFuzzy());

        assertTrue(parser.hasNext());
        message = parser.next();
        assertFalse(message.isHeader());
        assertFalse(message.isFuzzy());

        parser.next();
        parser.next();

        message = parser.next();
        assertTrue(message.isObsolete());

        parser.next();
        parser.next(); // last message

        assertFalse(parser.hasNext());
    }

    String msgString = "msgid \"hello world!\"\n" +
            "msgstr \"hei verden!\"\n";

    @Test
    public void testParseFromReader() throws Throwable {

        MessageStreamParser parser = new MessageStreamParser(
                new StringReader(msgString));

        Message message = parser.next();

        assertEquals(message.getMsgid(), "hello world!");
        assertEquals(message.getMsgstr(), "hei verden!");

    }

    @Test
    public void testParseFromInputStream() throws Throwable {
        MessageStreamParser parser = new MessageStreamParser(
                new ByteArrayInputStream(msgString.getBytes("UTF-8")));

        Message message = parser.next();

        assertEquals(message.getMsgid(), "hello world!");
        assertEquals(message.getMsgstr(), "hei verden!");
    }

    @Test
    public void testParseFromInputStreamWithUtf16Charset() throws Throwable {
        MessageStreamParser parser =
                new MessageStreamParser(
                        new ByteArrayInputStream(msgString.getBytes("UTF-16")),
                        Charset.forName("UTF-16"));

        Message message = parser.next();

        assertEquals(message.getMsgid(), "hello world!");
        assertEquals(message.getMsgstr(), "hei verden!");
    }

}
