package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Types;
import java.util.StringTokenizer;

public class ExportInsertsAction extends CustomAction {

    protected ExportInsertsAction() {
        super("Insert statements", "source.png", null);
    }

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
        StringTokenizer tokenizer = new StringTokenizer(ApplicationPanel.getInstance().getText());
        while (tokenizer.hasMoreTokens()) {
            if ("from".equals(tokenizer.nextToken().toLowerCase())) {
                if (tokenizer.hasMoreTokens()) {
                    tableName = tokenizer.nextToken();
                }
                break;
            }
        }
        StringBuffer prefix = new StringBuffer();
        prefix.append("insert into ");
        prefix.append(tableName);
        prefix.append(" (");
        int rowCount = table.getRowCount();
        int columnCount = table.getColumnCount();
        boolean[] isLob = new boolean[columnCount];
        boolean[] parseDate = new boolean[columnCount];
        for (int column = 0; column < columnCount; column++) {
            prefix.append(table.getColumnName(column));
            if (column + 1 < columnCount) {
                prefix.append(",");
            }
            parseDate[column] = getConnectionData().isOracle() && getColumnTypes()[column] == Types.DATE;
            isLob[column] = isLob(column);
        }
        prefix.append(") values (");
        StringBuffer inserts = new StringBuffer();
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
        openFile("export", ".txt", inserts.toString().getBytes());
    }
}
