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

import java.util.List;

/**
 * <p>
 * The management interface for the logging sub-system.
 * </p>
 * 
 * <p>
 * ObjectName =
 * {@link LogManager#LOGGING_MXBEAN_NAME java.util.logging:type=Logging}
 * </p>
 * 
 * @since 1.5
 */
public interface LoggingMXBean {
    /**
     * <p>
     * Gets the String value of the logging level of a logger. An empty String
     * is returned when the logger's level is defined by its parent.
     * </p>
     * 
     * @param loggerName The name of the logger lookup.
     * @return A String if the logger was found, otherwise <code>null</code>.
     * @see Level#getName()
     */
    String getLoggerLevel(String loggerName);

    /**
     * <p>
     * Gets a list of all currently registered logger's names. This is performed
     * using the {@link LogManager#getLoggerNames()}.
     * </p>
     * 
     * @return A List of String instances.
     */
    List<String> getLoggerNames();

    /**
     * <p>
     * Gets the name of the parent logger of a logger. If the logger doesn't
     * exist then <code>null</code> is returned. If the logger is the root
     * logger, then an empty String is returned.
     * </p>
     * 
     * @param loggerName The name of the logger to lookup.
     * @return A String if the logger was found, otherwise <code>null</code>.
     */
    String getParentLoggerName(String loggerName);

    /**
     * <p>
     * Sets the log level of a logger.
     * </p>
     * 
     * @param loggerName The name of the logger to set the level on, which must
     *        not be <code>null</code>.
     * @param levelName The level to set on the logger, which may be
     *        <code>null</code>.
     * @throws IllegalArgumentException if <code>loggerName</code> is not a
     *         registered logger or if <code>levelName</code> is not null and
     *         an invalid value.
     * @throws SecurityException if a security manager exists and the caller
     *         doesn't have LoggingPermission("control").
     * @see Level#parse(String)
     */
    void setLoggerLevel(String loggerName, String levelName);
}
