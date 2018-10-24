package dbedit;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SchemaBrowser extends JTree {

    public SchemaBrowser(ConnectionData connectionData) {
        super(new ObjectNode(connectionData));
        ((DefaultTreeCellRenderer) getCellRenderer()).setLeafIcon(null);
        ((DefaultTreeCellRenderer) getCellRenderer()).setOpenIcon(null);
        ((DefaultTreeCellRenderer) getCellRenderer()).setClosedIcon(null);
    }

    public void expand(String[] path) {
        TreeNode node = (TreeNode) getModel().getRoot();
        int row = 0;
        for (String aPath : path) {
            for (int j = 0; j < node.getChildCount(); j++) {
                TreeNode child = node.getChildAt(j);
                if (aPath.equals(child.toString())) {
                    expandRow(row += j + 1);
                    node = child;
                    break;
                }
            }
        }
        setSelectionRow(row + 1);
        scrollRowToVisible(row - 1);
    }

    public String[] getSelectedItems() {
        TreePath[] selectionPaths = getSelectionPaths();
        String[] selectedItems = new String[selectionPaths.length];
        for (int i = 0; i < selectionPaths.length; i++) {
            TreePath selectionPath = selectionPaths[i];
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            String s = treeNode.toString();
            if (treeNode.getLevel() == 3) {
                s = treeNode.getParent().getParent() + "." + s;
            }
            selectedItems[i] = s;
        }
        return selectedItems;
    }

    public String getSelectedOwner() {
        TreePath selectionPath = getSelectionPath();
        if (selectionPath != null) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            if (treeNode.getLevel() > 1) {
                while (treeNode.getLevel() > 1) {
                    treeNode = (DefaultMutableTreeNode) treeNode.getParent();
                }
                return treeNode.toString();
            }
        }
        return null;
    }

    private static class ObjectNode extends DynamicUtilTreeNode {

        private ConnectionData connectionData;

        public ObjectNode(ConnectionData connectionData) {
            this(connectionData.getName(), new Object[0], connectionData);
        }

        public ObjectNode(Object value, Object children, ConnectionData connectionData) {
            super(value, children);
            this.connectionData = connectionData;
        }

        protected void loadChildren() {
            try {
                loadedChildren = true;
                if (allowsChildren) {
                    addChildren();
                }
            } catch (Throwable t) {
                throw new IllegalStateException(t.getMessage());
            }
        }

        private void addChildren() throws SQLException {
            switch (getLevel()) {
                case 0: {
                    if (connectionData.isOracle()) {
                        addQuery("select username from all_users order by username", true);
                    } else if (connectionData.isDb2()) {
                        addQuery("select distinct rtrim(tabschema) from syscat.tables", true);
                    } else if (connectionData.isMySql()) {
                        addQuery("show databases", true);
                    } else {
                        addQuery(connectionData.getConnection().getMetaData().getSchemas(), true, 1);
                    }
                    break;
                }
                case 1: {
                    add("TABLES", true);
                    add("VIEWS", true);
                    add("PROCEDURES", true);
                    break;
                }
                case 2: {
                    String owner = getParent().toString();
                    String type = toString();
                    if ("TABLES".equals(type)) {
                        if (connectionData.isOracle()) {
                            addQuery("select table_name from all_tables where owner = '" + owner + "' order by table_name", true);
                        } else if (connectionData.isDb2()) {
                            addQuery("select rtrim(tabname) from syscat.tables where tabschema = '" + owner + "' order by tabname", true);
                        } else if (connectionData.isMySql()) {
                            addQuery("show tables from " + owner, true);
                        } else {
                            addQuery(connectionData.getConnection().getMetaData().getTables(null, owner, null, new String[] {"TABLE", "SYSTEM TABLE"}), true, 3);
                        }
                    } else if ("VIEWS".equals(type)) {
                        if (connectionData.isOracle()) {
                            addQuery("select view_name from all_views where owner = '" + owner + "' order by view_name", true);
                        } else if (connectionData.isDb2()) {
                            addQuery("select rtrim(viewname) from syscat.views where viewschema = '" + owner + "' order by viewname", true);
                        } else if (connectionData.isMySql()) {
                            addQuery("select rtrim(table_name) from information_schema.views where table_schema = '" + owner + "' order by table_name", true);
                        } else {
                            addQuery(connectionData.getConnection().getMetaData().getTables(null, owner, null, new String[] {"VIEW"}), true, 3);
                        }
                    } else if ("PROCEDURES".equals(type)) {
                        if (connectionData.isOracle()) {
                            addQuery("select distinct(object_name) from all_procedures where owner = '" + owner + "'", false);
                        } else if (connectionData.isDb2()) {
                            addQuery("select distinct(rtrim(procname)) as name from syscat.procedures where procschema = '" + owner + "' union select rtrim(funcname) as name from syscat.functions where funcschema = '" + owner + "'", false);
                        } else if (connectionData.isMySql()) {
                            addQuery("select distinct(rtrim(routine_name)) from information_schema.routines where routine_schema = '" + owner + "'", true);
                        } else {
                            addQuery(connectionData.getConnection().getMetaData().getProcedures(null, owner, null), true, 3);
                        }
                    }
                    break;
                }
                case 3: {
                    String table = toString();
                    if (connectionData.isOracle()) {
                        addQuery("select column_name from all_tab_cols where table_name = '" + table + "' order by column_id", false);
                    } else if (connectionData.isDb2()) {
                        addQuery("select rtrim(colname) from syscat.columns where tabname = '" + table + "' order by colno", false);
                    } else if (connectionData.isMySql()) {
                        addQuery("show columns from " + getParent().getParent() + "." + table, false);
                    } else {
                        addQuery(connectionData.getConnection().getMetaData().getColumns(null, getParent().getParent().toString(), table, null), false, 4);
                    }
                    break;
                }
            }
        }

        private void addQuery(String query, boolean children) throws SQLException {
            if (!connectionData.getConnection().isClosed()) {
                ResultSet resultSet = connectionData.getConnection().createStatement().executeQuery(query);
                addQuery(resultSet, children, 1);
            }
        }

        private void addQuery(ResultSet resultSet, boolean children, int columnIndex) throws SQLException {
            while (resultSet.next()) {
                add(resultSet.getString(columnIndex), children);
            }
        }

        private void add(String s, boolean children) {
            add(new ObjectNode(s, children ? new Object[0] : null, connectionData));
        }
    }
}

