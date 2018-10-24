package dbedit.actions;

import dbedit.ApplicationPanel;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;

public class DeleteAction extends CustomAction {

    protected DeleteAction() {
        super("Delete", "delete.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        int[] rows = ApplicationPanel.getInstance().getTable().getSelectedRows();
        ResultSet resultSet = connectionData.getResultSet();
        for (int i = rows.length - 1; i > -1; i--) {
            int row = rows[i];
            int origRow = ApplicationPanel.getInstance().getOriginalSelectedRow(row);
            resultSet.first();
            resultSet.relative(origRow);
            resultSet.deleteRow();
            ApplicationPanel.getInstance().removeRow(row);
        }
    }
}
