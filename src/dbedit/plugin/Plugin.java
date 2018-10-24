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

    void customizeConnectionPanel(final JPanel panel, GridBagConstraints c, final ConnectionData connectionData)
            throws Exception;
}
