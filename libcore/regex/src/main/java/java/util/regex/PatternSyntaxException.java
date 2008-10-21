/*
 * Copyright (C) 2007 The Android Open Source Project
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

package java.util.regex;

import java.util.Arrays;

/**
 * Encapsulates a syntax error that occurred during the compilation of a
 * {@link Pattern}. Might include a detailed description, the original regular
 * expression, and the index at which the error occurred.
 * 
 * @see Pattern#compile(String)
 * @see Pattern#compile(java.lang.String,int)
 * 
 * @since Android 1.0
 */
public class PatternSyntaxException extends IllegalArgumentException {
    
    private static final long serialVersionUID = -3864639126226059218L;
    
    /**
     * Holds the syntactically incorrect regular expression, or null if the
     * regular expression is not known.
     */
    private String pattern;

    /**
     * Holds the description of the syntax error, or null if the description is
     * not known.
     */
    private String description;

    /**
     * Holds the index around which the error occured, or -1, in case it is
     * unknown.
     */
    private int index = -1;

    /**
     * Creates a new PatternSyntaxException for a given message, pattern, and
     * error index.
     * 
     * @param description
     *            the description of the syntax error, or {@code null} if the
     *            description is not known.
     * @param pattern
     *            the syntactically incorrect regular expression, or
     *            {@code null} if the regular expression is not known.
     * @param index
     *            the character index around which the error occurred, or -1 if
     *            the index is not known.
     * @since Android 1.0
     */
    public PatternSyntaxException(String description, String pattern, int index) {
        this.pattern = pattern;
        this.description = description;
        this.index = index;
    }

    /**
     * Returns the syntactically incorrect regular expression.
     * 
     * @return the regular expression.
     * 
     * @since Android 1.0
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns a detailed error message for the exception. The message is
     * potentially multi-line, and it might include a detailed description, the
     * original regular expression, and the index at which the error occured.
     * 
     * @return the error message.
     * 
     * @since Android 1.0
     */
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("Syntax error");
        
        if (description != null) {
            builder.append(' ');
            builder.append(description);
        }

        if (index >= 0) {
            builder.append(" near index " + index + ":");
        }
        
        if (pattern != null) {
            builder.append('\n');
            builder.append(pattern);
            
            if (index >= 0) {
                char[] spaces = new char[index];
                Arrays.fill(spaces, ' ');
                builder.append('\n');
                builder.append(spaces);
                builder.append('^');
            }
        }
        
        return builder.toString();
    }

    /**
     * Returns the description of the syntax error, or {@code null} if the
     * description is not known.
     * 
     * @return the description.
     * @since Android 1.0
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the character index around which the error occurred, or -1 if the
     * index is not known.
     * 
     * @return the index.
     * 
     * @since Android 1.0
     */
    public int getIndex() {
        return index;
    }

}
