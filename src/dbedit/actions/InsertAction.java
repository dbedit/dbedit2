package dbedit.actions;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InsertAction extends EditAction {

    protected InsertAction() {
        super("Insert", "add.png", null);
    }

    protected void fillTextArea(JTextArea textArea, List selectedRow, int column) {
    }

    protected void position(ResultSet resultSet) throws SQLException {
        resultSet.moveToInsertRow();
    }

    protected void updateSelectedRow(List selectedRow, int column, String text) {
    }

    protected void store(ResultSet resultSet) throws SQLException {
        resultSet.insertRow();
    }
}
