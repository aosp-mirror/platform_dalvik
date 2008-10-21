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

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class CertPathBuilderException extends GeneralSecurityException {

    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = 5316471420178794402L;

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathBuilderException(Throwable cause) {
        super(cause);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathBuilderException(String msg) {
        super(msg);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CertPathBuilderException() {
    }
}