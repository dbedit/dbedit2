package dbedit.actions;

import dbedit.ApplicationPanel;
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
        super("Export to file", "export.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 1) {
            File dir = DirectoryChooser.chooseDirectory();
            if (dir != null) {
                List columnNames = new ArrayList();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (table.getSelectedColumn() != i) {
                        columnNames.add(table.getColumnName(i));
                    }
                }
                final JList list = new JList(columnNames.toArray());
                list.setVisibleRowCount(15);
                list.addMouseListener(this);
                list.addListSelectionListener(this);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(ApplicationPanel.getInstance(), new JScrollPane(list), "Column for file name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex != -1) {
                        if (selectedIndex >= table.getSelectedColumn()) selectedIndex++;
                        for (int i = 0; i < selectedRows.length; i++) {
                            int selectedRow = selectedRows[i];
                            table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                            exportLob(new File(dir, "" + table.getValueAt(selectedRow, selectedIndex)));
                        }
                    }
                }
            }
        } else if (JFileChooser.APPROVE_OPTION == FILE_CHOOSER.showSaveDialog(ApplicationPanel.getInstance())) {
            exportLob(FILE_CHOOSER.getSelectedFile());
        }
    }

    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            Container container = (Container) e.getSource();
            while (!(container instanceof JOptionPane)) {
                container = container.getParent();
            }
            JOptionPane optionPane = (JOptionPane) container;
            Object value = optionPane.getInitialValue();
            if (value == null) {
                value = new Integer(JOptionPane.OK_OPTION);
            }
            optionPane.setValue(value);
            while (!(container instanceof JDialog)) {
                container = container.getParent();
            }
            container.setVisible(false);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }
}
