/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.icu4jni.text;
 
public final class NativeBreakIterator
{  
    public NativeBreakIterator() {
        
    }

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

    static native int getCharacterInstanceImpl(String locale);
    
    static native int getWordInstanceImpl(String locale);
    
    static native int getLineInstanceImpl(String locale);
    
    static native int getSentenceInstanceImpl(String locale);

    static native void closeBreakIteratorImpl(int biaddress);
    
    static native void setTextImpl(int biaddress, String text);
    
    static native int cloneImpl(int biaddress);
    
    static native int precedingImpl(int biaddress, int offset);

    static native boolean isBoundaryImpl(int biaddress, int offset);

    static native int nextImpl(int biaddress, int n);

    static native int previousImpl(int biaddress);

    static native int currentImpl(int biaddress);

    static native int firstImpl(int biaddress);

    static native int followingImpl(int biaddress, int offset);

    static native int lastImpl(int biaddress);
}
