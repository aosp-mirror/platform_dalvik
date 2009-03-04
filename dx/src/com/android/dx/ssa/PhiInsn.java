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
import com.android.dx.rop.type.Type;
import com.android.dx.rop.type.TypeBearer;
import com.android.dx.util.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * A Phi instruction (magical post-control-flow-merge) instruction
 * in SSA form. Will be converted to moves in predecessor blocks before
 * conversion back to ROP form.
 */
public final class PhiInsn extends SsaInsn {

    /**
     * the original result register of the phi insn is needed during the
     * renaming process after the new result register has already been chosen.
     */
    private int ropResultReg;
    private ArrayList<Operand> operands = new ArrayList<Operand>();
    private RegisterSpecList sources;

    /**
     * A single phi operand, consiting of source register and block index
     * for move.
     */
    class Operand {
        RegisterSpec regSpec;
        int blockIndex;
        int ropLabel;       //mostly for debugging

        Operand (final RegisterSpec regSpec, final int blockIndex,
                final int ropLabel){
            this.regSpec = regSpec;
            this.blockIndex = blockIndex;
            this.ropLabel = ropLabel;
        }
    }

    public static interface Visitor {
        public void visitPhiInsn(PhiInsn insn);
    }

    public PhiInsn clone() {
        throw new UnsupportedOperationException("can't clone phi");
    }

    /**
     * Constructs a new phi insn with no operands.
     * @param resultReg the result reg for this phi insn
     * @param block block containing this insn.
     */
    PhiInsn(final RegisterSpec resultReg, final SsaBasicBlock block) {
        super(block);
        this.result = resultReg;
        ropResultReg = resultReg.getReg();
    }

    /**
     * Makes a phi insn with a void result type.
     * @param resultReg the result register for this phi insn.
     * @param block block containing this insn.
     */
    PhiInsn(final int resultReg, final SsaBasicBlock block) {
        super(block);

        /*
         * The type here is bogus: the type depends on the operand and
         * will be derived later.
         */
        this.result = RegisterSpec.make(resultReg, Type.VOID);
        ropResultReg = resultReg;
    }

    /**
     * Updates the TypeBearers of all the sources (phi operands) to be
     * the current TypeBearer of the register-defining instruction's result.
     * This is used during phi-type resolution.<p>
     *
     * Note that local association of operands are preserved in this step.
     *
     * @param ssaMeth method that contains this insn
     */
    void updateSourcesToDefinitions(SsaMethod ssaMeth) {

        for (Operand o: operands) {
            RegisterSpec def 
                = ssaMeth.getDefinitionForRegister(
                    o.regSpec.getReg()).getResult();

            o.regSpec = o.regSpec.withType(def.getType());
        }

        sources = null;
    }

    /**
     * Changes the result type. Used during phi type resolution
     *
     * @param type non-null; new TypeBearer
     * @param local null-ok; new local info, if available
     */
    void changeResultType(TypeBearer type, LocalItem local) {
        result = RegisterSpec.makeLocalOptional(result.getReg(), type, local);
    }

    /**
     * @return the original rop-form result reg. Useful during renaming.
     */
    int getRopResultReg() {
        return ropResultReg;
    }

    /**
     * Add an operand to this phi instruction
     * @param registerSpec register spec, including type and reg of operand
     * @param predBlock Predecessor block to be associated with this operand
     */
    public void addPhiOperand(RegisterSpec registerSpec,
            SsaBasicBlock predBlock) {
        operands.add(new Operand(registerSpec, predBlock.getIndex(),
                predBlock.getRopLabel()));
        
        // in case someone has already called getSources()
        sources = null;
    }

    /**
     * Gets the index of the pred block associated with the RegisterSpec
     * at the particular getSources() index.
     * @param sourcesIndex index of source in getSources()
     * @return block index
     */
    public int predBlockIndexForSourcesIndex(int sourcesIndex) {
        return operands.get(sourcesIndex).blockIndex;
    }

    /**
     * {@inheritDoc}
     *
     * Always returns null for <code>PhiInsn</code>s
     */
    @Override
    public Rop getOpcode() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Always returns null for <code>PhiInsn</code>s
     */
    @Override
    public Insn getOriginalRopInsn() {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * Always returns false for <code>PhiInsn</code>s
     */
    @Override
    public boolean canThrow() {
        return false;
    }

    /**
     * Gets sources. Constructed lazily from phi operand data structures and
     * then cached.
     * @return sources list
     */
    public RegisterSpecList getSources() {

        if (sources != null) {
            return sources;
        }

        if (operands.size() == 0) {
            // How'd this happen? A phi insn with no operand?
            return RegisterSpecList.EMPTY;
        }

        int szSources = operands.size();
        sources = new RegisterSpecList(szSources);

        for (int i = 0; i < szSources; i++) {
            Operand o = operands.get(i);

            sources.set(i, o.regSpec);
        }

        sources.setImmutable();
        return sources;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRegASource(int reg) {
        /*
         * Avoid creating a sources list in case it has not already been
         * created
         */

        for (Operand o: operands) {
            if (o.regSpec.getReg() == reg) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if all operands use the same register
     */
    public boolean areAllOperandsEqual() {
        if (operands.size() == 0 ) {
            // this should never happen
            return true;
        }

        int firstReg = operands.get(0).regSpec.getReg();
        for (Operand o: operands) {
            if (firstReg != o.regSpec.getReg()) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final void mapSourceRegisters(RegisterMapper mapper) {
        for (Operand o: operands) {
            RegisterSpec old = o.regSpec;
            o.regSpec = mapper.map(old);
            if (old != o.regSpec) {
                block.getParent().onSourceChanged(this, old, o.regSpec);
            }
        }
        sources = null;
    }

    /**
     * Always throws an exeption, since
     * a phi insn may not be converted back to rop form
     * @return always throws exception
     */
    @Override
    public Insn toRopInsn() {
        throw new IllegalArgumentException(
                "Cannot convert phi insns to rop form");
    }

    /**
     * Returns the list of predecessor blocks associated with all operands
     * that have <code>reg</code> as an operand register.
     *
     * @param reg register to look up
     * @param ssaMeth method we're operating on
     * @return List of predecessor blocks, empty if none
     */
    public List<SsaBasicBlock> predBlocksForReg (int reg, SsaMethod ssaMeth) {
        ArrayList<SsaBasicBlock> ret 
            = (ArrayList<SsaBasicBlock>)new ArrayList();

        for (Operand o: operands) {
            if (o.regSpec.getReg() == reg) {
                ret.add(ssaMeth.getBlocks().get(o.blockIndex));
            }
        }

        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public  boolean isPhiOrMove() {
        return true;    
    }

    /** {@inheritDoc} */
    @Override public boolean hasSideEffect() {
        return Optimizer.getPreserveLocals() && getLocalAssignment() != null;
    }

    /** {@inheritDoc} */
    @Override
    public void accept(SsaInsn.Visitor v) {
        v.visitPhiInsn(this);
    }

    /**
     * @return human-readable string for listing dumps
     */
    public String toHuman() {
        return toHumanWithInline(null);
    }

    /**
     * Returns human-readable string for listing dumps.
     * Allows sub-classes to specify extra text
     * @param extra null-ok; the argument to print after the opcode
     * @return human-readable string for listing dumps
     */
    protected final String toHumanWithInline(String extra) {
        StringBuffer sb = new StringBuffer(80);

        sb.append(SourcePosition.NO_INFO);
        sb.append(": ");
        sb.append("phi");       

        if (extra != null) {
            sb.append("(");
            sb.append(extra);
            sb.append(")");
        }

        if (result == null) {
            sb.append(" .");
        } else {
            sb.append(" ");
            sb.append(result.toHuman());
        }

        sb.append(" <-");

        int sz = getSources().size();
        if (sz == 0) {
            sb.append(" .");
        } else {
            for (int i = 0; i < sz; i++) {
                sb.append(" ");
                sb.append(sources.get(i).toHuman()
                        + "[b="
                        + Hex.u2(operands.get(i).ropLabel)  + "]");
            }
        }

        return sb.toString();
    }
}
