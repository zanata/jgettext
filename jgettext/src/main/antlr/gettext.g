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
package org.jboss.jdocbook.i18n.gettext.po.parse;
}

/**
 * Defines a parser for the GNU gettext PO/POT file formats.
 * <p/>
 * This grammar is copied nearly verbatim from the kdesdk project from its po/xml package.  It makes certain
 * assumptions about the PO/POT structure that are true for DocBook masters, which is what it was intended to
 * deal with (as is jDocBook, so we can live with those assumptions).
 */
class GetTextParser extends Parser;

options {
    exportVocab=GetText;
    buildAST=true;
//    k=2;
}

tokens {
    // synthetic tokens
    PORTABLE_OBJECT;
    MESSAGE;
}

{
    protected AST buildPortableObjectNode(AST messageBlocks) {
        return #( [PORTABLE_OBJECT, "portable-object"], messageBlocks );
    }

    protected AST buildMessageBlockNode(String text, AST entries) {
        return #( [MESSAGE, text], entries );
    }
}

/**
 * Main rule
 */
portableObject:
    mb:messageBlocks {
        #portableObject = buildPortableObjectNode( #mb );
    }
    ;

messageBlocks:
    ( messageBlock | kdesdkCommentBlock )*
    ;

kdesdkCommentBlock: (kdesdkComment)+ {
        #kdesdkCommentBlock = buildMessageBlockNode( "kdesdk-message-block", #kdesdkCommentBlock );
    }
    ;


/**
 * A message block defines all the lines related to a single translatable message entry.
 */
messageBlock:
    ( translatorComment | extracedComment | referenceComment | flagComment | prevEntryComment )*
    ( messageId | messageIdPlural | messageContext )+ messageTranslation ( messageTranslationPlural )* {
        #messageBlock = buildMessageBlockNode( "message-block", #messageBlock );
    }
    ;

translatorComment:
    TRANSLATOR_COMMENT^ (COMMENT_STRING)?
    ;

extracedComment:
    EXTRACTED_COMMENT^ (COMMENT_STRING)?
    ;

referenceComment:
    REFERENCE_COMMENT^ (COMMENT_STRING)?
    ;

flagComment:
    FLAG_COMMENT^ (COMMENT_STRING)?
    ;

prevEntryComment:
    PREV_ENTRY_COMMENT^ ( messageId | messageIdPlural | messageContext | messageTranslation | messageTranslationPlural )
    ;

kdesdkComment:
    KDESDK_COMMENT^ ( messageId | messageTranslation )
    ;

messageId:
    MSGID^ QUOTED_STRING
    ;

messageIdPlural:
    MSGID_PLURAL^ QUOTED_STRING
    ;

messageContext:
    MSGCTXT^ QUOTED_STRING
    ;

messageTranslation:
    MSGSTR^ ( QUOTED_STRING )?
    ;

messageTranslationPlural:
    MSGSTR_PLURAL^ OPEN_BRACKET! INTEGER CLOSE_BRACKET! ( QUOTED_STRING )?
    ;


// **** LEXER ******************************************************************

class GetTextLexer extends Lexer;

options {
    exportVocab=GetText;
    k=7;
    charVocabulary='\u0000'..'\uFFFE';
    caseSensitive = true;
}

TRANSLATOR_COMMENT: "#";
EXTRACTED_COMMENT: "#.";
REFERENCE_COMMENT: "#:";
FLAG_COMMENT: "#,";
PREV_ENTRY_COMMENT: "#|";
KDESDK_COMMENT: "#~";

MSGID: "msgid";
MSGID_PLURAL: "msgid-plural";
MSGCTXT: "msgctxt";
MSGSTR: "msgstr";

INTEGER: ( '0'..'9' )+;
OPEN_BRACKET: '[';
CLOSE_BRACKET: ']';

COMMENT_STRING: (~'\n')*;

QUOTED_STRING:	('"'! (ESC|~'"')* ('"'! (' ' | 't')*! '\n'! { newline(); } (' '! | '\t'!)*))+
    ;

WS  :   (   ' '
        |   '\t'
        |   '\r' '\n' { newline(); }
        |   '\n'      { newline(); }
        |   '\r'      { newline(); }
        )
        {$setType(Token.SKIP);} //ignore this token
    ;


// copied from example
protected
ESC	:	'\\'
        (	'n'
        |	'r'
        |	't'
        |	'b'
        |	'f'
        |	'"'
        |	'\''
        |	'\\'
        |	('0'..'3')
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :	('0'..'9')
                (
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :	'0'..'9'
                )?
            )?
        |	('4'..'7')
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :	('0'..'9')
            )?
        )
    ;
