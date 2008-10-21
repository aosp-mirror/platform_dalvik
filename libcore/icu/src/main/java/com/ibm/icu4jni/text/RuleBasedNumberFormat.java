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

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class RuleBasedNumberFormat extends NumberFormat {

    /**
     * Enum of predefined RBNF types.
     */
    public enum RBNFType {
        /**
         * This creates a spellout instance of RBNF.
         * It formats numbers into textual representation:
         * 15 -> 'fifteen' or 15.15 -> 'fifteen point one five'
         *  and it can parse words into numbers: 'twenty' -> 20
         */
        SPELLOUT(0),
        /**
         * This creates an ordinal instance of RBNF.
         * It formats numbers into an ordinal text representation:
         * 15 -> '15th' and by parsing it also works in the other direction.
         */
        ORDINAL(1),
        /**
         * This creates instance of RBNF that allows to format numbers into time
         * values: 15 -> '15 sec.' and by parsing it also works in the other
         * direction.
         */
        DURATION(2);
        
        int type;
        
        RBNFType(int t) {
            type = t;
        }
        
        int getType() {
            return type;
        }
    }
    
    @Override
    protected void finalize(){
        close();
    }
    
    private int addr = 0;

    /**
     * Open a new rule based number format of selected type for the 
     * default location
     * 
     * @param type the type of rule based number format
     */
    public void open(RBNFType type) {
        this.addr = openRBNFImpl(type.getType(),
                Locale.getDefault().toString());
    }

    /**
     * Open a new rule based number format of selected type for the 
     * given location
     * 
     * @param type the type of rule based number format
     * @param locale the locale to use for this rule based number format
     */
    public void open(RBNFType type, Locale locale) {
        this.addr = openRBNFImpl(type.getType(),
                locale.toString());
    }
    
    private static native int openRBNFImpl(int type, String loc);

    /**
     * Open a new rule based number format for the 
     * default location. The rule passed to the method has to be of the form
     * described in the ibm icu documentation for RuleBasedNumberFormat.
     * 
     * @param rule the rule for the rule based number format
     */
    public void open(String rule) {
        this.addr = openRBNFImpl(rule, Locale.getDefault().toString());
    }

    /**
     * Open a new rule based number format for the 
     * given location. The rule passed to the method has to be of the form
     * described in the ibm icu documentation for RuleBasedNumberFormat.
     * 
     * @param rule the rule for the rule based number format
     * @param locale the locale to use for this rule based number format
     */
    public void open(String rule, Locale locale) {
        this.addr = openRBNFImpl(rule, locale.toString());
    }
    
    private static native int openRBNFImpl(String rule, String loc);
    
    /**
     * close a RuleBasedNumberFormat
     */
    public void close() {
        if(this.addr != 0) {
            closeRBNFImpl(this.addr);
            this.addr = 0;
        }
    }
    
    private static native void closeRBNFImpl(int addr); 
    
    @Override
    public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {

        if(buffer == null) {
            throw new NullPointerException();
        }
        
        String fieldType = null;
        
        if(field != null) {
            fieldType = getFieldType(field.getFieldAttribute());
        }
        
        String result = formatRBNFImpl(this.addr, value, field, 
                fieldType, null);
        
        buffer.append(result.toCharArray(), 0, result.length());
        
        return buffer;
    }
    
    private static native String formatRBNFImpl(int addr, long value, 
            FieldPosition field, String fieldType, StringBuffer buffer);

    @Override
    public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {

        if(buffer == null) {
            throw new NullPointerException();
        }
        
        String fieldType = null;
        
        if(field != null) {
            fieldType = getFieldType(field.getFieldAttribute());
        }
        
        String result = formatRBNFImpl(this.addr, value, field, 
                fieldType, null);
        
        buffer.append(result.toCharArray(), 0, result.length());
        
        return buffer;
    }
    
    private static native String formatRBNFImpl(int addr, double value, 
            FieldPosition field, String fieldType, StringBuffer buffer);

    @Override
    public Number parse(String string, ParsePosition position) {
        return parseRBNFImpl(this.addr, string, position, false);
    }
    
    /**
     * This method has the same functionality 
     * as {@link #parse(String, ParsePosition)}
     * But it uses lenient parsing. This means it also accepts strings that
     * differ from the correct writing (e.g. case or umlaut differences).
     * 
     * @param string the string to parse
     * @param position the ParsePosition, updated on return with the index 
     *        following the parsed text, or on error the index is unchanged and 
     *        the error index is set to the index where the error occurred
     * @return the Number resulting from the parse, or null if there is an error
     */
    public Number parseLenient(String string, ParsePosition position) {
        return parseRBNFImpl(this.addr, string, position, true);
    }
    
    static native Number parseRBNFImpl(int addr, String string, ParsePosition position, boolean lenient);
    
    
    static private String getFieldType(Format.Field field) {
        if(field == null) {
            return null;
        }
        if(field.equals(NumberFormat.Field.SIGN)) {
            return "sign";
        }
        if(field.equals(NumberFormat.Field.INTEGER)) {
            return "integer";
        }
        if(field.equals(NumberFormat.Field.FRACTION)) {
            return "fraction";
        }
        if(field.equals(NumberFormat.Field.EXPONENT)) {
            return "exponent";
        }
        if(field.equals(NumberFormat.Field.EXPONENT_SIGN)) {
            return "exponent_sign";
        }
        if(field.equals(NumberFormat.Field.EXPONENT_SYMBOL)) {
            return "exponent_symbol";
        }
        if(field.equals(NumberFormat.Field.CURRENCY)) {
            return "currency";
        }
        if(field.equals(NumberFormat.Field.GROUPING_SEPARATOR)) {
            return "grouping_separator";
        }
        if(field.equals(NumberFormat.Field.DECIMAL_SEPARATOR)) {
            return "decimal_separator";
        }
        if(field.equals(NumberFormat.Field.PERCENT)) {
            return "percent";
        }
        if(field.equals(NumberFormat.Field.PERMILLE)) {
            return "permille";
        }
        return null;
    }
}
