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

package org.apache.harmony.security.tests.support.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.util.Collection;

/**
 * Additional class for verification CertStoreSpi
 * and CertStore
 * 
 */

public class MyCertStoreSpi extends CertStoreSpi {
    
    public MyCertStoreSpi(CertStoreParameters params)
            throws InvalidAlgorithmParameterException {
        super(params);        
        if (!(params instanceof MyCertStoreParameters)) {
            throw new InvalidAlgorithmParameterException("Invalid params");
        }
    }

    public Collection engineGetCertificates(CertSelector selector)
            throws CertStoreException {
        if (selector == null) {
            throw new CertStoreException("Parameter is null");
        }
        return null;
    }

    public Collection engineGetCRLs(CRLSelector selector)
            throws CertStoreException {
        if (selector == null) {
            throw new CertStoreException("Parameter is null");
        }
        return null;
    }
}
