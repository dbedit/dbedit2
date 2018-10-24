package dbedit;

import javax.swing.*;

public class Dialog extends JOptionPane {

    public Dialog(Object message, int messageType, int optionType, Object[] options, Object initialValue) {
        super(message, messageType, optionType, null, options, initialValue);
    }

    public static int show(String title, Object message, int messageType, int optionType) {
        Dialog dialog = new Dialog(message, messageType, optionType, null, null);
        dialog.createDialog(ApplicationPanel.getInstance(), title).show();
        return dialog.getValue() == null ? CLOSED_OPTION : ((Number) dialog.getValue()).intValue();
    }

    public static Object show(String title, Object message, int messageType, Object[] options, Object initialValue) {
        Dialog dialog = new Dialog(message, messageType, DEFAULT_OPTION, options, initialValue);
        dialog.createDialog(ApplicationPanel.getInstance(), title).show();
        return dialog.getValue();
    }

    public void selectInitialValue() {
    }
}
