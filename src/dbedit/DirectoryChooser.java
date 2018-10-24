package dbedit;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class DirectoryChooser extends JTree {

    private static DirectoryChooser directoryChooser;
    private static final FileSystemView FILE_SYSTEM_VIEW = FileSystemView.getFileSystemView();

    public static File chooseDirectory() {
        if (directoryChooser == null) {
            directoryChooser = new DirectoryChooser();
        }
        JScrollPane scrollPane = new JScrollPane(directoryChooser);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        if (Dialog.OK_OPTION == Dialog.show("Choose directory", scrollPane,
                Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
            return directoryChooser.getSelectedDirectory();
        } else {
            return null;
        }
    }

    public DirectoryChooser() {
        super(new DirNode());
        setCellRenderer(new DirRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void setSelectedDirectory(File dir) {
        expand(dir);
    }

    private DirNode expand(File dir) {
        File parentFile = FILE_SYSTEM_VIEW.getParentDirectory(dir);
        DirNode parentNode;
        if (parentFile != null) {
            parentNode = expand(parentFile);
        } else {
            setSelectionRow(0);
            return (DirNode) getModel().getRoot();
        }
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            DirNode childNode = (DirNode) parentNode.getChildAt(i);
            File childFile = (File) childNode.getUserObject();
            if (dir.equals(childFile)) {
                int row = getSelectionRows()[0] + i + 1;
                expandRow(row);
                setSelectionRow(row);
                scrollRowToVisible(row);
                return childNode;
            }
        }
        return null;
    }

    public File getSelectedDirectory() {
        DirNode node = (DirNode) getLastSelectedPathComponent();
        return node == null ? null : (File) node.getUserObject();
    }

    private static class DirNode extends DynamicUtilTreeNode {
        public DirNode() {
            this(FILE_SYSTEM_VIEW.getRoots()[0], new Object[0]);
        }

        public DirNode(Object value, Object children) {
            super(value, children);
        }

        protected void loadChildren() {
            loadedChildren = true;
            File[] files = FILE_SYSTEM_VIEW.getFiles((File) userObject, true);
            Arrays.sort(files);
            for (File file : files) {
                if (FILE_SYSTEM_VIEW.isTraversable(file)) {
                    add(new DirNode(file, new Object[0]));
                }
            }
        }
    }

    private static class DirRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            File file = (File) ((DefaultMutableTreeNode) value).getUserObject();
            Icon systemIcon = FILE_SYSTEM_VIEW.getSystemIcon(file);
            setOpenIcon(systemIcon);
            setClosedIcon(systemIcon);
            setLeafIcon(systemIcon);
            return super.getTreeCellRendererComponent(tree, FILE_SYSTEM_VIEW.getSystemDisplayName(file), sel, expanded,
                    leaf, row, hasFocus);
        }
    }
}

