package org.fedorahosted.tennera.jgettext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;

@SuppressWarnings("nls")
public class HeaderFields {

	private Map<String, String> entries = new LinkedHashMap<String, String>();

	public static final String KEY_ProjectIdVersion = "Project-Id-Version: PACKAGE VERSION\n";
	public static final String KEY_ReportMsgidBugsTo = "Report-Msgid-Bugs-To";
	public static final String KEY_PotCreationDate = "POT-Creation-Date";
	public static final String KEY_PoRevisionDate = "PO-Revision-Date";
	public static final String KEY_LastTranslator = "Last-Translator";
	public static final String KEY_LanguageTeam = "Language-Team";
	public static final String KEY_MimeVersion = "MIME-Version";
	public static final String KEY_ContentType = "Content-Type";
	public static final String KEY_ContentTransferEncoding = "Content-Transfer-Encoding";
	public static final String KEY_Language = "Language";

	private static final Set<String> defaultKeys;

	static {
		Set<String> keys = new HashSet<String>();
		keys.add(KEY_ProjectIdVersion);
		keys.add(KEY_ReportMsgidBugsTo);
		keys.add(KEY_PotCreationDate);
		keys.add(KEY_PoRevisionDate);
		keys.add(KEY_LastTranslator);
		keys.add(KEY_LanguageTeam);
		keys.add(KEY_MimeVersion);
		keys.add(KEY_ContentType);
		keys.add(KEY_ContentTransferEncoding);
		// keys.add(KEY_Language);
		defaultKeys = Collections.unmodifiableSet(keys);
	}

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mmZ");

	private static final Pattern pluralPattern = Pattern.compile(
			"nplurals(\\s*?)=(\\s*?)(\\d*?)([\\\\|;|\\n])",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern charsetPattern = Pattern
			.compile(
					"(content-type)(\\s*?):(.*?)charset(\\s*?)=(\\s*?)(.*?)([\\\\|;|\\n])",
					Pattern.CASE_INSENSITIVE);

	public static Set<String> getDefaultKeys() {
		return defaultKeys;
	}

	public String getValue(String key) {
		return entries.get(key);
	}

	public void setValue(String key, String value) {
		entries.put(key, value);
	}

	public void remove(String key) {
		entries.remove(key);
	}

	public Set<String> getKeys() {
		return Collections.unmodifiableSet(entries.keySet());
	}

	public static HeaderFields wrap(Message message) throws ParseException {
		return wrap(message.getMsgstr());
	}

	public static HeaderFields wrap(String msgstr) throws ParseException {
		HeaderFields header = new HeaderFields();
		String[] entries = msgstr.split("\n");
		for (String entry : entries) {
			String[] keyval = entry.split("\\:", 2);
			if (keyval.length != 2) {
				throw new ParseException("Could not parse header entry: "
						+ entry, -1);
			}
			header.entries.put(keyval[0].trim(), keyval[1].trim());
		}
		return header;
	}

	public void unwrap(Message message) {
		StringBuilder msgstr = new StringBuilder();
		for (String key : getKeys()) {
			if(getValue(key) != null) {
				msgstr.append(key);
				msgstr.append(": ");
				msgstr.append(getValue(key));
				msgstr.append("\n");
			}
		}
		message.setMsgstr(msgstr.toString());
	}

	public Message unwrap() {
		Message header = new Message();
		header.setMsgid("");
		header.setFuzzy(true);
		unwrap(header);
		return header;
	}

	public void updatePORevisionDate() {
		setValue(KEY_PoRevisionDate, dateFormat.format(new Date()));
	}

	public void updatePOTCreationDate() {
		setValue(KEY_PotCreationDate, dateFormat.format(new Date()));
	}

	private void setValues(HeaderValues param) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");
		setValue(KEY_ProjectIdVersion, param.projectIdVersion);
		setValue(KEY_ReportMsgidBugsTo, param.reportMsgidBugsTo);
		setValue(KEY_PotCreationDate, dateFormat.format(param.potCreationDate));
		setValue(KEY_PoRevisionDate, param.poRevisionDate);
		setValue(KEY_LastTranslator, param.lastTranslator);
		setValue(KEY_LanguageTeam, param.languageTeam);
		setValue(KEY_MimeVersion, "1.0");
		setValue(KEY_ContentType, "text/plain; charset=" + param.charset);
		setValue(KEY_ContentTransferEncoding, "8bit");
	}

	public static Message generateDefaultHeader() {
		HeaderValues param = new HeaderValues();
		param.projectIdVersion = "PACKAGE VERSION";
		param.reportMsgidBugsTo = "";
		param.potCreationDate = new Date();
		param.poRevisionDate = "YEAR-MO-DA HO:MI+ZONE";
		param.lastTranslator = "FULL NAME <EMAIL@ADDRESS>";
		param.languageTeam = "LANGUAGE <LL@li.org>";
		param.charset = "UTF-8";
		param.comments.add("SOME DESCRIPTIVE TITLE.");
		param.comments.add("Copyright (C) YEAR THE PACKAGE'S COPYRIGHT HOLDER");
		param.comments.add("This file is distributed under the same license as the PACKAGE package.");
		param.comments.add("FIRST AUTHOR <EMAIL@ADDRESS>, YEAR.");
		param.comments.add("");

		return generateHeader(param);
	}
	
	/**
	 * This "Parameter object" might eventually morph into our proper Header class.
	 * <p>
	 * To do that it, we will need HeaderFields.getValues(), and support for arbitrary
	 * field names (eg Set<String> otherFields).
	 * @author sflaniga
	 *
	 */
	private static class HeaderValues {
		public String projectIdVersion;
		public String reportMsgidBugsTo;
		public Date potCreationDate;
		/**
		 * Perhaps poRevisionDate should be a Date too, but null values will
		 * be converted to the string "YEAR-MO-DA HO:MI+ZONE" ?
		 */
		public String poRevisionDate;
		public String lastTranslator;
		public String languageTeam;
		public String charset;
		public List<String> comments = new ArrayList<String>();

		public HeaderValues() {
		}

		public HeaderValues(String projectIdVersion, String reportMsgidBugsTo,
				Date potCreationDate, String poRevisionDate,
				String lastTranslator, String languageTeam, String charset,
				List<String> comments) {
			this.projectIdVersion = projectIdVersion;
			this.reportMsgidBugsTo = reportMsgidBugsTo;
			this.potCreationDate = potCreationDate;
			this.poRevisionDate = poRevisionDate;
			this.lastTranslator = lastTranslator;
			this.languageTeam = languageTeam;
			this.charset = charset;
			this.comments = comments;
		}
	}

	
	/**
	 * This, more general, generate method is private, along with the 
	 * HeaderValues object, until we decide what the public API should be.
	 * @param param
	 * @return
	 */
	private static Message generateHeader(HeaderValues param){

		HeaderFields header = new HeaderFields();
		header.setValues(param);
		
		Message headerMsg = header.unwrap();
		
		for (String comment : param.comments) {
			headerMsg.addComment(comment);
		}

		return headerMsg;
	}

}
