/*
 * DBEdit 2
 * Copyright (C) 2006-2012 Jef Van Den Ouweland
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
import dbedit.Context;
import dbedit.FileIO;

import java.awt.event.ActionEvent;
import java.io.File;

public class FileSaveAction extends CustomAction {

    protected FileSaveAction() {
        super("Save File", "filesave.png", null);
        setEnabled(true);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        File openedFile = Context.getInstance().getOpenedFile();
        FileIO.saveFile(openedFile == null ? "" : openedFile.toString(),
                ApplicationPanel.getInstance().getText().getBytes());
    }
}
