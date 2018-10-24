package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Dialog;
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
        ResultSet resultSet = getConnectionData().getResultSet();
        JPanel panel = new JPanel(new GridBagLayout());
        JTextArea[] textAreas = new JTextArea[resultSet.getMetaData().getColumnCount()];
        GridBagConstraints constraints = new GridBagConstraints(-1, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        List<String> selectedRow = ApplicationPanel.getInstance().getSelectedRow();
        for (int column = 0; column < resultSet.getMetaData().getColumnCount(); column++) {
            String columnName = resultSet.getMetaData().getColumnName(column + 1);
            panel.add(new JLabel(columnName), constraints);
            if (column + 1 == resultSet.getMetaData().getColumnCount()) {
                constraints.weightx = 100;
                constraints.weighty = 100;
            }
            textAreas[column] = new JTextArea();
            panel.add(textAreas[column], constraints);
            fillTextArea(textAreas[column], selectedRow, column);
            if (isLob(column)) {
                textAreas[column].setEnabled(false);
            }
            textAreas[column].setBorder(BorderFactory.createLoweredBevelBorder());
            constraints.gridy++;
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setPreferredSize(new Dimension(600, 400));
        while (true) {
            try {
                if (Dialog.OK_OPTION == Dialog.show((String) getValue(Action.NAME), scrollPane,
                        Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
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

    protected void updateSelectedRow(List<String> selectedRow, int column, String text) {
        selectedRow.set(column, text);
    }

    protected void store(ResultSet resultSet) throws SQLException {
        resultSet.updateRow();
    }
}
