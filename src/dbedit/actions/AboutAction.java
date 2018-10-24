package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Config;
import dbedit.DBEdit;
import dbedit.ExceptionDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.sql.DatabaseMetaData;

public class AboutAction extends CustomAction {

    protected AboutAction() {
        super("About", "empty.png", null);
        setEnabled(true);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        final Font logoFont = new Font("Dialog", Font.BOLD, 28);
        JPanel panel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int x = 10;
                int y = 32;
                g.setFont(logoFont);
                g.setColor(getBackground().brighter());
                g.drawString(DBEdit.APPLICATION_NAME, x + 1, y + 1);
                g.setColor(getBackground().darker());
                g.drawString(DBEdit.APPLICATION_NAME, x - 1, y - 1);
                g.setColor(getBackground());
                g.drawString(DBEdit.APPLICATION_NAME, x, y);
            }
        };
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy++;
        c.anchor = GridBagConstraints.SOUTHWEST;
        panel.add(new JLabel("Version: "), c);
        panel.add(new JLabel(Config.getVersion()), c);
        c.gridy++;
        panel.add(new JLabel("Author: "), c);
        panel.add(new JLabel("Jef Van Den Ouweland"), c);
        c.gridy++;
        panel.add(new JLabel("Home page: "), c);
        JLabel link = new JLabel(Config.HOME_PAGE);
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(this);
        panel.add(link, c);
        c.gridy++;
        panel.add(new JLabel("Java VM: "), c);
        panel.add(new JLabel(System.getProperty("java.vm.version")), c);
        if (connectionData != null) {
            try {
                DatabaseMetaData metaData = connectionData.getConnection().getMetaData();
                c.gridy++;
                panel.add(new JLabel("Database: "), c);
                panel.add(new JLabel(metaData.getDatabaseProductName()), c);
                c.gridy++;
                panel.add(new JLabel(""), c);
                panel.add(new JLabel("<html>"+metaData.getDatabaseProductVersion().replaceAll("\n", "<br>") + "</html>"), c);
                c.gridy++;
                panel.add(new JLabel("Driver: "), c);
                panel.add(new JLabel(metaData.getDriverName()), c);
                c.gridy++;
                panel.add(new JLabel(""), c);
                panel.add(new JLabel(metaData.getDriverVersion()), c);
            } catch (Throwable t) {
                // ignore
            }
        }
        PLUGIN.customizeAboutPanel(panel, c);
        panel.setBorder(new BevelBorder(BevelBorder.RAISED) {
            Insets insets = new Insets(42, 10, 10, 10);
            public Insets getBorderInsets(Component c) {
                return insets;
            }
        });
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(ApplicationPanel.getInstance()), true);
        dialog.setUndecorated(true);
        dialog.addMouseListener(this);
        dialog.getContentPane().add(panel);
        dialog.setSize(panel.getPreferredSize());
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof JLabel) {
            JLabel label = (JLabel) e.getSource();
            try {
                openURL(label.getText());
            } catch (Exception e1) {
                ExceptionDialog.showException(e1);
            }
        } else {
            ((JDialog) e.getSource()).setVisible(false);
        }
    }
}
