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

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class LobOpenWithAction extends LobOpenAction {

    protected LobOpenWithAction() {
        super("Open in program of choice (Double Right Click)", "unknown.png", null);
    }

    @Override
    protected File getTempFile() throws IOException {
        return File.createTempFile("lob", "");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (MouseEvent.BUTTON1 != e.getButton() && e.getClickCount() == 2
                && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn())) {
            actionPerformed(null);
        }
    }
}
