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

import dbedit.Config;

import java.awt.event.ActionEvent;

public class UpdateAction extends CustomAction {

    protected UpdateAction() {
        super("Download latest version", "empty.png", null);
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        openURL(Config.HOME_PAGE + "download.html");
    }
}
