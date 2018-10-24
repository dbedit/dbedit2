/**
 * DBEdit 2
 * Copyright (C) 2006-2008 Jef Van Den Ouweland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
        panel.add(new JLabel(), c);
        panel.add(new JLabel(Config.getVersion()), c);
        c.gridy++;
        panel.add(new JLabel(), c);
        panel.add(new JLabel("Copyright (C) 2006-2008"), c);
        c.gridy++;
        panel.add(new JLabel("Author: "), c);
        panel.add(new JLabel("Jef Van Den Ouweland"), c);
        c.gridy++;
        panel.add(new JLabel("License: "), c);
        JLabel link = new JLabel("GNU General Public License");
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(this);
        panel.add(link, c);
        c.gridy++;
        panel.add(new JLabel("Home page: "), c);
        link = new JLabel(Config.HOME_PAGE);
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(this);
        panel.add(link, c);
        c.gridy++;
        panel.add(new JLabel("Java VM: "), c);
        panel.add(new JLabel(System.getProperty("java.version")), c);
        if (getConnectionData() != null) {
            try {
                DatabaseMetaData metaData = getConnectionData().getConnection().getMetaData();
                c.gridy++;
                panel.add(new JLabel("Database: "), c);
                panel.add(new JLabel(metaData.getDatabaseProductName()), c);
                c.gridy++;
                panel.add(new JLabel(""), c);
                String databaseProductVersion = metaData.getDatabaseProductVersion().replaceAll("\n", "<br>");
                panel.add(new JLabel("<html>" + databaseProductVersion + "</html>"), c);
                c.gridy++;
                panel.add(new JLabel("Driver: "), c);
                panel.add(new JLabel(metaData.getDriverName()), c);
                c.gridy++;
                panel.add(new JLabel(""), c);
                panel.add(new JLabel(metaData.getDriverVersion()), c);
            } catch (Throwable t) {
                ExceptionDialog.hideException(t);
            }
        }
        PLUGIN.customizeAboutPanel(panel, c);
        panel.setBorder(new BevelBorder(BevelBorder.RAISED) {
            private Insets insets = new Insets(42, 10, 10, 10);
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
                if (label.getText().startsWith("GNU")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStream in = Config.class.getResourceAsStream("/license.txt");
                    byte[] bytes = new byte[1024];
                    int length = in.read(bytes);
                    while (length != -1) {
                        out.write(bytes, 0, length);
                        length = in.read(bytes);
                    }
                    in.close();
                    JTextArea textArea = new JTextArea(new String(out.toByteArray()));
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width
                            + scrollPane.getVerticalScrollBar().getPreferredSize().width, 400));
                    JOptionPane.showMessageDialog(ApplicationPanel.getInstance(), scrollPane, "License",
                            JOptionPane.DEFAULT_OPTION);
                } else {
                    openURL(label.getText());
                }
            } catch (Exception e1) {
                ExceptionDialog.showException(e1);
            }
        } else {
            ((JDialog) e.getSource()).setVisible(false);
        }
    }
}
