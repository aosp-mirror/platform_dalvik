/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

package com.ibm.icu4jni.text;

/**
* Interface for storing ICU collation equivalent enum values.
* Constants with the prefix VALUE corresponds to ICU's UColAttributeValues,
* the rest corresponds to UColAttribute.
* @author syn wee quek
* @stable ICU 2.4
*/

public final class CollationAttribute
{ 
  // Collation strength constants ----------------------------------
  /**
  * Default value, accepted by most attributes
  * @stable ICU 2.4
  */ 
  public static final int VALUE_DEFAULT = -1;
  /** 
  * Primary collation strength 
  * @stable ICU 2.4
  */
  public static final int VALUE_PRIMARY = 0;
  /** 
  * Secondary collation strength 
  * @stable ICU 2.4
  */
  public static final int VALUE_SECONDARY = 1;
  /** 
  * Tertiary collation strength 
  * @stable ICU 2.4
  */
  public static final int VALUE_TERTIARY = 2;
  /** 
  * Default collation strength 
  * @stable ICU 2.4
  */
  public static final int VALUE_DEFAULT_STRENGTH = VALUE_TERTIARY;
  /** 
  * Quaternary collation strength 
  * @stable ICU 2.4
  */
  public static final int VALUE_QUATERNARY = 3;
  /** 
  * Identical collation strength 
  * @stable ICU 2.4
  */
  public static final int VALUE_IDENTICAL = 15;

  /** 
   * Turn the feature off - works for FRENCH_COLLATION, CASE_LEVEL, 
   * HIRAGANA_QUATERNARY_MODE and DECOMPOSITION_MODE
   * @stable ICU 2.4
   */
  public static final int VALUE_OFF = 16;
  /** @stable ICU 2.4 */
  public static final int VALUE_ON = 17;
  
  /** 
   * ALTERNATE_HANDLING mode constants
   * @stable ICU 2.4
   */
  public static final int VALUE_SHIFTED = 20;
  /** @stable ICU 2.4 */
  public static final int VALUE_NON_IGNORABLE = 21;

  /** 
   * CASE_FIRST mode constants
   * @stable ICU 2.4
   */
  public static final int VALUE_LOWER_FIRST = 24;
  /** @stable ICU 2.4 */
  public static final int VALUE_UPPER_FIRST = 25;

  /** 
   * NORMALIZATION_MODE mode constants
   * @deprecated ICU 2.4. Users advised to use VALUE_ON instead.
   */
  public static final int VALUE_ON_WITHOUT_HANGUL = 28;

  /** 
   * Number of attribute value constants
   * @stable ICU 2.4
   */
  public static final int VALUE_ATTRIBUTE_VALUE_COUNT = 29;

  // Collation attribute constants -----------------------------------
  
  /** 
   * Attribute for direction of secondary weights
   * @stable ICU 2.4
   */
  public static final int FRENCH_COLLATION = 0;
  /** 
   * Attribute for handling variable elements
   * @stable ICU 2.4
   */
  public static final int ALTERNATE_HANDLING = 1;
  /** 
   * Who goes first, lower case or uppercase.
   * @stable ICU 2.4
   */
  public static final int CASE_FIRST = 2;
  /** 
   * Do we have an extra case level
   * @stable ICU 2.4
   */
  public static final int CASE_LEVEL = 3;
  /** 
   * Attribute for normalization
   * @stable ICU 2.4
   */
  public static final int NORMALIZATION_MODE = 4; 
  /** 
   * Attribute for strength 
   * @stable ICU 2.4
   */
  public static final int STRENGTH = 5;
  /** 
   * Attribute count
   * @stable ICU 2.4
   */
  public static final int ATTRIBUTE_COUNT = 6;
  
  // package methods --------------------------------------------------
  
  /**
  * Checks if argument is a valid collation strength
  * @param strength potential collation strength
  * @return true if strength is a valid collation strength, false otherwise
  */
  static boolean checkStrength(int strength)
  {
    if (strength < VALUE_PRIMARY || 
        (strength > VALUE_QUATERNARY && strength != VALUE_IDENTICAL))
      return false;
    return true;
  }
  
  /**
  * Checks if argument is a valid collation type
  * @param type collation type to be checked
  * @return true if type is a valid collation type, false otherwise
  */
  static boolean checkType(int type)
  {
    if (type < FRENCH_COLLATION || type > STRENGTH)
      return false;
    return true;
  }

  /**
  * Checks if argument is a valid normalization type
  * @param type normalization type to be checked
  * @return true if type is a valid normalization type, false otherwise
  */
  static boolean checkNormalization(int type)
  {
    if (type != VALUE_ON && type != VALUE_OFF 
        && type != VALUE_ON_WITHOUT_HANGUL) {
        return false;
    }
    return true;
  }
  
  /**
  * Checks if attribute type and corresponding attribute value is valid
  * @param type attribute type
  * @param value attribute value
  * @return true if the pair is valid, false otherwise
  */
  static boolean checkAttribute(int type, int value)
  {
    if (value == VALUE_DEFAULT) {
      return true;
    }
      
    switch (type)
    {
      case FRENCH_COLLATION :
                          if (value >= VALUE_OFF && value <= VALUE_ON)
                            return true;
                          break;
      case ALTERNATE_HANDLING :
                          if (value >= VALUE_SHIFTED && 
                              value <= VALUE_NON_IGNORABLE)
                            return true;
                          break;
      case CASE_FIRST :
                          if (value >= VALUE_LOWER_FIRST && 
                              value <= VALUE_UPPER_FIRST)
                            return true;
                          break;
      case CASE_LEVEL :
                          return (value == VALUE_ON || 
                                  value <= VALUE_OFF);
      case NORMALIZATION_MODE : 
                          return (value == VALUE_OFF || value == VALUE_ON ||
                                  value == VALUE_ON_WITHOUT_HANGUL);
      case STRENGTH :
                          checkStrength(value);
    }
    return false;
  }
}
