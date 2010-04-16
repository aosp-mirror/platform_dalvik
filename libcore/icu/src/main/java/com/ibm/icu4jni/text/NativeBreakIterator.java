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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

public final class NativeBreakIterator implements Cloneable {
    // Acceptable values for the 'type' field.
    private static final int BI_CHAR_INSTANCE = 1;
    private static final int BI_WORD_INSTANCE = 2;
    private static final int BI_LINE_INSTANCE = 3;
    private static final int BI_SENT_INSTANCE = 4;

    private final int addr;
    private final int type;
    private CharacterIterator charIter;

    private NativeBreakIterator(int iterAddr, int type) {
        this.addr = iterAddr;
        this.type = type;
        this.charIter = new StringCharacterIterator("");
    }

    @Override
    public Object clone() {
        int cloneAddr = cloneImpl(this.addr);
        NativeBreakIterator clone = new NativeBreakIterator(cloneAddr, this.type);
        // The RI doesn't clone the CharacterIterator.
        clone.charIter = this.charIter;
        return clone;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof NativeBreakIterator)) {
            return false;
        }
        // TODO: is this sufficient? shouldn't we be checking the underlying rules?
        NativeBreakIterator rhs = (NativeBreakIterator) object;
        return type == rhs.type && charIter.equals(rhs.charIter);
    }

    @Override
    public int hashCode() {
        return 42; // No-one uses BreakIterator as a hash key.
    }

    @Override
    protected void finalize() {
        closeBreakIteratorImpl(this.addr);
    }

    public int current() {
        return currentImpl(this.addr);
    }

    public int first() {
        return firstImpl(this.addr);
    }

    public int following(int offset) {
        return followingImpl(this.addr, offset);
    }

    public CharacterIterator getText() {
        int newLoc = currentImpl(this.addr);
        this.charIter.setIndex(newLoc);
        return this.charIter;
    }

    public int last() {
        return lastImpl(this.addr);
    }

    public int next(int n) {
        return nextImpl(this.addr, n);
    }

    public int next() {
        return nextImpl(this.addr, 1);
    }

    public int previous() {
        return previousImpl(this.addr);
    }

    public void setText(CharacterIterator newText) {
        this.charIter = newText;
        StringBuilder sb = new StringBuilder();
        for (char c = newText.first(); c != CharacterIterator.DONE; c = newText.next()) {
            sb.append(c);
        }
        setTextImpl(this.addr, sb.toString());
    }

    public void setText(String newText) {
        setText(new StringCharacterIterator(newText));
    }

    public boolean isBoundary(int offset) {
        return isBoundaryImpl(this.addr, offset);
    }

    public int preceding(int offset) {
        return precedingImpl(this.addr, offset);
    }

    public static NativeBreakIterator getCharacterInstance(Locale where) {
        return new NativeBreakIterator(getCharacterInstanceImpl(where.toString()), BI_CHAR_INSTANCE);
    }

    public static NativeBreakIterator getLineInstance(Locale where) {
        return new NativeBreakIterator(getLineInstanceImpl(where.toString()), BI_LINE_INSTANCE);
    }

    public static NativeBreakIterator getSentenceInstance(Locale where) {
        return new NativeBreakIterator(getSentenceInstanceImpl(where.toString()), BI_SENT_INSTANCE);
    }

    public static NativeBreakIterator getWordInstance(Locale where) {
        return new NativeBreakIterator(getWordInstanceImpl(where.toString()), BI_WORD_INSTANCE);
    }

    private static native int getCharacterInstanceImpl(String locale);
    private static native int getWordInstanceImpl(String locale);
    private static native int getLineInstanceImpl(String locale);
    private static native int getSentenceInstanceImpl(String locale);
    private static native void closeBreakIteratorImpl(int addr);
    private static native void setTextImpl(int addr, String text);
    private static native int cloneImpl(int addr);
    private static native int precedingImpl(int addr, int offset);
    private static native boolean isBoundaryImpl(int addr, int offset);
    private static native int nextImpl(int addr, int n);
    private static native int previousImpl(int addr);
    private static native int currentImpl(int addr);
    private static native int firstImpl(int addr);
    private static native int followingImpl(int addr, int offset);
    private static native int lastImpl(int addr);
}
