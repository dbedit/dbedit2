/*
 * DBEdit 2
 * Copyright (C) 2006-2009 Jef Van Den Ouweland
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
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        if (isOracle()) {
            // set application name
            properties.setProperty("v$session.program", DBEdit.APPLICATION_NAME);
        } else if (isIbm()) {
            // set application name
            properties.setProperty("clientProgramName", DBEdit.APPLICATION_NAME);
            if (!url.contains("retrieveMessagesFromServerOnGetMessage")) {
                properties.setProperty("retrieveMessagesFromServerOnGetMessage", "true");
            }
        } else if (isDataDirect()) {
            // Workaround for a bug in datadirect driver where default connectionRetryCount = 5
            properties.setProperty("connectionRetryCount", "0");
        }

        connection = driverInstance.connect(url, properties);
//        if (isDataDirect()) {
            // set application name
            // http://media.datadirect.com/download/docs/jdbc/jdbcref/extensions.html
            // it doesn't work: "JDBC4DB2" is sent hard coded
//            connection.getClass().getMethod("setClientApplicationName", new Class[] {String.class})
//                    .invoke(connection, new Object[] {DBEdit.APPLICATION_NAME});
//        }
        connection.setAutoCommit(false);
    }

    private static URL[] retrieveAllJars() throws MalformedURLException {
        File[] files = new File(".").listFiles(new FilenameFilter() {
            public boolean accept(File dir, String fileName) {
                return fileName.toLowerCase().endsWith(".jar") || fileName.toLowerCase().endsWith(".zip");
            }
        });
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURL();
        }
        return urls;
    }

    public boolean isOracle() {
        return ORACLE_DRIVER.equals(driver);
    }

    public boolean isDb2() {
        return isIbm() || isDataDirect();
    }

    public boolean isIbm() {
        return IBM_DRIVER.equals(driver);
    }

    public boolean isDataDirect() {
        return DATADIRECT_DRIVER.equals(driver);
    }

    public boolean isMySql() {
        return MYSQL_DRIVER.equals(driver);
    }

    public boolean isHSQLDB() {
        return HSQLDB_DRIVER.equals(driver);
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        ConnectionData connectionData = (ConnectionData) o;
        return name.compareTo(connectionData.name);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
