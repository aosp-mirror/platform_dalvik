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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Formatter to convert a {@link LogRecord} into an XML string. The DTD
 * specified in Appendix A to the Java Logging APIs specification is used.
 * {@code XMLFormatter} uses the output handler's encoding if it is specified,
 * otherwise the default platform encoding is used instead. UTF-8 is the
 * recommended encoding.
 */
public class XMLFormatter extends Formatter {

    private static final String lineSeperator = LogManager
            .getSystemLineSeparator();

    private static final String indent = "    "; //$NON-NLS-1$

    /**
     * Constructs a new {@code XMLFormatter}.
     */
    public XMLFormatter() {
        super();
    }

    /**
     * Converts a {@code LogRecord} into an XML string.
     *
     * @param r
     *            the log record to be formatted.
     * @return the log record formatted as an XML string.
     */
    @SuppressWarnings("nls")
    @Override
    public String format(LogRecord r) {
        // call a method of LogRecord to ensure not null
        long time = r.getMillis();
        // format to date
        String date = MessageFormat.format("{0, date} {0, time}",
                new Object[] { new Date(time) });

        StringBuilder sb = new StringBuilder();
        sb.append(("<record>")).append(lineSeperator);
        sb.append(indent).append(("<date>")).append(date).append(("</date>"))
                .append(lineSeperator);
        sb.append(indent).append(("<millis>")).append(time).append(
                ("</millis>")).append(lineSeperator);
        sb.append(indent).append(("<sequence>")).append(r.getSequenceNumber())
                .append(("</sequence>")).append(lineSeperator);
        if (null != r.getLoggerName()) {
            sb.append(indent).append(("<logger>")).append(r.getLoggerName())
                    .append(("</logger>")).append(lineSeperator);
        }
        sb.append(indent).append(("<level>")).append(r.getLevel().getName())
                .append(("</level>")).append(lineSeperator);
        if (null != r.getSourceClassName()) {
            sb.append(indent).append(("<class>"))
                    .append(r.getSourceClassName()).append(("</class>"))
                    .append(lineSeperator);
        }
        if (null != r.getSourceMethodName()) {
            sb.append(indent).append(("<method>")).append(
                    r.getSourceMethodName()).append(("</method>")).append(
                    lineSeperator);
        }
        sb.append(indent).append(("<thread>")).append(r.getThreadID()).append(
                ("</thread>")).append(lineSeperator);
        formatMessages(r, sb);
        Object[] params;
        if ((params = r.getParameters()) != null) {
            for (Object element : params) {
                sb.append(indent).append(("<param>")).append(element).append(
                        ("</param>")).append(lineSeperator);
            }
        }
        formatThrowable(r, sb);
        sb.append(("</record>")).append(lineSeperator);
        return sb.toString();
    }

    @SuppressWarnings("nls")
    private void formatMessages(LogRecord r, StringBuilder sb) {
        // get localized message if has, but don't call Formatter.formatMessage
        // to parse pattern string
        ResourceBundle rb = r.getResourceBundle();
        String pattern = r.getMessage();
        if (null != rb && null != pattern) {
            String message;
            try {
                message = rb.getString(pattern);
            } catch (Exception e) {
                message = null;
            }

            if (message == null) {
                message = pattern;
                sb.append(indent).append(("<message>")).append(message).append(
                        ("</message>")).append(lineSeperator);
            } else {
                sb.append(indent).append(("<message>")).append(message).append(
                        ("</message>")).append(lineSeperator);
                sb.append(indent).append(("<key>")).append(pattern).append(
                        ("</key>")).append(lineSeperator);
                sb.append(indent).append(("<catalog>")).append(
                        r.getResourceBundleName()).append(("</catalog>"))
                        .append(lineSeperator);
            }
        } else if (null != pattern) {
            sb.append(indent).append(("<message>")).append(pattern).append(
                    ("</message>")).append(lineSeperator);
        } else {
            sb.append(indent).append(("<message/>"));
        }
    }

    @SuppressWarnings("nls")
    private void formatThrowable(LogRecord r, StringBuilder sb) {
        Throwable t;
        if ((t = r.getThrown()) != null) {
            sb.append(indent).append("<exception>").append(lineSeperator);
            sb.append(indent).append(indent).append("<message>").append(
                    t.toString()).append("</message>").append(lineSeperator);
            // format throwable's stack trace
            StackTraceElement[] elements = t.getStackTrace();
            for (StackTraceElement e : elements) {
                sb.append(indent).append(indent).append("<frame>").append(
                        lineSeperator);
                sb.append(indent).append(indent).append(indent).append(
                        "<class>").append(e.getClassName()).append("</class>")
                        .append(lineSeperator);
                sb.append(indent).append(indent).append(indent).append(
                        "<method>").append(e.getMethodName()).append(
                        "</method>").append(lineSeperator);
                sb.append(indent).append(indent).append(indent)
                        .append("<line>").append(e.getLineNumber()).append(
                                "</line>").append(lineSeperator);
                sb.append(indent).append(indent).append("</frame>").append(
                        lineSeperator);
            }
            sb.append(indent).append("</exception>").append(lineSeperator);
        }
    }

    /**
     * Returns the header string for a set of log records formatted as XML
     * strings, using the output handler's encoding if it is defined, otherwise
     * using the default platform encoding.
     *
     * @param h
     *            the output handler, may be {@code null}.
     * @return the header string for log records formatted as XML strings.
     */
    @SuppressWarnings("nls")
    @Override
    public String getHead(Handler h) {
        String encoding = null;
        if (null != h) {
            encoding = h.getEncoding();
        }
        if (null == encoding) {
            encoding = getSystemProperty("file.encoding");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"").append(encoding).append(
                "\" standalone=\"no\"?>").append(lineSeperator);
        sb.append("<!DOCTYPE log SYSTEM \"logger.dtd\">").append(lineSeperator);
        sb.append(("<log>"));
        return sb.toString();
    }

    /**
     * Returns the tail string for a set of log records formatted as XML
     * strings.
     *
     * @param h
     *            the output handler, may be {@code null}.
     * @return the tail string for log records formatted as XML strings.
     */
    @Override
    public String getTail(Handler h) {
        return "</log>"; //$NON-NLS-1$
    }

    // use privilege code to get system property
    private static String getSystemProperty(final String key) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key);
            }
        });
    }
}
