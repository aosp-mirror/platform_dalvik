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

package com.android.dx.gen;

import com.android.dx.rop.code.BasicBlockList;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.PlainCstInsn;
import com.android.dx.rop.code.PlainInsn;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Rop;
import static com.android.dx.rop.code.Rop.BRANCH_GOTO;
import static com.android.dx.rop.code.Rop.BRANCH_NONE;
import static com.android.dx.rop.code.Rop.BRANCH_RETURN;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.code.SourcePosition;
import com.android.dx.rop.code.ThrowingCstInsn;
import com.android.dx.rop.code.ThrowingInsn;
import com.android.dx.rop.type.StdTypeList;
import static com.android.dx.rop.type.Type.BT_BYTE;
import static com.android.dx.rop.type.Type.BT_CHAR;
import static com.android.dx.rop.type.Type.BT_INT;
import static com.android.dx.rop.type.Type.BT_SHORT;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Builds a sequence of instructions.
 */
public final class Code {
    private final Method<?, ?> method;
    /**
     * All allocated labels. Although the order of the labels in this list
     * shouldn't impact behavior, it is used to determine basic block indices.
     */
    private final List<Label> labels = new ArrayList<Label>();

    /**
     * The label currently receiving instructions. This is null if the most
     * recent instruction was a return or goto.
     */
    private Label currentLabel;

    /** true once we've fixed the positions of the parameter registers */
    private boolean localsInitialized;

    private final Local<?> thisLocal;
    private final List<Local<?>> parameters = new ArrayList<Local<?>>();
    private final List<Local<?>> locals = new ArrayList<Local<?>>();
    private SourcePosition sourcePosition = SourcePosition.NO_INFO;

    public Code(Method<?, ?> method) {
        this.method = method;
        thisLocal = method.isStatic()
                ? null
                : new Local<Object>(this, method.declaringType, Local.InitialValue.THIS);
        for (Type<?> parameter : method.parameters.types) {
            parameters.add(new Local<Object>(this, parameter, Local.InitialValue.PARAMETER));
        }
        this.currentLabel = newLabel();
        this.currentLabel.marked = true;
    }

    public <T> Local<T> newLocal(Type<T> type) {
        return allocateLocal(type, Local.InitialValue.NONE);
    }

    public <T> Local<T> getParameter(int index, Type<T> type) {
        return coerce(parameters.get(index), type);
    }

    public <T> Local<T> getThis(Type<T> type) {
        if (thisLocal == null) {
            throw new IllegalStateException("static methods cannot access 'this'");
        }
        return coerce(thisLocal, type);
    }

    @SuppressWarnings("unchecked") // guarded by an equals check
    private <T> Local<T> coerce(Local<?> local, Type<T> expectedType) {
        if (!local.type.equals(expectedType)) {
            throw new IllegalArgumentException(
                    "requested " + expectedType + " but was " + local.type);
        }
        return (Local<T>) local;
    }

    private <T> Local<T> allocateLocal(Type<T> type, Local.InitialValue initialValue) {
        if (localsInitialized) {
            throw new IllegalStateException("Cannot allocate locals after adding instructions");
        }
        Local<T> result = new Local<T>(this, type, initialValue);
        locals.add(result);
        return result;
    }

    /**
     * Assigns registers to locals. From the spec:
     *  "the N arguments to a method land in the last N registers of the
     *   method's invocation frame, in order. Wide arguments consume two
     *   registers. Instance methods are passed a this reference as their
     *   first argument."
     */
    void initializeLocals() {
        if (localsInitialized) {
            throw new AssertionError();
        }
        localsInitialized = true;

        int reg = 0;
        for (Local<?> local : locals) {
            reg += local.initialize(reg);
        }
        if (thisLocal != null) {
            reg += thisLocal.initialize(reg);
        }
        for (Local<?> local : parameters) {
            reg += local.initialize(reg);
        }
    }

    // labels

    /**
     * Creates a new label for use as a branch target. The new label must have
     * code attached to it later by calling {@link #mark(Label)}.
     */
    public Label newLabel() {
        Label result = new Label();
        labels.add(result);
        return result;
    }

    /**
     * Start defining instructions for the named label.
     */
    public void mark(Label label) {
        if (label.marked) {
            throw new IllegalStateException("already marked");
        }
        label.marked = true;
        if (currentLabel != null) {
            jump(label); // blocks must end with a branch, return or throw
        }
        currentLabel = label;
    }

    private void addInstruction(Insn insn) {
        addInstruction(insn, null);
    }

    private void addInstruction(Insn insn, Label branch) {
        if (currentLabel == null || !currentLabel.marked) {
            throw new IllegalStateException("no current label");
        }
        currentLabel.instructions.add(insn);

        switch (insn.getOpcode().getBranchingness()) {
        case BRANCH_NONE:
            if (branch != null) {
                throw new IllegalArgumentException("branch != null");
            }
            return;

        case BRANCH_RETURN:
            if (branch != null) {
                throw new IllegalArgumentException("branch != null");
            }
            currentLabel = null;
            break;

        case BRANCH_GOTO:
            if (branch == null) {
                throw new IllegalArgumentException("branch == null");
            }
            currentLabel.primarySuccessor = branch;
            currentLabel = null;
            break;

        case Rop.BRANCH_IF:
            if (branch == null) {
                throw new IllegalArgumentException("branch == null");
            }
            splitCurrentLabel(branch);
            break;

        case Rop.BRANCH_THROW:
            splitCurrentLabel(branch);
            break;

        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Closes the current label and starts a new one.
     */
    private void splitCurrentLabel(Label branch) {
        Label newLabel = newLabel();
        currentLabel.primarySuccessor = newLabel;
        currentLabel.alternateSuccessor = branch;
        currentLabel = newLabel;
        currentLabel.marked = true;
    }

    // instructions: constants

    public <T> void loadConstant(Local<T> target, T value) {
        Rop rop = Rops.opConst(target.type.ropType);
        if (rop.getBranchingness() == BRANCH_NONE) {
            addInstruction(new PlainCstInsn(rop, sourcePosition, target.spec(),
                    RegisterSpecList.EMPTY, Constants.getConstant(value)));
        } else {
            addInstruction(new ThrowingCstInsn(rop, sourcePosition,
                    RegisterSpecList.EMPTY, StdTypeList.EMPTY, Constants.getConstant(value)));
            moveResult(target, true);
        }
    }

    // instructions: unary

    public <T> void negate(Local<T> source, Local<T> target) {
        unary(Rops.opNeg(source.type.ropType), source, target);
    }

    public <T> void not(Local<T> source, Local<T> target) {
        unary(Rops.opNot(source.type.ropType), source, target);
    }

    public void numericCast(Local<?> source, Local<?> target) {
        unary(getCastRop(source.type.ropType, target.type.ropType), source, target);
    }

    private Rop getCastRop(com.android.dx.rop.type.Type sourceType,
            com.android.dx.rop.type.Type targetType) {
        if (sourceType.getBasicType() == BT_INT) {
            switch (targetType.getBasicType()) {
            case BT_SHORT:
                return Rops.TO_SHORT;
            case BT_CHAR:
                return Rops.TO_CHAR;
            case BT_BYTE:
                return Rops.TO_BYTE;
            }
        }
        return Rops.opConv(targetType, sourceType);
    }

    private void unary(Rop rop, Local<?> source, Local<?> target) {
        addInstruction(new PlainInsn(rop, sourcePosition, target.spec(), source.spec()));
    }

    // instructions: binary

    public <T> void op(BinaryOp op, Local<T> target, Local<T> a, Local<T> b) {
        Rop rop = op.rop(StdTypeList.make(a.type.ropType, b.type.ropType));
        RegisterSpecList sources = RegisterSpecList.make(a.spec(), b.spec());

        if (rop.getBranchingness() == BRANCH_NONE) {
            addInstruction(new PlainInsn(rop, sourcePosition, target.spec(), sources));
        } else {
            addInstruction(new ThrowingInsn(rop, sourcePosition, sources, StdTypeList.EMPTY));
            moveResult(target, true);
        }
    }

    // instructions: branches

    /**
     * Compare integers. If the comparison is true, execution jumps to {@code
     * trueLabel}. If it is false, execution continues to the next instruction.
     */
    public <T> void compare(Comparison comparison, Local<T> a, Local<T> b, Label trueLabel) {
        if (trueLabel == null) {
            throw new IllegalArgumentException();
        }
        Rop rop = comparison.rop(StdTypeList.make(a.type.ropType, b.type.ropType));
        addInstruction(new PlainInsn(rop, sourcePosition, null,
                RegisterSpecList.make(a.spec(), b.spec())), trueLabel);
    }

    public void jump(Label target) {
        addInstruction(new PlainInsn(Rops.GOTO, sourcePosition, null, RegisterSpecList.EMPTY),
                target);
    }

    // instructions: fields

    public <T, R> void iget(Field<T, R> field, Local<T> instance, Local<R> target) {
        addInstruction(new ThrowingCstInsn(Rops.opGetField(target.type.ropType), sourcePosition,
                RegisterSpecList.make(instance.spec()), StdTypeList.EMPTY, field.constant));
        moveResult(target, true);
    }

    public <T, R> void iput(Field<T, R> field, Local<T> instance, Local<R> source) {
        addInstruction(new ThrowingCstInsn(Rops.opPutField(source.type.ropType), sourcePosition,
                RegisterSpecList.make(source.spec(), instance.spec()), StdTypeList.EMPTY,
                field.constant));
    }

    public <T> void sget(Field<?, T> field, Local<T> target) {
        addInstruction(new ThrowingCstInsn(Rops.opGetStatic(target.type.ropType), sourcePosition,
                RegisterSpecList.EMPTY, StdTypeList.EMPTY, field.constant));
        moveResult(target, true);
    }

    public <T> void sput(Field<?, T> field, Local<T> source) {
        addInstruction(new ThrowingCstInsn(Rops.opPutStatic(source.type.ropType), sourcePosition,
                RegisterSpecList.make(source.spec()), StdTypeList.EMPTY, field.constant));
    }

    // instructions: invoke

    public <T> void newInstance(Local<T> target, Method<T, Void> constructor, Local<?>... args) {
        if (target == null) {
            throw new IllegalArgumentException();
        }
        addInstruction(new ThrowingCstInsn(Rops.NEW_INSTANCE, sourcePosition,
                RegisterSpecList.EMPTY, StdTypeList.EMPTY, constructor.declaringType.constant));
        moveResult(target, true);
        invokeDirect(constructor, null, target, args);
    }

    public <R> void invokeStatic(Method<?, R> method, Local<? super R> target, Local<?>... args) {
        invoke(Rops.opInvokeStatic(method.prototype(true)), method, target, null, args);
    }

    public <I, R> void invokeVirtual(Method<I, R> method, Local<? super R> target,
            Local<? extends I> object, Local<?>... args) {
        invoke(Rops.opInvokeVirtual(method.prototype(true)), method, target, object, args);
    }

    public <I, R> void invokeDirect(Method<I, R> method, Local<? super R> target,
            Local<? extends I> object, Local<?>... args) {
        invoke(Rops.opInvokeDirect(method.prototype(true)), method, target, object, args);
    }

    public <I, R> void invokeSuper(Method<I, R> method, Local<? super R> target,
            Local<? extends I> object, Local<?>... args) {
        invoke(Rops.opInvokeSuper(method.prototype(true)), method, target, object, args);
    }

    public <I, R> void invokeInterface(Method<I, R> method, Local<? super R> target,
            Local<? extends I> object, Local<?>... args) {
        invoke(Rops.opInvokeInterface(method.prototype(true)), method, target, object, args);
    }

    private <I, R> void invoke(Rop rop, Method<I, R> method, Local<? super R> target,
            Local<? extends I> object, Local<?>... args) {
        addInstruction(new ThrowingCstInsn(rop, sourcePosition, concatenate(object, args),
                StdTypeList.EMPTY, method.constant));
        if (target != null) {
            moveResult(target, false);
        }
    }

    // instructions: types

    public void instanceOfType(Local<?> target, Local<?> source, Type<?> type) {
        addInstruction(new ThrowingCstInsn(Rops.INSTANCE_OF, sourcePosition,
                RegisterSpecList.make(source.spec()), StdTypeList.EMPTY, type.constant));
        moveResult(target, true);
    }

    public void typeCast(Local<?> source, Local<?> target) {
        addInstruction(new ThrowingCstInsn(Rops.CHECK_CAST, sourcePosition,
                RegisterSpecList.make(source.spec()), StdTypeList.EMPTY, target.type.constant));
        moveResult(target, true);
    }

    // instructions: return

    public void returnVoid() {
        if (!method.returnType.isVoid()) {
            throw new IllegalArgumentException("declared " + method.returnType
                    + " but returned void");
        }
        addInstruction(new PlainInsn(Rops.RETURN_VOID, sourcePosition, null,
                RegisterSpecList.EMPTY));
    }

    public void returnValue(Local<?> result) {
        if (!result.type.equals(method.returnType)) {
            // TODO: this is probably too strict.
            throw new IllegalArgumentException("declared " + method.returnType
                    + " but returned " + result.type);
        }
        addInstruction(new PlainInsn(Rops.opReturn(result.type.ropType), sourcePosition,
                null, RegisterSpecList.make(result.spec())), null);
    }

    private void moveResult(Local<?> target, boolean afterNonInvokeThrowingInsn) {
        Rop rop = afterNonInvokeThrowingInsn
                ? Rops.opMoveResultPseudo(target.type.ropType)
                : Rops.opMoveResult(target.type.ropType);
        addInstruction(new PlainInsn(rop, sourcePosition, target.spec(), RegisterSpecList.EMPTY));
    }

    // produce BasicBlocks for dex

    BasicBlockList toBasicBlocks() {
        cleanUpLabels();

        BasicBlockList result = new BasicBlockList(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            result.set(i, labels.get(i).toBasicBlock());
        }
        return result;
    }

    /**
     * Removes empty labels and assigns IDs to non-empty labels.
     */
    private void cleanUpLabels() {
        int id = 0;
        for (Iterator<Label> i = labels.iterator(); i.hasNext();) {
            Label label = i.next();
            if (label.isEmpty()) {
                i.remove();
            } else {
                label.compact();
                label.id = id++;
            }
        }
    }

    private static RegisterSpecList concatenate(Local<?> first, Local<?>[] rest) {
        int offset = (first != null) ? 1 : 0;
        RegisterSpecList result = new RegisterSpecList(offset + rest.length);
        if (first != null) {
            result.set(0, first.spec());
        }
        for (int i = 0; i < rest.length; i++) {
            result.set(i + offset, rest[i].spec());
        }
        return result;
    }
}
