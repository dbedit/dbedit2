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
import dbedit.Dialog;
import dbedit.DirectoryChooser;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LobExportAction extends LobAbstractAction {

    protected LobExportAction() {
        super("Export", "export.png");
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
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
                list.addListSelectionListener(this);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                if (Dialog.OK_OPTION == Dialog.show("Column for file name", new JScrollPane(list),
                        Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex != -1) {
                        if (selectedIndex >= table.getSelectedColumn()) {
                            selectedIndex++;
                        }
                        for (int selectedRow : selectedRows) {
                            table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                            exportLob(new File(dir, "" + table.getValueAt(selectedRow, selectedIndex)), getLob());
                        }
                    }
                }
            }
        } else {
            byte[] lob = getLob();
            if (lob != null) {
                if (isAscii(lob)) {
                    showFile(new String(lob), lob);
                } else {
                    String ext = guessType(lob);
                    saveAndOpenFile("export" + ext, lob);
                }
            }
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
                    && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn())) {
                actionPerformed(null);
            } else if (e.getSource() instanceof JList) {
                Container container = (Container) e.getSource();
                while (!(container instanceof JOptionPane)) {
                    container = container.getParent();
                }
                JOptionPane optionPane = (JOptionPane) container;
                Object value = optionPane.getInitialValue();
                if (value == null) {
                    value = JOptionPane.OK_OPTION;
                }
                optionPane.setValue(value);
                while (!(container instanceof JDialog)) {
                    container = container.getParent();
                }
                container.setVisible(false);
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }
}
