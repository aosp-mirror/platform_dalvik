/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dx.io;

import static com.android.dx.io.EncodedValueReader.ENCODED_ANNOTATION;

/**
 * An annotation.
 */
public final class Annotation implements Comparable<Annotation> {
    private final DexBuffer buffer;
    private final byte visibility;
    private final EncodedValue encodedAnnotation;

    public Annotation(DexBuffer dexBuffer, byte visibility, EncodedValue encodedAnnotation) {
        this.buffer = dexBuffer;
        this.visibility = visibility;
        this.encodedAnnotation = encodedAnnotation;
    }

    public byte getVisibility() {
        return visibility;
    }

    public EncodedValueReader getReader() {
        return new EncodedValueReader(encodedAnnotation, ENCODED_ANNOTATION);
    }

    public int getTypeIndex() {
        EncodedValueReader reader = getReader();
        reader.readAnnotation();
        return reader.getAnnotationType();
    }

    public void writeTo(DexBuffer.Section out) {
        out.writeByte(visibility);
        encodedAnnotation.writeTo(out);
    }

    @Override public int compareTo(Annotation other) {
        return encodedAnnotation.compareTo(other.encodedAnnotation);
    }

    @Override public String toString() {
        return buffer == null
                ? visibility + " " + getTypeIndex()
                : visibility + " " + buffer.typeNames().get(getTypeIndex());
    }
}
