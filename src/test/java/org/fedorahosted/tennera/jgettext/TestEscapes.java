package org.fedorahosted.tennera.jgettext;

import static org.fedorahosted.tennera.jgettext.JGettextTestUtils.getResource;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path original = getResource("/valid/escapes_comment.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testEscapesFuzzyCommentRoundtrip() throws Throwable {
        Path original = getResource("/valid/escapes_comment_fuzzy.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testCRInMsgidAndMsgStrRoundtrip() throws Throwable {
        Path original = getResource("/valid/escapes_cr_in_msgid_and_msgstr.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testBackslashRoundtrip() throws Throwable {
        Path original = getResource("/valid/escapes_backslash.po");
        testEscapesRoundtrip(original);
    }

    @Test
    public void testBackslash() throws Throwable {
        Path original = getResource("/valid/escapes_backslash.po");
        Message msg = poParser.parseMessage(original);
        assertEquals("\\\\", msg.getMsgid());
        assertEquals("\\\\", msg.getMsgstr());
    }

    private void testEscapesRoundtrip(String message, Path f) throws Throwable {
        String output = escapesProcess(f);
        String originalString =
                JGettextTestUtils.readToStringFromMsgcat(f, true);
        assertEquals(message, originalString, output);
    }

    private void testEscapesRoundtrip(Path f) throws Throwable {
        testEscapesRoundtrip(null, f);
    }

    private String escapesProcess(Path original) throws Throwable {
        Catalog originalCatalog = poParser.parseCatalog(original);
        StringWriter outputWriter = new StringWriter();
        poWriter.write(originalCatalog, outputWriter);
        outputWriter.flush();
        return outputWriter.toString();
    }

}
