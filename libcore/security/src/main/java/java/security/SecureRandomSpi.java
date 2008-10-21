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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package java.security;

import java.io.Serializable;

/**
 * This class is a Service Provider Interface (therefore the Spi suffix) for
 * secure random number generation algorithms to be supplied by providers.
 * 
 */
public abstract class SecureRandomSpi implements Serializable {
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    private static final long serialVersionUID = -2991854161009191830L;
                
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract void engineSetSeed(byte[] seed);
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract void engineNextBytes(byte[] bytes);
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract byte[] engineGenerateSeed(int numBytes);
}
