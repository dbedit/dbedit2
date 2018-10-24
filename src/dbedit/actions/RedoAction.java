package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class RedoAction extends CustomAction {

    protected RedoAction() {
        super("Redo", "redo.png", KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().undoManager.redo();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
