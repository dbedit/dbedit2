package dbedit.actions;

import dbedit.ApplicationPanel;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class LobOpenWithAction extends LobOpenAction {

    protected LobOpenWithAction() {
        super("Open in program of choice (Double Right Click)", "unknown.png", null);
    }

    protected File getTempFile() throws IOException {
        return File.createTempFile("lob", "");
    }

    public void mouseClicked(MouseEvent e) {
        if (MouseEvent.BUTTON1 != e.getButton() && e.getClickCount() == 2
                && isLob(ApplicationPanel.getInstance().getTable().getSelectedColumn())) {
            actionPerformed(null);
        }
    }
}
