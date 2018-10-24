package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Grid;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExportFlatFileAction extends CustomAction {

    protected ExportFlatFileAction() {
        super("Flat file", "text.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        boolean selection = false;
        if (table.getSelectedRowCount() > 0) {
            int option = JOptionPane.showOptionDialog(ApplicationPanel.getInstance(), "Export", "Flat file", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Everything", "Selection"}, "Everything");
            if (JOptionPane.CLOSED_OPTION == option) {
                return;
            }
            selection = 1 == option;
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
                    s = s.replaceAll("\r\n", " | ").replaceAll("\n", " | ").replaceAll("\t", "    ");
                    if (s.length() > 500) s = s.substring(0, 500);
                    grid.set(j, count, s);
                }
                count++;
            }
        }
        grid.addSeparator();
        grid.set(0, grid.getHeight(), "Total: " + (grid.getHeight() - 3));
        File file = File.createTempFile("export", ".txt");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        out.write(grid.toString(rightAlignedColumns).getBytes());
        out.close();
        openFile(file.toString());
    }
}
