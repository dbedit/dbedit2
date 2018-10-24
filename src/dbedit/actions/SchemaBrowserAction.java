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
import dbedit.SchemaBrowser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class SchemaBrowserAction extends CustomAction {

    protected SchemaBrowserAction() {
        super("Schema Browser", "schema.png", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().showHideObjectChooser();
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            selectFromObjectChooser((SchemaBrowser) e.getSource());
            ApplicationPanel.getInstance().showHideObjectChooser();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() instanceof SchemaBrowser) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isAltDown()) {
                selectFromObjectChooser((SchemaBrowser) e.getSource());
                ApplicationPanel.getInstance().showHideObjectChooser();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                ApplicationPanel.getInstance().showHideObjectChooser();
            }
        }
    }

    private void selectFromObjectChooser(SchemaBrowser schemaBrowser) {
        String s = Arrays.asList(schemaBrowser.getSelectedItems()).toString();
        s = s.substring(1, s.length() - 1);
        ApplicationPanel.getInstance().replaceText(s);
    }
}
