package org.fedorahosted.tennera.antgettext;

/**
 * What to do with messages whose English value is the empty string.
 * @author sflaniga
 *
 */
enum EmptyStringPolicy 
{
	/**
	 * Leave out empty messages
	 */
	SKIP,
	/**
	 * Leave out empty messages; log a warning
	 */
	WARNANDSKIP,
	/**
	 * Include empty messages as empty msgid
	 */
	INCLUDE,
	/**
	 * Include empty messages as empty msgid; log a warning
	 */
	WARNANDINCLUDE, 
	/* Might implement if required:
	REPLACE:
	msgctxt "EMPTY_STRING"
	msgid ""
	msgstr ""
	
	->
	
	msgctxt "EMPTY_STRING"
	msgid "@@EMPTY@@"
	msgstr "@@EMPTY@@"
	 */
//	REPLACE,
	/**
	 * Fail the build with an error message
	 */
	FAIL;
}
