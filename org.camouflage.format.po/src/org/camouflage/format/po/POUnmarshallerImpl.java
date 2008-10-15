package org.camouflage.format.po;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

public class POUnmarshallerImpl implements POUnmarshaller {

	private static final int MODE_NONE= 0;
	private static final int MODE_COMMENT = 1;
	private static final int MODE_PREV_MSGCTXT = 2;
	private static final int MODE_PREV_MSGID = 3;
	private static final int MODE_PREV_MSGID_PLURAL = 4;
	private static final int MODE_MSGCTXT = 5;
	private static final int MODE_MSGID = 6;
	private static final int MODE_MSGID_PLURAL = 7;
	private static final int MODE_MSGSTR = 8;
	private static final int MODE_MSGSTR_PLURAL = 9;
	
	// state variables
	private int mode = MODE_NONE;
	private int pluralIndex = 0;
	private POEntry entry = null;
	private POFile file = null;
	
	private static final int BUFFER_SIZE = 1000;

	private void cleanUp(){
		setFile(null);
		setEntry(null);
		setMode(MODE_NONE);
		setPluralIndex(0);
	}

	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POUnmarshaller#unmarshall(java.io.InputStream)
	 */
	public POFile unmarshall(InputStream input) throws ParseException{

		POFile file = new POFile();
		setFile(file);
				
		// we use a InputStream rather than a reader so that we can change encoding
		// based on the PO header charset value
		InputStream stream = new BufferedInputStream(input);
		
		ByteBuffer line = ByteBuffer.allocate(BUFFER_SIZE);
		
		int lineCount = 0;

		try {
			int read = stream.read();

			while(read != -1){
				char ch = (char)read;
				
				// Line endings:
				//  - Windows:   \r\n
				//  - Mac        \r
				//  - Unix       \n
				//
				if(ch == '\r' || ch == '\n'){ 
					++lineCount;
					String strLine = decode(line);
					
					try{
						parseLine(strLine, false);
					}
					catch(ParseException e){
						e.setLine(lineCount);
						cleanUp();
						throw e;
					}
					
					line = ByteBuffer.allocate(BUFFER_SIZE);

					// do a read ahead to see if it's a windows line ending, 
					// and skip the extra '\n' if so. 
					if(ch == '\r'){
						stream.mark(1);
						read = stream.read();
						ch = (char)read;
						stream.reset();
						if(ch == '\n'){ // it is a windows line ending, ignore the following '\n' character
							stream.skip(1);
						}
					}
				} 
				else{ // not a line ending - add to buffer
					// expand buffer if full
					if(!line.hasRemaining()){ 
						ByteBuffer old = line;
						line = ByteBuffer.allocate(old.capacity()+ BUFFER_SIZE);
						line.put(old.array());
					}
					
					line.put((byte)read);
				}
				
				read = stream.read();
			}

			if(line.position() != 0){
				// TODO file ends without a newline, throw warning? 
				++lineCount;
				String strLine = decode(line);
				
				try{
					parseLine(strLine, false);
				}
				catch(ParseException e){
					e.setLine(lineCount);
					cleanUp();
					throw e;
				}
			}

			// end of file, check if we're in a non-complete entry
			if(getMode() < MODE_MSGSTR && getMode() > MODE_COMMENT){
				// TODO if the file ends with a comment, the comment is ignored and discarded. give warning?
				throw new ParseException("Unexpected end of input");
			}
		} catch (IOException e) {
			throw new ParseException("Unexpected I/O error", e);
		}
		finally{
			cleanUp();
		}

		return file;
		
	}
	
	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POUnmarshaller#unmarshall(java.lang.String)
	 */
	public POFile unmarshall(String filename) throws FileNotFoundException, ParseException{
		
		try {
			InputStream stream = new BufferedInputStream(new FileInputStream(filename));
			POFile retFile = unmarshall(stream);
			retFile.setFilename(filename);
			stream.close();
			return retFile;
		} catch (IOException e) {
			throw new ParseException("Unexpected I/O error", e);
		}


	
	}

	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POUnmarshaller#unmarshall(java.lang.String)
	 */
	public POFile unmarshall(File file) throws FileNotFoundException, ParseException{
		
		try {
			InputStream stream = new BufferedInputStream(new FileInputStream(file));
			POFile retFile = unmarshall(stream);
			retFile.setFilename(file.getName());
			stream.close();
			return retFile;
		} catch (IOException e) {
			throw new ParseException("Unexpected I/O error", e);
		}


	
	}

	private void parseLine(String line, boolean obsolete) throws ParseException{

		line = line.trim();
		if(line.isEmpty()){
			return;
		}
		
		if(line.startsWith("#")){
			if(line.startsWith("# ")){
				parseCommentLine(line, obsolete);
			}
			else if(line.startsWith("#.")){
				parseAutoCommentLine(line, obsolete);
			}
			else if(line.startsWith("#,")){
				parseFlagLine(line, obsolete);
			}
			else if(line.startsWith("#:")){
				parseReferenceLine(line, obsolete);
			}
			else if(line.startsWith("#| msgid ")){
				parsePrevMsgIdLine(line, obsolete);
			}
			else if(line.startsWith("#| msgid_plural")){
				parsePrevMsgIdPluralLine(line, obsolete);
			}
			else if(line.startsWith("#| msgctxt")){				
				parsePrevMsgCtxtLine(line, obsolete);
			}
			else if(line.startsWith("#| ")){				
				parseMsgLine(line, obsolete);
			}
			else if(!obsolete && line.startsWith("#~")){				
				parseObsoleteLine(line);
			}
			else {
				parseUnknownCommentLine(line, obsolete);
			}
			
		}
		else if(line.startsWith("msgid ")){
			parseMsgIdLine(line, obsolete);
		}
		else if(line.startsWith("msgid_plural ")){
			parseMsgIdPluralLine(line, obsolete);
		}
		else if(line.startsWith("msgctxt ")){
			parseMsgCtxtLine(line, obsolete);
		}
		else if(line.startsWith("msgstr ")){
			parseMsgStrLine(line, obsolete);
		}
		else if(line.startsWith("msgstr[")){
			parseMsgStrPlural(line, obsolete);
		}
		else if(line.startsWith("\"")){
			parseMsgLine(line, obsolete);
		}
		else{
			throw new ParseException("Unexpected token: " + line );
		}
		
	}

	private void checkObsolete(boolean obsolete) throws ParseException{
		if(getEntry().isObsolete() && !obsolete){
			throw new ParseException("Unexpected non-obsolete line in obsolete entry");
		}
		else if(!getEntry().isObsolete() && obsolete){
			throw new ParseException("Unexpected obsolete line in non-obsolete entry");
		}
	}
	private void parseMsgLine(String line, boolean obsolete) throws ParseException{
		
		checkObsolete(obsolete);
		String text = extractMsgLine(line);
		
		POEntry entry = getEntry();
		
		switch(getMode()){
		case MODE_PREV_MSGCTXT:
			entry.setPrevMsgCtxt(entry.getPrevMsgCtxt()+ text);
			break;
		case MODE_PREV_MSGID:
			entry.setPrevMsgId(entry.getPrevMsgId()+ text);
			break;
		case MODE_PREV_MSGID_PLURAL:
			entry.setPrevMsgIdPlural(entry.getPrevMsgIdPlural()+text);
			break;
		case MODE_MSGCTXT:
			entry.setMsgCtxt(entry.getMsgCtxt()+ text);
			break;
		case MODE_MSGID:
			entry.setMsgId(entry.getMsgId()+ text);
			break;
		case MODE_MSGID_PLURAL:
			entry.setMsgIdPlural(entry.getMsgIdPlural()+text);
			break;
		case MODE_MSGSTR:
			if(entry.isHeader()){
				modifyCharset();
			}
			entry.setMsgStr(entry.getMsgStr()+text);
			break;
		case MODE_MSGSTR_PLURAL:
			int pluralIndex = getPluralIndex();
			if(entry.getMsgStrings().size() <= pluralIndex){
				entry.getMsgStrings().add(text);
			}
			else{
				String current = entry.getMsgStrings().get(pluralIndex);
				text = (current == null) ? text : current+text;
				entry.setMsgStr(pluralIndex, text);
			}
			break;
		default:
			throw new ParseException("Unexpected token: " + line );
		}
	}
	
	private String extractMsgLine(String line) throws ParseException {
		if(line.startsWith("\"") && line.endsWith("\"")){
			line = line.substring(1,line.length()-1);
			line = POEntry.decodeMessage(line); 
			
			return line;
		}
		else if(line.startsWith("#|")){
			String lineTrim = line.substring(2).trim();
			return extractMsgLine(lineTrim);
		}
		else{
			throw new ParseException("Unexpected token: " + line );
		}
		
	}

	private void parseCommentLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}

		setMode(MODE_COMMENT);
		
		line = line.substring(2).trim();

		String currValue = getEntry().getComment();
		currValue = (currValue == null) ? line : currValue +"\n" + line;
		getEntry().setComment(currValue);
		
	}

	private void parseUnknownCommentLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_COMMENT);
		
		line = line.substring(1).trim();

		String split[] = line.split(" ",2);
		
		String key = split[0];
		String value = (split.length == 1) ? "" : line.substring(key.length());
		
		String currValue = getEntry().getUnknownComment().get(key);
		
		currValue = (currValue == null) ? value : currValue + value;
	
		getEntry().getUnknownComment().put(key, currValue);
	}

	private void parseAutoCommentLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_COMMENT);

		line = line.substring(2).trim();
		
		String currValue = getEntry().getAutoComment();
		currValue = (currValue == null) ? line : currValue +"\n" + line;
		getEntry().setAutoComment(currValue);
		
	}

	private void parseReferenceLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_COMMENT);
		
		line = line.substring(2).trim();
		getEntry().addReference(line);
	}

	private void parseFlagLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_COMMENT);
		
		line = line.substring(2).trim();

		StringTokenizer tok = new StringTokenizer(line, ",");
		while(tok.hasMoreTokens()){
			String flag = tok.nextToken().trim();
			getEntry().getFlags().add(flag);
		}
	
	}

	private void parsePrevMsgCtxtLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_PREV_MSGCTXT);

		line = line.substring(10).trim();
		
		String text = extractMsgLine(line);
		getEntry().setPrevMsgCtxt(text);
	
		
	}

	private void parsePrevMsgIdLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_PREV_MSGID);
		
		line = line.substring(9).trim();

		String text = extractMsgLine(line);
		getEntry().setPrevMsgId(text);
	
	}

	private void parsePrevMsgIdPluralLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // cannot have a comment between between msgid and last msgstr
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_PREV_MSGID_PLURAL);

		line = line.substring(16).trim();
		
		String text = extractMsgLine(line);
		getEntry().setPrevMsgIdPlural(text);
	
	}

	private void parseObsoleteLine(String line) throws ParseException {
		line = line.substring(2).trim();
		parseLine(line,true);
	}

	private void parseMsgCtxtLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
		}
		else if(getMode() >= MODE_MSGID){ // msgctxt must come before msgid
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_MSGCTXT);
		
		line = line.substring(8).trim();

		String text = extractMsgLine(line);
		String current = getEntry().getMsgCtxt();
		text = (current==null)? text : current+text;
		getEntry().setMsgCtxt(text);

		
	}


	private void parseMsgIdLine(String line, boolean obsolete) throws ParseException {

		if(getMode() >= MODE_MSGSTR || getMode() == MODE_NONE){ // starting a new entry
			initializeNewEntry(obsolete);
				
		}
		else if(getMode() == MODE_MSGID){ // cannot have two msgid's after each other
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_MSGID);
		
		line = line.substring(6).trim();

		String text = extractMsgLine(line);
		getEntry().setMsgId(getEntry().getMsgId()+ text);
	}

	private void parseMsgIdPluralLine(String line, boolean obsolete) throws ParseException {

		if(getMode() != MODE_MSGID){ // cannot have two msgid's after each other
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_MSGID_PLURAL);

		line = line.substring(13).trim();
		
		String text = extractMsgLine(line);
		String current = getEntry().getMsgIdPlural();
		text = (current==null)? text : current+text;
		getEntry().setMsgIdPlural(text);
	
	}

	private void parseMsgStrLine(String line, boolean obsolete) throws ParseException {

		if(getMode() != MODE_MSGID){ // check for a msgid
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_MSGSTR);
		
		line = line.substring(7).trim();

		String text = extractMsgLine(line);
		String current = getEntry().getMsgStr();
		text = (current==null)? text : current+text;
		getEntry().setMsgStr(text);
	}

	private void parseMsgStrPlural(String line, boolean obsolete) throws ParseException {
		
		if(getMode() < MODE_MSGID_PLURAL){ // check for a msgid
			throw new ParseException("Unexpected token: " + line );
		}
		else{
			checkObsolete(obsolete);
		}
		
		setMode(MODE_MSGSTR_PLURAL);

		line = line.substring(7).trim();
		
		int end = line.indexOf(']');
		int index = 0;
		try{
			index = Integer.parseInt(line.substring(0, end));
		}
		catch(NumberFormatException nfe){
			throw new ParseException("Unexpected token: " + line );
		}
		
		line = line.substring(end+1).trim();

		int pluralIndex = index;
		setPluralIndex(pluralIndex);

		String text = extractMsgLine(line);
		POEntry entry = getEntry();
		
		if(entry.getMsgStrings().size() <= pluralIndex){
			entry.getMsgStrings().add(text);
		}
		else{
			String current = entry.getMsgStrings().get(pluralIndex);
			text = (current == null) ? text : current+text;
			entry.setMsgStr(pluralIndex, text);
		}
		
	}

	Charset currentCharset = null;
	
	private void modifyCharset() throws ParseException{
		
		if(currentCharset != null) return;
		
		if(isOverrideCharset()){
			currentCharset = defaultCharset;
		}
		
		// we can now correctly set the encoding of the rest of the entries
		// based on the header value, if it exists
		String charset = POHeaderUtil.getCharset(getEntry());
		if(charset == null){
			return; 
		}
		if(Charset.isSupported(charset)){
			currentCharset = Charset.forName(charset);
		}
		else{
			if(isIgnoreInvalidCharset()){
				currentCharset = defaultCharset;
			}
			else
				throw new ParseException("Invalid charset specified: "+ charset);
		}
	}
	
	private String decode(ByteBuffer original) throws ParseException{
		if(currentCharset == null)
			return new String(original.array(), defaultCharset);
		
		return new String(original.array(), currentCharset);
	}
	
	private void initializeNewEntry(boolean obsolete) throws ParseException{
		if(getEntry() !=null){
			if(getEntry().isHeader()){
				modifyCharset();
			}
		}
		POEntry e = new POEntry();
		e.setObsolete(obsolete);
		e.setObsolete(obsolete);
		getFile().getEntries().add(e);
		setEntry(e);
	}

	public static void main(String[] args) {
		try {
			POUnmarshaller parser = new POUnmarshallerImpl();
			parser.setProperty(POUnmarshaller.OVERRIDE_CHARSET, true);
			parser.setProperty(POUnmarshaller.DEFAULT_CHARSET, Charset.forName("iso-8859-1"));
			POFile file = parser.unmarshall(args[0]);
			POMarshaller marshaller = new POMarshallerImpl();
			marshaller.setProperty(POMarshaller.PO_NOWRAP, false);
			marshaller.setProperty(POMarshaller.PO_INDENT, true);
			marshaller.marshall(file, System.out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PropertyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the mode
	 */
	private int getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	private void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public Object getProperty(String name) throws PropertyException {
        if( name == null ) {
            throw new IllegalArgumentException("Argument name must not be null");
        }   
        if(DEFAULT_CHARSET.equals(name)){
        	return getDefaultCharset();
        }else if(IGNORE_INVALID_CHARSET.equals(name)){
        	return isIgnoreInvalidCharset();
        }else if(OVERRIDE_CHARSET.equals(name)){
        	return isOverrideCharset();
        } 
        
        throw new PropertyException(name);
	}

	private Charset defaultCharset = Charset.forName("UTF-8");
	private boolean ignoreInvalidCharset = true;
	private boolean overrideCharset = false;
	
	
	@Override
	public void setProperty(String name, Object value) throws PropertyException {
        	
            if( name == null ) {
                throw new IllegalArgumentException("Argument name must not be nullname");
            }
        	if(DEFAULT_CHARSET.equals(name)){
        		checkCharset(name,value);
        		setDefaultCharset((Charset)value);
        	}
        	else if(IGNORE_INVALID_CHARSET.equals(name)){
        		checkBoolean(name,value);
        		setIgnoreInvalidCharset((Boolean)value);
        	}
        	else if(OVERRIDE_CHARSET.equals(name)){
        		checkBoolean(name,value);
        		setOverrideCharset((Boolean)value);
        	}
	}

	/*
     * assert that the given object is a Boolean
     */
    private void checkCharset( String name, Object value ) throws PropertyException {
        if(!(value instanceof Charset))
            throw new PropertyException("value of "+ name +" must be a Charset");
    }

	/*
     * assert that the given object is a Boolean
     */
    private void checkBoolean( String name, Object value ) throws PropertyException {
        if(!(value instanceof Boolean))
            throw new PropertyException("value of "+ name +" must be boolean");
    }

	/**
	 * @return the defaultCharset
	 */
	private Charset getDefaultCharset() {
		return defaultCharset;
	}

	/**
	 * @param defaultCharset the defaultCharset to set
	 */
	private void setDefaultCharset(Charset defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	/**
	 * @return the ignoreInvalidCharset
	 */
	private boolean isIgnoreInvalidCharset() {
		return ignoreInvalidCharset;
	}

	/**
	 * @param ignoreInvalidCharset the ignoreInvalidCharset to set
	 */
	private void setIgnoreInvalidCharset(boolean ignoreInvalidCharset) {
		this.ignoreInvalidCharset = ignoreInvalidCharset;
	}

	/**
	 * @return the overrideCharset
	 */
	private boolean isOverrideCharset() {
		return overrideCharset;
	}

	/**
	 * @param overrideCharset the overrideCharset to set
	 */
	private void setOverrideCharset(boolean overrideCharset) {
		this.overrideCharset = overrideCharset;
	}

	/**
	 * @return the file
	 */
	private POFile getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	private void setFile(POFile file) {
		this.file = file;
	}

	/**
	 * @return the entry
	 */
	private POEntry getEntry() {
		return entry;
	}

	/**
	 * @param entry the entry to set
	 */
	private void setEntry(POEntry entry) {
		this.entry = entry;
	}

	/**
	 * @return the pluralIndex
	 */
	private int getPluralIndex() {
		return pluralIndex;
	}

	/**
	 * @param pluralIndex the pluralIndex to set
	 */
	private void setPluralIndex(int pluralIndex) {
		this.pluralIndex = pluralIndex;
	}
	
	
}
