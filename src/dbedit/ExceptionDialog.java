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
package dbedit;

import dbedit.plugin.PluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
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
            ConnectionData connectionData = Context.getInstance().getConnectionData();
            if (connectionData != null) {
                textArea.append("\n");
                textArea.append(connectionData.getUrl());
                textArea.append("\n");
                textArea.append(connectionData.getDriver().getClass().getName());
            }
            textArea.append("\n");
            textArea.append(text);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
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
        showTip(t, text);
    }

    private static void showTip(Throwable t, String text) {
        StringBuilder msg = new StringBuilder();
        if (t instanceof SQLException) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(out));
            String exception = new String(out.toByteArray());
            if (t.getMessage().contains("Unsupported VM encoding")
                    || t.getCause() instanceof UnsupportedEncodingException) {
                msg.append("This can be resolved by reinstalling your JRE and enabling ");
                msg.append("\"Support for additional languages\" during setup.");
            } else if (((SQLException) t).getErrorCode() == 907) {
                if (text.toLowerCase().contains("order")
                        && (exception.contains("updateRow")
                         || exception.contains("insertRow")
                         || exception.contains("deleteRow"))) {
                    msg.append("Updating a resultset mostly fails when the ORDER BY clause is used.\n");
                    msg.append("Try the select without ORDER BY and sort the column afterwards.");
                }
            } else if (Context.getInstance().getConnectionData() != null
                    && Context.getInstance().getConnectionData().isIbm()) {
                if (t.getMessage().contains("Invalid operation: result set is closed.")) {
                    int length = Context.getInstance().getColumnTypes().length;
                    for (int i = 0; i < length; i++) {
                        if (ResultSetTable.isLob(i)) {
                            msg.append("The IBM JDBC driver doesn't support modifying rows with BLOB's or CLOB's ");
                            msg.append("in the resultset yet.\n");
                            msg.append("Please execute the update statement manually ");
                            msg.append("or try the DataDirect DB2 driver.\n");
                            msg.append("http://www.datadirect.com/products/jdbc/");
                            break;
                        }
                    }
                }
            } else {
                msg.append(PluginFactory.getPlugin().analyzeException(exception));
            }
        } else if (t instanceof OutOfMemoryError) {
            msg.append("DBEdit has a memory limit of 512 MB.\n");
            if (Config.IS_OS_WINDOWS) {
                msg.append("If you really want to increase it, create a file called ");
                try {
                    msg.append(new File("DBEdit.l4j.ini").getCanonicalPath());
                } catch (IOException e) {
                    msg.append("DBEdit.l4j.ini");
                }
                msg.append(".\n");
                msg.append("You can specify JVM options in that file.\n");
                msg.append("Enter -Xmx1g for 1 GB, -Xmx2g for 2 GB ...");
            } else {
                msg.append("If you really want to increase it, edit the startup script ");
                try {
                    msg.append(new File("dbedit").getCanonicalPath());
                } catch (IOException e) {
                    msg.append("dbedit");
                }
                msg.append(".\n");
                msg.append("Search for -Xmx512m.\n");
                msg.append("-Xmx512m means 512 MB of memory.\n");
                msg.append("Change it to -Xmx1g for 1 GB, -Xmx2g for 2 GB ...");
            }

        }
        if (msg.length() > 0) {
            Dialog.show("Tip", msg, Dialog.INFORMATION_MESSAGE, Dialog.DEFAULT_OPTION);
        }
    }

    public static void hideException(Throwable t) {
        t.printStackTrace();
    }

    public static void ignoreException(Throwable t) {
    }
}
