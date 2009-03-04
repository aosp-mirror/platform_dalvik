/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*
*******************************************************************************
*/

package com.ibm.icu4jni.text;

/**
* Collation key wrapper, containing the byte array sort key.
* @author syn wee quek
* @stable ICU 2.4
*/

public final class CollationKey implements Comparable
{ 
  // public methods -----------------------------------------------

  /**
  * Bitwise comparison for the collation keys
  * @param target CollationKey to be compared
  * @return comparison result from Collator, RESULT_LESS, RESULT_EQUAL, 
  *         RESULT_GREATER
  * @stable ICU 2.4    
  */
  public int compareTo(CollationKey target)
  {
    byte tgtbytes[] = target.m_bytes_;
    
    if (m_bytes_ == null || m_bytes_.length == 0) {
      if (tgtbytes == null || tgtbytes.length == 0) {
        return Collator.RESULT_EQUAL;
      }
      return Collator.RESULT_LESS;
    }
    else {
      if (tgtbytes == null || tgtbytes.length == 0) {
        return Collator.RESULT_GREATER;
      }
    }
        
    int count = m_bytes_.length;
    if (tgtbytes.length < count) {
      count = tgtbytes.length;
    }

    int s,
        t;
    for (int i = 0; i < count; i ++)
    {
      // unable to use Arrays.equals
      s = m_bytes_[i] & UNSIGNED_BYTE_MASK_;
      t = tgtbytes[i] & UNSIGNED_BYTE_MASK_;
      if (s < t) {
        return Collator.RESULT_LESS;
      }
      if (s > t) {
        return Collator.RESULT_GREATER;
      }
    }

    if (m_bytes_.length < target.m_bytes_.length) {
      return Collator.RESULT_LESS;
    }
    
    if (m_bytes_.length > target.m_bytes_.length) {
      return Collator.RESULT_GREATER;
    }
    
    return Collator.RESULT_EQUAL;
  }
  
  /**
  * Bitwise comparison for the collation keys.
  * Argument is casted to CollationKey
  * @param target CollationKey to be compared
  * @return comparison result from Collator, RESULT_LESS, RESULT_EQUAL, 
  * RESULT_GREATER
  * @stable ICU 2.4
  */
  public int compareTo(Object target)
  {
    return compareTo((CollationKey)target);
  }

  /**
  * Checks if target object is equal to this object.
  * Target is first casted to CollationKey and bitwise compared.
  * @param target comparison object
  * @return true if both objects are equal, false otherwise
  * @stable ICU 2.4
  */
  public boolean equals(Object target)
  {
    if (this == target) {
      return true;
    }
      
    // checks getClass here since CollationKey is final not subclassable
    if (target == null || target.getClass() != getClass()) {
      return false;
    }
    
    return compareTo((CollationKey)target) == Collator.RESULT_EQUAL;
  }

  /**
  * Creates a hash code for this CollationKey. 
  * Compute the hash by iterating sparsely over about 32 (up to 63) bytes 
  * spaced evenly through the string.  For each byte, multiply the previous 
  * hash value by a prime number and add the new byte in, like a linear 
  * congruential random number generator, producing a pseudorandom 
  * deterministic value well distributed over the output range.
  * @return hash value of collation key. Hash value is never 0.
  * @stable ICU 2.4
  */
  public int hashCode()
  {
    if (m_hash_ == 0)
    {
      if (m_bytes_ != null || m_bytes_.length != 0) 
      {                        
        int len = m_bytes_.length;
        int inc = ((len - 32) / 32) + 1;  
        for (int i = 0; i < len;)
        {
          m_hash_ = (m_hash_ * 37) + m_bytes_[i];
          i += inc;                         
        }                                     
      }             
      if (m_hash_ == 0)
        m_hash_ = 1;
    }
    return m_hash_;
  }

  /**
  * Create the value of the Collation key in term of bytes
  * @return value of Collation key in bytes
  * @stable ICU 2.4
  */
  public byte[] toByteArray()
  {
    if (m_bytes_ == null || m_bytes_.length == 0)
      return null;
      
    return (byte[])m_bytes_.clone(); 
  }

  // package constructors ----------------------------------------------
  
  /**
  * Default constructor, for use by the Collator and its subclasses.
  */
  CollationKey()
  {
    m_hash_ = 0;
  }
  
  /**
  * Constructor, for use only by the Collator and its subclasses.
  */
  CollationKey(byte[] bytes)
  {
    m_bytes_ = bytes;
    m_hash_ = 0;
  }

  // private data members -----------------------------------------------
  
  private byte m_bytes_[];
  
  /**
  * Mask value to retrieve a single unsigned byte
  */
  private static final int UNSIGNED_BYTE_MASK_ = 0x00FF;
  
  /**
  * Cached hash value
  */
  private int m_hash_;
}
