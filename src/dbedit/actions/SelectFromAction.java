package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SelectFromAction extends CustomAction {

    protected SelectFromAction() {
        super("Insert \"select * from \"", "empty.png", KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        setEnabled(true);
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().setText("select * from ");
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
