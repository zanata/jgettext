package org.fedorahosted.tennera.jgettext.catalog.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.Occurence;

import antlr.ASTPair;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

public class MessageStreamParser{

	private InternalMessageStreamParser internalParser;
	
	public MessageStreamParser(File file) throws FileNotFoundException, IOException{
		internalParser = new InternalMessageStreamParser(file);
	}
	
	private class InternalMessageStreamParser extends CatalogParser{
		
		private Message currentMessage = new Message();
		private Message nextMessage;

		public InternalMessageStreamParser(File file) throws FileNotFoundException, IOException {
			super( new CatalogLexer( file ) );
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

		protected boolean nextMessageBlock() throws RecognitionException, TokenStreamException {
			
			boolean ret = false;
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST messageBlocks_AST = null;
			
			try { // for error handling
				if ((_tokenSet_1.member(LA(1)))) {
					messageBlock();
					astFactory.addASTChild(currentAST, returnAST);
					ret = true;
				}
				else {
					reportWarning("No more entries!");
				}
				messageBlocks_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			}
			returnAST = messageBlocks_AST;
			
			return ret;
		}
		
		
		protected void handleMessageBlock(AST messageBlock) {
			nextMessage = currentMessage;
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

		public boolean hasNext() {
			if(nextMessage != null){
				return true;
			}
			
			boolean next = true;
			try{
				next = nextMessageBlock();
			}
			catch(RecognitionException e){
				currentRecognitionException = e;
			}
			catch(TokenStreamException e){
				currentTokenStreamException = e;
			}
			
			return next; 
		}

		private RecognitionException currentRecognitionException = null;
		private TokenStreamException currentTokenStreamException = null;
		
		private void checkExceptions() throws RecognitionException, TokenStreamException{
			if(currentRecognitionException != null){
				RecognitionException e = currentRecognitionException;
				currentRecognitionException = null;
				throw e;
			}
			else if(currentTokenStreamException != null){
				TokenStreamException e = currentTokenStreamException;
				currentTokenStreamException = null;
				throw e;
			}
			
		}
		
		public Message next() throws RecognitionException, TokenStreamException{
			checkExceptions();
			
			if(!hasNext()){
				return null;
			}
			
			Message next = nextMessage;
			nextMessage = null;
			return next;
		}
	}

	public boolean hasNext() {
		return internalParser.hasNext();
	}

	public Message next() throws RecognitionException, TokenStreamException {
		return internalParser.next();
	}
}
