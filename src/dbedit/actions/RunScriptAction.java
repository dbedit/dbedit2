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
package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.ExceptionDialog;
import dbedit.WaitingDialog;

import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunScriptAction extends ActionChangeAbstractAction {

    protected RunScriptAction() {
        super("Run script", "script.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        String text = ApplicationPanel.getInstance().getTextComponent().getText();
        getHistory().add(text);
        handleTextActions();
        // Search and capture all text that is followed by a semicolon,
        // zero or more whitespace characters [ \t\n\x0B\f\r],
        // end of the line and again zero or more whitespace characters
        // the regular expression is ran in dotall mode and multiline mode.
        Pattern pattern = Pattern.compile("(.*?);\\s*?$\\s*", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        int total = 0;
        while (matcher.find()) {
            total++;
        }
        matcher.reset();
        final Vector<Vector> dataVector = new Vector<Vector>();
        int count = 0;
        final Statement statement = getConnectionData().getConnection()
                .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        Runnable onCancel = new Runnable() {
            @Override
            public void run() {
                try {
                    statement.cancel();
                } catch (Throwable t) {
                    ExceptionDialog.hideException(t);
                }
            }
        };
        WaitingDialog waitingDialog = new WaitingDialog(onCancel);
        waitingDialog.setText(String.format("0/%d", total));
        try {
            while (waitingDialog.isVisible() && matcher.find()) {
                String sql = text.substring(matcher.start(1), matcher.end(1));
                Vector<String> row = new Vector<String>(1);
                PLUGIN.audit(sql);
                int i = statement.executeUpdate(sql);
                PLUGIN.audit(String.format("[%d rows updated]", i));
                row.add(Integer.toString(i));
                dataVector.add(row);
                waitingDialog.setText(String.format("%d/%d", count++, total));
            }
        } catch (Exception ex) {
            ApplicationPanel.getInstance().getTextComponent().setSelectionStart(matcher.start(1));
            ApplicationPanel.getInstance().getTextComponent().setSelectionEnd(matcher.end(1));
            ApplicationPanel.getInstance().getTextComponent().requestFocus();
            throw ex;
        } finally {
            waitingDialog.hide();
            getConnectionData().setResultSet(null);
            final Vector<String> columnIdentifiers = new Vector<String>(1);
            columnIdentifiers.add("Rows updated");
            setColumnTypes(new int[] {Types.INTEGER});
            setColumnTypeNames(new String[1]);
            ApplicationPanel.getInstance().setDataVector(dataVector, columnIdentifiers,
                    waitingDialog.getExecutionTime());
            handleActions();
        }
    }
}
