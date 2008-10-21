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

import java.io.Serializable;

/**
 * An implementation of this class is provided, but the documented constructor
 * can be used by the VM specific implementation to create instances.
 * 
 * StackTraceElement represents a stack frame.
 * 
 * @see Throwable#getStackTrace()
 * @since 1.4
 */
public final class StackTraceElement implements Serializable {

    private static final long serialVersionUID = 6992337162326171013L;

    private static final int NATIVE_LINE_NUMBER = -2;
    
    String declaringClass;

    String methodName;

    String fileName;

    int lineNumber;

    /**
     * <p>
     * Constructs a <code>StackTraceElement</code> for an execution point.
     * </p>
     * 
     * @param cls The fully qualified name of the class where execution is at.
     * @param method The name of the method where execution is at.
     * @param file The name of the file where execution is at or
     *        <code>null</code>.
     * @param line The line of the file where execution is at, a negative number
     *        if unknown or <code>-2</code> if the execution is in a native
     *        method.
     * 
     * @throws NullPointerException if <code>cls</code> or <code>method</code>
     *         is <code>null</code>.
     * 
     * @since 1.5
     */
    public StackTraceElement(String cls, String method, String file, int line) {
        super();
        if (cls == null || method == null) {
            throw new NullPointerException();
        }
        declaringClass = cls;
        methodName = method;
        fileName = file;
        lineNumber = line;
    }

    /**
     * <p>
     * Private, nullary constructor for VM use only.
     * </p>
     */
    @SuppressWarnings("unused")
    private StackTraceElement() {
        super();
    }

    /**
     * Compare this object with the object passed in
     * 
     * @param obj Object to compare with
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StackTraceElement)) {
            return false;
        }
        StackTraceElement castObj = (StackTraceElement) obj;

        /*
         * Unknown methods are never equal to anything (not strictly to spec,
         * but spec does not allow null method/class names)
         */
        if ((methodName == null) || (castObj.methodName == null)) {
            return false;
        }

        if (!getMethodName().equals(castObj.getMethodName())) {
            return false;
        }
        if (!getClassName().equals(castObj.getClassName())) {
            return false;
        }
        String localFileName = getFileName();
        if (localFileName == null) {
            if (castObj.getFileName() != null) {
                return false;
            }
        } else {
            if (!localFileName.equals(castObj.getFileName())) {
                return false;
            }
        }
        if (getLineNumber() != castObj.getLineNumber()) {
            return false;
        }

        return true;
    }

    /**
     * Returns the full name (i.e. including the package) of the class where
     * this stack trace element is executing.
     * 
     * @return the fully qualified type name of the class where this stack trace
     *         element is executing.
     */
    public String getClassName() {
        return (declaringClass == null) ? "<unknown class>" : declaringClass;
    }

    /**
     * If available, returns the name of the file containing the Java code
     * source which was compiled into the class where this stack trace element
     * is executing.
     * 
     * @return if available, the name of the file containing the Java code
     *         source for the stack trace element's executing class. If no such
     *         detail is available, a <code>null</code> value is returned.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * If available, returns the line number in the source for the class where
     * this stack trace element is executing.
     * 
     * @return if available, the line number in the source file for the class
     *         where this stack trace element is executing. If no such detail is
     *         available, a number less than <code>0</code>.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the name of the method where this stack trace element is
     * executing.
     * 
     * @return the name of the method where this stack trace element is
     *         executing.
     */
    public String getMethodName() {
        return (methodName == null) ? "<unknown method>" : methodName;
    }

    /**
     * Return this StackTraceElement objects hash code
     * 
     * @return This objects hash code
     */
    @Override
    public int hashCode() {
        /*
         * Either both methodName and declaringClass are null, or neither are
         * null.
         */
        if (methodName == null) {
            // all unknown methods hash the same
            return 0;
        }
        // declaringClass never null if methodName is non-null
        return methodName.hashCode() ^ declaringClass.hashCode();
    }

    /**
     * Returns <code>true</code> if the method name returned by
     * {@link #getMethodName()} is implemented as a native method.
     * 
     * @return if the method in which this stack trace element is executing is a
     *         native method
     */
    public boolean isNativeMethod() {
        return lineNumber == NATIVE_LINE_NUMBER;
    }

    /**
     * Return a String representing this StackTraceElement object
     * 
     * @return String representing this object
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(80);

        buf.append(getClassName());
        buf.append('.');
        buf.append(getMethodName());

        if (isNativeMethod()) {
            buf.append("(Native Method)");
        } else {
            String fName = getFileName();

            if (fName == null) {
                buf.append("(Unknown Source)");
            } else {
                int lineNum = getLineNumber();

                buf.append('(');
                buf.append(fName);
                if (lineNum >= 0) {
                    buf.append(':');
                    buf.append(lineNum);
                }
                buf.append(')');
            }
        }
        return buf.toString();
    }
}
