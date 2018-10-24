package dbedit.actions;

import dbedit.Dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FetchLimitAction extends CustomAction {

    protected FetchLimitAction() {
        super("Fetch limit = unlimited", "empty.png", null);
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(getFetchLimit(), -1, 999999, 1));
        if (Dialog.OK_OPTION == Dialog.show("Fetch limit", spinner, Dialog.QUESTION_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
            setFetchLimit(((Number) spinner.getValue()).intValue());
        }
        putValue(NAME, "Fetch limit = " + (getFetchLimit() == -1 ? "unlimited" : "" + getFetchLimit()));
    }
}
