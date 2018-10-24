package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Config;
import dbedit.Dialog;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

public class FavoritesAction extends CustomAction {

    protected FavoritesAction() {
        super("Favorites", "favorites.png", KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK));
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        favorites();
    }

    public void favorites() throws ParserConfigurationException, IOException, TransformerException, SAXException {
        Map favorites = Config.getFavorites();
        final JList list = new JList(favorites.keySet().toArray());
        list.setVisibleRowCount(15);
        list.addMouseListener(this);
        list.addListSelectionListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Object value = Dialog.show("Favorites", new JScrollPane(list), Dialog.PLAIN_MESSAGE, new Object[] {"OK", "Cancel", "Add", "Delete"}, "OK");
        if ("OK".equals(value)) {
            if (!list.isSelectionEmpty()) {
                String s = (String) favorites.get(list.getSelectedValue());
                ApplicationPanel.getInstance().getTextArea().setText(s);
                ApplicationPanel.getInstance().getTextArea().setCaretPosition(s.length());
            }
        } else if ("Delete".equals(value)) {
            if (!list.isSelectionEmpty()) {
                if (Dialog.YES_OPTION == Dialog.show("Delete favorite", "Are you sure?", Dialog.WARNING_MESSAGE, Dialog.YES_NO_OPTION)) {
                    favorites.remove(list.getSelectedValue());
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

    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            Container container = (Container) e.getSource();
            while (!(container instanceof JOptionPane)) {
                container = container.getParent();
            }
            JOptionPane optionPane = (JOptionPane) container;
            Object value = optionPane.getInitialValue();
            if (value == null) {
                value = new Integer(JOptionPane.OK_OPTION);
            }
            optionPane.setValue(value);
            while (!(container instanceof JDialog)) {
                container = container.getParent();
            }
            container.setVisible(false);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }
}
