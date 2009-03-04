package org.bouncycastle.jce.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DEREnumerated;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.asn1.x509.NameConstraints;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.X509Extensions;

/**
 * CertPathValidatorSpi implemenation for X.509 Certificate validation ala rfc 3280<br />
 **/
public class PKIXCertPathValidatorSpi extends CertPathValidatorSpi
{
    private static final String CERTIFICATE_POLICIES = X509Extensions.CertificatePolicies.getId();
    private static final String POLICY_MAPPINGS = X509Extensions.PolicyMappings.getId();
    private static final String INHIBIT_ANY_POLICY = X509Extensions.InhibitAnyPolicy.getId();
    private static final String ISSUING_DISTRIBUTION_POINT = X509Extensions.IssuingDistributionPoint.getId();
    private static final String DELTA_CRL_INDICATOR = X509Extensions.DeltaCRLIndicator.getId();
    private static final String POLICY_CONSTRAINTS = X509Extensions.PolicyConstraints.getId();
    private static final String BASIC_CONSTRAINTS = X509Extensions.BasicConstraints.getId();
    private static final String SUBJECT_ALTERNATIVE_NAME = X509Extensions.SubjectAlternativeName.getId();
    private static final String NAME_CONSTRAINTS = X509Extensions.NameConstraints.getId();
    private static final String KEY_USAGE = X509Extensions.KeyUsage.getId();

    private static final String CRL_NUMBER = X509Extensions.CRLNumber.getId();

    private static final String ANY_POLICY = "2.5.29.32.0";


    /*
     * key usage bits
     */
    private static final int    KEY_CERT_SIGN = 5;
    private static final int    CRL_SIGN = 6;

    private static final String[] crlReasons = new String[] {
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
    
    
    public CertPathValidatorResult engineValidate(
        CertPath certPath,
        CertPathParameters params)
        throws CertPathValidatorException, InvalidAlgorithmParameterException
    {
        if (!(params instanceof PKIXParameters))
        {
            throw new InvalidAlgorithmParameterException("params must be a PKIXParameters instance");
        }

        PKIXParameters paramsPKIX = (PKIXParameters)params;
        if (paramsPKIX.getTrustAnchors() == null)
        {
            throw new InvalidAlgorithmParameterException("trustAnchors is null, this is not allowed for path validation");
        }

        //
        // 6.1.1 - inputs
        //

        //
        // (a)
        //
        List    certs = certPath.getCertificates();
        int     n = certs.size();
        
        if (certs.isEmpty())
        {
            throw new CertPathValidatorException("CertPath is empty", null, certPath, 0);
        }

        //
        // (b)
        //
        Date validDate = CertPathValidatorUtilities.getValidDate(paramsPKIX);

        //
        // (c)
        //
        Set userInitialPolicySet = paramsPKIX.getInitialPolicies();

        //
        // (d)
        // 
        X509Certificate lastCert = (X509Certificate)certs.get(certs.size() - 1);
        TrustAnchor trust = CertPathValidatorUtilities.findTrustAnchor(lastCert, certPath, certs.size() - 1, paramsPKIX.getTrustAnchors());

        if (trust == null)
        {
            throw new CertPathValidatorException("TrustAnchor for CertPath not found.", null, certPath, -1);
        }
        
        //
        // (e), (f), (g) are part of the paramsPKIX object.
        //

        Iterator certIter;
        int index = 0;
        int i;

        //
        // 6.1.2 - setup
        //

        //
        // (a)
        //
        List     []  policyNodes = new ArrayList[n + 1];
        for (int j = 0; j < policyNodes.length; j++)
        {
            policyNodes[j] = new ArrayList();
        }

        Set policySet = new HashSet();

        policySet.add(ANY_POLICY);

        PKIXPolicyNode  validPolicyTree = new PKIXPolicyNode(new ArrayList(), 0, policySet, null, new HashSet(), ANY_POLICY, false);

        policyNodes[0].add(validPolicyTree);

        //
        // (b)
        //
        Set     permittedSubtreesDN = new HashSet();
        Set     permittedSubtreesEmail = new HashSet();
        Set     permittedSubtreesIP = new HashSet();
    
        //
        // (c)
        //
        Set     excludedSubtreesDN = new HashSet();
        Set     excludedSubtreesEmail = new HashSet();
        Set     excludedSubtreesIP = new HashSet();
    
        //
        // (d)
        //
        int explicitPolicy;
        Set acceptablePolicies = null;

        if (paramsPKIX.isExplicitPolicyRequired())
        {
            explicitPolicy = 0;
        }
        else
        {
            explicitPolicy = n + 1;
        }

        //
        // (e)
        //
        int inhibitAnyPolicy;

        if (paramsPKIX.isAnyPolicyInhibited())
        {
            inhibitAnyPolicy = 0;
        }
        else
        {
            inhibitAnyPolicy = n + 1;
        }
    
        //
        // (f)
        //
        int policyMapping;

        if (paramsPKIX.isPolicyMappingInhibited())
        {
            policyMapping = 0;
        }
        else
        {
            policyMapping = n + 1;
        }
    
        //
        // (g), (h), (i), (j)
        //
        PublicKey workingPublicKey;
        X500Principal workingIssuerName;

        X509Certificate sign = trust.getTrustedCert();
        try
        {
            if (sign != null)
            {
                workingIssuerName = CertPathValidatorUtilities.getSubjectPrincipal(sign);
                workingPublicKey = sign.getPublicKey();
            }
            else
            {
                workingIssuerName = new X500Principal(trust.getCAName());
                workingPublicKey = trust.getCAPublicKey();
            }
        }
        catch (IllegalArgumentException ex)
        {
            throw new CertPathValidatorException("TrustAnchor subjectDN: " + ex.toString());
        }

        boolean trustAnchorInChain = false;
        if (workingIssuerName.equals(CertPathValidatorUtilities.getSubjectPrincipal(lastCert)) &&
            workingPublicKey.equals(lastCert.getPublicKey()))
        {
            trustAnchorInChain = true;
        }

        AlgorithmIdentifier workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
        DERObjectIdentifier workingPublicKeyAlgorithm = workingAlgId.getObjectId();
        DEREncodable        workingPublicKeyParameters = workingAlgId.getParameters();

        //
        // (k)
        //
        int maxPathLength = n;

        //
        // 6.1.3
        //
        Iterator tmpIter;
        int tmpInt;

        if (paramsPKIX.getTargetCertConstraints() != null
            && !paramsPKIX.getTargetCertConstraints().match((X509Certificate)certs.get(0)))
        {
            throw new CertPathValidatorException("target certificate in certpath does not match targetcertconstraints", null, certPath, 0);
        }


        // 
        // initialise CertPathChecker's
        //
        List  pathCheckers = paramsPKIX.getCertPathCheckers();
        certIter = pathCheckers.iterator();
        while (certIter.hasNext())
        {
            ((PKIXCertPathChecker)certIter.next()).init(false);
        }

        X509Certificate cert = null;

        for (index = certs.size() - 1; index >= 0 ; index--)
        {
            try
            {
                //
                // i as defined in the algorithm description
                //
                i = n - index;
    
                //
                // set certificate to be checked in this round
                // sign and workingPublicKey and workingIssuerName are set
                // at the end of the for loop and initialied the
                // first time from the TrustAnchor
                //
                cert = (X509Certificate)certs.get(index);
    
                //
                // 6.1.3
                //
    
                //
                // (a) verify
                //
                try
                {
                    // (a) (1)
                    //
                    cert.verify(workingPublicKey, "BC");
                }
                catch (GeneralSecurityException e)
                {
                    throw new CertPathValidatorException("Could not validate certificate signature.", e, certPath, index);
                }
    
                try
                {
                    // (a) (2)
                    //
                    cert.checkValidity(validDate);
                }
                catch (CertificateExpiredException e)
                {
                    throw new CertPathValidatorException("Could not validate certificate: " + e.getMessage(), e, certPath, index);
                }
                catch (CertificateNotYetValidException e)
                {
                    throw new CertPathValidatorException("Could not validate certificate: " + e.getMessage(), e, certPath, index);
                }
    
                //
                // (a) (3)
                //
                if (paramsPKIX.isRevocationEnabled())
                {
                    checkCRLs(paramsPKIX, cert, validDate, sign, workingPublicKey);
                }
    
                //
                // (a) (4) name chaining
                //
                if (!CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert).equals(workingIssuerName))
                {
                    throw new CertPathValidatorException(
                                "IssuerName(" + CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert) +
                                ") does not match SubjectName(" + workingIssuerName +
                                ") of signing certificate", null, certPath, index);
                }
    
                //
                // (b), (c) permitted and excluded subtree checking.
                //
                if (!(CertPathValidatorUtilities.isSelfIssued(cert) && (i < n)))
                {
                    X500Principal principal = CertPathValidatorUtilities.getSubjectPrincipal(cert);
                    ASN1InputStream aIn = new ASN1InputStream(principal.getEncoded());
                    ASN1Sequence    dns;
    
                    try
                    {
                        dns = (ASN1Sequence)aIn.readObject();
                    }
                    catch (IOException e)
                    {
                        throw new CertPathValidatorException("exception extracting subject name when checking subtrees");
                    }
    
                    CertPathValidatorUtilities.checkPermittedDN(permittedSubtreesDN, dns);
    
                    CertPathValidatorUtilities.checkExcludedDN(excludedSubtreesDN, dns);
            
                    ASN1Sequence   altName = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, SUBJECT_ALTERNATIVE_NAME);
                    if (altName != null)
                    {
                        for (int j = 0; j < altName.size(); j++)
                        {
                            ASN1TaggedObject o = (ASN1TaggedObject)altName.getObjectAt(j);
    
                            switch(o.getTagNo())
                            {
                            case 1:
                                String email = DERIA5String.getInstance(o, true).getString();
    
                                CertPathValidatorUtilities.checkPermittedEmail(permittedSubtreesEmail, email);
                                CertPathValidatorUtilities.checkExcludedEmail(excludedSubtreesEmail, email);
                                break;
                            case 4:
                                ASN1Sequence altDN = ASN1Sequence.getInstance(o, true);
    
                                CertPathValidatorUtilities.checkPermittedDN(permittedSubtreesDN, altDN);
                                CertPathValidatorUtilities.checkExcludedDN(excludedSubtreesDN, altDN);
                                break;
                            case 7:
                                byte[] ip = ASN1OctetString.getInstance(o, true).getOctets();
    
                                CertPathValidatorUtilities.checkPermittedIP(permittedSubtreesIP, ip);
                                CertPathValidatorUtilities.checkExcludedIP(excludedSubtreesIP, ip);
                            }
                        }
                    }
                }
    
                //
                // (d) policy Information checking against initial policy and
                // policy mapping
                //
                ASN1Sequence   certPolicies = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, CERTIFICATE_POLICIES);
                if (certPolicies != null && validPolicyTree != null)
                {
                    //
                    // (d) (1)
                    //
                    Enumeration e = certPolicies.getObjects();
                    Set         pols = new HashSet();
                        
                    while (e.hasMoreElements())
                    {
                        PolicyInformation   pInfo = PolicyInformation.getInstance(e.nextElement());
                        DERObjectIdentifier pOid = pInfo.getPolicyIdentifier();
                        
                        pols.add(pOid.getId());
    
                        if (!ANY_POLICY.equals(pOid.getId()))
                        {
                            Set pq = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                            
                            boolean match = CertPathValidatorUtilities.processCertD1i(i, policyNodes, pOid, pq);
                            
                            if (!match)
                            {
                                CertPathValidatorUtilities.processCertD1ii(i, policyNodes, pOid, pq);
                            }
                        }
                    }
    
                    if (acceptablePolicies == null || acceptablePolicies.contains(ANY_POLICY))
                    {
                        acceptablePolicies = pols;
                    }
                    else
                    {
                        Iterator    it = acceptablePolicies.iterator();
                        Set         t1 = new HashSet();
    
                        while (it.hasNext())
                        {
                            Object  o = it.next();
    
                            if (pols.contains(o))
                            {
                                t1.add(o);
                            }
                        }
    
                        acceptablePolicies = t1;
                    }
    
                    //
                    // (d) (2)
                    //
                    if ((inhibitAnyPolicy > 0) || ((i < n) && CertPathValidatorUtilities.isSelfIssued(cert)))
                    {
                        e = certPolicies.getObjects();
    
                        while (e.hasMoreElements())
                        {
                            PolicyInformation   pInfo = PolicyInformation.getInstance(e.nextElement());
    
                            if (ANY_POLICY.equals(pInfo.getPolicyIdentifier().getId()))
                            {
                                Set    _apq   = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                                List      _nodes = policyNodes[i - 1];
                                
                                for (int k = 0; k < _nodes.size(); k++)
                                {
                                    PKIXPolicyNode _node = (PKIXPolicyNode)_nodes.get(k);
                                    
                                    Iterator _policySetIter = _node.getExpectedPolicies().iterator();
                                    while (_policySetIter.hasNext())
                                    {
                                        Object _tmp = _policySetIter.next();
                                        
                                        String _policy;
                                        if (_tmp instanceof String)
                                        {
                                            _policy = (String)_tmp;
                                        }
                                        else if (_tmp instanceof DERObjectIdentifier)
                                        {
                                            _policy = ((DERObjectIdentifier)_tmp).getId();
                                        }
                                        else
                                        {
                                            continue;
                                        }
                                        
                                        boolean  _found        = false;
                                        Iterator _childrenIter = _node.getChildren();
    
                                        while (_childrenIter.hasNext())
                                        {
                                            PKIXPolicyNode _child = (PKIXPolicyNode)_childrenIter.next();
    
                                            if (_policy.equals(_child.getValidPolicy()))
                                            {
                                                _found = true;
                                            }
                                        }
    
                                        if (!_found)
                                        {
                                            Set _newChildExpectedPolicies = new HashSet();
                                            _newChildExpectedPolicies.add(_policy);
    
                                            PKIXPolicyNode _newChild = new PKIXPolicyNode(new ArrayList(),
                                                                                          i,
                                                                                          _newChildExpectedPolicies,
                                                                                          _node,
                                                                                          _apq,
                                                                                          _policy,
                                                                                          false);
                                            _node.addChild(_newChild);
                                            policyNodes[i].add(_newChild);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                
                    //
                    // (d) (3)
                    //
                    for (int j = (i - 1); j >= 0; j--)
                    {
                        List      nodes = policyNodes[j];
                        
                        for (int k = 0; k < nodes.size(); k++)
                        {
                            PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                            if (!node.hasChildren())
                            {
                                validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                                if (validPolicyTree == null)
                                {
                                    break;
                                }
                            }
                        }
                    }
                
                    //
                    // d (4)
                    //
                    Set criticalExtensionOids = cert.getCriticalExtensionOIDs();
                    
                    if (criticalExtensionOids != null)
                    {
                        boolean critical = criticalExtensionOids.contains(CERTIFICATE_POLICIES);
                    
                        List      nodes = policyNodes[i];
                        for (int j = 0; j < nodes.size(); j++)
                        {
                            PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(j);
                            node.setCritical(critical);
                        }
                    }
                }
    
                // 
                // (e)
                //
                if (certPolicies == null)
                {
                    validPolicyTree = null;
                }
    
                //
                // (f)
                //
                if (explicitPolicy <= 0 && validPolicyTree == null)
                {
                    throw new CertPathValidatorException("No valid policy tree found when one expected.");
                }
    
                //
                // 6.1.4
                //
    
                if (i != n) // if not at the end-entity certificate
                {
                    if (cert != null && cert.getVersion() == 1)
                    {
                        if (!(i == 1 && trustAnchorInChain)) // if not at the root certificate
                        {
                            throw new CertPathValidatorException(
                                "Version 1 certs can't be used as intermediate certificates");
                        }
                    }
    
                    //
                    //
                    // (a) check the policy mappings
                    //
                    DERObject   pm = CertPathValidatorUtilities.getExtensionValue(cert, POLICY_MAPPINGS);
                    if (pm != null)
                    {
                        ASN1Sequence mappings = (ASN1Sequence)pm;
                    
                        for (int j = 0; j < mappings.size(); j++)
                        {
                            ASN1Sequence    mapping = (ASN1Sequence)mappings.getObjectAt(j);
    
                            DERObjectIdentifier issuerDomainPolicy = (DERObjectIdentifier)mapping.getObjectAt(0);
                            DERObjectIdentifier subjectDomainPolicy = (DERObjectIdentifier)mapping.getObjectAt(1);
    
                            if (ANY_POLICY.equals(issuerDomainPolicy.getId()))
                            {
                            
                                throw new CertPathValidatorException("IssuerDomainPolicy is anyPolicy");
                            }
                        
                            if (ANY_POLICY.equals(subjectDomainPolicy.getId()))
                            {
                            
                                throw new CertPathValidatorException("SubjectDomainPolicy is anyPolicy");
                            }
                        }
                    }
                  
                    // (b)
                    //
                    if (pm != null)
                    {
                        ASN1Sequence mappings = (ASN1Sequence)pm;
                        Map m_idp = new HashMap();
                        Set s_idp = new HashSet();
                        
                        for (int j = 0; j < mappings.size(); j++)
                        {
                            ASN1Sequence mapping = (ASN1Sequence)mappings.getObjectAt(j);
                            String id_p = ((DERObjectIdentifier)mapping.getObjectAt(0)).getId();
                            String sd_p = ((DERObjectIdentifier)mapping.getObjectAt(1)).getId();
                            Set tmp;
                            
                            if (!m_idp.containsKey(id_p))
                            {
                                tmp = new HashSet();
                                tmp.add(sd_p);
                                m_idp.put(id_p, tmp);
                                s_idp.add(id_p);
                            }
                            else
                            {
                                tmp = (Set)m_idp.get(id_p);
                                tmp.add(sd_p);
                            }
                        }
    
                        Iterator it_idp = s_idp.iterator();
                        while (it_idp.hasNext())
                        {
                            String id_p = (String)it_idp.next();
    
                            //
                            // (1)
                            //
                            if (policyMapping > 0)
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
                                            ASN1Sequence policies = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(
                                                    cert, CERTIFICATE_POLICIES);
                                            Enumeration e = policies.getObjects();
                                            while (e.hasMoreElements())
                                            {
                                                PolicyInformation pinfo = PolicyInformation.getInstance(e.nextElement());
                                                if (ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId()))
                                                {
                                                    pq = CertPathValidatorUtilities.getQualifierSet(pinfo.getPolicyQualifiers());
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
    
                            //
                            // (2)
                            //
                            }
                            else if (policyMapping <= 0)
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
                                                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node2);
                                                    if (validPolicyTree == null)
                                                    {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    //
                    // (g) handle the name constraints extension
                    //
                    ASN1Sequence ncSeq = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, NAME_CONSTRAINTS);
                    if (ncSeq != null)
                    {
                        NameConstraints nc = new NameConstraints(ncSeq);
    
                        //
                        // (g) (1) permitted subtrees
                        //
                        ASN1Sequence permitted = nc.getPermittedSubtrees();
                        if (permitted != null)
                        {
                            Enumeration e = permitted.getObjects();
                            while (e.hasMoreElements())
                            {
                                GeneralSubtree  subtree = GeneralSubtree.getInstance(e.nextElement());
                                GeneralName     base = subtree.getBase();
    
                                switch(base.getTagNo())
                                {
                                    case 1:
                                        permittedSubtreesEmail = CertPathValidatorUtilities.intersectEmail(permittedSubtreesEmail, DERIA5String.getInstance(base.getName()).getString());
                                        break;
                                    case 4:
                                        permittedSubtreesDN = CertPathValidatorUtilities.intersectDN(permittedSubtreesDN, (ASN1Sequence)base.getName());
                                        break;
                                    case 7:
                                        permittedSubtreesIP = CertPathValidatorUtilities.intersectIP(permittedSubtreesIP, ASN1OctetString.getInstance(base.getName()).getOctets());
                                        break;
                                }
                            }
                        }
                    
                        //
                        // (g) (2) excluded subtrees
                        //
                        ASN1Sequence excluded = nc.getExcludedSubtrees();
                        if (excluded != null)
                        {
                            Enumeration e = excluded.getObjects();
                            while (e.hasMoreElements())
                            {
                                GeneralSubtree  subtree = GeneralSubtree.getInstance(e.nextElement());
                                GeneralName     base = subtree.getBase();
    
                                switch(base.getTagNo())
                                {
                                case 1:
                                    excludedSubtreesEmail = CertPathValidatorUtilities.unionEmail(excludedSubtreesEmail, DERIA5String.getInstance(base.getName()).getString());
                                    break;
                                case 4:
                                    excludedSubtreesDN = CertPathValidatorUtilities.unionDN(excludedSubtreesDN, (ASN1Sequence)base.getName());
                                    break;
                                case 7:
                                    excludedSubtreesIP = CertPathValidatorUtilities.unionIP(excludedSubtreesIP, ASN1OctetString.getInstance(base.getName()).getOctets());
                                    break;
                                }
                            }
                        }
                    }
    
                    //
                    // (h)
                    //
                    if (!CertPathValidatorUtilities.isSelfIssued(cert))
                    {
                        //
                        // (1)
                        //
                        if (explicitPolicy != 0)
                        {
                            explicitPolicy--;
                        }
                    
                        //
                        // (2)
                        //
                        if (policyMapping != 0)
                        {
                            policyMapping--;
                        }
                    
                        //
                        // (3)
                        //
                        if (inhibitAnyPolicy != 0)
                        {
                            inhibitAnyPolicy--;
                        }
                    }
            
                    //
                    // (i)
                    //
                    ASN1Sequence pc = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, POLICY_CONSTRAINTS);
                
                    if (pc != null)
                    {
                        Enumeration policyConstraints = pc.getObjects();
    
                        while (policyConstraints.hasMoreElements())
                        {
                            ASN1TaggedObject    constraint = (ASN1TaggedObject)policyConstraints.nextElement();
                            switch (constraint.getTagNo())
                            {
                            case 0:
                                tmpInt = DERInteger.getInstance(constraint).getValue().intValue();
                                if (tmpInt < explicitPolicy)
                                {
                                    explicitPolicy = tmpInt;
                                }
                                break;
                            case 1:
                                tmpInt = DERInteger.getInstance(constraint).getValue().intValue();
                                if (tmpInt < policyMapping)
                                {
                                    policyMapping = tmpInt;
                                }
                            break;
                            }
                        }
                    }
            
                    //
                    // (j)
                    //
                    DERInteger iap = (DERInteger)CertPathValidatorUtilities.getExtensionValue(cert, INHIBIT_ANY_POLICY);
                
                    if (iap != null)
                    {
                        int _inhibitAnyPolicy = iap.getValue().intValue();
                    
                        if (_inhibitAnyPolicy < inhibitAnyPolicy)
                        {
                            inhibitAnyPolicy = _inhibitAnyPolicy;
                        }
                    }
            
                    //
                    // (k)
                    //
                    BasicConstraints    bc = BasicConstraints.getInstance(
                            CertPathValidatorUtilities.getExtensionValue(cert, BASIC_CONSTRAINTS));
                    if (bc != null)
                    {
                        if (!(bc.isCA()))
                        {
                            throw new CertPathValidatorException("Not a CA certificate");
                        }
                    }
                    else
                    {
                        if (!(i == 1 && trustAnchorInChain)) // if not at the root certificate
                        {
                            throw new CertPathValidatorException("Intermediate certificate lacks BasicConstraints");
                        }
                    }
                
                    //
                    // (l)
                    //
                    if (!CertPathValidatorUtilities.isSelfIssued(cert))
                    {
                        if (maxPathLength <= 0)
                        {
                            throw new CertPathValidatorException("Max path length not greater than zero");
                        }
                    
                        maxPathLength--;
                    }
            
                    //
                    // (m)
                    //
                    if (bc != null)
                    {
                        BigInteger          _pathLengthConstraint = bc.getPathLenConstraint();
                
                        if (_pathLengthConstraint != null)
                        {
                            int _plc = _pathLengthConstraint.intValue();
    
                            if (_plc < maxPathLength)
                            {
                                maxPathLength = _plc;
                            }
                        }
                    }
            
                    //
                    // (n)
                    //
                    boolean[] _usage = cert.getKeyUsage();
                
                    if ((_usage != null) && !_usage[5])
                    {
                        throw new CertPathValidatorException(
                                    "Issuer certificate keyusage extension is critical an does not permit key signing.\n",
                                    null, certPath, index);
                    }
    
                    //
                    // (o)
                    //
                    if (cert.getCriticalExtensionOIDs() != null)
                    {
                        Set criticalExtensions = new HashSet(cert.getCriticalExtensionOIDs());
                        // these extensions are handle by the algorithem
                        criticalExtensions.remove(KEY_USAGE);
                        criticalExtensions.remove(CERTIFICATE_POLICIES);
                        criticalExtensions.remove(POLICY_MAPPINGS);
                        criticalExtensions.remove(INHIBIT_ANY_POLICY);
                        criticalExtensions.remove(ISSUING_DISTRIBUTION_POINT);
                        criticalExtensions.remove(DELTA_CRL_INDICATOR);
                        criticalExtensions.remove(POLICY_CONSTRAINTS);
                        criticalExtensions.remove(BASIC_CONSTRAINTS);
                        criticalExtensions.remove(SUBJECT_ALTERNATIVE_NAME);
                        criticalExtensions.remove(NAME_CONSTRAINTS);
    
                        tmpIter = pathCheckers.iterator();
                        while (tmpIter.hasNext())
                        {
                            try
                            {
                                ((PKIXCertPathChecker)tmpIter.next()).check(cert, criticalExtensions);
                            }
                            catch (CertPathValidatorException e)
                            {
                                throw new CertPathValidatorException(e.getMessage(), e.getCause(), certPath, index);
                            }
                        }
                        if (!criticalExtensions.isEmpty())
                        {
                            throw new CertPathValidatorException(
                                "Certificate has unsupported critical extension", null, certPath, index);
                        }
                    }
                }
    
                // set signing certificate for next round
                sign = cert;
                workingPublicKey = sign.getPublicKey();
                try
                {
                    workingIssuerName = CertPathValidatorUtilities.getSubjectPrincipal(sign);
                }
                catch (IllegalArgumentException ex)
                {
                    throw new CertPathValidatorException(sign.getSubjectDN().getName() + " :" + ex.toString());
                }
                workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
                workingPublicKeyAlgorithm = workingAlgId.getObjectId();
                workingPublicKeyParameters = workingAlgId.getParameters();
            }
            catch (AnnotatedException e)
            {
                throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath, index);
            }
        }

        //
        // 6.1.5 Wrap-up procedure
        //

        //
        // (a)
        //
        if (!CertPathValidatorUtilities.isSelfIssued(cert) && (explicitPolicy != 0))
        {
            explicitPolicy--;
        }
    
        //
        // (b)
        //
        try
        {
            ASN1Sequence pc = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert, POLICY_CONSTRAINTS);
            if (pc != null)
            {
                Enumeration policyConstraints = pc.getObjects();
    
                while (policyConstraints.hasMoreElements())
                {
                    ASN1TaggedObject    constraint = (ASN1TaggedObject)policyConstraints.nextElement();
                    switch (constraint.getTagNo())
                    {
                    case 0:
                        tmpInt = DERInteger.getInstance(constraint).getValue().intValue();
                        if (tmpInt == 0)
                        {
                            explicitPolicy = 0;
                        }
                        break;
                    }
                }
            }
        }
        catch (AnnotatedException e)
        {
            throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath, index);
        }
    
        //
        // (c) (d) and (e) are already done
        //
    
        //
        // (f) 
        //
        Set criticalExtensions = cert.getCriticalExtensionOIDs();
        
        if (criticalExtensions != null)
        {
            criticalExtensions = new HashSet(criticalExtensions);
            // these extensions are handle by the algorithm
            criticalExtensions.remove(KEY_USAGE);
            criticalExtensions.remove(CERTIFICATE_POLICIES);
            criticalExtensions.remove(POLICY_MAPPINGS);
            criticalExtensions.remove(INHIBIT_ANY_POLICY);
            criticalExtensions.remove(ISSUING_DISTRIBUTION_POINT);
            criticalExtensions.remove(DELTA_CRL_INDICATOR);
            criticalExtensions.remove(POLICY_CONSTRAINTS);
            criticalExtensions.remove(BASIC_CONSTRAINTS);
            criticalExtensions.remove(SUBJECT_ALTERNATIVE_NAME);
            criticalExtensions.remove(NAME_CONSTRAINTS);
        }
        else
        {
            criticalExtensions = new HashSet();
        }
        
        tmpIter = pathCheckers.iterator();
        while (tmpIter.hasNext())
        {
            try
            {
                ((PKIXCertPathChecker)tmpIter.next()).check(cert, criticalExtensions);
            }
            catch (CertPathValidatorException e)
            {
                throw new CertPathValidatorException(e.getMessage(), e.getCause(), certPath, index);
            }
        }
        
        if (!criticalExtensions.isEmpty())
        {
            throw new CertPathValidatorException(
                "Certificate has unsupported critical extension", null, certPath, index);
        }

        //
        // (g)
        //
        PKIXPolicyNode intersection;
        

        //
        // (g) (i)
        //
        if (validPolicyTree == null)
        { 
            if (paramsPKIX.isExplicitPolicyRequired())
            {
                throw new CertPathValidatorException("Explicit policy requested but none available.");
            }
            intersection = null;
        }
        else if (CertPathValidatorUtilities.isAnyPolicy(userInitialPolicySet)) // (g) (ii)
        {
            if (paramsPKIX.isExplicitPolicyRequired())
            {
                if (acceptablePolicies.isEmpty())
                {
                    throw new CertPathValidatorException("Explicit policy requested but none available.");
                }
                else
                {
                    Set _validPolicyNodeSet = new HashSet();
                    
                    for (int j = 0; j < policyNodes.length; j++)
                    {
                        List      _nodeDepth = policyNodes[j];
                        
                        for (int k = 0; k < _nodeDepth.size(); k++)
                        {
                            PKIXPolicyNode _node = (PKIXPolicyNode)_nodeDepth.get(k);
                            
                            if (ANY_POLICY.equals(_node.getValidPolicy()))
                            {
                                Iterator _iter = _node.getChildren();
                                while (_iter.hasNext())
                                {
                                    _validPolicyNodeSet.add(_iter.next());
                                }
                            }
                        }
                    }
                    
                    Iterator _vpnsIter = _validPolicyNodeSet.iterator();
                    while (_vpnsIter.hasNext())
                    {
                        PKIXPolicyNode _node = (PKIXPolicyNode)_vpnsIter.next();
                        String _validPolicy = _node.getValidPolicy();
                        
                        if (!acceptablePolicies.contains(_validPolicy))
                        {
                            //validPolicyTree = removePolicyNode(validPolicyTree, policyNodes, _node);
                        }
                    }
                    if (validPolicyTree != null)
                    {
                        for (int j = (n - 1); j >= 0; j--)
                        {
                            List      nodes = policyNodes[j];
                            
                            for (int k = 0; k < nodes.size(); k++)
                            {
                                PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                                if (!node.hasChildren())
                                {
                                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                                }
                            }
                        }
                    }
                }
            }

            intersection = validPolicyTree;
        }
        else
        {
            //
            // (g) (iii)
            //
            // This implementation is not exactly same as the one described in RFC3280.
            // However, as far as the validation result is concerned, both produce 
            // adequate result. The only difference is whether AnyPolicy is remain 
            // in the policy tree or not. 
            //
            // (g) (iii) 1
            //
            Set _validPolicyNodeSet = new HashSet();
            
            for (int j = 0; j < policyNodes.length; j++)
            {
                List      _nodeDepth = policyNodes[j];
                
                for (int k = 0; k < _nodeDepth.size(); k++)
                {
                    PKIXPolicyNode _node = (PKIXPolicyNode)_nodeDepth.get(k);
                    
                    if (ANY_POLICY.equals(_node.getValidPolicy()))
                    {
                        Iterator _iter = _node.getChildren();
                        while (_iter.hasNext())
                        {
                            PKIXPolicyNode _c_node = (PKIXPolicyNode)_iter.next();
                            if (!ANY_POLICY.equals(_c_node.getValidPolicy()))
                            {
                                _validPolicyNodeSet.add(_c_node);
                            }
                        }
                    }
                }
            }
            
            //
            // (g) (iii) 2
            //
            Iterator _vpnsIter = _validPolicyNodeSet.iterator();
            while (_vpnsIter.hasNext())
            {
                PKIXPolicyNode _node = (PKIXPolicyNode)_vpnsIter.next();
                String _validPolicy = _node.getValidPolicy();

                if (!userInitialPolicySet.contains(_validPolicy))
                {
                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, _node);
                }
            }
            
            //
            // (g) (iii) 4
            //
            if (validPolicyTree != null)
            {
                for (int j = (n - 1); j >= 0; j--)
                {
                    List      nodes = policyNodes[j];
                    
                    for (int k = 0; k < nodes.size(); k++)
                    {
                        PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                        if (!node.hasChildren())
                        {
                            validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                        }
                    }
                }
            }
            
            intersection = validPolicyTree;
        }
 
        if ((explicitPolicy > 0) || (intersection != null))
        {
            return new PKIXCertPathValidatorResult(trust, intersection, workingPublicKey);
        }

        throw new CertPathValidatorException("Path processing failed on policy.", null, certPath, index);
    }
    
    private void checkCRLs(PKIXParameters paramsPKIX, X509Certificate cert, Date validDate, X509Certificate sign, PublicKey workingPublicKey) 
        throws AnnotatedException
    {
        X509CRLSelector crlselect;
        crlselect = new X509CRLSelector();
    
        try
        {
            crlselect.addIssuerName(CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert).getEncoded());
        }
        catch (IOException e)
        {
            throw new AnnotatedException("Cannot extract issuer from certificate: " + e, e);
        }
    
        crlselect.setCertificateChecking(cert);
    
        Iterator crl_iter = CertPathValidatorUtilities.findCRLs(crlselect, paramsPKIX.getCertStores()).iterator();
        boolean validCrlFound = false;
        X509CRLEntry crl_entry;
        while (crl_iter.hasNext())
        {
            X509CRL crl = (X509CRL)crl_iter.next();
    
            if (cert.getNotAfter().after(crl.getThisUpdate()))
            {
                if (crl.getNextUpdate() == null
                    || validDate.before(crl.getNextUpdate())) 
                {
                    validCrlFound = true;
                }
    
                if (sign != null)
                {
                    boolean[] keyusage = sign.getKeyUsage();
    
                    if (keyusage != null
                        && (keyusage.length < 7 || !keyusage[CRL_SIGN]))
                    {
                        throw new AnnotatedException(
                            "Issuer certificate keyusage extension does not permit crl signing.\n" + sign);
                    }
                }
    
                try
                {
                    crl.verify(workingPublicKey, "BC");
                }
                catch (Exception e)
                {
                    throw new AnnotatedException("can't verify CRL: " + e, e);
                }
    
                crl_entry = crl.getRevokedCertificate(cert.getSerialNumber());
                if (crl_entry != null
                    && !validDate.before(crl_entry.getRevocationDate()))
                {
                    String reason = null;
                    
                    if (crl_entry.hasExtensions())
                    {
                        DEREnumerated reasonCode = DEREnumerated.getInstance(CertPathValidatorUtilities.getExtensionValue(crl_entry, X509Extensions.ReasonCode.getId()));
                        if (reasonCode != null)
                        {
                            reason = crlReasons[reasonCode.getValue().intValue()];
                        }
                    }
                    
                    String message = "Certificate revocation after " + crl_entry.getRevocationDate();
                    
                    if (reason != null)
                    {
                        message += ", reason: " + reason;
                    }
                    
                    throw new AnnotatedException(message);
                }
    
                //
                // check the DeltaCRL indicator, base point and the issuing distribution point
                //
                DERObject idp = CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT);
                DERObject dci = CertPathValidatorUtilities.getExtensionValue(crl, DELTA_CRL_INDICATOR);
    
                if (dci != null)
                {
                    X509CRLSelector baseSelect = new X509CRLSelector();
    
                    try
                    {
                        baseSelect.addIssuerName(CertPathValidatorUtilities.getIssuerPrincipal(crl).getEncoded());
                    }
                    catch (IOException e)
                    {
                        throw new AnnotatedException("can't extract issuer from certificate: " + e, e);
                    }
    
                    baseSelect.setMinCRLNumber(((DERInteger)dci).getPositiveValue());
                    baseSelect.setMaxCRLNumber(((DERInteger)CertPathValidatorUtilities.getExtensionValue(crl, CRL_NUMBER)).getPositiveValue().subtract(BigInteger.valueOf(1)));
                    
                    boolean  foundBase = false;
                    Iterator it  = CertPathValidatorUtilities.findCRLs(baseSelect, paramsPKIX.getCertStores()).iterator();
                    while (it.hasNext())
                    {
                        X509CRL base = (X509CRL)it.next();
    
                        DERObject baseIdp = CertPathValidatorUtilities.getExtensionValue(base, ISSUING_DISTRIBUTION_POINT);
                        
                        if (idp == null)
                        {
                            if (baseIdp == null)
                            {
                                foundBase = true;
                                break;
                            }
                        }
                        else
                        {
                            if (idp.equals(baseIdp))
                            {
                                foundBase = true;
                                break;
                            }
                        }
                    }
                    
                    if (!foundBase)
                    {
                        throw new AnnotatedException("No base CRL for delta CRL");
                    }
                }
    
                if (idp != null)
                {
                    IssuingDistributionPoint    p = IssuingDistributionPoint.getInstance(idp);
                    BasicConstraints    bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, BASIC_CONSTRAINTS));
                    
                    if (p.onlyContainsUserCerts() && (bc != null && bc.isCA()))
                    {
                        throw new AnnotatedException("CA Cert CRL only contains user certificates");
                    }
                    
                    if (p.onlyContainsCACerts() && (bc == null || !bc.isCA()))
                    {
                        throw new AnnotatedException("End CRL only contains CA certificates");
                    }
                    
                    if (p.onlyContainsAttributeCerts())
                    {
                        throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted");
                    }
                }
            }
        }
    
        if (!validCrlFound)
        {
            throw new AnnotatedException("no valid CRL found");
        }
    }
}
