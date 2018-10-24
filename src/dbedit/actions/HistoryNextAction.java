package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class HistoryNextAction extends ActionChangeAbstractAction {

    protected HistoryNextAction() {
        super("History - next", "next.png", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK));
    }

    public void actionPerformed(final ActionEvent e) {
        if (history.hasNext()) {
            ApplicationPanel.getInstance().getTextArea().setText(history.next());
        }
        handleTextActions();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }
}
