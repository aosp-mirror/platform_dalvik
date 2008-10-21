/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

/**
 * CharacterIterator is used to sequence over a group of characters. The
 * iteration starts at the begin index in the group of character and continues
 * to one index before the end index.
 */
public interface CharacterIterator extends Cloneable {

    /**
     * A constant which indicates there is no character.
     */
    public static final char DONE = '\uffff';

    /**
     * Returns a new CharacterIterator with the same properties.
     * 
     * @return a shallow copy of this CharacterIterator
     * 
     * @see java.lang.Cloneable
     */
    public Object clone();

    /**
     * Returns the character at the current index.
     * 
     * @return the current character, or DONE if the current index is past the
     *         end
     */
    public char current();

    /**
     * Sets the current position to the begin index and returns the character at
     * the begin index.
     * 
     * @return the character at the begin index
     */
    public char first();

    /**
     * Returns the begin index.
     * 
     * @return the index of the first character to iterate
     */
    public int getBeginIndex();

    /**
     * Returns the end index.
     * 
     * @return the index one past the last character to iterate
     */
    public int getEndIndex();

    /**
     * Returns the current index.
     * 
     * @return the current index
     */
    public int getIndex();

    /**
     * Sets the current position to the end index - 1 and returns the character
     * at the current position.
     * 
     * @return the character before the end index
     */
    public char last();

    /**
     * Increments the current index and returns the character at the new index.
     * 
     * @return the character at the next index, or DONE if the next index is
     *         past the end
     */
    public char next();

    /**
     * Decrements the current index and returns the character at the new index.
     * 
     * @return the character at the previous index, or DONE if the previous
     *         index is past the beginning
     */
    public char previous();

    /**
     * Sets the current index.
     * 
     * @param location The index the <code>CharacterIterator</code> is set to.
     * 
     * @return the character at the new index, or DONE if the index is past the
     *         end
     * 
     * @exception IllegalArgumentException
     *                when the new index is less than the begin index or greater
     *                than the end index
     */
    public char setIndex(int location);
}
