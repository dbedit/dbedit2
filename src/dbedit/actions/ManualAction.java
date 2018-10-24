package dbedit.actions;

import dbedit.Config;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ManualAction extends CustomAction {

    protected ManualAction() {
        super("Manual", null, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        openFile(Config.HOME_PAGE + "manual.html");
    }
}
