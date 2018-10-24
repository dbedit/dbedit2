package dbedit;

import dbedit.actions.Actions;
import dbedit.plugin.PluginFactory;

import javax.swing.*;
import java.awt.*;

public class ApplicationMenuBar extends JMenuBar {

    public ApplicationMenuBar() {
        JMenu menu;
        JMenu subMenu;

        add(menu = new JMenu("Connection"));
        add(menu, Actions.CONNECT);
        add(menu, Actions.DISCONNECT);
        menu.addSeparator();
        add(menu, Actions.COMMIT);
        add(menu, Actions.ROLLBACK);

        add(menu = new JMenu("Editor"));
        add(menu, Actions.UNDO);
        add(menu, Actions.REDO);
        menu.addSeparator();
        add(menu, Actions.CUT);
        add(menu, Actions.COPY);
        add(menu, Actions.PASTE);
        menu.addSeparator();
        add(menu, Actions.RUN);
        add(menu, Actions.RUN_SCRIPT);
        add(menu, Actions.SCHEMA_BROWSER);
        menu.addSeparator();
        add(menu, Actions.FAVORITES);
        add(menu, Actions.HISTORY_PREVIOUS);
        add(menu, Actions.HISTORY_NEXT);

        add(menu = new JMenu("Grid"));
        add(menu, Actions.INSERT);
        add(menu, Actions.DELETE);
        add(menu, Actions.EDIT);
        add(menu, Actions.DUPLICATE);
        menu.addSeparator();
        add(menu, subMenu = new JMenu("Lob"));
        add(subMenu, Actions.LOB_OPEN);
        add(subMenu, Actions.LOB_OPEN_WITH);
        add(subMenu, Actions.LOB_IMPORT);
        add(subMenu, Actions.LOB_EXPORT);
        add(subMenu, Actions.LOB_COPY);
        add(subMenu, Actions.LOB_PASTE);
        menu.addSeparator();
        add(menu, subMenu = new JMenu("Export"));
        add(subMenu, Actions.EXPORT_EXCEL);
        add(subMenu, Actions.EXPORT_FLAT_FILE);
        add(subMenu, Actions.EXPORT_INSERTS);
        add(menu);

        add(menu = new JMenu("Settings"));
        add(menu, Actions.FETCH_LIMIT);

        add(menu = new JMenu("Help"));
        add(menu, Actions.MANUAL);
        add(menu, Actions.SELECT_FROM);
        menu.addSeparator();
        add(menu, Actions.ABOUT);

        PluginFactory.getPlugin().checkForUpdate(this);

        // Restrict menubar from collapsing when sizing down the frame too much
        setMinimumSize(getPreferredSize());
    }

    public void add(JMenu menu, Action action) {
        realignMenuItem(menu.add(action));
    }

    private void add(JMenu menu, JMenu subMenu) {
        realignMenuItem(menu.add(subMenu));
    }

    private void realignMenuItem(JMenuItem menuItem) {
        if (menuItem.getIcon() != null) {
            menuItem.setMargin(new Insets(2, -14, 2, 2));
        } else {
            menuItem.setMargin(new Insets(2, 6, 2, 2));
        }
    }
}
