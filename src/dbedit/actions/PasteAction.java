package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PasteAction extends CustomAction {

    protected PasteAction() {
        super("Paste", "paste.png", KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        setEnabled(true);
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().getTextArea().paste();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
