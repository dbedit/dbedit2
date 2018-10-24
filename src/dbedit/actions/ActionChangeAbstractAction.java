/*
 * DBEdit 2
 * Copyright (C) 2006-2011 Jef Van Den Ouweland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.History;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ActionChangeAbstractAction extends CustomAction {

    private static History history = new History();

    protected ActionChangeAbstractAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void handleTextActions() {
        if (Actions.UNDU_REDO_ENABLED) {
            Actions.UNDO.setEnabled(ApplicationPanel.getInstance().getUndoManager().canUndo());
            Actions.REDO.setEnabled(ApplicationPanel.getInstance().getUndoManager().canRedo());
        }
        Actions.HISTORY_PREVIOUS.setEnabled(history.hasPrevious());
        Actions.HISTORY_NEXT.setEnabled(history.hasNext());
    }

    protected void handleActions() {
        boolean isConnected = getConnectionData() != null;
        Actions.DISCONNECT.setEnabled(isConnected);
        Actions.COMMIT.setEnabled(isConnected);
        Actions.ROLLBACK.setEnabled(isConnected);
        Actions.RUN.setEnabled(isConnected);
        Actions.RUN_SCRIPT.setEnabled(isConnected);

        boolean hasResultSet = isConnected && getConnectionData().getResultSet() != null;
        Actions.EXPORT_EXCEL.setEnabled(hasResultSet);
        Actions.EXPORT_PDF.setEnabled(hasResultSet);
        Actions.EXPORT_FLAT_FILE.setEnabled(hasResultSet);
        Actions.EXPORT_INSERTS.setEnabled(hasResultSet);
        Actions.EXPORT_GROUP.setEnabled(hasResultSet);

        boolean hasUpdatableResultSet;
        try {
            hasUpdatableResultSet = hasResultSet
                    && getConnectionData().getResultSet().getConcurrency() == ResultSet.CONCUR_UPDATABLE;
        } catch (SQLException e) {
            hasUpdatableResultSet = false;
        }
        Actions.INSERT.setEnabled(hasUpdatableResultSet);

        boolean isRowSelected = hasResultSet
                && !ApplicationPanel.getInstance().getTable().getSelectionModel().isSelectionEmpty();
        Actions.EDIT.setEnabled(isRowSelected);

        boolean isUpdatableRowSelected = hasUpdatableResultSet && isRowSelected;
        Actions.DUPLICATE.setEnabled(isUpdatableRowSelected);
        Actions.DELETE.setEnabled(isUpdatableRowSelected);

        boolean isLobSelected = hasResultSet && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn());
        Actions.LOB_EXPORT.setEnabled(isLobSelected);
        Actions.LOB_COPY.setEnabled(isLobSelected);
        Actions.LOB_GROUP.setEnabled(isLobSelected);

        boolean isUpdatableLobSelected = hasUpdatableResultSet
                && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn());
        Actions.LOB_IMPORT.setEnabled(isUpdatableLobSelected);

        boolean canImportFromMemory = getSavedLobs() != null && isUpdatableLobSelected
                && ApplicationPanel.getInstance().getTable().getSelectedRowCount() == getSavedLobs().length;
        Actions.LOB_PASTE.setEnabled(canImportFromMemory);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        handleTextActions();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        handleTextActions();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleTextActions();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        handleActions();
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            handleActions();
        }
    }


    protected static History getHistory() {
        return history;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        JTableHeader tableHeader = (JTableHeader) e.getSource();
        final int col = tableHeader.columnAtPoint(e.getPoint());
        @SuppressWarnings("unchecked")
        List<List> list = ((DefaultTableModel) tableHeader.getTable().getModel()).getDataVector();
        Collections.sort(list, new Comparator<List>() {
            @Override
            public int compare(List l1, List l2) {
                Object o1 = (l1).get(col);
                Object o2 = (l2).get(col);
                int i = o1 == null && o2 == null ? 0
                                : o1 == null ? -1
                                : o2 == null ? 1
                                : o1 instanceof Number
                                        ? Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue())
                                        : o1.toString().compareTo(o2.toString());
                return i * (e.getButton() == MouseEvent.BUTTON1 ? 1 : -1);
            }
        });
        ApplicationPanel.getInstance().updateUI();
    }
}
