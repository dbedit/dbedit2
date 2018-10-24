/**
 * DBEdit 2
 * Copyright (C) 2006-2008 Jef Van Den Ouweland
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class LobOpenAction extends LobAbstractAction {

    protected LobOpenAction() {
        super("Open in text editor (Double Left Click)", "text.png", null);
    }

    public LobOpenAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        File tempFile = getTempFile();
        tempFile.deleteOnExit();
        exportLob(tempFile);
        openURL(tempFile.toString());
    }

    protected File getTempFile() throws IOException {
        return File.createTempFile("lob", ".txt");
    }

    public void mouseClicked(MouseEvent e) {
        if (MouseEvent.BUTTON1 == e.getButton() && e.getClickCount() == 2
                && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn())) {
            actionPerformed(null);
        }
    }
}
