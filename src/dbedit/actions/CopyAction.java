package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CopyAction extends CustomAction {

    protected CopyAction() {
        super("Copy", "copy.png", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        setEnabled(true);
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().getTextArea().copy();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
