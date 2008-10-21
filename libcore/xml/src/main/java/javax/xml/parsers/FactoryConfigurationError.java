/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.xml.parsers;

/**
 * Represents an error that occured during the configuration of parser factory.
 */
public class FactoryConfigurationError extends Error {

    /**
     * The nested exception that caused this exception. Note that the nested
     * exception will be stored in a special attribute, and can be queried
     * using the getException() method. It does not use the facility the
     * Exception class provides for storing nested exceptions, since the XML
     * API predates that facility.
     */
    private Exception cause;
    
    /**
     * Creates a new FactoryConfigurationError with no error message an no
     * cause.
     */
    public FactoryConfigurationError() {
        super();
    }

    /**
     * Creates a new FactoryConfigurationError with no error message and a given
     * cause.
     * 
     * @param cause The cause of the error. Note that the nested
     *          exception will be stored in a special attribute, and can be
     *          queried using the getException() method. It does not use the
     *          facility the Exception class provides for storing nested
     *          exceptions, since the XML API predates that facility.
     */
    public FactoryConfigurationError(Exception cause) {
        super();
        this.cause = cause;
    }

    /**
     * Creates a new FactoryConfigurationError with a given error message and
     * cause.
     * 
     * @param cause The cause of the error. Note that the nested
     *          exception will be stored in a special attribute, and can be
     *          queried using the getException() method. It does not use the
     *          facility the Exception class provides for storing nested
     *          exceptions, since the XML API predates that facility.
     * @param message The error message.
     */
    public FactoryConfigurationError(Exception cause, String message) {
        super(message);
        this.cause = cause;
    }

    /**
     * Creates a new FactoryConfigurationError with a given error message and no
     * cause.
     * 
     * @param message The error message.
     */
    public FactoryConfigurationError(String message) {
        super(message);
    }

    /**
     * Returns the cause of the error, in case there is one.
     * 
     * @return The exception that caused the error, or null if none is set.
     */
    public java.lang.Exception getException() {
        return cause;
    }

    /**
     * Returns the message of the error, in case there is one.
     * 
     * @return The message. If an explicit error message has been assigned to
     *         the exception, this one is returned. If not, and there is an
     *         underlying exception (the cause), then the result of invoking
     *         toString() for that is returned. Otherwise, null is returned.
     */
    public java.lang.String getMessage() {
        String message = super.getMessage();

        if (message != null) {
            return message;
        } else if (cause != null) {
            return cause.toString();
        } else {
            return null;
        }
    }
}
