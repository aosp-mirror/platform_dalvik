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

package java.lang.instrument;

/**
 * This exception may be thrown from implementations of the method
 * {@link java.lang.instrument.ClassFileTransformer#transform} when the class
 * file bytes supplied to it are found to be corrupted or otherwise in a format
 * which does not adhere to the expected Java class file format.
 * 
 */
public class IllegalClassFormatException extends Exception {

    private static final long serialVersionUID = -3841736710924794009L;

    /**
     * Constructs a new instance of <code>IllegalClassFormatException</code>
     * with no explanatory message.
     */
    public IllegalClassFormatException() {
        super();
    }

    /**
     * Constructs a new instance of <code>IllegalClassFormatException</code>
     * with the supplied message, <code>s</code>, for explanation.
     * 
     * @param s
     *            a string containing information on why the exception is being
     *            created.
     */
    public IllegalClassFormatException(String s) {
        super(s);
    }
}