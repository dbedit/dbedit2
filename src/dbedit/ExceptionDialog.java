/*
 * DBEdit 2
 * Copyright (C) 2006-2010 Jef Van Den Ouweland
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

import dbedit.actions.CustomAction;
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

public final class ExceptionDialog {

    private ExceptionDialog() {
    }

    public static void showException(Throwable t) {
        ApplicationPanel applicationPanel;
        String text = "";
        try {
            applicationPanel = ApplicationPanel.getInstance();
            text = applicationPanel.getText();
        } catch (Throwable e) {
            ExceptionDialog.hideException(e);
        }
        if ("Details".equals(Dialog.show(t.getClass().getName(), t.getMessage() != null ? t.getMessage() : "Error",
                Dialog.ERROR_MESSAGE, new Object[] {"Close", "Details"}, "Close"))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(out));
            JTextArea textArea = new JTextArea(new String(out.toByteArray()));
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            textArea.append("\n");
            textArea.append("DBEdit ");
            try {
                textArea.append(Config.getVersion());
            } catch (IOException e) {
                hideException(e);
            }
            textArea.append("\n");
            textArea.append(System.getProperty("os.name"));
            textArea.append(" ");
            textArea.append(System.getProperty("os.version"));
            textArea.append(" ");
            textArea.append(System.getProperty("sun.os.patch.level"));
            textArea.append(" ");
            textArea.append(System.getProperty("os.arch"));
            textArea.append("\n");
            textArea.append(System.getProperty("java.runtime.name"));
            textArea.append(" ");
            textArea.append(System.getProperty("java.runtime.version"));
            ConnectionData connectionData = CustomAction.getConnectionData();
            if (connectionData != null) {
                textArea.append("\n");
                textArea.append(connectionData.getUrl());
                textArea.append("\n");
                textArea.append(connectionData.getDriver());
                textArea.append("\n");
            }
            textArea.append(text);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            if ("Copy to clipboard".equals(Dialog.show(t.getClass().getName(), scrollPane, Dialog.ERROR_MESSAGE,
                    new Object[] {"Close", "Copy to clipboard"}, "Close"))) {
                try {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                            new StringSelection(textArea.getText().replace('\r', ' ')), null);
                } catch (Throwable t2) {
                    ExceptionDialog.hideException(t2);
                }
            }
        }

        //some tips
        String msg = null;
        if (t instanceof SQLException) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(out));
            String exception = new String(out.toByteArray());
            if (t.getMessage().contains("Unsupported VM encoding")) {
                msg = "This can be resolved by reinstalling your JRE and enabling "
                        + "\"Support for additional languages\" during setup.";
            } else if (((SQLException) t).getErrorCode() == 907) {
                if (text.toLowerCase().contains("order")
                        && (exception.contains("editingStopped") || exception.contains("deleteRow"))) {
                    msg = "Updating a resultset mostly fails when the ORDER BY clause is used.\n"
                            + "Try the select without ORDER BY and sort the column afterwards.";
                }
            } else if (exception.contains("ResultSet is not updat")) {
                if (text.toLowerCase().trim().endsWith("for fetch only")) {
                    msg = "Try your select without \"for fetch only\".";
                }
            } else if (CustomAction.getConnectionData() != null && CustomAction.getConnectionData().isIbm()) {
                if (t.getMessage().contains("Invalid operation: result set is closed.")) {
                    int length = CustomAction.getColumnTypes().length;
                    for (int i = 0; i < length; i++) {
                        if (CustomAction.isLob(i)) {
                            msg = "The IBM JDBC driver doesn't support modifying rows with BLOB's or CLOB's "
                                    + "in the resultset yet.\n"
                                    + "Please execute the update statement manually or try the DataDirect DB2 driver.\n"
                                    + "http://www.datadirect.com/products/jdbc/";
                            break;
                        }
                    }
                }
            } else {
                msg = PluginFactory.getPlugin().analyzeException(exception);
            }
        }
        if (msg != null) {
            Dialog.show("Tip", msg, Dialog.INFORMATION_MESSAGE, Dialog.DEFAULT_OPTION);
        }
    }

    public static void hideException(Throwable t) {
        t.printStackTrace();
    }

    public static void ignoreException(Throwable t) {
    }
}
