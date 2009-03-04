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
 * {@link java.lang.instrument.Instrumentation#redefineClasses} when one of the
 * desired class redefinition operations cannot be carried out. Such a situation
 * may arise if a redefinition attempts to alter the members of a class or its
 * inheritance hierarchy.
 * 
 */
public class UnmodifiableClassException extends Exception {

    private static final long serialVersionUID = 1716652643585309178L;

    /**
     * Constructs a new instance of <code>UnmodifiableClassException</code>
     * with no explanatory message.
     */
    public UnmodifiableClassException() {
        super();
    }

    /**
     * Constructs a new instance of <code>UnmodifiableClassException</code>
     * with the supplied message, <code>s</code>, for explanation.
     * 
     * @param s
     *            a string containing information on why the exception is being
     *            created.
     */
    public UnmodifiableClassException(String s) {
        super(s);
    }
}
