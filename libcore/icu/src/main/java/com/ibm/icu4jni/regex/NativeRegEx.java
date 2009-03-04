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

package com.ibm.icu4jni.regex;

public class NativeRegEx {

    /**
     * Opens (compiles) an ICU regular expression.
     */
    public static native int open(String pattern, int flags);
    
    /**
     * Makes a copy of a compiled regular expression.
     */
    public static native int clone(int regex);
    
    /**
     * Closes the regular expression, recovering all resources (memory) it was
     * holding. 
     */    
    public static native void close(int regex);

    /**
     * Sets the subject text string upon which the regular expression will look
     * for matches.
     */
    public static native void setText(int regex, String text);
    
    /**
     * Attempts to match the input string, beginning at startIndex, against the
     * pattern. 
     */
    public static native boolean matches(int regex, int startIndex);
    
    /**
     * Attempts to match the input string, starting from the specified index,
     * against the pattern. 
     */
    public static native boolean lookingAt(int regex, int startIndex);

    /**
     * Finds the first matching substring of the input string that matches the
     * pattern. 
     */
    public static native boolean find(int regex, int startIndex);
    
    /**
     * Finds the first matching substring of the input string that matches the
     * pattern. 
     */
    public static native boolean findNext(int regex);
    
    /**
     * Gets the number of capturing groups in this regular expression's pattern. 
     */
    public static native int groupCount(int regex);
    
    /**
     * Gets all the group information for the current match of the pattern.
     */
    public static native void startEnd(int regex, int[] startEnd);
    
    /**
     * Sets the region of the input to be considered during matching.
     */
    public static native void setRegion(int regex, int start, int end);
    
    /**
     * Queries the start of the region of the input to be considered during
     * matching.
     */
    public static native int regionStart(int regex);
    
    /**
     * Queries the end of the region of the input to be considered during
     * matching.
     */
    public static native int regionEnd(int regex);
    
    /**
     * Controls the transparency of the region bounds.
     */
    public static native void useTransparentBounds(int regex, boolean value);
    
    /**
     * Queries the transparency of the region bounds.
     */
    public static native boolean hasTransparentBounds(int regex);
    
    /**
     * Controls the anchoring property of the region bounds.
     */
    public static native void useAnchoringBounds(int regex, boolean value);
    
    /**
     * Queries the anchoring property of the region bounds.
     */
    public static native boolean hasAnchoringBounds(int regex);
    
    /**
     * Queries whether we hit the end of the input during the last match.
     */
    public static native boolean hitEnd(int regex);
    
    /**
     * Queries whether more input might change a current match, but wouldn't
     * destroy it.
     */
    public static native boolean requireEnd(int regex);
    
    /**
     * Resets the matcher, cause a current match to be lost, and sets the
     * position at which a subsequent findNext() would start.
     */
    public static native void reset(int regex, int position);
}
