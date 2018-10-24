/*
 * DBEdit 2
 * Copyright (C) 2006-2009 Jef Van Den Ouweland
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

    @Override
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

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        try {
            performThreaded(null);
        } catch (Throwable t) {
            ExceptionDialog.hideException(t);
        }
        System.exit(0);
    }
}
