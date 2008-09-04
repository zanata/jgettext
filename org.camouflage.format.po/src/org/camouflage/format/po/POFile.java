package org.camouflage.format.po;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class POFile {

	private String filename;
	private String sourceLanguage;
	private String targetLanguage;
	
	private List<POEntry> entries;

	public POFile(){
		entries = new ArrayList<POEntry>();
	}
	public boolean hasHeader(){
		return (entries.size() ==0) ? false : entries.get(0).isHeader();
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
	 * @return the entries
	 */
	public List<POEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(List<POEntry> entries) {
		this.entries = entries;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public static void main(String[] args) {
		POFile file = new POFile();
		POEntry e = new POEntry("hello world");
		e.setMsgIdPlural("this is the plural msgid");
		e.setComment("this is a \nmultiline comment");
		e.setMsgStr("this is the \nmsgstring");
		e.setMsgStr(0,"this is string 0");
		e.setMsgStr(4,"this is string 4");
		e.setMsgStr(2,"this is string 2");
		e.setMsgStr(18,"this is string 18");
		//e.getReferences().add("myfile:23");
		//e.getReferences().add("terer:sdfasdf:dsfsdf");
		e.getUnknownComment().put("~", "hello world");
		e.getUnknownComment().put("~", null);
		file.getEntries().add(e);
		

		file.getEntries().add(e);
		System.out.println(file.toString());
	}
	public String getSourceLanguage() {
		return sourceLanguage;
	}
	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}
	public String getTargetLanguage() {
		return targetLanguage;
	}
	public void setTargetLanguage(String targetLanguage) {
		this.targetLanguage = targetLanguage;
	}
}
