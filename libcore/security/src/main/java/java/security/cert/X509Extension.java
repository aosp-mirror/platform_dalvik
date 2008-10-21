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

import java.util.Set;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public interface X509Extension {

    /**
     * @com.intel.drl.spec_ref
     */
    public Set<String> getCriticalExtensionOIDs();

    /**
     * @com.intel.drl.spec_ref
     */
    public byte[] getExtensionValue(String oid);

    /**
     * @com.intel.drl.spec_ref
     */
    public Set<String> getNonCriticalExtensionOIDs();

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean hasUnsupportedCriticalExtension();
}
