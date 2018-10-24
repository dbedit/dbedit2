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

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;

public class FileOpenAction extends CustomAction {

    protected FileOpenAction() {
        super("Open File", "fileopen.png", null);
        setEnabled(true);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        File file = FileIO.openFile();
        if (file != null) {
            Context.getInstance().setOpenedFile(file);
            JEditorPane textComponent = ApplicationPanel.getInstance().getTextComponent();
            resetEditorPaneToAvoidMemoryLeak(textComponent);
            TransferHandler transferHandler = textComponent.getTransferHandler();
            StringSelection stringSelection = new StringSelection(new String(FileIO.readFile(file)));
            transferHandler.importData(new TransferHandler.TransferSupport(textComponent, stringSelection));
        }
    }

    private void resetEditorPaneToAvoidMemoryLeak(JEditorPane textComponent) {
        textComponent.setContentType("text/plain");
        textComponent.setContentType("text/sql");
    }
}
