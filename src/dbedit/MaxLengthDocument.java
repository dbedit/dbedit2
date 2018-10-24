package dbedit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class MaxLengthDocument extends PlainDocument  {
    private int max;
    private boolean numeric;

    public MaxLengthDocument(int newMax) {
        this(newMax, false);
    }

    public MaxLengthDocument(int newMax, boolean newNumeric) {
        this.max = newMax;
        this.numeric = newNumeric;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (getLength() + str.length() <= max) {
            try {
                if (numeric) {
                    Integer.parseInt(str);
                }
                super.insertString(offs, str, a);
            } catch (NumberFormatException e) {
                ExceptionDialog.hideException(e);
            }
        }
    }
}
