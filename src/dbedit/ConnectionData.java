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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.util.Properties;

public class ConnectionData implements Comparable, Cloneable {

    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    public static final String IBM_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    public static final String DATADIRECT_DRIVER = "com.ddtek.jdbc.db2.DB2Driver";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    private String name;
    private String url;
    private String user;
    private String password;
    private String driver;

    private Connection connection;
    private ResultSet resultSet;

    private String defaultOwner;

    public ConnectionData() {
    }

    public ConnectionData(String newName, String newUrl, String newUser,
                          String newPassword, String newDriver, String newDefaultOwner) {
        this.name = newName;
        this.url = newUrl;
        this.user = newUser;
        this.password = newPassword;
        this.driver = newDriver;
        this.defaultOwner = newDefaultOwner;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String newUrl) {
        this.url = newUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String newUser) {
        this.user = newUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String newDriver) {
        this.driver = newDriver;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet newResultSet) {
        this.resultSet = newResultSet;
    }

    public String getDefaultOwner() {
        return defaultOwner;
    }

    public void setDefaultOwner(String newDefaultOwner) {
        this.defaultOwner = newDefaultOwner;
    }

    public void connect() throws Exception {
        ClassLoader classLoader = new URLClassLoader(retrieveAllJars());
        Driver driverInstance = (Driver) Class.forName(this.driver, true, classLoader).newInstance();
        Properties originalProperties = new Properties();
        originalProperties.setProperty("user", user);
        originalProperties.setProperty("password", password);
        Properties properties = new Properties(originalProperties);
        addExtraProperties(properties);

        try {
            connection = driverInstance.connect(url, properties);
        } catch (Exception e) {
            ExceptionDialog.hideException(e);
            connection = driverInstance.connect(url, originalProperties);
        }
        if (connection == null) {
            throw new Exception("Unable to connect.\nURL = " + url + "\nDriver = " + driver);
        }
        connection.setAutoCommit(false);
    }

    private void addExtraProperties(Properties properties) {
        if (isOracle()) {
            addProperty(properties, "v$session.program", DBEdit.APPLICATION_NAME);
        } else if (isIbm()) {
            addProperty(properties, "clientProgramName", DBEdit.APPLICATION_NAME);
            addProperty(properties, "retrieveMessagesFromServerOnGetMessage", "true");
        } else if (isDataDirect()) {
            addProperty(properties, "applicationName", DBEdit.APPLICATION_NAME);
            addProperty(properties, "connectionRetryCount", "0");
        } else if (isHSQLDB()) {
            addProperty(properties, "shutdown", "true");
        }

    }

    private void addProperty(Properties properties, String name, String value) {
        if (!url.toLowerCase().contains(name.toLowerCase())) {
            properties.setProperty(name, value);
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

    public boolean isOracle() {
        return ORACLE_DRIVER.equals(driver);
    }

    public boolean isIbm() {
        return IBM_DRIVER.equals(driver);
    }

    public boolean isDataDirect() {
        return DATADIRECT_DRIVER.equals(driver);
    }

    public boolean isHSQLDB() {
        return HSQLDB_DRIVER.equals(driver);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        ConnectionData connectionData = (ConnectionData) o;
        return name.compareTo(connectionData.name);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
