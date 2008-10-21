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

package javax.net.ssl;

import java.security.Principal;
import java.security.cert.Certificate;
import javax.security.cert.X509Certificate;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public interface SSLSession {

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int getApplicationBufferSize();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public String getCipherSuite();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public long getCreationTime();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public byte[] getId();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public long getLastAccessedTime();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Certificate[] getLocalCertificates();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Principal getLocalPrincipal();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int getPacketBufferSize();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public X509Certificate[] getPeerCertificateChain()
            throws SSLPeerUnverifiedException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Certificate[] getPeerCertificates()
            throws SSLPeerUnverifiedException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public String getPeerHost();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int getPeerPort();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public String getProtocol();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public SSLSessionContext getSessionContext();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Object getValue(String name);

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public String[] getValueNames();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void invalidate();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public boolean isValid();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void putValue(String name, Object value);

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void removeValue(String name);
}