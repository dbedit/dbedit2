package dbedit;

import dbedit.actions.Actions;
import dbedit.actions.GroupAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ApplicationToolBar extends JToolBar {

    public ApplicationToolBar(JToggleButton schemaBrowserToggleButton) {
        setFloatable(false);
        add(Actions.CONNECT);
        add(Actions.DISCONNECT);
        add(Actions.COMMIT);
        add(Actions.ROLLBACK);
        addSeparator();
        add(Actions.UNDO);
        add(Actions.REDO);
        addSeparator();
        add(Actions.CUT);
        add(Actions.COPY);
        add(Actions.PASTE);
        addSeparator();
        add(Actions.RUN);
        add(Actions.RUN_SCRIPT);
        addSeparator();
        add(Actions.FAVORITES);
        add(Actions.HISTORY_PREVIOUS);
        add(Actions.HISTORY_NEXT);
        addSeparator();
        add(Actions.INSERT);
        add(Actions.DELETE);
        add(Actions.EDIT);
        add(Actions.DUPLICATE);
        addSeparator();
        add(Actions.LOB_GROUP);
        addSeparator();
        add(Actions.EXPORT_GROUP);
        /*addSeparator();
        add(new JLabel("Export "));
        add(Actions.EXPORT_EXCEL);
        add(Actions.EXPORT_FLAT_FILE);
        add(Actions.EXPORT_INSERTS);
        addSeparator();
        add(new JLabel("Lob "));
        add(Actions.LOB_IMPORT);
        add(Actions.LOB_EXPORT);
        add(Actions.LOB_COPY);
        add(Actions.LOB_PASTE);*/
        addSeparator();
        add(Box.createHorizontalGlue());
        schemaBrowserToggleButton.setAction(Actions.SCHEMA_BROWSER);
        schemaBrowserToggleButton.setText(null);
        add(setup(schemaBrowserToggleButton));
    }

    public JButton add(Action action) {
        return (JButton) setup(super.add(action));
    }

    private AbstractButton setup(AbstractButton button) {
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusable(false);
        String toolTipText = (String) button.getAction().getValue(Action.NAME);
        KeyStroke accelerator = (KeyStroke) button.getAction().getValue(AbstractAction.ACCELERATOR_KEY);
        if (accelerator != null) {
            toolTipText += " (";
            toolTipText += KeyEvent.getModifiersExText(accelerator.getModifiers());
            toolTipText += "+";
            toolTipText += KeyEvent.getKeyText(accelerator.getKeyCode());
            toolTipText += ")";
        }
        button.setToolTipText(toolTipText);
        return button;
    }

    public JToggleButton add(GroupAction action) {
        JToggleButton toggleButton = (JToggleButton) add(new JToggleButton(action));
        toggleButton.setHorizontalTextPosition(JButton.LEFT);
        toggleButton.setVerticalTextPosition(JButton.BOTTOM);
        return toggleButton;
    }
}
