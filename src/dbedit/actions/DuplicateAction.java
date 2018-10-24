package dbedit.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DuplicateAction extends EditAction {

    protected DuplicateAction() {
        super("Duplicate", "copy.png", null);
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
