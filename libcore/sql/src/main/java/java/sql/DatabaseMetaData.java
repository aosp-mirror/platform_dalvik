/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.sql;

/**
 * An interface which provides comprehensive information about the database.
 * <p>
 * This interface is implemented by JDBC driver writers in order to provide
 * information about the underlying Database capabilities and the JDBC driver
 * capabilities taken together.
 * <p>
 * Some of the methods in this interface take String parameters which are
 * Patterns. Within these string Patterns, '%' and '_' characters have special
 * meanings. '%' means "match any substring of 0 or more characters". '_' means
 * "match any one character". Only metadata entries that match the pattern are
 * returned. If such a search pattern string is set to <code>null</code>,
 * that argument's criteria are dropped from the search.
 * 
 */
public interface DatabaseMetaData {

    /**
     * States that it may not be permitted to store <code>NULL</code> values.
     */
    public static final short attributeNoNulls = 0;

    /**
     * States that <code>NULL</code> values are definitely permitted.
     */
    public static final short attributeNullable = 1;

    /**
     * States that whether <code>NULL</code> values are permitted is unknown.
     */
    public static final short attributeNullableUnknown = 2;

    /**
     * States the best row identifier is <em>NOT</em> a pseudo column.
     */
    public static final int bestRowNotPseudo = 1;

    /**
     * States that the best row identifier is a pseudo column.
     */
    public static final int bestRowPseudo = 2;

    /**
     * States that the remainder of the current session is used as the scope for
     * the best row identifier.
     */
    public static final int bestRowSession = 2;

    /**
     * States that best row identifier scope lasts only while the row is being
     * used.
     */
    public static final int bestRowTemporary = 0;

    /**
     * States that the remainder of the current transaction is used as the scope
     * for the best row identifier.
     */
    public static final int bestRowTransaction = 1;

    /**
     * States that the best row identifier may or may not be a pseudo column.
     */
    public static final int bestRowUnknown = 0;

    /**
     * States that the column might not allow <code>NULL</code> values.
     */
    public static final int columnNoNulls = 0;

    /**
     * States that the column definitely allows <code>NULL</code> values.
     */
    public static final int columnNullable = 1;

    /**
     * States that it is unknown whether the columns may be nulled.
     */
    public static final int columnNullableUnknown = 2;

    /**
     * For the column UPDATE_RULE, States that when the primary key is updated,
     * the foreign key (imported key) is changed to agree with it.
     */
    public static final int importedKeyCascade = 0;

    /**
     * States deferrability.
     */
    public static final int importedKeyInitiallyDeferred = 5;

    /**
     * States defer-ability.
     */
    public static final int importedKeyInitiallyImmediate = 6;

    /**
     * For the columns UPDATE_RULE and DELETE_RULE, States that if the primary
     * key has been imported, it cannot be updated or deleted.
     */
    public static final int importedKeyNoAction = 3;

    /**
     * States defer-ability.
     */
    public static final int importedKeyNotDeferrable = 7;

    /**
     * States that a primary key must not be updated when imported as a foreign
     * key by some other table. Used for the column UPDATE_RULE.
     */
    public static final int importedKeyRestrict = 1;

    /**
     * States that when the primary key is modified (updated or deleted) the
     * foreign (imported) key is changed to its default value. Applies to the
     * UPDATE_RULE and DELETE_RULE columns.
     */
    public static final int importedKeySetDefault = 4;

    /**
     * States that when the primary key is modified (updated or deleted) the
     * foreign (imported) key is changed to <code>NULL</code>. Applies to the
     * UPDATE_RULE and DELETE_RULE columns.
     */
    public static final int importedKeySetNull = 2;

    /**
     * States that this column stores IN type parameters.
     */
    public static final int procedureColumnIn = 1;

    /**
     * States that this column stores INOUT type parameters.
     */
    public static final int procedureColumnInOut = 2;

    /**
     * States that this column stores OUT type parameters.
     */
    public static final int procedureColumnOut = 4;

    /**
     * States that the column stores results
     */
    public static final int procedureColumnResult = 3;

    /**
     * States that the column stores return values.
     */
    public static final int procedureColumnReturn = 5;

    /**
     * States that type of the column is unknown.
     */
    public static final int procedureColumnUnknown = 0;

    /**
     * States that <code>NULL</code> values are not permitted.
     */
    public static final int procedureNoNulls = 0;

    /**
     * States that the procedure does not return a result.
     */
    public static final int procedureNoResult = 1;

    /**
     * States that <code>NULL</code> values are permitted.
     */
    public static final int procedureNullable = 1;

    /**
     * States that whether <code>NULL</code> values are permitted is unknown.
     */
    public static final int procedureNullableUnknown = 2;

    /**
     * States that it is unknown whether or not the procedure returns a result.
     */
    public static final int procedureResultUnknown = 0;

    /**
     * States that the procedure returns a result.
     */
    public static final int procedureReturnsResult = 2;

    /**
     * States that the value is an SQL99 SQLSTATE value.
     */
    public static final int sqlStateSQL99 = 2;

    /**
     * States that the value is an SQL CLI SQLSTATE value as defined by X/Open
     * (who are now know as Open Group) .
     */
    public static final int sqlStateXOpen = 1;

    /**
     * States that this table index is a clustered index.
     */
    public static final short tableIndexClustered = 1;

    /**
     * States that this table index is a hashed index.
     */
    public static final short tableIndexHashed = 2;

    /**
     * States this table's index is neither a clustered index, not a hashed
     * index, and not a table statistics index; i.e. it is something else.
     */
    public static final short tableIndexOther = 3;

    /**
     * States this column has the table's statistics, and that it is returned in
     * conjunction with the table's index description.
     */
    public static final short tableIndexStatistic = 0;

    /**
     * States that a <code>NULL</code> value is <em>NOT</em> permitted for
     * this data type.
     */
    public static final int typeNoNulls = 0;

    /**
     * States that a <code>NULL</code> value is permitted for this data type.
     */
    public static final int typeNullable = 1;

    /**
     * States that it is unknown if a <code>NULL</code> value is permitted for
     * this data type.
     */
    public static final int typeNullableUnknown = 2;

    /**
     * States that one can base all WHERE search clauses except WHERE .
     */
    public static final int typePredBasic = 2;

    /**
     * States that <code>WHERE</code> is the only WHERE search clause that may
     * be based on this type.
     */
    public static final int typePredChar = 1;

    /**
     * States that this type does not support <code>WHERE</code> search
     * clauses.
     */
    public static final int typePredNone = 0;

    /**
     * States that all WHERE search clauses may be based on this type.
     */
    public static final int typeSearchable = 3;

    /**
     * States that the version column is known to be not a pseudo column.
     */
    public static final int versionColumnNotPseudo = 1;

    /**
     * States that this version column is known to be a pseudo column.
     */
    public static final int versionColumnPseudo = 2;

    /**
     * States that the version column may be a pseudo column or not.
     */
    public static final int versionColumnUnknown = 0;

    /**
     * Returns whether all procedures returned by <code>getProcedures</code>
     * can be called by the current user.
     * 
     * @return <code>true</code> if all procedures can be called by the
     *         current user, <code>false</code> otherwise.
     * @throws SQLException
     *             if there is a database error
     */
    public boolean allProceduresAreCallable() throws SQLException;

    /**
     * Returns whether all the tables returned by <code>getTables</code> can
     * be used by the current user in a SELECT statement.
     * 
     * @return <code>true</code> if all the tables can be used,<code>false</code>
     *         otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean allTablesAreSelectable() throws SQLException;

    /**
     * Returns if a data definition statement in a transaction forces a commit
     * of the transaction.
     * 
     * @return <code>true</code> if the statement forces a commit,
     *         <code>false</code> otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException;

    /**
     * Returns whether the database ignores data definition statements within a
     * transaction.
     * 
     * @return <code>true</code> if the database ignores a data definition
     *         statement, <code>false</code> otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException;

    /**
     * Returns whether a visible row delete can be detected by calling
     * <code>ResultSet.rowDeleted</code>.
     * 
     * @param type
     *            the type of the ResultSet involved:
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if the visible row delete can be detected,
     *         <code>false</code> otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean deletesAreDetected(int type) throws SQLException;

    /**
     * Returns whether the return value of <code>getMaxRowSize</code> includes
     * the SQL data types <code>LONGVARCHAR</code> and
     * <code>LONGVARBINARY</code>.
     * 
     * @return <code>true</code> if the return value includes
     *         <code>LONGVARBINARY</code> and <code>LONGVARCHAR</code>,
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             if there is a database error
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException;

    /**
     * Returns a description of the specified attribute of the specified type
     * for an SQL User Defined Type (UDT) for a specified schema and catalog.
     * The descriptions returned are ordered by <code>TYPE_SCHEM</code>,
     * <code>TYPE_NAME</code> and ORDINAL_POSITION. The descriptions do not
     * contain inherited attributes.
     * <p>
     * The returned ResultSet object has rows with the following column names
     * and meanings:
     * <ol>
     * <li><code>TYPE_CAT</code> - String - the Type Catalog name (possibly
     * <code>null</code>)</li>
     * <li><code>TYPE_SCHEM</code> - String - the Type Schema name (possibly
     * <code>null</code>)</li>
     * <li><code>TYPE_NAME</code> - String - the Type name</li>
     * <li><code>ATTR_NAME</code> - String - the Attribute name</li>
     * <li><code>DATA_TYPE</code> - int - the Attribute type as defined in
     * <code>java.sql.Types</code></li>
     * <li><code>ATTR_TYPE_NAME</code> - String - the Attribute type name.
     * This depends on the data source. For a <code>UDT</code> the name is
     * fully qualified. For a <code>REF</code> it is both fully qualified and
     * represents the target type of the reference.</li>
     * <li><code>ATTR_SIZE</code> - int - the Column size. When referring to
     * char and date types this value is the maximum number of characters. When
     * referring to numeric types is is the precision.</li>
     * <li><code>DECIMAL_DIGITS</code> - int - how many fractional digits are
     * supported</li>
     * <li><code>NUM_PREC_RADIX</code> - int - numeric values radix</li>
     * <li><code>NULLABLE</code> - int - whether <code>NULL</code> is
     * permitted:
     * <ul>
     * <li>DatabaseMetaData.attributeNoNulls - might not allow
     * <code>NULL</code>s</li>
     * <li>DatabaseMetaData.attributeNullable - <code>NULL</code>s
     * definitely permitted</li>
     * <li>DatabaseMetaData.attributeNullableUnknown - unknown</li>
     * </ul>
     * </li>
     * <li><code>REMARKS</code> - String - A comment describing the attribute
     * (possibly <code>null</code>)</li>
     * <li>ATTR_DEF - String - Default value for the attribute (possibly
     * <code>null</code>)</li>
     * <li><code>SQL_DATA_TYPE</code> - int - not used</li>
     * <li>SQL_DATETIME_SUB - int - not used</li>
     * <li>CHAR_OCTET_LENGTH - int - For <code>CHAR</code> types, the max
     * number of bytes in the column</li>
     * <li>ORDINAL_POSITION - int - The Index of the column in the Table (based
     * on 1)</li>
     * <li>IS_NULLABLE - String - "NO" = column does not allow
     * <code>NULL</code>s, "YES" = column allows <code>NULL</code>s "" =
     * <code>NULL</code> status unknown</li>
     * <li><code>SCOPE_CATALOG</code> - String - Catalog for table,
     * <code>SCOPE</code> of Reference attribute. NULL if
     * <code>DATA_TYPE</code> is not REF.</li>
     * <li><code>SCOPE_SCHEMA</code> - String - Schema for table,
     * <code>SCOPE</code> of Reference attribute. NULL if
     * <code>DATA_TYPE</code> is not REF.</li>
     * <li><code>SCOPE_TABLE</code> - String - Table name for
     * <code>SCOPE</code> of Reference attribute. <code>NULL</code> if
     * <code>DATA_TYPE</code> is not REF.</li>
     * <li><code>SOURCE_DATA_TYPE</code> - String - The source type for user
     * generated REF type or for a Distinct type. (<code>NULL</code> if
     * <code>DATA_TYPE</code> is not DISTINCT or user generated REF)</li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param typeNamePattern
     *            a Type name. This pattern must match the type name stored in
     *            the database.
     * @param attributeNamePattern
     *            an Attribute name. Must match the attribute name as stored in
     *            the database.
     * @return a ResultSet, where each Row is an attribute description
     * @throws SQLException
     *             if there is a database error
     */
    public ResultSet getAttributes(String catalog, String schemaPattern,
            String typeNamePattern, String attributeNamePattern)
            throws SQLException;

    /**
     * Returns a list of a table's optimal set of columns that uniquely
     * identifies a row. The results are ordered by <code>SCOPE</code> (see
     * below).
     * <p>
     * The results are returned as a table, with one entry for each column, as
     * follows:
     * <ol>
     * <li><code>SCOPE</code> - short - the <code>SCOPE</code> of the
     * result, as follows:
     * <ul>
     * <li>DatabaseMetaData.bestRowTemporary - very temporary, while using row
     * </li>
     * <li>DatabaseMetaData.bestRowTransaction - good for remainder of current
     * transaction </li>
     * <li>DatabaseMetaData.bestRowSession - good for remainder of database
     * session </li>
     * </ul>
     * </li>
     * <li><code>COLUMN_NAME</code> - String - the column name </li>
     * <li><code>DATA_TYPE</code> - int - the Type of the data, as defined in
     * <code>java.sql.Types</code> </li>
     * <li><code>TYPE_NAME</code> - String - Name of the type - database
     * dependent. For UDT types the name is fully qualified </li>
     * <li><code>COLUMN_SIZE</code> - int - The precision of the data in the
     * column </li>
     * <li><code>BUFFER_LENGTH</code> - int - not used </li>
     * <li><code>DECIMAL_DIGITS</code> - short - number of fractional digits
     * </li>
     * <li><code>PSEUDO_COLUMN</code> - short - whether this is a pseudo
     * column eg. and Oracle ROWID:
     * <ul>
     * <li>DatabaseMetaData.bestRowUnknown - don't know whether this is a
     * pseudo column</li>
     * <li>DatabaseMetaData.bestRowNotPseudo - column is not pseudo</li>
     * <li>DatabaseMetaData.bestRowPseudo - column is a pseudo column</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schema
     *            a Schema Name Pattern. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param table
     *            the table name. This must match the name of the table as
     *            declared in the database.
     * @param scope
     *            the <code>SCOPE</code> of interest, values as defined above
     * @param nullable
     *            <code>true</code> = include columns that are nullable,
     *            <code>false</code> = do not include
     * @return a ResultSet where each row is a description of a column and the
     *         complete set of rows is the optimal set for this table.
     * @throws SQLException
     *             if there is a database error
     */
    public ResultSet getBestRowIdentifier(String catalog, String schema,
            String table, int scope, boolean nullable) throws SQLException;

    /**
     * Returns the set of catalog names available in this database. The set is
     * returned ordered by catalog name.
     * 
     * @return a ResultSet containing the Catalog names, with each row
     *         containing one Catalog name contained as a String in the single
     *         column named <code>TABLE_CAT</code>.
     * @throws SQLException
     *             if there is a database error
     */
    public ResultSet getCatalogs() throws SQLException;

    /**
     * Returns the separator that this database uses between a catalog name and
     * table name.
     * 
     * @return a String containing the separator
     * @throws SQLException
     *             if there is a database error
     */
    public String getCatalogSeparator() throws SQLException;

    /**
     * Returns the term that the database vendor prefers term for "catalog".
     * 
     * @return a String with the vendor's term for "catalog"
     * @throws SQLException
     *             if there is a database error
     */
    public String getCatalogTerm() throws SQLException;

    /**
     * Returns a description of access rights for a table's columns. Only access
     * rights matching the criteria for the column name are returned.
     * <p>
     * The description is returned as a ResultSet with rows of data for each
     * access right, with columns as follows:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - Catalog name (possibly
     * <code>null</code>)</li>
     * <li><code>TABLE_SCHEM</code> - String - Schema name (possibly
     * <code>null</code>) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li><code>COLUMN_NAME</code> - String - The Column name</li>
     * <li><code>GRANTOR</code> - String - The grantor of access (possibly
     * <code>null</code>)</li>
     * <li><code>PRIVILEGE</code> - String - Access right - one of SELECT,
     * INSERT, UPDATE, REFERENCES,...</li>
     * <li><code>IS_GRANTABLE</code> - String - "YES" implies that the
     * receiver can grant access to others, "NO" if the receiver cannot grant
     * access to others, <code>null</code> if unknown.</li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schema
     *            a Schema Name Pattern. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param table
     *            the table name. This must match the name of the table as
     *            declared in the database.
     * @param columnNamePattern
     *            the column name. This must match the name of a column in the
     *            table in the database.
     * @return a ResultSet containing the access rights, one row for each
     *         privilege description
     * @throws SQLException
     *             if there is a database error
     */
    public ResultSet getColumnPrivileges(String catalog, String schema,
            String table, String columnNamePattern) throws SQLException;

    /**
     * Returns a description of table columns available in a specified catalog.
     * Only descriptions meeting the specified Catalog, Schema, Table and Column
     * names are returned.
     * <p>
     * The descriptions are returned as a ResultSet conforming to the following
     * data layout, with one row per table column:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - Catalog name (possibly
     * <code>null</code>)</li>
     * <li><code>TABLE_SCHEM</code> - String - Schema name (possibly
     * <code>null</code>) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li><code>COLUMN_NAME</code> - String - The Column name</li>
     * <li><code>DATA_TYPE</code> - int - The SQL type as specified in
     * <code>java.sql.Types</code></li>
     * <li><code>TYPE_NAME</code> - String - Name for the data type, depends
     * on database, UDT names are fully qualified</li>
     * <li><code>COLUMN_SIZE</code> - int - Column size - the precision for
     * numeric types, max characters for char and date types</li>
     * <li><code>BUFFER_LENGTH</code> - int - Not used </li>
     * <li><code>DECIMAL_DIGITS</code> - int - maximum number of fractional
     * digits </li>
     * <li><code>NUM_PREC_RADIX</code> - int - the Radix </li>
     * <li><code>NULLABLE</code> - int - does the column allow
     * <code>null</code>s:
     * <ul>
     * <li>DatabaseMetaData.columnNoNulls = may not allow <code>NULL</code>s</li>
     * <li>DatabaseMetaData.columnNullable = does allow <code>NULL</code>s</li>
     * <li>DatabaseMetaData.columnNullableUnknown = unknown <code>NULL</code>
     * status</li>
     * </ul>
     * </li>
     * <li><code>REMARKS</code> - String - A description of the column
     * (possibly <code>null</code>) </li>
     * <li><code>COLUMN_DEF</code> - String - Default value for the column
     * (possibly <code>null</code>)</li>
     * <li><code>SQL_DATA_TYPE</code> - int - not used </li>
     * <li><code>SQL_DATETIME_SUB</code> - int - not used </li>
     * <li><code>CHAR_OCTET_LENGTH</code> - int - maximum number of bytes in
     * the char type columns </li>
     * <li><code>ORDINAL_POSITION</code> - int - Column index in the table (1
     * based) </li>
     * <li><code>IS_NULLABLE</code> - String - "NO" = column does not allow
     * NULLs, "YES" = column allows NULLs "" = <code>NULL</code> status
     * unknown</li>
     * <li><code>SCOPE</code>_CATALOG - String - Catalog for table,
     * <code>SCOPE</code> of Reference attribute. NULL if
     * <code>DATA_TYPE</code> is not REF.</li>
     * <li><code>SCOPE_SCHEMA</code> - String - Schema for table, scope of
     * Reference attribute. <code>NULL</code> if <code>DATA_TYPE</code> is
     * not REF.</li>
     * <li><code>SCOPE_TABLE</code> - String - Table name for scope of
     * Reference attribute. <code>NULL</code> if <code>DATA_TYPE</code> is
     * not REF.</li>
     * <li><code>SOURCE_DATA_TYPE</code> - String - The source type for user
     * generated REF type or for a Distinct type. (<code>NULL</code> if
     * <code>DATA_TYPE</code> is not DISTINCT or user generated REF)</li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param tableNamePattern
     *            the table name. This must match the name of the table as
     *            declared in the database.
     * @param columnNamePattern
     *            the column name. This must match the name of a column in the
     *            table in the database.
     * @return the descriptions as a ResultSet with rows in the form defined
     *         above
     * @throws SQLException
     *             if there is a database error
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern)
            throws SQLException;

    /**
     * Returns the database connection that created this metadata.
     * 
     * @return the connection
     * @throws SQLException
     *             if there is a database error
     */
    public Connection getConnection() throws SQLException;

    /**
     * Returns a list of foreign key columns in a given foreign key table that
     * reference the primary key columns of a supplied primary key table. This
     * describes how one table imports the key of another table. It would be
     * expected to return a single foreign key - primary key pair in most cases.
     * <p>
     * The descriptions are returned as a ResultSet with one row for each
     * Foreign key, with the following layout:
     * <ol>
     * <li><code>PKTABLE_CAT</code> - String - from the primary key table :
     * Catalog (possibly <code>null</code>)</li>
     * <li><code>PKTABLE_SCHEM</code> - String - from the primary key table :
     * Schema (possibly <code>null</code>) </li>
     * <li><code>PKTABLE_NAME</code> - String - primary key table : name
     * </li>
     * <li><code>PKCOLUMN_NAME</code> - String - primary key column : name</li>
     * <li><code>FKTABLE_CAT</code> - String - from the foreign key table :
     * the catalog name being exported (possibly <code>null</code>)</li>
     * <li><code>FKTABLE_SCHEM</code> - String - foreign key table : Schema
     * name being exported (possibly <code>null</code>) </li>
     * <li><code>FKTABLE_NAME</code> - String - foreign key table : the name
     * being exported</li>
     * <li><code>FKCOLUMN_NAME</code> - String - foreign key column : the
     * name being exported</li>
     * <li><code>KEY_SEQ</code> - short - sequence number (in the foreign
     * key)</li>
     * <li><code>UPDATE_RULE</code> - short - how to treat foreign key when
     * primary key is updated:
     * <ul>
     * <li>DatabaseMetaData.importedKeyNoAction - don't allow update of primary
     * key if imported</li>
     * <li>DatabaseMetaData.importedKeyCascade - change imported key to match
     * the primary key update</li>
     * <li>DatabaseMetaData.importedKeySetNull - set the imported key to
     * <code>null</code></li>
     * <li>DatabaseMetaData.importedKeySetDefault - set the imported key to
     * default values</li>
     * <li>DatabaseMetaData.importedKeyRestrict - same as importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li><code>DELETE_RULE</code> - short - how to treat foreign key when
     * primary key is deleted:
     * <ul>
     * <li>DatabaseMetaData.importedKeyNoAction - don't allow delete of primary
     * key if imported</li>
     * <li>DatabaseMetaData.importedKeyCascade - delete those rows that import
     * a deleted key</li>
     * <li>DatabaseMetaData.importedKeySetNull - set the imported key to
     * <code>null</code></li>
     * <li>DatabaseMetaData.importedKeySetDefault - set the imported key to
     * default values</li>
     * <li>DatabaseMetaData.importedKeyRestrict - same as importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>FK_NAME - String - foreign key name (possibly <code>null</code>)</li>
     * <li>PK_NAME - String - primary key name (possibly <code>null</code>)</li>
     * <li>DEFERRABILITY - short - can foreign key constraints be deferred
     * until commit (see SQL92 specification for definitions)?:
     * <ul>
     * <li>DatabaseMetaData.importedKeyInitiallyDeferred</li>
     * <li>DatabaseMetaData.importedKeyInitiallyImmediate</li>
     * <li>DatabaseMetaData.importedKeyNotDeferrable</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * @param primaryCatalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param primarySchema
     *            a Schema Name. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param primaryTable
     *            the name of the table which exports the key. It must match the
     *            name of the table in the database
     * @param foreignCatalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param foreignSchema
     *            a Schema Name. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param foreignTable
     *            the name of the table importing the key. It must match the
     *            name of the table in the database
     * @return a ResultSet containing rows with the descriptions of the foreign
     *         keys laid out according to the format defined above.
     * @throws SQLException
     *             if there is a database error
     */
    public ResultSet getCrossReference(String primaryCatalog,
            String primarySchema, String primaryTable, String foreignCatalog,
            String foreignSchema, String foreignTable) throws SQLException;

    /**
     * Returns the major version number of the database software.
     * 
     * @return the Major version number of the database software.
     * @throws SQLException
     *             a database error occurred
     */
    public int getDatabaseMajorVersion() throws SQLException;

    /**
     * Returns the minor version number of the database software.
     * 
     * @return the Minor version number of the database software.
     * @throws SQLException
     *             a database error occurred
     */
    public int getDatabaseMinorVersion() throws SQLException;

    /**
     * Returns the name of the database software.
     * 
     * @return a String with the name of the database software.
     * @throws SQLException
     *             a database error occurred
     */
    public String getDatabaseProductName() throws SQLException;

    /**
     * Returns the version number of this database software.
     * 
     * @return a String with the version number of the database software.
     * @throws SQLException
     *             a database error occurred
     */
    public String getDatabaseProductVersion() throws SQLException;

    /**
     * Returns the default transaction isolation level for this database.
     * 
     * @return the default transaction isolation level. One of
     *         <code>TRANSACTION_NONE</code>,
     *         <code>TRANSACTION_READ_COMMITTED</code>,
     *         <code>TRANSACTION_READ_UNCOMMITTED</code>,
     *         <code>TRANSACTION_REPEATABLE_READ</code> or
     *         <code>TRANSACTION_SERIALIZABLE</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public int getDefaultTransactionIsolation() throws SQLException;

    /**
     * Returns the JDBC driver's major version number.
     * 
     * @return the driver's major version number
     */
    public int getDriverMajorVersion();

    /**
     * Returns the JDBC driver's minor version number.
     * 
     * @return the driver's minor version number
     */
    public int getDriverMinorVersion();

    /**
     * Returns the name of this JDBC driver.
     * 
     * @return a String containing the name of the JDBC driver
     * @throws SQLException
     *             a database error occurred
     */
    public String getDriverName() throws SQLException;

    /**
     * Returns the version number of this JDBC driver.
     * 
     * @return a String containing the complete version number of the JDBC
     *         driver
     * @throws SQLException
     *             a database error occurred
     */
    public String getDriverVersion() throws SQLException;

    /**
     * Returns a list of the foreign key columns that reference the primary key
     * columns of a specified table (the foreign keys exported by a table).
     * <p>
     * The list is returned as a ResultSet with a row for each of the foreign
     * key columns, ordered by <code>FKTABLE_CAT</code>,
     * <code>FKTABLE_SCHEM</code>, <code>FKTABLE_NAME</code>, and
     * <code>KEY_SEQ</code>, with the format for each row being:
     * <ol>
     * <li><code>PKTABLE_CAT</code> - String - primary key table : Catalog
     * (possibly <code>null</code>)</li>
     * <li><code>PKTABLE_SCHEM</code> - String - primary key table : Schema
     * (possibly <code>null</code>) </li>
     * <li><code>PKTABLE_NAME</code> - String - primary key table : name
     * </li>
     * <li><code>PKCOLUMN_NAME</code> - String - primary key column : name</li>
     * <li><code>FKTABLE_CAT</code> - String - foreign key table : Catalog
     * name being exported (possibly <code>null</code>)</li>
     * <li><code>FKTABLE_SCHEM</code> - String - foreign key table : Schema
     * name being exported (possibly <code>null</code>) </li>
     * <li><code>FKTABLE_NAME</code> - String - foreign key table : name
     * being exported</li>
     * <li><code>FKCOLUMN_NAME</code> - String - foreign key column : name
     * being exported</li>
     * <li>KEY_SEQ - short - sequence number in the foreign key</li>
     * <li>UPDATE_RULE - short - how to treat foreign key when primary key is
     * updated:
     * <ul>
     * <li>DatabaseMetaData.importedKeyNoAction - don't allow update of primary
     * key if imported</li>
     * <li>DatabaseMetaData.importedKeyCascade - change imported key to match
     * the primary key update</li>
     * <li>DatabaseMetaData.importedKeySetNull - set the imported key to
     * <code>null</code></li>
     * <li>DatabaseMetaData.importedKeySetDefault - set the imported key to
     * default values</li>
     * <li>DatabaseMetaData.importedKeyRestrict - same as importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>DELETE_RULE - short - how to treat foreign key when primary key is
     * deleted:
     * <ul>
     * <li>DatabaseMetaData.importedKeyNoAction - don't allow delete of primary
     * key if imported</li>
     * <li>DatabaseMetaData.importedKeyCascade - the deletion should also
     * delete rows that import a deleted key</li>
     * <li>DatabaseMetaData.importedKeySetNull - it should set the imported key
     * to <code>null</code></li>
     * <li>DatabaseMetaData.importedKeySetDefault - deletion sets the imported
     * key to default values</li>
     * <li>DatabaseMetaData.importedKeyRestrict - same as importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>FK_NAME - String - foreign key name (possibly <code>null</code>)</li>
     * <li>PK_NAME - String - primary key name (possibly <code>null</code>)</li>
     * <li>DEFERRABILITY - short - defines whether foreign key constraints can
     * be deferred until commit (see SQL92 specification for definitions):
     * <ul>
     * <li>DatabaseMetaData.importedKeyInitiallyDeferred</li>
     * <li>DatabaseMetaData.importedKeyInitiallyImmediate</li>
     * <li>DatabaseMetaData.importedKeyNotDeferrable</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schema
     *            a Schema Name. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param table
     *            a table name, which must match the name of a table in the
     *            database
     * @return a ResultSet containing a row for each of the foreign key columns,
     *         as defined above
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException;

    /**
     * Returns a string of characters that may be used in unquoted identifier
     * names. The characters a-z, A-Z, 0-9 and _ are always permitted.
     * 
     * @return a String containing all the extra characters
     * @throws SQLException
     *             a database error occurred
     */
    public String getExtraNameCharacters() throws SQLException;

    /**
     * Returns the string used to quote SQL identifiers. Returns " " (space) if
     * identifier quoting not supported.
     * 
     * @return the String used to quote SQL identifiers.
     * @throws SQLException
     *             a database error occurred
     */
    public String getIdentifierQuoteString() throws SQLException;

    /**
     * Returns a list columns in a table that are both primary keys and
     * referenced by the table's foreign key columns (that is, the primary keys
     * imported by a table).
     * <p>
     * The list returned is a <code>ResultSet</code> with a row entry for each
     * primary key column, ordered by <code>PKTABLE_CAT</code>,
     * <code>PKTABLE_SCHEM</code>, <code>PKTABLE_NAME</code>, and
     * <code>KEY_SEQ</code>, with the following format:
     * <ol>
     * <li><code>PKTABLE_CAT</code> - String - primary key Catalog name being
     * imported (possibly <code>null</code>)</li>
     * <li><code>PKTABLE_SCHEM</code> - String - primary key Schema name
     * being imported (possibly <code>null</code>) </li>
     * <li><code>PKTABLE_NAME</code> - String - primary key Table name being
     * imported </li>
     * <li><code>PKCOLUMN_NAME</code> - String - primary key column name
     * being imported</li>
     * <li><code>FKTABLE_CAT</code> - String - foreign key table catalog name
     * (possibly <code>null</code>)</li>
     * <li><code>FKTABLE_SCHEM</code> - String - foreign key table Schema
     * name (possibly <code>null</code>) </li>
     * <li><code>FKTABLE_NAME</code> - String - foreign key table name</li>
     * <li><code>FKCOLUMN_NAME</code> - String - foreign key column name</li>
     * <li>KEY_SEQ - short - sequence number in the foreign key</li>
     * <li>UPDATE_RULE - short - how to treat foreign key when primary key is
     * updated:
     * <ul>
     * <li>DatabaseMetaData.importedKeyNoAction - don't allow update of primary
     * key if imported</li>
     * <li>DatabaseMetaData.importedKeyCascade - change imported key to match
     * the primary key update</li>
     * <li>DatabaseMetaData.importedKeySetNull - set the imported key to
     * <code>null</code></li>
     * <li>DatabaseMetaData.importedKeySetDefault - set the imported key to
     * default values</li>
     * <li>DatabaseMetaData.importedKeyRestrict - same as importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>DELETE_RULE - short - how to treat foreign key when primary key is
     * deleted:
     * <ul>
     * <li>DatabaseMetaData.importedKeyNoAction - don't allow delete of primary
     * key if imported</li>
     * <li>DatabaseMetaData.importedKeyCascade - delete those rows that import
     * a deleted key</li>
     * <li>DatabaseMetaData.importedKeySetNull - set the imported key to
     * <code>null</code></li>
     * <li>DatabaseMetaData.importedKeySetDefault - set the imported key to
     * default values</li>
     * <li>DatabaseMetaData.importedKeyRestrict - same as importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>FK_NAME - String - foreign key name (possibly <code>null</code>)</li>
     * <li>PK_NAME - String - primary key name (possibly <code>null</code>)</li>
     * <li>DEFERRABILITY - short - defines whether foreign key constraints can
     * be deferred until commit (see SQL92 specification for definitions):
     * <ul>
     * <li>DatabaseMetaData.importedKeyInitiallyDeferred</li>
     * <li>DatabaseMetaData.importedKeyInitiallyImmediate</li>
     * <li>DatabaseMetaData.importedKeyNotDeferrable</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schema
     *            a Schema Name. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param table
     *            a table name, which must match the name of a table in the
     *            database
     * @return a ResultSet containing the list of primary key columns as rows in
     *         the format defined above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException;

    /**
     * Returns a list of indices and statistics for a specified table.
     * <p>
     * The list is returned as a ResultSet, with one row for each index or
     * statistic. The list is ordered by NON_UNIQUE, TYPE, INDEX_NAME, and
     * ORDINAL_POSITION. Each row has the following format:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - table catalog name (possibly
     * <code>null</code>)</li>
     * <li><code>TABLE_SCHEM</code> - String - Table Schema name (possibly
     * <code>null</code>) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li><code>NON_UNIQUE</code> - boolean - <code>true</code> when index
     * values can be non-unique. Must be <code>false</code> when TYPE is
     * tableIndexStatistic</li>
     * <li><code>INDEX_QUALIFIER</code> - String : index catalog name.
     * <code>null</code> when TYPE is 'tableIndexStatistic'</li>
     * <li><code>INDEX_NAME</code> - String : index name. <code>null</code>
     * when TYPE is 'tableIndexStatistic'</li>
     * <li>TYPE - short - the index type. One of:
     * <ul>
     * <li>DatabaseMetaData.tableIndexStatistic - table statistics returned
     * with Index descriptions</li>
     * <li>DatabaseMetaData.tableIndexClustered - a clustered Index</li>
     * <li>DatabaseMetaData.tableIndexHashed - a hashed Index</li>
     * <li>DatabaseMetaData.tableIndexOther - other style of Index</li>
     * </ul>
     * </li>
     * <li>ORDINAL_POSITION - short - column sequence within Index. 0 when TYPE
     * is tableIndexStatistic </li>
     * <li><code>COLUMN_NAME</code> - String - the column name.
     * <code>null</code> when TYPE is tableIndexStatistic</li>
     * <li>ASC_OR_DESC - String - column sort sequence. <code>null</code> if
     * sequencing not supported or TYPE is tableIndexStatistic; otherwise "A"
     * means sort ascending and "D" means sort descending. </li>
     * <li>CARDINALITY - int - Number of unique values in the Index. If TYPE is
     * tableIndexStatistic, this is number of rows in the table.</li>
     * <li>PAGES - int - Number of pages for current Index. If TYPE is
     * tableIndexStatistic, this is number of pages used for the table.</li>
     * <li>FILTER_CONDITION - String - Filter condition. (possibly null) </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schema
     *            a Schema Name. null is used to imply no narrowing of the
     *            search using Schema Name. Otherwise, the name must match a
     *            Schema name in the database, with "" used to retrieve those
     *            without a Schema name.
     * @param table
     *            a table name, which must match the name of a table in the
     *            database
     * @param unique
     *            <code>true</code> means only return indices for unique
     *            values, <code>false</code> implies that they can be returned
     *            even if not unique.
     * @param approximate
     *            <code>true</code> implies that the list can contain
     *            approximate or "out of data" values, <code>false</code>
     *            implies that all values must be precisely accurate
     * @return a ResultSet containing the list of indices and statistics for the
     *         table, in the format defined above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getIndexInfo(String catalog, String schema, String table,
            boolean unique, boolean approximate) throws SQLException;

    /**
     * Returns this driver's major JDBC version number.
     * 
     * @return the major JDBC version number
     * @throws SQLException
     *             a database error occurred
     */
    public int getJDBCMajorVersion() throws SQLException;

    /**
     * Returns the minor JDBC version number for this driver.
     * 
     * @return the Minor JDBC Version Number
     * @throws SQLException
     *             a database error occurred
     */
    public int getJDBCMinorVersion() throws SQLException;

    /**
     * Get the maximum number of hex characters in an in-line binary literal for
     * this database.
     * 
     * @return the maximum number of hex characters in an in-line binary
     *         literal. If the number is unlimited then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxBinaryLiteralLength() throws SQLException;

    /**
     * Returns the maximum size of a Catalog name in this database.
     * 
     * @return the maximum size in characters for a Catalog name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxCatalogNameLength() throws SQLException;

    /**
     * Returns the maximum size for a character literal in this database.
     * 
     * @return the maximum size in characters for a character literal. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxCharLiteralLength() throws SQLException;

    /**
     * Returns the maximum size for a Column name for this database.
     * 
     * @return the maximum number of characters for a Column name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxColumnNameLength() throws SQLException;

    /**
     * Get the maximum number of columns in a GROUP BY clause for this database.
     * 
     * @return the maximum number of columns in a GROUP BY clause. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxColumnsInGroupBy() throws SQLException;

    /**
     * Returns the maximum number of columns in an Index for this database.
     * 
     * @return the maximum number of columns in an Index. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxColumnsInIndex() throws SQLException;

    /**
     * Returns the maximum number of columns in an ORDER BY clause for this
     * database.
     * 
     * @return the maximum number of columns in an ORDER BY clause. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxColumnsInOrderBy() throws SQLException;

    /**
     * Returns the maximum number of columns in a SELECT list for this database.
     * 
     * @return the maximum number of columns in a SELECT list. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxColumnsInSelect() throws SQLException;

    /**
     * Returns the maximum number of columns in a table for this database.
     * 
     * @return the maximum number of columns in a table. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxColumnsInTable() throws SQLException;

    /**
     * Returns the database's maximum number of concurrent connections.
     * 
     * @return the maximum number of connections. If the limit is unknown, or
     *         the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxConnections() throws SQLException;

    /**
     * Returns the maximum length of a cursor name for this database.
     * 
     * @return the maximum number of characters in a cursor name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxCursorNameLength() throws SQLException;

    /**
     * Returns the maximum length in bytes for an Index for this database. This
     * covers all the parts of a composite index.
     * 
     * @return the maximum length in bytes for an Index. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxIndexLength() throws SQLException;

    /**
     * Returns the maximum number of characters for a procedure name in this
     * database.
     * 
     * @return the maximum number of character for a procedure name. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxProcedureNameLength() throws SQLException;

    /**
     * Returns the maximum number of bytes within a single row for this
     * database.
     * 
     * @return the maximum number of bytes for a single row. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxRowSize() throws SQLException;

    /**
     * Returns the maximum number of characters in a schema name for this
     * database.
     * 
     * @return the maximum number of characters in a Schema name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxSchemaNameLength() throws SQLException;

    /**
     * Returns the maximum number of characters in an SQL statement for this
     * database.
     * 
     * @return the maximum number of characters in an SQL statement. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxStatementLength() throws SQLException;

    /**
     * Get the maximum number of simultaneously open active statements for this
     * database.
     * 
     * @return the maximum number of open active statements. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxStatements() throws SQLException;

    /**
     * Returns the maximum size for a table name in the database.
     * 
     * @return the maximum size in characters for a table name. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxTableNameLength() throws SQLException;

    /**
     * Returns the maximum number of tables permitted in a SELECT statement for
     * the database.
     * 
     * @return the maximum number of tables permitted in a SELECT statement. If
     *         the limit is unknown, or the value is unlimited, then the result
     *         is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxTablesInSelect() throws SQLException;

    /**
     * Returns the maximum number of characters in a user name for the database.
     * 
     * @return the maximum number of characters in a user name. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred
     */
    public int getMaxUserNameLength() throws SQLException;

    /**
     * Returns a list of the math functions available with this database. These
     * are used in the JDBC function escape clause and are the Open Group CLI
     * math function names.
     * 
     * @return a String which contains the list of Math functions as a comma
     *         separated list.
     * @throws SQLException
     *             a database error occurred
     */
    public String getNumericFunctions() throws SQLException;

    /**
     * Returns a list of the primary key columns of a specified table.
     * <p>
     * The list is returned as a ResultSet with one row for each primary key
     * column, ordered by <code>COLUMN_NAME</code>, with each row having the
     * structure as follows:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - table catalog name (possibly
     * null)</li>
     * <li><code>TABLE_SCHEM</code> - String - Table Schema name (possibly
     * null) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li><code>COLUMN_NAME</code> - String - The Column name </li>
     * <li><code>KEY_SEQ</code> - short - the sequence number for this column
     * in the primary key</li>
     * <li><code>PK_NAME</code> - String - the primary key name (possibly
     * null)</li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with the
     *            empty string used to retrieve those without a Catalog Name.
     * @param schema
     *            a Schema Name. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with the empty
     *            string used to retrieve those without a Schema name.
     * @param table
     *            the name of a table, which must match the name of a table in
     *            the database
     * @return a ResultSet containing the list of keys in the format defined
     *         above
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException;

    /**
     * Returns a list of parameter and result columns for the stored procedures
     * belonging to a specified Catalog.
     * <p>
     * The list is returned as a ResultSet with one row for each parameter or
     * result column. The data is ordered by PROCEDURE_SCHEM and PROCEDURE_NAME,
     * while for each procedure, the return value (if any) is first, followed by
     * the parameters in the order they appear in the stored procedure call,
     * followed by ResultSet columns in column number order. Each row has the
     * following structure:
     * <ol>
     * <li>PROCEDURE_CAT - String - the procedure catalog name</li>
     * <li>PROCEDURE_SCHEM - String - the procedure schema name (possibly null)
     * </li>
     * <li>PROCEDURE_NAME - String - the procedure name</li>
     * <li><code>COLUMN_NAME</code> - String - the name of the column</li>
     * <li>COLUMN_TYPE - short - the kind of column or parameter, as follows:
     * <ul>
     * <li>DatabaseMetaData.procedureColumnUnknown - type unknown</li>
     * <li>DatabaseMetaData.procedureColumnIn - an IN parameter</li>
     * <li>DatabaseMetaData.procedureColumnInOut - an INOUT parameter</li>
     * <li>DatabaseMetaData.procedureColumnOut - an OUT parameter</li>
     * <li>DatabaseMetaData.procedureColumnReturn - a return value</li>
     * <li>DatabaseMetaData.procedureReturnsResult - a result column in a
     * result set</li>
     * </ul>
     * </li>
     * <li><code>DATA_TYPE</code> - int - the SQL type of the data, as in
     * <code>java.sql.Types</code> </li>
     * <li><code>TYPE_NAME</code> - String - the SQL type name, for a UDT it
     * is fully qualified</li>
     * <li>PRECISION - int - the precision</li>
     * <li>LENGTH - int - the length of the data in bytes </li>
     * <li>SCALE - short - the scale for numeric types</li>
     * <li>RADIX - short - the Radix for numeric data (typically 2 or 10) </li>
     * <li>NULLABLE - short - can the data contain null:
     * <ul>
     * <li>DatabaseMetaData.procedureNoNulls - <code>NULL</code>s not
     * permitted</li>
     * <li>DatabaseMetaData.procedureNullable - <code>NULL</code>s are
     * permitted </li>
     * <li>DatabaseMetaData.procedureNullableUnknown - <code>NULL</code>
     * status unknown </li>
     * </ul>
     * </li>
     * <li><code>REMARKS</code> - String - an explanatory comment about the
     * data item </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. null is used to imply no narrowing of
     *            the search using Schema Name. Otherwise, the name must match a
     *            Schema name in the database, with "" used to retrieve those
     *            without a Schema name.
     * @param procedureNamePattern
     *            a pattern that must match the name of the procedure stored in
     *            the database
     * @param columnNamePattern
     *            a column name pattern. The name must match the column name
     *            stored in the database.
     * @return a ResultSet with the list of parameter and result columns in the
     *         format defined above
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getProcedureColumns(String catalog, String schemaPattern,
            String procedureNamePattern, String columnNamePattern)
            throws SQLException;

    /**
     * Returns a list of the stored procedures available in a specified catalog.
     * <p>
     * The list is returned as a ResultSet with one row for each stored
     * procedure, ordered by PROCEDURE_SCHEME and PROCEDURE_NAME, with the data
     * in each row as follows:
     * <ol>
     * <li><code>PROCEDURE_CAT</code> - String : the procedure catalog name</li>
     * <li><code>PROCEDURE_SCHEM</code> - String : the procedure schema name
     * (possibly <code>null</code>) </li>
     * <li><code>PROCEDURE_NAME</code> - String : the procedure name</li>
     * <li><code>Reserved</code></li>
     * <li><code>Reserved</code></li>
     * <li><code>Reserved</code></li>
     * <li><code>REMARKS</code> - String - information about the procedure</li>
     * <li><code>PROCEDURE_TYPE</code> - short : one of:
     * <ul>
     * <li>DatabaseMetaData.procedureResultUnknown - procedure may return a
     * result </li>
     * <li>DatabaseMetaData.procedureNoResult - procedure does not return a
     * result</li>
     * <li>DatabaseMetaData.procedureReturnsResult - procedure definitely
     * returns a result</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. null is used to imply no narrowing of
     *            the search using Schema Name. Otherwise, the name must match a
     *            Schema name in the database, with "" used to retrieve those
     *            without a Schema name.
     * @param procedureNamePattern
     *            a procedure name pattern, which must match the procedure name
     *            stored in the database
     * @return a ResultSet where each row is a description of a stored procedure
     *         in the format defined above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getProcedures(String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException;

    /**
     * Returns the database vendor's preferred name for "procedure".
     * 
     * @return a String with the vendor's preferred name for "procedure"
     * @throws SQLException
     *             a database error occurred
     */
    public String getProcedureTerm() throws SQLException;

    /**
     * Returns the result set's default hold-ability.
     * 
     * @return one of <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException
     *             a database error occurred
     */
    public int getResultSetHoldability() throws SQLException;

    /**
     * Returns a list of the schema names in the database. The list is returned
     * as a ResultSet, ordered by the Schema name, with one row per Schema in
     * the following format:
     * <ol>
     * <li><code>TABLE_SCHEM</code> - String - the Schema name</li>
     * <li><code>TABLE_CAT</code>ALOG - String - the Catalog name (possibly
     * null) </li>
     * </ol>
     * 
     * @return a ResultSet with one row for each schema in the format defined
     *         above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getSchemas() throws SQLException;

    /**
     * Returns the database vendor's preferred term for "schema".
     * 
     * @return a String which is the vendor's preferred term for schema
     * @throws SQLException
     *             a database error occurred
     */
    public String getSchemaTerm() throws SQLException;

    /**
     * Returns the string that is used to escape wildcard characters. This
     * string is used to escape the '_' and '%' wildcard characters in catalog
     * search strings which are a pattern and so which use the wildcard
     * characters. '_' is used to represent any single character wile '%' is
     * used for a sequence of zero or more characters.
     * 
     * @return a String used to escape the wildcard characters
     * @throws SQLException
     *             a database error occurred
     */
    public String getSearchStringEscape() throws SQLException;

    /**
     * Returns a list of all the SQL keywords that are NOT also SQL92 keywords
     * for the database.
     * 
     * @return a String containing the list of SQL keywords in a comma separated
     *         format.
     * @throws SQLException
     *             a database error occurred
     */
    public String getSQLKeywords() throws SQLException;

    /**
     * States the type of SQLState value returned by SQLException.getSQLState.
     * This can either be the X/Open (now known as Open Group) SQL CLI form or
     * the SQL99 form.
     * 
     * @return an integer, which is either DatabaseMetaData.sqlStateSQL99 or
     *         DatabaseMetaData.sqlStateXOpen.
     * @throws SQLException
     *             a database error occurred
     */
    public int getSQLStateType() throws SQLException;

    /**
     * Returns a list of string functions available with the database. These
     * functions are used in JDBC function escape clause and follow the Open
     * Group CLI string function names definition.
     * 
     * @return a String containing the list of string functions in comma
     *         separated format.
     * @throws SQLException
     *             a database error occurred
     */
    public String getStringFunctions() throws SQLException;

    /**
     * Returns a listing of the hierarchies of tables in a specified schema in
     * the database.
     * <p>
     * The listing only contains entries for tables that have a super table.
     * Super and sub tables must be defined in the same Catalog and Schema. The
     * list is returned as a ResultSet, with one row for each table that has a
     * super table, in the following format:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - table catalog name (possibly
     * null)</li>
     * <li><code>TABLE_SCHEM</code> - String - Table Schema name (possibly
     * null) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li>SUPER<code>TABLE_NAME</code> - String - The Super Table name
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. null is used to imply no narrowing of
     *            the search using Schema Name. Otherwise, the name must match a
     *            Schema name in the database, with "" used to retrieve those
     *            without a Schema name.
     * @param tableNamePattern
     *            a Table Name, which should match the Table name as stored in
     *            the database. it may be a fully qualified name. If it is fully
     *            qualified the Catalog Name and Schema Name parameters are
     *            ignored.
     * @return a ResultSet with one row for each table which has a super table,
     *         in the format defined above. An empty ResultSet is returned if
     *         the database does not support table hierarchies.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getSuperTables(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException;

    /**
     * Returns the User Defined Type (UDT) hierarchies for a given schema. Only
     * the immediate parent/child relationship is described. If a UDT does not
     * have a direct supertype, it is not listed.
     * <p>
     * The listing is returned as a ResultSet where there is one row for a
     * specific UDT which describes its supertype, with the data organized in
     * columns as follows:
     * <ol>
     * <li><code>TYPE_CAT</code> - String - the UDT Catalog name (possibly
     * null)</li>
     * <li><code>TYPE_SCHEM</code> - String - the UDT Schema name (possibly
     * null) </li>
     * <li><code>TYPE_NAME</code> - String - the UDT type name </li>
     * <li>SUPER<code>TYPE_CAT</code> - String - direct supertype's Catalog
     * name (possibly null)</li>
     * <li>SUPER<code>TYPE_SCHEM</code> - String - direct supertype's Schema
     * name (possibly null) </li>
     * <li>SUPER<code>TYPE_NAME</code> - String - direct supertype's name
     * </li>
     * </ol>
     * 
     * @param catalog
     *            the Catalog name. "" means get the UDTs without a catalog.
     *            null means don't use the catalog name to restrict the search.
     * @param schemaPattern
     *            the Schema pattern name. "" means get the UDT's without a
     *            schema.
     * @param typeNamePattern
     *            the UDT name pattern. This may be a fully qualified name. When
     *            a fully qualified name is specified, the Catalog name and
     *            Schema name parameters are ignored.
     * @return a ResultSet in which each row gives information about a
     *         particular UDT in the format defined above. An empty ResultSet is
     *         returned for a database that does not support type hierarchies.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getSuperTypes(String catalog, String schemaPattern,
            String typeNamePattern) throws SQLException;

    /**
     * Returns a list of system functions available with the database. These are
     * names used in the JDBC function escape clause and are Open Group CLI
     * function names.
     * 
     * @return a String containing the list of system functions in a comma
     *         separated format
     * @throws SQLException
     *             a database error occurred
     */
    public String getSystemFunctions() throws SQLException;

    /**
     * Returns a description of access rights for each table present in a
     * catalog. Table privileges can apply to one or more columns in the table -
     * but are not guaranteed to apply to all columns.
     * <p>
     * The privileges are returned as a ResultSet, with one row for each
     * privilege, ordered by <code>TABLE_SCHEM</code>,
     * <code>TABLE_NAME</code>, PRIVILEGE, and each row has data as defined
     * in the following column definitions:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - table catalog name (possibly
     * null)</li>
     * <li><code>TABLE_SCHEM</code> - String - Table Schema name (possibly
     * null) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li>GRANTOR - String - who granted the access</li>
     * <li>GRANTEE - String - who received the access grant </li>
     * <li>PRIVILEGE - String - the type of access granted - one of SELECT,
     * INSERT, UPDATE, REFERENCES,... </li>
     * <li>IS_GRANTABLE - String - "YES" implies the grantee can grant access
     * to others, "NO" implies guarantee cannot grant access to others, null
     * means this status is unknown</li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. null is used to imply no narrowing of
     *            the search using Schema Name. Otherwise, the name must match a
     *            Schema name in the database, with "" used to retrieve those
     *            without a Schema name.
     * @param tableNamePattern
     *            a Table Name, which should match the Table name as stored in
     *            the database.
     * @return a ResultSet containing a list with one row for each table in the
     *         format defined above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException;

    /**
     * Returns a description of the tables in a specified catalog.
     * <p>
     * The descriptions are returned as rows in a ResultSet, one row for each
     * Table. The ResultSet is ordered by <code>TABLE_TYPE</code>,
     * <code>TABLE_SCHEM</code> and <code>TABLE_NAME</code>. Each row in
     * the ResultSet consists of a series of columns as follows:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - table catalog name (possibly
     * null)</li>
     * <li><code>TABLE_SCHEM</code> - String - Table Schema name (possibly
     * null) </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li><code>TABLE_TYPE</code> - String - Typical names include "TABLE",
     * "VIEW", "SYSTEM TABLE", "ALIAS", "SYNONYM", "GLOBAL TEMPORARY"</li>
     * <li><code>REMARKS</code> - String - A comment describing the table
     * </li>
     * <li><code>TYPE_CAT</code> - String - the 'Types' catalog(possibly
     * null)</li>
     * <li><code>TYPE_SCHEM</code> - String - the 'Types' schema(possibly
     * null) </li>
     * <li><code>TYPE_NAME</code> - String - the 'Types' name (possibly null)
     * </li>
     * <li><code>SELF_REFERENCING_COL_NAME</code> - String - the name of a
     * designated identifier column in a typed table (possibly null) </li>
     * <li>REF_GENERATION - String - one of the following values : "SYSTEM" |
     * "USER" | "DERIVED" - specifies how values in the
     * <code>SELF_REFERENCING_COL_NAME</code> are created (possibly null)
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. null is used to imply no narrowing of
     *            the search using Schema Name. Otherwise, the name must match a
     *            Schema name in the database, with "" used to retrieve those
     *            without a Schema name.
     * @param tableNamePattern
     *            a Table Name, which should match the Table name as stored in
     *            the database.
     * @param types
     *            a list of table types to include in the list. null implies
     *            list all types.
     * @return a ResultSet with one row per table in the format defined above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getTables(String catalog, String schemaPattern,
            String tableNamePattern, String[] types) throws SQLException;

    /**
     * Returns a list of table types supported by the database.
     * <p>
     * The list is returned as a ResultSet with one row per table type, ordered
     * by the table type. The information in the ResultSet is structured into a
     * single column per row, as follows:
     * <ol>
     * <li><code>TABLE_TYPE</code> - String - the Table Type. Typical names
     * include "TABLE", "VIEW", "SYSTEM TABLE", "ALIAS", "SYNONYM", "GLOBAL
     * TEMPORARY" </li>
     * </ol>
     * 
     * @return a ResultSet with one row per table type in the format defined
     *         above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getTableTypes() throws SQLException;

    /**
     * Returns a list of time and date functions available for the database.
     * 
     * @return a String contain a comma separated list of the time and date
     *         functions.
     * @throws SQLException
     *             a database error occurred
     */
    public String getTimeDateFunctions() throws SQLException;

    /**
     * Get a list of the standard SQL Types supported by this database. The list
     * is returned as a ResultSet, with one row for each type, ordered by the
     * <code>DATA_TYPE</code> value, where the data in each row is structured
     * into the following columns:
     * <ol>
     * <li><code>TYPE_NAMR</code> - String : the Type name</li>
     * <li><code>DATA_TYPE</code> - int : the SQL data type value as defined
     * in <code>java.sql.Types</code></li>
     * <li><code>PRECISION</code> - int - the maximum precision of the type</li>
     * <li><code>LITERAL_PREFIX</code> - String : the prefix to be used when
     * quoting a literal value (possibly <code>null</code>)</li>
     * <li><code>LITERAL_SUFFIX</code> - String : the suffix to be used when
     * quoting a literal value (possibly <code>null</code>)</li>
     * <li><code>CREATE_PARAMS</code> - String : params used when creating
     * the type (possibly <code>null</code>)</li>
     * <li><code>NULLABLE</code> - short : shows if the value is null-able:
     * <ul>
     * <li>DatabaseMetaData.typeNoNulls : <code>NULL</code>s not permitted</li>
     * <li>DatabaseMetaData.typeNullable : <code>NULL</code>s are permitted
     * </li>
     * <li>DatabaseMetaData.typeNullableUnknown : <code>NULL</code> status
     * unknown </li>
     * </ul>
     * </li>
     * <li>CASE_SENSITIVE - boolean : true if the type is case sensitive</li>
     * <li>SEARCHABLE - short : how this type can be used with WHERE clauses:
     * <ul>
     * <li>DatabaseMetaData.typePredNone - cannot be used </li>
     * <li>DatabaseMetaData.typePredChar - support for WHERE...LIKE only</li>
     * <li>DatabaseMetaData.typePredBasic - support except for WHERE...LIKE</li>
     * <li>DatabaseMetaData.typeSearchable - support for all WHERE clauses</li>
     * </ul>
     * </li>
     * <li>UNSIGNED_ATTRIBUTE - boolean - the type is unsigned or not </li>
     * <li>FIXED_PREC_SCALE - boolean - fixed precision = it can be used as a
     * money value </li>
     * <li>AUTO_INCREMENT - boolean - can be used as an auto-increment value
     * </li>
     * <li>LOCAL_<code>TYPE_NAME</code> - String - a localized version of
     * the type name (possibly null)</li>
     * <li>MINIMUM_SCALE - short - the minimum scale supported </li>
     * <li>MAXIMUM_SCALE - short - the maximum scale supported </li>
     * <li>SQL_<code>DATA_TYPE</code> - int - not used </li>
     * <li>SQL_DATETIME_SUB - int - not used </li>
     * <li>NUM_PREC_RADIX - int - number radix (typically 2 or 10) </li>
     * </ol>
     * 
     * @return a ResultSet which is structured as described above
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getTypeInfo() throws SQLException;

    /**
     * Returns a description of the User Defined Types (UDTs) defined in a given
     * schema, which includes the types DISTINCT, STRUCT and JAVA_OBJECT.
     * <p>
     * The types matching the supplied the specified Catalog, Schema, Type Name
     * and Type are returned as rows in a ResultSet with columns of information
     * as follows:
     * <ol>
     * <li><code>TABLE_CAT</code> - String - Catalog name (possibly null)</li>
     * <li><code>TABLE_SCHEM</code> - String - Schema name (possibly null)
     * </li>
     * <li><code>TABLE_NAME</code> - String - The Table name </li>
     * <li><code>CLASS_NAME</code> - String - The Java class name</li>
     * <li><code>DATA_TYPE</code> - int - The SQL type as specified in
     * <code>java.sql.Types</code>. One of DISTINCT, STRUCT and JAVA_OBJECT</li>
     * <li><code>REMARKS</code> - String - A comment which describes the type
     * </li>
     * <li><code>BASE_TYPE</code> - short - A type code. For a DISTINCT type,
     * the source type. For a structured type this is the type that implements
     * the user generated reference type of the
     * <code>SELF_REFERENCING_COLUMN</code>. This is defined in
     * <code>java.sql.Types</code>, and will be <code>null</code> if the
     * <code>DATA_TYPE</code> does not match these criteria.</li>
     * </ol>
     * If the driver does not support UDTs, the ResultSet will be empty.
     * 
     * @param catalog
     *            a Catalog Name. null is used to imply no narrowing of the
     *            search using Catalog Name. Otherwise, the name must match a
     *            Catalog Name held in the database, with "" used to retrieve
     *            those without a Catalog Name.
     * @param schemaPattern
     *            a Schema Name Pattern. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param typeNamePattern
     *            a Type Name, which should match a Type name as stored in the
     *            database. It may be fully qualified.
     * @param types
     *            a list of the UDT types to include in the list - one of
     *            DISTINCT, STRUCT or JAVA_OBJECT.
     * @return a ResultSet in the format described above
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getUDTs(String catalog, String schemaPattern,
            String typeNamePattern, int[] types) throws SQLException;

    /**
     * Returns the URL for this database.
     * 
     * @return the URL for the database. <code>null</code> if it cannot be
     *         generated.
     * @throws SQLException
     *             a database error occurred
     */
    public String getURL() throws SQLException;

    /**
     * Determine the user name as known by the database.
     * 
     * @return the user name
     * @throws SQLException
     *             a database error occurred
     */
    public String getUserName() throws SQLException;

    /**
     * Returns which of a table's columns are automatically updated when any
     * value in a row is updated.
     * <p>
     * The result is laid-out in the following columns:
     * <ol>
     * <li><code>SCOPE</code> - short - not used </li>
     * <li><code>COLUMN_NAME</code> - String - Column name</li>
     * <li><code>DATA_TYPE</code> - int - The SQL data type, as defined in
     * <code>java.sql.Types</code> </li>
     * <li><code>TYPE_NAME</code> - String - The SQL type name, data source
     * dependent </li>
     * <li><code>COLUMN_SIZE</code> - int - Precision for numeric types </li>
     * <li><code>BUFFER_LENGTH</code> - int - Length of a column value in
     * bytes </li>
     * <li><code>DECIMAL_DIGITS</code> - short - Number of digits after the
     * decimal point </li>
     * <li><code>PSEUDO_COLUMN</code> - short - If this is a pseudo-column
     * (for example, an Oracle ROWID):
     * <ul>
     * <li>DatabaseMetaData.bestRowUnknown - don't know whether this is a
     * pseudo column</li>
     * <li>DatabaseMetaData.bestRowNotPseudo - column is not pseudo</li>
     * <li>DatabaseMetaData.bestRowPseudo - column is a pseudo column</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * @param catalog
     *            a Catalog Name. <code>null</code> is used to imply no
     *            narrowing of the search using Catalog Name. Otherwise, the
     *            name must match a Catalog Name held in the database, with ""
     *            used to retrieve those without a Catalog Name.
     * @param schema
     *            a Schema Name Pattern. <code>null</code> is used to imply no
     *            narrowing of the search using Schema Name. Otherwise, the name
     *            must match a Schema name in the database, with "" used to
     *            retrieve those without a Schema name.
     * @param table
     *            a table name. It must match the name of a table in the
     *            database.
     * @return a ResultSet containing the descriptions, one row for each column,
     *         in the format defined above.
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getVersionColumns(String catalog, String schema,
            String table) throws SQLException;

    /**
     * Determine if a visible row insert can be detected by calling
     * ResultSet.rowInserted.
     * 
     * @param type
     *            the ResultSet type. This may be one of
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> or
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code> or
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     * @return <code>true</code> if ResultSet.rowInserted detects a visible
     *         row insert otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean insertsAreDetected(int type) throws SQLException;

    /**
     * Determine whether a fully qualified table name is prefixed or suffixed to
     * a fully qualified table name.
     * 
     * @return <code>true</code> if the catalog appears at the start of a
     *         fully qualified table name, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean isCatalogAtStart() throws SQLException;

    /**
     * Determine if the database is in read-only mode.
     * 
     * @return <code>true</code> if the database is in read-only mode,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean isReadOnly() throws SQLException;

    /**
     * Determine if updates are made to a copy of, or directly on, Large Objects
     * (LOBs).
     * 
     * @return <code>true</code> if updates are made to a copy of the Large
     *         Object, <code>false</code> otherwise
     * @throws SQLException
     *             a database error occurred
     */
    public boolean locatorsUpdateCopy() throws SQLException;

    /**
     * Determine if the database handles concatenations between
     * <code>NULL</code> and non-<code>NULL</code> values by producing a
     * <code>NULL</code> output.
     * 
     * @return <code>true</code> if <code>NULL</code> to non-<code>NULL</code>
     *         concatenations produce a <code>NULL</code> result,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean nullPlusNonNullIsNull() throws SQLException;

    /**
     * Determine if <code>NULL</code> values are always sorted to the end of
     * sorted results regardless of requested sort order. This means that they
     * will appear at the end of sorted lists whatever other non-<code>NULL</code>
     * values may be present.
     * 
     * @return <code>true</code> if <code>NULL</code> values are sorted at
     *         the end, <code>false</code> otherwise
     * @throws SQLException
     *             a database error occurred
     */
    public boolean nullsAreSortedAtEnd() throws SQLException;

    /**
     * Determine if <code>NULL</code> values are always sorted at the start of
     * the sorted list, irrespective of the sort order. This means that they
     * appear at the start of sorted lists, whatever other values may be
     * present.
     * 
     * @return <code>true</code> if <code>NULL</code> values are sorted at
     *         the start, <code>false</code> otherwise
     * @throws SQLException
     *             a database error occurred
     */
    public boolean nullsAreSortedAtStart() throws SQLException;

    /**
     * Determine if <code>NULL</code> values are sorted high - i.e. they are
     * sorted as if they are higher than any other values.
     * 
     * @return <code>true</code> if <code>NULL</code> values are sorted
     *         high, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean nullsAreSortedHigh() throws SQLException;

    /**
     * Determine if <code>NULL</code> values are sorted low - ie they are
     * sorted as if they are lower than any other values.
     * 
     * @return <code>true</code> if <code>NULL</code> values are sorted low,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean nullsAreSortedLow() throws SQLException;

    /**
     * Determine if deletes made by others are visible, for a specified
     * ResultSet type.
     * 
     * @param type
     *            the type of the ResultSet. It may be either
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code> or
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>)
     * @return <code>true</code> if others' deletes are visible,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean othersDeletesAreVisible(int type) throws SQLException;

    /**
     * Determine if inserts made by others are visible, for a specified
     * ResultSet type.
     * 
     * @param type
     *            the type of the ResultSet. May be
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>, or
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if others' inserts are visible otherwise
     *         <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean othersInsertsAreVisible(int type) throws SQLException;

    /**
     * Determine if updates made by others are visible, for a specified
     * ResultSet type.
     * 
     * @param type
     *            the type of the ResultSet. May be
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>, or
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if others' inserts are visible otherwise
     *         <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean othersUpdatesAreVisible(int type) throws SQLException;

    /**
     * Determine if a ResultSet's own deletes are visible, for a specified
     * ResultSet type.
     * 
     * @param type
     *            the type of the ResultSet:
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if the delete's are seen by the own ResultSet
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean ownDeletesAreVisible(int type) throws SQLException;

    /**
     * Determine if its own inserts are visible to a given ResultSet type.
     * 
     * @param type
     *            the type of the ResultSet:
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if inserts are visible for this type
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean ownInsertsAreVisible(int type) throws SQLException;

    /**
     * Determine if for a supplied type of ResultSet, the ResultSet's own
     * updates are visible.
     * 
     * @param type
     *            the type of the ResultSet:
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if updates are visible to in this ResultSet
     *         type otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean ownUpdatesAreVisible(int type) throws SQLException;

    /**
     * Determine whether the database treats SQL identifiers that are in mixed
     * case (and unquoted) as case insensitive. If true then the database stores
     * them in lower case.
     * 
     * @return <code>true</code> if unquoted SQL identifiers are stored in
     *         lower case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException;

    /**
     * Determine whether the database considers mixed case quoted SQL
     * identifiers as case insensitive and stores them in lower case.
     * 
     * @return <code>true</code> if quoted SQL identifiers are stored in lower
     *         case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determine whether the database considers mixed case unquoted SQL
     * identifiers as case insensitive and stores them in mixed case.
     * 
     * @return <code>true</code> if unquoted SQL identifiers as stored in
     *         mixed case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException;

    /**
     * Determine whether the database considers identifiers as case insensitive
     * if they are mixed case quoted SQL. The database stores them in mixed
     * case.
     * 
     * @return <code>true</code> if quoted SQL identifiers are stored in mixed
     *         case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determine whether the database considers mixed case unquoted SQL
     * identifiers as case insensitive and stores them in upper case.
     * 
     * @return <code>true</code> if unquoted SQL identifiers are stored in
     *         upper case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException;

    /**
     * Determine whether the database considers mixed case quoted SQL
     * identifiers as case insensitive and stores them in upper case.
     * 
     * @return <code>true</code> if quoted SQL identifiers are stored in upper
     *         case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determine if the database supports ALTER TABLE operation with add column.
     * 
     * @return <code>true</code> if ALTER TABLE with add column is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsAlterTableWithAddColumn() throws SQLException;

    /**
     * Determine if the database supports ALTER TABLE operation with drop
     * column.
     * 
     * @return <code>true</code> if ALTER TABLE with drop column is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsAlterTableWithDropColumn() throws SQLException;

    /**
     * Determine if the database supports the ANSI92 entry level SQL grammar.
     * 
     * @return <code>true</code> if the ANSI92 entry level SQL grammar is
     *         supported, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException;

    /**
     * Determine if the database supports the ANSI92 full SQL grammar.
     * 
     * @return <code>true</code> if the ANSI92 full SQL grammar is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsANSI92FullSQL() throws SQLException;

    /**
     * Determine if the database supports the ANSI92 intermediate SQL Grammar.
     * 
     * @return <code>true</code> if the ANSI92 intermediate SQL grammar is
     *         supported, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsANSI92IntermediateSQL() throws SQLException;

    /**
     * Determine if the database supports Batch Updates.
     * 
     * @return <code>true</code> if batch updates are supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsBatchUpdates() throws SQLException;

    /**
     * Determine whether catalog names may be used in data manipulation
     * statements.
     * 
     * @return <code>true</code> if catalog names can be used in data
     *         manipulation statements, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCatalogsInDataManipulation() throws SQLException;

    /**
     * Determine if catalog names can be used in Index Definition statements.
     * 
     * @return <code>true</code> if catalog names can be used in Index
     *         Definition statements, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException;

    /**
     * Determine if catalog names can be used in privilege definition
     * statements.
     * 
     * @return <code>true</code> if catalog names can be used in privilege
     *         definition statements, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException;

    /**
     * Determine if catalog names can be used in procedure call statements.
     * 
     * @return <code>true</code> if catalog names can be used in procedure
     *         call statements.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException;

    /**
     * Determine if catalog names may be used in table definition statements.
     * 
     * @return <code>true</code> if catalog names can be used in definition
     *         statements, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException;

    /**
     * Determine if the database supports column aliasing.
     * <p>
     * If aliasing is supported, then the SQL AS clause is used to provide names
     * for computed columns and provide alias names for columns.
     * 
     * @return <code>true</code> if column aliasing is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsColumnAliasing() throws SQLException;

    /**
     * Determine if the database supports the CONVERT operation between SQL
     * types.
     * 
     * @return <code>true</code> if the CONVERT operation is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsConvert() throws SQLException;

    /**
     * Determine if the database supports CONVERT operation for two supplied SQL
     * types.
     * 
     * @param fromType
     *            the Type to convert from, as defined by
     *            <code>java.sql.Types</code>
     * @param toType
     *            the Type to convert to, as defined by
     *            <code>java.sql.Types</code>
     * @return <code>true</code> if the CONVERT operation is supported for
     *         these types, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsConvert(int fromType, int toType)
            throws SQLException;

    /**
     * Determine if the database supports the Core SQL Grammar for ODBC.
     * 
     * @return <code>true</code> if the Core SQL Grammar is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCoreSQLGrammar() throws SQLException;

    /**
     * Determine if the database supports correlated sub-queries.
     * 
     * @return <code>true</code> if the database does support correlated
     *         sub-queries and <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsCorrelatedSubqueries() throws SQLException;

    /**
     * Determine if the database allows both data definition and data
     * manipulation statements inside a transaction.
     * 
     * @return <code>true</code> if both types of statement are permitted,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions()
            throws SQLException;

    /**
     * Determine if the database only allows data manipulation statements inside
     * a transaction.
     * 
     * @return <code>true</code> if only data manipulation statements are
     *         permitted, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsDataManipulationTransactionsOnly()
            throws SQLException;

    /**
     * Determine if table correlation names are restricted to be different from
     * the names of the tables, when they are supported.
     * 
     * @return <code>true</code> if correlation names must be different to
     *         table names, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsDifferentTableCorrelationNames() throws SQLException;

    /**
     * Determine whether expressions in ORDER BY lists are supported.
     * 
     * @return <code>true</code> if expressions in ORDER BY lists are
     *         supported.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsExpressionsInOrderBy() throws SQLException;

    /**
     * Determine whether the Extended SQL Grammar for ODBC is supported.
     * 
     * @return <code>true</code> if the Extended SQL Grammar is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsExtendedSQLGrammar() throws SQLException;

    /**
     * Determine if the database supports full nested outer joins.
     * 
     * @return <code>true</code> if full nested outer joins are supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsFullOuterJoins() throws SQLException;

    /**
     * Determine if auto generated keys can be returned when a statement
     * executes.
     * 
     * @return <code>true</code> if auto generated keys can be returned,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsGetGeneratedKeys() throws SQLException;

    /**
     * Determine if the database supports a form of GROUP BY clause.
     * 
     * @return <code>true</code> if a form of GROUP BY clause is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsGroupBy() throws SQLException;

    /**
     * Determine if the database supports using a column name in a GROUP BY
     * clause not included in the SELECT statement as long as all of the columns
     * in the SELECT statement are used in the GROUP BY clause.
     * 
     * @return <code>true</code> if GROUP BY clauses can use column names in
     *         this way, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsGroupByBeyondSelect() throws SQLException;

    /**
     * Determine if the database supports using a column name in a GROUP BY
     * clause that is not in the SELECT statement.
     * 
     * @return <code>true</code> if GROUP BY clause can use a column name not
     *         in the SELECT statement, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsGroupByUnrelated() throws SQLException;

    /**
     * Determine whether the database supports SQL Integrity Enhancement
     * Facility.
     * 
     * @return <code>true</code> if the Integrity Enhancement Facility is
     *         supported, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsIntegrityEnhancementFacility() throws SQLException;

    /**
     * Determine if the database supports using a LIKE escape clause.
     * 
     * @return <code>true</code> if LIKE escape clause is supported,
     *         <code>false</code> otherwise
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsLikeEscapeClause() throws SQLException;

    /**
     * Determine if the database provides limited support for outer Join
     * operations.
     * 
     * @return <code>true</code> if there is limited support for outer Join
     *         operations, <code>false</code> otherwise. This will be
     *         <code>true</code> if <code>supportsFullOuterJoins</code>
     *         returns <code>true</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsLimitedOuterJoins() throws SQLException;

    /**
     * Determine if the database supports Minimum SQL Grammar for ODBC.
     * 
     * @return <code>true</code> if the Minimum SQL Grammar is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException;

    /**
     * Determine if the database treats mixed case unquoted SQL identifiers as
     * case sensitive storing them in mixed case.
     * 
     * @return <code>true</code> if unquoted SQL identifiers are stored in
     *         mixed case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsMixedCaseIdentifiers() throws SQLException;

    /**
     * Determine whether the database considers mixed case quoted SQL
     * identifiers as case sensitive, storing them in mixed case.
     * 
     * @return <code>true</code> if quoted SQL identifiers are stored in mixed
     *         case, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determine if it is possible for a single CallableStatement to return
     * multiple ResultSets simultaneously.
     * 
     * @return <code>true</code> if a single CallableStatement can return
     *         multiple ResultSets simultaneously, <code>false</code>
     *         otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsMultipleOpenResults() throws SQLException;

    /**
     * Determine whether retrieving multiple ResultSets from a single call to
     * the <code>execute</code> method is supported.
     * 
     * @return <code>true</code> if multiple ResultSets can be retrieved,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsMultipleResultSets() throws SQLException;

    /**
     * Determine whether multiple transactions in progress at at time on
     * different connections are supported.
     * 
     * @return <code>true</code> if multiple open transactions are supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsMultipleTransactions() throws SQLException;

    /**
     * Determine whether call-able statements with named parameters is
     * supported.
     * 
     * @return <code>true</code> if named parameters can be used with
     *         call-able statements, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsNamedParameters() throws SQLException;

    /**
     * Determine if columns in the database can be defined as non-nullable.
     * 
     * @return <code>true</code> if Columns can be defined non-nullable,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsNonNullableColumns() throws SQLException;

    /**
     * Determine whether keeping Cursors open across Commit operations is
     * supported.
     * 
     * @return <code>true</code> if Cursors can be kept open across Commit
     *         operations, <code>false</code> if they might get closed.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException;

    /**
     * Determine if the database can keep Cursors open across Rollback
     * operations.
     * 
     * @return <code>true</code> if Cursors can be kept open across Rollback
     *         operations, <code>false</code> if they might get closed.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException;

    /**
     * Determine whether keeping Statements open across Commit operations is
     * supported.
     * 
     * @return <code>true</code> if Statements can be kept open,
     *         <code>false</code> if they might not.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException;

    /**
     * Determine whether keeping Statements open across Rollback operations is
     * supported.
     * 
     * @return <code>true</code> if Statements can be kept open,
     *         <code>false</code> if they might not.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException;

    /**
     * Determine whether using a column in an ORDER BY clause that is not in the
     * SELECT statement is supported.
     * 
     * @return <code>true</code> if it is possible to ORDER using a column not
     *         in the SELECT, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsOrderByUnrelated() throws SQLException;

    /**
     * Determine whether outer join operations are supported.
     * 
     * @return <code>true</code> if outer join operations are supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsOuterJoins() throws SQLException;

    /**
     * Determine whether positioned DELETE statements are supported.
     * 
     * @return <code>true</code> if the database supports positioned DELETE
     *         statements.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsPositionedDelete() throws SQLException;

    /**
     * Determine whether positioned UPDATE statements are supported.
     * 
     * @return <code>true</code> if the database supports positioned UPDATE
     *         statements, <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsPositionedUpdate() throws SQLException;

    /**
     * Determine whether there is support for a given concurrency style for the
     * given ResultSet.
     * 
     * @param type
     *            the ResultSet type, as defined in
     *            <code>java.sql.ResultSet</code>:
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param concurrency
     *            a concurrency type, which may be one of
     *            <code>ResultSet.CONCUR_READ_ONLY</code> or
     *            <code>ResultSet.CONCUR_UPDATABLE</code>.
     * @return <code>true</code> if that concurrency and ResultSet type
     *         pairing is supported otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsResultSetConcurrency(int type, int concurrency)
            throws SQLException;

    /**
     * Determine whether the supplied ResultSet holdability is supported.
     * 
     * @param holdability
     *            as specified in java.sql.ResultSet:
     *            ResultSet.HOLD_CURSORS_OVER_COMMIT or
     *            ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @return <code>true</code> if the given ResultSet holdability is
     *         supported and if it isn't then <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsResultSetHoldability(int holdability)
            throws SQLException;

    /**
     * Determine whether the supplied ResultSet type is supported.
     * 
     * @param type
     *            the ResultSet type as defined in java.sql.ResultSet:
     *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> if the ResultSet type is supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsResultSetType(int type) throws SQLException;

    /**
     * Determine whether Savepoints for transactions are supported.
     * 
     * @return <code>true</code> if Savepoints are supported,
     *         <code>false</code> otherwise.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSavepoints() throws SQLException;

    /**
     * Determine whether a schema name may be used in a data manipulation
     * statement.
     * 
     * @return <code>true</code> if a schema name can be used in a data
     *         manipulation otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSchemasInDataManipulation() throws SQLException;

    /**
     * Determine whether a schema name may be used in an index definition
     * statement.
     * 
     * @return <code>true</code> if a schema name can be used in an index
     *         definition otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException;

    /**
     * Determine whether a database schema name can be used in a privilege
     * definition statement.
     * 
     * @return <code>true</code> if a database schema name may be used in a
     *         privilege definition otherwise <code>false</code>
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException;

    /**
     * Determine if a procedure call statement may be contain in a schema name.
     * 
     * @return <code>true</code> if a schema name can be used in a procedure
     *         call otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSchemasInProcedureCalls() throws SQLException;

    /**
     * Determine if a schema name can be used in a table definition statement.
     * 
     * @return <code>true</code> if a schema name can be used in a table
     *         definition otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSchemasInTableDefinitions() throws SQLException;

    /**
     * Determine if this <code>SELECT FOR UPDATE</code> statements ar
     * supported.
     * 
     * @return <code>true</code> if <code>SELECT FOR UPDATE</code>
     *         statements are supported otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSelectForUpdate() throws SQLException;

    /**
     * Determine whether statement pooling is supported.
     * 
     * @return <code>true</code> of the database does support statement
     *         pooling otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsStatementPooling() throws SQLException;

    /**
     * Determine whether stored procedure calls using the stored procedure
     * escape syntax is supported.
     * 
     * @return <code>true</code> if stored procedure calls using the stored
     *         procedure escape syntax are supported otherwise
     *         <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsStoredProcedures() throws SQLException;

    /**
     * Determine whether subqueries in comparison expressions are supported.
     * 
     * @return <code>true</code> if subqueries are supported in comparison
     *         expressions.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSubqueriesInComparisons() throws SQLException;

    /**
     * Determine whether subqueries in EXISTS expressions are supported.
     * 
     * @return <code>true</code> if subqueries are supported in EXISTS
     *         expressions otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSubqueriesInExists() throws SQLException;

    /**
     * Determine whether subqueries in <code>IN</code> statements are
     * supported.
     * 
     * @return <code>true</code> if subqueries are supported in IN statements
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSubqueriesInIns() throws SQLException;

    /**
     * Determine whether subqueries in quantified expressions are supported.
     * 
     * @return <code>true</code> if subqueries are supported otherwise
     *         <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException;

    /**
     * Determine whether the database has table correlation names support.
     * 
     * @return <code>true</code> if table correlation names are supported
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsTableCorrelationNames() throws SQLException;

    /**
     * Determine whether a specified transaction isolation level is supported.
     * 
     * @param level
     *            the transaction isolation level, as specified in
     *            <code>java.sql.Connection</code>:
     *            <code>TRANSACTION_NONE</code>,
     *            <code>TRANSACTION_READ_COMMITTED</code>,
     *            <code>TRANSACTION_READ_UNCOMMITTED</code>,
     *            <code>TRANSACTION_REPEATABLE_READ</code>,
     *            <code>TRANSACTION_SERIALIZABLE</code>
     * @return <code>true</code> if the specific isolation level is supported
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsTransactionIsolationLevel(int level)
            throws SQLException;

    /**
     * Determine whether transactions are supported.
     * <p>
     * If transactions are not supported, then the <code>commit</code> method
     * does nothing and the transaction isolation level is always
     * <code>TRANSACTION_NONE</code>.
     * 
     * @return <code>true</code> if transactions are supported otherwise
     *         <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsTransactions() throws SQLException;

    /**
     * Determine whether the <code>SQL UNION</code> operation is supported.
     * 
     * @return <code>true</code> of the database does support
     *         <code>UNION</code> otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsUnion() throws SQLException;

    /**
     * Determine whether the <code>SQL UNION ALL</code> operation is
     * supported.
     * 
     * @return <code>true</code> if the database does support UNION ALL
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean supportsUnionAll() throws SQLException;

    /**
     * Determine if the method <code>ResultSet.rowUpdated</code> can detect a
     * visible row update.
     * 
     * @param type
     *            ResultSet type: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @return <code>true</code> detecting changes is possible otherwise
     *         <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean updatesAreDetected(int type) throws SQLException;

    /**
     * Determine if this database uses a file for each table.
     * 
     * @return <code>true</code> if the database uses one file for each table
     *         otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean usesLocalFilePerTable() throws SQLException;

    /**
     * Determine whether this database uses a local file to store tables.
     * 
     * @return <code>true</code> of the database does store tables in a local
     *         file otherwise <code>false</code>.
     * @throws SQLException
     *             a database error occurred
     */
    public boolean usesLocalFiles() throws SQLException;
}
