package org.fedorahosted.tennera.jgettext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HeaderUtil {
	
	public static Message generateDefaultHeader() {
		return generateDefaultHeader(new Date());
	}
	
	public static Message generateDefaultHeader(Date potCreationDate) {
		HeaderValues param = new HeaderValues();
		param.projectIdVersion = "PACKAGE VERSION";
		param.reportMsgidBugsTo = "";
		param.potCreationDate = potCreationDate;
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
	private static Message generateHeader(HeaderValues param) {

		HeaderFields header = new HeaderFields();
		setValues(header, param);
		
		Message headerMsg = header.unwrap();
		
		for (String comment : param.comments) {
			headerMsg.addComment(comment);
		}

		return headerMsg;
	}

	private static void setValues(HeaderFields header, HeaderValues param) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");
		header.setValue(HeaderFields.KEY_ProjectIdVersion, param.projectIdVersion);
		header.setValue(HeaderFields.KEY_ReportMsgidBugsTo, param.reportMsgidBugsTo);
		header.setValue(HeaderFields.KEY_PotCreationDate, dateFormat.format(param.potCreationDate));
		header.setValue(HeaderFields.KEY_PoRevisionDate, param.poRevisionDate);
		header.setValue(HeaderFields.KEY_LastTranslator, param.lastTranslator);
		header.setValue(HeaderFields.KEY_LanguageTeam, param.languageTeam);
		header.setValue(HeaderFields.KEY_MimeVersion, "1.0");
		header.setValue(HeaderFields.KEY_ContentType, "text/plain; charset=" + param.charset);
		header.setValue(HeaderFields.KEY_ContentTransferEncoding, "8bit");
	}

}
