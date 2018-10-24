package dbedit.actions;

import dbedit.Config;

import java.awt.event.ActionEvent;

public class UpdateAction extends CustomAction {

    protected UpdateAction() {
        super("Download latest version", null, null);
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        openFile(Config.HOME_PAGE + "download.html");
    }
}
