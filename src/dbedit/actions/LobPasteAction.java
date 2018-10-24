package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LobPasteAction extends LobAbstractAction {

    protected LobPasteAction() {
        super("Paste", "paste.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        int[] selectedRows = table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            ApplicationPanel.getInstance().setTableValue(getSavedLobs()[i]);
            editingStopped(null);
        }
    }
}
