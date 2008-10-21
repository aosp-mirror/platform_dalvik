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

public class RuleBasedBreakIterator extends BreakIterator {

    private CharacterIterator charIter;
    
    private int addr;

    RuleBasedBreakIterator(int iterAddr, int type) {
        this.addr = iterAddr;
        this.type = type;
        this.charIter = new StringCharacterIterator("");
    }
    
    @Override
    public Object clone() {
        int cloneAddr = NativeBreakIterator.cloneImpl(this.addr);
        RuleBasedBreakIterator rbbi = 
                new RuleBasedBreakIterator(cloneAddr, this.type);
        
        rbbi.charIter = this.charIter;
        
        return rbbi;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null) {
            return false;
        }
        
        if(!(object instanceof RuleBasedBreakIterator)) {
            return false;
        }
        
        CharacterIterator iter = ((RuleBasedBreakIterator) object).charIter;
        
        boolean result = this.type == ((RuleBasedBreakIterator) object).type;
        
        return result && iter.equals(this.charIter);
    }
    
    @Override
    public int current() {
        return NativeBreakIterator.currentImpl(this.addr);
    }

    @Override
    public int first() {
        return NativeBreakIterator.firstImpl(this.addr);
    }

    @Override
    public int following(int offset) {
        return NativeBreakIterator.followingImpl(this.addr, offset);
    }

    @Override
    public CharacterIterator getText() {
        int newLoc = NativeBreakIterator.currentImpl(this.addr);
        this.charIter.setIndex(newLoc);
        return this.charIter;
    }

    @Override
    public int last() {
        return NativeBreakIterator.lastImpl(this.addr);
    }
    
    @Override
    public int next(int n) {
        return NativeBreakIterator.nextImpl(this.addr, n);
    }

    @Override
    public int next() {
        return NativeBreakIterator.nextImpl(this.addr, 1);
    }
    
    @Override
    public int previous() {
        return NativeBreakIterator.previousImpl(this.addr);
    }

    @Override
    public void setText(CharacterIterator newText) {
        this.charIter = newText;

        StringBuilder sb = new StringBuilder();
        
        char c = newText.first();
        while(c != CharacterIterator.DONE) {
            sb.append(c);
            c = newText.next();
        }

        NativeBreakIterator.setTextImpl(this.addr, sb.toString());
    }

    protected void finalize() {
        NativeBreakIterator.closeBreakIteratorImpl(this.addr);
    }

    @Override
    public boolean isBoundary(int offset) {
        return NativeBreakIterator.isBoundaryImpl(this.addr, offset);
    }

    @Override
    public int preceding(int offset) {
        return NativeBreakIterator.precedingImpl(this.addr, offset);
    }

}
