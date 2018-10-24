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
import dbedit.Grid;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.List;

public class ExportFlatFileAction extends CustomAction {

    protected ExportFlatFileAction() {
        super("Flat file", "text.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        boolean selection = false;
        if (table.getSelectedRowCount() > 0 && table.getSelectedRowCount() != table.getRowCount()) {
            Object option = Dialog.show("Flat file", "Export", Dialog.QUESTION_MESSAGE,
                    new Object[] {"Everything", "Selection"}, "Everything");
            if (option == null) {
                return;
            }
            selection = "Selection".equals(option);
        }
        Grid grid = new Grid();
        for (int i = 0; i < table.getColumnCount(); i++) {
            grid.set(i, 0, table.getColumnName(i));
        }
        grid.addSeparator();
        List list = ((DefaultTableModel) table.getModel()).getDataVector();
        int count = 2;
        int[] rightAlignedColumns = new int[table.getColumnCount()];
        for (int i = 0; i < list.size(); i++) {
            if (!selection || table.isRowSelected(i)) {
                List row = (List) list.get(i);
                for (int j = 0; j < row.size(); j++) {
                    rightAlignedColumns[j] = (rightAlignedColumns[j] > 0 || row.get(j) instanceof Number) ? j : -1;
                    String s = row.get(j) == null ? "" : row.get(j).toString();
                    if (!"".equals(s) && CustomAction.isLob(j)) {
                        s = CustomAction.getColumnTypeNames()[j];
                    }
                    s = s.replaceAll("\r\n", " | ").replaceAll("\n", " | ").replaceAll("\t", "    ");
                    if (s.length() > 500) {
                        s = s.substring(0, 500);
                    }
                    grid.set(j, count, s);
                }
                count++;
            }
        }
        grid.addSeparator();
        grid.set(0, grid.getHeight(), String.format("Total: %d", (grid.getHeight() - 3)));
        String text = grid.toString(rightAlignedColumns);
        showFile(text, null);
    }
}
