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
package org.fedorahosted.tennera.jgettext.catalog.write;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;

/**
 * CatalogWriter implementation
 *
 * @author Steve Ebersole
 */
@Deprecated
public class CatalogWriter {
	protected final Catalog catalog;

	public CatalogWriter(Catalog catalog) {
		this.catalog = catalog;
	}

	public void writeTo(File file) throws IOException {
	    writeTo(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); //$NON-NLS-1$
	}

	public void writeTo(Writer writer) {
		writeTo(new MessageWritingProcessor(catalog.locateHeader(), writer),
				new Date());
	}

	public void writeTo(Writer writer, Date potDate) {
		writeTo(new MessageWritingProcessor(catalog.locateHeader(), writer), potDate);
	}
	
	void writeTo(MessageWritingProcessor processor, Date potDate) {
		final Message existingHeader = catalog.locateHeader();
		if ( existingHeader == null ) {
			processor.writeMessage(generateHeader(potDate));
		}
		else {
			processor.writeMessage( existingHeader );
		}

		catalog.processMessages( processor );
	}
	
	private Message generateHeader(Date potDate) {
		Message header = new Message();
		header.setMsgid( "" ); //$NON-NLS-1$

		header.addComment("SOME DESCRIPTIVE TITLE."); //$NON-NLS-1$
		header.addComment("Copyright (C) YEAR THE PACKAGE'S COPYRIGHT HOLDER"); //$NON-NLS-1$
		header.addComment("This file is distributed under the same license as the PACKAGE package."); //$NON-NLS-1$
		header.addComment("FIRST AUTHOR <EMAIL@ADDRESS>, YEAR."); //$NON-NLS-1$
		header.addComment(""); //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();
		sb.append("Project-Id-Version: PACKAGE VERSION\n"); //$NON-NLS-1$
		sb.append("Report-Msgid-Bugs-To: \n"); //$NON-NLS-1$
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ"); //$NON-NLS-1$
		sb.append("POT-Creation-Date: " + dateFormat.format(potDate) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\n"); //$NON-NLS-1$
		sb.append("Last-Translator: FULL NAME <EMAIL@ADDRESS>\n"); //$NON-NLS-1$
		sb.append("Language-Team: LANGUAGE <LL@li.org>\n"); //$NON-NLS-1$
		sb.append("MIME-Version: 1.0\n"); //$NON-NLS-1$
		sb.append("Content-Type: text/plain; charset=UTF-8\n"); //$NON-NLS-1$
		sb.append("Content-Transfer-Encoding: 8bit\n");  //$NON-NLS-1$

		header.setMsgstr(sb.toString());
		
		header.markFuzzy();

		return header;
	}

}
