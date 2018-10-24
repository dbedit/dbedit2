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

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InsertAction extends EditAction {

    protected InsertAction() {
        super("Insert", "add.png");
    }

    @Override
    protected void fillTextArea(JTextArea textArea, List selectedRow, int column) {
    }

    @Override
    protected boolean change(String text, String originalText) {
        return text != null && !"".equals(text);
    }

    @Override
    protected void position(ResultSet resultSet) throws SQLException {
        resultSet.moveToInsertRow();
    }

    @Override
    protected void updateSelectedRow(List selectedRow, int column, String text) {
    }

    @Override
    protected void store(ResultSet resultSet) throws SQLException {
        resultSet.insertRow();
    }
}
