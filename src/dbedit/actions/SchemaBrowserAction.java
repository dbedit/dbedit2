package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.SchemaBrowser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class SchemaBrowserAction extends CustomAction {

    protected SchemaBrowserAction() {
        super("Schema browser", "schema.png", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK));
    }

    public void actionPerformed(final ActionEvent e) {
        ApplicationPanel.getInstance().showHideObjectChooser();
    }

    protected void performThreaded(ActionEvent e) throws Exception {
    }

    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            selectFromObjectChooser((SchemaBrowser) e.getSource());
            ApplicationPanel.getInstance().showHideObjectChooser();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getSource() instanceof SchemaBrowser) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isAltDown()) {
                selectFromObjectChooser((SchemaBrowser) e.getSource());
                ApplicationPanel.getInstance().showHideObjectChooser();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                ApplicationPanel.getInstance().showHideObjectChooser();
            }
        }
    }

    private void selectFromObjectChooser(SchemaBrowser schemaBrowser) {
        String s = Arrays.asList(schemaBrowser.getSelectedItems()).toString();
        s = s.substring(1, s.length() - 1);
        ApplicationPanel.getInstance().setText(s);
    }
}
