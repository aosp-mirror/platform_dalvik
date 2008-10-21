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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.harmony.logging.internal.nls.Messages;

/**
 * A <code>LogRecord</code> object represents a logging request. It is passed
 * between the logging framework and individual logging handlers. Client
 * applications should not modify a <code>LogRecord</code> object that has
 * been passed into the logging framework.
 * <p>
 * The <code>LogRecord</code> class will infer the source method name and
 * source class name the first time they are accessed if the client application
 * didn't specify them explicitly. This automatic inference is based on the
 * analysis of the call stack and is not guaranteed to be precise. Client
 * applications should force the initialization of these two fields by calling
 * <code>getSourceClassName</code> or <code>getSourceMethodName</code> if
 * they expect to use them after passing the <code>LogRecord</code> object to
 * another thread or transmitting it over RMI.
 * </p>
 * 
 */
public class LogRecord implements Serializable {

    private static final long serialVersionUID = 5372048053134512534L;

    // The major byte used in serialization.
    private static final int MAJOR = 1;

    // The minor byte used in serialization.
    private static final int MINOR = 4;

    // Store the current value for the sequence number.
    private static long currentSequenceNumber = 0;

    // Store the id for each thread.
    private static ThreadLocal<Integer> currentThreadId = new ThreadLocal<Integer>();

    // The base id as the starting point for thread ID allocation.
    private static int initThreadId = 0;

    /**
     * The logging level.
     * 
     * @serial
     */
    private Level level;

    /**
     * The sequence number.
     * 
     * @serial
     */
    private long sequenceNumber;

    /**
     * The name of the class that issued the logging call.
     * 
     * @serial
     */
    private String sourceClassName;

    /**
     * The name of the method that issued the logging call.
     * 
     * @serial
     */
    private String sourceMethodName;

    /**
     * The original message text.
     * 
     * @serial
     */
    private String message;

    /**
     * The ID of the thread that issued the logging call.
     * 
     * @serial
     */
    private int threadID;

    /**
     * The time that the event occurred, in milliseconds since 1970.
     * 
     * @serial
     */
    private long millis;

    /**
     * The associated <code>Throwable</code> object if any.
     * 
     * @serial
     */
    private Throwable thrown;

    /**
     * The name of the source logger.
     * 
     * @serial
     */
    private String loggerName;

    /**
     * The name of the resource bundle used to localize the log message.
     * 
     * @serial
     */
    private String resourceBundleName;

    // The associated resource bundle if any.
    private transient ResourceBundle resourceBundle;

    // The parameters.
    private transient Object[] parameters;

    // If the source method and source class has been initialized
    private transient boolean sourceInited;

    /**
     * Constructs a <code>LogRecord</code> object using the supplied the
     * logging level and message. The millis property is set to the current
     * time. The sequence property is set to a new unique value, allocated in
     * increasing order within a VM. The thread ID is set to a unique value for
     * the current thread. All other properties are set to <code>null</code>.
     * 
     * @param level
     *            the logging level which may not be null
     * @param msg
     *            the raw message
     */
    public LogRecord(Level level, String msg) {
        if (null == level) {
            // logging.4=The 'level' parameter is null.
            throw new NullPointerException(Messages.getString("logging.4")); //$NON-NLS-1$
        }
        this.level = level;
        this.message = msg;
        this.millis = System.currentTimeMillis();

        synchronized (LogRecord.class) {
            this.sequenceNumber = currentSequenceNumber++;
            Integer id = currentThreadId.get();
            if (null == id) {
                this.threadID = initThreadId;
                currentThreadId.set(Integer.valueOf(initThreadId++));
            } else {
                this.threadID = id.intValue();
            }
        }

        this.sourceClassName = null;
        this.sourceMethodName = null;
        this.loggerName = null;
        this.parameters = null;
        this.resourceBundle = null;
        this.resourceBundleName = null;
        this.thrown = null;
    }

    /**
     * Gets the logging level.
     * 
     * @return the logging level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Sets the logging level.
     * 
     * @param level
     *            the level to set
     */
    public void setLevel(Level level) {
        if (null == level) {
            // logging.4=The 'level' parameter is null.
            throw new NullPointerException(Messages.getString("logging.4")); //$NON-NLS-1$
        }
        this.level = level;
    }

    /**
     * Gets the name of the logger.
     * 
     * @return the logger name
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * Sets the name of the logger.
     * 
     * @param loggerName
     *            the logger name to set
     */
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * Gets the raw message.
     * 
     * @return the raw message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the raw message.
     * 
     * @param message
     *            the raw message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the time that the event occurred, in milliseconds since 1970.
     * 
     * @return the time that the event occurred, in milliseconds since 1970
     */
    public long getMillis() {
        return millis;
    }

    /**
     * Sets the time that the event occurred, in milliseconds since 1970.
     * 
     * @param millis
     *            the time that the event occurred, in milliseconds since 1970
     */
    public void setMillis(long millis) {
        this.millis = millis;
    }

    /**
     * Gets the parameters.
     * 
     * @return the array of parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters.
     * 
     * @param parameters
     *            the array of parameters to set
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the resource bundle used to localize the raw message during
     * formatting.
     * 
     * @return the associated resource bundle
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Sets the resource bundle used to localize the raw message during
     * formatting.
     * 
     * @param resourceBundle
     *            the resource bundle to set
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Gets the name of the resource bundle.
     * 
     * @return the name of the resource bundle
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * Sets the name of the resource bundle.
     * 
     * @param resourceBundleName
     *            the name of the resource bundle to set
     */
    public void setResourceBundleName(String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
    }

    /**
     * Gets the sequence number.
     * 
     * @return the sequence number
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number. It is usually unnecessary to call this method
     * to change the sequence number because the number is allocated when this
     * instance is constructed.
     * 
     * @param sequenceNumber
     *            the sequence number to set
     */
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the name of the class that issued the logging call.
     * 
     * @return the name of the class that issued the logging call
     */
    public String getSourceClassName() {
        initSource();
        return sourceClassName;
    }

    /*
     *  Init the sourceClass and sourceMethod fields.
     */
    private void initSource() {
        if (!sourceInited) {
            StackTraceElement[] elements = (new Throwable()).getStackTrace();
            int i = 0;
            String current = null;
            FINDLOG: for (; i < elements.length; i++) {
                current = elements[i].getClassName();
                if (current.equals(Logger.class.getName())) {
                    break FINDLOG;
                }
            }
            while(++i<elements.length && elements[i].getClassName().equals(current)) {
                // do nothing
            }
            if (i < elements.length) {
                this.sourceClassName = elements[i].getClassName();
                this.sourceMethodName = elements[i].getMethodName();
            }
            sourceInited = true;
        }
    }

    /**
     * Sets the name of the class that issued the logging call.
     * 
     * @param sourceClassName
     *            the name of the class that issued the logging call
     */
    public void setSourceClassName(String sourceClassName) {
        sourceInited = true;
        this.sourceClassName = sourceClassName;
    }

    /**
     * Gets the name of the method that issued the logging call.
     * 
     * @return the name of the method that issued the logging call
     */
    public String getSourceMethodName() {
        initSource();
        return sourceMethodName;
    }

    /**
     * Sets the name of the method that issued the logging call.
     * 
     * @param sourceMethodName
     *            the name of the method that issued the logging call
     */
    public void setSourceMethodName(String sourceMethodName) {
        sourceInited = true;
        this.sourceMethodName = sourceMethodName;
    }

    /**
     * Gets the ID of the thread originating the message.
     * 
     * @return the ID of the thread originating the message
     */
    public int getThreadID() {
        return threadID;
    }

    /**
     * Sets the ID of the thread originating the message.
     * 
     * @param threadID
     *            the ID of the thread originating the message
     */
    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    /**
     * Gets the <code>Throwable</code> object associated with this log record.
     * 
     * @return the <code>Throwable</code> object associated with this log
     *         record
     */
    public Throwable getThrown() {
        return thrown;
    }

    /**
     * Sets the <code>Throwable</code> object associated with this log record.
     * 
     * @param thrown
     *            the <code>Throwable</code> object associated with this log
     *            record
     */
    public void setThrown(Throwable thrown) {
        this.thrown = thrown;
    }

    /*
     * Customized serialization.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeByte(MAJOR);
        out.writeByte(MINOR);
        if (null == parameters) {
            out.writeInt(-1);
        } else {
            out.writeInt(parameters.length);
            for (Object element : parameters) {
                out.writeObject(null == element ? null : element.toString());
            }
        }
    }

    /*
     * Customized deserialization.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        byte major = in.readByte();
        byte minor = in.readByte();
        //only check MAJOR version
        if (major != MAJOR) {
            // logging.5=Different version - {0}.{1}
            throw new IOException(Messages.getString("logging.5", major, minor)); //$NON-NLS-1$ 
        }
        
        int length = in.readInt();
        if (length >= 0) {
            parameters = new Object[length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = in.readObject();
            }
        }
        if (null != resourceBundleName) {
            try {
                resourceBundle = Logger.loadResourceBundle(resourceBundleName);
            } catch (MissingResourceException e) {
                // Cannot find the specified resource bundle
                resourceBundle = null;
            }
        }
    }
}
