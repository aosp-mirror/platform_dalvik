/**
*******************************************************************************
* Copyright (C) 1996-2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                  *
*******************************************************************************
*
*******************************************************************************
*/
/** 
 * A JNI interface for ICU converters.
 *
 * 
 * @author Ram Viswanadha, IBM
 */
package com.ibm.icu4jni.charset;  

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import com.ibm.icu4jni.common.ErrorCode;
import com.ibm.icu4jni.converters.NativeConverter;

public final class CharsetEncoderICU extends CharsetEncoder {

    private static final int INPUT_OFFSET = 0,
                             OUTPUT_OFFSET = 1,
                             INVALID_CHARS  = 2,
                             INPUT_HELD     = 3,
                             LIMIT          = 4;
    /* data is 3 element array where
     * data[INPUT_OFFSET]   = on input contains the start of input and on output the number of input chars consumed
     * data[OUTPUT_OFFSET]  = on input contains the start of output and on output the number of output bytes written
     * data[INVALID_CHARS]  = number of invalid chars
     * data[INPUT_HELD]     = number of input chars held in the converter's state
     */
    private int[] data = new int[LIMIT];
    /* handle to the ICU converter that is opened */
    private long converterHandle=0;

    private char[] input = null;
    private byte[] output = null;

    // These instance variables are
    // always assigned in the methods
    // before being used. This class
    // inhrently multithread unsafe
    // so we dont have to worry about
    // synchronization
    private int inEnd;
    private int outEnd;
    private int ec;
    private int savedInputHeldLen;
    private int onUnmappableInput = NativeConverter.STOP_CALLBACK;;
    private int onMalformedInput = NativeConverter.STOP_CALLBACK;;

    /** 
     * Construcs a new encoder for the given charset
     * @param cs for which the decoder is created
     * @param cHandle the address of ICU converter
     * @param replacement the substitution bytes
     * @stable ICU 2.4
     */
    public CharsetEncoderICU(Charset cs, long cHandle, byte[] replacement) {
        super(
            cs,
            (float) NativeConverter.getAveBytesPerChar(cHandle),
            (float) NativeConverter.getMaxBytesPerChar(cHandle),
            replacement);
        byte[] sub = replacement();
        // The default callback action on unmappable input 
        // or malformed input is to ignore so we set ICU converter
        // callback to stop and report the error
        ec = NativeConverter.setCallbackEncode( cHandle,
                                                onMalformedInput,
                                                onUnmappableInput,
                                                sub, sub.length);
        converterHandle = cHandle;
        if (ErrorCode.isFailure(ec)) {
            throw ErrorCode.getException(ec);
        }
    }

    /**
     * Sets this encoders replacement string. Substitutes the string in output if an
     * umappable or illegal sequence is encountered
     * @param newReplacement to replace the error chars with
     * @stable ICU 2.4
     */
    protected void implReplaceWith(byte[] newReplacement) {
        if (converterHandle != 0) {
            if (newReplacement.length
                > NativeConverter.getMaxBytesPerChar(converterHandle)) {
                throw new IllegalArgumentException("Number of replacement Bytes are greater than max bytes per char");
            }
            ec = NativeConverter.setSubstitutionBytes(converterHandle,
                                                      newReplacement,
                                                      newReplacement.length);
            if (ErrorCode.isFailure(ec)) {
                throw ErrorCode.getException(ec);
            }
        }
    }

    /**
     * Sets the action to be taken if an illegal sequence is encountered
     * @param newAction action to be taken
     * @exception IllegalArgumentException
     * @stable ICU 2.4
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) {
        onMalformedInput = NativeConverter.STOP_CALLBACK;

        if (newAction.equals(CodingErrorAction.IGNORE)) {
            onMalformedInput = NativeConverter.SKIP_CALLBACK;
        } else if (newAction.equals(CodingErrorAction.REPLACE)) {
            onMalformedInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }
        byte[] sub = replacement();
        ec = NativeConverter.setCallbackEncode(converterHandle, onMalformedInput, onUnmappableInput, sub, sub.length);
        if (ErrorCode.isFailure(ec)) {
            throw ErrorCode.getException(ec);
        }

    }

    /**
     * Sets the action to be taken if an illegal sequence is encountered
     * @param newAction action to be taken
     * @exception IllegalArgumentException
     * @stable ICU 2.4
     */
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        onUnmappableInput = NativeConverter.STOP_CALLBACK;

        if (newAction.equals(CodingErrorAction.IGNORE)) {
            onUnmappableInput = NativeConverter.SKIP_CALLBACK;
        } else if (newAction.equals(CodingErrorAction.REPLACE)) {
            onUnmappableInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }
        byte[] sub = replacement();
        ec = NativeConverter.setCallbackEncode(converterHandle, onMalformedInput, onUnmappableInput, sub, sub.length);
        if (ErrorCode.isFailure(ec)) {
            throw ErrorCode.getException(ec);
        }
    }

    /**
     * Flushes any characters saved in the converter's internal buffer and
     * resets the converter.
     * @param out action to be taken
     * @return result of flushing action and completes the decoding all input. 
     *       Returns CoderResult.UNDERFLOW if the action succeeds.
     * @stable ICU 2.4
     */
    protected CoderResult implFlush(ByteBuffer out) {
        try {
            data[OUTPUT_OFFSET] = getArray(out);
            ec = NativeConverter.flushCharToByte(converterHandle,/* Handle to ICU Converter */
                                                 output, /* output array of chars */
                                                 outEnd, /* output index+1 to be written */
                                                 data /* contains data, inOff,outOff */
                                                );

            /* If we don't have room for the output, throw an exception*/
            if (ErrorCode.isFailure(ec)) {
                if (ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR) {
                    return CoderResult.OVERFLOW;
                }else if (ec == ErrorCode.U_TRUNCATED_CHAR_FOUND) {//CSDL: add this truncated character error handling
                    if(data[INPUT_OFFSET]>0){
                        return CoderResult.malformedForLength(data[INPUT_OFFSET]);
                    }
                }else {
                    ErrorCode.getException(ec);
                }
            }
            return CoderResult.UNDERFLOW;
        } finally {
            setPosition(out);
            implReset();
        }
    }

    /**
     * Resets the from Unicode mode of converter
     * @stable ICU 2.4
     */
    protected void implReset() {
        NativeConverter.resetCharToByte(converterHandle);
        data[INPUT_OFFSET] = 0;
        data[OUTPUT_OFFSET] = 0;
        data[INVALID_CHARS] = 0;
        data[INPUT_HELD] = 0;
        savedInputHeldLen = 0;
    }

    /**
     * Encodes one or more chars. The default behaviour of the
     * converter is stop and report if an error in input stream is encountered.
     * To set different behaviour use @see CharsetEncoder.onMalformedInput()
     * @param in buffer to decode
     * @param out buffer to populate with decoded result
     * @return result of decoding action. Returns CoderResult.UNDERFLOW if the decoding
     *       action succeeds or more input is needed for completing the decoding action.
     * @stable ICU 2.4
     */
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {

        if (!in.hasRemaining()) {
            return CoderResult.UNDERFLOW;
        }

        data[INPUT_OFFSET] = getArray(in);
        data[OUTPUT_OFFSET]= getArray(out);
        data[INPUT_HELD] = 0;
        // BEGIN android-added
        data[INVALID_CHARS] = 0; // Make sure we don't see earlier errors. 
        // END android added
        
        try {
            /* do the conversion */
            ec = NativeConverter.encode(converterHandle,/* Handle to ICU Converter */
                                        input, /* input array of bytes */
                                        inEnd, /* last index+1 to be converted */
                                        output, /* output array of chars */
                                        outEnd, /* output index+1 to be written */
                                        data, /* contains data, inOff,outOff */
                                        false /* donot flush the data */
                                        );
            if (ErrorCode.isFailure(ec)) {
                /* If we don't have room for the output return error */
                if (ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR) {
                    return CoderResult.OVERFLOW;
                } else if (ec == ErrorCode.U_INVALID_CHAR_FOUND) {
                    return CoderResult.unmappableForLength(data[INVALID_CHARS]);
                } else if (ec == ErrorCode.U_ILLEGAL_CHAR_FOUND) {
                    // in.position(in.position() - 1);
                    return CoderResult.malformedForLength(data[INVALID_CHARS]);
                }
            }
            return CoderResult.UNDERFLOW;
        } finally {
            /* save state */
            setPosition(in);
            setPosition(out);
        }
    }

    /**
     * Ascertains if a given Unicode character can 
     * be converted to the target encoding
     *
     * @param  c the character to be converted
     * @return true if a character can be converted
     * @stable ICU 2.4
     * 
     */
    public boolean canEncode(char c) {
        return canEncode((int) c);
    }

    /**
     * Ascertains if a given Unicode code point (32bit value for handling surrogates)
     * can be converted to the target encoding. If the caller wants to test if a
     * surrogate pair can be converted to target encoding then the
     * responsibility of assembling the int value lies with the caller.
     * For assembling a code point the caller can use UTF16 class of ICU4J and do something like:
     * <pre>
     * while(i<mySource.length){
     *      if(UTF16.isLeadSurrogate(mySource[i])&& i+1< mySource.length){
     *          if(UTF16.isTrailSurrogate(mySource[i+1])){
     *              int temp = UTF16.charAt(mySource,i,i+1,0);
     *              if(!((CharsetEncoderICU) myConv).canEncode(temp)){
     *          passed=false;
     *              }
     *              i++;
     *              i++;
     *          }
     *     }
     * }
     * </pre>
     * or
     * <pre>
     * String src = new String(mySource);
     * int i,codepoint;
     * boolean passed = false;
     * while(i<src.length()){
     *    codepoint = UTF16.charAt(src,i);
     *    i+= (codepoint>0xfff)? 2:1;
     *    if(!(CharsetEncoderICU) myConv).canEncode(codepoint)){
     *        passed = false;
     *    }
     * }
     * </pre>
     *
     * @param codepoint Unicode code point as int value
     * @return true if a character can be converted
     * @obsolete ICU 2.4
     * @deprecated ICU 3.4
     */
    public boolean canEncode(int codepoint) {
        return NativeConverter.canEncode(converterHandle, codepoint);
    }

    /**
     * Releases the system resources by cleanly closing ICU converter opened
     * @exception Throwable exception thrown by super class' finalize method
     * @stable ICU 2.4
     */
    protected void finalize() throws Throwable {
        NativeConverter.closeConverter(converterHandle);
        super.finalize();
        converterHandle=0;
    }

    //------------------------------------------
    // private utility methods
    //------------------------------------------
    private final int getArray(ByteBuffer out) {
        if(out.hasArray()){
            output = out.array();
            outEnd = out.limit();
            return out.position();
        }else{
            outEnd = out.remaining();
            if(output==null || (outEnd > output.length)){
                output = new byte[outEnd];
            }
            //since the new 
            // buffer start position 
            // is 0
            return 0;
        }
    }

    private final int getArray(CharBuffer in) {
        if(in.hasArray()){
            input = in.array();
            inEnd = in.limit();
            return in.position()+savedInputHeldLen;/*exclude the number fo bytes held in previous conversion*/
        }else{
            inEnd = in.remaining();
            if(input==null|| (inEnd > input.length)){ 
                input = new char[inEnd];
            }
            // save the current position
            int pos = in.position();
            in.get(input,0,inEnd);
            // reset the position
            in.position(pos);
            // the start position  
            // of the new buffer  
            // is whatever is savedInputLen
            return savedInputHeldLen;
        }

    }
    private final void setPosition(ByteBuffer out) {
        
        if (out.hasArray()) {
            // in getArray method we accessed the 
            // array backing the buffer directly and wrote to 
            // it, so just just set the position and return.
            // This is done to avoid the creation of temp array.
            out.position(out.position() + data[OUTPUT_OFFSET] );
        } else {
            out.put(output, 0, data[OUTPUT_OFFSET]);
        }
    }
    private final void setPosition(CharBuffer in){

// BEGIN android-removed
//        // was there input held in the previous invocation of encodeLoop 
//        // that resulted in output in this invocation?
//        if(data[OUTPUT_OFFSET]>0 && savedInputHeldLen>0){
//            int len = in.position() + data[INPUT_OFFSET] + savedInputHeldLen;
//            in.position(len);   
//            savedInputHeldLen = data[INPUT_HELD];
//        }else{
//            in.position(in.position() + data[INPUT_OFFSET] + savedInputHeldLen);
//            savedInputHeldLen = data[INPUT_HELD];
//            in.position(in.position() - savedInputHeldLen);
//        }     
// END android-removed

// BEGIN android-added
        // Slightly rewired original code to make it cleaner. Also
        // added a fix for the problem where input charatcers got
        // lost when invalid characters were encountered. Not sure
        // what happens when data[INVALID_CHARS] is > 1, though,
        // since we never saw that happening.
        int len = in.position() + data[INPUT_OFFSET] + savedInputHeldLen;
        len -= data[INVALID_CHARS]; // Otherwise position becomes wrong.
        in.position(len);   
        savedInputHeldLen = data[INPUT_HELD];
        // was there input held in the previous invocation of encodeLoop 
        // that resulted in output in this invocation?
        if(!(data[OUTPUT_OFFSET]>0 && savedInputHeldLen>0)){
            in.position(in.position() - savedInputHeldLen);
        }     
// END android-added
    }
}
