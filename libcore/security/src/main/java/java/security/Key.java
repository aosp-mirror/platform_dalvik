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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security;

import java.io.Serializable;

/**
 * Defines the basic properties of all key objects.
 * 
 * @see PublicKey
 */
public interface Key extends Serializable {
    /**
     * @com.intel.drl.spec_ref
     */
    public static final long serialVersionUID = 6603384152749567654L;

    /**
     * Returns the name of the algorithm that this key will work
     * with. If the algorithm is unknown, it returns null.
     * 
     * @return String the receiver's algorithm
     */
    public String getAlgorithm();

    /**
     * Returns the name of the format used to encode the key, or null
     * if it can not be encoded.
     * 
     * @return String the receiver's encoding format
     */
    public String getFormat();

    /**
     * Returns the encoded form of the receiver.
     * 
     * @return byte[] the encoded form of the receiver
     */
    public byte[] getEncoded();
}
