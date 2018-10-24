package dbedit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 6-sep-2005
 * Time: 11:33:02
 */
public class WaitingDialog {

    private JLabel message = new JLabel();
    private JDialog dialog;

    public WaitingDialog() {
        this(null);
    }

    public WaitingDialog(final Runnable onCancel) {
        final Dialog pane = new Dialog(message, Dialog.PLAIN_MESSAGE, Dialog.DEFAULT_OPTION, new Object[] {"Cancel"}, "Cancel");
        dialog = pane.createDialog(ApplicationPanel.getInstance(), null);
        new Thread(new Runnable() {
            public void run() {
                dialog.show();
                if (onCancel != null && "Cancel".equals(pane.getValue())) {
                    onCancel.run();
                }
            }
        }).start();
        while (!dialog.isVisible());
    }

    public void setText(String text) {
        message.setText(text);
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    public void hide() {
        dialog.hide();
    }
}
