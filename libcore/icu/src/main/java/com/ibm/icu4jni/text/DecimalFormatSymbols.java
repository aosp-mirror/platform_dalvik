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

import com.ibm.icu4jni.util.LocaleData;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class DecimalFormatSymbols implements Cloneable {
    
    // Constants corresponding to the native type UNumberFormatSymbol, for use with getSymbol/setSymbol.
    private static final int UNUM_DECIMAL_SEPARATOR_SYMBOL = 0;
    private static final int UNUM_GROUPING_SEPARATOR_SYMBOL = 1;
    private static final int UNUM_PATTERN_SEPARATOR_SYMBOL = 2;
    private static final int UNUM_PERCENT_SYMBOL = 3;
    private static final int UNUM_ZERO_DIGIT_SYMBOL = 4;
    private static final int UNUM_DIGIT_SYMBOL = 5;
    private static final int UNUM_MINUS_SIGN_SYMBOL = 6;
    private static final int UNUM_PLUS_SIGN_SYMBOL = 7;
    private static final int UNUM_CURRENCY_SYMBOL = 8;
    private static final int UNUM_INTL_CURRENCY_SYMBOL = 9;
    private static final int UNUM_MONETARY_SEPARATOR_SYMBOL = 10;
    private static final int UNUM_EXPONENTIAL_SYMBOL = 11;
    private static final int UNUM_PERMILL_SYMBOL = 12;
    private static final int UNUM_PAD_ESCAPE_SYMBOL = 13;
    private static final int UNUM_INFINITY_SYMBOL = 14;
    private static final int UNUM_NAN_SYMBOL = 15;
    private static final int UNUM_SIGNIFICANT_DIGIT_SYMBOL = 16;
    private static final int UNUM_MONETARY_GROUPING_SEPARATOR_SYMBOL = 17;
    private static final int UNUM_FORMAT_SYMBOL_COUNT = 18;

    private final int addr;

    // Used to implement clone.
    private DecimalFormatSymbols(DecimalFormatSymbols other) {
        this.addr = NativeDecimalFormat.cloneImpl(other.addr);
    }
    
    public DecimalFormatSymbols(Locale locale) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        this.addr = NativeDecimalFormat.openDecimalFormatImpl(locale.toString(),
                localeData.numberPattern);
        NativeDecimalFormat.setSymbol(this.addr, UNUM_CURRENCY_SYMBOL, localeData.currencySymbol);
        NativeDecimalFormat.setSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL,
                localeData.internationalCurrencySymbol);
    }
    
    public DecimalFormatSymbols(Locale locale, java.text.DecimalFormatSymbols symbols) {
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        this.addr = NativeDecimalFormat.openDecimalFormatImpl(locale.toString(),
                localeData.numberPattern);
        copySymbols(symbols);
    }
    
    /**
     * Copies the java.text.DecimalFormatSymbols' settings into this object.
     */
    public void copySymbols(final java.text.DecimalFormatSymbols dfs) {
        setCurrencySymbol(dfs.getCurrencySymbol());
        setDecimalSeparator(dfs.getDecimalSeparator());
        setDigit(dfs.getDigit());
        setGroupingSeparator(dfs.getGroupingSeparator());
        setInfinity(dfs.getInfinity());
        setInternationalCurrencySymbol(dfs.getInternationalCurrencySymbol());
        setMinusSign(dfs.getMinusSign());
        setMonetaryDecimalSeparator(dfs.getMonetaryDecimalSeparator());
        setNaN(dfs.getNaN());
        setPatternSeparator(dfs.getPatternSeparator());
        setPercent(dfs.getPercent());
        setPerMill(dfs.getPerMill());
        setZeroDigit(dfs.getZeroDigit());
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
        return new DecimalFormatSymbols(this);
    }
    
    public void setCurrency(Currency currency) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_CURRENCY_SYMBOL, currency.getSymbol());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL,
                currency.getCurrencyCode());
    }

    public void setCurrencySymbol(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_CURRENCY_SYMBOL, symbol);
    }

    public void setDecimalSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_DECIMAL_SEPARATOR_SYMBOL, symbol);
    }

    public void setDigit(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_DIGIT_SYMBOL, symbol);
    }

    public void setGroupingSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_GROUPING_SEPARATOR_SYMBOL, symbol);
        NativeDecimalFormat.setSymbol(this.addr, UNUM_MONETARY_GROUPING_SEPARATOR_SYMBOL, symbol);
    }

    public void setInfinity(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_INFINITY_SYMBOL, symbol);
    }

    public void setInternationalCurrencySymbol(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL, symbol);
    }

    public void setMinusSign(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_MINUS_SIGN_SYMBOL, symbol);
    }

    public void setMonetaryDecimalSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_MONETARY_SEPARATOR_SYMBOL, symbol);
    }

    public void setNaN(String symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_NAN_SYMBOL, symbol);
    }

    public void setPatternSeparator(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_PATTERN_SEPARATOR_SYMBOL, symbol);
    }

    public void setPercent(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_PERCENT_SYMBOL, symbol);
    }

    public void setPerMill(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_PERMILL_SYMBOL, symbol);
    }

    public void setZeroDigit(char symbol) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_ZERO_DIGIT_SYMBOL, symbol);
    }
 
    public Currency getCurrency() {
        String curr = NativeDecimalFormat.getSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL);
        if (curr.equals("") || curr.equals("\u00a4\u00a4")) {
            return null;
        }
        return Currency.getInstance(curr);
    }
 
    public String getCurrencySymbol() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_CURRENCY_SYMBOL);
    }
 
    public char getDecimalSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_DECIMAL_SEPARATOR_SYMBOL).charAt(0);
    }
 
    public char getDigit() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_DIGIT_SYMBOL).charAt(0);
    }
 
    public char getGroupingSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_GROUPING_SEPARATOR_SYMBOL).charAt(0);
    }
 
    public String getInfinity() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_INFINITY_SYMBOL);
    }
 
    public String getInternationalCurrencySymbol() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL);
    }
 
    public char getMinusSign() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_MINUS_SIGN_SYMBOL).charAt(0);
    }
 
    public char getMonetaryDecimalSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_MONETARY_SEPARATOR_SYMBOL).charAt(0);
    }

    public String getNaN() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_NAN_SYMBOL);
    }
 
    public char getPatternSeparator() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_PATTERN_SEPARATOR_SYMBOL).charAt(0);
    }
 
    public char getPercent() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_PERCENT_SYMBOL).charAt(0);
    }
 
    public char getPerMill() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_PERMILL_SYMBOL).charAt(0);
    }

    public char getZeroDigit() {
        return NativeDecimalFormat.getSymbol(this.addr, UNUM_ZERO_DIGIT_SYMBOL).charAt(0);
    }
    
    int getAddr() {
        return this.addr;
    }
    
    protected void finalize() {
        NativeDecimalFormat.closeDecimalFormatImpl(this.addr);
    }
}
