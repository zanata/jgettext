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

/**
 * MessageHashKey implementation
 *
 * @author Steve Ebersole
 */
public class MessageHashKey {
	private final String msgctxt;
	private final String msgid;
	private final int hashcode;

	public MessageHashKey(Message message) {
		this( message.getMsgctxt(), message.getMsgid() );
	}

	public MessageHashKey(String msgctxt, String msgid) {
		this.msgctxt = msgctxt;
		this.msgid = msgid;
		this.hashcode = generateHashCode();
	}

	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		MessageHashKey that = ( MessageHashKey ) o;

		return !( msgctxt != null ? !msgctxt.equals( that.msgctxt ) : that.msgctxt != null )
				&& msgid.equals( that.msgid );

	}

	public int hashCode() {
		return hashcode;
	}

	private int generateHashCode() {
		int result;
		result = ( msgctxt != null ? msgctxt.hashCode() : 0 );
		result = 31 * result + msgid.hashCode();
		return result;
	}
	
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MessageHashKey(msgctxt ");
		if(msgctxt == null) {
			sb.append("null");
		} else {
			sb.append('\"');
			sb.append(msgctxt);
			sb.append('\"');
		}
		sb.append(", msgid ");
		if(msgid == null) {
			sb.append("null");
		} else {
			sb.append('\"');
			sb.append(msgid);
			sb.append('\"');
		}
		sb.append(")");
		return sb.toString();
	}
}
