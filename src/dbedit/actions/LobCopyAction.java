/*
 * DBEdit 2
 * Copyright (C) 2006-2011 Jef Van Den Ouweland
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

import java.awt.event.ActionEvent;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

public class LobCopyAction extends CustomAction {

    protected LobCopyAction() {
        super("Copy", "copy.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        saveLobs(null);
        ResultSetTable table = ResultSetTable.getInstance();
        byte[][] lobs = new byte[table.getSelectedRowCount()][];
        if (lobs.length == 1) {
            lobs[0] = readLob(table.getTableValue());
            saveLobs(lobs);
        } else {
            WaitingDialog waitingDialog = new WaitingDialog(null);
            try {
                int[] selectedRows = table.getSelectedRows();
                waitingDialog.setText(String.format("0/%d", lobs.length));
                for (int i = 0; i < selectedRows.length && waitingDialog.isVisible(); i++) {
                    int selectedRow = selectedRows[i];
                    Object lob = table.getValueAt(selectedRow, table.getSelectedColumn());
                    lobs[i] = readLob(lob);
                    waitingDialog.setText(String.format("%d/%d", i + 1, lobs.length));
                }
                if (waitingDialog.isVisible()) {
                    saveLobs(lobs);
                }
            } finally {
                waitingDialog.hide();
            }
        }
    }

    private byte[] readLob(Object lob) throws SQLException {
        if (lob instanceof Blob) {
            return ((Blob) lob).getBytes(1, (int) ((Blob) lob).length());
        } else if (lob instanceof Clob) {
            return ((Clob) lob).getSubString(1, (int) ((Clob) lob).length()).getBytes();
        } else if (lob instanceof byte[]) {
            return (byte[]) lob;
        } else if (lob != null) {
            throw new UnsupportedOperationException("Unsupported type");
        } else {
            return null;
        }
    }

    private void saveLobs(byte[][] lobs) {
        Context.getInstance().setSavedLobs(lobs);
        if (lobs == null) {
            Actions.LOB_PASTE.putValue(NAME, "Paste");
        } else {
            Actions.LOB_PASTE.putValue(NAME, String.format("Paste (%d)", lobs.length));
        }
    }
}
