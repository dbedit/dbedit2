package dbedit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 12-aug-2005
 * Time: 11:38:50
 */
public class MaxLengthDocument extends PlainDocument  {
    private int max;
    private boolean numeric;

    public MaxLengthDocument(int max) {
        this(max, false);
    }

    public MaxLengthDocument(int max, boolean numeric) {
        this.max = max;
        this.numeric = numeric;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (getLength() + str.length() <= max) {
            try {
                if (numeric) Integer.parseInt(str);
                super.insertString(offs, str, a);
            } catch (NumberFormatException e) {
            }
        }
    }
}
