package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class UndoAction extends CustomAction {

    protected UndoAction() {
        super("Undo", "undo.png", KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().getUndoManager().undo();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
