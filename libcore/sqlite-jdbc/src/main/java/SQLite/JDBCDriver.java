package SQLite;

import java.sql.*;
import java.util.Properties;

public class JDBCDriver implements java.sql.Driver {

    public static final int MAJORVERSION = 1;
    public static final int MINORVERSION = 2;

    private static java.lang.reflect.Constructor makeConn = null;

    protected Connection conn;

    static {
    try {
        Class connClass = null;
        Class args[] = new Class[2];
        args[0] = Class.forName("java.lang.String");
        args[1] = args[0];
        String jvers = java.lang.System.getProperty("java.version");
        String cvers;
        if (jvers == null || jvers.startsWith("1.0")) {
        throw new java.lang.Exception("unsupported java version");
        } else if (jvers.startsWith("1.1")) {
        cvers = "SQLite.JDBC1.JDBCConnection";
        } else if (jvers.startsWith("1.2") || jvers.startsWith("1.3")) {
        cvers = "SQLite.JDBC2.JDBCConnection";
        } else if (jvers.startsWith("1.4")) {
        cvers = "SQLite.JDBC2x.JDBCConnection";
        } else if (jvers.startsWith("1.5")) {
        cvers = "SQLite.JDBC2y.JDBCConnection";
        try {
            Class.forName(cvers);
        } catch (java.lang.Exception e) {
            cvers = "SQLite.JDBC2x.JDBCConnection";
        }
        } else {
        cvers = "SQLite.JDBC2z.JDBCConnection";
        try {
            Class.forName(cvers);
        } catch (java.lang.Exception e) {
            cvers = "SQLite.JDBC2y.JDBCConnection";
            try {
            Class.forName(cvers);
            } catch (java.lang.Exception ee) {
            cvers = "SQLite.JDBC2x.JDBCConnection";
            }
        }
        }
        connClass = Class.forName(cvers);
        makeConn = connClass.getConstructor(args);
        java.sql.DriverManager.registerDriver(new JDBCDriver());
    } catch (java.lang.Exception e) {
        System.err.println(e);
    }
    }

    public JDBCDriver() {
    }
    
    public boolean acceptsURL(String url) throws SQLException {
    return url.startsWith("sqlite:/") ||
        url.startsWith("jdbc:sqlite:/");
    }

    public Connection connect(String url, Properties info)
    throws SQLException {
    if (!acceptsURL(url)) {
        return null;
    }
    Object args[] = new Object[2];
    args[0] = url;
    if (info != null) {
        args[1] = info.getProperty("encoding");
    }
    if (args[1] == null) {
        args[1] = java.lang.System.getProperty("SQLite.encoding");
    }
    try {
        conn = (Connection) makeConn.newInstance(args);
    } catch (java.lang.reflect.InvocationTargetException ie) {
        throw new SQLException(ie.getTargetException().toString());
    } catch (java.lang.Exception e) {
        throw new SQLException(e.toString());
    }
    return conn;
    }

    public int getMajorVersion() {
    return MAJORVERSION;
    }

    public int getMinorVersion() {
    return MINORVERSION;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
    throws SQLException {
    DriverPropertyInfo p[] = new DriverPropertyInfo[1];
    DriverPropertyInfo pp = new DriverPropertyInfo("encoding", "");
    p[0] = pp;
    return p;
    }

    public boolean jdbcCompliant() {
    return false;
    }
}
