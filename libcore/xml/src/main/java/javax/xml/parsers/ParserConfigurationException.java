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

package javax.xml.parsers;

/**
 * Represents an exception that occurred during the configuration of parser.
 * 
 * @since Android 1.0
 */
public class ParserConfigurationException extends Exception {

    /**
     * Creates a new {@code ParserConfigurationException} with no error message.
     * 
     * @since Android 1.0
     */
    public ParserConfigurationException() {
        super();
    }

    /**
     * Creates a new {@code ParserConfigurationException} with a given error
     * message.
     * 
     * @param msg the error message.
     * 
     * @since Android 1.0
     */
    public ParserConfigurationException(String msg) {
        super(msg);
    }

}
