package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.ExceptionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EditAction extends CustomAction {

    protected EditAction() {
        super("Edit", "edit.png", null);
    }

    protected EditAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        ResultSet resultSet = connectionData.getResultSet();
        JPanel panel = new JPanel(new GridBagLayout());
        JTextArea[] textAreas = new JTextArea[resultSet.getMetaData().getColumnCount()];
        GridBagConstraints constraints = new GridBagConstraints(-1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        java.util.List selectedRow = ApplicationPanel.getInstance().getSelectedRow();
        for (int column = 0; column < resultSet.getMetaData().getColumnCount(); column++) {
            String columnName = resultSet.getMetaData().getColumnName(column + 1);
            panel.add(new JLabel(columnName), constraints);
            if (column + 1 == resultSet.getMetaData().getColumnCount()) {
                constraints.weightx = 100;
                constraints.weighty = 100;
            }
            panel.add(textAreas[column] = new JTextArea(), constraints);
            fillTextArea(textAreas[column], selectedRow, column);
            if (isLobSelected(column)) {
                textAreas[column].setEnabled(false);
            }
            textAreas[column].setBorder(BorderFactory.createLoweredBevelBorder());
            constraints.gridy++;
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setPreferredSize(new Dimension(600, 400));
        JOptionPane pane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(ApplicationPanel.getInstance(), (String) getValue(Action.NAME));
        dialog.setResizable(true);
        while (true) {
            try {
                dialog.show();
                if (JOptionPane.OK_OPTION == ((Number) pane.getValue()).intValue()) {
                    position(resultSet);
                    for (int i = 0; i < textAreas.length; i++) {
                        String text = textAreas[i].getText();
                        if (!textAreas[i].isEnabled()) {
                            continue;
                        }
                        update(i + 1, text);
                        updateSelectedRow(selectedRow, i, text);
                    }
                    store(resultSet);
                }
                break;
            } catch (Throwable t) {
                ExceptionDialog.showException(t);
            }
        }
    }

    protected void fillTextArea(JTextArea textArea, List selectedRow, int column) {
        textArea.setText(selectedRow.get(column) == null ? "" : selectedRow.get(column).toString());
    }

    protected void position(ResultSet resultSet) throws SQLException {
        int origRow = ApplicationPanel.getInstance().getOriginalSelectedRow();
        resultSet.first();
        resultSet.relative(origRow);
    }

    protected void updateSelectedRow(List selectedRow, int column, String text) {
        selectedRow.set(column, text);
    }

    protected void store(ResultSet resultSet) throws SQLException {
        resultSet.updateRow();
    }
}
