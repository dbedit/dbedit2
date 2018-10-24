package dbedit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public abstract class ThreadedAction implements Runnable {

    public ThreadedAction() {
        new Thread(this).start();
    }

    public final void run() {
        final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        final Component glassPane = ApplicationPanel.getInstance().getRootPane().getGlassPane();
        try {
            if (!glassPane.isVisible()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        if (glassPane.getMouseListeners().length == 0) {
                            glassPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                            glassPane.addMouseListener(new MouseAdapter(){});
                        }
                        glassPane.setVisible(true);
                        glassPane.requestFocus();
                    }
                });
            }
            execute();
        } catch (Throwable t) {
            ExceptionDialog.showException(t);
        } finally {
            if (glassPane.isVisible()) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            glassPane.setVisible(false);
                            focusOwner.requestFocus();
                        }
                    });
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
    }

    protected abstract void execute() throws Exception;
}
