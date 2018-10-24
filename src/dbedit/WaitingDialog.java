package dbedit;

import javax.swing.*;

public class WaitingDialog {

    private JLabel message = new JLabel();
    private JDialog dialog;

    public WaitingDialog(final Runnable onCancel) {
        final Dialog pane = new Dialog(message, Dialog.PLAIN_MESSAGE, Dialog.DEFAULT_OPTION, new Object[] {"Cancel"}, "Cancel");
        dialog = pane.createDialog(ApplicationPanel.getInstance(), null);
        new Thread(new Runnable() {
            public void run() {
                dialog.setVisible(true);
                if (onCancel != null && "Cancel".equals(pane.getValue())) {
                    onCancel.run();
                }
            }
        }).start();
        while (!dialog.isVisible()) { /*wait*/ }
    }

    public void setText(String text) {
        message.setText(text);
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    public void hide() {
        dialog.setVisible(false);
    }
}
