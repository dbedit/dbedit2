package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FetchLimitAction extends CustomAction {

    protected FetchLimitAction() {
        super("Fetch limit = unlimited", null, null);
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(fetchLimit, -1, 999999, 1));
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ApplicationPanel.getInstance(), spinner, "Fetch limit", JOptionPane.OK_CANCEL_OPTION)) {
            fetchLimit = ((Number) spinner.getValue()).intValue();
        }
        putValue(NAME, "Fetch limit = " + (fetchLimit == -1 ? "unlimited" : ""+fetchLimit));
    }
}
