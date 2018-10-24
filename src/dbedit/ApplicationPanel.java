package dbedit;

import dbedit.actions.Actions;
import dbedit.actions.CustomAction;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 26-apr-2005
 * Time: 13:44:29
 */
public class ApplicationPanel extends JPanel {

    private static ApplicationPanel applicationPanel = new ApplicationPanel();

    private JTextArea text;
    public UndoManager undoManager;
    private JTable table;
    private List originalOrder;
    private JSplitPane splitPane;
    private JScrollPane rightComponent;
    protected JToggleButton schemaBrowserToggleButton = new JToggleButton();

    public static ApplicationPanel getInstance() {
        return applicationPanel;
    }

    private ApplicationPanel() {
        addAncestorListener(Actions.DISCONNECT);

        // Layout grid
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 100;
        c.gridy++;
        add(new ApplicationMenuBar(), c);
        c.gridy++;
        add(new ApplicationToolBar(schemaBrowserToggleButton), c);
        c.weighty = 100;
        c.gridy++;
        add(createWorkArea(), c);

        text.requestFocus();
    }

    private JSplitPane createWorkArea() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createQueryArea(), null);
        splitPane.setDividerSize(0);
        return splitPane;
    }

    private JSplitPane createQueryArea() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createQueryEditor(), createQueryTable());
        splitPane.setOneTouchExpandable(true);
        return splitPane;
    }

    private JScrollPane createQueryEditor() {
        text = new JTextArea();
        text.setFont(new Font("Courier New", Font.PLAIN, 12));
        text.setMargin(new Insets(2, 2, 2, 2));
        text.getDocument().addUndoableEditListener(undoManager = new UndoManager());
        text.getDocument().addDocumentListener(Actions.RUN);
        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setPreferredSize(new Dimension(0, 70));
        scrollPane.getViewport().setMinimumSize(new Dimension(0, 70));
        return scrollPane;
    }

    private JScrollPane createQueryTable() {
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setToolTipText("<html>Left click: sort asc<br>Right click: sort desc</html>");
        table.getTableHeader().addMouseListener(Actions.RUN);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.addMouseListener(Actions.LOB_OPEN);
        table.addMouseListener(Actions.LOB_OPEN_WITH);
        table.getSelectionModel().addListSelectionListener(Actions.RUN);
        table.getColumnModel().addColumnModelListener(Actions.RUN);
        table.getDefaultEditor(Object.class).addCellEditorListener(Actions.EDIT);
        table.setModel(new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return !CustomAction.isLobSelected(column);
            }
        });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Blob) {
                    value = "BLOB";
                } else if (value instanceof Clob) {
                    value = "CLOB";
                } else if (value instanceof byte[]) {
                    value = "BINARY";
                }
                JLabel tableCellRendererComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                tableCellRendererComponent.setHorizontalAlignment(value instanceof Number ? SwingConstants.TRAILING : SwingConstants.LEADING);
                return tableCellRendererComponent;
            }
        });
        return new JScrollPane(table);
    }

    public void initializeObjectChooser(final ConnectionData connectionData) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    final SchemaBrowser schemaBrowser = new SchemaBrowser(connectionData);
                    schemaBrowser.addKeyListener(Actions.SCHEMA_BROWSER);
                    schemaBrowser.addMouseListener(Actions.SCHEMA_BROWSER);
                    rightComponent = new JScrollPane(schemaBrowser);
                    schemaBrowser.expand(new String[] {connectionData.getName(), connectionData.getDefaultOwner(), "TABLES"});
                    Actions.SCHEMA_BROWSER.setEnabled(true);
                } catch (IllegalStateException e) {
                    // ignore: connection has been closed
                }
            }
        }).start();
    }

    public String destroyObjectChooser() {
        if (rightComponent != null) {
            String selectedOwner = getObjectChooser().getSelectedOwner();
            splitPane.setRightComponent(rightComponent = null);
            splitPane.setDividerSize(0);
            Actions.SCHEMA_BROWSER.setEnabled(false);
            return selectedOwner;
        } else {
            return null;
        }
    }

    public void showHideObjectChooser() {
        if (splitPane.getRightComponent() == null) {
            splitPane.setDividerLocation(.5);
            splitPane.setDividerSize(4);
            splitPane.setRightComponent(rightComponent);
            getObjectChooser().requestFocus();
            schemaBrowserToggleButton.setSelected(true);
        } else {
            splitPane.setRightComponent(null);
            splitPane.setDividerSize(0);
            text.requestFocus();
            schemaBrowserToggleButton.setSelected(false);
        }
    }

    private SchemaBrowser getObjectChooser() {
        return (SchemaBrowser) rightComponent.getViewport().getComponent(0);
    }

    public String getText() {
        return text.getSelectedText() != null ? text.getSelectedText() : text.getText();
    }

    public void setText(String t) {
        text.insert(t, text.getCaretPosition());
    }

    public JTextArea getTextArea() {
        return text;
    }

    public Object getTableValue() {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        return table.getValueAt(row, column);
    }

    public void setTableValue(Object o) {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        table.setValueAt(o, row, column);
    }

    public List getSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            return (List) ((DefaultTableModel) table.getModel()).getDataVector().get(row);
        } else {
            return null;
        }
    }

    public int getOriginalSelectedRow() {
        int row = table.getSelectedRow();
        return getOriginalSelectedRow(row);
    }

    public int getOriginalSelectedRow(int selectedRow) {
        return originalOrder.indexOf(((DefaultTableModel) table.getModel()).getDataVector().get(selectedRow));
    }

    public void removeRow(int row) {
        int originalSelectedRow = getOriginalSelectedRow(row);
        ((DefaultTableModel) table.getModel()).removeRow(row);
        originalOrder.remove(originalSelectedRow);
    }

    public JTable getTable() {
        return table;
    }

    public void setDataVector(Vector dataVector, Vector columnIdentifiers) {
        originalOrder = new ArrayList(dataVector);
        ((DefaultTableModel) table.getModel()).setDataVector(dataVector, columnIdentifiers);
        table.setToolTipText(dataVector.size() + " row" + (dataVector.size() != 1 ? "s" : ""));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resizeColumns(table);
            }
        });
    }

    public JFrame getFrame() {
        return (JFrame) getRootPane().getParent();
    }

    protected void resizeColumns(JTable table) {
        double[] widths = new double[table.getColumnCount()];
        TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            for (int j = 0; j < table.getModel().getColumnCount(); j++) {
                Component component = defaultRenderer.getTableCellRendererComponent(table, table.getModel().getValueAt(i, j), false, false, i, j);
                widths[j] = Math.max(widths[j], component.getPreferredSize().getWidth());
            }
        }
        defaultRenderer = table.getTableHeader().getDefaultRenderer();
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            Component component = defaultRenderer.getTableCellRendererComponent(table, columnModel.getColumn(i).getHeaderValue(), false, false, 0, i);
            widths[i] = Math.max(widths[i], component.getPreferredSize().getWidth());
            widths[i] = Math.min(widths[i], 550);
            columnModel.getColumn(i).setPreferredWidth((int) widths[i] + 2);
        }
    }
}
