package dbedit.actions;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class GroupAction extends CustomAction {

    private JPopupMenu popupMenu;

    protected GroupAction(String name) {
        super(name, "arrow.png", null);
        popupMenu = new JPopupMenu();
    }

    protected void addAction(Action action) {
        popupMenu.add(action).setMargin(new Insets(2, -14, 2, 2));
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }

    public void actionPerformed(final ActionEvent e) {
        popupMenu.show((Component) e.getSource(), 0, ((Component) e.getSource()).getHeight());
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                JToggleButton toggleButton = (JToggleButton) popupMenu.getInvoker();
                toggleButton.setSelected(false);
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
        });
    }
}
