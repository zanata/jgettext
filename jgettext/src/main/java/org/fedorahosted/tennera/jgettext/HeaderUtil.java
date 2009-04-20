package org.fedorahosted.tennera.jgettext;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;

public class HeaderUtil {

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

	public static HeaderUtil wrap(Message message) throws ParseException {
		return wrap(message.getMsgstr());
	}

	public static HeaderUtil wrap(String msgstr) throws ParseException {
		HeaderUtil header = new HeaderUtil();
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
			msgstr.append(key);
			msgstr.append(": ");
			msgstr.append(getValue(key));
			msgstr.append("\n");
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

	public void updatePOTCreationDate(Message message) {
		setValue(KEY_PotCreationDate, dateFormat.format(new Date()));
	}

	public static Message generateDefaultHeader() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");

		HeaderUtil header = new HeaderUtil();

		header.setValue(KEY_ProjectIdVersion, "PACKAGE VERSION");
		header.setValue(KEY_ReportMsgidBugsTo, "");
		header.setValue(KEY_PotCreationDate, dateFormat.format(new Date()));
		header.setValue(KEY_PoRevisionDate, "YEAR-MO-DA HO:MI+ZONE");
		header.setValue(KEY_LastTranslator, "FULL NAME <EMAIL@ADDRESS>");
		header.setValue(KEY_LanguageTeam, "LANGUAGE <LL@li.org>");
		header.setValue(KEY_MimeVersion, "1.0");
		header.setValue(KEY_ContentType, "text/plain; charset=UTF-8");
		header.setValue(KEY_ContentTransferEncoding, "8bit");

		Message headerMsg = header.unwrap();

		headerMsg.addComment("SOME DESCRIPTIVE TITLE.");
		headerMsg
				.addComment("Copyright (C) YEAR THE PACKAGE'S COPYRIGHT HOLDER");
		headerMsg
				.addComment("This file is distributed under the same license as the PACKAGE package.");
		headerMsg.addComment("FIRST AUTHOR <EMAIL@ADDRESS>, YEAR.");
		headerMsg.addComment("");

		return headerMsg;
	}

}
