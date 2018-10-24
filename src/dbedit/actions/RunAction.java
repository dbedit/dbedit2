package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.WaitingDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Vector;

public class RunAction extends ActionChangeAbstractAction {

    protected RunAction() {
        super("Run", "run.png", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        String text = ApplicationPanel.getInstance().getText();
        String originalText = text;
        history.add(text);
        handleTextActions();
        Vector columnIdentifiers = new Vector();
        Vector dataVector = new Vector();
        if (text.trim().toLowerCase().startsWith("select") || text.trim().toLowerCase().startsWith("show")) {
            int resultSetType;
            String originalQuery = text;
            if (connectionData.isOracle()) {
                text = "select x.* from (" + text + ") x where 1 = 1";
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
                if (fetchLimit > -1) {
                    text += " and rownum <= " + fetchLimit;
                    originalText += " and rownum <= " + fetchLimit;
                }
            } else if (connectionData.isDb2()) {
                resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
                if (fetchLimit > -1) {
                    text += " fetch first " + fetchLimit + " rows only";
                    originalText += " fetch first " + fetchLimit + " rows only";
                }
            } else if (connectionData.isMySql()) {
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
                if (fetchLimit > -1) {
                    text += " limit " + fetchLimit;
                    originalText += " limit " + fetchLimit;
                }
            } else {
                // ?
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }
            int columnCount;
            Statement statement = connectionData.getConnection().createStatement(resultSetType, ResultSet.CONCUR_UPDATABLE);
            final Statement[] statements = new Statement[] {statement};
            Runnable onCancel = new Runnable() {
                public void run() {
                    try {
                        statements[0].cancel();
                    } catch (Throwable t) {
                        // ignore
                    }
                }
            };
            WaitingDialog waitingDialog = new WaitingDialog(onCancel);
            waitingDialog.setText("Executing query");
            try {
                ResultSet resultSet;
                try {
                    resultSet = statement.executeQuery(text);
                } catch (SQLException e1) {
                    // try read-only and without modifications
                    statement = connectionData.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    statements[0] = statement;
                    resultSet = statement.executeQuery(originalText);
                }
                connectionData.setResultSet(resultSet);
                PLUGIN.audit(originalQuery);
                columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    columnIdentifiers.add(resultSet.getMetaData().getColumnName(i + 1));
                }
                while (waitingDialog.isVisible() && resultSet.next()) {
                    Vector row = new Vector(columnCount + 1);
                    for (int i = 0; i < columnCount; i++) {
                        Object object = resultSet.getObject(i + 1);
//                        System.out.println((i + 1) + " " + resultSet.getMetaData().getColumnName(i+1) + " - " + resultSet.getMetaData().getColumnTypeName(i+1) + " - " + resultSet.getMetaData().getColumnClassName(i+1) + " - \"" + object + "\"");
                        row.add(object);
                    }
                    dataVector.add(row);
                    waitingDialog.setText(dataVector.size() + " rows retrieved");
                }
                PLUGIN.audit("[" + dataVector.size() + " rows retrieved]");
            } finally {
                waitingDialog.hide();
            }
        } else {
            Vector row = new Vector(1);
            Statement statement = connectionData.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            PLUGIN.audit(text);
            int i = statement.executeUpdate(text);
            PLUGIN.audit("[" + i + " rows updated]");
            row.add(Integer.toString(i));
            dataVector.add(row);
            columnIdentifiers.add("Rows updated");
            connectionData.setResultSet(null);
        }
        ApplicationPanel.getInstance().setDataVector(dataVector, columnIdentifiers);
        handleActions();
    }
}
