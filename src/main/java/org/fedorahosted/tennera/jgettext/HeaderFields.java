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

@SuppressWarnings("nls")
public class HeaderFields {

	private Map<String, String> entries = new LinkedHashMap<String, String>();

	public static final String KEY_ProjectIdVersion = "Project-Id-Version";
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

	/**
	 * <p>Extracts key:value headers into a {@link HeaderFields}.</p>
	 * 
	 * <p>
	 *   The expected format is one or more lines each in the form
	 *   <code> "[key]:[value]\n"</code>.
	 * </p>
	 * 
	 * @param msgstr string describing one or more headers
	 * @throws ParseException
	 *            if msgstr is empty, or does not comply to the expected format.
	 */
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

}
