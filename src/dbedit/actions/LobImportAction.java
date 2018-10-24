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
import dbedit.Config;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;

public class LobImportAction extends LobAbstractAction {

    protected LobImportAction() {
        super("Import", "import.png");
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JFileChooser fileChooser = getFileChooser();
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(ApplicationPanel.getInstance())) {
            Config.saveLastUsedDir(fileChooser.getCurrentDirectory().getCanonicalPath());
            FileInputStream in = new FileInputStream(fileChooser.getSelectedFile());
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();
            ApplicationPanel.getInstance().setTableValue(b);
            editingStopped(null);
        }
    }
}
