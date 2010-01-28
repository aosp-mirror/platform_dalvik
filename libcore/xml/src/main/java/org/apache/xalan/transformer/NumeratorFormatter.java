/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: NumeratorFormatter.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.util.Locale;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;

/**
 * Converts enumerated numbers into strings, using the XSL conversion attributes.
 * Having this in a class helps avoid being forced to extract the attributes repeatedly.
 * @xsl.usage internal
 */
class NumeratorFormatter
{

  /** The owning xsl:number element.          */
  protected Element m_xslNumberElement;

  /** An instance of a Tokenizer          */
  NumberFormatStringTokenizer m_formatTokenizer;

  /** Locale we need to format in          */
  Locale m_locale;

  /** An instance of a NumberFormat         */
  java.text.NumberFormat m_formatter;

  /** An instance of a transformer          */
  TransformerImpl m_processor;

  /**
   * Table to help in converting decimals to roman numerals.
   * @see org.apache.xalan.transformer.DecimalToRoman
   */
  private final static DecimalToRoman m_romanConvertTable[] = {
    new DecimalToRoman(1000, "M", 900, "CM"),
    new DecimalToRoman(500, "D", 400, "CD"),
    new DecimalToRoman(100L, "C", 90L, "XC"),
    new DecimalToRoman(50L, "L", 40L, "XL"),
    new DecimalToRoman(10L, "X", 9L, "IX"),
    new DecimalToRoman(5L, "V", 4L, "IV"),
    new DecimalToRoman(1L, "I", 1L, "I") };

  /**
   * Chars for converting integers into alpha counts.
   * @see TransformerImpl#int2alphaCount
   */
  private final static char[] m_alphaCountTable = { 'Z',  // z for zero
                                                    'A', 'B', 'C', 'D', 'E',
                                                    'F', 'G', 'H', 'I', 'J',
                                                    'K', 'L', 'M', 'N', 'O',
                                                    'P', 'Q', 'R', 'S', 'T',
                                                    'U', 'V', 'W', 'X', 'Y' };

  /**
   * Construct a NumeratorFormatter using an element
   * that contains XSL number conversion attributes -
   * format, letter-value, xml:lang, digit-group-sep,
   * n-digits-per-group, and sequence-src.
   *
   * @param xslNumberElement The given xsl:number element
   * @param processor a non-null transformer instance
   */
  NumeratorFormatter(Element xslNumberElement, TransformerImpl processor)
  {
    m_xslNumberElement = xslNumberElement;
    m_processor = processor;
  }  // end NumeratorFormatter(Element) constructor

  /**
   * Convert a long integer into alphabetic counting, in other words
   * count using the sequence A B C ... Z AA AB AC.... etc.
   * 
   * @param val Value to convert -- must be greater than zero.
   * @param table a table containing one character for each digit in the radix
   * @return String representing alpha count of number.
   * @see org.apache.xalan.transformer.DecimalToRoman
   *
   * Note that the radix of the conversion is inferred from the size
   * of the table.
   */
  protected String int2alphaCount(int val, char[] table)
  {

    int radix = table.length;

    // Create a buffer to hold the result
    // TODO:  size of the table can be detereined by computing
    // logs of the radix.  For now, we fake it.
    char buf[] = new char[100];

    // next character to set in the buffer
    int charPos = buf.length - 1;  // work backward through buf[]

    // index in table of the last character that we stored
    int lookupIndex = 1;  // start off with anything other than zero to make correction work

    //                                          Correction number
    //
    //  Correction can take on exactly two values:
    //
    //          0       if the next character is to be emitted is usual
    //
    //      radix - 1
    //                  if the next char to be emitted should be one less than
    //                  you would expect
    //
    // For example, consider radix 10, where 1="A" and 10="J"
    //
    // In this scheme, we count: A, B, C ...   H, I, J (not A0 and certainly
    // not AJ), A1
    //
    // So, how do we keep from emitting AJ for 10?  After correctly emitting the
    // J, lookupIndex is zero.  We now compute a correction number of 9 (radix-1).
    // In the following line, we'll compute (val+correction) % radix, which is,
    // (val+9)/10.  By this time, val is 1, so we compute (1+9) % 10, which
    // is 10 % 10 or zero.  So, we'll prepare to emit "JJ", but then we'll
    // later suppress the leading J as representing zero (in the mod system,
    // it can represent either 10 or zero).  In summary, the correction value of
    // "radix-1" acts like "-1" when run through the mod operator, but with the
    // desireable characteristic that it never produces a negative number.
    int correction = 0;

    // TODO:  throw error on out of range input
    do
    {

      // most of the correction calculation is explained above,  the reason for the
      // term after the "|| " is that it correctly propagates carries across
      // multiple columns.
      correction =
        ((lookupIndex == 0) || (correction != 0 && lookupIndex == radix - 1))
        ? (radix - 1) : 0;

      // index in "table" of the next char to emit
      lookupIndex = (val + correction) % radix;

      // shift input by one "column"
      val = (val / radix);

      // if the next value we'd put out would be a leading zero, we're done.
      if (lookupIndex == 0 && val == 0)
        break;

      // put out the next character of output
      buf[charPos--] = table[lookupIndex];
    }
    while (val > 0);

    return new String(buf, charPos + 1, (buf.length - charPos - 1));
  }

  /**
   * Convert a long integer into roman numerals.
   * @param val Value to convert.
   * @param prefixesAreOK true_ to enable prefix notation (e.g. 4 = "IV"),
   * false_ to disable prefix notation (e.g. 4 = "IIII").
   * @return Roman numeral string.
   * @see DecimalToRoman
   * @see m_romanConvertTable
   */
  String long2roman(long val, boolean prefixesAreOK)
  {

    if (val <= 0)
    {
      return "#E(" + val + ")";
    }

    String roman = "";
    int place = 0;

    if (val <= 3999L)
    {
      do
      {
        while (val >= m_romanConvertTable[place].m_postValue)
        {
          roman += m_romanConvertTable[place].m_postLetter;
          val -= m_romanConvertTable[place].m_postValue;
        }

        if (prefixesAreOK)
        {
          if (val >= m_romanConvertTable[place].m_preValue)
          {
            roman += m_romanConvertTable[place].m_preLetter;
            val -= m_romanConvertTable[place].m_preValue;
          }
        }

        place++;
      }
      while (val > 0);
    }
    else
    {
      roman = "#error";
    }

    return roman;
  }  // end long2roman

  /**
   * This class returns tokens using non-alphanumberic
   * characters as delimiters.
   */
  class NumberFormatStringTokenizer
  {

    /** Field holding the current position in the string      */
    private int currentPosition;

    /** The total length of the string          */
    private int maxPosition;

    /** The string to tokenize          */
    private String str;

    /**
     * Construct a NumberFormatStringTokenizer.
     *
     * @param str The string to tokenize
     */
    NumberFormatStringTokenizer(String str)
    {
      this.str = str;
      maxPosition = str.length();
    }
    
    /**
     * Reset tokenizer so that nextToken() starts from the beginning. 
     *
     */
    void reset()
    {
      currentPosition = 0;
    }

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return     the next token from this string tokenizer.
     * @throws  NoSuchElementException  if there are no more tokens in this
     *               tokenizer's string.
     */
    String nextToken()
    {

      if (currentPosition >= maxPosition)
      {
        throw new NoSuchElementException();
      }

      int start = currentPosition;

      while ((currentPosition < maxPosition)
             && Character.isLetterOrDigit(str.charAt(currentPosition)))
      {
        currentPosition++;
      }

      if ((start == currentPosition)
              && (!Character.isLetterOrDigit(str.charAt(currentPosition))))
      {
        currentPosition++;
      }

      return str.substring(start, currentPosition);
    }

    /**
     * Tells if <code>nextToken</code> will throw an exception      * if it is called.
     *
     * @return true if <code>nextToken</code> can be called      * without throwing an exception.
     */
    boolean hasMoreTokens()
    {
      return (currentPosition >= maxPosition) ? false : true;
    }

    /**
     * Calculates the number of times that this tokenizer's
     * <code>nextToken</code> method can be called before it generates an
     * exception.
     *
     * @return  the number of tokens remaining in the string using the current
     *          delimiter set.
     * @see     java.util.StringTokenizer#nextToken()
     */
    int countTokens()
    {

      int count = 0;
      int currpos = currentPosition;

      while (currpos < maxPosition)
      {
        int start = currpos;

        while ((currpos < maxPosition)
               && Character.isLetterOrDigit(str.charAt(currpos)))
        {
          currpos++;
        }

        if ((start == currpos)
                && (Character.isLetterOrDigit(str.charAt(currpos)) == false))
        {
          currpos++;
        }

        count++;
      }

      return count;
    }
  }  // end NumberFormatStringTokenizer
}
