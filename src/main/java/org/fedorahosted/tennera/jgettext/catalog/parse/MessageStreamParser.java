package org.fedorahosted.tennera.jgettext.catalog.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.fedorahosted.tennera.jgettext.Message;

import antlr.ASTPair;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

public class MessageStreamParser {

    private InternalMessageStreamParser internalParser;

    /**
     * Uses the charset encoding specified in the file's Gettext header.
     * 
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MessageStreamParser(File file) throws FileNotFoundException,
            IOException {
        internalParser = new InternalMessageStreamParser(file);
    }

    public MessageStreamParser(Reader reader) {
        internalParser = new InternalMessageStreamParser(reader);
    }

    /**
     * Uses the charset encoding specified in the stream's Gettext header.
     * 
     * @param inputStream
     * @throws IOException
     */
    public MessageStreamParser(InputStream inputStream) throws IOException {
        internalParser = new InternalMessageStreamParser(inputStream);
    }

    public MessageStreamParser(InputStream inputStream, Charset charset) {
        internalParser = new InternalMessageStreamParser(inputStream, charset);
    }

    // TODO this class is a fragile, ugly hack to allow stream parsing
    // by knowing far too much about the internals of the generated
    // CatalogParser
    private class InternalMessageStreamParser extends CatalogParser {

        private Message currentMessage = new Message();
        private Message nextMessage;

        public InternalMessageStreamParser(File file)
                throws FileNotFoundException, IOException {
            super(new CatalogLexer(file));
        }

        public InternalMessageStreamParser(Reader reader) {
            super(new CatalogLexer(reader));
        }

        public InternalMessageStreamParser(InputStream inputStream)
                throws IOException {
            super(new CatalogLexer(inputStream));
        }

        public InternalMessageStreamParser(InputStream inputStream,
                Charset charset) {
            super(new CatalogLexer(inputStream, charset));
        }

        public void reportError(RecognitionException e) {
            UnexpectedTokenException utEx =
                    new UnexpectedTokenException(e.getMessage(), e.getLine());
            utEx.initCause(e);
            throw utEx;
        }

        public void reportError(String s) {
            throw new ParseException("error parsing catalog : " + s, -1);
        }

        public void reportWarning(String s) {
        }

        protected boolean nextMessageBlock() throws RecognitionException,
                TokenStreamException {

            boolean ret = false;
            returnAST = null;
            ASTPair currentAST = new ASTPair();
            AST messageBlocks_AST = null;

            try { // for error handling
                _pseudocomments:
                // this do loop was pinched from the generated messageBlocks()
                do {
                    switch (LA(1)) {
                    case COMMENT: {
                        catalogComment();
                        astFactory.addASTChild(currentAST, returnAST);
                        break;
                    }
                    case EXTRACTION: {
                        extractedComment();
                        astFactory.addASTChild(currentAST, returnAST);
                        break;
                    }
                    case REFERENCE: {
                        reference();
                        astFactory.addASTChild(currentAST, returnAST);
                        break;
                    }
                    case FLAG: {
                        flag();
                        astFactory.addASTChild(currentAST, returnAST);
                        break;
                    }
                    default: {
                        break _pseudocomments;
                    }
                    }
                } while (true);
                if (EOF != LA(1)) {
                    messageBlock();
                    astFactory.addASTChild(currentAST, returnAST);
                    ret = true;
                }
                else {
                    reportWarning("No more entries!");
                }
                messageBlocks_AST = (AST) currentAST.root;
            } catch (RecognitionException ex) {
                reportError(ex);
                recover(ex, _tokenSet_0);
            }
            returnAST = messageBlocks_AST;

            return ret;
        }

        @Override
        protected void handleMessageBlock(AST messageBlock) {
            nextMessage = currentMessage;
            currentMessage = new Message();
        }

        @Override
        protected void handleObsoleteMessageBlock(AST messageBlock) {
            currentMessage.markObsolete();
            handleMessageBlock(messageBlock);
        }

        @Override
        protected void handleCatalogComment(AST comment) {
            currentMessage.addComment(extractText(comment));
        }

        @Override
        protected void handleExtractedComment(AST comment) {
            currentMessage.addExtractedComment(extractText(comment));
        }

        @Override
        protected void handleReference(AST sourceRef) {
            currentMessage.addSourceReference(parseSourceReference(sourceRef));
        }

        @Override
        protected void handleFlag(AST flag) {
            String[] flags = flag.getText().split(",");
            for (String flagStr : flags) {
                flagStr = flagStr.trim();
                if (!flagStr.isEmpty())
                    currentMessage.addFormat(flagStr);
            }
        }

        @Override
        protected void handlePreviousMsgctxt(AST previousMsgctxt) {
            currentMessage.setPrevMsgctx(extractText(previousMsgctxt));
        }

        @Override
        protected void handlePreviousMsgid(AST previousMsgid) {
            currentMessage.setPrevMsgid(extractText(previousMsgid));
        }

        @Override
        protected void handlePreviousMsgidPlural(AST previousMsgidPlural) {
            currentMessage.setPrevMsgidPlural(extractText(previousMsgidPlural));
        }

        @Override
        protected void handleDomain(AST domain) {
            currentMessage.setDomain(extractText(domain));
        }

        @Override
        protected void handleMsgctxt(AST msgctxt) {
            currentMessage.setMsgctxt(extractText(msgctxt));
        }

        @Override
        protected void handleMsgid(AST msgid) {
            currentMessage.setMsgid(extractText(msgid));
        }

        @Override
        protected void handleMsgidPlural(AST msgidPlural) {
            currentMessage.setMsgidPlural(extractText(msgidPlural));
        }

        @Override
        protected void handleMsgstr(AST msgstr) {
            currentMessage.setMsgstr(extractText(msgstr));
        }

        @Override
        protected void handleMsgstrPlural(AST msgstr, AST plurality) {
            currentMessage.addMsgstrPlural(extractText(msgstr),
                    Integer.parseInt(plurality.getText()));
        }

        private String extractText(AST ast) {
            return ast == null ? "" : ast.getText();
        }

        private String parseSourceReference(AST ast) {
            return ast.getText();
        }

        public boolean hasNext() {
            if (nextMessage != null) {
                return true;
            }

            boolean next = true;
            try {
                next = nextMessageBlock();
            } catch (RecognitionException e) {
                currentRecognitionException = e;
            } catch (TokenStreamException e) {
                currentTokenStreamException = e;
            }

            return next;
        }

        private RecognitionException currentRecognitionException = null;
        private TokenStreamException currentTokenStreamException = null;

        private void checkExceptions() throws RecognitionException,
                TokenStreamException {
            if (currentRecognitionException != null) {
                RecognitionException e = currentRecognitionException;
                currentRecognitionException = null;
                throw e;
            }
            else if (currentTokenStreamException != null) {
                TokenStreamException e = currentTokenStreamException;
                currentTokenStreamException = null;
                throw e;
            }

        }

        public Message next() throws RecognitionException, TokenStreamException {
            checkExceptions();

            if (!hasNext()) {
                return null;
            }

            Message next = nextMessage;
            nextMessage = null;
            return next;
        }
    }

    public boolean hasNext() {
        return internalParser.hasNext();
    }

    public Message next() throws ParseException {
        try {
            return internalParser.next();
        } catch (RecognitionException e) {
            throw new ParseException(e.getMessage(), e, e.getLine());
        } catch (TokenStreamException e) {
            throw new ParseException(e.getMessage(), e, -1);
        }
    }

}
