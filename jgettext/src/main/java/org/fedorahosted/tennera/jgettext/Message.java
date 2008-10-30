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
 */
package org.fedorahosted.tennera.jgettext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Message implementation
 *
 * @author Steve Ebersole
 */
public class Message {
	private String domain;
	private String msgctxt;
	private String msgid;
	private String msgidPlural;
	private String msgstr;
	private List<String> msgstrPlural =	new ArrayList<String>();

	private String prevMsgctx;
	private String prevMsgid;
	private String prevMsgidPlural;

	private Collection<String> comments = new ArrayList<String>();
	private Collection<String> extractedComments = new ArrayList<String>();
	private List<Occurence> occurences = new ArrayList<Occurence>();
	private Collection<String> formats = new ArrayList<String>();

	private boolean fuzzy;
	private boolean obsolete;

	private Boolean allowWrap;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getMsgctxt() {
		return msgctxt;
	}

	public void setMsgctxt(String msgctxt) {
		this.msgctxt = msgctxt;
	}

	public String getPrevMsgctx() {
		return prevMsgctx;
	}

	public void setPrevMsgctx(String prevMsgctx) {
		this.prevMsgctx = prevMsgctx;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getPrevMsgid() {
		return prevMsgid;
	}

	public void setPrevMsgid(String prevMsgid) {
		this.prevMsgid = prevMsgid;
	}

	public String getMsgidPlural() {
		return msgidPlural;
	}

	public void setMsgidPlural(String msgidPlural) {
		this.msgidPlural = msgidPlural;
	}

	public String getPrevMsgidPlural() {
		return prevMsgidPlural;
	}

	public void setPrevMsgidPlural(String prevMsgidPlural) {
		this.prevMsgidPlural = prevMsgidPlural;
	}

	public String getMsgstr() {
		return msgstr;
	}

	public void setMsgstr(String msgstr) {
		this.msgstr = msgstr;
	}

	public void addMsgstrPlural(String msgstr, int position) {
		if ( msgstrPlural == null ) {
			msgstrPlural = new ArrayList<String>();
		}
		msgstrPlural.add( position, msgstr );
	}

	public void markFuzzy() {
		this.fuzzy = true;
	}

	public boolean isFuzzy() {
		return fuzzy;
	}

	public void markObsolete() {
		this.obsolete = true;
	}

	public boolean isObsolete() {
		return obsolete;
	}

	public Boolean getAllowWrap() {
		return allowWrap;
	}

	public void setAllowWrap(Boolean allowWrap) {
		this.allowWrap = allowWrap;
	}

	public void addComment(String comment) {
		comments.add( comment );
	}

	public void addExtractedComment(String comment) {
		extractedComments.add( comment );
	}

	public Occurence addOccurence(String file, int line) {
		Occurence o = new Occurence( file+':'+line );
		addOccurence( o );
		return o;
	}

	public void addOccurence(Occurence occurence) {
		occurences.add( occurence );
	}

	public void addFormat(String format) {
		formats.add( format );
	}

	public boolean isHeader() {
		return msgctxt == null && "".equals( msgid );
	}

	public List<String> getMsgstrPlural() {
		return msgstrPlural;
	}

	public List<Occurence> getOccurences() {
		return occurences;
	}

	public Collection<String> getComments() {
		return comments;
	}

	public Collection<String> getExtractedComments() {
		return extractedComments;
	}

	public Collection<String> getFormats() {
		return formats;
	}
	
	/**
	 * Returns a string representation of the Message, in a format 
	 * suitable for inclusion in debug messages.
	 */
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Message(msgctxt \"").append(msgctxt).
	    	append("\", msgid \"").append(getMsgid()).
	    	append("\", msgstr \"").append(getMsgstr());
	    if(!getComments().isEmpty())
	    	sb.append("\", transComments \"").append(getComments());
	    if(!getExtractedComments().isEmpty())
	    	sb.append("\", extComments \"").append(getExtractedComments());
	    if(!getFormats().isEmpty())
	    	sb.append("\", flags \"").append(getFormats());
	    if(!getOccurences().isEmpty())
	    	sb.append("\", references \"").append(getOccurences());
	    	// TODO should include fuzzy, obsolete, plurals, domain, prevMsg
	    	sb.append("\")");
	    return sb.toString();
	}

}
