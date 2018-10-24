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

import dbedit.ApplicationPanel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

public abstract class LobAbstractAction extends CustomAction {

    protected LobAbstractAction(String name, String icon) {
        super(name, icon, null);
    }

    protected byte[] getLob() throws SQLException {
        Object lob = ApplicationPanel.getInstance().getTableValue();
        if (lob instanceof Blob) {
            return ((Blob) lob).getBytes(1, (int) ((Blob) lob).length());
        } else if (lob instanceof Clob) {
            return ((Clob) lob).getSubString(1, (int) ((Clob) lob).length()).getBytes();
        } else if (lob instanceof byte[]) {
            return (byte[]) lob;
        } else if (lob != null) {
            throw new UnsupportedOperationException("Unsupported type");
        } else {
            return null;
        }
    }

    protected void exportLob(File file, byte[] bytes) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }
}
