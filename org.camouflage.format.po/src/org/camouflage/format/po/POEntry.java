package org.camouflage.format.po;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class POEntry {
	
	// header items
	private String comment;
	private String autoComment;
	private List<String> flags;
	private Map<String, POReference> references;
	private String prevMsgId;
	private String prevMsgIdPlural;
	private String prevMsgCtxt;
	private Map<String,String> unknownComment;
	private boolean obsolete;
	
	
	// source
	private String msgId;
	private String msgIdPlural;
	
	// targets
	private List<String> msgStrings;
	private String msgCtxt;
	
	
	
	public POEntry(){
		this.comment = null;
		this.autoComment = null;
		this.flags = new ArrayList<String>();
		this.references = new HashMap<String,POReference>();
		this.prevMsgId = null;
		this.prevMsgIdPlural = null;
		this.prevMsgCtxt = null;
		this.msgId = "";
		this.msgIdPlural = null;
		this.msgStrings = new ArrayList<String>();
		this.msgCtxt = null;
		this.unknownComment = new HashMap<String,String>();
		this.obsolete = false;
	}

	public POEntry(String msgId){
		this();
		this.msgId = msgId;
	}	
	
	public boolean isPlural(){
		return (this.msgIdPlural != null);
	}
	
	public boolean isHeader(){
		return (this.msgId.length() == 0);
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the autoComment
	 */
	public String getAutoComment() {
		return autoComment;
	}

	/**
	 * @param autoComment the autoComment to set
	 */
	public void setAutoComment(String autoComment) {
		this.autoComment = autoComment;
	}

	/**
	 * @return the flags
	 */
	public List<String> getFlags() {
		return flags;
	}

	/**
	 * @param flags the flags to set
	 */
	public void setFlags(List<String> flags) {
		this.flags = flags;
	}

	/**
	 * @return the references
	 */
	public Map<String,POReference> getReferences() {
		return references;
	}

	/**
	 * @param references the references to set
	 */
	public void setReferences(Map<String,POReference> references) {
		this.references = references;
	}

	/**
	 * @return the msgId
	 */
	public String getMsgId() {
		return msgId;
	}

	/**
	 * @param msgId the msgId to set
	 */
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	/**
	 * @return the msgIdPlural
	 */
	public String getMsgIdPlural() {
		return msgIdPlural;
	}

	/**
	 * @param msgIdPlural the msgIdPlural to set
	 */
	public void setMsgIdPlural(String msgIdPlural) {
		this.msgIdPlural = msgIdPlural;
	}

	public String getMsgStr(){
		if(this.msgStrings.size() ==0){
			return "";
		}
		return this.msgStrings.get(0);
	}

	public void setMsgStr(String msgStr){
		if(this.msgStrings.size() ==0){
			msgStrings.add(msgStr);
		}
		else{
			msgStrings.set(0, msgStr);
		}
	}

	public void setMsgStr(int index, String msgStr){
		if(! (this.msgStrings.size() > index) ){
			int toAdd = index - msgStrings.size() +1;
			for(int i=0;i<toAdd;i++){
				msgStrings.add("");
			}
		}
		msgStrings.set(index, msgStr);
	}
	
	/**
	 * @return the msgStrings
	 */
	public List<String> getMsgStrings() {
		return msgStrings;
	}

	/**
	 * @param msgStrings the msgStrings to set
	 */
	public void setMsgStrings(List<String> msgStrings) {
		this.msgStrings = msgStrings;
	}

	/**
	 * @return the msgCtxt
	 */
	public String getMsgCtxt() {
		return msgCtxt;
	}

	/**
	 * @param msgCtxt the msgCtxt to set
	 */
	public void setMsgCtxt(String msgCtxt) {
		this.msgCtxt = msgCtxt;
	}

	/**
	 * @return the prevMsgId
	 */
	public String getPrevMsgId() {
		return prevMsgId;
	}

	/**
	 * @param prevMsgId the prevMsgId to set
	 */
	public void setPrevMsgId(String prevMsgId) {
		this.prevMsgId = prevMsgId;
	}

	/**
	 * @return the prevMsgCtxt
	 */
	public String getPrevMsgCtxt() {
		return prevMsgCtxt;
	}

	/**
	 * @param prevMsgCtxt the prevMsgCtxt to set
	 */
	public void setPrevMsgCtxt(String prevMsgCtxt) {
		this.prevMsgCtxt = prevMsgCtxt;
	}
	
	public static String encodeMessage(String message){
		String result = message.replaceAll("\n", "\\\\n");
		return result;
	}

	public static String decodeMessage(String message){
		String result = message.replaceAll("\\\\n", "\n"); 	// \\n -> \n
		result = result.replaceAll("\\\\r", "\r"); 			// \\r -> \r
		result = result.replaceAll("\\\\t", "\t");			// \\t -> \t
		result = result.replaceAll("\\\\{2}", Matcher.quoteReplacement("\\"));
															// \\ -> \
		result = result.replaceAll("\\\\\"", "\"");			// \" -> "
		return result;
	}
	
	
	@Override
	public String toString() {
		POMarshaller m = new POMarshallerImpl();
		StringWriter writer = new StringWriter();
		try {
			m.marshall(this, writer);
		} catch (IOException e) {}
		
		return writer.toString();
	}

	/**
	 * @return the unknownComment
	 */
	public Map<String, String> getUnknownComment() {
		return unknownComment;
	}

	/**
	 * @param unknownComment the unknownComment to set
	 */
	public void setUnknownComment(Map<String, String> unknownComment) {
		this.unknownComment = unknownComment;
	}

	/**
	 * @return the prevMsgIdPlural
	 */
	public String getPrevMsgIdPlural() {
		return prevMsgIdPlural;
	}

	/**
	 * @param prevMsgIdPlural the prevMsgIdPlural to set
	 */
	public void setPrevMsgIdPlural(String prevMsgIdPlural) {
		this.prevMsgIdPlural = prevMsgIdPlural;
	}

	/**
	 * @return the obsolete
	 */
	public boolean isObsolete() {
		return obsolete;
	}

	/**
	 * @param obsolete the obsolete to set
	 */
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	
}

