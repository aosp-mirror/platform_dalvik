/**
*******************************************************************************
* Copyright (C) 1996-2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

package com.ibm.icu4jni.charset;

import java.nio.charset.Charset;

public final class NativeConverter {
    /**
     * Converts an array of bytes containing characters in an external
     * encoding into an array of Unicode characters.  This  method allows
     * buffer-by-buffer conversion of a data stream.  The state of the
     * conversion is saved between calls.  Among other things,
     * this means multibyte input sequences can be split between calls.
     * If a call to results in an error, the conversion may be
     * continued by calling this method again with suitably modified parameters.
     * All conversions should be finished with a call to the flush method.
     *
     * @param converterHandle Address of converter object created by C code
     * @param input byte array containing text to be converted.
     * @param inEnd stop conversion at this offset in input array (exclusive).
     * @param output character array to receive conversion result.
     * @param outEnd stop writing to output array at this offset (exclusive).
     * @param data integer array containing the following data
     *        data[0] = inputOffset
     *        data[1] = outputOffset
     * @return int error code returned by ICU
     * @internal ICU 2.4
     */
    public static native int decode(long converterHandle, byte[] input, int inEnd,
            char[] output, int outEnd, int[] data, boolean flush);

    /**
     * Converts an array of Unicode chars to an array of bytes in an external encoding.
     * This  method allows a buffer by buffer conversion of a data stream.  The state of the
     * conversion is saved between calls to convert.  Among other things,
     * this means multibyte input sequences can be split between calls.
     * If a call results in an error, the conversion may be
     * continued by calling this method again with suitably modified parameters.
     * All conversions should be finished with a call to the flush method.
     *
     * @param converterHandle Address of converter object created by C code
     * @param input char array containing text to be converted.
     * @param inEnd stop conversion at this offset in input array (exclusive).
     * @param output byte array to receive conversion result.
     * @param outEnd stop writing to output array at this offset (exclusive).
     * @param data integer array containing the following data
     *        data[0] = inputOffset
     *        data[1] = outputOffset
     * @return int error code returned by ICU
     * @internal ICU 2.4
     */
    public static native int encode(long converterHandle, char[] input, int inEnd,
            byte[] output, int outEnd, int[] data, boolean flush);

    /**
     * Writes any remaining output to the output buffer and resets the
     * converter to its initial state.
     *
     * @param converterHandle Address of converter object created by C code
     * @param output byte array to receive flushed output.
     * @param outEnd stop writing to output array at this offset (exclusive).
     * @return int error code returned by ICU
     * @param data integer array containing the following data
     *        data[0] = inputOffset
     *        data[1] = outputOffset
     * @internal ICU 2.4
     */
    public static native int flushCharToByte(long converterHandle, byte[] output, int outEnd, int[] data);

    /**
     * Writes any remaining output to the output buffer and resets the
     * converter to its initial state.
     *
     * @param converterHandle Address of converter object created by the native code
     * @param output char array to receive flushed output.
     * @param outEnd stop writing to output array at this offset (exclusive).
     * @return int error code returned by ICU
     * @param data integer array containing the following data
     *        data[0] = inputOffset
     *        data[1] = outputOffset
     * @internal ICU 2.4
     */
    public static native int flushByteToChar(long converterHandle, char[] output,  int outEnd, int[] data);

    public static native long openConverter(String encoding);
    public static native void closeConverter(long converterHandle);

    public static native void resetByteToChar(long  converterHandle);
    public static native void resetCharToByte(long  converterHandle);

    public static native int setSubstitutionChars(long converterHandle, char[] subChars,int length);
    public static native int setSubstitutionBytes(long converterHandle, byte[] subChars,int length);
    public static native byte[] getSubstitutionBytes(long converterHandle);

    public static native int getMaxBytesPerChar(long converterHandle);
    public static native int getMinBytesPerChar(long converterHandle);
    public static native float getAveBytesPerChar(long converterHandle);
    public static native int getMaxCharsPerByte(long converterHandle);
    public static native float getAveCharsPerByte(long converterHandle);

    public static native boolean contains(long converterHandle1, long converterHandle2);

    public static native boolean canEncode(long converterHandle, int codeUnit);

    public static native String[] getAvailableCharsetNames();
    public static native Charset charsetForName(String charsetName);

    public static final int STOP_CALLBACK = 0;//CodingErrorAction.REPORT
    public static final int SKIP_CALLBACK = 1;//CodingErrorAction.IGNORE
    public static final int SUBSTITUTE_CALLBACK = 2;//CodingErrorAction.REPLACE
    public static native int setCallbackDecode(long converterHandle, int onMalformedInput, int onUnmappableInput, char[] subChars, int length);
    public static native int setCallbackEncode(long converterHandle, int onMalformedInput, int onUnmappableInput, byte[] subBytes, int length);
}
