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
import com.android.dx.io.CodeReader;

final class InstructionTransformer {
    private final IndexMap indexMap;
    private final CodeReader reader;

    public InstructionTransformer(IndexMap indexMap) {
        this.indexMap = indexMap;
        this.reader = new CodeReader();
        this.reader.setJumboStringVisitor(new JumboStringInstruction());
        this.reader.setStringVisitor(new StringInstruction());
        this.reader.setTypeVisitor(new TypeVisitor());
        this.reader.setFieldVisitor(new FieldVisitor());
        this.reader.setMethodVisitor(new MethodVisitor());
    }

    public short[] transform(short[] instructions) throws DexException {
        instructions = instructions.clone();
        reader.visitAll(instructions);
        return instructions;
    }

    private class StringInstruction implements CodeReader.Visitor {
        public void visit(CodeReader.Instruction instruction, short[] instructions, int i) {
            int stringIndex = instructions[i + 1] & 0xFFFF;
            int mappedIndex = indexMap.adjustString(stringIndex);
            if (mappedIndex > 0xFFFF) {
                throw new DexException("Cannot convert string to jumbo string!");
            }
            instructions[i + 1] = (short) mappedIndex;
        }
    }

    private class JumboStringInstruction implements CodeReader.Visitor {
        public void visit(CodeReader.Instruction instruction, short[] instructions, int i) {
            throw new UnsupportedOperationException("Jumbo strings not implemented. "
                    + "Due to a lack of dex files requiring jumbo strings, this class doesn't "
                    + "bother to support jumbo strings!");
        }
    }

    private class FieldVisitor implements CodeReader.Visitor {
        public void visit(CodeReader.Instruction instruction, short[] instructions, int i) {
            short field = instructions[i + 1];
            instructions[i + 1] = indexMap.adjustField(field);
        }
    }

    private class TypeVisitor implements CodeReader.Visitor {
        public void visit(CodeReader.Instruction instruction, short[] instructions, int i) {
            short type = instructions[i + 1];
            instructions[i + 1] = indexMap.adjustType(type);
        }
    }

    private class MethodVisitor implements CodeReader.Visitor {
        public void visit(CodeReader.Instruction instruction, short[] instructions, int i) {
            short method = instructions[i + 1];
            instructions[i + 1] = indexMap.adjustMethod(method);
        }
    }
}
