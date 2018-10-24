package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CutAction extends CustomAction {

    protected CutAction() {
        super("Cut", "cut.png", KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        setEnabled(true);
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().getTextArea().cut();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
