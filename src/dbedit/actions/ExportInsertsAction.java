package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Types;
import java.util.StringTokenizer;

public class ExportInsertsAction extends CustomAction {

    protected ExportInsertsAction() {
        super("Insert statements", "source.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        boolean selection = false;
        if (table.getSelectedRowCount() > 0) {
            int option = JOptionPane.showOptionDialog(ApplicationPanel.getInstance(), "Export", "Insert statements", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Everything", "Selection"}, "Everything");
            if (JOptionPane.CLOSED_OPTION == option) {
                return;
            }
            selection = 1 == option;
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
            int columnType = connectionData.getResultSet().getMetaData().getColumnType(column + 1);
            parseDate[column] = connectionData.isOracle() && columnType == Types.DATE;
            isLob[column] = isLobSelected(column);
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
                        inserts.append(value);
                        inserts.append("'");
                    }
                    if (column + 1 < columnCount) {
                        inserts.append(",");
                    }
                }
                inserts.append(");\n");
            }
        }
        File file = File.createTempFile("export", ".txt");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        out.write(inserts.toString().getBytes());
        out.close();
        openFile(file.toString());
    }
}
