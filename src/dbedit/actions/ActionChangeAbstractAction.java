package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseEvent;
import java.util.*;

public abstract class ActionChangeAbstractAction extends CustomAction {

    static ListIterator<String> history = new ArrayList<String>().listIterator();

    protected ActionChangeAbstractAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void handleTextActions() {
        Actions.UNDO.setEnabled(ApplicationPanel.getInstance().undoManager.canUndo());
        Actions.REDO.setEnabled(ApplicationPanel.getInstance().undoManager.canRedo());
        Actions.HISTORY_PREVIOUS.setEnabled(history.hasPrevious());
        Actions.HISTORY_NEXT.setEnabled(history.hasNext());
    }

    protected void handleActions() {
        boolean isConnected = connectionData != null;
        Actions.DISCONNECT.setEnabled(isConnected);
        Actions.COMMIT.setEnabled(isConnected);
        Actions.ROLLBACK.setEnabled(isConnected);
        Actions.RUN.setEnabled(isConnected);
        Actions.RUN_SCRIPT.setEnabled(isConnected);

        boolean hasResultSet = isConnected && connectionData.getResultSet() != null;
        Actions.INSERT.setEnabled(hasResultSet);
        Actions.EXPORT_EXCEL.setEnabled(hasResultSet);
        Actions.EXPORT_FLAT_FILE.setEnabled(hasResultSet);
        Actions.EXPORT_INSERTS.setEnabled(hasResultSet);
        Actions.EXPORT_GROUP.setEnabled(hasResultSet);

        boolean isRowSelected = hasResultSet && !ApplicationPanel.getInstance().getTable().getSelectionModel().isSelectionEmpty();
        Actions.EDIT.setEnabled(isRowSelected);
        Actions.DUPLICATE.setEnabled(isRowSelected);
        Actions.DELETE.setEnabled(isRowSelected);

        boolean isLobSelected = hasResultSet && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn());
        Actions.LOB_IMPORT.setEnabled(isLobSelected);
        Actions.LOB_EXPORT.setEnabled(isLobSelected);
        Actions.LOB_OPEN.setEnabled(isLobSelected);
        Actions.LOB_OPEN_WITH.setEnabled(isLobSelected);
        Actions.LOB_COPY.setEnabled(isLobSelected);
        Actions.LOB_GROUP.setEnabled(isLobSelected);

        boolean canImportFromMemory = savedLobs != null && isLobSelected && ApplicationPanel.getInstance().getTable().getSelectedRowCount() == savedLobs.length;
        Actions.LOB_PASTE.setEnabled(canImportFromMemory);
    }

    public void changedUpdate(DocumentEvent e) {
        handleTextActions();
    }

    public void insertUpdate(DocumentEvent e) {
        handleTextActions();
    }

    public void removeUpdate(DocumentEvent e) {
        handleTextActions();
    }

    public void valueChanged(ListSelectionEvent e) {
        handleActions();
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            handleActions();
        }
    }

    public void mouseClicked(final MouseEvent e) {
        JTableHeader tableHeader = (JTableHeader) e.getSource();
        final int col = tableHeader.columnAtPoint(e.getPoint());
        @SuppressWarnings("unchecked")
        List<List> list = ((DefaultTableModel) tableHeader.getTable().getModel()).getDataVector();
        Collections.sort(list, new Comparator<List>() {
            public int compare(List l1, List l2) {
                Object o1 = (l1).get(col);
                Object o2 = (l2).get(col);
                int i =
                        o1 == null && o2 == null ? 0 :
                        o1 == null ? -1 :
                        o2 == null ? 1 :
                        o1 instanceof Number ? Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue()) :
                        o1.toString().compareTo(o2.toString());
                return i * (e.getButton() == MouseEvent.BUTTON1 ? 1 : -1);
            }
        });
        ApplicationPanel.getInstance().updateUI();
    }
}
