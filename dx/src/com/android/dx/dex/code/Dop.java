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

package com.android.dx.dex.code;

/**
 * Representation of an opcode.
 */
public final class Dop {
    /**
     * {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the opcode value
     * itself
     */
    private final int opcode;

    /**
     * {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the opcode family
     */
    private final int family;

    /**
     * {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} what opcode (by
     * number) to try next when attempting to match an opcode to
     * particular arguments; {@code DalvOps.NO_NEXT} to indicate that
     * this is the last opcode to try in a particular chain
     */
    private final int nextOpcode;

    /** {@code non-null;} the instruction format */
    private final InsnFormat format;

    /** whether this opcode uses a result register */
    private final boolean hasResult;

    /** {@code non-null;} the name */
    private final String name;

    /**
     * Constructs an instance.
     *
     * @param opcode {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the opcode
     * value itself
     * @param family {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the
     * opcode family
     * @param nextOpcode {@code DalvOps.NO_NEXT..DalvOps.MAX_VALUE;}
     * what opcode (by number) to try next when attempting to match an
     * opcode to particular arguments; {@code DalvOps.NO_NEXT} to
     * indicate that this is the last opcode to try in a particular
     * chain
     * @param format {@code non-null;} the instruction format
     * @param hasResult whether the opcode has a result register; if so it
     * is always the first register
     * @param name {@code non-null;} the name
     */
    public Dop(int opcode, int family, int nextOpcode, InsnFormat format,
            boolean hasResult, String name) {
        if ((opcode < DalvOps.MIN_VALUE) || (opcode > DalvOps.MAX_VALUE)) {
            throw new IllegalArgumentException("bogus opcode");
        }

        if ((family < DalvOps.MIN_VALUE) || (family > DalvOps.MAX_VALUE)) {
            throw new IllegalArgumentException("bogus family");
        }

        if ((nextOpcode < DalvOps.MIN_VALUE)
                || (nextOpcode > DalvOps.MAX_VALUE)) {
            throw new IllegalArgumentException("bogus nextOpcode");
        }

        if (format == null) {
            throw new NullPointerException("format == null");
        }

        if (name == null) {
            throw new NullPointerException("name == null");
        }

        this.opcode = opcode;
        this.family = family;
        this.nextOpcode = nextOpcode;
        this.format = format;
        this.hasResult = hasResult;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the opcode value.
     *
     * @return {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the opcode value
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Gets the opcode family. The opcode family is the unmarked (no
     * "/...") opcode that has equivalent semantics to this one.
     *
     * @return {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the opcode family
     */
    public int getFamily() {
        return family;
    }

    /**
     * Gets the instruction format.
     *
     * @return {@code non-null;} the instruction format
     */
    public InsnFormat getFormat() {
        return format;
    }

    /**
     * Returns whether this opcode uses a result register.
     *
     * @return {@code true} iff this opcode uses a result register
     */
    public boolean hasResult() {
        return hasResult;
    }

    /**
     * Gets the opcode name.
     *
     * @return {@code non-null;} the opcode name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the opcode value to try next when attempting to match an
     * opcode to particular arguments. This returns {@code
     * DalvOps.NO_NEXT} to indicate that this is the last opcode to
     * try in a particular chain.
     *
     * @return {@code DalvOps.MIN_VALUE..DalvOps.MAX_VALUE;} the opcode value
     */
    public int getNextOpcode() {
        return nextOpcode;
    }

    /**
     * Gets the opcode for the opposite test of this instance. This is only
     * valid for opcodes which are in fact tests.
     *
     * @return {@code non-null;} the opposite test
     */
    public Dop getOppositeTest() {
        switch (opcode) {
            case DalvOps.IF_EQ:  return Dops.IF_NE;
            case DalvOps.IF_NE:  return Dops.IF_EQ;
            case DalvOps.IF_LT:  return Dops.IF_GE;
            case DalvOps.IF_GE:  return Dops.IF_LT;
            case DalvOps.IF_GT:  return Dops.IF_LE;
            case DalvOps.IF_LE:  return Dops.IF_GT;
            case DalvOps.IF_EQZ: return Dops.IF_NEZ;
            case DalvOps.IF_NEZ: return Dops.IF_EQZ;
            case DalvOps.IF_LTZ: return Dops.IF_GEZ;
            case DalvOps.IF_GEZ: return Dops.IF_LTZ;
            case DalvOps.IF_GTZ: return Dops.IF_LEZ;
            case DalvOps.IF_LEZ: return Dops.IF_GTZ;
        }

        throw new IllegalArgumentException("bogus opcode: " + this);
    }
}
