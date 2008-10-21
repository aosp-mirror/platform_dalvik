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

import org.apache.harmony.luni.util.Msg;

/**
 * The unchecked exception will be thrown out if the format conversion is
 * unknown.
 * 
 * 
 */
public class UnknownFormatConversionException extends IllegalFormatException {
    private static final long serialVersionUID = 19060418L;

    private String s;

    /**
     * Constructs an UnknownFormatConversionException with the unknown format
     * conversion.
     * 
     * @param s
     *            The unknown format conversion
     */
    public UnknownFormatConversionException(String s) {
        this.s = s;
    }

    /**
     * Returns the conversion associated with the exception.
     * 
     * @return The conversion associated with the exception.
     */
    public String getConversion() {
        return s;
    }

    /**
     * Returns the message of the exception.
     * 
     * @return The message of the exception.
     */
    @Override
    public String getMessage() {
        return Msg.getString("K0349", s);
    }
}
