package dbedit;

import dbedit.actions.Actions;
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class ApplicationMenuBar extends JMenuBar {

    public ApplicationMenuBar() {
        UIManager.put("Menu.checkIcon", "No icon");
        UIManager.put("MenuItem.checkIcon", "No icon");

        JMenu menu;
        JMenu subMenu;

        menu = new JMenu("Connection");
        add(menu);
        menu.add(Actions.CONNECT);
        menu.add(Actions.DISCONNECT);
        menu.add(new JSeparator());
        menu.add(Actions.COMMIT);
        menu.add(Actions.ROLLBACK);

        menu = new JMenu("Editor");
        add(menu);
        menu.add(Actions.UNDO);
        menu.add(Actions.REDO);
        menu.add(new JSeparator());
        menu.add(Actions.CUT);
        menu.add(Actions.COPY);
        menu.add(Actions.PASTE);
        menu.add(new JSeparator());
        menu.add(Actions.RUN);
        menu.add(Actions.RUN_SCRIPT);
        menu.add(Actions.SCHEMA_BROWSER);
        menu.add(new JSeparator());
        menu.add(Actions.FAVORITES);
        menu.add(Actions.HISTORY_PREVIOUS);
        menu.add(Actions.HISTORY_NEXT);

        menu = new JMenu("Grid");
        add(menu);
        menu.add(Actions.INSERT);
        menu.add(Actions.DELETE);
        menu.add(Actions.EDIT);
        menu.add(Actions.DUPLICATE);
        menu.add(new JSeparator());
        subMenu = new JMenu("Lob");
        menu.add(subMenu);
        if (Config.IS_OS_WINDOWS) {
            subMenu.add(Actions.LOB_OPEN);
            subMenu.add(Actions.LOB_OPEN_WITH);
        }
        subMenu.add(Actions.LOB_IMPORT);
        subMenu.add(Actions.LOB_EXPORT);
        subMenu.add(Actions.LOB_COPY);
        subMenu.add(Actions.LOB_PASTE);
        menu.add(new JSeparator());
        subMenu = new JMenu("Export");
        menu.add(subMenu);
        subMenu.add(Actions.EXPORT_EXCEL);
        subMenu.add(Actions.EXPORT_FLAT_FILE);
        subMenu.add(Actions.EXPORT_INSERTS);

        menu = new JMenu("Settings");
        add(menu);
        menu.add(Actions.FETCH_LIMIT);

        menu = new JMenu("Help");
        add(menu);
        menu.add(Actions.MANUAL);
        menu.add(Actions.SELECT_FROM);
        menu.add(new JSeparator());
        menu.add(Actions.ABOUT);

        PluginFactory.getPlugin().checkForUpdate(this);

        // Restrict menubar from collapsing when sizing down the frame too much
        setMinimumSize(getPreferredSize());

        setMnemonicsAndIcons(this);
    }

    private void setMnemonicsAndIcons(MenuElement menuElement) {
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
                setMnemonicsAndIcons(((JMenu) item).getPopupMenu());
                if (menuElement != this) {
                    // dummy icons for sub menus
                    item.setIcon(new ImageIcon(ApplicationMenuBar.class.getResource("/icons/empty.png")));
                }
            }
        }
    }
}
