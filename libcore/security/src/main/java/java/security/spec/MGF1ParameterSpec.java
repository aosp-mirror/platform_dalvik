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
public class MGF1ParameterSpec implements AlgorithmParameterSpec {
    /**
     * @com.intel.drl.spec_ref
     */
    public static final MGF1ParameterSpec SHA1 =
        new MGF1ParameterSpec("SHA-1"); //$NON-NLS-1$
    /**
     * @com.intel.drl.spec_ref
     */
    public static final MGF1ParameterSpec SHA256 =
        new MGF1ParameterSpec("SHA-256"); //$NON-NLS-1$
    /**
     * @com.intel.drl.spec_ref
     */
    public static final MGF1ParameterSpec SHA384 =
        new MGF1ParameterSpec("SHA-384"); //$NON-NLS-1$
    /**
     * @com.intel.drl.spec_ref
     */
    public static final MGF1ParameterSpec SHA512 =
        new MGF1ParameterSpec("SHA-512"); //$NON-NLS-1$

    //  Message digest algorithm name
    private final String mdName;

    /**
     * @com.intel.drl.spec_ref
     */
    public MGF1ParameterSpec(String mdName) {
        this.mdName = mdName;
        if (this.mdName == null) {
            throw new NullPointerException(Messages.getString("security.80")); //$NON-NLS-1$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getDigestAlgorithm() {
        return mdName;
    }
}
