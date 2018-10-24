/*
 * DBEdit 2
 * Copyright (C) 2006-2012 Jef Van Den Ouweland
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
package dbedit;

import dbedit.actions.Actions;
import dbedit.actions.CopyCellValueAction;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public final class ResultSetTable extends JTable {

    private static final ResultSetTable RESULT_SET_TABLE = new ResultSetTable();

    private List<Vector> originalOrder;

    private ResultSetTable() {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setToolTipText("<html>Left click: sort asc<br>Right click: sort desc</html>");
        getTableHeader().addMouseListener(new TableSorter());
        getTableHeader().setFont(getTableHeader().getFont().deriveFont(Font.BOLD));
        addMouseListener(Actions.LOB_EXPORT);
        getSelectionModel().addListSelectionListener(Actions.getInstance());
        getColumnModel().addColumnModelListener(Actions.getInstance());
        setModel(new ResultSetTableModel());
        setDefaultRenderer(Object.class, new ResultSetTableCellRenderer());
        getActionMap().put("copy", new CopyCellValueAction(getActionMap().get("copy")));
    }

    public static ResultSetTable getInstance() {
        return RESULT_SET_TABLE;
    }

    public Object getTableValue() {
        int row = getSelectedRow();
        int column = getSelectedColumn();
        return getValueAt(row, column);
    }

    public void setTableValue(Object o) {
        int row = getSelectedRow();
        int column = getSelectedColumn();
        setValueAt(o, row, column);
    }

    @SuppressWarnings("unchecked")
    public List<String> getSelectedRowData() {
        int row = getSelectedRow();
        if (row != -1) {
            return (List<String>) ((DefaultTableModel) getModel()).getDataVector().get(row);
        } else {
            return null;
        }
    }

    public int getOriginalSelectedRow() {
        int row = getSelectedRow();
        return getOriginalSelectedRow(row);
    }

    public int getOriginalSelectedRow(int selectedRow) {
        Vector row = (Vector) ((DefaultTableModel) getModel()).getDataVector().get(selectedRow);
        return originalOrder.indexOf(row);
    }

    public void removeRow(int row) {
        int originalSelectedRow = getOriginalSelectedRow(row);
        ((DefaultTableModel) getModel()).removeRow(row);
        originalOrder.remove(originalSelectedRow);
    }

    public void setDataVector(Vector<Vector> dataVector, Vector columnIdentifiers, String executionTime) {
        originalOrder = new ArrayList<Vector>(dataVector);
        ((DefaultTableModel) getModel()).setDataVector(dataVector, columnIdentifiers);
        String rows = String.format("%d %s", dataVector.size(), dataVector.size() != 1 ? "rows" : "row");
        JComponent scrollPane = (JComponent) getParent().getParent();
        if (dataVector.isEmpty()) {
            scrollPane.setToolTipText(String.format("%s - %s", rows, executionTime));
            setToolTipText(null);
        } else {
            setToolTipText(String.format("%s - %s", rows, executionTime));
            scrollPane.setToolTipText(null);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                resizeColumns();
            }
        });
    }

    public static boolean isLob(int column) {
        if (column == -1 || column >= Context.getInstance().getColumnTypes().length) {
            return false;
        }
        int columnType = Context.getInstance().getColumnTypes()[column];
        return Types.LONGVARBINARY == columnType
                || Types.VARBINARY == columnType
                || Types.BLOB == columnType
                || Types.CLOB == columnType
                || 2007 /* oracle xmltype */ == columnType;
    }

    protected void resizeColumns() {
        double[] widths = new double[getColumnCount()];
        TableCellRenderer defaultRenderer = getDefaultRenderer(Object.class);
        for (int i = 0; i < getModel().getRowCount(); i++) {
            for (int j = 0; j < getModel().getColumnCount(); j++) {
                Component component = defaultRenderer.getTableCellRendererComponent(
                        this, getModel().getValueAt(i, j), false, false, i, j);
                widths[j] = Math.max(widths[j], component.getPreferredSize().getWidth());
            }
        }
        defaultRenderer = getTableHeader().getDefaultRenderer();
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            Component component = defaultRenderer.getTableCellRendererComponent(
                    this, columnModel.getColumn(i).getHeaderValue(), false, false, 0, i);
            widths[i] = Math.max(widths[i], component.getPreferredSize().getWidth());
            widths[i] = Math.min(widths[i], 550);
            columnModel.getColumn(i).setPreferredWidth((int) widths[i] + 2);
        }
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        super.editingStopped(e);
        ResultSet resultSet = Context.getInstance().getResultSet();
        int column = getSelectedColumn();
        Object value = null;
        try {
            int origRow = getOriginalSelectedRow();
            resultSet.first();
            resultSet.relative(origRow);
            value = resultSet.getObject(column + 1);
            if (value == null || !value.toString().equals(getTableValue())) {
                update(column + 1, getTableValue());
                resultSet.updateRow();
                try {
                    value = resultSet.getObject(column + 1);
                } catch (SQLException e1) {
                    // e.g. Derby: java.sql.SQLException: Stream or LOB value cannot be retrieved more than once
                    ExceptionDialog.hideException(e1);
                }
            }
        } catch (Throwable t) {
            if (e == null) {
                // explicitly invoked
                throw new RuntimeException(t.getMessage(), t);
            }
            ExceptionDialog.showException(t);
        } finally {
            try {
                if (value != null) {
                    setTableValue(value);
                }
            } catch (Exception e1) {
                ExceptionDialog.hideException(e1);
            }
        }
    }

    public void update(int column, Object o) throws Exception {
        ConnectionData connectionData = Context.getInstance().getConnectionData();
        ResultSet resultSet = Context.getInstance().getResultSet();
        int columnType = resultSet.getMetaData().getColumnType(column);
        if (Types.LONGVARBINARY == columnType || Types.VARBINARY == columnType) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[]) o);
            resultSet.updateBinaryStream(column, byteArrayInputStream, byteArrayInputStream.available());
        } else if (Types.BLOB == columnType) {
            Blob blob = connectionData.getConnection().createBlob();
            resultSet.updateBlob(column, blob);
            resultSet.updateRow();
            blob = resultSet.getBlob(column);
            blob.setBytes(1, (byte[]) o);
        } else if (Types.CLOB == columnType) {
            Clob clob = connectionData.getConnection().createClob();
            resultSet.updateClob(column, clob);
            resultSet.updateRow();
            clob = resultSet.getClob(column);
            clob.setString(1, new String((byte[]) o));
        } else if (Types.NCLOB == columnType) {
            NClob nclob = connectionData.getConnection().createNClob();
            resultSet.updateNClob(column, nclob);
            resultSet.updateRow();
            nclob = resultSet.getNClob(column);
            nclob.setString(1, new String((byte[]) o));
        } else {
            if (o != null && "".equals(o.toString())) {
                o = null;
            }
            resultSet.updateObject(column, o);
        }
    }

    private class ResultSetTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            try {
                return Context.getInstance().getConnectionData() != null
                        && Context.getInstance().getResultSet() != null
                        && Context.getInstance().getResultSet().getConcurrency() == ResultSet.CONCUR_UPDATABLE
                        && !isLob(column);
            } catch (SQLException e) {
                return false;
            }
        }
    }

    private class ResultSetTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable componentTable, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (value != null && isLob(column)) {
                value = Context.getInstance().getColumnTypeNames()[column];
            }
            JLabel tableCellRendererComponent = (JLabel) super.getTableCellRendererComponent(
                    componentTable, value, isSelected, hasFocus, row, column);
            tableCellRendererComponent.setHorizontalAlignment(
                    value instanceof Number ? SwingConstants.TRAILING : SwingConstants.LEADING);
            return tableCellRendererComponent;
        }
    }
}
