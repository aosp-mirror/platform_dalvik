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

import org.apache.harmony.instrument.internal.nls.Messages;

/**
 * Wraps a {@link java.lang.Class} that is to be redefined together with the
 * byte array which constitutes the updated version of the class.
 * 
 */
public final class ClassDefinition {

    /**
     * The <code>Class</code> object for the class that will be instrumented.
     */
    private Class<?> definitionClass;

    /**
     * The new version of the class file bytes for the class being instrumented.
     */
    private byte[] definitionClassFile;

    /**
     * Constructs a new instance of <code>ClassDefinition</code> with the
     * supplied {@link Class} object and byte array representing the new class
     * file bytes.
     * 
     * @param theClass
     *            the <code>Class</code> object for the class to be redefined
     * @param theClassFile
     *            an array of bytes containing the updated version of the class
     *            to be redefined.
     * @throws NullPointerException
     *             if either <code>theClass</code> or
     *             <code>theClassFile</code> are <code>null</code>.
     */
    public ClassDefinition(Class<?> theClass, byte[] theClassFile) {
        if (theClass == null) {
            throw new NullPointerException(Messages.getString("instrument.1")); //$NON-NLS-1$
        }
        if (theClassFile == null) {
            throw new NullPointerException(Messages.getString("instrument.2")); //$NON-NLS-1$
        }
        this.definitionClass = theClass;
        this.definitionClassFile = theClassFile;
    }

    /**
     * Returns the {@link Class} object for the class to be redefined.
     * 
     * @return the <code>Class</code> object
     */
    public Class<?> getDefinitionClass() {
        return this.definitionClass;
    }

    /**
     * Returns a reference to the byte array containing the re-engineered
     * version of the class.
     * 
     * @return byte array containing the new version of the class
     */
    public byte[] getDefinitionClassFile() {
        return this.definitionClassFile;
    }
}
