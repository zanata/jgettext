package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringWriter;

import org.junit.Test;

public class TestEscapes {

    PoParser poParser;
    PoWriter poWriter;

    public TestEscapes() {
        poParser = new PoParser();
        poWriter = new PoWriter();
    }

    @Test
    public void testEscapesCommentRoundtrip() throws Throwable {
        File original = getResource("/valid/escapes_comment.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testEscapesFuzzyCommentRoundtrip() throws Throwable {
        File original = getResource("/valid/escapes_comment_fuzzy.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testCRInMsgidAndMsgStrRoundtrip() throws Throwable {
        File original = getResource("/valid/escapes_cr_in_msgid_and_msgstr.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testBackslashRoundtrip() throws Throwable {
        File original = getResource("/valid/escapes_backslash.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testBackslash() throws Throwable {
        File original = getResource("/valid/escapes_backslash.po");
        Message msg = poParser.parseMessage(original);
        assertEquals("\\\\", msg.getMsgid());
        assertEquals("\\\\", msg.getMsgstr());
    }

    private void testEscapesRoundtrip(String message, File f) throws Throwable {
        String output = escapesProcess(f);
        String originalString =
                JGettextTestUtils.readToStringFromMsgcat(f, true);
        assertEquals(message, originalString, output);
    }

    private void testEscapesRoundtrip(File f) throws Throwable {
        testEscapesRoundtrip(null, f);
    }

    private String escapesProcess(File original) throws Throwable {
        Catalog originalCatalog = poParser.parseCatalog(original);
        StringWriter outputWriter = new StringWriter();
        poWriter.write(originalCatalog, outputWriter);
        outputWriter.flush();
        return outputWriter.toString();
    }

    private File getResource(String file) {
        return new File(getClass().getResource(file).getFile());
    }

}
