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
package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class HistoryPreviousAction extends ActionChangeAbstractAction {

    protected HistoryPreviousAction() {
        super("History - previous", "back.png", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK));
    }

    public void actionPerformed(final ActionEvent e) {
        if (getHistory().hasPrevious()) {
            ApplicationPanel.getInstance().getTextComponent().setText(getHistory().previous());
        }
        handleTextActions();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
