/**
 * DBEdit 2
 * Copyright (C) 2006-2008 Jef Van Den Ouweland
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
import dbedit.ExceptionDialog;
import dbedit.WaitingDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;

public class RunAction extends ActionChangeAbstractAction {

    protected RunAction() {
        super("Run", "run.png", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        String text = ApplicationPanel.getInstance().getText();
        String originalText = text;
        getHistory().add(text);
        handleTextActions();
        Vector<String> columnIdentifiers = new Vector<String>();
        Vector<Vector> dataVector = new Vector<Vector>();
        int[] columnTypes;
        String[] columnTypeNames;
        int resultSetType;
        String originalQuery = text;
        boolean query = text.trim().toLowerCase().startsWith("select")
                || text.trim().toLowerCase().startsWith("with");
        if (query) {
            if (getConnectionData().isOracle()) {
                // http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/resltset.htm#CIHEJHJI
                text = "select x.* from (" + text + ") x where 1 = 1";
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (getConnectionData().isDb2()) {
                resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
            } else if (getConnectionData().isMySql()) {
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (getConnectionData().isHSQLDB()) {
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else {
                // ?
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }
        } else {
            resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        }
        Statement statement = getConnectionData().getConnection()
                .createStatement(resultSetType, ResultSet.CONCUR_UPDATABLE);
        statement.setMaxRows(getFetchLimit());
        final Statement[] statements = new Statement[] {statement};
        Runnable onCancel = new Runnable() {
            public void run() {
                try {
                    statements[0].cancel();
                } catch (Throwable t) {
                    ExceptionDialog.hideException(t);
                }
            }
        };
        WaitingDialog waitingDialog = new WaitingDialog(onCancel);
        waitingDialog.setText("Executing statement");
        try {
            boolean hasResultSet;
            try {
                hasResultSet = statement.execute(text);
            } catch (SQLException e1) {
                // try read-only and without modifications
                statement = getConnectionData().getConnection()
                        .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                statements[0] = statement;
                hasResultSet = statement.execute(originalText);
            }
            ResultSet resultSet = statement.getResultSet();
            getConnectionData().setResultSet(resultSet);
            PLUGIN.audit(originalQuery);
            if (hasResultSet) {

                int columnCount = resultSet.getMetaData().getColumnCount();
                columnTypes = new int[columnCount];
                columnTypeNames = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columnIdentifiers.add(resultSet.getMetaData().getColumnName(i + 1));
                    columnTypes[i] = resultSet.getMetaData().getColumnType(i + 1);
                    columnTypeNames[i] = resultSet.getMetaData().getColumnTypeName(i + 1);
                }
                while (waitingDialog.isVisible() && resultSet.next()) {
                    Vector<Object> row = new Vector<Object>(columnCount + 1);
                    for (int i = 0; i < columnCount; i++) {
                        Object object = resultSet.getObject(i + 1);
//                        System.out.println((i + 1) + " "
//                                + resultSet.getMetaData().getColumnName(i+1) + " - "
//                                + resultSet.getMetaData().getColumnType(i+1) + " - "
//                                + resultSet.getMetaData().getColumnTypeName(i+1) + " - "
//                                + resultSet.getMetaData().getColumnClassName(i+1) + " - \"" + object + "\"");
                        row.add(object);
                    }
                    dataVector.add(row);
                    waitingDialog.setText(dataVector.size() + " rows retrieved");
                }
                PLUGIN.audit("[" + dataVector.size() + " rows retrieved]");
            } else {
                int updateCount = statement.getUpdateCount();
                if (updateCount != -1) {
                    Vector<Object> row = new Vector<Object>(1);
                    PLUGIN.audit("[" + updateCount + " rows updated]");
                    row.add(Integer.toString(updateCount));
                    dataVector.add(row);
                    columnIdentifiers.add("Rows updated");
                    columnTypes = new int[] {Types.INTEGER};
                    columnTypeNames = new String[1];
                } else {
                    columnIdentifiers.add("Statement executed");
                    columnTypes = new int[] {Types.INTEGER};
                    columnTypeNames = new String[1];
                }
            }
        } finally {
            waitingDialog.hide();
        }
        setColumnTypes(columnTypes);
        setColumnTypeNames(columnTypeNames);
        ApplicationPanel.getInstance().setDataVector(dataVector, columnIdentifiers, waitingDialog.getExecutionTime());
        handleActions();
    }
}
