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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.fedorahosted.tennera.jgettext.catalog.util.StringUtil;

import antlr.TokenStream;

/**
 * An Antlr lexer, hand-written to better match up with the processing of the GNU gettext po parser lexer.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("nls")
public class CatalogLexer implements TokenStream, CatalogTokenTypes {

	private final Iterator<antlr.Token> tokens;

	public CatalogLexer(File file) throws FileNotFoundException, IOException {
		tokens = Tokenizer.tokenize( file );
	}

	/**
	 * This is the main Antlr lexer contract.
	 * <p/>
	 * Here we are simply cycling through the Tokens we have already created and queued.
	 *
	 * @return The next token.
	 */
	public antlr.Token nextToken() {
		if ( !tokens.hasNext() ) {
			return new antlr.CommonToken( EOF, "<eof>" );
		}
		return tokens.next();
	}


	/**
	 * (Stateful) delegate to process and tokenize the incoming stream.
	 * <p/>
	 * Its main purpose for existence is to provided isolated lexer state.
	 */
	private static class Tokenizer implements Iterator<antlr.Token> {

		private class Token extends antlr.CommonToken {
			private Token(int i, String s) {
				super( i, s );
				super.setFilename( filename );
				super.setLine( lineNumber() );
				super.setColumn( column );
			}
		}

		public static final String DOMAIN_TXT = "domain";
		public static final String MSGCTXT_TXT = "msgctxt";
		public static final String MSGID_TXT = "msgid";
		public static final String MSGID_PLURAL_TXT = "msgid_plural";
		public static final String MSGSTR_TXT = "msgstr";
		public static final String MSGSTR_PLURAL_TXT = "msgstr[";

		public static Iterator<antlr.Token> tokenize(File file) 
			throws FileNotFoundException, IOException {
			LineNumberReader ioReader = new LineNumberReader( new FileReader( file ) );
			return new Tokenizer( file.getName(), ioReader );
		}

		private boolean eof = false;
		private final Queue<antlr.Token> tokenQueue = new LinkedList<antlr.Token>();

		private final LineNumberReader ioReader;
		private final String filename;
		private int column;

		private EntryCollector entryCollector;

		public Tokenizer(String filename, LineNumberReader ioReader) {
			this.filename = filename;
			this.ioReader = ioReader;
		}

		public boolean hasNext() {
			if ( !eof && tokenQueue.isEmpty() ) {
				readToken();
			}
			return !tokenQueue.isEmpty();
		}		

		/**
		 * Reads in one or more lines until at least one token is 
		 * encountered.
		 * Precondition: tokenQueue.isEmpty()
		 */
		private void readToken() {
			try {
				while ( !eof && tokenQueue.isEmpty() ) {
					String line = readLine();
					if ( line != null ) {
						resetColumn();
						processLine( line );
					} else {
						eof = true;
						// wrap up the final multi-line token, if any
						if ( entryCollector != null ) {
							entryCollector.wrapUp();
							entryCollector = null;
						}
						ioReader.close();
					}
				}
			} catch (IOException e) {
				try {
					ioReader.close();
				} catch (IOException e2) {
					throw new ParseException(e2.getMessage(), e2, lineNumber());
				}
				throw new ParseException(e.getMessage(), e, lineNumber());
			}
		}

		public antlr.Token next() {
			if ( hasNext() )
				return tokenQueue.remove();
			else
				throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		private void addToken(Token token) {
			tokenQueue.add(token);
		}

		private void resetColumn() {
			column = -1;
		}

		private String readLine() {
			try {
				String line = ioReader.readLine();
				return line;
			}
			catch ( IOException e ) {
				throw new ParseException( "unable to read line", lineNumber() );
			}
		}

		private int lineNumber() {
			return ioReader.getLineNumber();
		}

		private void processLine(String line) {
			line = line.trim();

			if ( line.length() == 0 ) {
				return;
			}

			if ( '\"' == line.charAt( 0 ) ) {
				processContinuation( line );
				return;
			}

//			if ( entryCollector != null ) {
//				entryCollector.wrapUp();
//				entryCollector = null;
//			}

			if ( '#' == line.charAt( 0 ) ) {
				processComment( line );
			}
			else {
				processEntry( line );
			}
		}

		private void processComment(String line) {
			if ( line.length() == 1 ) {
				processCatalogComment( "" );
				return;
			}

			switch ( line.charAt( 1 ) ) {
				case ',' :
					processFlag( line.substring( 2 ).trim() );
					break;
				case ':' :
					processOccurence( line.substring( 2 ).trim() );
					break;
				case '.' :
					processExtractedComment( line.substring( 2 ).trim() );
					break;
				case '|' :
					processPreviousEntry( line.substring( 2 ).trim() );
					break;
				case '~' :
					processObsolete( line.substring( 2 ).trim() );
					break;
				default:
					processCatalogComment( line.substring( 1 ) );
			}
		}

		private void processFlag(String flag) {
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			addToken( new Token( FLAG, flag ) );
		}

		private void processOccurence(String occurence) {
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			addToken( new Token( OCCURENCE, occurence ) );
		}

		private void processPreviousEntry(String entry) {
			processLine( entry );
			entryCollector.previous = true;
		}

		private void processObsolete(String entry) {
			//addToken( new Token( OBSOLETE, "<obsolete>" ) );
			//processLine( entry );
			entry = entry.trim();

			if ( entry.length() == 0 ) {
				return;
			}
			
			if ( '\"' == entry.charAt( 0 ) ){
				processContinuation( entry );
				return;
			}
			
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			
			addToken( new Token( OBSOLETE, "<obsolete>" ) );
			processEntry( entry );
		}

		private void processExtractedComment(String comment) {
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			addToken( new Token( EXTRACTION, comment ) );
		}

		private void processCatalogComment(String comment) {
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			addToken( new Token( COMMENT, comment ) );
		}

		private void processContinuation(String line) {
			if ( entryCollector == null ) {
				throw new ParseException( "expecting continuation context", lineNumber() );
			}

			entryCollector.collect( interpretString( line ) );
		}

		private void processEntry(String line) {
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			if ( line.startsWith( DOMAIN_TXT ) ) {
				processDomain( interpretString( line.substring( DOMAIN_TXT.length() ) ) );
			}
			else if ( line.startsWith( MSGCTXT_TXT ) ) {
				processMessageContext( interpretString( line.substring( MSGCTXT_TXT.length() ) ) );
			}
			else if ( line.startsWith( MSGID_PLURAL_TXT ) ) {
				processMsgidPlural( interpretString( line.substring( MSGID_PLURAL_TXT.length() ) ) );
			}
			else if ( line.startsWith( MSGSTR_PLURAL_TXT ) ) {
				int pos = line.indexOf( ']' );
				String n = line.substring( MSGSTR_PLURAL_TXT.length(), pos );
				processTranslationPlural( Integer.parseInt( n ), interpretString( line.substring( pos + 1 ) ) );
			}
			else if ( line.startsWith( MSGSTR_TXT ) ) {
				processTranslation( interpretString( line.substring( MSGSTR_TXT.length() ) ) );
			}
			else if ( line.startsWith( MSGID_TXT ) ) {
				processMsgid( interpretString( line.substring( MSGID_TXT.length() ) ) );
			}
			else {
				throw new UnexpectedTokenException( "unrecognized entry directive [" + line + "]", lineNumber() );
			}
		}

		private void processDomain(String domain) {
			addToken( new Token( DOMAIN, domain ) );
		}

		private void processMessageContext(String msgctxt) {
			newEntryCollection( new MsgctxtCollector ( msgctxt ) );
		}

		private void processMsgid(String msgid) {
			newEntryCollection( new MsgidCollector ( msgid ) );
		}

		private void processMsgidPlural(String msgidPlural) {
			newEntryCollection( new MsgidPluralCollector ( msgidPlural ) );
		}

		private void processTranslationPlural(int n, String translation) {
			newEntryCollection( new MsgstrPluralCollector( n, translation ) );
		}

		private void processTranslation(String translation) {
			newEntryCollection( new MsgstrCollector( translation ) );
		}

		private void newEntryCollection(EntryCollector entryCollector) {
			if ( this.entryCollector != null ) {
				throw new ParseException( "illegal state; continuation collector encountered on new collectible entry start", lineNumber() );
			}
			this.entryCollector = entryCollector;
		}

		private String interpretString(String quotedString) {
		    	quotedString = StringUtil.removeEscapes(quotedString);
			quotedString = quotedString.trim();
			// remove quotes:
			return quotedString.substring( 1, quotedString.length() - 1 );
		}

		private class MsgctxtCollector extends EntryCollector {
			private MsgctxtCollector(String initial) {
				super();
				collect( initial );
			}

			protected void wrapUp(String entry, boolean isPrevious) {
				if ( isPrevious ) {
					addToken( new Token( PREV_MSGCTXT, entry ) );
				}
				else {
					addToken( new Token( MSGCTXT, entry ) );
				}
			}
		}

		private class MsgidCollector extends EntryCollector {
			private MsgidCollector(String initial) {
				super();
				collect( initial );
			}

			protected void wrapUp(String entry, boolean isPrevious) {
				if ( isPrevious ) {
					addToken( new Token( PREV_MSGID, entry ) );
				}
				else {
					addToken( new Token( MSGID, entry ) );
				}
			}
		}

		private class MsgidPluralCollector extends EntryCollector {
			private MsgidPluralCollector(String initial) {
				super();
				collect( initial );
			}

			protected void wrapUp(String entry, boolean isPrevious) {
				if ( isPrevious ) {
					addToken( new Token( PREV_MSGID_PLURAL, entry ) );
				}
				else {
					addToken( new Token( MSGID_PLURAL, entry ) );
				}
			}
		}

		private class MsgstrCollector extends EntryCollector {
			private MsgstrCollector(String initial) {
				super();
				collect( initial );
			}

			protected void wrapUp(String entry, boolean isPrevious) {
				if ( isPrevious ) {
					throw new ParseException( "translation does not allow previous entry according to PO schematic", lineNumber() );
				}
				else {
					addToken( new Token( MSGSTR, entry ) );
				}
			}
		}

		private class MsgstrPluralCollector extends EntryCollector {
			private final int n;

			private MsgstrPluralCollector(int n, String initial) {
				super();
				this.n = n;
				collect( initial );
			}

			protected void wrapUp(String entry, boolean isPrevious) {
				if ( isPrevious ) {
					throw new ParseException( "translation does not allow previous entry according to PO schematic", lineNumber() );
				}
				else {
					addToken( new Token( MSGSTR_PLURAL, entry ) );
					addToken( new Token( PLURALITY, Integer.toString( n ) ) );
				}
			}
		}
	}

	private static abstract class EntryCollector {
		private final StringBuilder buffer = new StringBuilder();
		private boolean previous;

		public void collect(String entry) {
			buffer.append( entry );
		}

		protected abstract void wrapUp(String entry, boolean isPrevious);

		public void wrapUp() {
			wrapUp( buffer.toString(), previous );
		}
	}
}
