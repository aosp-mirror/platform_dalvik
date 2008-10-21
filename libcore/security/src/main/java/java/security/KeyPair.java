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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security;

import java.io.Serializable;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public final class KeyPair implements Serializable {
    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -7565189502268009837L;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    /**
     * @com.intel.drl.spec_ref
     */
    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public PrivateKey getPrivate() {
        return privateKey;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public PublicKey getPublic() {
        return publicKey;
    }
}
