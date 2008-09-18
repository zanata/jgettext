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
package org.jboss.jdocbook.render.format;

/**
 * Descriptors of the bundled DocBook styles in which we are interested.
 *
 * @author Steve Ebersole
 */
public class StandardDocBookFormatDescriptors {

	public static final StandardDocBookFormatDescriptor PDF = new BasicDescriptor()
			.setName( "pdf" )
			.setStandardFileExtension( "pdf" )
			.setImagePathSettingRequired( true )
			.setImageCopyingRequired( false )
			.setDoingChunking( false )
			.setStylesheetResource( "/fo/docbook.xsl" )
			.setProfiledStylesheetResource( "/fo/profile-docbook.xsl" );

	public static final StandardDocBookFormatDescriptor XHTML = new BasicDescriptor()
			.setName( "xhtml" )
			.setStandardFileExtension( "xhtml" )
			.setImagePathSettingRequired( false )
			.setImageCopyingRequired( true )
			.setDoingChunking( false )
			.setStylesheetResource( "/xhtml/docbook.xsl" )
			.setProfiledStylesheetResource( "/xhtml/profile-docbook.xsl" );

	public static final StandardDocBookFormatDescriptor ECLIPSE = new HtmlBasedDescriptor()
			.setName( "eclipse" )
			.setStylesheetResource( "/eclipse/eclipse.xsl" );

	public static final StandardDocBookFormatDescriptor HTML = new HtmlBasedDescriptor()
			.setName( "html" )
			.setStylesheetResource( "/html/chunk.xsl" )
			.setProfiledStylesheetResource( "/html/profile-chunk.xsl" );

	public static final StandardDocBookFormatDescriptor HTML_SINGLE = new HtmlBasedDescriptor()
			.setName( "html_single" )
			.setDoingChunking( false )
			.setStylesheetResource( "/html/docbook.xsl" )
			.setProfiledStylesheetResource( "/html/profile-docbook.xsl" );

	public static final StandardDocBookFormatDescriptor HTMLHELP = new HtmlBasedDescriptor()
			.setName( "htmlhelp" )
			.setStylesheetResource( "/htmlhelp/htmlhelp.xsl" )
			.setProfiledStylesheetResource( "/htmlhelp/profile-htmlhelp.xsl" );

	public static final StandardDocBookFormatDescriptor JAVAHELP = new HtmlBasedDescriptor()
			.setName( "javahelp" )
			.setStylesheetResource( "/javahelp/javahelp.xsl" )
			.setProfiledStylesheetResource( "/javahelp/profile-javahelp.xsl" );

	public static final StandardDocBookFormatDescriptor MAN = new HtmlBasedDescriptor()
			.setName( "man" )
			.setDoingChunking( false )
			.setStylesheetResource( "/manpages/docbook.xsl" );

	public static final StandardDocBookFormatDescriptor WEBSITE = new HtmlBasedDescriptor()
			.setName( "website" )
			.setDoingChunking( false )
			.setStylesheetResource( "/website/website.xsl" );

	private static class HtmlBasedDescriptor extends BasicDescriptor {
		private HtmlBasedDescriptor() {
			doingChunking = true;
			standardFileExtension = "html";
			imagePathSettingRequired = false;
			imageCopyingRequired = true;
		}
	}

	private static class BasicDescriptor implements StandardDocBookFormatDescriptor {
		protected String name;
		protected String standardFileExtension;
		protected String stylesheetResource;
		protected String profiledStylesheetResource;
		protected boolean imagePathSettingRequired;
		protected boolean imageCopyingRequired;
		protected boolean doingChunking;

		public String getName() {
			return name;
		}

		public BasicDescriptor setName(String name) {
			this.name = name;
			return this;
		}

		public String getStandardFileExtension() {
			return standardFileExtension;
		}

		public BasicDescriptor setStandardFileExtension(String standardFileExtension) {
			this.standardFileExtension = standardFileExtension;
			return this;
		}

		public String getStylesheetResource() {
			return stylesheetResource;
		}

		public BasicDescriptor setStylesheetResource(String stylesheetResource) {
			this.stylesheetResource = stylesheetResource;
			// default :
			this.profiledStylesheetResource = stylesheetResource;
			return this;
		}

		public String getProfiledStylesheetResource() {
			return profiledStylesheetResource;
		}

		public BasicDescriptor setProfiledStylesheetResource(String profiledStylesheetResource) {
			this.profiledStylesheetResource = profiledStylesheetResource;
			return this;
		}

		public boolean isImagePathSettingRequired() {
			return imagePathSettingRequired;
		}

		public BasicDescriptor setImagePathSettingRequired(boolean imagePathSettingRequired) {
			this.imagePathSettingRequired = imagePathSettingRequired;
			return this;
		}

		public boolean isImageCopyingRequired() {
			return imageCopyingRequired;
		}

		public BasicDescriptor setImageCopyingRequired(boolean imageCopyingRequired) {
			this.imageCopyingRequired = imageCopyingRequired;
			return this;
		}

		public boolean isDoingChunking() {
			return doingChunking;
		}

		public BasicDescriptor setDoingChunking(boolean doingChunking) {
			this.doingChunking = doingChunking;
			return this;
		}
	}

	public static StandardDocBookFormatDescriptor getDescriptor(String name) {
		if ( ECLIPSE.getName().equals( name ) ) {
			return ECLIPSE;
		}
		else if ( HTML.getName().equals( name ) ) {
			return HTML;
		}
		else if ( HTML_SINGLE.getName().equals( name ) ) {
			return HTML_SINGLE;
		}
		else if ( HTMLHELP.getName().equals( name ) ) {
			return HTMLHELP;
		}
		else if ( JAVAHELP.getName().equals( name ) ) {
			return JAVAHELP;
		}
		else if ( MAN.getName().equals( name ) ) {
			return MAN;
		}
		else if ( PDF.getName().equals( name ) ) {
			return PDF;
		}
		else if ( WEBSITE.getName().equals( name ) ) {
			return WEBSITE;
		}
		else if ( XHTML.getName().equals( name ) ) {
			return XHTML;
		}
		else {
			return null;
		}
	}

	/**
	 * Disallow external instantiation of StandardDocBookFormatDescriptors.
	 */
	private StandardDocBookFormatDescriptors() {
	}
}
