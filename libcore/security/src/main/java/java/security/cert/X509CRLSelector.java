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
 * @author Alexander Y. Kleymenov
 * @version $Revision$
 */

package java.security.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.internal.nls.Messages;
import org.apache.harmony.security.x501.Name;

/**
 * @com.intel.drl.spec_ref
 */
public class X509CRLSelector implements CRLSelector {

    // issuerNames criterion:
    // contains X.500 distinguished names in CANONICAL format
    private ArrayList<String> issuerNames;
    // contains X500Principal objects corresponding to the names
    // from issuerNames collection (above)
    private ArrayList<X500Principal> issuerPrincipals;
    // minCRLNumber criterion
    private BigInteger minCRL;
    // maxCRLNumber criterion
    private BigInteger maxCRL;
    // dateAndTime criterion
    private long dateAndTime = -1;
    // the certificate being checked
    private X509Certificate certificateChecking;

    /**
     * @com.intel.drl.spec_ref
     */
    public X509CRLSelector() { }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setIssuers(Collection<X500Principal> issuers) {
        if (issuers == null) {
            issuerNames = null;
            issuerPrincipals = null;
            return;
        }
        issuerNames = new ArrayList<String>(issuers.size());
        issuerPrincipals = new ArrayList<X500Principal>(issuers);
        for (X500Principal issuer: issuers) {
            issuerNames.add(issuer.getName(X500Principal.CANONICAL));
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setIssuerNames(Collection<?> names) throws IOException {
        if (names == null) {
            issuerNames = null;
            issuerPrincipals = null;
            return;
        }
        if (names.size() == 0) {
            return;
        }
        issuerNames = new ArrayList<String>(names.size());
        for (Object name: names) {
            if (name instanceof String) {
                issuerNames.add(
                        new Name((String) name).getName(
                            X500Principal.CANONICAL));
            } else if (name instanceof byte[]) {
                issuerNames.add(
                        new Name((byte[]) name).getName(
                            X500Principal.CANONICAL));
            } else {
                throw new IOException(
                        Messages.getString("security.62")); //$NON-NLS-1$
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void addIssuer(X500Principal issuer) {
        if (issuer == null) {
            throw new NullPointerException(Messages.getString("security.61")); //$NON-NLS-1$
        }
        if (issuerNames == null) {
            issuerNames = new ArrayList<String>();
        }
        String name = issuer.getName(X500Principal.CANONICAL);
        if (!issuerNames.contains(name)) {
            issuerNames.add(name);
        }
        if (issuerPrincipals == null) {
            issuerPrincipals = new ArrayList<X500Principal>(issuerNames.size());
        }
        // extend the list of issuer Principals
        int size = issuerNames.size() - 1;
        for (int i=issuerPrincipals.size(); i<size; i++) {
            issuerPrincipals.add(new X500Principal(issuerNames.get(i)));
        }
        issuerPrincipals.add(issuer);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void addIssuerName(String iss_name) throws IOException {
        if (issuerNames == null) {
            issuerNames = new ArrayList<String>();
        }

        if (iss_name == null) {
            iss_name = ""; //$NON-NLS-1$
        }

        String name = new Name(iss_name).getName(X500Principal.CANONICAL);
        if (!issuerNames.contains(name)) {
            issuerNames.add(name);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void addIssuerName(byte[] iss_name) throws IOException {
        if (iss_name == null) {
            throw new NullPointerException(Messages.getString("security.63")); //$NON-NLS-1$
        }
        if (issuerNames == null) {
            issuerNames = new ArrayList<String>();
        }
        String name = new Name(iss_name).getName(X500Principal.CANONICAL);
        if (!issuerNames.contains(name)) {
            issuerNames.add(name);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setMinCRLNumber(BigInteger minCRL) {
        this.minCRL = minCRL;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setMaxCRLNumber(BigInteger maxCRL) {
        this.maxCRL = maxCRL;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setDateAndTime(Date dateAndTime) {
        if (dateAndTime == null) {
            this.dateAndTime = -1;
            return;
        }
        this.dateAndTime = dateAndTime.getTime();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setCertificateChecking(X509Certificate cert) {
        this.certificateChecking = cert;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Collection<X500Principal> getIssuers() {
        if (issuerNames == null) {
            return null;
        }
        if (issuerPrincipals == null) {
            issuerPrincipals = new ArrayList<X500Principal>(issuerNames.size());
        }
        int size = issuerNames.size();
        // extend the list of issuer Principals
        for (int i=issuerPrincipals.size(); i<size; i++) {
            issuerPrincipals.add(new X500Principal(issuerNames.get(i)));
        }
        return Collections.unmodifiableCollection(issuerPrincipals);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Collection<Object> getIssuerNames() {
        if (issuerNames == null) {
            return null;
        }
        return Collections.unmodifiableCollection((ArrayList<?>) issuerNames);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getMinCRL() {
        return minCRL;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getMaxCRL() {
        return maxCRL;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Date getDateAndTime() {
        if (dateAndTime == -1) {
            return null;
        }
        return new Date(dateAndTime);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public X509Certificate getCertificateChecking() {
        return certificateChecking;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("X509CRLSelector:\n["); //$NON-NLS-1$
        if (issuerNames != null) {
            result.append("\n  IssuerNames:\n  ["); //$NON-NLS-1$
            int size = issuerNames.size();
            for (int i=0; i<size; i++) {
                result.append("\n    " //$NON-NLS-1$
                    + issuerNames.get(i));
            }
            result.append("\n  ]"); //$NON-NLS-1$
        }
        if (minCRL != null) {
            result.append("\n  minCRL: " + minCRL); //$NON-NLS-1$
        }
        if (maxCRL != null) {
            result.append("\n  maxCRL: " + maxCRL); //$NON-NLS-1$
        }
        if (dateAndTime != -1) {
            result.append("\n  dateAndTime: " + (new Date(dateAndTime))); //$NON-NLS-1$
        }
        if (certificateChecking != null) {
            result.append("\n  certificateChecking: " + certificateChecking); //$NON-NLS-1$
        }
        result.append("\n]"); //$NON-NLS-1$
        return result.toString();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean match(CRL crl) {
        if (!(crl instanceof X509CRL)) {
            return false;
        }
        X509CRL crlist = (X509CRL) crl;
        if ((issuerNames != null) &&
                // the search speed depends on the class of issuerNames
                !(issuerNames.contains(
                        crlist.getIssuerX500Principal().getName(
                            X500Principal.CANONICAL)))) {
            return false;
        }
        if ((minCRL != null) || (maxCRL != null)) {
            try {
                // As specified in rfc 3280 (http://www.ietf.org/rfc/rfc3280.txt)
                // CRL Number Extension's OID is 2.5.29.20 .
                byte[] bytes = crlist.getExtensionValue("2.5.29.20"); //$NON-NLS-1$
                bytes = (byte[]) ASN1OctetString.getInstance().decode(bytes);
                BigInteger crlNumber = new BigInteger((byte[])
                        ASN1Integer.getInstance().decode(bytes));
                if ((minCRL != null) && (crlNumber.compareTo(minCRL) < 0)) {
                    return false;
                }
                if ((maxCRL != null) && (crlNumber.compareTo(maxCRL) > 0)) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }
        if (dateAndTime != -1) {
            Date thisUp = crlist.getThisUpdate();
            Date nextUp = crlist.getNextUpdate();
            if ((thisUp == null) || (nextUp == null)) {
                return false;
            }
            if ((dateAndTime < thisUp.getTime())
                                || (dateAndTime > nextUp.getTime())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Object clone() {
        X509CRLSelector result = new X509CRLSelector();
        if (issuerNames != null) {
            result.issuerNames = new ArrayList<String>(issuerNames);
        }
        result.minCRL = minCRL;
        result.maxCRL = maxCRL;
        result.dateAndTime = dateAndTime;
        result.certificateChecking = certificateChecking;
        return result;
    }
}

