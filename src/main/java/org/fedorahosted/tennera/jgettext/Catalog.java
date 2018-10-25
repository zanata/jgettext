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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Models a catalog
 *
 * @author Steve Ebersole
 */
public class Catalog implements Iterable<Message> {
    // todo : segment by domain?

    /**
     * Is this a POT (template)? Or a PO?
     */
    private boolean template;

    public Catalog(boolean template) {
        this.template = template;
    }

    public Catalog()
    {
        this(false);
    }

    private final LinkedHashMap<MessageHashKey, Message> messageMap =
            new LinkedHashMap<MessageHashKey, Message>();

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public boolean isTemplate() {
        return template;
    }

    public void addMessage(Message message) {
        messageMap.put(new MessageHashKey(message), message);
    }

    public Message locateHeader() {
        for (Message message : messageMap.values()) {
            if (message.isHeader()) {
                return message;
            }
        }
        return null;
    }

    public Message locateMessage(String msgctxt, String msgid) {
        return locateMessage(new MessageHashKey(msgctxt, msgid));
    }

    public Message locateMessage(MessageHashKey key) {
        return messageMap.get(key);
    }

    public boolean containsMessage(String msgctxt, String msgid) {
        return containsMessage(new MessageHashKey(msgctxt, msgid));
    }

    public boolean containsMessage(MessageHashKey key) {
        return messageMap.containsKey(key);
    }

    public boolean isEmpty() {
        return messageMap.isEmpty();
    }

    public int size() {
        return messageMap.size();
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Catalog(");
        for (Entry<MessageHashKey, Message> entry : messageMap.entrySet()) {
            // sb.append(entry.getKey());
            // sb.append(" => ");
            sb.append(entry.getValue());
            sb.append('\n');
        }
        sb.append(",template=");
        sb.append(template);
        sb.append(")");
        return sb.toString();
    }

    public void processMessages(MessageProcessor processor) {
        for (Message message : messageMap.values()) {
            processor.processMessage(message);
        }
    }

    @Override
    public Iterator<Message> iterator() {
        return messageMap.values().iterator();
    }
}
