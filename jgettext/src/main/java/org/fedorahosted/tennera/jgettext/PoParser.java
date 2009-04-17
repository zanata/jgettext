package org.fedorahosted.tennera.jgettext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.fedorahosted.tennera.jgettext.catalog.parse.UnexpectedTokenException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class PoParser {

	public Catalog parseCatalog(File file) throws FileNotFoundException, IOException, ParseException{
		ExtendedCatalogParser parser = new ExtendedCatalogParser(file);
		return parseCatalog(parser);
	}
	
	public Catalog parseCatalog(Reader reader, boolean isPot) throws ParseException{
		ExtendedCatalogParser parser = new ExtendedCatalogParser(reader, isPot);
		return parseCatalog(parser);
	}
	
	public Catalog parseCatalog(InputStream inputStream, boolean isPot) throws ParseException{
		ExtendedCatalogParser parser = new ExtendedCatalogParser(inputStream, isPot);
		return parseCatalog(parser);
	}
	
	public Catalog parseCatalog(InputStream inputStream, Charset charset, boolean isPot) throws ParseException{
		ExtendedCatalogParser parser = new ExtendedCatalogParser(inputStream, charset, isPot);
		return parseCatalog(parser);
	}
	
	private Catalog parseCatalog(ExtendedCatalogParser parser) throws ParseException{
		try {
			parser.catalog();
		} catch (RecognitionException e) {
			throw new UnexpectedTokenException(e.getMessage(), e.getLine());
		} catch (TokenStreamException e) {
			throw new ParseException(e.getMessage(),e,-1);
		}
		return parser.getCatalog();
	}
	
	public Message parseMessage(File file) throws FileNotFoundException, IOException, ParseException{
		MessageStreamParser parser = new MessageStreamParser(file);
		return parseMessage(parser);
	}
	
	public Message parseMessage(Reader reader) throws ParseException{
		MessageStreamParser parser = new MessageStreamParser(reader);
		return parseMessage(parser);
	}
	
	public Message parseMessage(InputStream inputStream) throws ParseException{
		MessageStreamParser parser = new MessageStreamParser(inputStream);
		return parseMessage(parser);
	}
	
	public Message parseMessage(InputStream inputStream, Charset charset) throws ParseException{
		MessageStreamParser parser = new MessageStreamParser(inputStream, charset);
		return parseMessage(parser);
	}

	private Message parseMessage(MessageStreamParser parser) throws ParseException{
		if(!parser.hasNext())
			throw new ParseException("No Message in input", -1);
		Message message = parser.next();
		if(parser.hasNext())
			throw new ParseException("Input contains more than a single Message", -1);
		return message;
	}

}
