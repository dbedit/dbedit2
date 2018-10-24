/*
 * DBEdit 2
 * Copyright (C) 2006-2010 Jef Van Den Ouweland
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

import javax.swing.*;

public class Dialog extends JOptionPane {

    public Dialog(Object message, int messageType, int optionType, Object[] options, Object initialValue) {
        super(message, messageType, optionType, null, options, initialValue);
    }

    public static int show(String title, Object message, int messageType, int optionType) {
        Dialog dialog = new Dialog(message, messageType, optionType, null, null);
        dialog.createDialog(ApplicationPanel.getInstance(), title).setVisible(true);
        return dialog.getValue() == null ? CLOSED_OPTION : ((Number) dialog.getValue()).intValue();
    }

    public static Object show(String title, Object message, int messageType, Object[] options, Object initialValue) {
        Dialog dialog = new Dialog(message, messageType, DEFAULT_OPTION, options, initialValue);
        try {
            dialog.createDialog(ApplicationPanel.getInstance(), title).setVisible(true);
        } catch (NoClassDefFoundError e) {
            dialog.createDialog(null, title).setVisible(true);
        }
        return dialog.getValue();
    }

    @Override
    public void selectInitialValue() {
    }
}
