package SQLite.JDBC2y;

import java.sql.*;
import java.math.BigDecimal;

public class JDBCResultSet implements java.sql.ResultSet {

    /**
     * Current row to be retrieved.
     */
    private int row;

    /**
     * Table returned by Database.get_table()
     */
    protected SQLite.TableResult tr;

    /**
     * Statement from which result set was produced.
     */
    private JDBCStatement s;

    /**
     * Meta data for result set or null.
     */
    private JDBCResultSetMetaData m;

    /**
     * Last result cell retrieved or null.
     */
    private String lastg;


    public JDBCResultSet(SQLite.TableResult tr, JDBCStatement s) {
    this.tr = tr;
    this.s = s;
    this.m = null;
    this.lastg = null;
    this.row = -1;
    }

    public boolean next() throws SQLException {
    if (tr == null) {
        return false;
    }
    row++;
    return row < tr.nrows;
    }

    public int findColumn(String columnName) throws SQLException {
    JDBCResultSetMetaData m = (JDBCResultSetMetaData) getMetaData();
    return m.findColByName(columnName);
    }
  
    public int getRow() throws SQLException {
    if (tr == null) {
        throw new SQLException("no rows");
    }
    return row + 1;
    }

    public boolean previous() throws SQLException {
    if (tr == null) {
        return false;
    }
    if (row >= 0) {
        row--;
    }
    return row >= 0;
    }

    public boolean absolute(int row) throws SQLException {
    if (tr == null) {
        return false;
    }
    if (row < 0) {
        row = tr.nrows + 1 + row;
    }
    row--;
    if (row < 0 || row > tr.nrows) {
        return false;
    }
    this.row = row;
    return true;
    }

    public boolean relative(int row) throws SQLException {
    if (tr == null) {
        return false;
    }
    if (this.row + row < 0 || this.row + row >= tr.nrows) {
        return false;
    }
    this.row += row;
    return true;
    }

    public void setFetchDirection(int dir) throws SQLException {
    throw new SQLException("not supported");
    }

    public int getFetchDirection() throws SQLException {
    throw new SQLException("not supported");
    }

    public void setFetchSize(int fsize) throws SQLException {
    throw new SQLException("not supported");
    }

    public int getFetchSize() throws SQLException {
    throw new SQLException("not supported");
    }

    public String getString(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    return lastg;
    }

    public String getString(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getString(col);
    }

    public int getInt(int columnIndex) throws SQLException {
    Integer i = internalGetInt(columnIndex);
    if (i != null) {
        return i.intValue();
    }
    return 0;
    }

    private Integer internalGetInt(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return Integer.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public int getInt(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getInt(col);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public boolean getBoolean(String columnName) throws SQLException {
    throw new SQLException("not supported");
    }

    public ResultSetMetaData getMetaData() throws SQLException {
    if (m == null) {
        m = new JDBCResultSetMetaData(this);
    }
    return m;
    }

    public short getShort(int columnIndex) throws SQLException {
    Short s = internalGetShort(columnIndex);
    if (s != null) {
        return s.shortValue();
    }
    return 0;
    }

    private Short internalGetShort(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return Short.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public short getShort(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getShort(col);
    }

    public java.sql.Time getTime(int columnIndex) throws SQLException {
    return internalGetTime(columnIndex, null);
    }

    private java.sql.Time internalGetTime(int columnIndex,
                      java.util.Calendar cal)
    throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return java.sql.Time.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public java.sql.Time getTime(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getTime(col);
    }

    public java.sql.Time getTime(int columnIndex, java.util.Calendar cal)
    throws SQLException {
    return internalGetTime(columnIndex, cal);
    }

    public java.sql.Time getTime(String columnName, java.util.Calendar cal)
    throws SQLException{
    int col = findColumn(columnName);
    return getTime(col, cal);
    }

    public java.sql.Timestamp getTimestamp(int columnIndex)
    throws SQLException{
    return internalGetTimestamp(columnIndex, null);
    }

    private java.sql.Timestamp internalGetTimestamp(int columnIndex,
                            java.util.Calendar cal)
    throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return java.sql.Timestamp.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public java.sql.Timestamp getTimestamp(String columnName)
    throws SQLException{
    int col = findColumn(columnName);
    return getTimestamp(col);
    }

    public java.sql.Timestamp getTimestamp(int columnIndex,
                       java.util.Calendar cal)
    throws SQLException {
    return internalGetTimestamp(columnIndex, cal);
    }

    public java.sql.Timestamp getTimestamp(String columnName,
                       java.util.Calendar cal)
    throws SQLException {
    int col = findColumn(columnName);
    return getTimestamp(col, cal);
    }

    public java.sql.Date getDate(int columnIndex) throws SQLException {
    return internalGetDate(columnIndex, null);
    }

    private java.sql.Date internalGetDate(int columnIndex,
                      java.util.Calendar cal)
    throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return java.sql.Date.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public java.sql.Date getDate(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getDate(col);
    }

    public java.sql.Date getDate(int columnIndex, java.util.Calendar cal)
    throws SQLException{
    return internalGetDate(columnIndex, cal);
    }

    public java.sql.Date getDate(String columnName, java.util.Calendar cal)
    throws SQLException{
    int col = findColumn(columnName);
    return getDate(col, cal);
    }

    public double getDouble(int columnIndex) throws SQLException {
    Double d = internalGetDouble(columnIndex);
    if (d != null) {
        return d.doubleValue();
    }
    return 0;
    }

    private Double internalGetDouble(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return  Double.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }
    
    public double getDouble(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getDouble(col);
    }

    public float getFloat(int columnIndex) throws SQLException {
    Float f = internalGetFloat(columnIndex);
    if (f != null) {
        return f.floatValue();
    }
    return 0;
    }

    private Float internalGetFloat(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return Float.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public float getFloat(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getFloat(col);
    }

    public long getLong(int columnIndex) throws SQLException {
    Long l = internalGetLong(columnIndex);
    if (l != null) {
        return l.longValue();
    }
    return 0;
    }

    private Long internalGetLong(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    try {
        return Long.valueOf(lastg);
    } catch (java.lang.Exception e) {
        lastg = null;
    }
    return null;
    }

    public long getLong(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getLong(col);
    }

    @Deprecated
    public java.io.InputStream getUnicodeStream(int columnIndex)
    throws SQLException {
    throw new SQLException("not supported");
    }

    @Deprecated
    public java.io.InputStream getUnicodeStream(String columnName)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public java.io.InputStream getAsciiStream(String columnName)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public java.io.InputStream getAsciiStream(int columnIndex)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public BigDecimal getBigDecimal(String columnName)
    throws SQLException {
    throw new SQLException("not supported");
    }

    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public java.io.InputStream getBinaryStream(int columnIndex)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public java.io.InputStream getBinaryStream(String columnName)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public byte getByte(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public byte getByte(String columnName) throws SQLException {
    throw new SQLException("not supported");
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    byte ret[] = null;
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    if (lastg != null) {
        ret = SQLite.StringEncoder.decode(lastg);
    }
    return ret;
    }

    public byte[] getBytes(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getBytes(col);
    }

    public String getCursorName() throws SQLException {
    return null;
    }

    public Object getObject(int columnIndex) throws SQLException {
    if (tr == null || columnIndex < 1 || columnIndex > tr.ncolumns) {
        throw new SQLException("column " + columnIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[columnIndex - 1];
    Object ret = lastg;
    if (tr instanceof TableResultX) {
        switch (((TableResultX) tr).sql_type[columnIndex - 1]) {
        case Types.SMALLINT:
        ret = internalGetShort(columnIndex);
        break;
        case Types.INTEGER:
        ret = internalGetInt(columnIndex);
        break;
        case Types.DOUBLE:
        ret = internalGetDouble(columnIndex);
        break;
        case Types.FLOAT:
        ret = internalGetFloat(columnIndex);
        break;
        case Types.BIGINT:
        ret = internalGetLong(columnIndex);
        break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        ret = getBytes(columnIndex);
        break;
        case Types.NULL:
        ret = null;
        break;
        /* defaults to String below */
        }
    }
    return ret;
    }

    public Object getObject(String columnName) throws SQLException {
    int col = findColumn(columnName);
    return getObject(col);
    }

    public Object getObject(int columnIndex, java.util.Map map) 
    throws SQLException {
    throw new SQLException("not supported");
    }

    public Object getObject(String columnIndex, java.util.Map map)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Ref getRef(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Ref getRef(String columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Blob getBlob(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Blob getBlob(String columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Clob getClob(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Clob getClob(String columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Array getArray(int columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.sql.Array getArray(String columnIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public java.io.Reader getCharacterStream(int columnIndex)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public java.io.Reader getCharacterStream(String columnName)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public SQLWarning getWarnings() throws SQLException {
    throw new SQLException("not supported");
    }

    public boolean wasNull() throws SQLException {
    return lastg == null;
    }
    
    public void clearWarnings() throws SQLException {
    throw new SQLException("not supported");
    }

    public boolean isFirst() throws SQLException {
    if (tr == null) {
        return true;
    }
    return row == 0;
    }

    public boolean isBeforeFirst() throws SQLException {
    if (tr == null || tr.nrows <= 0) {
        return false;
    }
    return row < 0;
    }

    public void beforeFirst() throws SQLException {
    if (tr == null) {
        return;
    }
    row = -1;
    }

    public boolean first() throws SQLException {
    if (tr == null || tr.nrows <= 0) {
        return false;
    }
    row = 0;
    return true;
    }

    public boolean isAfterLast() throws SQLException {
    if (tr == null || tr.nrows <= 0) {
        return false;
    }
    return row >= tr.nrows;
    }

    public void afterLast() throws SQLException {
    if (tr == null) {
        return;
    }
    row = tr.nrows;
    }

    public boolean isLast() throws SQLException {
    if (tr == null) {
        return true;
    }
    return row == tr.nrows - 1;
    }

    public boolean last() throws SQLException {
    if (tr == null || tr.nrows <= 0) {
        return false;
    }
    row = tr.nrows -1;
    return true;
    }

    public int getType() throws SQLException {
    return TYPE_SCROLL_INSENSITIVE;
    }

    public int getConcurrency() throws SQLException {
    return CONCUR_READ_ONLY;
    }

    public boolean rowUpdated() throws SQLException {
    throw new SQLException("not supported");
    }

    public boolean rowInserted() throws SQLException {
    throw new SQLException("not supported");
    }

    public boolean rowDeleted() throws SQLException {
    throw new SQLException("not supported");
    }

    public void insertRow() throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateRow() throws SQLException {
    throw new SQLException("not supported");
    }

    public void deleteRow() throws SQLException {
    throw new SQLException("not supported");
    }

    public void refreshRow() throws SQLException {
    throw new SQLException("not supported");
    }

    public void cancelRowUpdates() throws SQLException {
    throw new SQLException("not supported");
    }

    public void moveToInsertRow() throws SQLException {
    throw new SQLException("not supported");
    }

    public void moveToCurrentRow() throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateNull(int colIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBoolean(int colIndex, boolean b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateByte(int colIndex, byte b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateShort(int colIndex, short b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateInt(int colIndex, int b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateLong(int colIndex, long b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateFloat(int colIndex, float f) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateDouble(int colIndex, double f) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBigDecimal(int colIndex, BigDecimal f)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateString(int colIndex, String s) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBytes(int colIndex, byte[] s) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateDate(int colIndex, java.sql.Date d) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateTime(int colIndex, java.sql.Time t) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateTimestamp(int colIndex, java.sql.Timestamp t)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateAsciiStream(int colIndex, java.io.InputStream in, int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBinaryStream(int colIndex, java.io.InputStream in, int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateCharacterStream(int colIndex, java.io.Reader in, int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateObject(int colIndex, Object obj) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateObject(int colIndex, Object obj, int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateNull(String colIndex) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBoolean(String colIndex, boolean b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateByte(String colIndex, byte b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateShort(String colIndex, short b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateInt(String colIndex, int b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateLong(String colIndex, long b) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateFloat(String colIndex, float f) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateDouble(String colIndex, double f) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBigDecimal(String colIndex, BigDecimal f)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateString(String colIndex, String s) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBytes(String colIndex, byte[] s) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateDate(String colIndex, java.sql.Date d)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateTime(String colIndex, java.sql.Time t)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateTimestamp(String colIndex, java.sql.Timestamp t)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateAsciiStream(String colIndex, java.io.InputStream in,
                  int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBinaryStream(String colIndex, java.io.InputStream in,
                   int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateCharacterStream(String colIndex, java.io.Reader in,
                      int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateObject(String colIndex, Object obj)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateObject(String colIndex, Object obj, int s)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public Statement getStatement() throws SQLException {
    if (s == null) {
        throw new SQLException("stale result set");
    }
    return s;
    }

    public void close() throws SQLException {
    s = null;
    tr = null;
    lastg = null;
    row = -1;
    }

    public java.net.URL getURL(int colIndex) throws SQLException {
    if (tr == null || colIndex < 1 || colIndex > tr.ncolumns) {
        throw new SQLException("column " + colIndex + " not found");
    }
    String rd[] = (String []) tr.rows.elementAt(row);
    lastg = rd[colIndex - 1];
    java.net.URL url = null;
    if (lastg == null) {
        return url;
    }
    try {
        url = new java.net.URL(lastg);
    } catch (java.lang.Exception e) {
        url = null;
    }
    return url;
    }

    public java.net.URL getURL(String colIndex) throws SQLException {
    int col = findColumn(colIndex);
    return getURL(col);
    }

    public void updateRef(int colIndex, java.sql.Ref x) throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateRef(String colIndex, java.sql.Ref x)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBlob(int colIndex, java.sql.Blob x)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateBlob(String colIndex, java.sql.Blob x)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateClob(int colIndex, java.sql.Clob x)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateClob(String colIndex, java.sql.Clob x)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateArray(int colIndex, java.sql.Array x)
    throws SQLException {
    throw new SQLException("not supported");
    }

    public void updateArray(String colIndex, java.sql.Array x)
    throws SQLException {
    throw new SQLException("not supported");
    }

}
