/*
 * DBEdit 2
 * Copyright (C) 2006-2012 Jef Van Den Ouweland
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
import jsyntaxpane.DefaultSyntaxKit;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyListener;

public final class ApplicationPanel extends JPanel {

    private static final ApplicationPanel APPLICATION_PANEL = new ApplicationPanel();

    private JEditorPane text;
    private UndoManager undoManager;
    private JSplitPane splitPane;
    private JScrollPane rightComponent;
    private JToggleButton schemaBrowserToggleButton = new JToggleButton();

    public static ApplicationPanel getInstance() {
        return APPLICATION_PANEL;
    }

    private ApplicationPanel() {
        addAncestorListener((AncestorListener) Actions.DISCONNECT);

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
        queryArea.setDividerLocation(120);
        return queryArea;
    }

    private JScrollPane createQueryEditor() {
        text = new JEditorPane();
        if (Actions.UNDU_REDO_ENABLED) {
            undoManager = new UndoManager();
            text.getDocument().addUndoableEditListener(undoManager);
        }
        text.getDocument().addDocumentListener(Actions.getInstance());
        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        addsyntaxHighlighting();
        return scrollPane;
    }

    private void addsyntaxHighlighting() {
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

//      7 SyntaxHighlighter
//        http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/
//        stops recolouring after a while

//        Scanner scanner = new JavaScanner();
//        text = new SyntaxHighlighter(24, 80, scanner);

//      8 jsyntaxpane
//        http://code.google.com/p/jsyntaxpane/
//        Looks great, let's use it!

        DefaultSyntaxKit.initKit();
        text.setContentType("text/sql");

        // Rearrange the menu a bit
        JPopupMenu menu = text.getComponentPopupMenu();
        menu.remove(17); // "Goto line" defined twice
        menu.remove(15); // "Show abbreviations", not implemented for text/sql
        menu.remove(12); // "Jump to Pair", not implemented for text/sql
        menu.add(text.getActionMap().get("toggle-lines"));

        // Set proper menu texts
        DefaultSyntaxKit kit = (DefaultSyntaxKit) text.getEditorKit();
        for (Object key : text.getActionMap().keys()) {
            Action action = text.getActionMap().get(key);
            action.putValue(Action.NAME,
                    kit.getProperty(String.format("Action.%s.MenuText", action.getValue(Action.NAME))));
        }
    }

    private JScrollPane createQueryTable() {
        return new JScrollPane(ResultSetTable.getInstance());
    }

    public void initializeObjectChooser(final ConnectionData connectionData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final SchemaBrowser schemaBrowser = new SchemaBrowser(connectionData);
                    schemaBrowser.addKeyListener((KeyListener) Actions.SCHEMA_BROWSER);
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

    public void destroyObjectChooser() {
        if (rightComponent != null) {
            rightComponent = null;
            splitPane.setRightComponent(rightComponent);
            splitPane.setDividerSize(0);
            schemaBrowserToggleButton.setSelected(false);
            Actions.SCHEMA_BROWSER.setEnabled(false);
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

    public SchemaBrowser getObjectChooser() {
        return rightComponent == null ? null : (SchemaBrowser) rightComponent.getViewport().getComponent(0);
    }

    public String getText() {
        return text.getSelectedText() != null ? text.getSelectedText() : text.getText();
    }

    public void replaceText(String t) {
        text.replaceSelection(t);
    }

    public void setText(final String t) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                text.setText(t);
            }
        });
    }

    public JEditorPane getTextComponent() {
        return text;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public JFrame getFrame() {
        return (JFrame) getRootPane().getParent();
    }

}
