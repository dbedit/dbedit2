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
package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LobPasteAction extends LobAbstractAction {

    protected LobPasteAction() {
        super("Paste", "paste.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        int[] selectedRows = table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            ApplicationPanel.getInstance().setTableValue(getSavedLobs()[i]);
            editingStopped(null);
        }
    }
}
