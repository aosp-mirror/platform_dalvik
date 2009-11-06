/*
 * Copyright (C) 2009 The Android Open Source Project
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

package org.kxml2.io;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KXmlSerializerTest extends TestCase {

    /** the namespace */
    final String ns = null;

    public void testEmittingNullCharacterThrows() throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        KXmlSerializer serializer = new KXmlSerializer();
        serializer.setOutput(bytesOut, "UTF-8");
        serializer.startDocument("UTF-8", null);

        serializer.startTag(ns, "foo");
        try {
            serializer.text("bar\0baz");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        serializer.startTag(ns, "bar");
        try {
            serializer.attribute(ns, "baz", "qu\0ux");
        } catch (IllegalArgumentException expected) {
        }
    }
}
