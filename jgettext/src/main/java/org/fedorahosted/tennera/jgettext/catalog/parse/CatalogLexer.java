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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fedorahosted.tennera.jgettext.catalog.util.StringUtil;

import antlr.TokenStream;

/**
 * An Antlr lexer, hand-written to better match up with the processing of the GNU gettext po parser lexer.
 *
 * @author Steve Ebersole
 */
public class CatalogLexer implements TokenStream, CatalogTokenTypes {

	private final Iterator<antlr.Token> tokens;

	public CatalogLexer(File file) throws FileNotFoundException {
		tokens = Tokenizer.tokenize( file ).iterator();
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
	private static class Tokenizer {

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

		public static List<antlr.Token> tokenize(File file) throws FileNotFoundException {
			LineNumberReader ioReader = new LineNumberReader( new BufferedReader( new FileReader( file ) ) );
			try {
				Tokenizer me = new Tokenizer( file.getName(), ioReader );
				return me.buildTokens();
			}
			finally {
				try {
					ioReader.close();
				}
				catch ( Throwable ignore ) {
				}
			}
		}

		private final ArrayList<antlr.Token> tokens = new ArrayList<antlr.Token>();

		private final LineNumberReader ioReader;
		private final String filename;
		private int column;

		private EntryCollector entryCollector;

		public Tokenizer(String filename, LineNumberReader ioReader) {
			this.filename = filename;
			this.ioReader = ioReader;
		}

		private List<antlr.Token> buildTokens() {
			String line = readLine();
			while ( line != null ) {
				resetColumn();
				processLine( line );
				line = readLine();
			}
			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}
			return tokens;
		}

		private void resetColumn() {
			column = -1;
		}

		private String readLine() {
			try {
				return ioReader.readLine();
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

			if ( entryCollector != null ) {
				entryCollector.wrapUp();
				entryCollector = null;
			}

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
			tokens.add( new Token( FLAG, flag ) );
		}

		private void processOccurence(String occurence) {
			tokens.add( new Token( OCCURENCE, occurence ) );
		}

		private void processPreviousEntry(String entry) {
			processLine( entry );
			entryCollector.previous = true;
		}

		private void processObsolete(String entry) {
			tokens.add( new Token( OBSOLETE, "<obsolete>" ) );
			processLine( entry );
		}

		private void processExtractedComment(String comment) {
			tokens.add( new Token( EXTRACTION, comment ) );
		}

		private void processCatalogComment(String comment) {
			tokens.add( new Token( COMMENT, comment ) );
		}

		private void processContinuation(String line) {
			if ( entryCollector == null ) {
				throw new ParseException( "expecting continuation context", lineNumber() );
			}

			entryCollector.collect( interpretString( line ) );
		}

		private void processEntry(String line) {
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
			tokens.add( new Token( DOMAIN, domain ) );
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
					tokens.add( new Token( PREV_MSGCTXT, entry ) );
				}
				else {
					tokens.add( new Token( MSGCTXT, entry ) );
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
					tokens.add( new Token( PREV_MSGID, entry ) );
				}
				else {
					tokens.add( new Token( MSGID, entry ) );
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
					tokens.add( new Token( PREV_MSGID_PLURAL, entry ) );
				}
				else {
					tokens.add( new Token( MSGID_PLURAL, entry ) );
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
					tokens.add( new Token( MSGSTR, entry ) );
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
					tokens.add( new Token( MSGSTR_PLURAL, entry ) );
					tokens.add( new Token( PLURALITY, Integer.toString( n ) ) );
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
