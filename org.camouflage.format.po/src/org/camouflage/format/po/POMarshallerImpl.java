package org.camouflage.format.po;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


public class POMarshallerImpl implements POMarshaller {

	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POMarshaller#marshall(org.camouflage.format.po.POFile, java.io.Writer)
	 */
	public void marshall(POFile file, Writer writer) throws IOException{
		for(POEntry e:file.getEntries()){
			marshall(e,	 writer);
			writer.write("\n");
		}
		
	}

	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POMarshaller#marshall(org.camouflage.format.po.POFile, java.io.OutputStream)
	 */
	public void marshall(POFile file, OutputStream stream) throws IOException{
		Writer w = new PrintWriter(stream);
		marshall(file,w);
		w.close();
	}	
	
	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POMarshaller#marshall(org.camouflage.format.po.POEntry, java.io.OutputStream)
	 */
	public void marshall(POEntry entry, OutputStream stream) throws IOException{
		Writer w = new PrintWriter(stream);
		marshall(entry,w);
		w.close();
	}
	
	/* (non-Javadoc)
	 * @see org.camouflage.format.po.POMarshaller#marshall(org.camouflage.format.po.POEntry, java.io.Writer)
	 */
	public void marshall(POEntry entry, Writer writer) throws IOException{

		boolean noWrap = entry.getFlags().indexOf("no-wrap") != -1; 
		
		String comment = entry.getComment();
		if(comment !=null){
			String[] commentLines = comment.split("\n");
			for(String c :commentLines){
				if(entry.isObsolete()){
					writer.write("#~ ");
				}
				writer.write("# ");
				writer.write(c);
				writer.write("\n");
			}
		}
		
		String autoComment = entry.getAutoComment();
		if(autoComment !=null){
			String[] autoCommentLines = autoComment.split("\n");
			for(String c :autoCommentLines){
				if(entry.isObsolete()){
					writer.write("#~ ");
				}
				writer.write("#. ");
				writer.write(c);
				writer.write("\n");
			}
		}
		
		Map<String,POReference> references = entry.getReferences();
		if(references.size() !=0){
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("#: ");
			writeReferences(references, writer);
			writer.write("\n");
		}
		
		List<String> flags = entry.getFlags();
		if(flags.size() !=0){
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("#, ");
			writeFlags(flags, writer);
			writer.write("\n");
		}
		
		String prevMsgId = entry.getPrevMsgId();
		if(prevMsgId != null){
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("#| msgid \"");
			writer.write(prevMsgId);
			writer.write("\"\n");
		}
		
		String prevMsgIdPlural = entry.getPrevMsgIdPlural();
		if(prevMsgIdPlural != null){
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("#| msgid_plural \"");
			writer.write(prevMsgIdPlural);
			writer.write("\"\n");
		}
		
		String prevMsgCtxt = entry.getPrevMsgCtxt();
		if(prevMsgCtxt != null){
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("#| msgctxt \"");
			writer.write(prevMsgCtxt);
			writer.write("\"\n");
		}
		
		Map<String,String> unknownComment = entry.getUnknownComment();
		
		if(unknownComment.size() !=0){
			for(String key: unknownComment.keySet()){
				String commentLine = unknownComment.get(key);
				if(commentLine !=null){
					String[] commentLines = commentLine.split("\n");
					for(String c :commentLines){
						if(entry.isObsolete()){
							writer.write("#~ ");
						}
						writer.write("#");
						writer.write(key);
						writer.write(" ");
						writer.write(c);
						writer.write("\n");
					}
				}
			}
		}
		
		//msgctxt
		String msgCtxt = entry.getMsgCtxt();
		if(msgCtxt != null){
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("msgctxt \"");
			writer.write(msgCtxt);
			writer.write("\"\n");
		}

		// msgid
		String msgId = entry.getMsgId();
		msgId = POEntry.encodeMessage(msgId);
		if(entry.isObsolete()){
			writer.write("#~ ");
		}
		writer.write("msgid ");
		writeMessage(msgId,writer, noWrap, entry.isObsolete());
		
		//msgid_plural
		String msgIdPlural = entry.getMsgIdPlural();
		if(msgIdPlural == null){
			
			String msgStr = POEntry.encodeMessage(entry.getMsgStr());
			// not a plural message, output msgstr
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("msgstr ");
			writeMessage(msgStr, writer, noWrap, entry.isObsolete());
		}
		else{ // msgid_plural
			
			msgIdPlural = POEntry.encodeMessage(msgIdPlural);
			if(entry.isObsolete()){
				writer.write("#~ ");
			}
			writer.write("msgid_plural ");
			writeMessage(msgIdPlural,writer, noWrap, entry.isObsolete());
			
			List<String> msgStrings = entry.getMsgStrings();
			for(int i=0;i<msgStrings.size();i++){
				String msgStr = POEntry.encodeMessage(msgStrings.get(i));
				if(entry.isObsolete()){
					writer.write("#~ ");
				}
				writer.write("msgstr[");
				writer.write(String.valueOf(i));
				writer.write("] ");
				writeMessage(msgStr,writer, noWrap, entry.isObsolete());
			}
		}
		
	}

	private void writeFlags(List<String> flags, Writer writer) throws IOException {

		for(int i=0;i<flags.size();i++){
			writer.write(flags.get(i));
			if(i != flags.size()-1)
				writer.write(", ");
		}
		
	}

	private void writeReferences(Map<String,POReference> references, Writer writer) throws IOException {
		
		StringBuffer refString = new StringBuffer(64);
		Collection<POReference> refs = references.values();
		for (POReference reference : refs) {
			for (String location : reference.getLocations()) {
				refString.append(reference.getFile());
				refString.append(":");
				refString.append(location);
				refString.append(" ");
			}
		}
		writer.write(refString.toString().trim());
		
	}

	private void writeMessage(String message, Writer writer, boolean noWrap, boolean obsolete) throws IOException{

		
		if(noWrap || isNoWrap()){
			if(isIndent() && !obsolete){
				writer.write("\t");
			}
			writer.write("\"");
			writer.write(message);
			writer.write("\"\n");
			return;
		}
		
		String[] list = message.split(Pattern.quote("\\n"));

		final int LINE_WIDTH = 76; // START_QUOTE + 76 CHARS + BACKSLASH + LOWERCASE_N + END_QUOTE = 80
		
		// make first line start on a new line?
		boolean skipFirst = true;
		if(skipFirst){
			if(list[0].length() > LINE_WIDTH){
				writer.write("\"\"\n");
			}
		}
		
		for(int i=0;i<list.length;i++){

			String listItem = list[i];
			
			String[] tokens = StringUtils.splitPreserveAllTokens(listItem, ' ');
			
			if(i == 0 && skipFirst){
				// ignore
			}
			else if(obsolete){
				writer.write("#~ ");
			}
			if(isIndent() && !obsolete){
				writer.write("\t");
			}
			writer.write("\"");
			
			int currLen = 0;
			for (int j = 0; j < tokens.length; j++) {
				int tokLen = tokens[j].length();

				if(currLen + tokLen+1 > LINE_WIDTH){
					writer.write("\"\n");
					if(obsolete){
						writer.write("#~ ");
					}
					else if(isIndent()){
						writer.write("\t");
					}
					writer.write("\"");
					currLen = 0;
				}
				
				writer.write(tokens[j]);
				
				if(j != tokens.length-1)
					writer.write(" ");
				
				currLen += tokLen+1;
			}
			if(i != list.length-1){
				writer.write("\\n");
			}
			writer.write("\"\n");
		}
	}
	
	
    /* (non-Javadoc)
	 * @see org.camouflage.format.po.POMarshaller#setProperty(java.lang.String, java.lang.Object)
	 */
    public void setProperty( String name, Object value ) 
    	throws PropertyException{
    	
        if( name == null ) {
            throw new IllegalArgumentException("Argument name must not be nullname");
        }
    	if(PO_NOWRAP.equals(name)){
    		checkBoolean(name,value);
    		setNoWrap((Boolean)value);
    	}
    	else if(PO_INDENT.equals(name)){
    		checkBoolean(name,value);
    		setIndent((Boolean)value);
    	}
    	
    }
    
    /* (non-Javadoc)
	 * @see org.camouflage.format.po.POMarshaller#getProperty(java.lang.String)
	 */
    public Object getProperty( String name ) throws PropertyException{
        if( name == null ) {
            throw new IllegalArgumentException("Argument name must not be null");
        }   
        if(PO_NOWRAP.equals(name)){
        	return isNoWrap();
        }else if(PO_INDENT.equals(name)){
        	return isIndent();
        } 
        
        throw new PropertyException(name);
    }

	
	private boolean noWrap = false;
	private boolean indent = false;

	/**
	 * @return the wrapLines
	 */
	public boolean isNoWrap() {
		return noWrap;
	}

	/**
	 * @param noWrap the wrapLines to set
	 */
	public void setNoWrap(boolean noWrap) {
		this.noWrap = noWrap;
	}
	
	
    /*
     * assert that the given object is a Boolean
     */
    private void checkBoolean( String name, Object value ) throws PropertyException {
        if(!(value instanceof Boolean))
            throw new PropertyException("value of "+ name +" must be boolean");
    }

	/**
	 * @return the indent
	 */
	public boolean isIndent() {
		return indent;
	}

	/**
	 * @param indent the indent to set
	 */
	public void setIndent(boolean indent) {
		this.indent = indent;
	}
	
}
