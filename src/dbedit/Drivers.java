/*
 * DBEdit 2
 * Copyright (C) 2006-2011 Jef Van Den Ouweland
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
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Scanner;

public final class Drivers {

    private static boolean initialized = false;

    private Drivers() {
    }

    public static void initialize() throws Exception {
        if (!initialized) {
            addAllJarsToClasspath();
            loadCustomDrivers();
            initialized = true;
        }
    }

    private static void addAllJarsToClasspath() throws Exception {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        URL[] urls = retrieveAllJars();
        for (URL url1 : urls) {
            addURL.invoke(systemClassLoader, url1);
        }
    }

    private static URL[] retrieveAllJars() throws MalformedURLException {
        File[] files = new File(".").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                return fileName.toLowerCase().endsWith(".jar") || fileName.toLowerCase().endsWith(".zip");
            }
        });
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }

    private static void loadCustomDrivers() throws Exception {
        String drivers = Config.getDrivers();
        drivers += ",org.sqlite.JDBC,net.sourceforge.jtds.jdbc.Driver";
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(drivers);
        scanner.useDelimiter("[\\s,;]+");
        while (scanner.hasNext()) {
            String driver = scanner.next();
            if (stringBuilder.indexOf(driver) == -1) {
                try {
                    Class.forName(driver);
                    stringBuilder.append(driver);
                    stringBuilder.append("\n");
                } catch (ClassNotFoundException e) {
                    ExceptionDialog.hideException(e);
                }
            }
        }
        Config.saveDrivers(stringBuilder.toString());
    }

    public static void editDrivers() throws Exception {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy++;
        c.gridwidth = 3;
        panel.add(new JLabel(String.format("The JDBC driver jar files are located in \"%s\".",
                new File(".").getCanonicalPath())), c);
        c.gridy++;
        panel.add(new JLabel("Most drivers are automatically detected."), c);
        c.gridy++;
        panel.add(new JLabel(" "), c);
        c.gridy++;
        panel.add(new JSeparator(), c);
        c.gridy++;
        panel.add(new JLabel(" "), c);
        c.gridy++;
        panel.add(new JLabel("Currently loaded drivers:"), c);
        c.gridy++;
        panel.add(new JLabel(" "), c);
        c.gridwidth = 1;
        Enumeration<Driver> loadedDrivers = DriverManager.getDrivers();
        while (loadedDrivers.hasMoreElements()) {
            Driver loadedDriver = loadedDrivers.nextElement();
            c.gridy++;
            c.gridx = 0;
            panel.add(new JLabel(loadedDriver.getClass().getName()), c);
            c.gridx++;
            panel.add(new JLabel(String.format("v%d.%d",
                    loadedDriver.getMajorVersion(), loadedDriver.getMinorVersion())), c);
            c.gridx++;
            try {
                URI uri = loadedDriver.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
                String path = new File(uri).getName();
                panel.add(new JLabel(path), c);
            } catch (Exception e) {
                panel.add(new JLabel(), c);
            }
        }
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel(" "), c);
        c.gridy++;
        panel.add(new JSeparator(), c);
        c.gridy++;
        panel.add(new JLabel(" "), c);
        c.gridy++;
        panel.add(new JLabel("Add any driver that couldn't automatically be loaded to the list below,"
                + " separated by a comma or a new line."), c);
        c.gridy++;
        panel.add(new JLabel(" "), c);
        c.gridy++;
        String drivers = Config.getDrivers();
        JTextArea driverfield = new JTextArea(drivers, 4, 0);
        panel.add(new JScrollPane(driverfield), c);
        if (Dialog.OK_OPTION  == Dialog.show("Drivers", panel, Dialog.PLAIN_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
            Config.saveDrivers(driverfield.getText());
            loadCustomDrivers();
        }
    }
}
