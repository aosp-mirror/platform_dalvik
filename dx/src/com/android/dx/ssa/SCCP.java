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

import com.android.dx.rop.code.CstInsn;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.RegOps;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.cst.Constant;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.TypedConstant;
import com.android.dx.rop.type.TypeBearer;
import com.android.dx.rop.type.Type;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * A small variant of Wegman and Zadeck's Sparse Conditional Constant
 * Propagation algorithm.
 */
public class SCCP {
    /** Lattice values  */
    private static final int TOP = 0;
    private static final int CONSTANT = 1;
    private static final int VARYING = 2;
    /** method we're processing */
    private SsaMethod ssaMeth;
    /** ssaMeth.getRegCount() */
    private int regCount;
    /** Lattice values for each SSA register */
    private int[] latticeValues;
    /** For those registers that are constant, this is the constant value */
    private Constant[] latticeConstants;
    /** Worklist of basic blocks to be processed */
    private ArrayList<SsaBasicBlock> cfgWorklist;
    /** Bitset containing bits for each block that has been found executable */
    private BitSet executableBlocks;
    /** Worklist for SSA edges.  This is a list of registers to process */
    private ArrayList<SsaInsn> ssaWorklist;
    /**
     * Worklist for SSA edges that represent varying values.  It makes the
     * algorithm much faster if you move all values to VARYING as fast as
     * possible.
     */
    private ArrayList<SsaInsn> varyingWorklist;

    private SCCP(SsaMethod ssaMeth) {
        this.ssaMeth = ssaMeth;
        this.regCount = ssaMeth.getRegCount();
        this.latticeValues = new int[this.regCount];
        this.latticeConstants = new Constant[this.regCount];
        this.cfgWorklist = new ArrayList<SsaBasicBlock>();
        this.executableBlocks = new BitSet(ssaMeth.getBlocks().size());
        this.ssaWorklist = new ArrayList<SsaInsn>();
        this.varyingWorklist = new ArrayList<SsaInsn>();
        for (int i = 0; i < this.regCount; i++) {
            latticeValues[i] = TOP;
            latticeConstants[i] = null;
        }
    }

    /**
     * Performs sparse conditional constant propagation on a method.
     * @param ssaMethod Method to process
     */
    public static void process (SsaMethod ssaMethod) {
        new SCCP(ssaMethod).run();
    }

    /**
     * Add a new SSA basic block to the CFG worklist
     * @param ssaBlock Block to add
     */
    private void addBlockToWorklist(SsaBasicBlock ssaBlock) {
        if (!executableBlocks.get(ssaBlock.getIndex())) {
            cfgWorklist.add(ssaBlock);
            executableBlocks.set(ssaBlock.getIndex());
        }
    }

    /**
     * Adds an SSA register's uses to the SSA worklist.
     * @param reg SSA register
     * @param latticeValue new lattice value for @param reg.
     */
    private void addUsersToWorklist(int reg, int latticeValue) {
        if (latticeValue == VARYING) {
            for (SsaInsn insn : ssaMeth.getUseListForRegister(reg)) {
                varyingWorklist.add(insn);
            }
        } else {
            for (SsaInsn insn : ssaMeth.getUseListForRegister(reg)) {
                ssaWorklist.add(insn);
            }
        }
    }

    /**
     * Sets a lattice value for a register to value.
     * @param reg SSA register
     * @param value Lattice value
     * @param cst Constant value (may be null)
     * @return true if the lattice value changed.
     */
    private boolean setLatticeValueTo(int reg, int value, Constant cst) {
        if (value != CONSTANT) {
            if (latticeValues[reg] != value) {
                latticeValues[reg] = value;
                return true;
            }
            return false;
        } else {
            if (latticeValues[reg] != value
                    || !latticeConstants[reg].equals(cst)) {
                latticeValues[reg] = value;
                latticeConstants[reg] = cst;
                return true;
            }
            return false;
        }
    }
    
    /**
     * Simulates a PHI node and set the lattice for the result
     * to the approriate value.
     * Meet values:
     * TOP x anything = anything
     * VARYING x anything = VARYING
     * CONSTANT x CONSTANT = CONSTANT if equal constants, VARYING otherwise
     * @param insn PHI to simulate.
     */
    private void simulatePhi(PhiInsn insn) {
        int phiResultReg = insn.getResult().getReg();

        if (latticeValues[phiResultReg] == VARYING) {
            return;
        }

        RegisterSpecList sources = insn.getSources();
        int phiResultValue = TOP;
        Constant phiConstant = null;
        int sourceSize = sources.size();

        for (int i = 0; i < sourceSize; i++) {
            int predBlockIndex = insn.predBlockIndexForSourcesIndex(i);
            int sourceReg = sources.get(i).getReg();
            int sourceRegValue = latticeValues[sourceReg];

            if (!executableBlocks.get(predBlockIndex)
                    || sourceRegValue == TOP) {
                continue;
            }

            if (sourceRegValue == CONSTANT) {
                if (phiConstant == null) {
                    phiConstant = latticeConstants[sourceReg];
                    phiResultValue = CONSTANT;
                 } else if (!latticeConstants[sourceReg].equals(phiConstant)){
                    phiResultValue = VARYING;
                    break;
                }

            } else if (sourceRegValue == VARYING) {
                phiResultValue = VARYING;
                break;
            }
        }
        if (setLatticeValueTo(phiResultReg, phiResultValue, phiConstant)) {
            addUsersToWorklist(phiResultReg, phiResultValue);
        }
    }

    /**
     * Simulate a block and note the results in the lattice.
     * @param block Block to visit
     */
    private void simulateBlock(SsaBasicBlock block) {
        for (SsaInsn insn : block.getInsns()) {
            if (insn instanceof PhiInsn) {
                simulatePhi((PhiInsn) insn);
            } else {
                simulateStmt(insn);
            }
        }
    }
    private static String latticeValName(int latticeVal) {
        switch (latticeVal) {
            case TOP: return "TOP";
            case CONSTANT: return "CONSTANT";
            case VARYING: return "VARYING";
            default: return "UNKNOWN";
        }
    }

    /**
     * Simplifies a jump statement.
     * @param insn jump to simplify
     * @return an instruction representing the simplified jump.
     */
    private Insn simplifyJump (Insn insn) {
        return insn;
    }

    /**
     * Simulates math insns, if possible.
     *
     * @param insn non-null insn to simulate
     * @return constant result or null if not simulatable.
     */
    private Constant simulateMath(SsaInsn insn) {
        Insn ropInsn = insn.getOriginalRopInsn();
        int opcode = insn.getOpcode().getOpcode();
        RegisterSpecList sources = insn.getSources();
        int regA = sources.get(0).getReg();
        Constant cA;
        Constant cB;

        if (latticeValues[regA] != CONSTANT) {
            cA = null;
        } else {
            cA = latticeConstants[regA];
        }

        if (sources.size() == 1) {
            CstInsn cstInsn = (CstInsn) ropInsn;
            cB = cstInsn.getConstant();
        } else { /* sources.size() == 2 */
            int regB = sources.get(1).getReg();
            if (latticeValues[regB] != CONSTANT) {
                cB = null;
            } else {
                cB = latticeConstants[regB];
            }
        }

        if (cA == null || cB == null) {
            //TODO handle a constant of 0 with MUL or AND
            return null;
        }

        switch (insn.getResult().getBasicType()) {
            case Type.BT_INT:
                int vR;
                boolean skip=false;

                int vA = ((CstInteger) cA).getValue();
                int vB = ((CstInteger) cB).getValue();

                switch (opcode) {
                    case RegOps.ADD:
                        vR = vA + vB;
                        break;
                    case RegOps.SUB:
                        vR = vA - vB;
                        break;
                    case RegOps.MUL:
                        vR = vA * vB;
                        break;
                    case RegOps.DIV:
                        if (vB == 0) {
                            skip = true;
                            vR = 0; // just to hide a warning
                        } else {
                            vR = vA / vB;
                        }
                        break;
                    case RegOps.AND:
                        vR = vA & vB;
                        break;
                    case RegOps.OR:
                        vR = vA | vB;
                        break;
                    case RegOps.XOR:
                        vR = vA ^ vB;
                        break;
                    case RegOps.SHL:
                        vR = vA << vB;
                        break;
                    case RegOps.SHR:
                        vR = vA >> vB;
                        break;
                    case RegOps.USHR:
                        vR = vA >>> vB;
                        break;
                    case RegOps.REM:
                        vR = vA % vB;
                        break;
                    default:
                        throw new RuntimeException("Unexpected op");
                }

                return skip ? null : CstInteger.make(vR);

            default:
                // not yet supported
                return null;
        }
    }

    /**
     * Simulates a statement and set the result lattice value.
     * @param insn instruction to simulate
     */
    private void simulateStmt(SsaInsn insn) {
        Insn ropInsn = insn.getOriginalRopInsn();
        if (ropInsn.getOpcode().getBranchingness() != Rop.BRANCH_NONE
                || ropInsn.getOpcode().isCallLike()) {
            ropInsn = simplifyJump (ropInsn);
            /* TODO: If jump becomes constant, only take true edge. */
            SsaBasicBlock block = insn.getBlock();
            int successorSize = block.getSuccessorList().size();
            for (int i = 0; i < successorSize; i++) {
                int successor = block.getSuccessorList().get(i);
                addBlockToWorklist(ssaMeth.getBlocks().get(successor));
            }
        }

        if (insn.getResult() == null) {
            return;
        }

        /* TODO: Simplify statements when possible using the constants. */
        int resultReg = insn.getResult().getReg();
        int resultValue = VARYING;
        Constant resultConstant = null;
        int opcode = insn.getOpcode().getOpcode();
        switch (opcode) {
            case RegOps.CONST: {
                CstInsn cstInsn = (CstInsn)ropInsn;
                resultValue = CONSTANT;
                resultConstant = cstInsn.getConstant();
                break;
            }
            case RegOps.MOVE: {
                if (insn.getSources().size() == 1) {
                    int sourceReg = insn.getSources().get(0).getReg();
                    resultValue = latticeValues[sourceReg];
                    resultConstant = latticeConstants[sourceReg];
                }
                break;
            }

            case RegOps.ADD:
            case RegOps.SUB:
            case RegOps.MUL:
            case RegOps.DIV:
            case RegOps.AND:
            case RegOps.OR:
            case RegOps.XOR:
            case RegOps.SHL:
            case RegOps.SHR:
            case RegOps.USHR:
            case RegOps.REM:

                resultConstant = simulateMath(insn);

                if (resultConstant == null) {
                    resultValue = VARYING;
                } else {
                    resultValue = CONSTANT;
                }
            break;
            /* TODO: Handle non-int arithmetic.
               TODO: Eliminate check casts that we can prove the type of. */
            default: {}
        }
        if (setLatticeValueTo(resultReg, resultValue, resultConstant)) {
            addUsersToWorklist(resultReg, resultValue);
        }
    }

    private void run() {
        SsaBasicBlock firstBlock = ssaMeth.getEntryBlock();
        addBlockToWorklist(firstBlock);

        /* Empty all the worklists by propagating our values */
        while (!cfgWorklist.isEmpty()
                || !ssaWorklist.isEmpty()
                || !varyingWorklist.isEmpty()) {
            while (!cfgWorklist.isEmpty()) {
                int listSize = cfgWorklist.size() - 1;
                SsaBasicBlock block = cfgWorklist.remove(listSize);
                simulateBlock(block);
            }
            while (!varyingWorklist.isEmpty()) {
                int listSize = varyingWorklist.size() - 1;
                SsaInsn insn = varyingWorklist.remove(listSize);

                if (!executableBlocks.get(insn.getBlock().getIndex())) {
                    continue;
                }

                if (insn instanceof PhiInsn) {
                    simulatePhi((PhiInsn)insn);
                } else {
                    simulateStmt(insn);
                }
            }
            while (!ssaWorklist.isEmpty()) {
                int listSize = ssaWorklist.size() - 1;
                SsaInsn insn = ssaWorklist.remove(listSize);

                if (!executableBlocks.get(insn.getBlock().getIndex())) {
                    continue;
                }

                if (insn instanceof PhiInsn) {
                    simulatePhi((PhiInsn)insn);
                } else {
                    simulateStmt(insn);
                }
            }
        }

        replaceConstants();
    }

    /**
     * Replaces TypeBearers in source register specs with constant type
     * bearers if possible. These are then referenced in later optimization
     * steps.
     */
    private void replaceConstants() {
        for (int reg = 0; reg < regCount; reg++) {            
            if (latticeValues[reg] != CONSTANT) {
                continue;
            }
            if (!(latticeConstants[reg] instanceof TypedConstant)) {
                // We can't do much with these
                continue;
            }

            SsaInsn defn = ssaMeth.getDefinitionForRegister(reg);
            TypeBearer typeBearer = defn.getResult().getTypeBearer();

            if (typeBearer.isConstant()) {
                /*
                 * The definition was a constant already.
                 * The uses should be as well.
                 */
                continue;
            }

            /*
             * Update the sources RegisterSpec's of all non-move uses.
             * These will be used in later steps.
             */
            for (SsaInsn insn : ssaMeth.getUseListForRegister(reg)) {
                if (insn.isPhiOrMove()) {
                    continue;
                }

                NormalSsaInsn nInsn = (NormalSsaInsn) insn;
                RegisterSpecList sources = insn.getSources();

                int index = sources.indexOfRegister(reg);

                RegisterSpec spec = sources.get(index);
                RegisterSpec newSpec
                        = spec.withType((TypedConstant)latticeConstants[reg]);

                nInsn.changeOneSource(index, newSpec);
            }
        }
    }
}
