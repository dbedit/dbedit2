package dbedit.actions;

import dbedit.*;
import dbedit.Dialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class ConnectAction extends ActionChangeAbstractAction {

    protected ConnectAction() {
        super("Connect", "connect.png", null);
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        setConnectionDatas(Config.getDatabases());
        final JList list = new JList(getConnectionDatas());
        list.setVisibleRowCount(15);
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
                        setConnectionData(connectionData);
                        ApplicationPanel.getInstance().getFrame().setTitle(
                                DBEdit.APPLICATION_NAME + " - " + getConnectionData().getName());
                        handleActions();
                        ApplicationPanel.getInstance().initializeObjectChooser(getConnectionData());
                        connected = true;
                    } catch (Throwable t) {
                        ExceptionDialog.showException(t);
                        if (editConnection(connectionData, false)) {
                            Config.saveDatabases(getConnectionDatas());
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
                getConnectionDatas().add(connectionData);
                Config.saveDatabases(getConnectionDatas());
            }
            performThreaded(e);
        } else if ("Edit".equals(value)) {
            if (!list.isSelectionEmpty()) {
                ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                if (editConnection(connectionData, false)) {
                    Config.saveDatabases(getConnectionDatas());
                }
            }
            performThreaded(e);
        } else if ("Duplicate".equals(value)) {
            if (!list.isSelectionEmpty()) {
                ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                connectionData = (ConnectionData) connectionData.clone();
                if (editConnection(connectionData, false)) {
                    getConnectionDatas().add(connectionData);
                    Config.saveDatabases(getConnectionDatas());
                }
            }
            performThreaded(e);
        } else if ("Delete".equals(value)) {
            if (!list.isSelectionEmpty()) {
                if (Dialog.YES_OPTION == Dialog.show(
                        "Delete connection", "Are you sure?", Dialog.WARNING_MESSAGE, Dialog.YES_NO_OPTION)) {
                    ConnectionData connectionData = (ConnectionData) list.getSelectedValue();
                    getConnectionDatas().remove(connectionData);
                    Config.saveDatabases(getConnectionDatas());
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
        final JTextField name = new JTextField(connectionData.getName(), 50);
        panel.add(name, c);
        c.gridy++;
        panel.add(new JLabel("URL"), c);
        final JTextField url = new JTextField(connectionData.getUrl());
        panel.add(url, c);
        c.gridy++;
        panel.add(new JLabel("User"), c);
        final JTextField user = new JTextField(connectionData.getUser());
        panel.add(user, c);
        c.gridy++;
        panel.add(new JLabel("Password"), c);
        final JTextField password = new JPasswordField(connectionData.getPassword());
        panel.add(password, c);
        c.gridy++;
        panel.add(new JLabel("Driver"), c);
        final JComboBox driver = new JComboBox(new Object[] {
                ConnectionData.ORACLE_DRIVER, ConnectionData.IBM_DRIVER, ConnectionData.DATADIRECT_DRIVER,
                ConnectionData.MYSQL_DRIVER, ConnectionData.HSQLDB_DRIVER});
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

    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
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

    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        list.ensureIndexIsVisible(list.getSelectedIndex());
    }
}
