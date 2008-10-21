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

package java.security.spec;

import java.math.BigInteger;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class ECPrivateKeySpec implements KeySpec {
    // Private value associated with this key
    private final BigInteger s;
    // Elliptic Curve domain parameters associated with this key
    private final ECParameterSpec params;

    /**
     * @com.intel.drl.spec_ref
     */
    public ECPrivateKeySpec(BigInteger s, ECParameterSpec params) {
        this.s = s;
        this.params = params;
        // throw NullPointerException if s or params is null
        if (this.s == null) {
            throw new NullPointerException(Messages.getString("security.83", "s")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (this.params == null) {
            throw new NullPointerException(Messages.getString("security.83", "params")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ECParameterSpec getParams() {
        return params;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getS() {
        return s;
    }
}
