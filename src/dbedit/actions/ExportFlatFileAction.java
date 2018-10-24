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

    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        boolean selection = false;
        if (table.getSelectedRowCount() > 0 && table.getSelectedRowCount() != table.getRowCount()) {
            Object option = Dialog.show("Flat file", "Export", Dialog.QUESTION_MESSAGE, new Object[] {"Everything", "Selection"}, "Everything");
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
                    if (i == 0) {
                        rightAlignedColumns[j] = row.get(j) instanceof Number ? j : -1;
                    }
                    String s = row.get(j) == null ? "" : row.get(j).toString();
                    if (!"".equals(s) && CustomAction.isLob(j)) {
                        s = CustomAction.columnTypeNames[j];
                    }
                    s = s.replaceAll("\r\n", " | ").replaceAll("\n", " | ").replaceAll("\t", "    ");
                    if (s.length() > 500) s = s.substring(0, 500);
                    grid.set(j, count, s);
                }
                count++;
            }
        }
        grid.addSeparator();
        grid.set(0, grid.getHeight(), "Total: " + (grid.getHeight() - 3));
        openFile("export", ".txt", grid.toString(rightAlignedColumns).getBytes());
    }
}
