/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.util;

import java.io.Serializable;

/**
 * 
 * The unchecked exception will be thrown out if the combination of the format
 * flags is illegal.
 * 
 */
public class IllegalFormatFlagsException extends IllegalFormatException
        implements Serializable {
    private static final long serialVersionUID = 790824L;

    private String flags;

    /**
     * Constructs an IllegalFormatFlagsException with the specified flags.
     * 
     * @param f
     *            The specified flags.
     */
    public IllegalFormatFlagsException(String f) {
        if (null == f) {
            throw new NullPointerException();
        }
        flags = f;
    }

    /**
     * Return the flags that are illegal.
     * 
     * @return The flags that are illegal.
     */
    public String getFlags() {
        return flags;
    }

    /**
     * Return the message string of the IllegalFormatFlagsException.
     * 
     * @return The message string of the IllegalFormatFlagsException.
     */
    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Flags = '");
        buffer.append(flags);
        buffer.append("'");
        return buffer.toString();
    }

}
