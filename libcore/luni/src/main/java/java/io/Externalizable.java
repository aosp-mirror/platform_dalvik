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

package java.io;

/**
 * Objects that want to be serialized/deserialized using
 * ObjectOutputStream/ObjectInputStream but defining their own byte
 * representation should implement this interface.
 */
public interface Externalizable extends Serializable {
    /**
     * Reads the next object from the ObjectInput <code>input</code>
     * 
     * @param input
     *            the ObjectInput from which the next object is read
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this ObjectInput.
     * @throws ClassNotFoundException
     *             If the class of the instance being loaded cannot be found
     */
    public void readExternal(ObjectInput input) throws IOException,
            ClassNotFoundException;

    /**
     * Writes the receiver to the ObjectOutput <code>output</code>.
     * 
     * @param output
     *            an ObjectOutput where to write the object
     * 
     * @throws IOException
     *             If an error occurs attempting to write to the ObjectOutput.
     */
    public void writeExternal(ObjectOutput output) throws IOException;
}
