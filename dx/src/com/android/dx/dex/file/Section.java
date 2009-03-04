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

package com.android.dx.dex.file;

import com.android.dx.util.AnnotatedOutput;

import java.util.Collection;

/**
 * A section of a <code>.dex</code> file. Each section consists of a list
 * of items of some sort or other.
 */
public abstract class Section {
    /** null-ok; name of this part, for annotation purposes */
    private final String name;

    /** non-null; file that this instance is part of */
    private final DexFile file;

    /** &gt; 0; alignment requirement for the final output;
     * must be a power of 2 */
    private final int alignment;

    /** &gt;= -1; offset from the start of the file to this part, or
     * <code>-1</code> if not yet known */
    private int fileOffset;

    /** whether {@link #prepare} has been called successfully on this
     * instance */
    private boolean prepared;

    /**
     * Validates an alignment.
     * 
     * @param alignment the alignment
     * @throws IllegalArgumentException thrown if <code>alignment</code>
     * isn't a positive power of 2
     */
    public static void validateAlignment(int alignment) {
        if ((alignment <= 0) ||
            (alignment & (alignment - 1)) != 0) {
            throw new IllegalArgumentException("invalid alignment");
        }
    }

    /**
     * Constructs an instance. The file offset is initially unknown.
     *
     * @param name null-ok; the name of this instance, for annotation
     * purposes
     * @param file non-null; file that this instance is part of
     * @param alignment &gt; 0; alignment requirement for the final output;
     * must be a power of 2
     */
    public Section(String name, DexFile file, int alignment) {
        if (file == null) {
            throw new NullPointerException("file == null");
        }

        validateAlignment(alignment);

        this.name = name;
        this.file = file;
        this.alignment = alignment;
        this.fileOffset = -1;
        this.prepared = false;
    }

    /**
     * Gets the file that this instance is part of.
     *
     * @return non-null; the file
     */
    public final DexFile getFile() {
        return file;
    }

    /** 
     * Gets the alignment for this instance's final output.
     * 
     * @return &gt; 0; the alignment
     */
    public final int getAlignment() {
        return alignment;
    }

    /**
     * Gets the offset from the start of the file to this part. This
     * throws an exception if the offset has not yet been set.
     *
     * @return &gt;= 0; the file offset
     */
    public final int getFileOffset() {
        if (fileOffset < 0) {
            throw new RuntimeException("fileOffset not set");
        }

        return fileOffset;
    }

    /**
     * Sets the file offset. It is only valid to call this method once
     * once per instance.
     *
     * @param fileOffset &gt;= 0; the desired offset from the start of the
     * file where this for this instance
     * @return &gt;= 0; the offset that this instance should be placed at
     * in order to meet its alignment constraint
     */
    public final int setFileOffset(int fileOffset) {
        if (fileOffset < 0) {
            throw new IllegalArgumentException("fileOffset < 0");
        }

        if (this.fileOffset >= 0) {
            throw new RuntimeException("fileOffset already set");
        }

        int mask = alignment - 1;
        fileOffset = (fileOffset + mask) & ~mask;

        this.fileOffset = fileOffset;

        return fileOffset;
    }

    /**
     * Writes this instance to the given raw data object.
     *
     * @param out non-null; where to write to
     */
    public final void writeTo(AnnotatedOutput out) {
        throwIfNotPrepared();        
        align(out);

        int cursor = out.getCursor();

        if (fileOffset < 0) {
            fileOffset = cursor;
        } else if (fileOffset != cursor) {
            throw new RuntimeException("alignment mismatch: for " + this +
                                       ", at " + cursor +
                                       ", but expected " + fileOffset);
        }

        if (out.annotates()) {
            if (name != null) {
                out.annotate(0, "\n" + name + ":");
            } else if (cursor != 0) {
                out.annotate(0, "\n");
            }
        }

        writeTo0(out);
    }

    /**
     * Returns the absolute file offset, given an offset from the
     * start of this instance's output. This is only valid to call
     * once this instance has been assigned a file offset (via {@link
     * #setFileOffset}).
     * 
     * @param relative &gt;= 0; the relative offset
     * @return &gt;= 0; the corresponding absolute file offset
     */
    public final int getAbsoluteOffset(int relative) {
        if (relative < 0) {
            throw new IllegalArgumentException("relative < 0");
        }

        if (fileOffset < 0) {
            throw new RuntimeException("fileOffset not yet set");
        }

        return fileOffset + relative;
    }

    /**
     * Returns the absolute file offset of the given item which must
     * be contained in this section. This is only valid to call
     * once this instance has been assigned a file offset (via {@link
     * #setFileOffset}).
     * 
     * <p><b>Note:</b> Subclasses must implement this as appropriate for
     * their contents.</p>
     * 
     * @param item non-null; the item in question
     * @return &gt;= 0; the item's absolute file offset
     */
    public abstract int getAbsoluteItemOffset(Item item);

    /**
     * Prepares this instance for writing. This performs any necessary
     * prerequisites, including particularly adding stuff to other
     * sections. This method may only be called once per instance;
     * subsequent calls will throw an exception.
     */
    public final void prepare() {
        throwIfPrepared();
        prepare0();
        prepared = true;
    }

    /**
     * Gets the collection of all the items in this section.
     * It is not valid to attempt to change the returned list.
     *
     * @return non-null; the items
     */
    public abstract Collection<? extends Item> items();

    /**
     * Does the main work of {@link #prepare}.
     */
    protected abstract void prepare0();

    /**
     * Gets the size of this instance when output, in bytes.
     *
     * @return &gt;= 0; the size of this instance, in bytes
     */
    public abstract int writeSize();

    /**
     * Throws an exception if {@link #prepare} has not been
     * called on this instance.
     */
    protected final void throwIfNotPrepared() {
        if (!prepared) {
            throw new RuntimeException("not prepared");
        }
    }

    /**
     * Throws an exception if {@link #prepare} has already been called
     * on this instance.
     */
    protected final void throwIfPrepared() {
        if (prepared) {
            throw new RuntimeException("already prepared");
        }
    }

    /**
     * Aligns the output of the given data to the alignment of this instance.
     * 
     * @param out non-null; the output to align
     */
    protected final void align(AnnotatedOutput out) {
        out.alignTo(alignment);
    }

    /**
     * Writes this instance to the given raw data object. This gets
     * called by {@link #writeTo} after aligning the cursor of
     * <code>out</code> and verifying that either the assigned file
     * offset matches the actual cursor <code>out</code> or that the
     * file offset was not previously assigned, in which case it gets
     * assigned to <code>out</code>'s cursor.
     * 
     * @param out non-null; where to write to
     */
    protected abstract void writeTo0(AnnotatedOutput out);

    /**
     * Returns the name of this section, for annotation purposes.
     * 
     * @return null-ok; name of this part, for annotation purposes
     */
    protected final String getName() {
        return name;
    }
}
