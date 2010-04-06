/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import junit.framework.AssertionFailedError;

public class SerializableTester<T> {

    private final String golden;
    private final T value;

    public SerializableTester(T value, String golden) {
        this.golden = golden;
        this.value = value;
    }

    protected void verify(T deserialized) {}

    public void test() {
        try {
            if (golden == null || golden.length() == 0) {
                fail("No golden value supplied! Consider using this: "
                        + hexEncode(serialize(value)));
            }

            // just a sanity check! if this fails, verify() is probably broken
            verify(value);

            @SuppressWarnings("unchecked") // deserialize should return the proper type
            T deserialized = (T) deserialize(hexDecode(golden));
            assertEquals("User-constructed value doesn't equal deserialized golden value",
                    value, deserialized);
            verify(deserialized);

            @SuppressWarnings("unchecked") // deserialize should return the proper type
            T reserialized = (T) deserialize(serialize(value));
            assertEquals("User-constructed value doesn't equal itself, reserialized",
                    value, reserialized);
            verify(reserialized);

        } catch (Exception e) {
            Error failure = new AssertionFailedError();
            failure.initCause(e);
            throw failure;
        }
    }

    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(object);
        return out.toByteArray();
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object result = in.readObject();
        assertEquals(-1, in.read());
        return result;
    }

    private String hexEncode(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private byte[] hexDecode(String s) {
        byte[] result = new byte[s.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(s.substring(i*2, i*2 + 2), 16);
        }
        return result;
    }
}

