package dbedit.plugin;

import dbedit.ApplicationMenuBar;
import dbedit.ConnectionData;

import javax.swing.*;
import java.awt.*;

public interface Plugin {

    void audit(String sql) throws Exception;

    String analyzeException(String exception);

    void checkForUpdate(final ApplicationMenuBar menuBar);

    void customizeAboutPanel(JPanel panel, GridBagConstraints c);

    void customizeConnectionPanel(final JPanel panel, GridBagConstraints c, final ConnectionData connectionData) throws Exception;
}
