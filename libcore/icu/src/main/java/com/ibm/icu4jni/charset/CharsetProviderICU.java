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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public final class CharsetProviderICU extends CharsetProvider {
    public CharsetProviderICU() {
    }

    @Override
    public Charset charsetForName(String charsetName) {
        return NativeConverter.charsetForName(charsetName);
    }

    @Override
    public Iterator<Charset> charsets() {
        ArrayList<Charset> result = new ArrayList<Charset>();
        for (String charsetName : NativeConverter.getAvailable()) {
            result.add(charsetForName(charsetName));
        }
        return result.iterator();
    }

    /**
     * Implements Charset.availableCharsets.
     */
    public SortedMap<String, Charset> initAvailableCharsets() {
        SortedMap<String, Charset> result =
                new TreeMap<String, Charset>(String.CASE_INSENSITIVE_ORDER);
        for (String charset : NativeConverter.getAvailable()) {
            if (!result.containsKey(charset)) {
                result.put(charset, charsetForName(charset));
            }
        }
        return result;
    }
}
