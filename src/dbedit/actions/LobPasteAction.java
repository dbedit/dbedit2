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

import dbedit.Context;
import dbedit.ResultSetTable;
import dbedit.WaitingDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LobPasteAction extends CustomAction {

    protected LobPasteAction() {
        super("Paste", "paste.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ResultSetTable.getInstance();
        if (table.getSelectedRowCount() == 1) {
            ResultSetTable.getInstance().setTableValue(Context.getInstance().getSavedLobs()[0]);
            ResultSetTable.getInstance().editingStopped(null);
        } else {
            WaitingDialog waitingDialog = new WaitingDialog(null);
            try {
                int[] selectedRows = table.getSelectedRows();
                waitingDialog.setText(String.format("0/%d", selectedRows.length));
                for (int i = 0; i < selectedRows.length && waitingDialog.isVisible(); i++) {
                    int selectedRow = selectedRows[i];
                    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                    ResultSetTable.getInstance().setTableValue(Context.getInstance().getSavedLobs()[i]);
                    ResultSetTable.getInstance().editingStopped(null);
                    waitingDialog.setText(String.format("%d/%d", i + 1, selectedRows.length));
                }
            } finally {
                waitingDialog.hide();
            }
        }
    }
}
