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
 * The unchecked exception will be thrown out when the parameter is incompatible
 * with the corresponding format specifier.
 * @since 1.5
 */
public class IllegalFormatConversionException extends IllegalFormatException
        implements Serializable {
    private static final long serialVersionUID = 17000126L;

    private char c;

    private Class<?> arg;

    /**
     * Constructs an IllegalFormatConversionException with the class of the
     * mismatched conversion and corresponding parameter.
     * 
     * @param c
     *            The class of the mismatched conversion.
     * @param arg
     *            The corresponding parameter.
     */
    public IllegalFormatConversionException(char c, Class<?> arg) {
        this.c = c;
        if (arg == null) {
            throw new NullPointerException();
        }
        this.arg = arg;
    }

    /**
     * Return the class of the mismatched parameter.
     * 
     * @return The class of the mismatched parameter.
     */
    public Class<?> getArgumentClass() {
        return arg;
    }

    /**
     * Return the incompatible conversion.
     * 
     * @return The incompatible conversion.
     */
    public char getConversion() {
        return c;
    }

    /**
     * Return the message string of the IllegalFormatConversionException.
     * 
     * @return The message string of the IllegalFormatConversionException.
     */
    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(c);
        buffer.append(" is incompatible with ");
        buffer.append(arg.getName());
        return buffer.toString();
    }

}
