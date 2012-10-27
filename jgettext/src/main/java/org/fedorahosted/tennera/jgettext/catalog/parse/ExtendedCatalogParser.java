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
 * Red Hat Author(s): Steve Ebersole
 */
package org.fedorahosted.tennera.jgettext.catalog.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Here we extend the Antlr-generated parser to provide implementations of the supplied callback hooks
 * in order to transform the AST into a more workable object model.
 *
 * @author Steve Ebersole
 */
public class ExtendedCatalogParser extends CatalogParser {
	private final Catalog catalog;
	private Message currentMessage = new Message();

	/**
	 * Uses the charset encoding specified in the file's Gettext header.
	 * @param catalog
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ExtendedCatalogParser(Catalog catalog, File file) throws FileNotFoundException, IOException {
		super( new CatalogLexer( file ) );
		catalog.setTemplate(isPot(file));
      this.catalog = catalog;
	}
	public ExtendedCatalogParser(Catalog catalog, Reader reader, boolean isPot){
		super( new CatalogLexer( reader ) );
		catalog.setTemplate(isPot);
      this.catalog = catalog;
	}

	/**
	 * Uses the charset encoding specified in the stream's Gettext header.
    * @param catalog
	 * @param inputStream
	 * @param isPot
	 * @throws IOException
	 */
	public ExtendedCatalogParser(Catalog catalog, InputStream inputStream, boolean isPot) throws IOException {
		super( new CatalogLexer( inputStream ) );
		catalog.setTemplate(isPot);
      this.catalog = catalog;
	}
	
	public ExtendedCatalogParser(Catalog catalog, InputStream inputStream, Charset charset, boolean isPot){
		super( new CatalogLexer( inputStream, charset ) );
		catalog.setTemplate(isPot);
      this.catalog = catalog;
	}
	
	private static boolean isPot(File file) {
	    return file.getName().toLowerCase().endsWith(".pot");
	}

	public Catalog getCatalog() {
		return catalog;
	}

	@Override
	public void reportError(RecognitionException e) {
		UnexpectedTokenException utEx = new UnexpectedTokenException( e.getMessage(), e.getLine() );
		utEx.initCause(e);
		throw utEx;
	}

	@Override
	public void reportError(String s) {
		throw new ParseException( "error parsing catalog : " + s, -1 );
	}

	@Override
	public void reportWarning(String s) {
	}

	@Override
	protected void handleMessageBlock(AST messageBlock) {
		catalog.addMessage( currentMessage );
		currentMessage = new Message();
	}

	@Override
	protected void handleObsoleteMessageBlock(AST messageBlock) {
		currentMessage.markObsolete();
		handleMessageBlock( messageBlock );
	}

	@Override
	protected void handleCatalogComment(AST comment) {
		currentMessage.addComment( extractText( comment ) );
	}

	@Override
	protected void handleExtractedComment(AST comment) {
		currentMessage.addExtractedComment( extractText( comment ) );
	}

	@Override
	protected void handleReference(AST sourceRef) {
		currentMessage.addSourceReference( parseSourceReference( sourceRef ) );
	}

	@Override
	protected void handleFlag(AST flag) {
		String [] flags = flag.getText().split(",");
		for(String flagStr : flags){
			flagStr = flagStr.trim();
			if(!flagStr.isEmpty())
				currentMessage.addFormat(flagStr);
		}
	}

	@Override
	protected void handlePreviousMsgctxt(AST previousMsgctxt) {
		currentMessage.setPrevMsgctx( extractText( previousMsgctxt ) );
	}

	@Override
	protected void handlePreviousMsgid(AST previousMsgid) {
		currentMessage.setPrevMsgid( extractText( previousMsgid ) );
	}

	@Override
	protected void handlePreviousMsgidPlural(AST previousMsgidPlural) {
		currentMessage.setPrevMsgidPlural( extractText( previousMsgidPlural ) );
	}

	@Override
	protected void handleDomain(AST domain) {
		currentMessage.setDomain( extractText( domain ) );
	}

	@Override
	protected void handleMsgctxt(AST msgctxt) {
		currentMessage.setMsgctxt( extractText( msgctxt ) );
	}

	@Override
	protected void handleMsgid(AST msgid) {
		currentMessage.setMsgid( extractText( msgid ) );
	}

	@Override
	protected void handleMsgidPlural(AST msgidPlural) {
		currentMessage.setMsgidPlural( extractText( msgidPlural ) );
	}

	@Override
	protected void handleMsgstr(AST msgstr) {
		currentMessage.setMsgstr( extractText( msgstr ) );
	}

	@Override
	protected void handleMsgstrPlural(AST msgstr, AST plurality) {
		currentMessage.addMsgstrPlural( extractText( msgstr ), Integer.parseInt( plurality.getText() ) );
	}

	private String extractText(AST ast) {
		return ast == null ? "" : ast.getText();
	}

	private String parseSourceReference(AST ast) {
		return ast.getText();
	}
}