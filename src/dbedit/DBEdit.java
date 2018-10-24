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
package dbedit;

import dbedit.actions.Actions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DBEdit extends JFrame {

    public static final String APPLICATION_NAME = "DBEdit 2";

    public static void main(String[] args) throws IllegalAccessException, UnsupportedLookAndFeelException,
                                                  InstantiationException, ClassNotFoundException {
        if (Config.IS_OS_WINDOWS) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        UIManager.put("TextArea.font", new Font(Font.MONOSPACED, Font.PLAIN, 12));

        new DBEdit();
    }

    public DBEdit() {
        try {
            setTitle(APPLICATION_NAME);
            setIconImage(new ImageIcon(DBEdit.class.getResource("/icons/icon.gif")).getImage());
            getContentPane().add(ApplicationPanel.getInstance());
            setJMenuBar(new ApplicationMenuBar());
            double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            setSize((int) (screenWidth * .8), (int) (screenHeight * .8));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setVisible(true);
            Actions.CONNECT.actionPerformed(new ActionEvent(this, 0, null));
        } catch (Throwable t) {
            ExceptionDialog.showException(t);
            System.exit(1);
        }
    }
}
