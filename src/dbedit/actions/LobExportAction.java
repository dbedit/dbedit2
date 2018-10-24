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

import dbedit.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LobExportAction extends CustomAction {

    protected LobExportAction() {
        super("Export", "export.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ResultSetTable.getInstance();
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 1) {
            File dir = DirectoryChooser.chooseDirectory();
            if (dir != null) {
                List<String> columnNames = new ArrayList<String>();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (table.getSelectedColumn() != i) {
                        columnNames.add(table.getColumnName(i));
                    }
                }
                final JList list = new JList(columnNames.toArray());
                list.addMouseListener(this);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                if (Dialog.OK_OPTION == Dialog.show("Column for file name", new JScrollPane(list),
                        Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex != -1) {
                        if (selectedIndex >= table.getSelectedColumn()) {
                            selectedIndex++;
                        }
                        WaitingDialog waitingDialog = new WaitingDialog(null);
                        try {
                            waitingDialog.setText(String.format("0/%d", selectedRows.length));
                            for (int i = 0; i < selectedRows.length && waitingDialog.isVisible(); i++) {
                                int selectedRow = selectedRows[i];
                                FileIO.writeFile(
                                        new File(dir, String.valueOf(table.getValueAt(selectedRow, selectedIndex))),
                                        getLob(selectedRow, table.getSelectedColumn()));
                                waitingDialog.setText(String.format("%d/%d", i + 1, selectedRows.length));
                            }
                        } finally {
                            waitingDialog.hide();
                        }
                    }
                }
            }
        } else {
            byte[] lob = getLob(table.getSelectedRow(), table.getSelectedColumn());
            if (lob != null) {
                if (isAscii(lob)) {
                    ExportPreviewer.preview(new String(lob), lob);
                } else {
                    String ext = guessType(lob);
                    FileIO.saveAndOpenFile("export" + ext, lob);
                }
            }
        }
    }

    private byte[] getLob(int row, int column) throws SQLException {
        Object lob = ResultSetTable.getInstance().getValueAt(row, column);
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

    private String guessType(byte[] b) {
        if (startsWith(b, '%', 'P', 'D', 'F')) {
            return ".pdf";
        } else if (startsWith(b, 'G', 'I', 'F', '8')) {
            return ".gif";
        } else if (startsWith(b, 0xFF, 0xD8, 0xFF)) {
            return ".jpg";
        } else if (startsWith(b, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return ".png";
        } else if (startsWith(b, 'P', 'K', 3, 4)) {
            return ".zip";
        } else if (startsWith(b, 0x1b, '*')) {
            return ".pcl";
        } else if (startsWith(b, 0x1b, '%')) {
            return ".pcl";
        } else if (startsWith(b, 0x1b, '&')) {
            return ".pcl";
        } else {
            return "";
        }
    }

    private boolean startsWith(byte[] bytes, int... b) {
        for (int i = 0; i < b.length && i < bytes.length; i++) {
            if ((bytes[i] & 0xFF) != b[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isAscii(byte[] bytes) {
        for (byte b : bytes) {
            if (Character.isISOControl((char) b) && b != '\n' && b != '\r' && b != '\t') {
                return false;
            }
        }
        return true;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (e.getSource() instanceof JTable
                    && ResultSetTable.isLob(ResultSetTable.getInstance().getSelectedColumn())) {
                actionPerformed(null);
            } else  {
                super.mouseClicked(e);
            }
        }
    }
}
