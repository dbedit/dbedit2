package dbedit.actions;

import java.awt.event.ActionEvent;

public class CommitAction extends CustomAction {

    protected CommitAction() {
        super("Commit", "commit.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        PLUGIN.audit("Commit");
        getConnectionData().getConnection().commit();
    }
}
