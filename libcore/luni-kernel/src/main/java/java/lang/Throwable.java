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

package java.lang;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This class is the superclass of all classes which can be thrown by
 * the virtual machine. The two direct subclasses represent
 * recoverable exceptions (Exception) and unrecoverable errors
 * (Error). This class provides common methods for accessing a string
 * message which provides extra information about the circumstances in
 * which the Throwable was created, and for filling in a stack trace
 * (i.e., a record of the call stack at a particular point in time)
 * which can be printed later.
 * 
 * @see Error
 * @see Exception
 * @see RuntimeException
 */
public class Throwable implements java.io.Serializable {

    private static final long serialVersionUID = -3042686055658047285L;

    /**
     * The message provided when the exception was created.
     */
    private String detailMessage;

    /**
     * The cause of this Throwable. Null when there is no cause.
     */
    private Throwable cause = this;
    
    /**
     * An intermediate representation of the stack trace.  This field may
     * be accessed by the VM; do not rename.
     */
    private volatile Object stackState;

    /**
     * A fully-expanded representation of the stack trace.
     */
    private StackTraceElement[] stackTrace;

    /**
     * Constructs a new instance of this class with its stack trace filled in.
     */
    public Throwable() {
        super();
        fillInStackTrace();
    }

    /**
     * Constructs a new instance of this class with its stack trace and message
     * filled in.
     * 
     * @param detailMessage String The detail message for the exception.
     */
    public Throwable(String detailMessage) {
        this();
        this.detailMessage = detailMessage;
    }

    /**
     * Constructs a new instance of this class with its stack trace,
     * message, and cause filled in.
     * 
     * @param detailMessage String The detail message for the exception.
     * @param throwable The cause of this Throwable
     */
    public Throwable(String detailMessage, Throwable throwable) {
        this();
        this.detailMessage = detailMessage;
        cause = throwable;
    }

    /**
     * Constructs a new instance of this class with its stack trace and cause
     * filled in.
     * 
     * @param throwable The cause of this Throwable
     */
    public Throwable(Throwable throwable) {
        this();
        this.detailMessage = throwable == null ? null : throwable.toString();
        cause = throwable;
    }

    /**
     * Records in the receiver a stack trace from the point where this
     * message was sent. The method is public so that code which
     * catches a throwable and then <em>re-throws</em> it can adjust
     * the stack trace to represent the location where the exception
     * was re-thrown.
     * 
     * @return the receiver
     */
    public Throwable fillInStackTrace() {
        // Fill in the intermediate representation
        stackState = nativeFillInStackTrace();
        // Mark the full representation as empty
        stackTrace = null;
        return this;
    }

    /**
     * Returns the extra information message which was provided when
     * the throwable was created. If no message was provided at
     * creation time, then return null.
     * 
     * @return String The receiver's message.
     */
    public String getMessage() {
        return detailMessage;
    }

    /**
     * Returns the extra information message which was provided when
     * the throwable was created. If no message was provided at
     * creation time, then return null. Subclasses may override this
     * method to return localized text for the message.
     * 
     * @return String The receiver's message.
     */
    public String getLocalizedMessage() {
        return getMessage();
    }

    /**
     * Returns an array of StackTraceElement. Each StackTraceElement
     * represents a entry on the stack.
     * 
     * @return an array of StackTraceElement representing the stack
     */
    public StackTraceElement[] getStackTrace() {
        return getInternalStackTrace().clone();
    }

    /**
     * Sets the array of StackTraceElements. Each StackTraceElement
     * represents a entry on the stack. A copy of this array will be
     * returned by getStackTrace() and printed by printStackTrace().
     * 
     * @param trace The array of StackTraceElement
     */
    public void setStackTrace(StackTraceElement[] trace) {
        StackTraceElement[] newTrace = trace.clone();
        for (java.lang.StackTraceElement element : newTrace) {
            if (element == null) {
                throw new NullPointerException();
            }
        }
        stackTrace = newTrace;
    }

    /**
     * Outputs a printable representation of the receiver's stack
     * trace on the System.err stream.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Counts the number of duplicate stack frames, starting from the
     * end of the stack.
     * 
     * @param currentStack a stack to compare
     * @param parentStack a stack to compare
     * 
     * @return the number of duplicate stack frames.
     */
    private static int countDuplicates(StackTraceElement[] currentStack,
            StackTraceElement[] parentStack) {
        int duplicates = 0;
        int parentIndex = parentStack.length;
        for (int i = currentStack.length; --i >= 0 && --parentIndex >= 0;) {
            StackTraceElement parentFrame = parentStack[parentIndex];
            if (parentFrame.equals(currentStack[i])) {
                duplicates++;
            } else {
                break;
            }
        }
        return duplicates;
    }

    /**
     * Returns an array of StackTraceElement. Each StackTraceElement
     * represents a entry on the stack.
     * 
     * @return an array of StackTraceElement representing the stack
     */
    private StackTraceElement[] getInternalStackTrace() {
        if (stackTrace == null) {
            stackTrace = nativeGetStackTrace(stackState);
            stackState = null; // Clean up intermediate representation
        }
        return stackTrace;
    }

    /**
     * Outputs a printable representation of the receiver's stack
     * trace on the PrintStream specified by the argument.
     * 
     * @param err PrintStream The stream to write the stack trace on.
     */
    public void printStackTrace(PrintStream err) {
        err.println(toString());
        // Don't use getStackTrace() as it calls clone()
        // Get stackTrace, in case stackTrace is reassigned
        StackTraceElement[] stack = getInternalStackTrace();
        for (java.lang.StackTraceElement element : stack) {
            err.println("\tat " + element);
        }

        StackTraceElement[] parentStack = stack;
        Throwable throwable = getCause();
        while (throwable != null) {
            err.print("Caused by: ");
            err.println(throwable);
            StackTraceElement[] currentStack =
                throwable.getInternalStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                err.println("\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                err.println("\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }

    /**
     * Outputs a printable representation of the receiver's stack
     * trace on the PrintWriter specified by the argument.
     * 
     * @param err PrintWriter The writer to write the stack trace on.
     */
    public void printStackTrace(PrintWriter err) {
        err.println(toString());
        // Don't use getStackTrace() as it calls clone()
        // Get stackTrace, in case stackTrace is reassigned
        StackTraceElement[] stack = getInternalStackTrace();
        for (java.lang.StackTraceElement element : stack) {
            err.println("\tat " + element);
        }

        StackTraceElement[] parentStack = stack;
        Throwable throwable = getCause();
        while (throwable != null) {
            err.print("Caused by: ");
            err.println(throwable);
            StackTraceElement[] currentStack =
                throwable.getInternalStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                err.println("\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                err.println("\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return String a printable representation for the receiver.
     */
    @Override
    public String toString() {
        String msg = getLocalizedMessage();
        String name = getClass().getName();
        if (msg == null) {
            return name;
        }
        return new StringBuffer(name.length() + 2 + msg.length()).
            append(name).append(": ").append(msg).toString();
    }

    /**
     * Initialize the cause of the receiver. The cause cannot be reassigned.
     * 
     * @param throwable The cause of this Throwable
     * 
     * @exception IllegalArgumentException when the cause is the receiver
     * @exception IllegalStateException when the cause has already been
     *            initialized
     * 
     * @return the receiver.
     */
    public Throwable initCause(Throwable throwable) {
        if (cause == this) {
            if (throwable != this) {
                cause = throwable;
                return this;
            }
            throw new IllegalArgumentException("Cause cannot be the receiver");
        }
        throw new IllegalStateException("Cause already initialized");
    }

    /**
     * Returns the cause of this Throwable, or null if there is no cause.
     * 
     * @return Throwable The receiver's cause.
     */
    public Throwable getCause() {
        if (cause == this) {
            return null;
        }
        return cause;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        // ensure the stackTrace field is initialized
        getInternalStackTrace();
        s.defaultWriteObject();
    }

    /*
     * Creates a compact, VM-specific collection of goodies, suitable for
     * storing in the "stackState" field, based on the current thread's
     * call stack.
     */
    native private static Object nativeFillInStackTrace();

    /*
     * Creates an array of StackTraceElement objects from the data held
     * in "stackState".
     */
    native private static StackTraceElement[] nativeGetStackTrace(Object stackState);
}

