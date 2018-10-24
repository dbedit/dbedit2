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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Types;
import java.util.Scanner;

public class ExportInsertsAction extends CustomAction {

    protected ExportInsertsAction() {
        super("Insert statements", "source.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        boolean selection = false;
        if (table.getSelectedRowCount() > 0 && table.getSelectedRowCount() != table.getRowCount()) {
            Object option = Dialog.show("Insert statements", "Export", Dialog.QUESTION_MESSAGE,
                    new Object[] {"Everything", "Selection"}, "Everything");
            if (option == null) {
                return;
            }
            selection = "Selection".equals(option);
        }
        String tableName = "?";
        Scanner scanner = new Scanner(getQuery());
        while (scanner.hasNext()) {
            if ("from".equals(scanner.next().toLowerCase())) {
                if (scanner.hasNext()) {
                    tableName = scanner.next();
                }
                break;
            }
        }
        StringBuilder prefix = new StringBuilder();
        prefix.append("insert into ");
        prefix.append(tableName);
        prefix.append(" (");
        int rowCount = table.getRowCount();
        int columnCount = table.getColumnCount();
        boolean[] isLob = new boolean[columnCount];
        boolean[] parseDate = new boolean[columnCount];
        for (int column = 0; column < columnCount; column++) {
            prefix.append(getConnectionData().checkMixedCaseQuotedIdentifier(table.getColumnName(column)));
            if (column + 1 < columnCount) {
                prefix.append(",");
            }
            parseDate[column] = getConnectionData().isOracle()
                    && (getColumnTypes()[column] == Types.DATE || getColumnTypes()[column] == Types.TIMESTAMP);
            isLob[column] = isLob(column);
        }
        prefix.append(") values (");
        StringBuilder inserts = new StringBuilder();
        for (int row = 0; row < rowCount; row++) {
            if (!selection || table.isRowSelected(row)) {
                inserts.append(prefix);
                for (int column = 0; column < columnCount; column++) {
                    Object value = table.getValueAt(row, column);
                    if (value instanceof Number || value == null) {
                        inserts.append(value);
                    } else if (parseDate[column]) {
                        inserts.append("to_date('");
                        value = value.toString().substring(0, value.toString().indexOf('.'));
                        inserts.append(value);
                        inserts.append("','YYYY-MM-DD HH24:MI:SS')");
                    } else if (isLob[column]) {
                        inserts.append("null");
                    } else {
                        inserts.append("'");
                        inserts.append(value.toString().replaceAll("'", "''"));
                        inserts.append("'");
                    }
                    if (column + 1 < columnCount) {
                        inserts.append(",");
                    }
                }
                inserts.append(");\n");
            }
        }
        String text = inserts.toString();
        showFile(text, null);
    }
}
