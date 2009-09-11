/*
 * Copyright (C) 2009 The Android Open Source Project
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

#include "../../CompilerInternals.h"
#include "dexdump/OpCodeNames.h"
#include "ArmLIR.h"

/* Decode and print a ARM register name */
static char * decodeRegList(int vector, char *buf)
{
    int i;
    bool printed = false;
    buf[0] = 0;
    for (i = 0; i < 8; i++, vector >>= 1) {
        if (vector & 0x1) {
            if (printed) {
                sprintf(buf + strlen(buf), ", r%d", i);
            } else {
                printed = true;
                sprintf(buf, "r%d", i);
            }
        }
    }
    return buf;
}

static int expandImmediate(int value)
{
    int mode = (value & 0xf00) >> 8;
    u4 bits = value & 0xff;
    switch(mode) {
        case 0:
            return bits;
       case 1:
            return (bits << 16) | bits;
       case 2:
            return (bits << 24) | (bits << 8);
       case 3:
            return (bits << 24) | (bits << 16) | (bits << 8) | bits;
      default:
            break;
    }
    bits = (bits | 0x80) << 24;
    return bits >> (((value & 0xf80) >> 7) - 8);
}

/*
 * Interpret a format string and build a string no longer than size
 * See format key in Assemble.c.
 */
static void buildInsnString(char *fmt, ArmLIR *lir, char* buf,
                            unsigned char *baseAddr, int size)
{
    int i;
    char *bufEnd = &buf[size-1];
    char *fmtEnd = &fmt[strlen(fmt)];
    char tbuf[256];
    char nc;
    while (fmt < fmtEnd) {
        int operand;
        if (*fmt == '!') {
            fmt++;
            assert(fmt < fmtEnd);
            nc = *fmt++;
            if (nc=='!') {
                strcpy(tbuf, "!");
            } else {
               assert(fmt < fmtEnd);
               assert((unsigned)(nc-'0') < 4);
               operand = lir->operands[nc-'0'];
               switch(*fmt++) {
                   case 'b':
                       strcpy(tbuf,"0000");
                       for (i=3; i>= 0; i--) {
                           tbuf[i] += operand & 1;
                           operand >>= 1;
                       }
                       break;
                   case 'n':
                       operand = ~expandImmediate(operand);
                       sprintf(tbuf,"%d [0x%x]", operand, operand);
                       break;
                   case 'm':
                       operand = expandImmediate(operand);
                       sprintf(tbuf,"%d [0x%x]", operand, operand);
                       break;
                   case 's':
                       sprintf(tbuf,"s%d",operand & FP_REG_MASK);
                       break;
                   case 'S':
                       sprintf(tbuf,"d%d",(operand & FP_REG_MASK) >> 1);
                       break;
                   case 'h':
                       sprintf(tbuf,"%04x", operand);
                       break;
                   case 'M':
                   case 'd':
                       sprintf(tbuf,"%d", operand);
                       break;
                   case 'D':
                       sprintf(tbuf,"%d", operand+8);
                       break;
                   case 'E':
                       sprintf(tbuf,"%d", operand*4);
                       break;
                   case 'F':
                       sprintf(tbuf,"%d", operand*2);
                       break;
                   case 'c':
                       switch (operand) {
                           case ARM_COND_EQ:
                               strcpy(tbuf, "eq");
                               break;
                           case ARM_COND_NE:
                               strcpy(tbuf, "ne");
                               break;
                           case ARM_COND_LT:
                               strcpy(tbuf, "lt");
                               break;
                           case ARM_COND_GE:
                               strcpy(tbuf, "ge");
                               break;
                           case ARM_COND_GT:
                               strcpy(tbuf, "gt");
                               break;
                           case ARM_COND_LE:
                               strcpy(tbuf, "le");
                               break;
                           case ARM_COND_CS:
                               strcpy(tbuf, "cs");
                               break;
                           case ARM_COND_MI:
                               strcpy(tbuf, "mi");
                               break;
                           default:
                               strcpy(tbuf, "");
                               break;
                       }
                       break;
                   case 't':
                       sprintf(tbuf,"0x%08x",
                               (int) baseAddr + lir->generic.offset + 4 +
                               (operand << 1));
                       break;
                   case 'u': {
                       int offset_1 = lir->operands[0];
                       int offset_2 = NEXT_LIR(lir)->operands[0];
                       intptr_t target =
                           ((((intptr_t) baseAddr + lir->generic.offset + 4) &
                            ~3) + (offset_1 << 21 >> 9) + (offset_2 << 1)) &
                           0xfffffffc;
                       sprintf(tbuf, "%p", (void *) target);
                       break;
                    }

                   /* Nothing to print for BLX_2 */
                   case 'v':
                       strcpy(tbuf, "see above");
                       break;
                   case 'R':
                       decodeRegList(operand, tbuf);
                       break;
                   default:
                       strcpy(tbuf,"DecodeError");
                       break;
               }
               if (buf+strlen(tbuf) <= bufEnd) {
                   strcpy(buf, tbuf);
                   buf += strlen(tbuf);
               } else {
                   break;
               }
            }
        } else {
           *buf++ = *fmt++;
        }
        if (buf == bufEnd)
            break;
    }
    *buf = 0;
}

/* Pretty-print a LIR instruction */
static void dumpLIRInsn(LIR *arg, unsigned char *baseAddr)
{
    ArmLIR *lir = (ArmLIR *) arg;
    char buf[256];
    char opName[256];
    int offset = lir->generic.offset;
    int dest = lir->operands[0];
    u2 *cPtr = (u2*)baseAddr;
    /* Handle pseudo-ops individually, and all regular insns as a group */
    switch(lir->opCode) {
        case ARM_PSEUDO_IT_BOTTOM:
            LOGD("-------- IT_Bottom");
            break;
        case ARM_PSEUDO_EXTENDED_MIR:
            /* intentional fallthrough */
        case ARM_PSEUDO_SSA_REP:
            LOGD("-------- %s\n", (char *) dest);
            break;
        case ARM_PSEUDO_TARGET_LABEL:
            break;
        case ARM_PSEUDO_CHAINING_CELL_BACKWARD_BRANCH:
            LOGD("-------- chaining cell (backward branch): 0x%04x\n", dest);
            break;
        case ARM_PSEUDO_CHAINING_CELL_NORMAL:
            LOGD("-------- chaining cell (normal): 0x%04x\n", dest);
            break;
        case ARM_PSEUDO_CHAINING_CELL_HOT:
            LOGD("-------- chaining cell (hot): 0x%04x\n", dest);
            break;
        case ARM_PSEUDO_CHAINING_CELL_INVOKE_PREDICTED:
            LOGD("-------- chaining cell (predicted)\n");
            break;
        case ARM_PSEUDO_CHAINING_CELL_INVOKE_SINGLETON:
            LOGD("-------- chaining cell (invoke singleton): %s/%p\n",
                 ((Method *)dest)->name,
                 ((Method *)dest)->insns);
            break;
        case ARM_PSEUDO_ENTRY_BLOCK:
            LOGD("-------- entry offset: 0x%04x\n", dest);
            break;
        case ARM_PSEUDO_DALVIK_BYTECODE_BOUNDARY:
            LOGD("-------- dalvik offset: 0x%04x @ %s\n", dest,
                   getOpcodeName(lir->operands[1]));
            break;
        case ARM_PSEUDO_EXIT_BLOCK:
            LOGD("-------- exit offset: 0x%04x\n", dest);
            break;
        case ARM_PSEUDO_ALIGN4:
            LOGD("%p (%04x): .align4\n", baseAddr + offset, offset);
            break;
        case ARM_PSEUDO_PC_RECONSTRUCTION_CELL:
            LOGD("-------- reconstruct dalvik PC : 0x%04x @ +0x%04x\n", dest,
                 lir->operands[1]);
            break;
        case ARM_PSEUDO_PC_RECONSTRUCTION_BLOCK_LABEL:
            /* Do nothing */
            break;
        case ARM_PSEUDO_EH_BLOCK_LABEL:
            LOGD("Exception_Handling:\n");
            break;
        case ARM_PSEUDO_NORMAL_BLOCK_LABEL:
            LOGD("L%#06x:\n", dest);
            break;
        default:
            if (lir->isNop) {
                break;
            }
            buildInsnString(EncodingMap[lir->opCode].name, lir, opName,
                            baseAddr, 256);
            buildInsnString(EncodingMap[lir->opCode].fmt, lir, buf, baseAddr,
                            256);
            LOGD("%p (%04x): %-8s%s\n",
                 baseAddr + offset, offset, opName, buf);
            break;
    }
}

/* Dump instructions and constant pool contents */
void dvmCompilerCodegenDump(CompilationUnit *cUnit)
{
    LOGD("Dumping LIR insns\n");
    LIR *lirInsn;
    ArmLIR *armLIR;

    LOGD("installed code is at %p\n", cUnit->baseAddr);
    LOGD("total size is %d bytes\n", cUnit->totalSize);
    for (lirInsn = cUnit->firstLIRInsn; lirInsn; lirInsn = lirInsn->next) {
        dumpLIRInsn(lirInsn, cUnit->baseAddr);
    }
    for (lirInsn = cUnit->wordList; lirInsn; lirInsn = lirInsn->next) {
        armLIR = (ArmLIR *) lirInsn;
        LOGD("%p (%04x): .word (0x%x)\n",
             (char*)cUnit->baseAddr + armLIR->generic.offset,
             armLIR->generic.offset,
             armLIR->operands[0]);
    }
}
