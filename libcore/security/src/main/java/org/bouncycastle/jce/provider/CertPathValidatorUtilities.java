package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.PKIXParameters;
import java.security.cert.PolicyQualifierInfo;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;

public class CertPathValidatorUtilities
{

    protected static final String CERTIFICATE_POLICIES = X509Extensions.CertificatePolicies.getId();
    protected static final String BASIC_CONSTRAINTS = X509Extensions.BasicConstraints.getId();
    protected static final String POLICY_MAPPINGS = X509Extensions.PolicyMappings.getId();
    protected static final String SUBJECT_ALTERNATIVE_NAME = X509Extensions.SubjectAlternativeName.getId();
    protected static final String NAME_CONSTRAINTS = X509Extensions.NameConstraints.getId();
    protected static final String KEY_USAGE = X509Extensions.KeyUsage.getId();
    protected static final String INHIBIT_ANY_POLICY = X509Extensions.InhibitAnyPolicy.getId();
    protected static final String ISSUING_DISTRIBUTION_POINT = X509Extensions.IssuingDistributionPoint.getId();
    protected static final String DELTA_CRL_INDICATOR = X509Extensions.DeltaCRLIndicator.getId();
    protected static final String POLICY_CONSTRAINTS = X509Extensions.PolicyConstraints.getId();
    

    protected static final String ANY_POLICY = "2.5.29.32.0";
    
    protected static final String CRL_NUMBER = X509Extensions.CRLNumber.getId();
    
    /*
     * key usage bits
     */
    protected static final int    KEY_CERT_SIGN = 5;
    protected static final int    CRL_SIGN = 6;

    protected static final String[] crlReasons = new String[] {
        "unspecified",
        "keyCompromise",
        "cACompromise",
        "affiliationChanged",
        "superseded",
        "cessationOfOperation",
        "certificateHold",
        "unknown",
        "removeFromCRL",
        "privilegeWithdrawn",
        "aACompromise" };
    
    /**
     * Search the given Set of TrustAnchor's for one that is the
     * issuer of the given X509 certificate.
     *
     * @param cert the X509 certificate
     * @param trustAnchors a Set of TrustAnchor's
     *
     * @return the <code>TrustAnchor</code> object if found or
     * <code>null</code> if not.
     *
     * @exception CertPathValidatorException if a TrustAnchor  was
     * found but the signature verification on the given certificate
     * has thrown an exception. This Exception can be obtainted with
     * <code>getCause()</code> method.
     **/
    protected static final TrustAnchor findTrustAnchor(
        X509Certificate cert,
        CertPath        certPath,
        int             index,
        Set             trustAnchors) 
        throws CertPathValidatorException
    {
        Iterator iter = trustAnchors.iterator();
        TrustAnchor trust = null;
        PublicKey trustPublicKey = null;
        Exception invalidKeyEx = null;

        X509CertSelector certSelectX509 = new X509CertSelector();

        try
        {
            certSelectX509.setSubject(getEncodedIssuerPrincipal(cert).getEncoded());
        }
        catch (IOException ex)
        {
            throw new CertPathValidatorException(ex);
        }

        while (iter.hasNext() && trust == null)
        {
            trust = (TrustAnchor) iter.next();
            if (trust.getTrustedCert() != null)
            {
                if (certSelectX509.match(trust.getTrustedCert()))
                {
                    trustPublicKey = trust.getTrustedCert().getPublicKey();
                }
                else
                {
                    trust = null;
                }
            }
            else if (trust.getCAName() != null
                    && trust.getCAPublicKey() != null)
            {
                try
                {
                    X500Principal certIssuer = getEncodedIssuerPrincipal(cert);
                    X500Principal caName = new X500Principal(trust.getCAName());
                    if (certIssuer.equals(caName))
                    {
                        trustPublicKey = trust.getCAPublicKey();
                    }
                    else
                    {
                        trust = null;
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    trust = null;
                }
            }
            else
            {
                trust = null;
            }

            if (trustPublicKey != null)
            {
                try
                {
                    cert.verify(trustPublicKey);
                }
                catch (Exception ex)
                {
                    invalidKeyEx = ex;
                    trust = null;
                }
            }
        }

        if (trust == null && invalidKeyEx != null)
        {
            throw new CertPathValidatorException("TrustAnchor found but certificate validation failed.", invalidKeyEx, certPath, index);
        }

        return trust;
    }

    protected static X500Principal getEncodedIssuerPrincipal(X509Certificate cert)
    {
        return cert.getIssuerX500Principal();
    }

    protected static Date getValidDate(PKIXParameters paramsPKIX)
    {
        Date validDate = paramsPKIX.getDate();

        if (validDate == null)
        {
            validDate = new Date();
        }

        return validDate;
    }

    protected static X500Principal getSubjectPrincipal(X509Certificate cert)
    {
        return cert.getSubjectX500Principal();
    }
    
    protected static boolean isSelfIssued(X509Certificate cert)
    {
        return cert.getSubjectDN().equals(cert.getIssuerDN());
    }
    
    
    /**
     * extract the value of the given extension, if it exists.
     */
    protected static DERObject getExtensionValue(
        java.security.cert.X509Extension    ext,
        String                              oid)
        throws AnnotatedException
    {
        byte[]  bytes = ext.getExtensionValue(oid);
        if (bytes == null)
        {
            return null;
        }

        return getObject(oid, bytes);
    }
    
    private static DERObject getObject(
            String oid,
            byte[] ext)
            throws AnnotatedException
    {
        try
        {
            ASN1InputStream aIn = new ASN1InputStream(ext);
            ASN1OctetString octs = (ASN1OctetString)aIn.readObject();

            aIn = new ASN1InputStream(octs.getOctets());
            return aIn.readObject();
        }
        catch (IOException e)
        {
            throw new AnnotatedException("exception processing extension " + oid, e);
        }
    }
    
    protected static X500Principal getIssuerPrincipal(X509CRL crl)
    {
        return crl.getIssuerX500Principal();
    }
    
    protected static AlgorithmIdentifier getAlgorithmIdentifier(
        PublicKey key)
        throws CertPathValidatorException
    {
        try
        {
            ASN1InputStream      aIn = new ASN1InputStream(key.getEncoded());

            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());

            return info.getAlgorithmId();
        }
        catch (IOException e)
        {
            throw new CertPathValidatorException("exception processing public key");
        }
    }

    //
    // Utility functions for name constraint checking
    //

    private static boolean withinDNSubtree(ASN1Sequence dns, ASN1Sequence subtree)
    {
        if (subtree.size() < 1)
        {
            return false;
        }

        if (subtree.size() > dns.size())
        {
            return false;
        }

        for (int j = subtree.size() - 1; j >= 0; j--)
        {
            if (!subtree.getObjectAt(j).equals(dns.getObjectAt(j)))
            {
                return false;
            }
        }

        return true;
    }

    protected static void checkPermittedDN(Set permitted, ASN1Sequence dns)
            throws CertPathValidatorException
    {
        if (permitted.isEmpty())
        {
            return;
        }

        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            ASN1Sequence subtree = (ASN1Sequence) it.next();

            if (withinDNSubtree(dns, subtree))
            {
                return;
            }
        }

        throw new CertPathValidatorException(
                "Subject distinguished name is not from a permitted subtree");
    }

    protected static void checkExcludedDN(Set excluded, ASN1Sequence dns)
            throws CertPathValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            ASN1Sequence subtree = (ASN1Sequence) it.next();

            if (withinDNSubtree(dns, subtree))
            {
                throw new CertPathValidatorException(
                        "Subject distinguished name is from an excluded subtree");
            }
        }
    }

    protected static Set intersectDN(Set permitted, ASN1Sequence dn)
    {
        if (permitted.isEmpty())
        {
            permitted.add(dn);

            return permitted;
        }
        else
        {
            Set intersect = new HashSet();

            Iterator _iter = permitted.iterator();
            while (_iter.hasNext())
            {
                ASN1Sequence subtree = (ASN1Sequence) _iter.next();

                if (withinDNSubtree(dn, subtree))
                {
                    intersect.add(dn);
                }
                else if (withinDNSubtree(subtree, dn))
                {
                    intersect.add(subtree);
                }
            }

            return intersect;
        }
    }

    protected static Set unionDN(Set excluded, ASN1Sequence dn)
    {
        if (excluded.isEmpty())
        {
            excluded.add(dn);

            return excluded;
        }
        else
        {
            Set intersect = new HashSet();

            Iterator _iter = excluded.iterator();
            while (_iter.hasNext())
            {
                ASN1Sequence subtree = (ASN1Sequence) _iter.next();

                if (withinDNSubtree(dn, subtree))
                {
                    intersect.add(subtree);
                }
                else if (withinDNSubtree(subtree, dn))
                {
                    intersect.add(dn);
                }
                else
                {
                    intersect.add(subtree);
                    intersect.add(dn);
                }
            }

            return intersect;
        }
    }

    protected static Set intersectEmail(Set permitted, String email)
    {
        String _sub = email.substring(email.indexOf('@') + 1);

        if (permitted.isEmpty())
        {
            permitted.add(_sub);

            return permitted;
        }
        else
        {
            Set intersect = new HashSet();

            Iterator _iter = permitted.iterator();
            while (_iter.hasNext())
            {
                String _permitted = (String) _iter.next();

                if (_sub.endsWith(_permitted))
                {
                    intersect.add(_sub);
                }
                else if (_permitted.endsWith(_sub))
                {
                    intersect.add(_permitted);
                }
            }

            return intersect;
        }
    }

    protected static Set unionEmail(Set excluded, String email)
    {
        String _sub = email.substring(email.indexOf('@') + 1);

        if (excluded.isEmpty())
        {
            excluded.add(_sub);
            return excluded;
        }
        else
        {
            Set intersect = new HashSet();

            Iterator _iter = excluded.iterator();
            while (_iter.hasNext())
            {
                String _excluded = (String) _iter.next();

                if (_sub.endsWith(_excluded))
                {
                    intersect.add(_excluded);
                }
                else if (_excluded.endsWith(_sub))
                {
                    intersect.add(_sub);
                }
                else
                {
                    intersect.add(_excluded);
                    intersect.add(_sub);
                }
            }

            return intersect;
        }
    }

    protected static Set intersectIP(Set permitted, byte[] ip)
    {
        // TBD
        return permitted;
    }

    protected static Set unionIP(Set excluded, byte[] ip)
    {
        // TBD
        return excluded;
    }

    protected static void checkPermittedEmail(Set permitted, String email)
            throws CertPathValidatorException
    {
        if (permitted.isEmpty())
        {
            return;
        }

        String sub = email.substring(email.indexOf('@') + 1);
        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            String str = (String) it.next();

            if (sub.endsWith(str))
            {
                return;
            }
        }

        throw new CertPathValidatorException(
                "Subject email address is not from a permitted subtree");
    }

    protected static void checkExcludedEmail(Set excluded, String email)
            throws CertPathValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        String sub = email.substring(email.indexOf('@') + 1);
        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            String str = (String) it.next();
            if (sub.endsWith(str))
            {
                throw new CertPathValidatorException(
                        "Subject email address is from an excluded subtree");
            }
        }
    }

    protected static void checkPermittedIP(Set permitted, byte[] ip)
            throws CertPathValidatorException
    {
        if (permitted.isEmpty())
        {
            return;
        }

        // TODO: ??? Something here
    }

    protected static void checkExcludedIP(Set excluded, byte[] ip)
            throws CertPathValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        // TODO, check RFC791 and RFC1883 for IP bytes definition.
    }
    
    
    // crl checking
    
    /**
     * Return a Collection of all CRLs found in the
     * CertStore's that are matching the crlSelect criteriums.
     *
     * @param crlSelect a {@link CertSelector CertSelector}
     * object that will be used to select the CRLs
     * @param crlStores a List containing only {@link CertStore
     * CertStore} objects. These are used to search for
     * CRLs
     *
     * @return a Collection of all found {@link CRL CRL}
     * objects. May be empty but never <code>null</code>.
     */
    protected static final Collection findCRLs(
        X509CRLSelector crlSelect,
        List            crlStores)
        throws AnnotatedException
    {
        Set crls = new HashSet();
        Iterator iter = crlStores.iterator();
    
        while (iter.hasNext())
        {
            CertStore   certStore = (CertStore)iter.next();
    
            try
            {
                crls.addAll(certStore.getCRLs(crlSelect));
            }
            catch (CertStoreException e)
            {
                throw new AnnotatedException("cannot extract crl: " + e, e);
            }
        }
    
        return crls;
    }
    
    //
    // policy checking
    // 
    
    protected static final Set getQualifierSet(ASN1Sequence qualifiers) 
        throws CertPathValidatorException
    {
        Set             pq   = new HashSet();
        
        if (qualifiers == null)
        {
            return pq;
        }
        
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
    
        Enumeration e = qualifiers.getObjects();
    
        while (e.hasMoreElements())
        {
            try
            {
                aOut.writeObject(e.nextElement());
    
                pq.add(new PolicyQualifierInfo(bOut.toByteArray()));
            }
            catch (IOException ex)
            {
                throw new CertPathValidatorException("exception building qualifier set: " + ex);
            }
    
            bOut.reset();
        }
        
        return pq;
    }
    
    protected static PKIXPolicyNode removePolicyNode(
        PKIXPolicyNode  validPolicyTree,
        List     []        policyNodes,
        PKIXPolicyNode _node)
    {
        PKIXPolicyNode _parent = (PKIXPolicyNode)_node.getParent();
        
        if (validPolicyTree == null)
        {
            return null;
        }

        if (_parent == null)
        {
            for (int j = 0; j < policyNodes.length; j++)
            {
                policyNodes[j] = new ArrayList();
            }

            return null;
        }
        else
        {
            _parent.removeChild(_node);
            removePolicyNodeRecurse(policyNodes, _node);

            return validPolicyTree;
        }
    }
    
    private static void removePolicyNodeRecurse(
        List     []        policyNodes,
        PKIXPolicyNode  _node)
    {
        policyNodes[_node.getDepth()].remove(_node);

        if (_node.hasChildren())
        {
            Iterator _iter = _node.getChildren();
            while (_iter.hasNext())
            {
                PKIXPolicyNode _child = (PKIXPolicyNode)_iter.next();
                removePolicyNodeRecurse(policyNodes, _child);
            }
        }
    }
    
    
    protected static boolean processCertD1i(
        int                 index,
        List     []            policyNodes,
        DERObjectIdentifier pOid,
        Set                 pq)
    {
        List       policyNodeVec = policyNodes[index - 1];

        for (int j = 0; j < policyNodeVec.size(); j++)
        {
            PKIXPolicyNode node = (PKIXPolicyNode)policyNodeVec.get(j);
            Set            expectedPolicies = node.getExpectedPolicies();
            
            if (expectedPolicies.contains(pOid.getId()))
            {
                Set childExpectedPolicies = new HashSet();
                childExpectedPolicies.add(pOid.getId());
                
                PKIXPolicyNode child = new PKIXPolicyNode(new ArrayList(),
                                                           index,
                                                           childExpectedPolicies,
                                                           node,
                                                           pq,
                                                           pOid.getId(),
                                                           false);
                node.addChild(child);
                policyNodes[index].add(child);
                
                return true;
            }
        }
        
        return false;
    }

    protected static void processCertD1ii(
        int                 index,
        List     []            policyNodes,
        DERObjectIdentifier _poid,
        Set _pq)
    {
        List       policyNodeVec = policyNodes[index - 1];

        for (int j = 0; j < policyNodeVec.size(); j++)
        {
            PKIXPolicyNode _node = (PKIXPolicyNode)policyNodeVec.get(j);
            Set            _expectedPolicies = _node.getExpectedPolicies();
            
            if (ANY_POLICY.equals(_node.getValidPolicy()))
            {
                Set _childExpectedPolicies = new HashSet();
                _childExpectedPolicies.add(_poid.getId());
                
                PKIXPolicyNode _child = new PKIXPolicyNode(new ArrayList(),
                                                           index,
                                                           _childExpectedPolicies,
                                                           _node,
                                                           _pq,
                                                           _poid.getId(),
                                                           false);
                _node.addChild(_child);
                policyNodes[index].add(_child);
                return;
            }
        }
    }
    
    protected static void prepareNextCertB1(
            int i,
            List[] policyNodes,
            String id_p,
            Map m_idp,
            X509Certificate cert
            ) throws AnnotatedException,CertPathValidatorException
    {
        boolean idp_found = false;
        Iterator nodes_i = policyNodes[i].iterator();
        while (nodes_i.hasNext())
        {
            PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
            if (node.getValidPolicy().equals(id_p))
            {
                idp_found = true;
                node.expectedPolicies = (Set)m_idp.get(id_p);
                break;
            }
        }

        if (!idp_found)
        {
            nodes_i = policyNodes[i].iterator();
            while (nodes_i.hasNext())
            {
                PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                if (ANY_POLICY.equals(node.getValidPolicy()))
                {
                    Set pq = null;
                    ASN1Sequence policies = (ASN1Sequence)getExtensionValue(cert, CERTIFICATE_POLICIES);
                    Enumeration e = policies.getObjects();
                    while (e.hasMoreElements())
                    {
                        PolicyInformation pinfo = PolicyInformation.getInstance(e.nextElement());
                        if (ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId()))
                        {
                            pq = getQualifierSet(pinfo.getPolicyQualifiers());
                            break;
                        }
                    }
                    boolean ci = false;
                    if (cert.getCriticalExtensionOIDs() != null)
                    {
                        ci = cert.getCriticalExtensionOIDs().contains(CERTIFICATE_POLICIES);
                    }

                    PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                    if (ANY_POLICY.equals(p_node.getValidPolicy()))
                    {
                        PKIXPolicyNode c_node = new PKIXPolicyNode(
                                new ArrayList(), i,
                                (Set)m_idp.get(id_p),
                                p_node, pq, id_p, ci);
                        p_node.addChild(c_node);
                        policyNodes[i].add(c_node);
                    }
                    break;
                }
            }
        }
    }
    
    protected static PKIXPolicyNode prepareNextCertB2(
            int i,
            List[] policyNodes,
            String id_p,
            PKIXPolicyNode validPolicyTree) 
    {
        Iterator nodes_i = policyNodes[i].iterator();
        while (nodes_i.hasNext())
        {
            PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
            if (node.getValidPolicy().equals(id_p))
            {
                PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                p_node.removeChild(node);
                nodes_i.remove();
                for (int k = (i - 1); k >= 0; k--)
                {
                    List nodes = policyNodes[k];
                    for (int l = 0; l < nodes.size(); l++)
                    {
                        PKIXPolicyNode node2 = (PKIXPolicyNode)nodes.get(l);
                        if (!node2.hasChildren())
                        {
                            validPolicyTree = removePolicyNode(validPolicyTree, policyNodes, node2);
                            if (validPolicyTree == null)
                            {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return validPolicyTree;
    }
    
    protected static boolean isAnyPolicy(
        Set policySet)
    {
        return policySet == null || policySet.contains(ANY_POLICY) || policySet.isEmpty();
    }
    
}
