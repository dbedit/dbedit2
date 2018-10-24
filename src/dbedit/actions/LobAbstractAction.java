/*
 * DBEdit 2
 * Copyright (C) 2006-2009 Jef Van Den Ouweland
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

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

public abstract class LobAbstractAction extends CustomAction {

    protected LobAbstractAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void exportLob(File file) throws IOException, SQLException {
        Object lob = ApplicationPanel.getInstance().getTableValue();
        byte[] bytes;
        if (lob instanceof Blob) {
            bytes = ((Blob) lob).getBytes(1, (int) ((Blob) lob).length());
        } else if (lob instanceof Clob) {
            bytes = ((Clob) lob).getSubString(1, (int) ((Clob) lob).length()).getBytes();
        } else if (lob instanceof byte[]) {
            bytes = (byte[]) lob;
        } else if (lob != null) {
            throw new UnsupportedOperationException("Unsupported type");
        } else {
            return;
        }
        getFileChooser().setSelectedFile(file);
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }
}
