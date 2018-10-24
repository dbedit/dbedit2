package dbedit.actions;

import java.awt.event.ActionEvent;

public class RollbackAction extends CustomAction {

    protected RollbackAction() {
        super("Rollback", "rollback.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        getConnectionData().getConnection().rollback();
    }
}
