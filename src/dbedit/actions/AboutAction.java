/*
 * DBEdit 2
 * Copyright (C) 2006-2012 Jef Van Den Ouweland
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

import dbedit.*;
import dbedit.Dialog;

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

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image background = new ImageIcon(AboutAction.class.getResource("/icons/logo.png")).getImage();
                g.drawImage(background, 5, 17, null);
                g.setColor(getBackground());
                g.fillRect(getWidth() - 5, 0, 5, background.getHeight(null) + 17);
            }
        };
        panel.setBackground(new Color(226, 226, 226));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.SOUTHWEST;
        panel.add(new JLabel(String.format(
                "<html><font style=\"font-weight:bold\">DBEdit %s</font></html>", Config.getVersion())), c);
        c.gridy++;
        panel.add(new JLabel(
                "<html><font style=\"color:gray\">Copyright © 2006-2012 Jef Van Den Ouweland</font></html>"), c);
        c.gridy++;
        JLabel link = new JLabel("GNU General Public License");
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(this);
        panel.add(link, c);
        c.gridy++;
        link = new JLabel(Config.HOME_PAGE);
        link.setForeground(Color.BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(this);
        panel.add(link, c);
        c.gridy++;
        c.gridwidth = 1;
        panel.add(new JLabel("Java VM: "), c);
        panel.add(new JLabel(System.getProperty("java.version")), c);
        if (Context.getInstance().getConnectionData() != null) {
            try {
                DatabaseMetaData metaData = Context.getInstance().getConnectionData().getConnection().getMetaData();
                c.gridy++;
                panel.add(new JLabel("Database: "), c);
                panel.add(new JLabel(metaData.getDatabaseProductName()), c);
                c.gridy++;
                panel.add(new JLabel(""), c);
                String databaseProductVersion = metaData.getDatabaseProductVersion().replaceAll("\n", "<br>");
                panel.add(new JLabel(String.format("<html>%s</html>", databaseProductVersion)), c);
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
        panel.setBorder(new BevelBorder(BevelBorder.RAISED) {
            private Insets insets = new Insets(65, 10, 10, 10);
            @Override
            public Insets getBorderInsets(Component c) {
                return insets;
            }
        });
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(ApplicationPanel.getInstance()), true);
        dialog.setUndecorated(true);
        dialog.addMouseListener(this);
        dialog.getContentPane().add(panel);
        dialog.setSize(panel.getPreferredSize());
        dialog.setMinimumSize(new Dimension(395, 0));
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    @Override
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
                    dbedit.Dialog.show("License", scrollPane, Dialog.PLAIN_MESSAGE, Dialog.DEFAULT_OPTION);
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
