/**
*******************************************************************************
* Copyright (C) 1996-2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
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

import com.ibm.icu4jni.common.ErrorCode;
import com.ibm.icu4jni.converters.NativeConverter;


import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.ByteBuffer;

public final class CharsetDecoderICU extends CharsetDecoder{ 
        

    private static final int INPUT_OFFSET   = 0,
                             OUTPUT_OFFSET  = 1,
                             INVALID_BYTES  = 2,
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

    
    private  byte[] input = null;
    private  char[] output= null;
    
    // These instance variables are
    // always assigned in the methods
    // before being used. This class
    // inhrently multithread unsafe
    // so we dont have to worry about
    // synchronization
    private int inEnd;
    private int outEnd;
    private int ec;
    private int onUnmappableInput = NativeConverter.STOP_CALLBACK;;
    private int onMalformedInput = NativeConverter.STOP_CALLBACK;;
    private int savedInputHeldLen;
    
    /** 
     * Constructs a new decoder for the given charset
     * @param cs for which the decoder is created
     * @param cHandle the address of ICU converter
     * @exception RuntimeException
     * @stable ICU 2.4
     */
    public CharsetDecoderICU(Charset cs,long cHandle){
         super(cs,
               NativeConverter.getAveCharsPerByte(cHandle),
               NativeConverter.getMaxCharsPerByte(cHandle)
               );
                       
         char[] sub = replacement().toCharArray();
         ec = NativeConverter.setCallbackDecode(cHandle,
                                                onMalformedInput,
                                                onUnmappableInput,
                                                sub, sub.length);
         if(ErrorCode.isFailure(ec)){
            throw ErrorCode.getException(ec);
         }
         // store the converter handle
         converterHandle=cHandle;

    }
    
    /**
     * Sets this decoders replacement string. Substitutes the string in input if an
     * umappable or illegal sequence is encountered
     * @param newReplacement to replace the error bytes with
     * @stable ICU 2.4
     */    
    protected void implReplaceWith(String newReplacement) {
        if(converterHandle > 0){
            if( newReplacement.length() > NativeConverter.getMaxBytesPerChar(converterHandle)) {
                    throw new IllegalArgumentException();
            }           
            ec =NativeConverter.setSubstitutionChars(converterHandle,
                                                    newReplacement.toCharArray(),
                                                    newReplacement.length()
                                                    );
            if(ErrorCode.isFailure(ec)){
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
    protected final void implOnMalformedInput(CodingErrorAction newAction) {
        if(newAction.equals(CodingErrorAction.IGNORE)){
            onMalformedInput = NativeConverter.SKIP_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPLACE)){
            onMalformedInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPORT)){
            onMalformedInput = NativeConverter.STOP_CALLBACK;
        }
        char[] sub = replacement().toCharArray();
        //System.out.println(" setting callbacks mfi " + onMalformedInput +" umi " + onUnmappableInput);
        ec = NativeConverter.setCallbackDecode(converterHandle, onMalformedInput, onUnmappableInput, sub, sub.length);
        if(ErrorCode.isFailure(ec)){
            throw ErrorCode.getException(ec);
        } 
    }
    
    /**
     * Sets the action to be taken if an illegal sequence is encountered
     * @param newAction action to be taken
     * @exception IllegalArgumentException
     * @stable ICU 2.4
     */
    protected final void implOnUnmappableCharacter(CodingErrorAction newAction) {
        if(newAction.equals(CodingErrorAction.IGNORE)){
            onUnmappableInput = NativeConverter.SKIP_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPLACE)){
            onUnmappableInput = NativeConverter.SUBSTITUTE_CALLBACK;
        }else if(newAction.equals(CodingErrorAction.REPORT)){
            onUnmappableInput = NativeConverter.STOP_CALLBACK;
        }
        char[] sub = replacement().toCharArray();
        ec = NativeConverter.setCallbackDecode(converterHandle,onMalformedInput, onUnmappableInput, sub, sub.length);
        if(ErrorCode.isFailure(ec)){
            throw ErrorCode.getException(ec);
        } 
    }
    
    /**
     * Flushes any characters saved in the converter's internal buffer and
     * resets the converter.
     * @param out action to be taken
     * @return result of flushing action and completes the decoding all input. 
     *         Returns CoderResult.UNDERFLOW if the action succeeds.
     * @stable ICU 2.4
     */
    protected final CoderResult implFlush(CharBuffer out) {
       try{
           
           data[OUTPUT_OFFSET] = getArray(out);

            ec=NativeConverter.flushByteToChar(
                                            converterHandle,  /* Handle to ICU Converter */
                                            output,           /* input array of chars */
                                            outEnd,           /* input index+1 to be written */
                                            data              /* contains data, inOff,outOff */
                                            );
                                      
            
            /* If we don't have room for the output, throw an exception*/
            if (ErrorCode.isFailure(ec)) {
                if (ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR) {
                    return CoderResult.OVERFLOW;
                }else if (ec == ErrorCode.U_TRUNCATED_CHAR_FOUND ) {//CSDL: add this truncated character error handling
                    if(data[INPUT_OFFSET]>0){
                        return CoderResult.malformedForLength(data[INPUT_OFFSET]);
                    }
                }else {
                    ErrorCode.getException(ec);
                }
            }
            return CoderResult.UNDERFLOW;
       }finally{
            /* save the flushed data */
            setPosition(out);
            implReset();
       }
    }
    
    /**
     * Resets the to Unicode mode of converter
     * @stable ICU 2.4
     */
    protected void implReset() {
        NativeConverter.resetByteToChar(converterHandle);
        data[INPUT_OFFSET] = 0;
        data[OUTPUT_OFFSET] = 0;
        data[INVALID_BYTES] = 0;
        data[INPUT_HELD] = 0;
        savedInputHeldLen = 0;
        output = null;
        input = null;
    }
      
    /**
     * Decodes one or more bytes. The default behaviour of the converter
     * is stop and report if an error in input stream is encountered. 
     * To set different behaviour use @see CharsetDecoder.onMalformedInput()
     * This  method allows a buffer by buffer conversion of a data stream.  
     * The state of the conversion is saved between calls to convert.  
     * Among other things, this means multibyte input sequences can be 
     * split between calls. If a call to convert results in an Error, the 
     * conversion may be continued by calling convert again with suitably 
     * modified parameters.All conversions should be finished with a call to 
     * the flush method.
     * @param in buffer to decode
     * @param out buffer to populate with decoded result
     * @return result of decoding action. Returns CoderResult.UNDERFLOW if the decoding
     *         action succeeds or more input is needed for completing the decoding action.
     * @stable ICU 2.4
     */
    protected CoderResult decodeLoop(ByteBuffer in,CharBuffer out){

        if(!in.hasRemaining()){
            return CoderResult.UNDERFLOW;
        }

        data[INPUT_OFFSET] = getArray(in);
        data[OUTPUT_OFFSET]= getArray(out);
        data[INPUT_HELD] = 0;
        
        try{
            /* do the conversion */
            ec=NativeConverter.decode(
                                converterHandle,  /* Handle to ICU Converter */
                                input,            /* input array of bytes */
                                inEnd,            /* last index+1 to be converted */
                                output,           /* input array of chars */
                                outEnd,           /* input index+1 to be written */
                                data,             /* contains data, inOff,outOff */
                                false             /* donot flush the data */
                                );
            

            /* return an error*/
            if(ec == ErrorCode.U_BUFFER_OVERFLOW_ERROR){
                return CoderResult.OVERFLOW;
            }else if(ec==ErrorCode.U_INVALID_CHAR_FOUND){
                return CoderResult.unmappableForLength(data[INVALID_BYTES]);
            }else if(ec==ErrorCode.U_ILLEGAL_CHAR_FOUND){
                return CoderResult.malformedForLength(data[INVALID_BYTES]);
            }
            /* decoding action succeded */
            return CoderResult.UNDERFLOW;
        }finally{
            setPosition(in);
            setPosition(out);
        }
    }
    
    /**
     * Releases the system resources by cleanly closing ICU converter opened
     * @stable ICU 2.4
     */
    protected void finalize()throws Throwable{
        NativeConverter.closeConverter(converterHandle);
        super.finalize();
        converterHandle = 0;
    }
    
    //------------------------------------------
    // private utility methods
    //------------------------------------------

    private final int getArray(CharBuffer out){
        if(out.hasArray()){
            output = out.array();
            outEnd = out.limit();
            return out.position();
        }else{
            outEnd = out.remaining();
            if(output==null || (outEnd > output.length)){
                output = new char[outEnd];
            }
            //since the new 
            // buffer start position 
            // is 0
            return 0;
        }
        
    }
    private  final int getArray(ByteBuffer in){
        if(in.hasArray()){
            input = in.array();
            inEnd = in.limit();
            return in.position()+savedInputHeldLen;/*exclude the number fo bytes held in previous conversion*/
        }else{
            inEnd = in.remaining();
            if(input==null|| (inEnd > input.length)){ 
                input = new byte[inEnd];
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
    private final void setPosition(CharBuffer out){
        if(out.hasArray()){
            out.position(out.position() + data[OUTPUT_OFFSET]);
        }else{
            out.put(output,0,data[OUTPUT_OFFSET]);
        }
    }
    private final void setPosition(ByteBuffer in){

        // ok was there input held in the previous invocation of decodeLoop 
        // that resulted in output in this invocation?
        if(data[OUTPUT_OFFSET]>0 && savedInputHeldLen >0){
            int len = in.position() + data[INPUT_OFFSET] + savedInputHeldLen;
            in.position(len);   
            savedInputHeldLen = data[INPUT_HELD];
        }else{
            in.position(in.position() + data[INPUT_OFFSET] + savedInputHeldLen);
            savedInputHeldLen = data[INPUT_HELD];
            in.position(in.position() - savedInputHeldLen);
        }       
    }
}
