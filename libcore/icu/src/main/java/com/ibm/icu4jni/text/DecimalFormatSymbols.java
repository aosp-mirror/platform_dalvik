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

import com.ibm.icu4jni.text.NativeDecimalFormat.UNumberFormatSymbol;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class DecimalFormatSymbols {
    
    private int addr;
    
    private Locale loc;

    private DecimalFormatSymbols(int addr, Locale loc) {
        this.addr = addr;
        this.loc = loc;
    }
    
    public DecimalFormatSymbols(Locale locale) {
        this.loc = locale;
        ResourceBundle bundle = AccessController.
        doPrivileged(new PrivilegedAction<ResourceBundle>() {
            public ResourceBundle run() {
            return ResourceBundle.getBundle(
                    "org.apache.harmony.luni.internal.locale.Locale", loc); //$NON-NLS-1$
            }
        });
        String pattern = bundle.getString("Number");
        this.addr = NativeDecimalFormat.openDecimalFormatImpl(
                locale.toString(), pattern);
        String currSymbol = bundle.getString("CurrencySymbol");
        String intCurrSymbol = bundle.getString("IntCurrencySymbol");
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_CURRENCY_SYMBOL.ordinal(), currSymbol);
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_INTL_CURRENCY_SYMBOL.ordinal(), 
                intCurrSymbol);
    }
    
    @Override
    public boolean equals(Object object) {
        if(object == null) {
            return false;
        }
        if(!(object instanceof DecimalFormatSymbols)) {
            return false;
        }
        
        DecimalFormatSymbols sym = (DecimalFormatSymbols) object;
        
        if(sym.addr == this.addr) {
            return true;
        }
        
        boolean result = true;
        
        Currency objCurr = sym.getCurrency();
        Currency thisCurr = this.getCurrency();
        if(objCurr != null) {
            result &= objCurr.getCurrencyCode().equals(thisCurr.getCurrencyCode());
            result &= objCurr.getSymbol().equals(thisCurr.getSymbol());
            result &= objCurr.getDefaultFractionDigits() == thisCurr.getDefaultFractionDigits();
        } else {
            result &= thisCurr == null;
        }
        result &= sym.getCurrencySymbol().equals(this.getCurrencySymbol());
        result &= sym.getDecimalSeparator() == this.getDecimalSeparator();
        result &= sym.getDigit() == this.getDigit();
        result &= sym.getGroupingSeparator() == this.getGroupingSeparator();
        result &= sym.getInfinity().equals(this.getInfinity());
        result &= sym.getInternationalCurrencySymbol().equals(
                this.getInternationalCurrencySymbol());
        result &= sym.getMinusSign() == this.getMinusSign();
        result &= sym.getMonetaryDecimalSeparator() == 
                this.getMonetaryDecimalSeparator();
        result &= sym.getNaN().equals(this.getNaN());
        result &= sym.getPatternSeparator() == this.getPatternSeparator();
        result &= sym.getPercent() == this.getPercent();
        result &= sym.getPerMill() == this.getPerMill();
        result &= sym.getZeroDigit() == this.getZeroDigit();
        
        return result;
    }

    @Override
    public Object clone() {
        int addr = NativeDecimalFormat.cloneImpl(this.addr);
        Locale loc = (Locale) this.loc.clone();
        return new DecimalFormatSymbols(addr, loc);
    }
    
    public void setCurrency(Currency currency) {
        NativeDecimalFormat.setSymbol(this.addr,
               UNumberFormatSymbol.UNUM_CURRENCY_SYMBOL.ordinal(), 
               currency.getSymbol());
        NativeDecimalFormat.setSymbol(this.addr,
               UNumberFormatSymbol.UNUM_INTL_CURRENCY_SYMBOL.ordinal(), 
               currency.getCurrencyCode());
    }

    public void setCurrencySymbol(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_CURRENCY_SYMBOL.ordinal(), 
                symbol);
    }

    public void setDecimalSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_DECIMAL_SEPARATOR_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setDigit(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_DIGIT_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setGroupingSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_GROUPING_SEPARATOR_SYMBOL.ordinal(), 
                "" + symbol);
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_MONETARY_GROUPING_SEPARATOR_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setInfinity(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_INFINITY_SYMBOL.ordinal(), 
                symbol);
    }

    public void setInternationalCurrencySymbol(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_INTL_CURRENCY_SYMBOL.ordinal(), 
                symbol);
    }

    public void setMinusSign(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_MINUS_SIGN_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setMonetaryDecimalSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_MONETARY_SEPARATOR_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setNaN(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_NAN_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setPatternSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_PATTERN_SEPARATOR_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setPercent(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_PERCENT_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setPerMill(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_PERMILL_SYMBOL.ordinal(), 
                "" + symbol);
    }

    public void setZeroDigit(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr,
                UNumberFormatSymbol.UNUM_ZERO_DIGIT_SYMBOL.ordinal(), 
                "" + symbol);
    }
 
    public Currency getCurrency() {
        String curr = NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_INTL_CURRENCY_SYMBOL.ordinal());
        if(curr.equals("") || curr.equals("\u00a4\u00a4")) {
            return null;
        }
         return Currency.getInstance(curr);
    }
 
    public String getCurrencySymbol() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_CURRENCY_SYMBOL.ordinal());
    }
 
    public char getDecimalSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_DECIMAL_SEPARATOR_SYMBOL.ordinal())
                .charAt(0);
    }
 
    public char getDigit() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_DIGIT_SYMBOL.ordinal())
                .charAt(0);
    }
 
    public char getGroupingSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_GROUPING_SEPARATOR_SYMBOL.ordinal())
                .charAt(0);
    }
 
    public String getInfinity() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_INFINITY_SYMBOL.ordinal());
    }
 
    public String getInternationalCurrencySymbol() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_INTL_CURRENCY_SYMBOL.ordinal());
    }
 
    public char getMinusSign() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_MINUS_SIGN_SYMBOL.ordinal())
                .charAt(0);
    }
 
    public char getMonetaryDecimalSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_MONETARY_SEPARATOR_SYMBOL.ordinal())
                .charAt(0);
    }

    public String getNaN() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_NAN_SYMBOL.ordinal());
    }
 
    public char getPatternSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_PATTERN_SEPARATOR_SYMBOL.ordinal())
                .charAt(0);
    }
 
    public char getPercent() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_PERCENT_SYMBOL.ordinal())
                .charAt(0);
    }
 
    public char getPerMill() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_PERMILL_SYMBOL.ordinal())
                .charAt(0);
    }

    public char getZeroDigit() {
        return NativeDecimalFormat.getSymbol(this.addr,
                UNumberFormatSymbol.UNUM_ZERO_DIGIT_SYMBOL.ordinal())
                .charAt(0);
    }
    
    int getAddr() {
        return this.addr;
    }
    
    Locale getLocale() {
        return this.loc;
    }
    
    protected void finalize() {
        NativeDecimalFormat.closeDecimalFormatImpl(this.addr);
    }
}
