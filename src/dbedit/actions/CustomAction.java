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

import dbedit.*;
import dbedit.Dialog;
import dbedit.plugin.Plugin;
import dbedit.plugin.PluginFactory;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.sql.*;

public abstract class CustomAction extends AbstractAction
                                   implements CellEditorListener, MouseListener, ListSelectionListener,
                                              TableColumnModelListener, AncestorListener, DocumentListener,
                                              KeyListener {

    private static ConnectionData connectionData;
    private static String query;
    private static int[] columnTypes;
    private static String[] columnTypeNames;
    private static int fetchLimit = 0;
    private static byte[][] savedLobs;
    private static File openedFile;
    protected static final Plugin PLUGIN = PluginFactory.getPlugin();
    private static JFileChooser fileChooser;

    protected CustomAction(String name, String icon, KeyStroke accelerator) {
        super(name);
        putValue(SMALL_ICON, new ImageIcon(CustomAction.class.getResource("/icons/" + icon)));
        putValue(ACCELERATOR_KEY, accelerator);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        new ThreadedAction() {
            @Override
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

    public void showFile(String text, byte[] bytes) throws Exception {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        boolean isXml = text.startsWith("<?xml");
        Object[] options = isXml
            ? new Object[] {"Save to file", "Save to file and open", "Copy to clipboard", "Pretty print XML", "Cancel"}
            : new Object[] {"Save to file", "Save to file and open", "Copy to clipboard", "Cancel"};
        Object value = Dialog.show("Preview", scrollPane, Dialog.PLAIN_MESSAGE, options, "Save to file");
        if ("Save to file".equals(value)) {
            String fileName = isXml ? "export.xml" : "export.txt";
            saveFile(fileName, bytes != null ? bytes : text.getBytes());
        } else if ("Save to file and open".equals(value)) {
            String fileName = isXml ? "export.xml" : "export.txt";
            File file = saveFile(fileName, bytes != null ? bytes : text.getBytes());
            if (file != null) {
                openFile(file);
            }
        } else if ("Copy to clipboard".equals(value)) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            } catch (Throwable t2) {
                ExceptionDialog.hideException(t2);
            }
        } else if ("Pretty print XML".equals(value)) {
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                transformer.transform(new StreamSource(new StringReader(text)), new StreamResult(outputStream));
                text = outputStream.toString();
            } catch (Throwable t) {
                ExceptionDialog.showException(t);
            }
            showFile(text, bytes);
        }
    }

    public void saveAndOpenFile(String fileName, byte[] bytes) throws Exception {
        File file = saveFile(fileName, bytes);
        if (file != null && Dialog.YES_OPTION == Dialog.show(
                "Open file", "Open the file with the associated application?",
                Dialog.QUESTION_MESSAGE, Dialog.YES_NO_OPTION)) {
            openFile(file);
        }
    }

    public File saveFile(String fileName, byte[] bytes) throws Exception {
        JFileChooser fileChooser = getFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(ApplicationPanel.getInstance())) {
            Config.saveLastUsedDir(fileChooser.getCurrentDirectory().getCanonicalPath());
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.exists() || Dialog.YES_OPTION == Dialog.show("File exists",
                    "Overwrite existing file?", Dialog.QUESTION_MESSAGE, Dialog.YES_NO_OPTION)) {
                FileOutputStream out = new FileOutputStream(selectedFile);
                out.write(bytes);
                out.close();
                return selectedFile;
            }
        }
        return null;
    }

    private void openFile(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }

    public void openURL(String uri) throws Exception {
        Desktop.getDesktop().browse(new URI(uri));
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
    }

    @Override
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
            Blob blob = getConnectionData().getConnection().createBlob();
            resultSet.updateBlob(column, blob);
            resultSet.updateRow();
            blob = resultSet.getBlob(column);
            blob.setBytes(1, (byte[]) o);
        } else if (Types.CLOB == columnType) {
            Clob clob = getConnectionData().getConnection().createClob();
            resultSet.updateClob(column, clob);
            resultSet.updateRow();
            clob = resultSet.getClob(column);
            clob.setString(1, new String((byte[]) o));
        } else if (Types.NCLOB == columnType) {
            NClob nclob = getConnectionData().getConnection().createNClob();
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

    public static ConnectionData getConnectionData() {
        return connectionData;
    }

    protected static void setConnectionData(ConnectionData newConnectionData) {
        connectionData = newConnectionData;
    }

    public static String getQuery() {
        return query;
    }

    public static void setQuery(String query) {
        CustomAction.query = query;
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

    public static File getOpenedFile() {
        return openedFile;
    }

    public static void setOpenedFile(File newOpenedFile) {
        openedFile = newOpenedFile;
    }

    protected static JFileChooser getFileChooser() throws IOException, SAXException, ParserConfigurationException {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        String dir = Config.getLastUsedDir();
        if (dir != null) {
            fileChooser.setCurrentDirectory(new File(dir));
        }
        return fileChooser;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
