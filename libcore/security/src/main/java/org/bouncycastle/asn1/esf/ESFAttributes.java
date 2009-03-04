package org.bouncycastle.asn1.esf;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

public interface ESFAttributes
{
    public static final DERObjectIdentifier  sigPolicyId = PKCSObjectIdentifiers.id_aa_sigPolicyId;
    public static final DERObjectIdentifier  commitmentType = PKCSObjectIdentifiers.id_aa_commitmentType;
    public static final DERObjectIdentifier  signerLocation = PKCSObjectIdentifiers.id_aa_signerLocation;
}
