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
import dbedit.Dialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.sql.SQLWarning;
import java.util.Vector;

public class ConnectAction extends ActionChangeAbstractAction {

    protected ConnectAction() {
        super("Connect", "connect.png", null);
        setEnabled(true);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        ((DisconnectAction) Actions.DISCONNECT).saveDefaultOwner();
        Vector<ConnectionData> connectionDatas = Config.getDatabases();
        final JList list = new JList(connectionDatas);
        list.addMouseListener(this);
        list.addListSelectionListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Object value = Dialog.show("Connections", new JScrollPane(list), Dialog.PLAIN_MESSAGE,
                new Object[] {"Connect", "Cancel", "Add", "Edit", "Duplicate", "Delete"}, "Connect");
        if ("Connect".equals(value)) {
            if (!list.isSelectionEmpty()) {
                Actions.DISCONNECT.performThreaded(e);
                ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                boolean connected = false;
                while (!connected) {
                    try {
                        connectionData.connect();
                        SQLWarning warnings = connectionData.getConnection().getWarnings();
                        while (warnings != null) {
                            Dialog.show("Warning", warnings.getMessage(), Dialog.WARNING_MESSAGE,
                                    Dialog.DEFAULT_OPTION);
                            warnings = warnings.getNextWarning();
                        }

                        setConnectionData(connectionData);
                        ApplicationPanel.getInstance().getFrame().setTitle(
                                String.format("%s - %s", DBEdit.APPLICATION_NAME, getConnectionData().getName()));
                        handleActions();
                        ApplicationPanel.getInstance().initializeObjectChooser(getConnectionData());
                        connected = true;
                    } catch (Throwable t) {
                        ExceptionDialog.showException(t);
                        if (editConnection(connectionData, false)) {
                            Config.saveDatabases(connectionDatas);
                        } else {
                            performThreaded(e);
                            return;
                        }
                    }
                }
            }
        } else if ("Add".equals(value)) {
            ConnectionData connectionData = new ConnectionData();
            if (editConnection(connectionData, true)) {
                connectionDatas.add(connectionData);
                Config.saveDatabases(connectionDatas);
            }
            performThreaded(e);
        } else if ("Edit".equals(value)) {
            if (!list.isSelectionEmpty()) {
                ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                if (editConnection(connectionData, false)) {
                    Config.saveDatabases(connectionDatas);
                }
            }
            performThreaded(e);
        } else if ("Duplicate".equals(value)) {
            if (!list.isSelectionEmpty()) {
                ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                connectionData = (ConnectionData) connectionData.clone();
                if (editConnection(connectionData, false)) {
                    connectionDatas.add(connectionData);
                    Config.saveDatabases(connectionDatas);
                }
            }
            performThreaded(e);
        } else if ("Delete".equals(value)) {
            if (!list.isSelectionEmpty()) {
                if (Dialog.YES_OPTION == Dialog.show(
                        "Delete connection", "Are you sure?", Dialog.WARNING_MESSAGE, Dialog.YES_NO_OPTION)) {
                    ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                    connectionDatas.remove(connectionData);
                    Config.saveDatabases(connectionDatas);
                }
            }
            performThreaded(e);
        }
    }

    private boolean editConnection(final ConnectionData connectionData, boolean add) throws Exception {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy++;
        panel.add(new JLabel("Name"), c);
        JTextField name = new JTextField(connectionData.getName(), 50);
        panel.add(name, c);
        c.gridy++;
        panel.add(new JLabel("URL"), c);
        final JTextField url = new JTextField(connectionData.getUrl());
        panel.add(url, c);
        JLabel link = new JLabel("<html><b>?</b></html>");
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(this);
        panel.add(link, c);
        c.gridy++;
        panel.add(new JLabel("User"), c);
        JTextField user = new JTextField(connectionData.getUser());
        panel.add(user, c);
        c.gridy++;
        panel.add(new JLabel("Password"), c);
        JTextField password = new JPasswordField(connectionData.getPassword());
        panel.add(password, c);
        c.gridy++;
        panel.add(new JLabel("Driver"), c);
        JComboBox driver = new JComboBox(new Object[] {
                ConnectionData.ORACLE_DRIVER, ConnectionData.IBM_DRIVER, ConnectionData.DATADIRECT_DRIVER,
                ConnectionData.MYSQL_DRIVER, ConnectionData.HSQLDB_DRIVER, ConnectionData.SQLITE_DRIVER});
        driver.setEditable(true);
        driver.setSelectedItem(connectionData.getDriver());
        panel.add(driver, c);
        if (add) {
            PLUGIN.customizeConnectionPanel(panel, c, connectionData);
        }
        int i = Dialog.show("Connection", panel, Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION);
        connectionData.setName(name.getText());
        connectionData.setUrl(url.getText());
        connectionData.setUser(user.getText());
        connectionData.setPassword(password.getText());
        connectionData.setDriver(driver.getSelectedItem() == null ? "" : (String) driver.getSelectedItem());
        return Dialog.OK_OPTION == i;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getSource() instanceof JLabel) {
            try {
                openURL(Config.HOME_PAGE + "manual.html#connect");
            } catch (Exception e1) {
                ExceptionDialog.showException(e1);
            }
        } else if (e.getClickCount() == 2) {
            Container container = (Container) e.getSource();
            while (!(container instanceof JOptionPane)) {
                container = container.getParent();
            }
            JOptionPane optionPane = (JOptionPane) container;
            Object value = optionPane.getInitialValue();
            if (value == null) {
                value = JOptionPane.OK_OPTION;
            }
            optionPane.setValue(value);
            while (!(container instanceof JDialog)) {
                container = container.getParent();
            }
            container.setVisible(false);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }
}
