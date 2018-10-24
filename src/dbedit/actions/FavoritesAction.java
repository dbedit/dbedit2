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
import dbedit.Dialog;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;

public class FavoritesAction extends CustomAction {

    protected FavoritesAction() {
        super("Favorites", "favorites.png", KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK));
        setEnabled(true);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        favorites();
    }

    public void favorites() throws ParserConfigurationException, IOException, TransformerException, SAXException {
        Map<String, String> favorites = Config.getFavorites();
        final JList list = new JList(favorites.keySet().toArray());
        list.addMouseListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Object value = Dialog.show("Favorites", new JScrollPane(list), Dialog.PLAIN_MESSAGE,
                new Object[] {"OK", "Cancel", "Add", "Delete"}, "OK");
        if ("OK".equals(value)) {
            if (!list.isSelectionEmpty()) {
                String name = (String) list.getSelectedValue();
                String s = favorites.get(name);
                ApplicationPanel.getInstance().setText(s);
            }
        } else if ("Delete".equals(value)) {
            if (!list.isSelectionEmpty()) {
                if (Dialog.YES_OPTION == Dialog.show("Delete favorite", "Are you sure?",
                        Dialog.WARNING_MESSAGE, Dialog.YES_NO_OPTION)) {
                    String name = (String) list.getSelectedValue();
                    favorites.remove(name);
                    Config.saveFavorites(favorites);
                }
            }
            favorites();
        } else if ("Add".equals(value)) {
            JComboBox comboBox = new JComboBox(favorites.keySet().toArray());
            comboBox.setEditable(true);
            comboBox.setSelectedIndex(-1);
            if (Dialog.OK_OPTION == Dialog.show("Name", comboBox, Dialog.QUESTION_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
                String name = (String) comboBox.getSelectedItem();
                if (name != null && !"".equals(name.trim())) {
                    favorites.put(name, ApplicationPanel.getInstance().getText());
                    Config.saveFavorites(favorites);
                }
            }
            favorites();
        }
    }
}
