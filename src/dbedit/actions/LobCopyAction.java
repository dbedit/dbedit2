package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Blob;
import java.sql.Clob;

public class LobCopyAction extends LobAbstractAction {

    protected LobCopyAction() {
        super("Copy", "copy.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JTable table = ApplicationPanel.getInstance().getTable();
        int[] selectedRows = table.getSelectedRows();
        setSavedLobs(new byte[selectedRows.length][]);
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = selectedRows[i];
            Object lob = table.getValueAt(selectedRow, table.getSelectedColumn());
            if (lob instanceof Blob) {
                getSavedLobs()[i] = ((Blob) lob).getBytes(1, (int) ((Blob) lob).length());
            } else if (lob instanceof Clob) {
                getSavedLobs()[i] = ((Clob) lob).getSubString(1, (int) ((Clob) lob).length()).getBytes();
            } else if (lob instanceof byte[]) {
                getSavedLobs()[i] = (byte[]) lob;
            } else if (lob != null) {
                throw new UnsupportedOperationException("Unsupported type");
            }
        }
    }
}
