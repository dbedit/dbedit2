/*
 * DBEdit 2
 * Copyright (C) 2006-2010 Jef Van Den Ouweland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dbedit.actions;

import dbedit.*;

import javax.swing.event.AncestorEvent;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class DisconnectAction extends ActionChangeAbstractAction {

    protected DisconnectAction() {
        super("Disconnect", "disconnect.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        if (getConnectionData() != null) {
            saveDefaultOwner();
            ApplicationPanel.getInstance().destroyObjectChooser();
            try {
                if (!getConnectionData().getConnection().isClosed()) {
                    getConnectionData().getConnection().rollback();
                    getConnectionData().getConnection().close();
                }
            } catch (Throwable t) {
                ExceptionDialog.hideException(t);
            }
            setConnectionData(null);
            ApplicationPanel.getInstance().getFrame().setTitle(DBEdit.APPLICATION_NAME);
            handleActions();
        }
    }

    /**
     * Remember last selected schema
     */
    public void saveDefaultOwner() throws Exception {
        if (getConnectionData() != null && ApplicationPanel.getInstance().getObjectChooser() != null) {
            String selectedOwner = ApplicationPanel.getInstance().getObjectChooser().getSelectedOwner();
            if (selectedOwner != null && !selectedOwner.equals(getConnectionData().getDefaultOwner())) {
                getConnectionData().setDefaultOwner(selectedOwner);
                Vector<ConnectionData> connectionDatas = Config.getDatabases();
                for (ConnectionData connectionData : connectionDatas) {
                    if (connectionData.getName().equals(getConnectionData().getName())) {
                        connectionData.setDefaultOwner(selectedOwner);
                    }
                }
                Config.saveDatabases(connectionDatas);
            }
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        // Kills the application in 10 seconds in case disconnecting will hang
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 10000);
        try {
            performThreaded(null);
        } catch (Throwable t) {
            ExceptionDialog.hideException(t);
        }
        System.exit(0);
    }
}
