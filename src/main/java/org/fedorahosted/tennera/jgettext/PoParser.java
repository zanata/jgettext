/*
 * Copyright (c) 2007, Red Hat Middleware, LLC. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, v. 2.1. This program is distributed in the
 * hope that it will be useful, but WITHOUT A WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License, v.2.1 along with this
 * distribution; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Red Hat Author(s): Asgeir Frimannsson
 */
package org.fedorahosted.tennera.jgettext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.fedorahosted.tennera.jgettext.catalog.parse.UnexpectedTokenException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class PoParser {

    private Catalog catalog;

    public PoParser() {
        this(new Catalog());
    }

    public PoParser(Catalog catalog) {
        this.catalog = catalog;
    }

    public Catalog parseCatalog(Path file) throws IOException, ParseException {
        ExtendedCatalogParser parser = new ExtendedCatalogParser(catalog, file);
        return parseCatalog(parser);
    }

    @Deprecated
    public Catalog parseCatalog(File file) throws IOException, ParseException {
        return parseCatalog(file.toPath());
    }

    public Catalog parseCatalog(Reader reader, boolean isPot)
            throws ParseException {
        ExtendedCatalogParser parser =
                new ExtendedCatalogParser(catalog, reader, isPot);
        return parseCatalog(parser);
    }

    public Catalog parseCatalog(InputStream inputStream, boolean isPot)
            throws ParseException, IOException {
        ExtendedCatalogParser parser =
                new ExtendedCatalogParser(catalog, inputStream, isPot);
        return parseCatalog(parser);
    }

    public Catalog parseCatalog(InputStream inputStream, Charset charset,
            boolean isPot) throws ParseException {
        ExtendedCatalogParser parser =
                new ExtendedCatalogParser(catalog, inputStream, charset, isPot);
        return parseCatalog(parser);
    }

    private Catalog parseCatalog(ExtendedCatalogParser parser)
            throws ParseException {
        try {
            parser.catalog();
        } catch (RecognitionException e) {
            throw new UnexpectedTokenException(e.getMessage(), e.getLine());
        } catch (TokenStreamException e) {
            throw new ParseException(e.getMessage(), e, -1);
        }
        return parser.getCatalog();
    }

    public Message parseMessage(Path file) throws  IOException, ParseException {
        MessageStreamParser parser = new MessageStreamParser(file);
        return parseMessage(parser);
    }

    @Deprecated
    public Message parseMessage(File file) throws  IOException, ParseException {
        return parseMessage(file.toPath());
    }

    public Message parseMessage(Reader reader) throws ParseException {
        MessageStreamParser parser = new MessageStreamParser(reader);
        return parseMessage(parser);
    }

    public Message parseMessage(InputStream inputStream) throws ParseException,
            IOException {
        MessageStreamParser parser = new MessageStreamParser(inputStream);
        return parseMessage(parser);
    }

    public Message parseMessage(InputStream inputStream, Charset charset)
            throws ParseException {
        MessageStreamParser parser =
                new MessageStreamParser(inputStream, charset);
        return parseMessage(parser);
    }

    private Message parseMessage(MessageStreamParser parser)
            throws ParseException {
        if (!parser.hasNext())
            throw new ParseException("No Message in input", -1);
        Message message = parser.next();
        if (parser.hasNext())
            throw new ParseException(
                    "Input contains more than a single Message", -1);
        return message;
    }

}
