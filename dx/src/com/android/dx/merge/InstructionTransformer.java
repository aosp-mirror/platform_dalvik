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

package com.android.dx.merge;

import com.android.dx.dex.DexException;
import java.util.BitSet;

/**
 * Adjusts a block of instructions to a new index.
 */
public final class InstructionTransformer {

    private static final Instruction[] INSTRUCTIONS = new Instruction[] {
            // 0x00...0x0f
            new Instruction(1, "nop"),
            new Instruction(1, "move vA, vB"),
            new Instruction(2, "move/from vAA, vBBBB"),
            new Instruction(3, "move/16 vAAAA, vBBBB"),
            new Instruction(1, "move-wide, vA, vB"),
            new Instruction(2, "move-wide/from16 vAA, vBBBB"),
            new Instruction(3, "move-wide/from16 vAAAA, vBBBB"),
            new Instruction(1, "move-object vA, vB"),
            new Instruction(2, "move-object/from16 vAA, vBBBB"),
            new Instruction(3, "move-object/16 vAAAA, vBBBB"),
            new Instruction(1, "move-result vAA"),
            new Instruction(1, "move-result-wide vAA"),
            new Instruction(1, "move-result-object vAA"),
            new Instruction(1, "move-exception vAA"),
            new Instruction(1, "return void"),
            new Instruction(1, "return vAA"),

            // 0x10...0x1f
            new Instruction(1, "return-wide vAA"),
            new Instruction(1, "return-object vAA"),
            new Instruction(1, "const/4 vA, #+B"),
            new Instruction(2, "const/16 vAA, #+BBBB"),
            new Instruction(3, "const vAA, #+BBBBBBBB"),
            new Instruction(2, "const/high16 vAA, #+BBBB0000"),
            new Instruction(2, "const-wide/16 vAA, #+BBBB"),
            new Instruction(3, "const-wide/32 vAA, #+BBBBBBBB"),
            new Instruction(5, "const-wide vAA, #+BBBBBBBBBBBBBBBB"),
            new Instruction(2, "const-wide/high16 vAA, #+BBBB000000000000"),
            new StringInstruction(2, "const-string vAA, string@BBBB"),
            new JumboStringInstruction(3, "const-string/jumbo vAA, string@BBBBBBBB"),
            new TypeInstruction(2, "const-class vAA, type@BBBB"),
            new Instruction(1, "monitor-enter vAA"),
            new Instruction(1, "monitor-exit vAA"),
            new TypeInstruction(2, "check-cast vAA type@BBBB"),

            // 0x20...0x2f
            new TypeInstruction(2, "instance-of vA, vB, type@CCCC"),
            new Instruction(1, "array-length vA, vB"),
            new TypeInstruction(2, "new-instance vAA, type@BBBB"),
            new TypeInstruction(2, "new-array vA, vB, type@CCCC"),
            new TypeInstruction(3, "filled-new-array {vD, vE, vF, vG, vA}, type@CCCC"),
            new TypeInstruction(3, "filled-new-array/range {vCCCC..vNNNN}, type@BBBB"),
            new FillArrayInstruction(3, "fill-array-data vAA, +BBBBBBBB"),
            new Instruction(1, "throw vAA"),
            new Instruction(1, "goto +AA"),
            new Instruction(2, "goto/16 +AAAA"),
            new Instruction(3, "goto/32 +AAAAAAAA"),
            new PackedSwitchInstruction(3, "packed-switch vAA, +BBBBBBBB"),
            new SparseSwitchInstruction(3, "sparse-switch vAA, +BBBBBBBB"),
            new Instruction(2, "cmpl-float vAA, vBB, vCC"),
            new Instruction(2, "cmpg-float vAA, vBB, vCC"),
            new Instruction(2, "cmpl-double vAA, vBB, vCC"),

            // 0x30...0x3f
            new Instruction(2, "cmpg-double vAA, vBB, vCC"),
            new Instruction(2, "cmp-long vAA, vBB, vCC"),
            new Instruction(2, "if-eq vA, vB, +CCCC"),
            new Instruction(2, "if-ne vA, vB, +CCCC"),
            new Instruction(2, "if-lt vA, vB, +CCCC"),
            new Instruction(2, "if-ge vA, vB, +CCCC"),
            new Instruction(2, "if-gt vA, vB, +CCCC"),
            new Instruction(2, "if-le vA, vB, +CCCC"),
            new Instruction(2, "if-eqz vAA, +BBBB"),
            new Instruction(2, "if-nez vAA, +BBBB"),
            new Instruction(2, "if-ltz vAA, +BBBB"),
            new Instruction(2, "if-gez vAA, +BBBB"),
            new Instruction(2, "if-gtz vAA, +BBBB"),
            new Instruction(2, "if-lez vAA, +BBBB"),
            new UnusedInstruction(),
            new UnusedInstruction(),

            // 0x40...0x4f
            new UnusedInstruction(),
            new UnusedInstruction(),
            new UnusedInstruction(),
            new UnusedInstruction(),
            new Instruction(2, "aget vAA, vBB, vCC"),
            new Instruction(2, "aget-wide vAA, vBB, vCC"),
            new Instruction(2, "aget-object vAA, vBB, vCC"),
            new Instruction(2, "aget-boolean vAA, vBB, vCC"),
            new Instruction(2, "aget-byte vAA, vBB, vCC"),
            new Instruction(2, "aget-char vAA, vBB, vCC"),
            new Instruction(2, "aget-short vAA, vBB, vCC"),
            new Instruction(2, "aput vAA, vBB, vCC"),
            new Instruction(2, "aput-wide vAA, vBB, vCC"),
            new Instruction(2, "aput-object vAA, vBB, vCC"),
            new Instruction(2, "aput-boolean vAA, vBB, vCC"),
            new Instruction(2, "aput-byte vAA, vBB, vCC"),

            // 0x50...0x5f
            new Instruction(2, "aput-char vAA, vBB, vCC"),
            new Instruction(2, "aput-short vAA, vBB, vCC"),
            new FieldInstruction(2, "iget vA, vB, field@CCCC"),
            new FieldInstruction(2, "iget-wide vA, vB, field@CCCC"),
            new FieldInstruction(2, "iget-object vA, vB, field@CCCC"),
            new FieldInstruction(2, "iget-boolean vA, vB, field@CCCC"),
            new FieldInstruction(2, "iget-byte vA, vB, field@CCCC"),
            new FieldInstruction(2, "iget-char vA, vB, field@CCCC"),
            new FieldInstruction(2, "iget-short vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput-wide vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput-object vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput-boolean vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput-byte vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput-char vA, vB, field@CCCC"),
            new FieldInstruction(2, "iput-short vA, vB, field@CCCC"),

            // 0x60...0x6f
            new FieldInstruction(2, "sget vAA, field@BBBB"),
            new FieldInstruction(2, "sget-wide vAA, field@BBBB"),
            new FieldInstruction(2, "sget-object vAA, field@BBBB"),
            new FieldInstruction(2, "sget-boolean vAA, field@BBBB"),
            new FieldInstruction(2, "sget-byte vAA, field@BBBB"),
            new FieldInstruction(2, "sget-char vAA, field@BBBB"),
            new FieldInstruction(2, "sget-short vAA, field@BBBB"),
            new FieldInstruction(2, "sput vAA, field@BBBB"),
            new FieldInstruction(2, "sput-wide vAA, field@BBBB"),
            new FieldInstruction(2, "sput-object vAA, field@BBBB"),
            new FieldInstruction(2, "sput-boolean vAA, field@BBBB"),
            new FieldInstruction(2, "sput-byte vAA, field@BBBB"),
            new FieldInstruction(2, "sput-char vAA, field@BBBB"),
            new FieldInstruction(2, "sput-short vAA, field@BBBB"),
            new MethodInstruction(3, "invoke-virtual {vD, vE, vF, vG, vA}, meth@CCCC"),
            new MethodInstruction(3, "invoke-super {vD, vE, vF, vG, vA}, meth@CCCC"),

            // 0x70...0x7f
            new MethodInstruction(3, "invoke-direct {vD, vE, vF, vG, vA}, meth@CCCC"),
            new MethodInstruction(3, "invoke-static {vD, vE, vF, vG, vA}, meth@CCCC"),
            new MethodInstruction(3, "invoke-interface {vD, vE, vF, vG, vA}, meth@CCCC"),
            new UnusedInstruction(),
            new MethodInstruction(3, "invoke-virtual/range {vCCCC..vNNNN}, meth@BBBB"),
            new MethodInstruction(3, "invoke-super/range {vCCCC..vNNNN}, meth@BBBB"),
            new MethodInstruction(3, "invoke-direct/range {vCCCC..vNNNN}, meth@BBBB"),
            new MethodInstruction(3, "invoke-static/range {vCCCC..vNNNN}, meth@BBBB"),
            new MethodInstruction(3, "invoke-interface/range {vCCCC..vNNNN}, meth@BBBB"),
            new UnusedInstruction(),
            new UnusedInstruction(),
            new Instruction(1, "neg-int vA, vB"),
            new Instruction(1, "not-int vA, vB"),
            new Instruction(1, "neg-long vA, vB"),
            new Instruction(1, "not-long vA, vB"),
            new Instruction(1, "neg-float vA, vB"),

            // 0x80...0x8f
            new Instruction(1, "neg-double vA, vB"),
            new Instruction(1, "int-to-long vA, vB"),
            new Instruction(1, "int-to-float vA, vB"),
            new Instruction(1, "int-to-double vA, vB"),
            new Instruction(1, "long-to-int vA, vB"),
            new Instruction(1, "long-to-float vA, vB"),
            new Instruction(1, "long-to-double vA, vB"),
            new Instruction(1, "float-to-int vA, vB"),
            new Instruction(1, "float-to-long vA, vB"),
            new Instruction(1, "float-to-double vA, vB"),
            new Instruction(1, "double-to-int vA, vB"),
            new Instruction(1, "double-to-long vA, vB"),
            new Instruction(1, "double-to-float vA, vB"),
            new Instruction(1, "int-to-byte vA, vB"),
            new Instruction(1, "int-to-char vA, vB"),
            new Instruction(1, "int-to-short vA, vB"),

            // 0x90...0x9f
            new Instruction(2, "add-int vAA, vBB, vCC"),
            new Instruction(2, "sub-int vAA, vBB, vCC"),
            new Instruction(2, "mul-int vAA, vBB, vCC"),
            new Instruction(2, "div-int vAA, vBB, vCC"),
            new Instruction(2, "rem-int vAA, vBB, vCC"),
            new Instruction(2, "and-int vAA, vBB, vCC"),
            new Instruction(2, "or-int vAA, vBB, vCC"),
            new Instruction(2, "xor-int vAA, vBB, vCC"),
            new Instruction(2, "shl-int vAA, vBB, vCC"),
            new Instruction(2, "shr-int vAA, vBB, vCC"),
            new Instruction(2, "ushr-int vAA, vBB, vCC"),
            new Instruction(2, "add-long vAA, vBB, vCC"),
            new Instruction(2, "sub-long vAA, vBB, vCC"),
            new Instruction(2, "mul-long vAA, vBB, vCC"),
            new Instruction(2, "div-long vAA, vBB, vCC"),
            new Instruction(2, "rem-long vAA, vBB, vCC"),

            // 0xa0...0xaf
            new Instruction(2, "and-long vAA, vBB, vCC"),
            new Instruction(2, "or-long vAA, vBB, vCC"),
            new Instruction(2, "xor-long vAA, vBB, vCC"),
            new Instruction(2, "shl-long vAA, vBB, vCC"),
            new Instruction(2, "shr-long vAA, vBB, vCC"),
            new Instruction(2, "ushr-long vAA, vBB, vCC"),
            new Instruction(2, "add-float vAA, vBB, vCC"),
            new Instruction(2, "sub-float vAA, vBB, vCC"),
            new Instruction(2, "mul-float vAA, vBB, vCC"),
            new Instruction(2, "div-float vAA, vBB, vCC"),
            new Instruction(2, "rem-float vAA, vBB, vCC"),
            new Instruction(2, "add-double vAA, vBB, vCC"),
            new Instruction(2, "sub-double vAA, vBB, vCC"),
            new Instruction(2, "mul-double vAA, vBB, vCC"),
            new Instruction(2, "div-double vAA, vBB, vCC"),
            new Instruction(2, "rem-double vAA, vBB, vCC"),

            // 0xb0..0xbf
            new Instruction(1, "add-int/2addr vA, vB"),
            new Instruction(1, "sub-int/2addr vA, vB"),
            new Instruction(1, "mul-int/2addr vA, vB"),
            new Instruction(1, "div-int/2addr vA, vB"),
            new Instruction(1, "rem-int/2addr vA, vB"),
            new Instruction(1, "and-int/2addr vA, vB"),
            new Instruction(1, "or-int/2addr vA, vB"),
            new Instruction(1, "xor-int/2addr vA, vB"),
            new Instruction(1, "shl-int/2addr vA, vB"),
            new Instruction(1, "shr-int/2addr vA, vB"),
            new Instruction(1, "ushr-int/2addr vA, vB"),
            new Instruction(1, "add-long/2addr vA, vB"),
            new Instruction(1, "sub-long/2addr vA, vB"),
            new Instruction(1, "mul-long/2addr vA, vB"),
            new Instruction(1, "div-long/2addr vA, vB"),
            new Instruction(1, "rem-long/2addr vA, vB"),

            // 0xc0...0xcf
            new Instruction(1, "and-long/2addr vA, vB"),
            new Instruction(1, "or-long/2addr vA, vB"),
            new Instruction(1, "xor-long/2addr vA, vB"),
            new Instruction(1, "shl-long/2addr vA, vB"),
            new Instruction(1, "shr-long/2addr vA, vB"),
            new Instruction(1, "ushr-long/2addr vA, vB"),
            new Instruction(1, "add-float/2addr vA, vB"),
            new Instruction(1, "sub-float/2addr vA, vB"),
            new Instruction(1, "mul-float/2addr vA, vB"),
            new Instruction(1, "div-float/2addr vA, vB"),
            new Instruction(1, "rem-float/2addr vA, vB"),
            new Instruction(1, "add-double/2addr vA, vB"),
            new Instruction(1, "sub-double/2addr vA, vB"),
            new Instruction(1, "mul-double/2addr vA, vB"),
            new Instruction(1, "div-double/2addr vA, vB"),
            new Instruction(1, "rem-double/2addr vA, vB"),

            // 0xd0...0xdf
            new Instruction(2, "add-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "rsub-int (reverse subtract) vA, vB, #+CCCC"),
            new Instruction(2, "mul-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "div-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "rem-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "and-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "or-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "xor-int/lit16 vA, vB, #+CCCC"),
            new Instruction(2, "add-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "rsub-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "mul-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "div-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "rem-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "and-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "or-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "xor-int/lit8 vAA, vBB, #+CC"),

            // 0xe0...0xef
            new Instruction(2, "shl-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "shr-int/lit8 vAA, vBB, #+CC"),
            new Instruction(2, "ushr-int/lit8 vAA, vBB, #+CC"),
    };

    private final IndexMap indexMap;

    public InstructionTransformer(IndexMap indexMap) {
        this.indexMap = indexMap;
    }

    public short[] transform(short[] instructions) throws DexException {
        instructions = instructions.clone();
        BitSet skippedInstructions = new BitSet();

        for (int i = 0; i < instructions.length; ) {
            if (skippedInstructions.get(i)) {
                i++;
                continue;
            }

            int index = instructions[i] & 0xFF;
            if (index < 0 || index >= INSTRUCTIONS.length) {
                throw new DexException("Unhandled instruction at " + i
                        + ": " + Integer.toHexString(index));
            }

            Instruction instruction = INSTRUCTIONS[index];
            instruction.transform(instructions, i, indexMap, skippedInstructions);
            i += instruction.codeUnits;
        }

        return instructions;
    }

    private static class Instruction {
        private final String name;
        private final int codeUnits;
        Instruction(int codeUnits, String name) {
            this.name = name;
            this.codeUnits = codeUnits;
        }

        public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {}

        @Override public String toString() {
            return name;
        }
    }

    private static class UnusedInstruction extends Instruction {
        UnusedInstruction() {
            super(1, "unused");
        }
    }

    private static class StringInstruction extends Instruction {
        StringInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            int stringIndex = instructions[i + 1] & 0xFFFF;
            int mappedIndex = indexMap.stringIds[stringIndex];
            if (mappedIndex > 0xFFFF) {
                throw new DexException("Cannot convert string to jumbo string!");
            }
            instructions[i + 1] = (short) mappedIndex;
        }
    }

    private static class JumboStringInstruction extends Instruction {
        JumboStringInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            throw new UnsupportedOperationException("Jumbo strings not implemented. "
                    + "Due to a lack of dex files requiring jumbo strings, this class doesn't "
                    + "bother to support jumbo strings!");
        }
    }

    private static class FieldInstruction extends Instruction {
        FieldInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            short field = instructions[i + 1];
            instructions[i + 1] = (short) indexMap.fieldIds[field];
        }
    }

    private static class TypeInstruction extends Instruction {
        TypeInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            short type = instructions[i + 1];
            instructions[i + 1] = (short) indexMap.typeIds[type];
        }
    }

    private static class MethodInstruction extends Instruction {
        MethodInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            short method = instructions[i + 1];
            instructions[i + 1] = (short) indexMap.methodIds[method];
        }
    }

    private static class PackedSwitchInstruction extends Instruction {
        public PackedSwitchInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            int offset = (instructions[i + 1] & 0xFFFF)
                    + ((instructions[i + 2] & 0xFFFF) << 16);
            if (instructions[i + offset] != 0x100) {
                throw new DexException("Expected packed-switch pseudo-opcode but was 0x"
                        + Integer.toHexString(instructions[i + offset]));
            }
            short size = instructions[i + offset + 1];
            skippedInstructions.set(i + offset, i + offset + 4 + (size * 2));
        }
    }

    private static class SparseSwitchInstruction extends Instruction {
        public SparseSwitchInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            int offset = (instructions[i + 1] & 0xFFFF)
                    + ((instructions[i + 2] & 0xFFFF) << 16);
            if (instructions[i + offset] != 0x200) {
                throw new DexException("Expected sparse-switch pseudo-opcode but was 0x"
                        + Integer.toHexString(instructions[i + offset]));
            }
            short size = instructions[i + offset + 1];
            skippedInstructions.set(i + offset, i + offset + 2 + (size * 4));
        }
    }

    private static class FillArrayInstruction extends Instruction {
        public FillArrayInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override public void transform(short[] instructions, int i, IndexMap indexMap,
                BitSet skippedInstructions) throws DexException {
            int offset = (instructions[i + 1] & 0xFFFF)
                    + ((instructions[i + 2] & 0xFFFF) << 16);
            if (instructions[i + offset] != 0x300) {
                throw new DexException("Expected fill-array-data pseudo-opcode but was 0x"
                        + Integer.toHexString(instructions[i + offset]));
            }
            int bytesPerElement = instructions[i + offset + 1];
            int size = (instructions[i + offset + 2] & 0xFFFF)
                    + ((instructions[i + offset + 3] & 0xFFFF) << 4);
            int totalBytes = size * bytesPerElement;
            int totalShorts = (totalBytes + 1) / 2; // round up!
            skippedInstructions.set(i + offset, i + offset + 4 + totalShorts);
        }
    }
}
