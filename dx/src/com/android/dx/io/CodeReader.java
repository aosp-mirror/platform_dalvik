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

import com.android.dx.dex.DexException;
import java.util.BitSet;

/**
 * Walks through a block of code and calls visitor call backs.
 */
public final class CodeReader {

    private final Instruction[] instructions = new Instruction[] {
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
            new Instruction(2, "const-string vAA, string@BBBB"),
            new Instruction(3, "const-string/jumbo vAA, string@BBBBBBBB"),
            new Instruction(2, "const-class vAA, type@BBBB"),
            new Instruction(1, "monitor-enter vAA"),
            new Instruction(1, "monitor-exit vAA"),
            new Instruction(2, "check-cast vAA type@BBBB"),

            // 0x20...0x2f
            new Instruction(2, "instance-of vA, vB, type@CCCC"),
            new Instruction(1, "array-length vA, vB"),
            new Instruction(2, "new-instance vAA, type@BBBB"),
            new Instruction(2, "new-array vA, vB, type@CCCC"),
            new Instruction(3, "filled-new-array {vD, vE, vF, vG, vA}, type@CCCC"),
            new Instruction(3, "filled-new-array/range {vCCCC..vNNNN}, type@BBBB"),
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
            new Instruction(2, "iget vA, vB, field@CCCC"),
            new Instruction(2, "iget-wide vA, vB, field@CCCC"),
            new Instruction(2, "iget-object vA, vB, field@CCCC"),
            new Instruction(2, "iget-boolean vA, vB, field@CCCC"),
            new Instruction(2, "iget-byte vA, vB, field@CCCC"),
            new Instruction(2, "iget-char vA, vB, field@CCCC"),
            new Instruction(2, "iget-short vA, vB, field@CCCC"),
            new Instruction(2, "iput vA, vB, field@CCCC"),
            new Instruction(2, "iput-wide vA, vB, field@CCCC"),
            new Instruction(2, "iput-object vA, vB, field@CCCC"),
            new Instruction(2, "iput-boolean vA, vB, field@CCCC"),
            new Instruction(2, "iput-byte vA, vB, field@CCCC"),
            new Instruction(2, "iput-char vA, vB, field@CCCC"),
            new Instruction(2, "iput-short vA, vB, field@CCCC"),

            // 0x60...0x6f
            new Instruction(2, "sget vAA, field@BBBB"),
            new Instruction(2, "sget-wide vAA, field@BBBB"),
            new Instruction(2, "sget-object vAA, field@BBBB"),
            new Instruction(2, "sget-boolean vAA, field@BBBB"),
            new Instruction(2, "sget-byte vAA, field@BBBB"),
            new Instruction(2, "sget-char vAA, field@BBBB"),
            new Instruction(2, "sget-short vAA, field@BBBB"),
            new Instruction(2, "sput vAA, field@BBBB"),
            new Instruction(2, "sput-wide vAA, field@BBBB"),
            new Instruction(2, "sput-object vAA, field@BBBB"),
            new Instruction(2, "sput-boolean vAA, field@BBBB"),
            new Instruction(2, "sput-byte vAA, field@BBBB"),
            new Instruction(2, "sput-char vAA, field@BBBB"),
            new Instruction(2, "sput-short vAA, field@BBBB"),
            new Instruction(3, "invoke-virtual {vD, vE, vF, vG, vA}, meth@CCCC"),
            new Instruction(3, "invoke-super {vD, vE, vF, vG, vA}, meth@CCCC"),

            // 0x70...0x7f
            new Instruction(3, "invoke-direct {vD, vE, vF, vG, vA}, meth@CCCC"),
            new Instruction(3, "invoke-static {vD, vE, vF, vG, vA}, meth@CCCC"),
            new Instruction(3, "invoke-interface {vD, vE, vF, vG, vA}, meth@CCCC"),
            new UnusedInstruction(),
            new Instruction(3, "invoke-virtual/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction(3, "invoke-super/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction(3, "invoke-direct/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction(3, "invoke-static/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction(3, "invoke-interface/range {vCCCC..vNNNN}, meth@BBBB"),
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

    /**
     * Sets {@code visitor} as the visitor for all string instructions.
     */
    public void setStringVisitor(Visitor visitor) {
        instructions[0x1a].setVisitor("const-string vAA, string@BBBB", visitor);
    }

    /**
     * Sets {@code visitor} as the visitor for all jumbo string instructions.
     */
    public void setJumboStringVisitor(Visitor visitor) {
        instructions[0x1b].setVisitor("const-string/jumbo vAA, string@BBBBBBBB", visitor);
    }

    /**
     * Sets {@code visitor} as the visitor for all type instructions.
     */
    public void setTypeVisitor(Visitor visitor) {
        instructions[0x1c].setVisitor("const-class vAA, type@BBBB", visitor);
        instructions[0x1f].setVisitor("check-cast vAA type@BBBB", visitor);
        instructions[0x20].setVisitor("instance-of vA, vB, type@CCCC", visitor);
        instructions[0x22].setVisitor("new-instance vAA, type@BBBB", visitor);
        instructions[0x23].setVisitor("new-array vA, vB, type@CCCC", visitor);
        instructions[0x24].setVisitor("filled-new-array {vD, vE, vF, vG, vA}, type@CCCC", visitor);
        instructions[0x25].setVisitor("filled-new-array/range {vCCCC..vNNNN}, type@BBBB", visitor);
    }

    /**
     * Sets {@code visitor} as the visitor for all field instructions.
     */
    public void setFieldVisitor(Visitor visitor) {
        instructions[0x52].setVisitor("iget vA, vB, field@CCCC", visitor);
        instructions[0x53].setVisitor("iget-wide vA, vB, field@CCCC", visitor);
        instructions[0x54].setVisitor("iget-object vA, vB, field@CCCC", visitor);
        instructions[0x55].setVisitor("iget-boolean vA, vB, field@CCCC", visitor);
        instructions[0x56].setVisitor("iget-byte vA, vB, field@CCCC", visitor);
        instructions[0x57].setVisitor("iget-char vA, vB, field@CCCC", visitor);
        instructions[0x58].setVisitor("iget-short vA, vB, field@CCCC", visitor);
        instructions[0x59].setVisitor("iput vA, vB, field@CCCC", visitor);
        instructions[0x5a].setVisitor("iput-wide vA, vB, field@CCCC", visitor);
        instructions[0x5b].setVisitor("iput-object vA, vB, field@CCCC", visitor);
        instructions[0x5c].setVisitor("iput-boolean vA, vB, field@CCCC", visitor);
        instructions[0x5d].setVisitor("iput-byte vA, vB, field@CCCC", visitor);
        instructions[0x5e].setVisitor("iput-char vA, vB, field@CCCC", visitor);
        instructions[0x5f].setVisitor("iput-short vA, vB, field@CCCC", visitor);
        instructions[0x60].setVisitor("sget vAA, field@BBBB", visitor);
        instructions[0x61].setVisitor("sget-wide vAA, field@BBBB", visitor);
        instructions[0x62].setVisitor("sget-object vAA, field@BBBB", visitor);
        instructions[0x63].setVisitor("sget-boolean vAA, field@BBBB", visitor);
        instructions[0x64].setVisitor("sget-byte vAA, field@BBBB", visitor);
        instructions[0x65].setVisitor("sget-char vAA, field@BBBB", visitor);
        instructions[0x66].setVisitor("sget-short vAA, field@BBBB", visitor);
        instructions[0x67].setVisitor("sput vAA, field@BBBB", visitor);
        instructions[0x68].setVisitor("sput-wide vAA, field@BBBB", visitor);
        instructions[0x69].setVisitor("sput-object vAA, field@BBBB", visitor);
        instructions[0x6a].setVisitor("sput-boolean vAA, field@BBBB", visitor);
        instructions[0x6b].setVisitor("sput-byte vAA, field@BBBB", visitor);
        instructions[0x6c].setVisitor("sput-char vAA, field@BBBB", visitor);
        instructions[0x6d].setVisitor("sput-short vAA, field@BBBB", visitor);
    }

    /**
     * Sets {@code visitor} as the visitor for all method instructions.
     */
    public void setMethodVisitor(Visitor visitor) {
        instructions[0x6e].setVisitor("invoke-virtual {vD, vE, vF, vG, vA}, meth@CCCC", visitor);
        instructions[0x6f].setVisitor("invoke-super {vD, vE, vF, vG, vA}, meth@CCCC", visitor);
        instructions[0x70].setVisitor("invoke-direct {vD, vE, vF, vG, vA}, meth@CCCC", visitor);
        instructions[0x71].setVisitor("invoke-static {vD, vE, vF, vG, vA}, meth@CCCC", visitor);
        instructions[0x72].setVisitor("invoke-interface {vD, vE, vF, vG, vA}, meth@CCCC", visitor);
        instructions[0x74].setVisitor("invoke-virtual/range {vCCCC..vNNNN}, meth@BBBB", visitor);
        instructions[0x75].setVisitor("invoke-super/range {vCCCC..vNNNN}, meth@BBBB", visitor);
        instructions[0x76].setVisitor("invoke-direct/range {vCCCC..vNNNN}, meth@BBBB", visitor);
        instructions[0x77].setVisitor("invoke-static/range {vCCCC..vNNNN}, meth@BBBB", visitor);
        instructions[0x78].setVisitor("invoke-interface/range {vCCCC..vNNNN}, meth@BBBB", visitor);
    }

    public void visitAll(short[] instructions) throws DexException {
        BitSet skippedInstructions = new BitSet();

        for (int i = 0; i < instructions.length; ) {
            if (skippedInstructions.get(i)) {
                i++;
                continue;
            }

            int index = instructions[i] & 0xFF;
            if (index < 0 || index >= this.instructions.length) {
                throw new DexException("Unhandled instruction at " + i
                        + ": " + Integer.toHexString(index));
            }

            Instruction instruction = this.instructions[index];
            instruction.mask(instructions, i, skippedInstructions);
            if (instruction.visitor != null) {
                instruction.visitor.visit(instruction, instructions, i);
            }
            i += instruction.codeUnits;
        }
    }

    public static class Instruction {
        private final String name;
        private final int codeUnits;
        private Visitor visitor;

        private Instruction(int codeUnits, String name) {
            this.name = name;
            this.codeUnits = codeUnits;
        }

        public String getName() {
            return name;
        }

        /**
         * Sets the visitor to be notified when this instruction is encountered,
         * or null if this instruction has no visitor.
         */
        public void setVisitor(String name, Visitor visitor) {
            if (!this.name.equals(name)) {
                throw new IllegalArgumentException("Expected " + this.name + " but was " + name);
            }
            this.visitor = visitor;
        }

        protected void mask(short[] instructions, int offset, BitSet skippedInstructions) {}

        @Override public String toString() {
            return name;
        }
    }

    public interface Visitor {
        void visit(Instruction instruction, short[] instructions, int offset);
    }

    private static class UnusedInstruction extends Instruction {
        UnusedInstruction() {
            super(1, "unused");
        }
    }

    private static class PackedSwitchInstruction extends Instruction {
        public PackedSwitchInstruction(int codeUnits, String name) {
            super(codeUnits, name);
        }
        @Override protected void mask(short[] instructions, int i, BitSet skippedInstructions) {
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
        @Override protected void mask(short[] instructions, int i, BitSet skippedInstructions) {
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
        @Override protected void mask(short[] instructions, int i, BitSet skippedInstructions) {
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
