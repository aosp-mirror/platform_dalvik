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

import com.android.dx.dex.code.DalvOps;

/**
 * Representation of an instruction format, which knows how to decode into
 * and encode from instances of {@link DecodedInstruction}.
 */
public enum InstructionCodec {
    FORMAT_00X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return new DecodedInstruction(this, opcodeUnit, 0, null,
                    0, 0L, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(insn.getOpcodeUnit());
        }
    },

    FORMAT_10X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int literal = byte1(opcodeUnit); // should be zero
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(insn.getOpcodeUnit());
        }
    },

    FORMAT_12X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = nibble2(opcodeUnit);
            int b = nibble3(opcodeUnit);
            return new DecodedInstruction(this, opcode, 0, null,
                    0, 0L, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcodeUnit(),
                             makeByte(insn.getA(), insn.getB())));
        }
    },

    FORMAT_11N() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = nibble2(opcodeUnit);
            int literal = (nibble3(opcodeUnit) << 28) >> 28; // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcodeUnit(),
                             makeByte(insn.getA(), insn.getLiteralNibble())));
        }
    },

    FORMAT_11X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            return new DecodedInstruction(this, opcode, 0, null,
                    0, 0L, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(codeUnit(insn.getOpcode(), insn.getA()));
        }
    },

    FORMAT_10T() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = (byte) byte1(opcodeUnit); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    a, 0L, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(codeUnit(insn.getOpcode(), insn.getTargetByte()));
        }
    },

    FORMAT_20T() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int literal = byte1(opcodeUnit); // should be zero
            int target = (short) in.read(); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    target, literal, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(insn.getOpcodeUnit(), insn.getTargetUnit());
        }
    },

    FORMAT_20BC() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            // Note: We use the literal field to hold the decoded AA value.
            int opcode = byte0(opcodeUnit);
            int literal = byte1(opcodeUnit);
            int index = in.read();
            return new DecodedInstruction(this, opcode,
                    index, IndexType.VARIES,
                    0, literal, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getLiteralByte()),
                    insn.getIndexUnit());
        }
    },

    FORMAT_22X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int b = in.read();
            return new DecodedInstruction(this, opcode, 0, null,
                    0, 0L, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    insn.getBUnit());
        }
    },

    FORMAT_21T() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int target = (short) in.read(); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    target, 0L, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    insn.getTargetUnit());
        }
    },

    FORMAT_21S() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int literal = (short) in.read(); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    insn.getLiteralUnit());
        }
    },

    FORMAT_21H() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int literal = (short) in.read(); // sign-extend

            /*
             * Format 21h decodes differently depending on the opcode,
             * because the "signed hat" might represent either a 32-
             * or 64- bit value.
             */
            literal <<= (opcode == DalvOps.CONST_HIGH16) ? 16 : 48;

            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            // See above.
            int opcode = insn.getOpcode();
            int shift = (opcode == DalvOps.CONST_HIGH16) ? 16 : 48;
            short literal = (short) (insn.getLiteral() >> shift);

            out.write(codeUnit(opcode, insn.getA()), literal);
        }
    },

    FORMAT_21C() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int index = in.read();
            IndexType indexType = OpcodeInfo.getIndexType(opcode);
            return new DecodedInstruction(this, opcode, index, indexType,
                    0, 0L, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    insn.getLiteralUnit());
        }
    },

    FORMAT_23X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int bc = in.read();
            int b = byte0(bc);
            int c = byte1(bc);
            return new DecodedInstruction(this, opcode, 0, null,
                    0, 0L, null,
                    3, a, b, c, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    codeUnit(insn.getB(), insn.getC()));
        }
    },

    FORMAT_22B() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int bc = in.read();
            int b = byte0(bc);
            int literal = (byte) byte1(bc); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    codeUnit(insn.getB(),
                             insn.getLiteralByte()));
        }
    },

    FORMAT_22T() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = nibble2(opcodeUnit);
            int b = nibble3(opcodeUnit);
            int target = (short) in.read(); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    target, 0L, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(),
                             makeByte(insn.getA(), insn.getB())),
                    insn.getTargetUnit());
        }
    },

    FORMAT_22S() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = nibble2(opcodeUnit);
            int b = nibble3(opcodeUnit);
            int literal = (short) in.read(); // sign-extend
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(),
                             makeByte(insn.getA(), insn.getB())),
                    insn.getLiteralUnit());
        }
    },

    FORMAT_22C() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = nibble2(opcodeUnit);
            int b = nibble3(opcodeUnit);
            int index = in.read();
            IndexType indexType = OpcodeInfo.getIndexType(opcode);
            return new DecodedInstruction(this, opcode, index, indexType,
                    0, 0L, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(),
                             makeByte(insn.getA(), insn.getB())),
                    insn.getIndexUnit());
        }
    },

    FORMAT_22CS() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = nibble2(opcodeUnit);
            int b = nibble3(opcodeUnit);
            int index = in.read();
            return new DecodedInstruction(this, opcode,
                    index, IndexType.FIELD_OFFSET,
                    0, 0L, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    codeUnit(insn.getOpcode(),
                             makeByte(insn.getA(), insn.getB())),
                    insn.getIndexUnit());
        }
    },

    FORMAT_30T() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int literal = byte1(opcodeUnit); // should be zero
            int target = in.readInt();
            return new DecodedInstruction(this, opcode, 0, null,
                    target, literal, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int target = insn.getTarget();
            out.write(insn.getOpcodeUnit(), unit0(target), unit1(target));
        }
    },

    FORMAT_32X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int literal = byte1(opcodeUnit); // should be zero
            int a = in.read();
            int b = in.read();
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(insn.getOpcodeUnit(), insn.getAUnit(), insn.getBUnit());
        }
    },

    FORMAT_31I() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int literal = in.readInt();
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int literal = insn.getLiteralInt();
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    unit0(literal),
                    unit1(literal));
        }
    },

    FORMAT_31T() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int target = in.readInt();
            return new DecodedInstruction(this, opcode, 0, null,
                    target, 0L, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int target = insn.getTarget();
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    unit0(target),
                    unit1(target));
        }
    },

    FORMAT_31C() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            int index = in.readInt();
            IndexType indexType = OpcodeInfo.getIndexType(opcode);
            return new DecodedInstruction(this, opcode, index, indexType,
                    0, 0L, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int index = insn.getIndex();
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    unit0(index),
                    unit1(index));
        }
    },

    FORMAT_35C() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return decodeRegisterList(this, opcodeUnit, in);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            encodeRegisterList(insn, out);
        }
    },

    FORMAT_35MS() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return decodeRegisterList(this, opcodeUnit, in);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            encodeRegisterList(insn, out);
        }
    },

    FORMAT_35MI() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return decodeRegisterList(this, opcodeUnit, in);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            encodeRegisterList(insn, out);
        }
    },

    FORMAT_3RC() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return decodeRegisterRange(this, opcodeUnit, in);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            encodeRegisterRange(insn, out);
        }
    },

    FORMAT_3RMS() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return decodeRegisterRange(this, opcodeUnit, in);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            encodeRegisterRange(insn, out);
        }
    },

    FORMAT_3RMI() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            return decodeRegisterRange(this, opcodeUnit, in);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            encodeRegisterRange(insn, out);
        }
    },

    FORMAT_51L() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int opcode = byte0(opcodeUnit);
            int a = byte1(opcodeUnit);
            long literal = in.readLong();
            return new DecodedInstruction(this, opcode, 0, null,
                    0, literal, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int literal = insn.getLiteralInt();
            out.write(
                    codeUnit(insn.getOpcode(), insn.getA()),
                    unit0(literal),
                    unit1(literal),
                    unit2(literal),
                    unit3(literal));
        }
    },

    FORMAT_33X() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int ab = in.read();
            int a = byte0(ab);
            int b = byte1(ab);
            int c = in.read();
            return new DecodedInstruction(this, opcodeUnit, 0, null,
                    0, 0L, null,
                    3, a, b, c, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    insn.getOpcodeUnit(),
                    codeUnit(insn.getA(), insn.getB()),
                    insn.getCUnit());
        }
    },

    FORMAT_32S() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int ab = in.read();
            int a = byte0(ab);
            int b = byte1(ab);
            int literal = (short) in.read(); // sign-extend
            return new DecodedInstruction(this, opcodeUnit, 0, null,
                    0, literal, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            out.write(
                    insn.getOpcodeUnit(),
                    codeUnit(insn.getA(), insn.getB()),
                    insn.getLiteralUnit());
        }
    },

    FORMAT_40SC() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            // Note: We use the literal field to hold the decoded AA value.
            int index = in.readInt();
            int literal = in.read();
            return new DecodedInstruction(this, opcodeUnit,
                    index, IndexType.VARIES,
                    0, literal, null,
                    0, 0, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int index = insn.getIndex();
            out.write(
                    insn.getOpcodeUnit(),
                    unit0(index),
                    unit1(index),
                    insn.getLiteralUnit());
        }
    },

    FORMAT_41C() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int index = in.readInt();
            int a = in.read();
            IndexType indexType = OpcodeInfo.getIndexType(opcodeUnit);
            return new DecodedInstruction(this, opcodeUnit, index, indexType,
                    0, 0L, null,
                    1, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int index = insn.getIndex();
            out.write(
                    insn.getOpcodeUnit(),
                    unit0(index),
                    unit1(index),
                    insn.getAUnit());
        }
    },

    FORMAT_52C() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int index = in.readInt();
            int a = in.read();
            int b = in.read();
            IndexType indexType = OpcodeInfo.getIndexType(opcodeUnit);
            return new DecodedInstruction(this, opcodeUnit, index, indexType,
                    0, 0L, null,
                    2, a, b, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int index = insn.getIndex();
            out.write(
                    insn.getOpcodeUnit(),
                    unit0(index),
                    unit1(index),
                    insn.getAUnit(),
                    insn.getBUnit());
        }
    },

    FORMAT_5RC() {
        @Override public DecodedInstruction decode(int opcodeUnit,
                CodeInput in) {
            int index = in.readInt();
            int registerCount = in.read();
            int a = in.read();
            IndexType indexType = OpcodeInfo.getIndexType(opcodeUnit);
            return new DecodedInstruction(this, opcodeUnit, index, indexType,
                    0, 0L, null,
                    registerCount, a, 0, 0, 0, 0);
        }

        @Override public void encode(DecodedInstruction insn, CodeOutput out) {
            int index = insn.getIndex();
            out.write(
                    insn.getOpcodeUnit(),
                    unit0(index),
                    unit1(index),
                    insn.getRegisterCountUnit(),
                    insn.getAUnit());
        }
    };

    /**
     * Decodes an instruction specified by the given opcode unit, reading
     * any required additional code units from the given input source.
     */
    public abstract DecodedInstruction decode(int opcodeUnit, CodeInput in);

    /**
     * Encodes the given instruction.
     */
    public abstract void encode(DecodedInstruction insn, CodeOutput out);

    /**
     * Helper method that decodes any of the register-list formats.
     */
    private static DecodedInstruction decodeRegisterList(
            InstructionCodec format, int opcodeUnit, CodeInput in) {
        int opcode = byte0(opcodeUnit);
        int e = nibble2(opcodeUnit);
        int registerCount = nibble3(opcodeUnit);
        int index = in.read();
        int abcd = in.read();
        int a = nibble0(abcd);
        int b = nibble1(abcd);
        int c = nibble2(abcd);
        int d = nibble3(abcd);
        IndexType indexType = OpcodeInfo.getIndexType(opcode);
        return new DecodedInstruction(format, opcode, index, indexType,
                0, 0L, null,
                registerCount, a, b, c, d, e);
    }

    /**
     * Helper method that encodes any of the register-list formats.
     */
    private static void encodeRegisterList(DecodedInstruction insn,
            CodeOutput out) {
        out.write(codeUnit(insn.getOpcode(),
                        makeByte(insn.getE(), insn.getRegisterCount())),
                insn.getIndexUnit(),
                codeUnit(insn.getA(), insn.getB(), insn.getC(), insn.getD()));
    }

    /**
     * Helper method that decodes any of the three-unit register-range formats.
     */
    private static DecodedInstruction decodeRegisterRange(
            InstructionCodec format, int opcodeUnit, CodeInput in) {
        int opcode = byte0(opcodeUnit);
        int registerCount = byte1(opcodeUnit);
        int index = in.read();
        int a = in.read();
        IndexType indexType = OpcodeInfo.getIndexType(opcode);
        return new DecodedInstruction(format, opcode, index, indexType,
                0, 0L, null,
                registerCount, a, 0, 0, 0, 0);
    }

    /**
     * Helper method that encodes any of the three-unit register-range formats.
     */
    private static void encodeRegisterRange(DecodedInstruction insn,
            CodeOutput out) {
        out.write(codeUnit(insn.getOpcode(), insn.getRegisterCount()),
                insn.getIndexUnit(),
                insn.getAUnit());
    }

    private static short codeUnit(int lowByte, int highByte) {
        if ((lowByte & ~0xff) != 0) {
            throw new IllegalArgumentException("bogus lowByte");
        }

        if ((highByte & ~0xff) != 0) {
            throw new IllegalArgumentException("bogus highByte");
        }

        return (short) (lowByte | (highByte << 8));
    }

    private static short codeUnit(int nibble0, int nibble1, int nibble2,
            int nibble3) {
        if ((nibble0 & ~0xf) != 0) {
            throw new IllegalArgumentException("bogus nibble0");
        }

        if ((nibble1 & ~0xf) != 0) {
            throw new IllegalArgumentException("bogus nibble1");
        }

        if ((nibble2 & ~0xf) != 0) {
            throw new IllegalArgumentException("bogus nibble2");
        }

        if ((nibble3 & ~0xf) != 0) {
            throw new IllegalArgumentException("bogus nibble3");
        }

        return (short) (nibble0 | (nibble1 << 4)
                | (nibble2 << 8) | (nibble3 << 12));
    }

    private static int makeByte(int lowNibble, int highNibble) {
        if ((lowNibble & ~0xf) != 0) {
            throw new IllegalArgumentException("bogus lowNibble");
        }

        if ((highNibble & ~0xf) != 0) {
            throw new IllegalArgumentException("bogus highNibble");
        }

        return lowNibble | (highNibble << 4);
    }

    private static short unit0(int value) {
        return (short) value;
    }

    private static short unit1(int value) {
        return (short) (value >> 16);
    }

    private static short unit0(long value) {
        return (short) value;
    }

    private static short unit1(long value) {
        return (short) (value >> 16);
    }

    private static short unit2(long value) {
        return (short) (value >> 32);
    }

    private static short unit3(long value) {
        return (short) (value >> 48);
    }

    private static int byte0(int value) {
        return value & 0xff;
    }

    private static int byte1(int value) {
        return (value >> 8) & 0xff;
    }

    private static int byte2(int value) {
        return (value >> 16) & 0xff;
    }

    private static int byte3(int value) {
        return value >>> 24;
    }

    private static int nibble0(int value) {
        return value & 0xf;
    }

    private static int nibble1(int value) {
        return (value >> 4) & 0xf;
    }

    private static int nibble2(int value) {
        return (value >> 8) & 0xf;
    }

    private static int nibble3(int value) {
        return (value >> 12) & 0xf;
    }
}
