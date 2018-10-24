package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;

public class LobImportAction extends LobAbstractAction {

    protected LobImportAction() {
        super("Import from file", "import.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        if (JFileChooser.APPROVE_OPTION == FILE_CHOOSER.showOpenDialog(ApplicationPanel.getInstance())) {
            FileInputStream in = new FileInputStream(FILE_CHOOSER.getSelectedFile());
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();
            ApplicationPanel.getInstance().setTableValue(b);
            editingStopped(null);
        }
    }
}
