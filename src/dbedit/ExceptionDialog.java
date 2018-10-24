package dbedit;

import dbedit.actions.CustomAction;
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 15-dec-2005
 * Time: 12:48:13
 */
public class ExceptionDialog {

    public static void showException(Throwable t) {
        ApplicationPanel applicationPanel = null;
        String text = "";
        try {
            applicationPanel = ApplicationPanel.getInstance();
            text = applicationPanel.getText();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if ("Details".equals(Dialog.show(t.getClass().getName(), t.getMessage() != null ? t.getMessage() : "Error", Dialog.ERROR_MESSAGE, new Object[] {"Close", "Details"}, "Close"))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(out));
            JTextArea textArea = new JTextArea(new String(out.toByteArray()));
            ConnectionData connectionData = CustomAction.connectionData;
            if (connectionData != null) {
                textArea.append("\n");
                textArea.append(connectionData.getName());
                textArea.append("\n");
                textArea.append(connectionData.getUrl());
                textArea.append("\n");
                textArea.append(connectionData.getUser());
                textArea.append("\n");
                textArea.append(connectionData.getDriver());
                textArea.append("\n");
            }
            textArea.append("\n");
            textArea.append(text);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            if ("Copy".equals(Dialog.show(t.getClass().getName(), scrollPane, Dialog.ERROR_MESSAGE, new Object[] {"Close", "Copy"}, "Close"))) {
                try {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText().replace('\r', ' ')), null);
                } catch (Throwable t2) {
                    t2.printStackTrace();
                }
            }
        }

        //some tips
        String msg = null;
        if (t instanceof SQLException && t.getMessage().indexOf("Unsupported VM encoding") != -1) {
            msg = "This can be resolved by reinstalling your JRE and enabling \"Support for additional languages\" during setup.";
        } else if (t instanceof SQLException) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(out));
            String exception = new String(out.toByteArray());
            if (((SQLException) t).getErrorCode() == 907) {
                if (text.toLowerCase().indexOf("order") != -1 &&
                        (exception.indexOf("editingStopped") != -1 || exception.indexOf("deleteRow") != -1)) {
                    msg = "Updating a resultset mostly fails when the ORDER BY clause is used.\n" +
                            "Try the select without ORDER BY and sort the column afterwards.";
                }
            } else if (exception.indexOf("ResultSet is not updat") != -1) {
                if (text.toLowerCase().trim().endsWith("for fetch only")) {
                    msg = "Try your select without \"for fetch only\".";
                }
            } else {
                msg = PluginFactory.getPlugin().analyzeException(exception);
            }
        }
        if (msg != null) {
            Dialog.show("Tip", msg, Dialog.INFORMATION_MESSAGE, Dialog.DEFAULT_OPTION);
        }
    }
}
