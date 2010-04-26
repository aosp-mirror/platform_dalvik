package org.bouncycastle.asn1.cmp;

public interface PKIStatus
{

  public static final int GRANTED                 = 0;
  public static final int GRANTED_WITH_MODS       = 1;
  public static final int REJECTION               = 2;
  public static final int WAITING                 = 3;
  public static final int REVOCATION_WARNING      = 4;
  public static final int REVOCATION_NOTIFICATION = 5;

}
