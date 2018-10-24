package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class HistoryPreviousAction extends ActionChangeAbstractAction {

    protected HistoryPreviousAction() {
        super("History - previous", "back.png", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK));
    }

    public void actionPerformed(final ActionEvent e) {
        if (history.hasPrevious()) {
            ApplicationPanel.getInstance().getTextArea().setText((String) history.previous());
        }
        handleTextActions();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
