/**
 * DBEdit 2
 * Copyright (C) 2006-2008 Jef Van Den Ouweland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
