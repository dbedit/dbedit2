/*
 * DBEdit 2
 * Copyright (C) 2006-2012 Jef Van Den Ouweland
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

import dbedit.ResultSetTable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class CopyCellValueAction extends AbstractAction {

    private Action defaultAction;

    public CopyCellValueAction(Action defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTable table = (JTable) e.getSource();
        if (table.getSelectedRowCount() == 1) {
            Object value = ResultSetTable.getInstance().getTableValue();
            if (value != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(value.toString()), null);
            }
        } else {
            defaultAction.actionPerformed(e);
        }
    }
}
