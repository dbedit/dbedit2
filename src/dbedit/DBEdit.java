package dbedit;

import dbedit.actions.Actions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DBEdit extends JFrame {

    public static final String APPLICATION_NAME = "DBEdit 2";

    public static void main(String[] args) throws IllegalAccessException, UnsupportedLookAndFeelException,
                                                  InstantiationException, ClassNotFoundException {
        // Don't set the system look and feel for OS'es other than Windows
        // Xfce on Xubuntu looked OK (GTK)
        // GNOME on Red Hat Ent. Linux and Oracle Ent. Linux and Solaris didn't look too good (GTK)
        // CDE on Solaris also didn't look good (Motif)
        // Others not yet tested
        // So use default look and feel (Metal)
        if (Config.IS_OS_WINDOWS) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        new DBEdit();
    }

    public DBEdit() {
        try {
            setTitle(APPLICATION_NAME);
            setIconImage(new ImageIcon(DBEdit.class.getResource("/icons/icon.gif")).getImage());
            getContentPane().add(ApplicationPanel.getInstance());
            setJMenuBar(new ApplicationMenuBar());
            double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            setSize((int) (screenWidth * .8), (int) (screenHeight * .8));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setVisible(true);
            Actions.CONNECT.actionPerformed(new ActionEvent(this, 0, null));
        } catch (Throwable t) {
            ExceptionDialog.showException(t);
            System.exit(1);
        }
    }
}
