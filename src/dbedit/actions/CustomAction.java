package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.ConnectionData;
import dbedit.ExceptionDialog;
import dbedit.ThreadedAction;
import dbedit.plugin.Plugin;
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Vector;

public abstract class CustomAction extends AbstractAction implements CellEditorListener, MouseListener, ListSelectionListener, TableColumnModelListener, AncestorListener, DocumentListener, KeyListener {

    public static ConnectionData connectionData;
    protected static Vector connectionDatas;
    protected static int fetchLimit = -1;
    protected static byte[][] savedLobs;
    protected static final Plugin PLUGIN = PluginFactory.getPlugin();

    protected CustomAction(String name, String icon, KeyStroke accelerator) {
        super(name);
        if (icon != null) {
            putValue(SMALL_ICON, new ImageIcon(CustomAction.class.getResource("/icons/" + icon)));
        }
        putValue(ACCELERATOR_KEY, accelerator);
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent e) {
        new ThreadedAction() {
            protected void execute() throws Exception {
                performThreaded(e);
            }
        };
    }

    protected abstract void performThreaded(ActionEvent e) throws Exception;

    public static boolean isLobSelected(int column) {
        try {
            ResultSet resultSet = connectionData.getResultSet();
            if (resultSet != null) {
                int columnType = resultSet.getMetaData().getColumnType(column + 1);
                return Types.LONGVARBINARY == columnType || Types.VARBINARY == columnType || Types.BLOB == columnType || Types.CLOB == columnType;
            }
        } catch (Throwable t) {
            // ignore
        }
        return false;
    }

    public void openFile(String file) throws IOException {
        Runtime.getRuntime().exec(new String[] {"rundll32", "shell32,ShellExec_RunDLL", file});
    }

    public void editingCanceled(ChangeEvent e) {
    }

    public void editingStopped(ChangeEvent e) {
        ResultSet resultSet = connectionData.getResultSet();
        int column = ApplicationPanel.getInstance().getTable().getSelectedColumn();
        Object value = null;
        try {
            int origRow = ApplicationPanel.getInstance().getOriginalSelectedRow();
            resultSet.first();
            resultSet.relative(origRow);
            value = resultSet.getObject(column + 1);
            if (value == null || !value.toString().equals(ApplicationPanel.getInstance().getTableValue())) {
                String log = ("" + value).trim();
                update(column + 1, ApplicationPanel.getInstance().getTableValue());
                resultSet.updateRow();
                value = resultSet.getObject(column + 1);
                log += " -> " + ("" + resultSet.getObject(column + 1)).trim();
                PLUGIN.audit(log);
            }
        } catch (Throwable t) {
            ExceptionDialog.showException(t);
        } finally {
            try {
                if (value != null) {
                    ApplicationPanel.getInstance().setTableValue(value);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    protected void update(int column, Object o) throws Exception {
        ResultSet resultSet = connectionData.getResultSet();
        int columnType = resultSet.getMetaData().getColumnType(column);
        if (Types.LONGVARBINARY == columnType || Types.VARBINARY == columnType) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[]) o);
            resultSet.updateBinaryStream(column, byteArrayInputStream, byteArrayInputStream.available());
        } else if (Types.BLOB == columnType) {
            String sql;
            if (connectionData.isOracle()) {
                sql = "select empty_blob() from dual";
            } else if (connectionData.isDb2()) {
                sql = "select blob('') from sysibm.sysdummy1";
            } else {
                throw new Exception("Not supported");
            }
            ResultSet set = resultSet.getStatement().getConnection().createStatement().executeQuery(sql);
            set.next();
            Blob blob = set.getBlob(1);
            resultSet.updateBlob(column, blob);
            resultSet.updateRow();
            blob = resultSet.getBlob(column);
            blob.setBytes(1, (byte[]) o);
        } else if (Types.CLOB == columnType) {
            String sql;
            if (connectionData.isOracle()) {
                sql = "select empty_clob() from dual";
            } else if (connectionData.isDb2()) {
                sql = "select clob('') from sysibm.sysdummy1";
            } else {
                throw new Exception("Not supported");
            }
            ResultSet set = resultSet.getStatement().getConnection().createStatement().executeQuery(sql);
            set.next();
            Clob clob = set.getClob(1);
            resultSet.updateClob(column, clob);
            resultSet.updateRow();
            clob = resultSet.getClob(column);
            clob.setString(1, new String((byte[]) o));
        } else {
            if (o != null && "".equals(o.toString().trim())) {
                o = null;
            }
            resultSet.updateObject(column, o);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void valueChanged(ListSelectionEvent e) {
    }

    public void columnMarginChanged(ChangeEvent e) {
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    public void columnAdded(TableColumnModelEvent e) {
    }

    public void columnMoved(TableColumnModelEvent e) {
    }

    public void columnRemoved(TableColumnModelEvent e) {
    }

    public void ancestorAdded(AncestorEvent event) {
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
