package dbedit.actions;

import dbedit.*;
import dbedit.plugin.Plugin;
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Vector;

public abstract class CustomAction extends AbstractAction
                                   implements CellEditorListener, MouseListener, ListSelectionListener,
                                              TableColumnModelListener, AncestorListener, DocumentListener,
                                              KeyListener {

    private static ConnectionData connectionData;
    private static int[] columnTypes;
    private static String[] columnTypeNames;
    private static Vector<ConnectionData> connectionDatas;
    private static int fetchLimit = -1;
    private static byte[][] savedLobs;
    protected static final Plugin PLUGIN = PluginFactory.getPlugin();
    private static JFileChooser fileChooser;

    protected CustomAction(String name, String icon, KeyStroke accelerator) {
        super(name);
        putValue(SMALL_ICON, new ImageIcon(CustomAction.class.getResource("/icons/" + icon)));
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

    public static boolean isLob(int column) {
        if (column == -1 || column >= columnTypes.length) {
            return false;
        }
        int columnType = columnTypes[column];
        return Types.LONGVARBINARY == columnType
                || Types.VARBINARY == columnType
                || Types.BLOB == columnType
                || Types.CLOB == columnType
                || 2007 /* oracle xmltype */ == columnType;
    }

    public void openFile(String prefix, String suffix, byte[] bytes) throws Exception {
        if (Config.IS_OS_WINDOWS) {
            File file = File.createTempFile(prefix, suffix);
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.close();
            openURL(file.toString());
        } else {
            getFileChooser().setSelectedFile(new File(prefix + suffix));
            if (JFileChooser.APPROVE_OPTION == getFileChooser().showSaveDialog(ApplicationPanel.getInstance())) {
                File selectedFile = getFileChooser().getSelectedFile();
                if (!selectedFile.exists() || Dialog.YES_OPTION == Dialog.show("File exists",
                        "Overwrite existing file?", Dialog.QUESTION_MESSAGE, Dialog.YES_NO_OPTION)) {
                    FileOutputStream out = new FileOutputStream(selectedFile);
                    out.write(bytes);
                    out.close();
                }
            }
        }
    }

    public void openURL(String file) throws Exception {
        if (Config.IS_OS_WINDOWS) {
            Runtime.getRuntime().exec(new String[] {"rundll32", "shell32,ShellExec_RunDLL", file});
        } else if (Config.IS_OS_MAC_OS) {
           Class fileMgr = Class.forName("com.apple.eio.FileManager");
           Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
           openURL.invoke(null, file);
        } else {
            // Assume Unix
            String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
            try {
                for (String browser : browsers) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browser}).waitFor() == 0) {
                        Runtime.getRuntime().exec(new String[]{browser, file});
                        break;
                    }
                }
            } catch (IOException e) {
                Runtime.getRuntime().exec(new String[]{"netscape", file});
            }
        }
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
                ExceptionDialog.hideException(e1);
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

    public static ConnectionData getConnectionData() {
        return connectionData;
    }

    protected static void setConnectionData(ConnectionData newConnectionData) {
        connectionData = newConnectionData;
    }

    public static int[] getColumnTypes() {
        return columnTypes;
    }

    protected static void setColumnTypes(int[] newColumnTypes) {
        columnTypes = newColumnTypes;
    }

    public static String[] getColumnTypeNames() {
        return columnTypeNames;
    }

    protected static void setColumnTypeNames(String[] newColumnTypeNames) {
        columnTypeNames = newColumnTypeNames;
    }

    protected static Vector<ConnectionData> getConnectionDatas() {
        return connectionDatas;
    }

    protected static void setConnectionDatas(Vector<ConnectionData> newConnectionDatas) {
        connectionDatas = newConnectionDatas;
    }

    protected static int getFetchLimit() {
        return fetchLimit;
    }

    protected static void setFetchLimit(int newFetchLimit) {
        fetchLimit = newFetchLimit;
    }

    protected static byte[][] getSavedLobs() {
        return savedLobs;
    }

    protected static void setSavedLobs(byte[][] newSavedLobs) {
        savedLobs = newSavedLobs;
    }

    protected static JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        return fileChooser;
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
