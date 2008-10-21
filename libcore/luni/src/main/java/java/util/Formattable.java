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

/**
 * Any class that need to perform customer formatting by transferring converter
 * specifier 's' to Formatter should implement the Formattable interface. Basic
 * format is allowed by the interface to format arbitrary objects.
 */

public interface Formattable {

    /**
     * Formats the object using the specified formatter.
     * 
     * @param formatter
     *            The formatter to use in the formatTo.
     * @param flags
     *            The flags applied to the output format, which is a bitmask
     *            that is any combination of FormattableFlags.LEFT_JUSTIFY,
     *            FormattableFlags.UPPERCASE, and FormattableFlags.ALTERNATE. If
     *            no such flag is set, the output is formatted by the default
     *            formatting of the implementation of the interface.
     * @param width
     *            The minimum number of characters that should be written to the
     *            output. Additional space ' ' is added to the output if the
     *            length of the converted value is less than the width until the
     *            length equals the width. These spaces are added at the
     *            beginning by default unless the flag
     *            FormattableFlags.LEFT_JUSTIFY is set, which denotes that
     *            padding should be added at the end. If width is -1, then no
     *            minimum requirement.
     * @param precision
     *            The maximum number of characters that can be written to the
     *            output. The procedure to trunk the output according to the
     *            precision is invoked before that of padding to width. If the
     *            precision is -1, then no maximum requirement.
     * @throws IllegalFormatException
     *             If any of the parameters is not supported.
     */
    void formatTo(Formatter formatter, int flags, int width, int precision)
            throws IllegalFormatException;
}
