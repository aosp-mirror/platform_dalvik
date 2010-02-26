package SQLite.JDBC2y;

import java.sql.*;
import java.util.*;

public class JDBCConnection
    implements java.sql.Connection, SQLite.BusyHandler {

    /**
     * Open database.
     */
    protected DatabaseX db;

    /**
     * Database URL.
     */
    protected String url;

    /**
     * Character encoding.
     */
    protected String enc;

    /**
     * Autocommit flag, true means autocommit.
     */
    protected boolean autocommit = true;

    /**
     * In-transaction flag.
     * Can be true only when autocommit false.
     */
    protected boolean intrans = false;

    /**
     * Timeout for Database.exec()
     */
    protected int timeout = 1000000;

    /**
     * File name of database.
     */
    private String dbfile = null;

    /**
     * Reference to meta data or null.
     */
    private JDBCDatabaseMetaData meta = null;

    /**
     * Base time value for timeout handling.
     */
    private long t0;

    /**
     * Database in readonly mode.
     */
    private boolean readonly = false;


    private boolean busy0(DatabaseX db, int count) {
    if (count <= 1) {
        t0 = System.currentTimeMillis();
    }
    if (db != null) {
        long t1 = System.currentTimeMillis();
        if (t1 - t0 > timeout) {
        return false;
        }
        db.wait(100);
        return true;
    }
    return false;
    }

    public boolean busy(String table, int count) {
    return busy0(db, count);
    }

    protected boolean busy3(DatabaseX db, int count) {
    if (count <= 1) {
        t0 = System.currentTimeMillis();
    }
    if (db != null) {
        long t1 = System.currentTimeMillis();
        if (t1 - t0 > timeout) {
        return false;
        }
        return true;
    }
    return false;
    }

    private DatabaseX open(boolean readonly) throws SQLException {
    DatabaseX db = null;
    try {
        db = new DatabaseX();
        db.open(dbfile, readonly ? 0444 : 0644);
        db.set_encoding(enc);
    } catch (SQLite.Exception e) {
        throw new SQLException(e.toString());
    }
    int loop = 0;
    while (true) {
        try {
        db.exec("PRAGMA short_column_names = off;", null);
        db.exec("PRAGMA full_column_names = on;", null);
        db.exec("PRAGMA empty_result_callbacks = on;", null);
        if (SQLite.Database.version().compareTo("2.6.0") >= 0) {
            db.exec("PRAGMA show_datatypes = on;", null);
        }
        } catch (SQLite.Exception e) {
        if (db.last_error() != SQLite.Constants.SQLITE_BUSY ||
            !busy0(db, ++loop)) {
            try {
            db.close();
            } catch (SQLite.Exception ee) {
            }
            throw new SQLException(e.toString());
        }
        continue;
        }
        break;
    }
    return db;
    }

    public JDBCConnection(String url, String enc) throws SQLException {
    if (url.startsWith("sqlite:/")) {
        dbfile = url.substring(8);
    } else if (url.startsWith("jdbc:sqlite:/")) {
        dbfile = url.substring(13);
    } else {
        throw new SQLException("unsupported url");
    }
    this.url = url;
    this.enc = enc;
    try {
        db = open(readonly);
        db.busy_handler(this);
    } catch (SQLException e) {
        if (db != null) {
        try {
            db.close();
        } catch (SQLite.Exception ee) {
        }
        }
        throw e;
    }
    }

    /* non-standard */
    public SQLite.Database getSQLiteDatabase() {
    return (SQLite.Database) db;
    }
  
    public Statement createStatement() {
    JDBCStatement s = new JDBCStatement(this);
    return s;
    }
  
    public Statement createStatement(int resultSetType,
                     int resultSetConcurrency)
    throws SQLException {
    JDBCStatement s = new JDBCStatement(this);
    return s;
    }
    
    public DatabaseMetaData getMetaData() throws SQLException {
    if (meta == null) {
        meta = new JDBCDatabaseMetaData(this);
    }
    return meta;
    }

    public void close() throws SQLException {
    try {
        rollback();
    } catch (SQLException e) {
        /* ignored */
    }
    intrans = false;
    if (db != null) {
        try {
        db.close();
        db = null;
        } catch (SQLite.Exception e) {
        throw new SQLException(e.toString());
        }
    }
    }

    public boolean isClosed() throws SQLException {
    return db == null;
    }

    public boolean isReadOnly() throws SQLException {
    return readonly;
    }

    public void clearWarnings() throws SQLException {
    }

    public void commit() throws SQLException {
    if (db == null) {
        throw new SQLException("stale connection");
    }
    if (!intrans) {
        return;
    }
    try {
        db.exec("COMMIT", null);
        intrans = false;
    } catch (SQLite.Exception e) {
        throw new SQLException(e.toString());
    }
    }

    public boolean getAutoCommit() throws SQLException {
    return autocommit;
    }

    public String getCatalog() throws SQLException {
    return null;
    }

    public int getTransactionIsolation() throws SQLException {
    return TRANSACTION_SERIALIZABLE;
    }

    public SQLWarning getWarnings() throws SQLException {
    return null;
    }

    public String nativeSQL(String sql) throws SQLException {
    throw new SQLException("not supported");
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
    throw new SQLException("not supported");
    }

    public CallableStatement prepareCall(String sql, int x, int y)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
    JDBCPreparedStatement s = new JDBCPreparedStatement(this, sql);
    return s;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                          int resultSetConcurrency)
    throws SQLException {
    JDBCPreparedStatement s = new JDBCPreparedStatement(this, sql);
    return s;
    }

    public void rollback() throws SQLException {
    if (db == null) {
        throw new SQLException("stale connection");
    }
    if (!intrans) {
        return;
    }
    try {
        db.exec("ROLLBACK", null);
        intrans = false;
    } catch (SQLite.Exception e) {
        throw new SQLException(e.toString());
    }
    }

    public void setAutoCommit(boolean ac) throws SQLException {
    if (ac && intrans && db != null) {
        try {
        db.exec("ROLLBACK", null);
        } catch (SQLite.Exception e) {
        throw new SQLException(e.toString());
        }
    }
    intrans = false;
    autocommit = ac;
    }

    public void setCatalog(String catalog) throws SQLException {
    }

    public void setReadOnly(boolean ro) throws SQLException {
    if (intrans) {
        throw new SQLException("incomplete transaction");
    }
    if (ro != readonly) {
        DatabaseX db = null;
        try {
        db = open(ro);
        this.db.close();
        this.db = db;
        db = null;
        readonly = ro;
        } catch (SQLException e) {
        throw e;
        } catch (SQLite.Exception ee) {
        if (db != null) {
            try {
            db.close();
            } catch (SQLite.Exception eee) {
            }
        }
        throw new SQLException(ee.toString());
        }
    }
    }

    public void setTransactionIsolation(int level) throws SQLException {
    if (level != TRANSACTION_SERIALIZABLE) {
        throw new SQLException("not supported");
    }
    }

    public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
    throw new SQLException("not supported");
    }

    public void setTypeMap(java.util.Map map) throws SQLException {
    throw new SQLException("not supported");
    }
  
    public int getHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    public void setHoldability(int holdability) throws SQLException {
    if (holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
        return;
    }
    throw new SQLException("not supported");
    }

    public Savepoint setSavepoint() throws SQLException {
    throw new SQLException("not supported");
    }

    public Savepoint setSavepoint(String name) throws SQLException {
    throw new SQLException("not supported");
    }

    public void rollback(Savepoint x) throws SQLException {
    throw new SQLException("not supported");
    }

    public void releaseSavepoint(Savepoint x) throws SQLException {
    throw new SQLException("not supported");
    }

    public Statement createStatement(int resultSetType,
                     int resultSetConcurrency,
                     int resultSetHoldability)
    throws SQLException {
    if (resultSetHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
        throw new SQLException("not supported");
    }
    return createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                          int resultSetConcurrency,
                          int resultSetHoldability)
    throws SQLException {
    if (resultSetHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
        throw new SQLException("not supported");
    }
    return prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int x, int y, int z)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public PreparedStatement prepareStatement(String sql, int autokeys)
    throws SQLException {
    if (autokeys != Statement.NO_GENERATED_KEYS) {
        throw new SQLException("not supported");
    }
    return prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(String sql, int colIndexes[])
    throws SQLException {
    throw new SQLException("not supported");
    }

    public PreparedStatement prepareStatement(String sql, String columns[])
    throws SQLException {
    throw new SQLException("not supported");
    }

}

class DatabaseX extends SQLite.Database {

    static Object lock = new Object();

    public DatabaseX() {
    super();
    }

    void wait(int ms) {
    try {
        synchronized (lock) {
        lock.wait(ms);
        }
    } catch (java.lang.Exception e) {
    }
    }

    public void exec(String sql, SQLite.Callback cb)
    throws SQLite.Exception {
    super.exec(sql, cb);
    synchronized (lock) {
        lock.notifyAll();
    }
    }

    public void exec(String sql, SQLite.Callback cb, String args[])
    throws SQLite.Exception {
    super.exec(sql, cb, args);
    synchronized (lock) {
        lock.notifyAll();
    }
    }

    public SQLite.TableResult get_table(String sql, String args[])
    throws SQLite.Exception {
    SQLite.TableResult ret = super.get_table(sql, args);
    synchronized (lock) {
        lock.notifyAll();
    }
    return ret;
    }

    public void get_table(String sql, String args[], SQLite.TableResult tbl)
    throws SQLite.Exception {
    super.get_table(sql, args, tbl);
    synchronized (lock) {
        lock.notifyAll();
    }
    }

}
