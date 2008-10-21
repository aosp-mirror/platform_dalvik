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

package java.security.cert;

import java.security.GeneralSecurityException;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class CertPathValidatorException extends GeneralSecurityException {
    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -3083180014971893139L;

    /**
     * @com.intel.drl.spec_ref
     * 
     * Serialized field for storing certPath which is defined in constructor
     * CertPathValidatorException(msg, cause, certPath, index)
     */
    private CertPath certPath;

    /**
     * @com.intel.drl.spec_ref
     * 
     * Serialized field for storing index which is defined in constructor
     * CertPathValidatorException(msg, cause, certPath, index)
     */
    private int index = -1;

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathValidatorException(String msg, Throwable cause,
            CertPath certPath, int index) {
        super(msg, cause);
        // check certPath and index parameters
        if ((certPath == null) && (index != -1)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.53")); //$NON-NLS-1$
        }
        if ((certPath != null)
                && ((index < -1) || (index >= certPath.getCertificates().size()))) {
            throw new IndexOutOfBoundsException(Messages.getString("security.54")); //$NON-NLS-1$
        }
        this.certPath = certPath;
        this.index = index;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathValidatorException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathValidatorException(Throwable cause) {
        super(cause);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathValidatorException(String msg) {
        super(msg);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathValidatorException() {
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPath getCertPath() {
        return certPath;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getIndex() {
        return index;
    }
}