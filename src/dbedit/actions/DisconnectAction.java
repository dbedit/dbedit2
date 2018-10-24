package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Config;
import dbedit.DBEdit;
import dbedit.ExceptionDialog;

import javax.swing.event.AncestorEvent;
import java.awt.event.ActionEvent;

public class DisconnectAction extends ActionChangeAbstractAction {

    protected DisconnectAction() {
        super("Disconnect", "disconnect.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        if (getConnectionData() != null) {
            String selectedOwner = ApplicationPanel.getInstance().destroyObjectChooser();
            if (selectedOwner != null && !selectedOwner.equals(getConnectionData().getDefaultOwner())) {
                getConnectionData().setDefaultOwner(selectedOwner);
                Config.saveDatabases(getConnectionDatas());
            }
        }
        try {
            if (getConnectionData() != null && !getConnectionData().getConnection().isClosed()) {
                getConnectionData().getConnection().rollback();
                if (getConnectionData().isHSQLDB()) {
                    getConnectionData().getConnection().createStatement().execute("shutdown");
                }
                getConnectionData().getConnection().close();
            }
        } catch (Throwable t) {
            ExceptionDialog.hideException(t);
        }
        setConnectionData(null);
        ApplicationPanel.getInstance().getFrame().setTitle(DBEdit.APPLICATION_NAME);
        handleActions();
    }

    public void ancestorRemoved(AncestorEvent event) {
        try {
            performThreaded(null);
        } catch (Throwable t) {
            ExceptionDialog.hideException(t);
        }
        System.exit(0);
    }
}
