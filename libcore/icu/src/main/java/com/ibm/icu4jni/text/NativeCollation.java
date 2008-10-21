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
* Package static class for declaring all native methods for collation use.
* @author syn wee quek
* @internal ICU 2.4
*/
    
public final class NativeCollation
{
  // collator methods ---------------------------------------------
  
  public NativeCollation() {
      
  }
    
  /**
  * Method to create a new C Collator using the default locale rules.
  * @return new c collator
  * @internal ICU 2.4
  */
  static native int openCollator();
  
  /**
  * Method to create a new C Collator using the argument locale rules.
  * @param locale locale name
  * @return new c collator
  * @internal ICU 2.4
  */
  static native int openCollator(String locale);
  
  /**
  * Method to create a new C Collator using the argument rules.
  * @param rules , set of collation rules
  * @param normalizationmode default normalization mode
  * @param collationstrength default collation strength
  * @return new c collator
  * @internal ICU 2.4
  */
  static native int openCollatorFromRules(String rules,
                                           int normalizationmode,
                                           int collationstrength);

  /** 
  * Close a C collator
  * Once closed, a UCollatorOld should not be used.
  * @param collatoraddress The UCollatorOld to close
  * @internal ICU 2.4
  */
  static native void closeCollator(int collatoraddress);
  
  /**
  * Compare two strings.
  * The strings will be compared using the normalization mode and options
  * specified in openCollator or openCollatorFromRules
  * @param collatoraddress address of the c collator
  * @param source The source string.
  * @param target The target string.
  * @return result of the comparison, Collation.EQUAL, 
  *         Collation.GREATER or Collation.LESS
  * @internal ICU 2.4
  */
  static native int compare(int collatoraddress, String source, 
                            String target);
                             
  /**
  * Get the normalization mode for this object.
  * The normalization mode influences how strings are compared.
  * @param collatoraddress 
  * @return normalization mode; one of the values from Normalization
  * @internal ICU 2.4
  */
  static native int getNormalization(int collatoraddress);

  /**
  * Set the normalization mode used int this object
  * The normalization mode influences how strings are compared.
  * @param collatoraddress the address of the C collator
  * @param normalizationmode desired normalization mode; one of the values 
  *        from Normalization
  * @internal ICU 2.4
  */
  static native void setNormalization(int collatoraddress, 
                                      int normalizationmode);

  /**
  * Get the collation rules from a UCollator.
  * The rules will follow the rule syntax.
  * @param collatoraddress the address of the C collator
  * @return collation rules.
  * @internal ICU 2.4
  */
  static native String getRules(int collatoraddress);

  /**
  * Get a sort key for the argument string
  * Sort keys may be compared using java.util.Arrays.equals
  * @param collatoraddress address of the C collator
  * @param source string for key to be generated
  * @return sort key
  * @internal ICU 2.4
  */
  static native byte[] getSortKey(int collatoraddress, String source);
                                   
  /**
  * Gets the version information for collation. 
  * @param collatoraddress address of the C collator
  * @return version information
  * @internal ICU 2.4
  */
  // private native String getVersion(int collatoraddress);

  /**
  * Universal attribute setter.
  * @param collatoraddress address of the C collator
  * @param type type of attribute to be set
  * @param value attribute value
  * @exception RuntimeException when error occurs while setting attribute value
  * @internal ICU 2.4
  */
  static native void setAttribute(int collatoraddress, int type, int value);

  /**
  * Universal attribute getter
  * @param collatoraddress address of the C collator
  * @param type type of attribute to be set
  * @return attribute value
  * @exception RuntimeException thrown when error occurs while getting attribute value
  * @internal ICU 2.4
  */
  static native int getAttribute(int collatoraddress, int type);

  /**
  * Thread safe cloning operation
  * @param collatoraddress address of C collator to be cloned
  * @return address of the new clone
  * @exception RuntimeException thrown when error occurs while cloning
  * @internal ICU 2.4
  */
  static native int safeClone(int collatoraddress);
  
  /** 
  * Create a CollationElementIterator object that will iterator over the 
  * elements in a string, using the collation rules defined in this 
  * RuleBasedCollator
  * @param collatoraddress address of C collator
  * @param source string to iterate over
  * @return address of C collationelementiterator
  * @internal ICU 2.4
  */
  static native int getCollationElementIterator(int collatoraddress, 
                                                 String source);
                                                 
  /**
  * Returns a hash of this collation object
  * @param collatoraddress address of C collator
  * @return hash of this collation object
  * @internal ICU 2.4
  */
  static native int hashCode(int collatoraddress);

    
  // collationelementiterator methods -------------------------------------
  
  /**
  * Close a C collation element iterator.
  * @param address of C collation element iterator to close.
  * @internal ICU 2.4
  */
  static native void closeElements(int address);

  /**
  * Reset the collation elements to their initial state.
  * This will move the 'cursor' to the beginning of the text.
  * @param address of C collation element iterator to reset.
  * @internal ICU 2.4
  */
  static native void reset(int address);

  /**
  * Get the ordering priority of the next collation element in the text.
  * A single character may contain more than one collation element.
  * @param address if C collation elements containing the text.
  * @return next collation elements ordering, or NULLORDER if the end of the 
  *         text is reached.
  * @internal ICU 2.4
  */
  static native int next(int address);

  /**
  * Get the ordering priority of the previous collation element in the text.
  * A single character may contain more than one collation element.
  * @param address of the C collation element iterator containing the text.
  * @return previous collation element ordering, or NULLORDER if the end of 
  *         the text is reached.
  * @internal ICU 2.4
  */
  static native int previous(int address);

  /**
  * Get the maximum length of any expansion sequences that end with the 
  * specified comparison order.
  * @param address of the C collation element iterator containing the text.
  * @param order collation order returned by previous or next.
  * @return maximum length of any expansion sequences ending with the 
  *         specified order.
  * @internal ICU 2.4
  */
  static native int getMaxExpansion(int address, int order);

  /**
  * Set the text containing the collation elements.
  * @param address of the C collation element iterator to be set
  * @param source text containing the collation elements.
  * @internal ICU 2.4
  */
  static native void setText(int address, String source);

  /**
  * Get the offset of the current source character.
  * This is an offset into the text of the character containing the current
  * collation elements.
  * @param address of the C collation elements iterator to query.
  * @return offset of the current source character.
  * @internal ICU 2.4
  */
  static native int getOffset(int address);

  /**
  * Set the offset of the current source character.
  * This is an offset into the text of the character to be processed.
  * @param address of the C collation element iterator to set.
  * @param offset The desired character offset.
  * @internal ICU 2.4
  */
  static native void setOffset(int address, int offset);

  // BEGIN android-added
  static String[] getAvailableLocalesImpl() {
      int count =  getAvailableLocalesCountImpl();
      String[] result = new String[count];
      
      for(int i = 0; i < count; i++) {
          result[i] = getAvailableLocalesImpl(i);
      }
      return result;
  }
  
  private static native String getAvailableLocalesImpl(int i);
  
  private static native int getAvailableLocalesCountImpl();
  // END android-added
}
