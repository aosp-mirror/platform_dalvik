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

import java.io.EOFException;

/**
 * A decoded Dalvik instruction. This consists of a format codec, a
 * numeric opcode, an optional index type, and any additional
 * arguments of the instruction. The additional arguments (if any) are
 * represented as uninterpreted data.
 *
 * <p><b>Note:</b> The names of the arguments are <i>not</i> meant to
 * match the names given in the Dalvik instruction format
 * specification, specification which just names fields (somewhat)
 * arbitrarily alphabetically from A. In this class, non-register
 * fields are given descriptive names and register fields are
 * consistently named alphabetically.</p>
 */
public final class DecodedInstruction {
    /** non-null; instruction format / codec */
    private final InstructionCodec format;

    /** opcode number */
    private final int opcode;

    /** constant index argument */
    private final int index;

    /** null-ok; index type */
    private final IndexType indexType;

    /**
     * target address argument. This is an absolute address, not just a
     * signed offset.
     */
    private final int target;

    /**
     * literal value argument; also used for special verification error
     * constants (formats 20bc and 40sc) as well as should-be-zero values
     * (formats 10x, 20t, 30t, and 32x)
     */
    private final long literal;

    /** null-ok; literal data */
    private final short[] data;

    /** register count */
    private final int registerCount;

    /** argument "A"; always a register number */
    private final int a;

    /** argument "B"; always a register number */
    private final int b;

    /** argument "C"; always a register number */
    private final int c;

    /** argument "D"; always a register number */
    private final int d;

    /** argument "E"; always a register number */
    private final int e;

    /**
     * Decodes an instruction from the given input source.
     */
    public static DecodedInstruction decode(CodeInput in) throws EOFException {
        int opcodeUnit = in.read();
        int opcode = Opcodes.extractOpcodeFromUnit(opcodeUnit);
        InstructionCodec format = OpcodeInfo.getFormat(opcode);

        return format.decode(opcodeUnit, in);
    }

    /**
     * Constructs an instance. This is the base constructor that takes
     * all arguments.
     */
    public DecodedInstruction(InstructionCodec format, int opcode,
            int index, IndexType indexType, int target, long literal,
            short[] data,
            int registerCount, int a, int b, int c, int d, int e) {
        if (format == null) {
            throw new NullPointerException("format == null");
        }

        if (!Opcodes.isValidShape(opcode)) {
            throw new IllegalArgumentException("invalid opcode");
        }

        this.format = format;
        this.opcode = opcode;
        this.index = index;
        this.indexType = indexType;
        this.target = target;
        this.literal = literal;
        this.data = data;
        this.registerCount = registerCount;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    public InstructionCodec getFormat() {
        return format;
    }

    public int getOpcode() {
        return opcode;
    }

    /**
     * Gets the opcode, as a code unit.
     */
    public short getOpcodeUnit() {
        return (short) opcode;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Gets the index, as a code unit.
     */
    public short getIndexUnit() {
        return (short) index;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public int getTarget() {
        return target;
    }

    /**
     * Gets the target, as a code unit.
     */
    public short getTargetUnit() {
        return (short) target;
    }

    /**
     * Gets the target, masked to be a byte in size.
     */
    public int getTargetByte() {
        return target & 0xff;
    }

    public long getLiteral() {
        return literal;
    }

    /**
     * Gets the literal value, masked to be an int in size.
     */
    public int getLiteralInt() {
        return (int) literal;
    }

    /**
     * Gets the literal value, as a code unit.
     */
    public short getLiteralUnit() {
        return (short) literal;
    }

    /**
     * Gets the literal value, masked to be a byte in size.
     */
    public int getLiteralByte() {
        return (int) literal & 0xff;
    }

    /**
     * Gets the literal value, masked to be a nibble in size.
     */
    public int getLiteralNibble() {
        return (int) literal & 0xf;
    }

    public short[] getData() {
        return data;
    }

    public int getRegisterCount() {
        return registerCount;
    }

    /**
     * Gets the register count, as a code unit.
     */
    public short getRegisterCountUnit() {
        return (short) registerCount;
    }

    public int getA() {
        return a;
    }

    public short getAUnit() {
        return (short) a;
    }

    public int getB() {
        return b;
    }

    public short getBUnit() {
        return (short) b;
    }

    public int getC() {
        return c;
    }

    public short getCUnit() {
        return (short) b;
    }

    public int getD() {
        return d;
    }

    public int getE() {
        return e;
    }

    /**
     * Encodes this instance to the given output.
     */
    public void encode(CodeOutput out) {
        format.encode(this, out);
    }

    /**
     * Returns an instance just like this one, except with the index replaced
     * with the given one.
     */
    public DecodedInstruction withIndex(int newIndex) {
        return new DecodedInstruction(format, opcode, newIndex, indexType,
                target, literal, data,
                registerCount, a, b, c, d, e);
    }
}
