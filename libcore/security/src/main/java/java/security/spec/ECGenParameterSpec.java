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

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class ECGenParameterSpec implements AlgorithmParameterSpec {
    // Standard (or predefined) name for EC domain
    // parameters to be generated
    private final String name;

    /**
     * @com.intel.drl.spec_ref
     */
    public ECGenParameterSpec(String name) {
        this.name = name;
        if (this.name == null) {
            throw new NullPointerException(Messages.getString("security.83", "name")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getName() {
        return name;
    }
}
