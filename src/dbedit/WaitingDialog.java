/*
 * DBEdit 2
 * Copyright (C) 2006-2010 Jef Van Den Ouweland
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
package dbedit;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingDialog extends TimerTask {

    private JLabel message1 = new JLabel();
    private JLabel message2 = new JLabel();
    private JDialog dialog;
    private long startTime = System.currentTimeMillis();

    public WaitingDialog(final Runnable onCancel) throws InterruptedException {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(message1);
        panel.add(message2);
        final Dialog pane = new Dialog(panel, Dialog.PLAIN_MESSAGE, Dialog.DEFAULT_OPTION,
                new Object[] {"Cancel"}, "Cancel");
        dialog = pane.createDialog(ApplicationPanel.getInstance(), null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                dialog.setVisible(true);
                if (onCancel != null && "Cancel".equals(pane.getValue())) {
                    onCancel.run();
                }
            }
        }).start();
        // wait
        boolean visible = false;
        while (!visible) {
            // x64 systems hang without this Thread.sleep(20)
            Thread.sleep(20);
            visible = dialog.isVisible();
        }
        new Timer().schedule(this, 3000, 1000);
    }

    public void setText(String text) {
        message1.setText(text);
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    public void hide() {
        dialog.dispose();
        cancel();
    }

    public String getExecutionTime() {
        long executionTime = (System.currentTimeMillis() - startTime) / 1000;
        long hours = executionTime / 60 / 60;
        long minutes = executionTime / 60 % 60;
        long seconds = executionTime % 60;
        String text = hours == 0 ? "" : hours + (hours == 1 ? " hour " : " hours ");
        text += minutes == 0 ? "" : minutes + (minutes == 1 ? " minute " : " minutes ");
        text += seconds + (seconds == 1 ? " second" : " seconds");
        return text;
    }

    @Override
    public void run() {
        message2.setText(getExecutionTime());
    }
}
