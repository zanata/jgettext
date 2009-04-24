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
import org.fedorahosted.tennera.jgettext.Occurence;

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

	public ExtendedCatalogParser(File file) throws FileNotFoundException, IOException {
		super( new CatalogLexer( file ) );
		catalog = new Catalog(isPot(file));
	}
	public ExtendedCatalogParser(Reader reader, boolean isPot){
		super( new CatalogLexer( reader ) );	
		catalog = new Catalog(isPot);
	}
	
	public ExtendedCatalogParser(InputStream inputStream, boolean isPot){
		super( new CatalogLexer( inputStream ) );	
		catalog = new Catalog(isPot);
	}
	
	public ExtendedCatalogParser(InputStream inputStream, Charset charset, boolean isPot){
		super( new CatalogLexer( inputStream, charset ) );	
		catalog = new Catalog(isPot);
	}
	
	private static boolean isPot(File file) {
	    return file.getName().toLowerCase().endsWith(".pot");
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public void reportError(RecognitionException e) {
		UnexpectedTokenException utEx = new UnexpectedTokenException( e.getMessage(), e.getLine() );
		utEx.initCause(e);
		throw utEx;
	}

	public void reportError(String s) {
		throw new ParseException( "error parsing catalog : " + s, -1 );
	}

	public void reportWarning(String s) {
	}

	protected void handleMessageBlock(AST messageBlock) {
		catalog.addMessage( currentMessage );
		currentMessage = new Message();
	}

	protected void handleObsoleteMessageBlock(AST messageBlock) {
		currentMessage.markObsolete();
		handleMessageBlock( messageBlock );
	}

	protected void handleCatalogComment(AST comment) {
		currentMessage.addComment( extractText( comment ) );
	}

	protected void handleExtractedComment(AST comment) {
		currentMessage.addExtractedComment( extractText( comment ) );
	}

	protected void handleOccurence(AST occurence) {
		currentMessage.addOccurence( parseOccurence( occurence ) );
	}

	protected void handleFlag(AST flag) {
		if ( "fuzzy".equals( flag.getText() ) ) {
			currentMessage.markFuzzy();
		}
		else {
			currentMessage.addFormat( flag.getText() );
		}
	}

	protected void handlePreviousMsgctxt(AST previousMsgctxt) {
		currentMessage.setPrevMsgctx( extractText( previousMsgctxt ) );
	}

	protected void handlePreviousMsgid(AST previousMsgid) {
		currentMessage.setPrevMsgid( extractText( previousMsgid ) );
	}

	protected void handlePreviousMsgidPlural(AST previousMsgidPlural) {
		currentMessage.setPrevMsgidPlural( extractText( previousMsgidPlural ) );
	}

	protected void handleDomain(AST domain) {
		currentMessage.setDomain( extractText( domain ) );
	}

	protected void handleMsgctxt(AST msgctxt) {
		currentMessage.setMsgctxt( extractText( msgctxt ) );
	}

	protected void handleMsgid(AST msgid) {
		currentMessage.setMsgid( extractText( msgid ) );
	}

	protected void handleMsgidPlural(AST msgidPlural) {
		currentMessage.setMsgidPlural( extractText( msgidPlural ) );
	}

	protected void handleMsgstr(AST msgstr) {
		currentMessage.setMsgstr( extractText( msgstr ) );
		if(currentMessage.isFuzzy() && extractText( msgstr ).isEmpty()) {
			currentMessage.removeFuzzy();
		}
	}

	protected void handleMsgstrPlural(AST msgstr, AST plurality) {
		currentMessage.addMsgstrPlural( extractText( msgstr ), Integer.parseInt( plurality.getText() ) );
	}

	private String extractText(AST ast) {
		return ast == null ? "" : ast.getText();
	}

	private Occurence parseOccurence(AST ast) {
		String text = ast.getText();
		return new Occurence(text);
	}
}