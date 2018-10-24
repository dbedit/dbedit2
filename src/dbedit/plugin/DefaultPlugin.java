package dbedit.plugin;

import dbedit.ApplicationMenuBar;
import dbedit.ConnectionData;

import javax.swing.*;
import java.awt.*;

public class DefaultPlugin implements Plugin {

    public void audit(String sql) throws Exception {
    }

    public String analyzeException(String exception) {
        return null;
    }

    public void checkForUpdate(final ApplicationMenuBar menuBar) {
    }

    public void customizeAboutPanel(JPanel panel, GridBagConstraints c) {
    }

    public void customizeConnectionPanel(final JPanel panel, GridBagConstraints c, final ConnectionData connectionData)
            throws Exception {
    }
}
