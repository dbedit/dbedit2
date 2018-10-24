package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Config;
import dbedit.DBEdit;

import javax.swing.event.AncestorEvent;
import java.awt.event.ActionEvent;

public class DisconnectAction extends ActionChangeAbstractAction {

    protected DisconnectAction() {
        super("Disconnect", "disconnect.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        if (connectionData != null) {
            String selectedOwner = ApplicationPanel.getInstance().destroyObjectChooser();
            if (selectedOwner != null && !selectedOwner.equals(connectionData.getDefaultOwner())) {
                connectionData.setDefaultOwner(selectedOwner);
                Config.saveDatabases(connectionDatas);
            }
        }
        try {
            if (connectionData != null && !connectionData.getConnection().isClosed()) {
                connectionData.getConnection().rollback();
                connectionData.getConnection().close();
            }
        } catch (Throwable t) {
            // ignore
        }
        connectionData = null;
        ApplicationPanel.getInstance().getFrame().setTitle(DBEdit.APPLICATION_NAME);
        handleActions();
    }

    public void ancestorRemoved(AncestorEvent event) {
        try {
            performThreaded(null);
        } catch (Throwable t) {
            // ignore
        }
        System.exit(0);
    }
}
