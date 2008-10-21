/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package java.security;

/**
 * Instances of this class are used to wrap exceptions which occur within
 * privileged operations.
 * 
 */
public class PrivilegedActionException extends Exception {

    /**
     * @com.intel.drl.spec_ref 
     */
    private static final long serialVersionUID = 4724086851538908602l;

    /**
     * @com.intel.drl.spec_ref 
     */
    private Exception exception;

    /**
     * Constructs a new instance of this class with its exception filled in.
     * @param ex 
     */
    public PrivilegedActionException(Exception ex) {
        super(ex);
        this.exception = ex;
    }

    /**
     * Returns the exception which caused the receiver to be thrown.
     * @return exception
     */
    public Exception getException() {
        return exception; // return ( getCause() instanceof Exception ) ?
        // getCause() : null;
    }

    /**
     * Returns the cause of this Throwable, or null if there is no cause.
     * 
     * 
     * @return Throwable The receiver's cause.
     */
    public Throwable getCause() {
        return exception;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * 
     * @return String a printable representation for the receiver.
     */
    public String toString() {
        String s = getClass().getName();
        return exception == null ? s : s + ": " + exception; //$NON-NLS-1$
    }

}
