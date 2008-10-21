/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/ 

package com.ibm.icu4jni.charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.*;
import java.util.Iterator;
import com.ibm.icu4jni.converters.NativeConverter;

public final class CharsetProviderICU extends CharsetProvider{
    
    /**
     * Constructs a CharsetProviderICU object 
     * @stable ICU 2.4
     */
    public CharsetProviderICU(){
    }
    
    /**
     * Constructs a charset for the given charset name
     * @param charsetName charset name
     * @return charset objet for the given charset name
     * @stable ICU 2.4
     */
    public final Charset charsetForName(String charsetName) {
        // get the canonical name    
        String icuCanonicalName = NativeConverter.getICUCanonicalName(charsetName);      

        // create the converter object and return it
        if(icuCanonicalName==null || icuCanonicalName.length()==0){
            // this would make the Charset API to throw 
            // unsupported encoding exception
            return null;
        }
        
        // BEGIN android-added
        try{
            long cn = NativeConverter.openConverter(icuCanonicalName);
            NativeConverter.closeConverter(cn);
        }catch (RuntimeException re) {
            // unsupported encoding. let the charset api throw an 
            // UnsupportedEncodingException
            return null;
        }
        // END android-added
        
        return getCharset(icuCanonicalName);
    }
    private final Charset getCharset(String icuCanonicalName){
       String[] aliases = (String[])NativeConverter.getAliases(icuCanonicalName);    
       String canonicalName = NativeConverter.getJavaCanonicalName(icuCanonicalName);
       return (new CharsetICU(canonicalName,icuCanonicalName, aliases));  
    }
    /**
     * Adds an entry to the given map whose key is the charset's 
     * canonical name and whose value is the charset itself. 
     * @param map a map to receive charset objects and names
     * @stable ICU 2.4
     */
    public final void putCharsets(Map map) {
        // Get the available converter canonical names and aliases    
        String[] charsets = NativeConverter.getAvailable();        
        for(int i=0; i<charsets.length;i++){           
            // store the charsets and aliases in a Map    
            if (!map.containsKey(charsets[i])){
                map.put(charsets[i], charsetForName(charsets[i]));
            }
        }
    }
    /**
     * Class that implements the iterator for charsets
     * @stable ICU 2.4
     */
    protected final class CharsetIterator implements Iterator{
      private String[] names;
      private int currentIndex;
      protected CharsetIterator(String[] strs){
        names = strs;
        currentIndex=0;
      }
      public boolean hasNext(){
        return (currentIndex< names.length);
      }
      public Object next(){
        if(currentIndex<names.length){
              return charsetForName(names[currentIndex++]);
        }else{
              throw new NoSuchElementException();
        }
      }
      public void remove() {
            throw new UnsupportedOperationException();
      }
    }
      

    /**
     * Returns an iterator for the available charsets
     * @return Iterator the charset name iterator
     * @stable ICU 2.4
     */
    public final Iterator charsets(){
          String[] charsets = NativeConverter.getAvailable();
          Iterator iter = new CharsetIterator(charsets);
          return iter;
    }
     
}
