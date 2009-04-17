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

package com.android.dx.cf.code;

import com.android.dx.rop.cst.Constant;
import com.android.dx.rop.cst.CstFieldRef;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.CstInterfaceMethodRef;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.Prototype;
import com.android.dx.rop.type.Type;
import com.android.dx.rop.type.TypeBearer;
import com.android.dx.rop.code.LocalItem;
import com.android.dx.util.Hex;
import com.android.dx.util.IntList;

import java.util.List;
import java.util.ArrayList;

/**
 * Class which knows how to simulate the effects of executing bytecode.
 *
 * <p><b>Note:</b> This class is not thread-safe. If multiple threads
 * need to use a single instance, they must synchronize access explicitly
 * between themselves.</p>
 */
public class Simulator {
    /**
     * {@code non-null;} canned error message for local variable
     * table mismatches
     */
    private static final String LOCAL_MISMATCH_ERROR =
        "This is symptomatic of .class transformation tools that ignore " +
        "local variable information.";
    
    /** {@code non-null;} machine to use when simulating */
    private final Machine machine;

    /** {@code non-null;} array of bytecode */
    private final BytecodeArray code;

    /** {@code non-null;} local variable information */
    private final LocalVariableList localVariables;

    /** {@code non-null;} visitor instance to use */
    private final SimVisitor visitor;

    /**
     * Constructs an instance.
     * 
     * @param machine {@code non-null;} machine to use when simulating
     * @param method {@code non-null;} method data to use
     */
    public Simulator(Machine machine, ConcreteMethod method) {
        if (machine == null) {
            throw new NullPointerException("machine == null");
        }

        if (method == null) {
            throw new NullPointerException("method == null");
        }

        this.machine = machine;
        this.code = method.getCode();
        this.localVariables = method.getLocalVariables();
        this.visitor = new SimVisitor();
    }

    /**
     * Simulates the effect of executing the given basic block. This modifies
     * the passed-in frame to represent the end result.
     * 
     * @param bb {@code non-null;} the basic block
     * @param frame {@code non-null;} frame to operate on
     */
    public void simulate(ByteBlock bb, Frame frame) {
        int end = bb.getEnd();

        visitor.setFrame(frame);

        try {
            for (int off = bb.getStart(); off < end; /*off*/) {
                int length = code.parseInstruction(off, visitor);
                visitor.setPreviousOffset(off);
                off += length;
            }
        } catch (SimException ex) {
            frame.annotate(ex);
            throw ex;
        }
    }

    /**
     * Simulates the effect of the instruction at the given offset, by
     * making appropriate calls on the given frame.
     * 
     * @param offset {@code >= 0;} offset of the instruction to simulate
     * @param frame {@code non-null;} frame to operate on
     * @return the length of the instruction, in bytes
     */
    public int simulate(int offset, Frame frame) {
        visitor.setFrame(frame);
        return code.parseInstruction(offset, visitor);
    }

    /**
     * Constructs an "illegal top-of-stack" exception, for the stack
     * manipulation opcodes.
     */
    private static SimException illegalTos() {
        return new SimException("stack mismatch: illegal " +
                "top-of-stack for opcode");
    }

    /**
     * Bytecode visitor used during simulation.
     */
    private class SimVisitor implements BytecodeArray.Visitor {
        /**
         * {@code non-null;} machine instance to use (just to avoid excessive
         * cross-object field access)
         */
        private final Machine machine;

        /**
         * {@code null-ok;} frame to use; set with each call to 
         * {@link Simulator#simulate}
         */
        private Frame frame;

        /** offset of the previous bytecode */
        private int previousOffset;

        /**
         * Constructs an instance.
         */
        public SimVisitor() {
            this.machine = Simulator.this.machine;
            this.frame = null;
        }

        /**
         * Sets the frame to act on.
         * 
         * @param frame {@code non-null;} the frame
         */
        public void setFrame(Frame frame) {
            if (frame == null) {
                throw new NullPointerException("frame == null");
            }

            this.frame = frame;
        }

        /** {@inheritDoc} */
        public void visitInvalid(int opcode, int offset, int length) {
            throw new SimException("invalid opcode " + Hex.u1(opcode));
        }

        /** {@inheritDoc} */
        public void visitNoArgs(int opcode, int offset, int length,
                Type type) {
            switch (opcode) {
                case ByteOps.NOP: {
                    machine.clearArgs();
                    break;
                }
                case ByteOps.INEG: {
                    machine.popArgs(frame, type);
                    break;
                }
                case ByteOps.I2L:
                case ByteOps.I2F:
                case ByteOps.I2D:
                case ByteOps.I2B:
                case ByteOps.I2C:
                case ByteOps.I2S: {
                    machine.popArgs(frame, Type.INT);
                    break;
                }
                case ByteOps.L2I:
                case ByteOps.L2F:
                case ByteOps.L2D: {
                    machine.popArgs(frame, Type.LONG);
                    break;
                }
                case ByteOps.F2I:
                case ByteOps.F2L:
                case ByteOps.F2D: {
                    machine.popArgs(frame, Type.FLOAT);
                    break;
                }
                case ByteOps.D2I:
                case ByteOps.D2L:
                case ByteOps.D2F: {
                    machine.popArgs(frame, Type.DOUBLE);
                    break;
                }
                case ByteOps.RETURN: {
                    machine.clearArgs();
                    checkReturnType(Type.VOID);
                    break;
                }
                case ByteOps.IRETURN: {
                    Type checkType = type;
                    if (type == Type.OBJECT) {
                        /*
                         * For an object return, use the best-known
                         * type of the popped value.
                         */
                        checkType = frame.getStack().peekType(0);
                    }
                    machine.popArgs(frame, type);
                    checkReturnType(checkType);
                    break;
                }
                case ByteOps.POP: {
                    Type peekType = frame.getStack().peekType(0);
                    if (peekType.isCategory2()) {
                        throw illegalTos();
                    }
                    machine.popArgs(frame, 1);
                    break;
                }
                case ByteOps.ARRAYLENGTH: {
                    Type arrayType = frame.getStack().peekType(0);
                    if (!arrayType.isArrayOrKnownNull()) {
                        throw new SimException("type mismatch: expected " +
                                "array type but encountered " +
                                arrayType.toHuman());
                    }
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.ATHROW:
                case ByteOps.MONITORENTER:
                case ByteOps.MONITOREXIT: {
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.IALOAD: {
                    /*
                     * Change the type (which is to be pushed) to
                     * reflect the actual component type of the array
                     * being popped, unless it turns out to be a
                     * known-null, in which case we just use the type
                     * implied by the original instruction.
                     */
                    Type foundArrayType = frame.getStack().peekType(1);
                    Type requireArrayType;

                    if (foundArrayType != Type.KNOWN_NULL) {
                        requireArrayType = foundArrayType;
                        type = foundArrayType.getComponentType();
                    } else {
                        requireArrayType = type.getArrayType();
                    }

                    machine.popArgs(frame, requireArrayType, Type.INT);
                    break;
                }
                case ByteOps.IADD:
                case ByteOps.ISUB:
                case ByteOps.IMUL:
                case ByteOps.IDIV:
                case ByteOps.IREM:
                case ByteOps.IAND:
                case ByteOps.IOR:
                case ByteOps.IXOR: {
                    machine.popArgs(frame, type, type);
                    break;
                }
                case ByteOps.ISHL:
                case ByteOps.ISHR:
                case ByteOps.IUSHR: {
                    machine.popArgs(frame, type, Type.INT);
                    break;
                }
                case ByteOps.LCMP: {
                    machine.popArgs(frame, Type.LONG, Type.LONG);
                    break;
                }
                case ByteOps.FCMPL:
                case ByteOps.FCMPG: {
                    machine.popArgs(frame, Type.FLOAT, Type.FLOAT);
                    break;
                }
                case ByteOps.DCMPL:
                case ByteOps.DCMPG: {
                    machine.popArgs(frame, Type.DOUBLE, Type.DOUBLE);
                    break;
                }
                case ByteOps.IASTORE: {
                    /*
                     * Change the type (which is the type of the
                     * element) to reflect the actual component type
                     * of the array being popped, unless it turns out
                     * to be a known-null, in which case we just use
                     * the type implied by the original instruction.
                     * The category 1 vs. 2 thing here is that, if the
                     * element type is category 2, we have to skip over
                     * one extra stack slot to find the array.
                     */
                    Type foundArrayType =
                        frame.getStack().peekType(type.isCategory1() ? 2 : 3);
                    Type requireArrayType;

                    if (foundArrayType != Type.KNOWN_NULL) {
                        requireArrayType = foundArrayType;
                        type = foundArrayType.getComponentType();
                    } else {
                        requireArrayType = type.getArrayType();
                    }

                    machine.popArgs(frame, requireArrayType, Type.INT, type);
                    break;
                }
                case ByteOps.POP2:
                case ByteOps.DUP2: {
                    ExecutionStack stack = frame.getStack();
                    int pattern;

                    if (stack.peekType(0).isCategory2()) {
                        // "form 2" in vmspec-2
                        machine.popArgs(frame, 1);
                        pattern = 0x11;
                    } else if (stack.peekType(1).isCategory1()) {
                        // "form 1"
                        machine.popArgs(frame, 2);
                        pattern = 0x2121;
                    } else {
                        throw illegalTos();
                    }

                    if (opcode == ByteOps.DUP2) {
                        machine.auxIntArg(pattern);
                    }
                    break;
                }
                case ByteOps.DUP: {
                    Type peekType = frame.getStack().peekType(0);

                    if (peekType.isCategory2()) {
                        throw illegalTos();
                    }

                    machine.popArgs(frame, 1);
                    machine.auxIntArg(0x11);
                    break;
                }
                case ByteOps.DUP_X1: {
                    ExecutionStack stack = frame.getStack();

                    if (! (stack.peekType(0).isCategory1() &&
                           stack.peekType(1).isCategory1())) {
                        throw illegalTos();
                    }

                    machine.popArgs(frame, 2);
                    machine.auxIntArg(0x212);
                    break;
                }
                case ByteOps.DUP_X2: {
                    ExecutionStack stack = frame.getStack();

                    if (stack.peekType(0).isCategory2()) {
                        throw illegalTos();
                    }
                    
                    if (stack.peekType(1).isCategory2()) {
                        // "form 2" in vmspec-2
                        machine.popArgs(frame, 2);
                        machine.auxIntArg(0x212);
                    } else if (stack.peekType(2).isCategory1()) {
                        // "form 1"
                        machine.popArgs(frame, 3);
                        machine.auxIntArg(0x3213);
                    } else {
                        throw illegalTos();
                    }
                    break;
                }
                case ByteOps.DUP2_X1: {
                    ExecutionStack stack = frame.getStack();

                    if (stack.peekType(0).isCategory2()) {
                        // "form 2" in vmspec-2
                        if (stack.peekType(2).isCategory2()) {
                            throw illegalTos();
                        }
                        machine.popArgs(frame, 2);
                        machine.auxIntArg(0x212);
                    } else {
                        // "form 1"
                        if (stack.peekType(1).isCategory2() ||
                            stack.peekType(2).isCategory2()) {
                            throw illegalTos();
                        }
                        machine.popArgs(frame, 3);
                        machine.auxIntArg(0x32132);
                    }
                    break;
                }
                case ByteOps.DUP2_X2: {
                    ExecutionStack stack = frame.getStack();

                    if (stack.peekType(0).isCategory2()) {
                        if (stack.peekType(2).isCategory2()) {
                            // "form 4" in vmspec-2
                            machine.popArgs(frame, 2);
                            machine.auxIntArg(0x212);
                        } else if (stack.peekType(3).isCategory1()) {
                            // "form 2"
                            machine.popArgs(frame, 3);
                            machine.auxIntArg(0x3213);
                        } else {
                            throw illegalTos();
                        }
                    } else if (stack.peekType(1).isCategory1()) {
                        if (stack.peekType(2).isCategory2()) {
                            // "form 3"
                            machine.popArgs(frame, 3);
                            machine.auxIntArg(0x32132);
                        } else if (stack.peekType(3).isCategory1()) {
                            // "form 1"
                            machine.popArgs(frame, 4);
                            machine.auxIntArg(0x432143);
                        } else {
                            throw illegalTos();
                        }
                    } else {
                        throw illegalTos();
                    }
                    break;
                }
                case ByteOps.SWAP: {
                    ExecutionStack stack = frame.getStack();

                    if (! (stack.peekType(0).isCategory1() &&
                           stack.peekType(1).isCategory1())) {
                        throw illegalTos();
                    }

                    machine.popArgs(frame, 2);
                    machine.auxIntArg(0x12);
                    break;
                }
                default: {
                    visitInvalid(opcode, offset, length);
                    return;
                }
            }

            machine.auxType(type);
            machine.run(frame, offset, opcode);
        }

        /**
         * Checks whether the prototype is compatible with returning the
         * given type, and throws if not.
         * 
         * @param encountered {@code non-null;} the encountered return type
         */
        private void checkReturnType(Type encountered) {
            Type returnType = machine.getPrototype().getReturnType();

            /*
             * Check to see if the prototype's return type is
             * possibly assignable from the type we encountered. This
             * takes care of all the salient cases (types are the same,
             * they're compatible primitive types, etc.).
             */
            if (! Merger.isPossiblyAssignableFrom(returnType, encountered)) {
                throw new SimException("return type mismatch: prototype " +
                        "indicates " + returnType.toHuman() +
                        ", but encountered type " + encountered.toHuman());
            }
        }

        /** {@inheritDoc} */
        public void visitLocal(int opcode, int offset, int length,
                int idx, Type type, int value) {
            /*
             * Note that the "type" parameter is always the simplest
             * type based on the original opcode, e.g., "int" for
             * "iload" (per se) and "Object" for "aload". So, when
             * possible, we replace the type with the one indicated in
             * the local variable table, though we still need to check
             * to make sure it's valid for the opcode.
             * 
             * The reason we use (offset + length) for the localOffset
             * for a store is because it is only after the store that
             * the local type becomes valid. On the other hand, the
             * type associated with a load is valid at the start of
             * the instruction.
             */
            int localOffset =
                (opcode == ByteOps.ISTORE) ? (offset + length) : offset;
            LocalVariableList.Item local =
                localVariables.pcAndIndexToLocal(localOffset, idx);
            Type localType;

            if (local != null) {
                localType = local.getType();
                if (localType.getBasicFrameType() !=
                        type.getBasicFrameType()) {
                    BaseMachine.throwLocalMismatch(type, localType);
                    return;
                }
            } else {
                localType = type;
            }

            switch (opcode) {
                case ByteOps.ILOAD:
                case ByteOps.RET: {
                    machine.localArg(frame, idx);
                    machine.auxType(type);
                    break;
                }
                case ByteOps.ISTORE: {
                    LocalItem item
                            = (local == null) ? null : local.getLocalItem();
                    machine.popArgs(frame, type);
                    machine.auxType(type);
                    machine.localTarget(idx, localType, item);
                    break;
                }
                case ByteOps.IINC: {
                    LocalItem item
                            = (local == null) ? null : local.getLocalItem();
                    machine.localArg(frame, idx);
                    machine.localTarget(idx, localType, item);
                    machine.auxType(type);
                    machine.auxIntArg(value);
                    machine.auxCstArg(CstInteger.make(value));
                    break;
                }
                default: {
                    visitInvalid(opcode, offset, length);
                    return;
                }
            }

            machine.run(frame, offset, opcode);
        }

        /** {@inheritDoc} */
        public void visitConstant(int opcode, int offset, int length,
                Constant cst, int value) {
            switch (opcode) {
                case ByteOps.ANEWARRAY: {
                    machine.popArgs(frame, Type.INT);
                    break;
                }
                case ByteOps.PUTSTATIC: {
                    Type fieldType = ((CstFieldRef) cst).getType();
                    machine.popArgs(frame, fieldType);
                    break;
                }
                case ByteOps.GETFIELD:
                case ByteOps.CHECKCAST:
                case ByteOps.INSTANCEOF: {
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.PUTFIELD: {
                    Type fieldType = ((CstFieldRef) cst).getType();
                    machine.popArgs(frame, Type.OBJECT, fieldType);
                    break;
                }
                case ByteOps.INVOKEINTERFACE: {
                    /*
                     * Convert the interface method ref into a normal
                     * method ref.
                     */
                    cst = ((CstInterfaceMethodRef) cst).toMethodRef();
                    // and fall through...
                }
                case ByteOps.INVOKEVIRTUAL:
                case ByteOps.INVOKESPECIAL: {
                    /*
                     * Get the instance prototype, and use it to direct
                     * the machine.
                     */
                    Prototype prototype = 
                        ((CstMethodRef) cst).getPrototype(false);
                    machine.popArgs(frame, prototype);
                    break;
                }
                case ByteOps.INVOKESTATIC: {
                    /*
                     * Get the static prototype, and use it to direct
                     * the machine.
                     */
                    Prototype prototype = 
                        ((CstMethodRef) cst).getPrototype(true);
                    machine.popArgs(frame, prototype);
                    break;
                }
                case ByteOps.MULTIANEWARRAY: {
                    /*
                     * The "value" here is the count of dimensions to
                     * create. Make a prototype of that many "int"
                     * types, and tell the machine to pop them. This
                     * isn't the most efficient way in the world to do
                     * this, but then again, multianewarray is pretty
                     * darn rare and so not worth much effort
                     * optimizing for.
                     */
                    Prototype prototype =
                        Prototype.internInts(Type.VOID, value);
                    machine.popArgs(frame, prototype);
                    break;
                }
                default: {
                    machine.clearArgs();
                    break;
                }
            }

            machine.auxIntArg(value);
            machine.auxCstArg(cst);
            machine.run(frame, offset, opcode);
        }

        /** {@inheritDoc} */
        public void visitBranch(int opcode, int offset, int length,
                int target) {
            switch (opcode) {
                case ByteOps.IFEQ:
                case ByteOps.IFNE:
                case ByteOps.IFLT:
                case ByteOps.IFGE:
                case ByteOps.IFGT:
                case ByteOps.IFLE: {
                    machine.popArgs(frame, Type.INT);
                    break;
                }
                case ByteOps.IFNULL:
                case ByteOps.IFNONNULL: {
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.IF_ICMPEQ:
                case ByteOps.IF_ICMPNE:
                case ByteOps.IF_ICMPLT:
                case ByteOps.IF_ICMPGE:
                case ByteOps.IF_ICMPGT:
                case ByteOps.IF_ICMPLE: {
                    machine.popArgs(frame, Type.INT, Type.INT);
                    break;
                }
                case ByteOps.IF_ACMPEQ:
                case ByteOps.IF_ACMPNE: {
                    machine.popArgs(frame, Type.OBJECT, Type.OBJECT);
                    break;
                }
                case ByteOps.GOTO:
                case ByteOps.JSR:
                case ByteOps.GOTO_W:
                case ByteOps.JSR_W: {
                    machine.clearArgs();
                    break;
                }
                default: {
                    visitInvalid(opcode, offset, length);
                    return;
                }
            }

            machine.auxTargetArg(target);
            machine.run(frame, offset, opcode);
        }

        /** {@inheritDoc} */
        public void visitSwitch(int opcode, int offset, int length,
                SwitchList cases, int padding) {
            machine.popArgs(frame, Type.INT);
            machine.auxIntArg(padding);
            machine.auxSwitchArg(cases);
            machine.run(frame, offset, opcode);
        }

        /** {@inheritDoc} */
        public void visitNewarray(int offset, int length, CstType type,
                ArrayList<Constant> initValues) {
            machine.popArgs(frame, Type.INT);
            machine.auxInitValues(initValues);
            machine.auxCstArg(type);
            machine.run(frame, offset, ByteOps.NEWARRAY);
        }

        /** {@inheritDoc} */
        public void setPreviousOffset(int offset) {
            previousOffset = offset;
        }

        /** {@inheritDoc} */
        public int getPreviousOffset() {
            return previousOffset;
        }
    }
}
