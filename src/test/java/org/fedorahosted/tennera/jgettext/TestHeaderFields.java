package org.fedorahosted.tennera.jgettext;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

public class TestHeaderFields {

    PoParser parser;

    public TestHeaderFields() {
        parser = new PoParser();
    }

    @Test
    public void testSamplePo() throws Throwable {
        File sample = getResource("/valid/sample.po");
        Catalog catalog = parser.parseCatalog(sample);
        Message headerMsg = catalog.locateHeader();

        String headerStr = headerMsg.getMsgstr();

        HeaderFields header = HeaderFields.wrap(headerMsg);
        assertEquals(9, header.getKeys().size());
        assertEquals("1.0", header.getValue(HeaderFields.KEY_MimeVersion));

        header.setValue("MyKey", "abcd");
        assertEquals(10, header.getKeys().size());
        header.setValue("MyKey", "xxx");
        assertEquals(10, header.getKeys().size());

        Message result = header.unwrap();
        assertEquals(headerStr + "MyKey: xxx\n", result.getMsgstr());

    }

    private File getResource(String file) throws URISyntaxException {
        return new File(getClass().getResource(file).toURI());
    }
}
