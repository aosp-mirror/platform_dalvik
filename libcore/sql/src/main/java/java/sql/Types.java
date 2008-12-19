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
 * A class which defines constants used to identify generic SQL types, also
 * called JDBC types. The type constant values are equivalent to those defined
 * by X/OPEN.
 * 
 * @since Android 1.0
 */
public class Types {

    /*
     * Private constructor to prevent instantiation.
     */
    private Types() {
        super();
    }

    /**
     * The type code that identifies the SQL type {@code ARRAY}.
     * 
     * @since Android 1.0
     */
    public static final int ARRAY = 2003;

    /**
     * The type code that identifies the SQL type {@code BIGINT}.
     * 
     * @since Android 1.0
     */
    public static final int BIGINT = -5;

    /**
     * The type code that identifies the SQL type {@code BINARY}.
     * 
     * @since Android 1.0
     */
    public static final int BINARY = -2;

    /**
     * The type code that identifies the SQL type {@code BIT}.
     * 
     * @since Android 1.0
     */
    public static final int BIT = -7;

    /**
     * The type code that identifies the SQL type {@code BLOB}.
     * 
     * @since Android 1.0
     */
    public static final int BLOB = 2004;

    /**
     * The type code that identifies the SQL type {@code BOOLEAN}.
     * 
     * @since Android 1.0
     */
    public static final int BOOLEAN = 16;

    /**
     * The type code that identifies the SQL type {@code CHAR}.
     * 
     * @since Android 1.0
     */
    public static final int CHAR = 1;

    /**
     * The type code that identifies the SQL type {@code CLOB}.
     * 
     * @since Android 1.0
     */
    public static final int CLOB = 2005;

    /**
     * The type code that identifies the SQL type {@code DATALINK}.
     * 
     * @since Android 1.0
     */
    public static final int DATALINK = 70;

    /**
     * The type code that identifies the SQL type {@code DATE}.
     * 
     * @since Android 1.0
     */
    public static final int DATE = 91;

    /**
     * The type code that identifies the SQL type {@code DECIMAL}.
     * 
     * @since Android 1.0
     */
    public static final int DECIMAL = 3;

    /**
     * The type code that identifies the SQL type {@code DISTINCT}.
     * 
     * @since Android 1.0
     */
    public static final int DISTINCT = 2001;

    /**
     * The type code that identifies the SQL type {@code DOUBLE}.
     * 
     * @since Android 1.0
     */
    public static final int DOUBLE = 8;

    /**
     * The type code that identifies the SQL type {@code FLOAT}.
     * 
     * @since Android 1.0
     */
    public static final int FLOAT = 6;

    /**
     * The type code that identifies the SQL type {@code INTEGER}.
     * 
     * @since Android 1.0
     */
    public static final int INTEGER = 4;

    /**
     * The type code that identifies the SQL type {@code JAVA_OBJECT}.
     * 
     * @since Android 1.0
     */
    public static final int JAVA_OBJECT = 2000;

    /**
     * The type code that identifies the SQL type {@code LONGVARBINARY}.
     * 
     * @since Android 1.0
     */
    public static final int LONGVARBINARY = -4;

    /**
     * The type code that identifies the SQL type {@code LONGVARCHAR}.
     * 
     * @since Android 1.0
     */
    public static final int LONGVARCHAR = -1;

    /**
     * The type code that identifies the SQL type {@code NULL}.
     * 
     * @since Android 1.0
     */
    public static final int NULL = 0;

    /**
     * The type code that identifies the SQL type {@code NUMERIC}.
     * 
     * @since Android 1.0
     */
    public static final int NUMERIC = 2;

    /**
     * The type code that identifies that the SQL type is database specific and
     * is mapped to a Java object, accessed via the methods
     * {@code getObject} and {@code setObject}.
     * 
     * @since Android 1.0
     */
    public static final int OTHER = 1111;

    /**
     * The type code that identifies the SQL type {@code REAL}.
     * 
     * @since Android 1.0
     */
    public static final int REAL = 7;

    /**
     * The type code that identifies the SQL type {@code REF}.
     * 
     * @since Android 1.0
     */
    public static final int REF = 2006;

    /**
     * The type code that identifies the SQL type {@code SMALLINT}.
     * 
     * @since Android 1.0
     */
    public static final int SMALLINT = 5;

    /**
     * The type code that identifies the SQL type {@code STRUCT}.
     * 
     * @since Android 1.0
     */
    public static final int STRUCT = 2002;

    /**
     * The type code that identifies the SQL type {@code TIME}.
     * 
     * @since Android 1.0
     */
    public static final int TIME = 92;

    /**
     * The type code that identifies the SQL type {@code TIMESTAMP}.
     * 
     * @since Android 1.0
     */
    public static final int TIMESTAMP = 93;

    /**
     * The type code that identifies the SQL type {@code TINYINT}.
     * 
     * @since Android 1.0
     */
    public static final int TINYINT = -6;

    /**
     * The type code that identifies the SQL type {@code VARBINARY}.
     * 
     * @since Android 1.0
     */
    public static final int VARBINARY = -3;

    /**
     * The type code that identifies the SQL type {@code VARCHAR}.
     * 
     * @since Android 1.0
     */
    public static final int VARCHAR = 12;
}
