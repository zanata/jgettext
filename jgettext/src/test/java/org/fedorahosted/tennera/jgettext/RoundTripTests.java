package org.fedorahosted.tennera.jgettext;

import static org.fedorahosted.tennera.jgettext.Assert.assertNoDiff;

import java.io.File;

import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.write.CatalogWriter;
import org.junit.Test;


public class RoundTripTests {
	
	@Test
	public void testRoundtrip1() throws Throwable{
		File original = getResource("/roundtrip/sample.po");
		File output = new File("test1.po.out");
		roundtrip(original, output);
		assertNoDiff(original, output);
	}

	private void roundtrip(File original, File destination) throws Throwable{
		ExtendedCatalogParser parser = new ExtendedCatalogParser(original);
		parser.catalog();
		CatalogWriter writer = new CatalogWriter(parser.getCatalog());
		writer.writeTo(destination);
	}
	
	private File getResource(String file){
		return new File( getClass().getResource(file).getFile() );
	}
	
	
}
