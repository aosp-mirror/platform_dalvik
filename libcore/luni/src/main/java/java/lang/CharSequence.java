/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;


/**
 * The CharSequence interface represents an ordered set of characters and the
 * functions to probe them.
 */
public interface CharSequence {

    /**
     * Returns the number of characters in the sequence.
     * 
     * @return the number of characters in the sequence
     */
    public int length();

    /**
     * Returns the character at the specified index, with the first character
     * having index zero.
     * 
     * @param index The index of the character to return
     * @return The requested character
     * @throws IndexOutOfBoundsException
     *             when <code>index &lt; 0</code> or
     *             <code>index</code> &gt;= the length of the <code>CharSequence</code>
     */
    public char charAt(int index);

    /**
     * Returns a CharSequence from the <code>start</code> index (inclusive) to
     * the <code>end</code> index (exclusive) of this sequence.
     *
     * @param       start The starting offset of the sub-sequence, that is, the
     *              index of the first character that goes into the sub-sequence
     * @param       end The ending offset of the sub-sequence, that is, the
     *              index of the first character after those that go into the
     *              sub-sequence
     * @return      The requested sub-sequence
     * @throws      IndexOutOfBoundsException when 1. either index is below 0
     *              2. either index &gt;= <code>this.length()</code>
     *              3. <code>start &gt; end </code>
     */
    public CharSequence subSequence(int start, int end);

    /**
     * Returns a String with the same characters and ordering of this
     * CharSequence
     * 
     * @return a String based on the CharSequence
     */
    public String toString();
}
