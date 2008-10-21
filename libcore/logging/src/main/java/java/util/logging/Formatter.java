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


package java.util.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * <code>Formatter</code> objects are used to format <code>LogRecord</code>
 * objects into a string representation. Head and tail strings are sometime used
 * to wrap a set of records. The <code>getHead</code> and <code>getTail</code>
 * methods are presented for this purpose.
 * 
 */
public abstract class Formatter {

    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    /**
     * Constructs a <code>Formatter</code> object.
     */
    protected Formatter() {
        super();
    }

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Formats a <code>LogRecord</code> object into a string representation.
     * The resulted string is usually localized and includes the message field
     * of the supplied <code>LogRecord</code> object.
     * 
     * @param r
     *            the log record to be formatted into a string
     * @return the string resulted from the formatting
     */
    public abstract String format(LogRecord r);

    /**
     * Formats a <code>LogRecord</code> object into a localized string
     * representation. This method can be regarded as a convenience for
     * subclasses of <code>Formatter</code> to use.
     * <p>
     * The message string is firstly localized using the
     * <code>ResourceBundle</code> object associated with the supplied
     * <code>LogRecord</code>.
     * </p>
     * 
     * @param r
     *            the log record to be formatted
     * @return the string resulted from the formatting
     */
    public String formatMessage(LogRecord r) {
        String pattern = r.getMessage();
        ResourceBundle rb = null;
        // try to localize the message string first
        if (null != (rb = r.getResourceBundle())) {
            try {
                pattern = rb.getString(pattern);
            } catch (Exception e) {
                pattern = r.getMessage();
            }
        }
        if (null != pattern) {
            Object[] params = r.getParameters();
            /*
             * if the message contains "{0", use java.text.MessageFormat to
             * format the string
             */
            if (pattern.indexOf("{0") >= 0 && null != params //$NON-NLS-1$
                    && params.length > 0) {
                try {
                    pattern = MessageFormat.format(pattern, params);
                } catch (IllegalArgumentException e) {
                    pattern = r.getMessage();
                }
            }
        }
        return pattern;
    }

    /**
     * Gets the head string used to wrap a set of log records. This base class
     * always returns the empty string.
     * 
     * @param h
     *            the target handler
     * @return the head string used to wrap a set of log records
     */
    @SuppressWarnings("unused")
    public String getHead(Handler h) {
        return ""; //$NON-NLS-1$
    }

    /**
     * Gets the tail string used to wrap a set of log records. This base class
     * always returns the empty string.
     * 
     * @param h
     *            the target handler
     * @return the tail string used to wrap a set of log records
     */
    @SuppressWarnings("unused")
    public String getTail(Handler h) {
        return ""; //$NON-NLS-1$
    }

}

