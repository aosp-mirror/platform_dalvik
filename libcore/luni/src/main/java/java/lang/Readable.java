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
import java.nio.CharBuffer;

/**
 * Readable marks that the implementing class provides character sequence.
 * Readable gives a reference to character sequence from within itself to caller
 * through a <code>CharBuffer</code> parameter of the <code>read</code>
 * method.
 */
public interface Readable {

    /**
     * Reads the characters into the given <code>CharBuffer</code>. The
     * maximum number of characters read is <code>CharBuffer.remaining()</code>.
     * 
     * @param cb
     *            the buffer to be filled in by the characters read
     * @return the number of characters actually read, or -1 if this
     *         <code>Readable</code> reaches its end
     * @throws IOException
     *             if some I/O operations fail
     */
    int read(CharBuffer cb) throws IOException;
}
