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
import com.android.dx.util.Hex;

/**
 * Walks through a block of code and calls visitor call backs.
 */
public final class CodeReader {

    private final Instruction[] instructions = new Instruction[] {
            // 0x00...0x0f
            new Instruction("nop"),
            new Instruction("move vA, vB"),
            new Instruction("move/from vAA, vBBBB"),
            new Instruction("move/16 vAAAA, vBBBB"),
            new Instruction("move-wide, vA, vB"),
            new Instruction("move-wide/from16 vAA, vBBBB"),
            new Instruction("move-wide/from16 vAAAA, vBBBB"),
            new Instruction("move-object vA, vB"),
            new Instruction("move-object/from16 vAA, vBBBB"),
            new Instruction("move-object/16 vAAAA, vBBBB"),
            new Instruction("move-result vAA"),
            new Instruction("move-result-wide vAA"),
            new Instruction("move-result-object vAA"),
            new Instruction("move-exception vAA"),
            new Instruction("return void"),
            new Instruction("return vAA"),

            // 0x10...0x1f
            new Instruction("return-wide vAA"),
            new Instruction("return-object vAA"),
            new Instruction("const/4 vA, #+B"),
            new Instruction("const/16 vAA, #+BBBB"),
            new Instruction("const vAA, #+BBBBBBBB"),
            new Instruction("const/high16 vAA, #+BBBB0000"),
            new Instruction("const-wide/16 vAA, #+BBBB"),
            new Instruction("const-wide/32 vAA, #+BBBBBBBB"),
            new Instruction("const-wide vAA, #+BBBBBBBBBBBBBBBB"),
            new Instruction("const-wide/high16 vAA, #+BBBB000000000000"),
            new Instruction("const-string vAA, string@BBBB"),
            new Instruction("const-string/jumbo vAA, string@BBBBBBBB"),
            new Instruction("const-class vAA, type@BBBB"),
            new Instruction("monitor-enter vAA"),
            new Instruction("monitor-exit vAA"),
            new Instruction("check-cast vAA type@BBBB"),

            // 0x20...0x2f
            new Instruction("instance-of vA, vB, type@CCCC"),
            new Instruction("array-length vA, vB"),
            new Instruction("new-instance vAA, type@BBBB"),
            new Instruction("new-array vA, vB, type@CCCC"),
            new Instruction("filled-new-array {vD, vE, vF, vG, vA}, type@CCCC"),
            new Instruction("filled-new-array/range {vCCCC..vNNNN}, type@BBBB"),
            new Instruction("fill-array-data vAA, +BBBBBBBB"),
            new Instruction("throw vAA"),
            new Instruction("goto +AA"),
            new Instruction("goto/16 +AAAA"),
            new Instruction("goto/32 +AAAAAAAA"),
            new Instruction("packed-switch vAA, +BBBBBBBB"),
            new Instruction("sparse-switch vAA, +BBBBBBBB"),
            new Instruction("cmpl-float vAA, vBB, vCC"),
            new Instruction("cmpg-float vAA, vBB, vCC"),
            new Instruction("cmpl-double vAA, vBB, vCC"),

            // 0x30...0x3f
            new Instruction("cmpg-double vAA, vBB, vCC"),
            new Instruction("cmp-long vAA, vBB, vCC"),
            new Instruction("if-eq vA, vB, +CCCC"),
            new Instruction("if-ne vA, vB, +CCCC"),
            new Instruction("if-lt vA, vB, +CCCC"),
            new Instruction("if-ge vA, vB, +CCCC"),
            new Instruction("if-gt vA, vB, +CCCC"),
            new Instruction("if-le vA, vB, +CCCC"),
            new Instruction("if-eqz vAA, +BBBB"),
            new Instruction("if-nez vAA, +BBBB"),
            new Instruction("if-ltz vAA, +BBBB"),
            new Instruction("if-gez vAA, +BBBB"),
            new Instruction("if-gtz vAA, +BBBB"),
            new Instruction("if-lez vAA, +BBBB"),
            new UnusedInstruction(),
            new UnusedInstruction(),

            // 0x40...0x4f
            new UnusedInstruction(),
            new UnusedInstruction(),
            new UnusedInstruction(),
            new UnusedInstruction(),
            new Instruction("aget vAA, vBB, vCC"),
            new Instruction("aget-wide vAA, vBB, vCC"),
            new Instruction("aget-object vAA, vBB, vCC"),
            new Instruction("aget-boolean vAA, vBB, vCC"),
            new Instruction("aget-byte vAA, vBB, vCC"),
            new Instruction("aget-char vAA, vBB, vCC"),
            new Instruction("aget-short vAA, vBB, vCC"),
            new Instruction("aput vAA, vBB, vCC"),
            new Instruction("aput-wide vAA, vBB, vCC"),
            new Instruction("aput-object vAA, vBB, vCC"),
            new Instruction("aput-boolean vAA, vBB, vCC"),
            new Instruction("aput-byte vAA, vBB, vCC"),

            // 0x50...0x5f
            new Instruction("aput-char vAA, vBB, vCC"),
            new Instruction("aput-short vAA, vBB, vCC"),
            new Instruction("iget vA, vB, field@CCCC"),
            new Instruction("iget-wide vA, vB, field@CCCC"),
            new Instruction("iget-object vA, vB, field@CCCC"),
            new Instruction("iget-boolean vA, vB, field@CCCC"),
            new Instruction("iget-byte vA, vB, field@CCCC"),
            new Instruction("iget-char vA, vB, field@CCCC"),
            new Instruction("iget-short vA, vB, field@CCCC"),
            new Instruction("iput vA, vB, field@CCCC"),
            new Instruction("iput-wide vA, vB, field@CCCC"),
            new Instruction("iput-object vA, vB, field@CCCC"),
            new Instruction("iput-boolean vA, vB, field@CCCC"),
            new Instruction("iput-byte vA, vB, field@CCCC"),
            new Instruction("iput-char vA, vB, field@CCCC"),
            new Instruction("iput-short vA, vB, field@CCCC"),

            // 0x60...0x6f
            new Instruction("sget vAA, field@BBBB"),
            new Instruction("sget-wide vAA, field@BBBB"),
            new Instruction("sget-object vAA, field@BBBB"),
            new Instruction("sget-boolean vAA, field@BBBB"),
            new Instruction("sget-byte vAA, field@BBBB"),
            new Instruction("sget-char vAA, field@BBBB"),
            new Instruction("sget-short vAA, field@BBBB"),
            new Instruction("sput vAA, field@BBBB"),
            new Instruction("sput-wide vAA, field@BBBB"),
            new Instruction("sput-object vAA, field@BBBB"),
            new Instruction("sput-boolean vAA, field@BBBB"),
            new Instruction("sput-byte vAA, field@BBBB"),
            new Instruction("sput-char vAA, field@BBBB"),
            new Instruction("sput-short vAA, field@BBBB"),
            new Instruction("invoke-virtual {vD, vE, vF, vG, vA}, meth@CCCC"),
            new Instruction("invoke-super {vD, vE, vF, vG, vA}, meth@CCCC"),

            // 0x70...0x7f
            new Instruction("invoke-direct {vD, vE, vF, vG, vA}, meth@CCCC"),
            new Instruction("invoke-static {vD, vE, vF, vG, vA}, meth@CCCC"),
            new Instruction("invoke-interface {vD, vE, vF, vG, vA}, meth@CCCC"),
            new UnusedInstruction(),
            new Instruction("invoke-virtual/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction("invoke-super/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction("invoke-direct/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction("invoke-static/range {vCCCC..vNNNN}, meth@BBBB"),
            new Instruction("invoke-interface/range {vCCCC..vNNNN}, meth@BBBB"),
            new UnusedInstruction(),
            new UnusedInstruction(),
            new Instruction("neg-int vA, vB"),
            new Instruction("not-int vA, vB"),
            new Instruction("neg-long vA, vB"),
            new Instruction("not-long vA, vB"),
            new Instruction("neg-float vA, vB"),

            // 0x80...0x8f
            new Instruction("neg-double vA, vB"),
            new Instruction("int-to-long vA, vB"),
            new Instruction("int-to-float vA, vB"),
            new Instruction("int-to-double vA, vB"),
            new Instruction("long-to-int vA, vB"),
            new Instruction("long-to-float vA, vB"),
            new Instruction("long-to-double vA, vB"),
            new Instruction("float-to-int vA, vB"),
            new Instruction("float-to-long vA, vB"),
            new Instruction("float-to-double vA, vB"),
            new Instruction("double-to-int vA, vB"),
            new Instruction("double-to-long vA, vB"),
            new Instruction("double-to-float vA, vB"),
            new Instruction("int-to-byte vA, vB"),
            new Instruction("int-to-char vA, vB"),
            new Instruction("int-to-short vA, vB"),

            // 0x90...0x9f
            new Instruction("add-int vAA, vBB, vCC"),
            new Instruction("sub-int vAA, vBB, vCC"),
            new Instruction("mul-int vAA, vBB, vCC"),
            new Instruction("div-int vAA, vBB, vCC"),
            new Instruction("rem-int vAA, vBB, vCC"),
            new Instruction("and-int vAA, vBB, vCC"),
            new Instruction("or-int vAA, vBB, vCC"),
            new Instruction("xor-int vAA, vBB, vCC"),
            new Instruction("shl-int vAA, vBB, vCC"),
            new Instruction("shr-int vAA, vBB, vCC"),
            new Instruction("ushr-int vAA, vBB, vCC"),
            new Instruction("add-long vAA, vBB, vCC"),
            new Instruction("sub-long vAA, vBB, vCC"),
            new Instruction("mul-long vAA, vBB, vCC"),
            new Instruction("div-long vAA, vBB, vCC"),
            new Instruction("rem-long vAA, vBB, vCC"),

            // 0xa0...0xaf
            new Instruction("and-long vAA, vBB, vCC"),
            new Instruction("or-long vAA, vBB, vCC"),
            new Instruction("xor-long vAA, vBB, vCC"),
            new Instruction("shl-long vAA, vBB, vCC"),
            new Instruction("shr-long vAA, vBB, vCC"),
            new Instruction("ushr-long vAA, vBB, vCC"),
            new Instruction("add-float vAA, vBB, vCC"),
            new Instruction("sub-float vAA, vBB, vCC"),
            new Instruction("mul-float vAA, vBB, vCC"),
            new Instruction("div-float vAA, vBB, vCC"),
            new Instruction("rem-float vAA, vBB, vCC"),
            new Instruction("add-double vAA, vBB, vCC"),
            new Instruction("sub-double vAA, vBB, vCC"),
            new Instruction("mul-double vAA, vBB, vCC"),
            new Instruction("div-double vAA, vBB, vCC"),
            new Instruction("rem-double vAA, vBB, vCC"),

            // 0xb0..0xbf
            new Instruction("add-int/2addr vA, vB"),
            new Instruction("sub-int/2addr vA, vB"),
            new Instruction("mul-int/2addr vA, vB"),
            new Instruction("div-int/2addr vA, vB"),
            new Instruction("rem-int/2addr vA, vB"),
            new Instruction("and-int/2addr vA, vB"),
            new Instruction("or-int/2addr vA, vB"),
            new Instruction("xor-int/2addr vA, vB"),
            new Instruction("shl-int/2addr vA, vB"),
            new Instruction("shr-int/2addr vA, vB"),
            new Instruction("ushr-int/2addr vA, vB"),
            new Instruction("add-long/2addr vA, vB"),
            new Instruction("sub-long/2addr vA, vB"),
            new Instruction("mul-long/2addr vA, vB"),
            new Instruction("div-long/2addr vA, vB"),
            new Instruction("rem-long/2addr vA, vB"),

            // 0xc0...0xcf
            new Instruction("and-long/2addr vA, vB"),
            new Instruction("or-long/2addr vA, vB"),
            new Instruction("xor-long/2addr vA, vB"),
            new Instruction("shl-long/2addr vA, vB"),
            new Instruction("shr-long/2addr vA, vB"),
            new Instruction("ushr-long/2addr vA, vB"),
            new Instruction("add-float/2addr vA, vB"),
            new Instruction("sub-float/2addr vA, vB"),
            new Instruction("mul-float/2addr vA, vB"),
            new Instruction("div-float/2addr vA, vB"),
            new Instruction("rem-float/2addr vA, vB"),
            new Instruction("add-double/2addr vA, vB"),
            new Instruction("sub-double/2addr vA, vB"),
            new Instruction("mul-double/2addr vA, vB"),
            new Instruction("div-double/2addr vA, vB"),
            new Instruction("rem-double/2addr vA, vB"),

            // 0xd0...0xdf
            new Instruction("add-int/lit16 vA, vB, #+CCCC"),
            new Instruction("rsub-int (reverse subtract) vA, vB, #+CCCC"),
            new Instruction("mul-int/lit16 vA, vB, #+CCCC"),
            new Instruction("div-int/lit16 vA, vB, #+CCCC"),
            new Instruction("rem-int/lit16 vA, vB, #+CCCC"),
            new Instruction("and-int/lit16 vA, vB, #+CCCC"),
            new Instruction("or-int/lit16 vA, vB, #+CCCC"),
            new Instruction("xor-int/lit16 vA, vB, #+CCCC"),
            new Instruction("add-int/lit8 vAA, vBB, #+CC"),
            new Instruction("rsub-int/lit8 vAA, vBB, #+CC"),
            new Instruction("mul-int/lit8 vAA, vBB, #+CC"),
            new Instruction("div-int/lit8 vAA, vBB, #+CC"),
            new Instruction("rem-int/lit8 vAA, vBB, #+CC"),
            new Instruction("and-int/lit8 vAA, vBB, #+CC"),
            new Instruction("or-int/lit8 vAA, vBB, #+CC"),
            new Instruction("xor-int/lit8 vAA, vBB, #+CC"),

            // 0xe0...0xef
            new Instruction("shl-int/lit8 vAA, vBB, #+CC"),
            new Instruction("shr-int/lit8 vAA, vBB, #+CC"),
            new Instruction("ushr-int/lit8 vAA, vBB, #+CC"),
    };

    /**
     * Sets {@code visitor} as the visitor for all instructions.
     */
    public void setAllVisitors(Visitor visitor) {
        for (Instruction instruction : instructions) {
            instruction.setVisitor(null, visitor);
        }
    }

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

    public void visitAll(DecodedInstruction[] decodedInstructions)
            throws DexException {
        int size = decodedInstructions.length;

        for (int i = 0; i < size; i++) {
            DecodedInstruction di = decodedInstructions[i];
            if (di == null) {
                continue;
            }

            Instruction instruction = instructions[di.getOpcode()];
            Visitor visitor = instruction.visitor;
            if (visitor != null) {
                visitor.visit(instruction, decodedInstructions, di);
            }
        }
    }

    public void visitAll(short[] encodedInstructions) throws DexException {
        DecodedInstruction[] decodedInstructions =
            DecodedInstruction.decodeAll(encodedInstructions);
        visitAll(decodedInstructions);
    }

    public static class Instruction {
        private final String name;
        private Visitor visitor;

        private Instruction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        /**
         * Sets the visitor to be notified when this instruction is encountered,
         * or null if this instruction has no visitor.
         */
        public void setVisitor(String name, Visitor visitor) {
            if ((name != null) && !this.name.equals(name)) {
                throw new IllegalArgumentException("Expected " + this.name + " but was " + name);
            }
            this.visitor = visitor;
        }

        @Override public String toString() {
            return name;
        }
    }

    public interface Visitor {
        void visit(Instruction instruction, DecodedInstruction[] all,
                DecodedInstruction one);
    }

    private static class UnusedInstruction extends Instruction {
        UnusedInstruction() {
            super("unused");
        }
    }
}
