header {
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
package org.jboss.jgettext.catalog.parse;
}

/**
 * Defines a parser for the GNU gettext PO/POT file formats.
 * <p/>
 * This grammar is copied nearly verbatim from the kdesdk project from its po/xml package.  It makes certain
 * assumptions about the PO/POT structure that are true for DocBook masters, which is what it was intended to
 * deal with (as is jDocBook, so we can live with those assumptions).
 */
class CatalogParser extends Parser;

options {
    exportVocab=Catalog;
    buildAST=true;
    k=2;
}

tokens {
	COMMENT;
	EXTRACTION;
	OCCURENCE;
	FLAG;

	DOMAIN;

	MSGCTXT;
	MSGID;
	MSGID_PLURAL;

	MSGSTR;
	MSGSTR_PLURAL;

	PREV_MSGCTXT;
	PREV_MSGID;
	PREV_MSGID_PLURAL;

	OBSOLETE;

	PLURALITY;

    // synthetic grouping tokens
    CATALOG;
    MESSAGE;
}

{
    protected AST buildCatalogNode(AST messageBlocks) {
        return #( [CATALOG, "catalog"], messageBlocks );
    }

    private AST buildMessageBlockNode(AST entries) {
        AST node = buildMessageBlockNode( "message", entries );
        handleMessageBlock( node );
        return node;
    }

    private AST buildObsoleteMessageBlockNode(AST entries) {
        AST node = buildMessageBlockNode( "obsolete-message", entries );
        handleObsoleteMessageBlock( node );
        return node;
    }

    private AST buildMessageBlockNode(String text, AST entries) {
        return #( [MESSAGE, text], entries );
    }


    // callbacks ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    protected void handleMessageBlock(AST messageBlock) {
    }

    protected void handleObsoleteMessageBlock(AST messageBlock) {
    }

    protected void handleCatalogComment(AST comment) {
    }

    protected void handleExtractedComment(AST comment) {
    }

    protected void handleOccurence(AST occurence) {
    }

    protected void handleFlag(AST flag) {
    }

    protected void handlePreviousMsgctxt(AST previousMsgctxt) {
    }

    protected void handlePreviousMsgid(AST previousMsgid) {
    }

    protected void handlePreviousMsgidPlural(AST previousMsgidPlural) {
    }

    protected void handleDomain(AST domain) {
    }

    protected void handleMsgctxt(AST msgctxt) {
    }

    protected void handleMsgid(AST msgid) {
    }

    protected void handleMsgidPlural(AST msgidPlural) {
    }

    protected void handleMsgstr(AST msgstr) {
    }

    protected void handleMsgstrPlural(AST msgstr, AST plurality) {
    }
}

/**
 * Main rule
 */
catalog:
    mb:messageBlocks {
        #catalog = buildCatalogNode( #mb );
    }
    ;

messageBlocks:
    ( messageBlock )*
    ;

/**
 * A message block defines all the lines related to a single translatable message entry.
 */
messageBlock:
    ( catalogComment )*
    ( extractedComment )*
    ( occurence )*
    ( flag )*
    ( previousMsgctxt )?
    ( previousMsgid )?
    ( previousMsgidPlural )?
    ( domain )?
    ( entries | o:obsoleteEntries ) {
        if ( #o == null ) {
            #messageBlock = buildMessageBlockNode( #messageBlock );
        }
        else {
            #messageBlock = buildObsoleteMessageBlockNode( #messageBlock );
        }
    }
    ;

entries:
    ( msgctxt )?
    msgid
    ( msgstr | msgidPlural (msgstrPlural)+ )
    ;

obsoleteEntries:
    ( OBSOLETE! msgctxt )?
    OBSOLETE! msgid
    ( OBSOLETE! msgstr | OBSOLETE! msgidPlural (OBSOLETE! msgstrPlural)+ )
    ;

catalogComment: c:COMMENT {
        handleCatalogComment( #c );
    }
    ;

extractedComment: c:EXTRACTION {
        handleExtractedComment( #c );
    }
    ;

occurence: o:OCCURENCE {
        handleOccurence( #o );
    }
    ;

flag: f:FLAG {
        handleFlag( #f );
    }
    ;

previousMsgctxt: pmc:PREV_MSGCTXT {
        handlePreviousMsgctxt( #pmc );
    }
    ;

previousMsgid: pmi:PREV_MSGID {
        handlePreviousMsgid( #pmi );
    }
    ;

previousMsgidPlural: pmip:PREV_MSGID_PLURAL {
        handlePreviousMsgidPlural( #pmip );
    }
    ;

domain: d:DOMAIN {
        handleDomain( #d );
    }
    ;

msgctxt: mc:MSGCTXT {
        handleMsgctxt( #mc );
    }
    ;

msgid: mi:MSGID {
        handleMsgid( #mi );
    }
    ;

msgidPlural: mip:MSGID_PLURAL {
        handleMsgidPlural( #mip );
    }
    ;

msgstr: t:MSGSTR {
        handleMsgstr( #t );
    }
    ;

msgstrPlural: t:MSGSTR_PLURAL p:PLURALITY {
        handleMsgstrPlural( #t, #p );
    }
    ;

