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

import java.io.IOException;

/**
 * Appendable is an object used to append character or character sequence. Any
 * class implements this interface can receive data formatted by
 * <code>Formatter</code>. The appended character or character sequence
 * should be valid accroding to the rules described
 * <code>Unicode Character Representation</code>.
 * <p>
 * Appendable itself does not gurantee thread safety. This responsibility is up
 * to the implementing class.</p>
 * <p>
 * The implementing class can choose different exception handling mechanism. It
 * can choose to throw exceptions other than IOException but which must be
 * compatible with IOException, or does not throw any exceptions at all and use
 * error code instead. All in all, the implementing class does not gurantee to
 * propagate the exception declared by this interface.</p>
 * 
 */
public interface Appendable {
    
    /**
     * Append the given character.
     * 
     * @param c the character to append
     * @return this <code>Appendable</code>
     * @throws IOException  if some I/O operation fails
     */
    Appendable append(char c) throws IOException;

    /**
     * Append the given <code>CharSequence</code>.
     * <p>
     * The behaviour of this method depends on the implementation class of 
     * <code>Appendable</code>.</p>
     * <p>
     * If the give <code>CharSequence</code> is null, the sequence is treated as 
     * String "null".</p>
     * 
     * @param csq   the <code>CharSequence</code> to be append
     * @return this <code>Appendable</code>
     * @throws IOException  if some I/O operation fails
     */
    Appendable append(CharSequence csq) throws IOException;

    /**
     * Append part of the given <code>CharSequence</code>.
     * <p>
     * If the given <code>CharSequence</code> is not null, this method behaves 
     * same as the following statement:</p>
     * <pre>    out.append(csq.subSequence(start, end)) </pre>
     * <p>
     * If the give <code>CharSequence</code> is null, the sequence is treated as 
     * String "null".</p>
     * 
     * @param csq       the <code>CharSequence</code> to be append 
     * @param start     the index to spicify the start position of 
     *                  <code>CharSequence</code> to be append, must be non-negative,
     *                  and not larger than the end
     * @param end       the index to speicify the end position of
     *                  <code>CharSequence</code> to be append, must be non-negative,
     *                  and not larger than the size of csq 
     * @return this <code>Appendable</code>
     * @throws IOException  if some I/O operation fails
     * @throws IndexOutOfBoundsException
     *                  if the start or end is illegal
     */
    Appendable append(CharSequence csq, int start, int end) throws IOException;
}
