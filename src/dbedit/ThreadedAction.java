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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class ThreadedAction implements Runnable {

    public ThreadedAction() {
        new Thread(this).start();
    }

    public final void run() {
        final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        final Component glassPane = ApplicationPanel.getInstance().getRootPane().getGlassPane();
        try {
            if (!glassPane.isVisible()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        if (glassPane.getMouseListeners().length == 0) {
                            glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                            glassPane.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent e) {
                                    super.mouseClicked(e);
                                }
                            });
                        }
                        glassPane.setVisible(true);
                        glassPane.requestFocus();
                    }
                });
            }
            execute();
        } catch (Throwable t) {
            ExceptionDialog.showException(t);
        } finally {
            if (glassPane.isVisible()) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            glassPane.setVisible(false);
                            if (focusOwner != null) {
                                focusOwner.requestFocus();
                            }
                        }
                    });
                } catch (Throwable t) {
                    ExceptionDialog.hideException(t);
                }
            }
        }
    }

    protected abstract void execute() throws Exception;
}
