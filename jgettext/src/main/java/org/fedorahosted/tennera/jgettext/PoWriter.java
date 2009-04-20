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
 *                    Asgeir Frimannsson
 */
package org.fedorahosted.tennera.jgettext;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PoWriter {

	private boolean generateHeader = false;
	private boolean updatePOTCreationDate = false;
	private boolean updatePORevisionData = false;
	private boolean wrap = true;
	
	public void write(Catalog catalog, File file) throws IOException{
		Writer writer = new BufferedWriter(new FileWriter(file));
		write(catalog, writer);
	}

	public void write(Catalog catalog, Writer writer) throws IOException{
		Message header = catalog.locateHeader();
		boolean wroteHeader = false;
		if(header != null){
			write(header, writer);
			wroteHeader = true;
		}
		else if(generateHeader){
			write(HeaderFields.generateDefaultHeader(), writer);
			wroteHeader = true;
		}
		boolean isFirst = true;
		for(Message message: catalog){
			if(!message.isHeader()){
				if(isFirst){
					isFirst = false;

					if(wroteHeader){
						// we wrote the header, so need a blank line before
						// the first unit
						writer.write('\n');
					}
				}
				else{
					// we have already written a unit and are about to write another
					// so we need to add a line inbetween
					writer.write('\n');
				}
				
				write(message, writer);
			}
		}
		
	}

	public void write(Catalog catalog, OutputStream outputStream) throws IOException{
		write(catalog, outputStream, Charset.forName("UTF-8"));
	}
	
	public void write(Catalog catalog, OutputStream outputStream, Charset charset) throws IOException{
		Writer writer = new OutputStreamWriter(new BufferedOutputStream(outputStream),charset);
		write(catalog, writer);
	}

	
	public void write(Message message, File file) throws IOException{
		Writer writer = new BufferedWriter(new FileWriter(file));
		write(message, writer);
	}

	public void write(Message message, Writer writer) throws IOException{

    	for ( String comment : message.getComments() ) {
    	    writeComment("#", comment, writer); // no space on purpose!!!
    	}

    	for ( String comment : message.getExtractedComments() ) {
    	    writeComment("#. ", comment, writer);
    	}

    	for ( Occurence occurence : message.getOccurences() ) {
    	    writeComment("#: ", occurence.toString(), writer);
    	}

    	if ( message.isFuzzy() ) {
    		writer.write( "#, fuzzy" );
    		writer.write( '\n' );
    	}

    	for ( String format : message.getFormats() ) {
    		writer.write( "#, " + format );
    		writer.write( '\n' );
    	}

    	if ( message.getPrevMsgctx() != null ) {
    		writeMsgctxt( "#| ", message.getPrevMsgctx(), writer );
    	}

    	if ( message.getPrevMsgid() != null ) {
    		writeMsgid( "#| ", message.getPrevMsgid(), writer );
    	}

    	if ( message.getPrevMsgidPlural() != null ) {
    		writeMsgidPlural( "#| ", message.getPrevMsgidPlural(), writer );
    	}

    	String prefix = message.isObsolete() ? "#~ " : "";
    	if ( message.getMsgctxt() != null ) {
    		writeMsgctxt( prefix, message.getMsgctxt(), writer );
    	}

    	writeMsgid( prefix, message.getMsgid(), writer );

    	if ( message.getMsgidPlural() != null ) {
    		writeMsgidPlural( prefix, message.getMsgidPlural(), writer );
    	}

    	writeMsgstr( prefix, message.getMsgstr(), writer );

    	writeMsgstrPlurals( prefix, message.getMsgstrPlural(), writer );

    	writer.flush();
	}

	public void write(Message message, OutputStream outputStream) throws IOException{
		write(message, outputStream, Charset.forName("UTF-8"));
	}
	
	public void write(Message message, OutputStream outputStream, Charset charset) throws IOException{
		Writer writer = new OutputStreamWriter(new BufferedOutputStream(outputStream),charset);
		write(message, writer);
	}

	
	protected void writeComment(String prefix, String comment, Writer writer) throws IOException {
	    String[] lines = comment.split("\n");
	    for (String line : lines) {
		writer.write(prefix);
		writer.write(line);
		writer.write('\n');
	    }
	}

	protected void writeString(String s, Writer writer) throws IOException {
		writer.write('\"');
		
		// check if we should output a empty first line
		int firstLineEnd = s.indexOf('\n'); 
		if(wrap && 
				((firstLineEnd != -1 && firstLineEnd > 70 ) || s.length()> 70)){ 
			// TODO this depends on context, 70 ^^ is just a guess
			writer.write('\"');
			writer.write('\n');
			writer.write('\"');
		}
		
		StringBuilder currentLine = new StringBuilder(100);
		
		int lastSpacePos = 0;

		for(int i=0;i<s.length();i++){
			char currentChar = s.charAt(i);
		
			switch(currentChar){
			case '\n':
				currentLine.append('\\');
				currentLine.append('n');
				if(wrap && i != s.length()-1){
					writer.write(currentLine.toString());
					writer.write('\"');
					writer.write('\n');
					writer.write('\"');
					lastSpacePos = 0;
					currentLine.delete(0, currentLine.length());
				}
				break;
			case '\\':
				currentLine.append(currentChar);
				break;
			case '\r':
				currentLine.append('\\');
				currentLine.append('r');
				break;
			//case '=':
			case '"':
				currentLine.append('\\');
				currentLine.append(currentChar);
				break;
			case ' ':
				lastSpacePos = currentLine.length();
				currentLine.append(currentChar);
				break;
			default:
				currentLine.append(currentChar);
			}

			if(wrap && currentLine.length() > 76 && lastSpacePos != 0){
				writer.write(currentLine.substring(0, lastSpacePos+1));
				writer.write('\"');
				writer.write('\n');
				writer.write('\"');
				currentLine.delete(0, lastSpacePos+1);
				lastSpacePos = 0;
			}
		}
		writer.write(currentLine.toString());

		writer.write('\"');
		writer.write('\n');
	}

	protected void writeMsgctxt(String prefix, String ctxt, Writer writer) throws IOException {
		writer.write( prefix + "msgctxt ");
		writeString(ctxt, writer);
	}

	protected void writeMsgid(String prefix, String msgid, Writer writer) throws IOException {
		writer.write( prefix + "msgid ");
		writeString(msgid, writer);
	}

	protected void writeMsgidPlural(String prefix, String msgidPlural, Writer writer) throws IOException {
		writer.write( prefix + "msgid_plural ");
		writeString(msgidPlural, writer);
	}

	protected void writeMsgstr(String prefix, String msgstr, Writer writer) throws IOException {
		if ( msgstr == null ) {
			msgstr = "";
		}
		writer.write( prefix + "msgstr ");
		writeString(msgstr, writer);
	}

	protected void writeMsgstrPlurals(String prefix, List<String> msgstrPlurals, Writer writer) throws IOException {
		int i = 0;
		for ( String msgstr : msgstrPlurals ) {
			writer.write( prefix + "msgstr[" + i + "] ");
			writeString(msgstr, writer);
			i++;
		}
	}
	
	
}
