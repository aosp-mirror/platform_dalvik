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

import com.android.dx.rop.code.TranslationAdvice;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.code.PlainInsn;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.code.RegOps;
import com.android.dx.rop.cst.CstLiteralBits;
import com.android.dx.rop.type.TypeBearer;

import java.util.List;

/**
 * Upgrades insn to their literal (constant-immediate) equivilent if possible.
 * Also switches IF instructions that compare with a constant zero or null
 * to be their IF_*Z equivalents.
 */
public class LiteralOpUpgrader {

    /** method we're processing */
    private final SsaMethod ssaMeth;

    /**
     * Process a method.
     *
     * @param ssaMethod {@code non-null;} method to process
     */
    public static void process(SsaMethod ssaMethod) {
        LiteralOpUpgrader dc;

        dc = new LiteralOpUpgrader(ssaMethod);
            
        dc.run();
    }

    private LiteralOpUpgrader(SsaMethod ssaMethod) {
        this.ssaMeth = ssaMethod;
    }

    /**
     * Returns true if the register contains an integer 0 or a known-null
     * object reference
     *
     * @param spec non-null spec
     * @return true for 0 or null type bearers
     */
    private static boolean isConstIntZeroOrKnownNull(RegisterSpec spec) {
        TypeBearer tb = spec.getTypeBearer();
        if (tb instanceof CstLiteralBits) {
            CstLiteralBits clb = (CstLiteralBits) tb;
            return (clb.getLongBits() == 0);
        }
        return false;
    }

    /**
     * Run the literal op upgrader
     */
    private void run() {
        final TranslationAdvice advice = Optimizer.getAdvice();

        ssaMeth.forEachInsn(new SsaInsn.Visitor() {
            public void visitMoveInsn(NormalSsaInsn insn) {
                // do nothing
            }

            public void visitPhiInsn(PhiInsn insn) {
                // do nothing
            }

            public void visitNonMoveInsn(NormalSsaInsn insn) {

                Insn originalRopInsn = insn.getOriginalRopInsn();
                Rop opcode = originalRopInsn.getOpcode();
                RegisterSpecList sources = insn.getSources();

                if (sources.size() != 2 ) {
                    // We're only dealing with two-source insns here.
                    return;
                }

                if (opcode.getBranchingness() == Rop.BRANCH_IF) {
                    /*
                     * An if instruction can become an if-*z instruction.
                     */
                    if (isConstIntZeroOrKnownNull(sources.get(0))) {
                        replacePlainInsn(insn, sources.withoutFirst(),
                                RegOps.flippedIfOpcode(opcode.getOpcode()));
                    } else if (isConstIntZeroOrKnownNull(sources.get(1))) {
                        replacePlainInsn(insn, sources.withoutLast(),
                                opcode.getOpcode());
                    }
                } else if (advice.hasConstantOperation(
                        opcode, sources.get(0), sources.get(1))) {
                    insn.upgradeToLiteral();
                } else  if (opcode.isCommutative()
                        && advice.hasConstantOperation(
                        opcode, sources.get(1), sources.get(0))) {
                    /*
                     * An instruction can be commuted to a literal operation
                     */

                    insn.setNewSources(
                            RegisterSpecList.make(
                                    sources.get(1), sources.get(0)));

                    insn.upgradeToLiteral();
                }
            }
        });
    }

    /**
     * Replaces an SsaInsn containing a PlainInsn with a new PlainInsn. The
     * new PlainInsn is contructed with a new RegOp and new sources.
     *
     * TODO move this somewhere else.
     *
     * @param insn {@code non-null;} an SsaInsn containing a PlainInsn
     * @param newSources {@code non-null;} new sources list for new insn
     * @param newOpcode A RegOp from {@link RegOps}
     */
    private void replacePlainInsn(NormalSsaInsn insn,
            RegisterSpecList newSources, int newOpcode) {

        Insn originalRopInsn = insn.getOriginalRopInsn();
        Rop newRop = Rops.ropFor(newOpcode,
                insn.getResult(), newSources, null);
        Insn newRopInsn = new PlainInsn(newRop,
                originalRopInsn.getPosition(), insn.getResult(),
                newSources);
        NormalSsaInsn newInsn
                = new NormalSsaInsn(newRopInsn, insn.getBlock());

        List<SsaInsn> insns = insn.getBlock().getInsns();

        ssaMeth.onInsnRemoved(insn);
        insns.set(insns.lastIndexOf(insn), newInsn);
        ssaMeth.onInsnAdded(newInsn);
    }
}
