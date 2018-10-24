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

    public ConnectionData(String name, String url, String user, String password, String driver, String defaultOwner) {
        this.name = name;
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        this.defaultOwner = defaultOwner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public String getDefaultOwner() {
        return defaultOwner;
    }

    public void setDefaultOwner(String defaultOwner) {
        this.defaultOwner = defaultOwner;
    }

    public void connect() throws Exception {
        ClassLoader classLoader = new URLClassLoader(retrieveAllJars());
        Driver driver = (Driver) Class.forName(this.driver, true, classLoader).newInstance();
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        if (isOracle()) {
            // set application name
            properties.setProperty("v$session.program", DBEdit.APPLICATION_NAME);
        } else if (isIbm()) {
            // set application name
            properties.setProperty("clientProgramName", DBEdit.APPLICATION_NAME);
        } else if (isDataDirect()) {
            // Workaround for a bug in datadirect driver where default connectionRetryCount = 5
            properties.setProperty("connectionRetryCount", "0");
        }

        connection = driver.connect(url, properties);
        if (isDataDirect()) {
            // set application name
            // http://media.datadirect.com/download/docs/jdbc/jdbcref/extensions.html
            // it doesn't work: "JDBC4DB2" is sent hard coded
//            connection.getClass().getMethod("setClientApplicationName", new Class[] {String.class})
//                    .invoke(connection, new Object[] {DBEdit.APPLICATION_NAME});
        }
        connection.setAutoCommit(false);
    }

    private static URL[] retrieveAllJars() throws MalformedURLException {
        File[] files = new File(".").listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".zip");
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

    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        ConnectionData connectionData = (ConnectionData) o;
        return name.compareTo(connectionData.name);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
