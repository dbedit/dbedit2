package dbedit;

import dbedit.actions.Actions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 26-apr-2005
 * Time: 13:44:29
 */
public class DBEdit extends JFrame {

    public static final String APPLICATION_NAME = "DBEdit 2";

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
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
