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

import javax.swing.*;

public class WaitingDialog {

    private JLabel message = new JLabel();
    private JDialog dialog;

    public WaitingDialog(final Runnable onCancel) {
        final Dialog pane = new Dialog(message, Dialog.PLAIN_MESSAGE, Dialog.DEFAULT_OPTION,
                new Object[] {"Cancel"}, "Cancel");
        dialog = pane.createDialog(ApplicationPanel.getInstance(), null);
        new Thread(new Runnable() {
            public void run() {
                dialog.setVisible(true);
                if (onCancel != null && "Cancel".equals(pane.getValue())) {
                    onCancel.run();
                }
            }
        }).start();
        // wait
        boolean visible = false;
        while (!visible) {
            visible = dialog.isVisible();
        }
    }

    public void setText(String text) {
        message.setText(text);
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    public void hide() {
        dialog.setVisible(false);
    }
}
