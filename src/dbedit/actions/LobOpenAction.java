package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class LobOpenAction extends LobAbstractAction {

    protected LobOpenAction() {
        super("Open in text editor (Double Left Click)", "text.png", null);
    }

    public LobOpenAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        File tempFile = getTempFile();
        tempFile.deleteOnExit();
        exportLob(tempFile);
        openURL(tempFile.toString());
    }

    protected File getTempFile() throws IOException {
        return File.createTempFile("lob", ".txt");
    }

    public void mouseClicked(MouseEvent e) {
        if (MouseEvent.BUTTON1 == e.getButton() && e.getClickCount() == 2
                && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn())) {
            actionPerformed(null);
        }
    }
}
