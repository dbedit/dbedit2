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
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class ApplicationMenuBar extends JMenuBar {

    public ApplicationMenuBar() {

        JMenu menu;
        JMenu subMenu;

        menu = new JMenu("Connection");
        add(menu);
        menu.add(Actions.CONNECT);
        menu.add(Actions.DISCONNECT);
        menu.addSeparator();
        menu.add(Actions.COMMIT);
        menu.add(Actions.ROLLBACK);

        menu = new JMenu("Editor");
        add(menu);
        if (Actions.UNDU_REDO_ENABLED) {
            menu.add(Actions.UNDO);
            menu.add(Actions.REDO);
            menu.addSeparator();
        }
        if (Actions.CUT_COPY_PASTE_ENABLED) {
            menu.add(Actions.CUT);
            menu.add(Actions.COPY);
            menu.add(Actions.PASTE);
            menu.addSeparator();
        }
        menu.add(Actions.FILE_OPEN);
        menu.add(Actions.FILE_SAVE);
        menu.add(Actions.FAVORITES);
        menu.add(Actions.HISTORY_PREVIOUS);
        menu.add(Actions.HISTORY_NEXT);
        menu.addSeparator();
        menu.add(Actions.FORMAT_SQL);
        menu.addSeparator();
        menu.add(Actions.RUN);
        menu.add(Actions.RUN_SCRIPT);
        menu.add(Actions.SCHEMA_BROWSER);

        menu = new JMenu("Grid");
        add(menu);
        menu.add(Actions.INSERT);
        menu.add(Actions.DELETE);
        menu.add(Actions.EDIT);
        menu.add(Actions.DUPLICATE);
        menu.addSeparator();
        subMenu = new JMenu("Lob");
        menu.add(subMenu);
        subMenu.add(Actions.LOB_EXPORT);
        subMenu.add(Actions.LOB_IMPORT);
        subMenu.add(Actions.LOB_COPY);
        subMenu.add(Actions.LOB_PASTE);
        menu.addSeparator();
        subMenu = new JMenu("Export");
        menu.add(subMenu);
        subMenu.add(Actions.EXPORT_EXCEL);
        subMenu.add(Actions.EXPORT_PDF);
        subMenu.add(Actions.EXPORT_FLAT_FILE);
        subMenu.add(Actions.EXPORT_INSERTS);

        menu = new JMenu("Settings");
        add(menu);
        menu.add(Actions.FETCH_LIMIT);

        menu = new JMenu("Help");
        add(menu);
        menu.add(Actions.MANUAL);
        menu.add(Actions.SELECT_FROM);
        menu.addSeparator();
        menu.add(Actions.ABOUT);

        PluginFactory.getPlugin().checkForUpdate(this);

        setMnemonics(this);
    }

    private void setMnemonics(MenuElement menuElement) {
        Set<Character> used = new HashSet<Character>();
        MenuElement[] subElements = menuElement.getSubElements();
        for (MenuElement subElement : subElements) {
            AbstractButton item = (AbstractButton) subElement;
            char[] chars = item.getText().toCharArray();
            for (char aChar : chars) {
                if (used.add(aChar)) {
                    item.setMnemonic(aChar);
                    break;
                }
            }
            if (item instanceof JMenu) {
                setMnemonics(((JMenu) item).getPopupMenu());
            }
        }
    }
}
