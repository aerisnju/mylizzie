package wagner.stephanie.lizzie.util;

import org.apache.commons.lang3.StringUtils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.util.LinkedList;
import java.util.Objects;

public class TextLineManager {
    private static final SimpleAttributeSet NORMAL_TEXT;
    private static final SimpleAttributeSet BOLD_TEXT;
    private static final SimpleAttributeSet ITALIC_TEXT;
    private static final SimpleAttributeSet BOLD_ITALIC_TEXT;

    static {
        NORMAL_TEXT = new SimpleAttributeSet();
        BOLD_TEXT = new SimpleAttributeSet();
        ITALIC_TEXT = new SimpleAttributeSet();
        BOLD_ITALIC_TEXT = new SimpleAttributeSet();

        StyleConstants.setBold(BOLD_TEXT, true);
        StyleConstants.setBold(BOLD_ITALIC_TEXT, true);
        StyleConstants.setItalic(ITALIC_TEXT, true);
        StyleConstants.setItalic(BOLD_ITALIC_TEXT, true);
    }

    private Document document;
    private LinkedList<String> documentLines;
    private int documentLineCountLimit;

    public TextLineManager(Document document, int documentLineCountLimit) {
        this.document = document;
        documentLines = new LinkedList<>();
        if (documentLineCountLimit > 0) {
            this.documentLineCountLimit = documentLineCountLimit;
        } else {
            this.documentLineCountLimit = Integer.MAX_VALUE;
        }
    }

    public int getDocumentLineCountLimit() {
        return documentLineCountLimit;
    }

    public void setDocumentLineCountLimit(int documentLineCountLimit) {
        if (documentLineCountLimit > 0) {
            this.documentLineCountLimit = documentLineCountLimit;
        } else {
            this.documentLineCountLimit = Integer.MAX_VALUE;
        }

        try {
            shrinkToLineLimit();
        } catch (BadLocationException e) {
            throw new GenericLizzieException("Unexpected exception: cannot shrink text.", e);
        }
    }

    public void appendNormalLine(String line) {
        appendLine(line, NORMAL_TEXT);
    }

    public void appendBoldLine(String line) {
        appendLine(line, BOLD_TEXT);
    }

    public void appendItalicLine(String line) {
        appendLine(line, ITALIC_TEXT);
    }

    public void appendBoldItalicLine(String line) {
        appendLine(line, BOLD_ITALIC_TEXT);
    }

    public void appendLine(String line, SimpleAttributeSet simpleAttributeSet) {
        line = StringUtils.strip(line, "\r\n") + "\n";
        try {
            appendTextAtTheEnd(line, simpleAttributeSet);
            documentLines.offer(line);

            shrinkToLineLimit();
        } catch (BadLocationException e) {
            throw new GenericLizzieException("Unexpected exception: cannot append text.", e);
        }
    }

    private void shrinkToLineLimit() throws BadLocationException {
        while (documentLines.size() > 0 && documentLines.size() > documentLineCountLimit) {
            String lineToRemove = Objects.requireNonNull(documentLines.poll());
            removeTextAtTheBeginning(lineToRemove.length());
        }
    }

    private void removeTextAtTheBeginning(int length) throws BadLocationException {
        document.remove(0, length);
    }

    private void appendTextAtTheEnd(String text, SimpleAttributeSet simpleAttributeSet) throws BadLocationException {
        document.insertString(document.getLength(), text, simpleAttributeSet);
    }

    public void clear() {
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            throw new GenericLizzieException("Unexpected exception: cannot clear text.", e);
        }
    }
}
