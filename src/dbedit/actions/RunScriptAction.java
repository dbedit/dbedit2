package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.WaitingDialog;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunScriptAction extends ActionChangeAbstractAction {

    protected RunScriptAction() {
        super("Run script", "script.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        String text = ApplicationPanel.getInstance().getTextArea().getText();
        history.add(text);
        handleTextActions();
        // Search and capture all text that is followed by a semicolon,
        // zero or more whitespace characters [ \t\n\x0B\f\r],
        // end of the line and again zero or more whitespace characters
        // the regular expression is ran in dotall mode and multiline mode.
        Pattern pattern = Pattern.compile("(.*?);\\s*?$\\s*", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        int total = 0;
        while (matcher.find()) total++;
        matcher.reset();
        final Vector dataVector = new Vector();
        int count = 0;
        final Statement statement = connectionData.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        Runnable onCancel = new Runnable() {
            public void run() {
                try {
                    statement.cancel();
                } catch (Throwable t) {
                    // ignore
                }
            }
        };
        WaitingDialog waitingDialog = new WaitingDialog(onCancel);
        waitingDialog.setText("0/" + total);
        try {
            while (waitingDialog.isVisible() && matcher.find()) {
                String sql = text.substring(matcher.start(1), matcher.end(1));
                Vector row = new Vector(1);
                PLUGIN.audit(sql);
                int i = statement.executeUpdate(sql);
                PLUGIN.audit("[" + i + " rows updated]");
                row.add(Integer.toString(i));
                dataVector.add(row);
                waitingDialog.setText(count++ + "/" + total);
            }
        } catch (Exception ex) {
            ApplicationPanel.getInstance().getTextArea().setSelectionStart(matcher.start(1));
            ApplicationPanel.getInstance().getTextArea().setSelectionEnd(matcher.end(1));
            ApplicationPanel.getInstance().getTextArea().requestFocus();
            throw ex;
        } finally {
            waitingDialog.hide();
            connectionData.setResultSet(null);
            final Vector columnIdentifiers = new Vector();
            columnIdentifiers.add("Rows updated");
            ApplicationPanel.getInstance().setDataVector(dataVector, columnIdentifiers);
            handleActions();
        }
    }
}
