/**
 * DBEdit 2
 * Copyright (C) 2006-2008 Jef Van Den Ouweland
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
package dbedit;

import dbedit.actions.Actions;
import dbedit.actions.CustomAction;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public final class ApplicationPanel extends JPanel {

    private static ApplicationPanel applicationPanel = new ApplicationPanel();

    private JTextComponent text;
    private UndoManager undoManager;
    private JTable table;
    private List<Vector> originalOrder;
    private JSplitPane splitPane;
    private JScrollPane rightComponent;
    private JToggleButton schemaBrowserToggleButton = new JToggleButton();

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
        JSplitPane queryArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createQueryEditor(), createQueryTable());
        queryArea.setOneTouchExpandable(true);
        return queryArea;
    }

    private JScrollPane createQueryEditor() {
        text = new JTextArea();

//      Syntax highlighting solutions tried so far:

//      1 jEdit Syntax Package
//        http://syntax.jedit.org/
//        Not pluggable on JTextComponent, needed to use custom JEditTextArea, too heavy and all the wrong features

//        syntax.SyntaxDocument doc = new SyntaxDocument();
//        doc.setTokenMarker(new TSQLTokenMarker());
//        text.setDocument(doc);

//      2 http://www.discoverteenergy.com/files/SyntaxDocument.java
//        Highlighting comments doesn't work well

//        text = new JEditorPane();
//        ((JEditorPane) text).setEditorKit(new StyledEditorKit() {
//            public Document createDefaultDocument() {
//                return new SyntaxDocument();
//            }
//        });

//      3 com.Ostermiller.Syntax
//        http://ostermiller.org/syntax/
//        Strings in quotes aren't highlighted in real-time

//        HighlightedDocument doc = new HighlightedDocument();
//        doc.setHighlightStyle(HighlightedDocument.SQL_STYLE);
//        text.setDocument(doc);

//      4 JSyntaxColor
//        http://www.japisoft.com/syntaxcolor/
//        Not free and not open source

//      5 Colorer
//        http://colorer.sourceforge.net/
//        OS dependant (dll files and stuff)

//      6 CodeDocument
//        http://forum.java.sun.com/thread.jspa?forumID=57&threadID=607646
//        Lot of bugs

//        text.setDocument(new CodeDocument());

        text.setFont(new Font("Courier New", Font.PLAIN, 12));
        text.setMargin(new Insets(2, 2, 2, 2));
        undoManager = new UndoManager();
        text.getDocument().addUndoableEditListener(undoManager);
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
        if (Config.IS_OS_WINDOWS) {
            table.addMouseListener(Actions.LOB_OPEN);
            table.addMouseListener(Actions.LOB_OPEN_WITH);
        }
        table.getSelectionModel().addListSelectionListener(Actions.RUN);
        table.getColumnModel().addColumnModelListener(Actions.RUN);
        table.getDefaultEditor(Object.class).addCellEditorListener(Actions.EDIT);
        table.setModel(new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return CustomAction.getConnectionData() != null
                        && CustomAction.getConnectionData().getResultSet() != null
                        && !CustomAction.isLob(column);
            }
        });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable componentTable, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                if (value != null && CustomAction.isLob(column)) {
                    value = CustomAction.getColumnTypeNames()[column];
                }
                JLabel tableCellRendererComponent = (JLabel) super.getTableCellRendererComponent(
                        componentTable, value, isSelected, hasFocus, row, column);
                tableCellRendererComponent.setHorizontalAlignment(value instanceof Number ? SwingConstants.TRAILING
                                                                                          : SwingConstants.LEADING);
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
                    schemaBrowser.expand(new String[] {
                            connectionData.getName(), connectionData.getDefaultOwner(), "TABLES"});
                    Actions.SCHEMA_BROWSER.setEnabled(true);
                } catch (IllegalStateException e) {
                    // ignore: connection has been closed
                    ExceptionDialog.ignoreException(e);
                }
            }
        }).start();
    }

    public String destroyObjectChooser() {
        if (rightComponent != null) {
            String selectedOwner = getObjectChooser().getSelectedOwner();
            rightComponent = null;
            splitPane.setRightComponent(rightComponent);
            splitPane.setDividerSize(0);
            schemaBrowserToggleButton.setSelected(false);
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
        text.replaceSelection(t);
    }

    public JTextComponent getTextComponent() {
        return text;
    }

    public UndoManager getUndoManager() {
        return undoManager;
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

    public List<String> getSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            @SuppressWarnings("unchecked")
            List<String> strings = (List<String>) ((DefaultTableModel) table.getModel()).getDataVector().get(row);
            return strings;
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

    public void setDataVector(Vector<Vector> dataVector, Vector columnIdentifiers, String executionTime) {
        originalOrder = new ArrayList<Vector>(dataVector);
        ((DefaultTableModel) table.getModel()).setDataVector(dataVector, columnIdentifiers);
        String rows = dataVector.size() + " row" + (dataVector.size() != 1 ? "s" : "");
        JComponent scrollPane = (JComponent) table.getParent().getParent();
        if (dataVector.isEmpty()) {
            scrollPane.setToolTipText(rows + " - " + executionTime);
            table.setToolTipText(null);
        } else {
            table.setToolTipText(rows + " - " + executionTime);
            scrollPane.setToolTipText(null);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resizeColumns(table);
            }
        });
    }

    public JFrame getFrame() {
        return (JFrame) getRootPane().getParent();
    }

    protected void resizeColumns(JTable tableToResize) {
        double[] widths = new double[tableToResize.getColumnCount()];
        TableCellRenderer defaultRenderer = tableToResize.getDefaultRenderer(Object.class);
        for (int i = 0; i < tableToResize.getModel().getRowCount(); i++) {
            for (int j = 0; j < tableToResize.getModel().getColumnCount(); j++) {
                Component component = defaultRenderer.getTableCellRendererComponent(
                        tableToResize, tableToResize.getModel().getValueAt(i, j), false, false, i, j);
                widths[j] = Math.max(widths[j], component.getPreferredSize().getWidth());
            }
        }
        defaultRenderer = tableToResize.getTableHeader().getDefaultRenderer();
        TableColumnModel columnModel = tableToResize.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            Component component = defaultRenderer.getTableCellRendererComponent(
                    tableToResize, columnModel.getColumn(i).getHeaderValue(), false, false, 0, i);
            widths[i] = Math.max(widths[i], component.getPreferredSize().getWidth());
            widths[i] = Math.min(widths[i], 550);
            columnModel.getColumn(i).setPreferredWidth((int) widths[i] + 2);
        }
    }
}
