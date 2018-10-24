package dbedit.actions;

import dbedit.ApplicationPanel;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

public abstract class LobAbstractAction extends CustomAction {

    protected static final JFileChooser FILE_CHOOSER = new JFileChooser();

    protected LobAbstractAction(String name, String icon, KeyStroke accelerator) {
        super(name, icon, accelerator);
    }

    protected void exportLob(File file) throws IOException, SQLException {
        Object lob = ApplicationPanel.getInstance().getTableValue();
        byte[] bytes;
        if (lob instanceof Blob) {
            bytes = ((Blob) lob).getBytes(1, (int) ((Blob) lob).length());
        } else if (lob instanceof Clob) {
            bytes = ((Clob) lob).getSubString(1, (int) ((Clob) lob).length()).getBytes();
        } else if (lob instanceof byte[]) {
            bytes = (byte[]) lob;
        } else {
            return;
        }
        FILE_CHOOSER.setSelectedFile(file);
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }
}
