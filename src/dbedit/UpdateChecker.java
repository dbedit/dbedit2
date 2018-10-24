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
package dbedit;

import dbedit.actions.Actions;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateChecker implements Runnable {

    private JMenuBar menuBar;

    public UpdateChecker(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public void check() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            System.setProperty("java.net.useSystemProxies", "true");
            DateFormat format = new SimpleDateFormat("(dd/MM/yyyy)");
            String localVersion = Config.getVersion();
            String latestVersion = new BufferedReader(new InputStreamReader(
                    new URL(Config.HOME_PAGE + "changes.txt").openStream())).readLine();
            int index = localVersion.indexOf('(');
            if (index != -1) {
                Date localVersionDate = format.parse(localVersion.substring(index));
                Date latestVersionDate = format.parse(latestVersion.substring(latestVersion.indexOf('(')));
                if (localVersionDate.compareTo(latestVersionDate) < 0) {
                    JMenu menu;
                    menuBar.add(Box.createHorizontalGlue());
                    menu = new JMenu("Update");
                    menuBar.add(menu);
                    menu.setForeground(Color.RED);
                    menu.add(Actions.UPDATE);
                }
            }
        } catch (Throwable t) {
            ExceptionDialog.hideException(t);
        }
    }
}
