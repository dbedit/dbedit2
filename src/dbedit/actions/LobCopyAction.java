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
import java.sql.Blob;
import java.sql.Clob;

public class LobCopyAction extends LobAbstractAction {

    protected LobCopyAction() {
        super("Copy", "copy.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        int[] selectedRows = table.getSelectedRows();
        setSavedLobs(new byte[selectedRows.length][]);
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            Object lob = table.getValueAt(selectedRow, table.getSelectedColumn());
            if (lob instanceof Blob) {
                getSavedLobs()[i] = ((Blob) lob).getBytes(1, (int) ((Blob) lob).length());
            } else if (lob instanceof Clob) {
                getSavedLobs()[i] = ((Clob) lob).getSubString(1, (int) ((Clob) lob).length()).getBytes();
            } else if (lob instanceof byte[]) {
                getSavedLobs()[i] = (byte[]) lob;
            } else if (lob != null) {
                throw new UnsupportedOperationException("Unsupported type");
            }
        }
    }
}
