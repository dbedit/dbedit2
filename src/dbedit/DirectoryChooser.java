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

    private static DirectoryChooser DIRECTORY_CHOOSER;

    public static File chooseDirectory() {
        if (DIRECTORY_CHOOSER == null) {
            DIRECTORY_CHOOSER = new DirectoryChooser();
        }
        JScrollPane scrollPane = new JScrollPane(DIRECTORY_CHOOSER);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        if (Dialog.OK_OPTION == Dialog.show("Choose directory", scrollPane, Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
            return DIRECTORY_CHOOSER.getSelectedDirectory();
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
        File parentFile = FileSystemView.getFileSystemView().getParentDirectory(dir);
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
            this(FileSystemView.getFileSystemView().getRoots()[0], new Object[0]);
        }

        public DirNode(Object value, Object children) {
            super(value, children);
        }

        protected void loadChildren() {
            loadedChildren = true;
            File[] files = FileSystemView.getFileSystemView().getFiles((File) userObject, true);
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (FileSystemView.getFileSystemView().isTraversable(file).booleanValue()) {
                    add(new DirNode(file, new Object[0]));
                }
            }
        }
    }

    private static class DirRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            File file = (File) ((DefaultMutableTreeNode) value).getUserObject();
            setOpenIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
            setClosedIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
            setLeafIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
            return super.getTreeCellRendererComponent(tree, FileSystemView.getFileSystemView().getSystemDisplayName(file), sel, expanded, leaf, row, hasFocus);
        }
    }
}

