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

package com.android.dx.ssa;

import com.android.dx.rop.code.*;
import com.android.dx.util.ToHuman;

/**
 * An instruction in SSA form
 */
public abstract class SsaInsn implements ToHuman, Cloneable {

    protected RegisterSpec result;
    protected final SsaBasicBlock block;

    /**
     * Constructs an instance
     * @param block block containing this insn. Can never change.
     */
    protected SsaInsn(final SsaBasicBlock block) {
        this.block = block;
    }

    /**
     * Makes a new SSA insn form a ROP insn
     *
     * @param insn non-null; rop insn
     * @param block non-null; owning block
     * @return non-null; an appropriately constructed instance
     */
    public static SsaInsn makeFromRop(Insn insn, SsaBasicBlock block) {
        return new NormalSsaInsn(insn, block);
    }

    /** {@inheritDoc} */
    @Override
    public SsaInsn clone() {
        try {
            return (SsaInsn)super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException ("unexpected", ex);
        }
    }

    /**
     * Like {@link com.android.dx.rop.code.Insn getResult()}.
     * @return result register
     */
    public RegisterSpec getResult() {
        return result;
    }

    /**
     * Like {@link com.android.dx.rop.code.Insn getSources()}.
     * @return non-null; sources list
     */
    abstract public RegisterSpecList getSources();

    /**
     * Gets the block to which this insn instance belongs.
     *
     * @return owning block
     */
    public SsaBasicBlock getBlock() {
        return block;
    }

    /**
     * is the specified reg the result reg?
     * @param reg register to test
     * @return true if there is a result and it is stored in the specified
     * register
     */
    public boolean isResultReg(int reg) {
        return result != null && result.getReg() == reg;
    }


    /**
     * Changes the result register if this insn has a result.
     * Used during renaming.
     * @param reg new result register.
     */
    public void changeResultReg(int reg) {
        if (result != null) {
            result = result.withReg(reg);
        }
    }

    /**
     * Sets the local association for the result of this insn.
     * This is sometimes updated during the SsaRenamer process.
     *
     * @param local null-ok; New debug/local variable info.
     */
    public final void setResultLocal(LocalItem local) {
        LocalItem oldItem = result.getLocalItem();

        if (local != oldItem && (local == null
                || !local.equals(result.getLocalItem()))) {
            result = RegisterSpec.makeLocalOptional(
                    result.getReg(), result.getType(), local);
        }
    }

    /**
     * Map registers after register allocation.
     *
     * @param mapper
     */
    public final void mapRegisters(RegisterMapper mapper) {
        RegisterSpec oldResult = result;
        result = mapper.map(result);
        block.getParent().updateOneDefinition(this, oldResult);
        mapSourceRegisters(mapper);        
    }

    /**
     * Maps only source registers.
     *
     * @param mapper new mapping
     */
    abstract public void mapSourceRegisters(RegisterMapper mapper);


    /**
     * Returns the Rop opcode for this insn, or null if this is a phi insn
     *
     * TODO move this up into NormalSsaInsn
     *
     * @return null-ok; Rop opcode if there is one.
     */
    abstract public Rop getOpcode();

    /**
     * Returns the original Rop insn for this insn, or null if this is
     * a phi insn.
     * 
     * TODO move this up into NormalSsaInsn
     *
     * @return null-ok; Rop insn if there is one.
     */
    abstract public Insn getOriginalRopInsn();

    /**
     * Gets the spec of a local variable assignment that occurs at this
     * instruction, or null if no local variable assignment occurs. This
     * may be the result register, or for <code>mark-local</code> insns
     * it may be the source.
     *
     * @return null-ok; a local-associated register spec or null
     * @see com.android.dx.rop.code.Insn#getLocalAssignment() 
     */
    public RegisterSpec getLocalAssignment() {
        if (result != null && result.getLocalItem() != null) {
            return result;
        }

        return null;
    }

    /**
     * Indicates whether the specified register is amongst the registers
     * used as sources for this instruction.
     * @param reg The register in question
     * @return true if the reg is a source
     */
    public boolean isRegASource(int reg) {
        return null != getSources().specForRegister(reg);
    }

    /**
     * Transform back to ROP form.
     *
     * TODO move this up into NormalSsaInsn
     *
     * @return non-null; a ROP representation of this instruction, with
     * updated registers.
     */
    public abstract Insn toRopInsn();

    /**
     * @return true if this is a PhiInsn or a normal move insn
     */
    public abstract boolean isPhiOrMove();

    /**
     * Returns true if this insn is considered to have a side effect beyond
     * that of assigning to the result reg.
     * 
     * @return true if this insn is considered to have a side effect beyond
     * that of assigning to the result reg.
     */
    public abstract boolean hasSideEffect();

    /**
     * @return true if this is a move (but not a move-operand or move-exception)
     * instruction
     */
    public boolean isNormalMoveInsn() {
        return false;
    }

    /**
     * @return true if this is a move-exception instruction.
     * These instructions must immediately follow a preceeding invoke*
     */
    public boolean isMoveException() {
        return false;
    }

    /**
     * @return true if this instruction can throw.
     */
    abstract public boolean canThrow();

    /**
     * accepts a visitor
     * @param v visitor
     */
    public abstract void accept(Visitor v);

    /**
     * Visitor interface for this class.
     */
    public static interface Visitor {

        /**
         * Any non-phi move instruction
         * @param insn non-null; the instruction to visit
         */
        public void visitMoveInsn(NormalSsaInsn insn);

        /**
         * Any phi insn
         * @param insn non-null; the instruction to visit
         */
        public void visitPhiInsn(PhiInsn insn);

        /**
         * Any insn that isn't a move or a phi (which is also a move).
         * @param insn non-null; the instruction to visit
         */
        public void visitNonMoveInsn(NormalSsaInsn insn);
    }
}
