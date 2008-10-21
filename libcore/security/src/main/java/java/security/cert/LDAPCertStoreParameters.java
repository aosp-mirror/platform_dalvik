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

package java.security.cert;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class LDAPCertStoreParameters implements CertStoreParameters {
    // Default LDAP server name
    private static final String DEFAULT_LDAP_SERVER_NAME = "localhost"; //$NON-NLS-1$
    // Default LDAP server port number 
    private static final int DEFAULT_LDAP_PORT  = 389;

    // LDAP server name for this cert store
    private final String serverName;
    // LDAP server port number for this cert store
    private final int port;

    /**
     * @com.intel.drl.spec_ref
     */
    public LDAPCertStoreParameters(String serverName, int port) {
        this.port = port;
        this.serverName = serverName;
        if (this.serverName == null) {
            throw new NullPointerException();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public LDAPCertStoreParameters() {
        this.serverName = DEFAULT_LDAP_SERVER_NAME;
        this.port = DEFAULT_LDAP_PORT;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public LDAPCertStoreParameters(String serverName) {
        this.port = DEFAULT_LDAP_PORT;
        this.serverName = serverName;
        if (this.serverName == null) {
            throw new NullPointerException();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Object clone() {
        return new LDAPCertStoreParameters(serverName, port);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getPort() {
        return port;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb =
            new StringBuffer("LDAPCertStoreParameters: [\n serverName: "); //$NON-NLS-1$
        sb.append(getServerName());
        sb.append("\n port: "); //$NON-NLS-1$
        sb.append(getPort());
        sb.append("\n]"); //$NON-NLS-1$
        return sb.toString();
    }
}
