/*
 * Copyright (C) 2010-2011 Intel Corporation
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


/*! \file LowerJump.cpp
    \brief This file lowers the following bytecodes: IF_XXX, GOTO
*/
#include <math.h>
#include "libdex/DexOpcodes.h"
#include "libdex/DexFile.h"
#include "Lower.h"
#include "NcgAot.h"
#include "enc_wrapper.h"
#include "interp/InterpDefs.h"
#include "NcgHelper.h"
#include "Scheduler.h"

#if defined VTUNE_DALVIK
#include "compiler/JitProfiling.h"
#endif

LabelMap* globalMap;
LabelMap* globalShortMap;//make sure for each bytecode, there is no duplicated label
LabelMap* globalWorklist = NULL;
LabelMap* globalShortWorklist;

int globalMapNum;
int globalWorklistNum;
int globalDataWorklistNum;
int VMAPIWorklistNum;
int globalPCWorklistNum;
#if defined(WITH_JIT)
int chainingWorklistNum;
#endif

LabelMap* globalDataWorklist = NULL;
LabelMap* globalPCWorklist = NULL;
#if defined(WITH_JIT)
LabelMap* chainingWorklist = NULL;
#endif
LabelMap* VMAPIWorklist = NULL;

char* ncgClassData;
char* ncgClassDataPtr;
char* ncgMethodData;
char* ncgMethodDataPtr;
int   ncgClassNum;
int   ncgMethodNum;

NCGWorklist* globalNCGWorklist;
DataWorklist* methodDataWorklist;
#ifdef ENABLE_TRACING
MapWorklist* methodMapWorklist;
#endif
/*!
\brief search globalShortMap to find the entry for the given label

*/
LabelMap* findItemForShortLabel(const char* label) {
    LabelMap* ptr = globalShortMap;
    while(ptr != NULL) {
        if(!strcmp(label, ptr->label)) {
            return ptr;
        }
        ptr = ptr->nextItem;
    }
    return NULL;
}
//assume size of "jump reg" is 2
#define JUMP_REG_SIZE 2
#define ADD_REG_REG_SIZE 3
/*!
\brief update value of the immediate in the given jump instruction

check whether the immediate is out of range for the pre-set size
*/
int updateJumpInst(char* jumpInst, OpndSize immSize, int relativeNCG) {
#ifdef DEBUG_NCG_JUMP
    LOGI("update jump inst @ %p with %d\n", jumpInst, relativeNCG);
#endif
    if(immSize == OpndSize_8) { //-128 to 127
        if(relativeNCG >= 128 || relativeNCG < -128) {
            LOGI("ERROR: pre-allocated space for a forward jump is not big enough\n");
            exit(-1);
        }
    }
    if(immSize == OpndSize_16) { //-2^16 to 2^16-1
        if(relativeNCG >= 32768 || relativeNCG < -32768) {
            LOGI("ERROR: pre-allocated space for a forward jump is not big enough\n");
            exit(-1);
        }
    }
    dump_imm_update(relativeNCG, jumpInst, false);
    return 0;
}

/*!
\brief insert a label

It takes argument checkDup, if checkDup is true, an entry is created in globalShortMap, entries in globalShortWorklist are checked, if there exists a match, the immediate in the jump instruction is updated and the entry is removed from globalShortWorklist;
otherwise, an entry is created in globalMap.
*/
int insertLabel(const char* label, bool checkDup) {
    LabelMap* item = NULL;
    if(!checkDup) {
        item = (LabelMap*)malloc(sizeof(LabelMap));
        if(item == NULL) {
            LOGE("Memory allocation failed");
            return -1;
        }
        snprintf(item->label, LABEL_SIZE, "%s", label);
        item->codePtr = stream;
        item->nextItem = globalMap;
        globalMap = item;
#ifdef DEBUG_NCG_CODE_SIZE
        LOGI("insert global label %s %p\n", label, stream);
#endif
        globalMapNum++;
        return 0;
    }

    if (gDvmJit.scheduling)
        g_SchedulerInstance.signalEndOfNativeBasicBlock(); /* will update stream */

    item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    snprintf(item->label, LABEL_SIZE, "%s", label);
    item->codePtr = stream;
    item->nextItem = globalShortMap;
    globalShortMap = item;
#ifdef DEBUG_NCG
    LOGI("insert short-term label %s %p\n", label, stream);
#endif
    LabelMap* ptr = globalShortWorklist;
    LabelMap* ptr_prevItem = NULL;
    while(ptr != NULL) {
        if(!strcmp(ptr->label, label)) {
            //perform work
            int relativeNCG = stream - ptr->codePtr;
            unsigned instSize = encoder_get_inst_size(ptr->codePtr);
            relativeNCG -= instSize; //size of the instruction
#ifdef DEBUG_NCG
            LOGI("perform work short-term %p for label %s relative %d\n", ptr->codePtr, label, relativeNCG);
#endif
            updateJumpInst(ptr->codePtr, ptr->size, relativeNCG);
            //remove work
            if(ptr_prevItem == NULL) {
                globalShortWorklist = ptr->nextItem;
                free(ptr);
                ptr = globalShortWorklist; //ptr_prevItem is still NULL
            }
            else {
                ptr_prevItem->nextItem = ptr->nextItem;
                free(ptr);
                ptr = ptr_prevItem->nextItem;
            }
        }
        else {
            ptr_prevItem = ptr;
            ptr = ptr->nextItem;
        }
    } //while
    return 0;
}
/*!
\brief search globalMap to find the entry for the given label

*/
char* findCodeForLabel(const char* label) {
    LabelMap* ptr = globalMap;
    while(ptr != NULL) {
        if(!strcmp(label, ptr->label)) {
            return ptr->codePtr;
        }
        ptr = ptr->nextItem;
    }
    return NULL;
}
/*!
\brief search globalShortMap to find the entry for the given label

*/
char* findCodeForShortLabel(const char* label) {
    LabelMap* ptr = globalShortMap;
    while(ptr != NULL) {
        if(!strcmp(label, ptr->label)) {
            return ptr->codePtr;
        }
        ptr = ptr->nextItem;
    }
    return NULL;
}
int insertLabelWorklist(const char* label, OpndSize immSize) {
    LabelMap* item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    snprintf(item->label, LABEL_SIZE, "%s", label);
    item->codePtr = stream;
    item->size = immSize;
    item->nextItem = globalWorklist;
    globalWorklist = item;
#ifdef DEBUG_NCG
    LOGI("insert globalWorklist: %s %p\n", label, stream);
#endif
    return 0;
}

int insertShortWorklist(const char* label, OpndSize immSize) {
    LabelMap* item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    snprintf(item->label, LABEL_SIZE, "%s", label);
    item->codePtr = stream;
    item->size = immSize;
    item->nextItem = globalShortWorklist;
    globalShortWorklist = item;
#ifdef DEBUG_NCG
    LOGI("insert globalShortWorklist: %s %p\n", label, stream);
#endif
    return 0;
}
/*!
\brief free memory allocated for globalMap

*/
void freeLabelMap() {
    LabelMap* ptr = globalMap;
    while(ptr != NULL) {
        globalMap = ptr->nextItem;
        free(ptr);
        ptr = globalMap;
    }
}
/*!
\brief free memory allocated for globalShortMap

*/
void freeShortMap() {
    LabelMap* ptr = globalShortMap;
    while(ptr != NULL) {
        globalShortMap = ptr->nextItem;
        free(ptr);
        ptr = globalShortMap;
    }
    globalShortMap = NULL;
}

int insertGlobalPCWorklist(char * offset, char * codeStart)
{
    LabelMap* item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    snprintf(item->label, LABEL_SIZE, "%s", "export_pc");
    item->size = OpndSize_32;
    item->codePtr = offset; //points to the immediate operand
    item->addend = codeStart - streamMethodStart; //relative code pointer
    item->nextItem = globalPCWorklist;
    globalPCWorklist = item;
    globalPCWorklistNum ++;

#ifdef DEBUG_NCG
    LOGI("insert globalPCWorklist: %p %p %p %x %p\n", globalDvmNcg->streamCode,  codeStart, streamCode, item->addend, item->codePtr);
#endif
    return 0;
}

#if defined(WITH_JIT)
int insertChainingWorklist(int bbId, char * codeStart)
{
    LabelMap* item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    item->size = OpndSize_32;
    item->codePtr = codeStart; //points to the move instruction
    item->addend = bbId; //relative code pointer
    item->nextItem = chainingWorklist;
    chainingWorklist = item;

#ifdef DEBUG_NCG
    LOGI("insertChainingWorklist: %p basic block %d\n", codeStart, bbId);
#endif
    return 0;
}
#endif

int insertGlobalDataWorklist(char * offset, const char* label)
{
    LabelMap* item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    snprintf(item->label, LABEL_SIZE, "%s", label);
    item->codePtr = offset;
    item->size = OpndSize_32;
    item->nextItem = globalDataWorklist;
    globalDataWorklist = item;
    globalDataWorklistNum ++;

#ifdef DEBUG_NCG
    LOGI("insert globalDataWorklist: %s %p \n", label, offset);
#endif

    return 0;
}

int insertVMAPIWorklist(char * offset, const char* label)
{
    LabelMap* item = (LabelMap*)malloc(sizeof(LabelMap));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    snprintf(item->label, LABEL_SIZE, "%s", label);
    item->codePtr = offset;
    item->size = OpndSize_32;

    item->nextItem = VMAPIWorklist;
    VMAPIWorklist = item;

    VMAPIWorklistNum ++;

#ifdef DEBUG_NCG
    LOGI("insert VMAPIWorklist: %s %p \n", label, offset);
#endif
    return 0;
}
////////////////////////////////////////////////


int updateImmRMInst(char* moveInst, const char* label, int relativeNCG); //forward declaration
//////////////////// performLabelWorklist is defined differently for code cache
#if defined(WITH_JIT)
void performChainingWorklist() {
    LabelMap* ptr = chainingWorklist;
    while(ptr != NULL) {
        int tmpNCG = traceLabelList[ptr->addend].lop.generic.offset;
        char* NCGaddr = streamMethodStart + tmpNCG;
        updateImmRMInst(ptr->codePtr, "", (int)NCGaddr);
        chainingWorklist = ptr->nextItem;
        free(ptr);
        ptr = chainingWorklist;
    }
}
void freeChainingWorklist() {
    LabelMap* ptr = chainingWorklist;
    while(ptr != NULL) {
        chainingWorklist = ptr->nextItem;
        free(ptr);
        ptr = chainingWorklist;
    }
}
#endif
#ifdef INC_NCG_O0
void performMethodLabelWorklist()
{
    LabelMap* ptr = globalWorklist;
    LabelMap* last_item = NULL;
    while(ptr != NULL) {
#ifdef DEBUG_NCG
        LOGI("perform work global %p for label %s\n", ptr->codePtr, ptr->label);
#endif
        char* targetCode = findCodeForLabel(ptr->label);
        if(targetCode == NULL)
             LOGI("ERROR ERROR can't find label %s\n", ptr->label);
        assert(targetCode != NULL);
        int relativeNCG = targetCode - ptr->codePtr;
        unsigned instSize = encoder_get_inst_size(ptr->codePtr);
        relativeNCG -= instSize; //size of the instruction
        updateJumpInst(ptr->codePtr, ptr->size, relativeNCG);
#ifdef DEBUG_NCG
        LOGI("perform work local jmp %p for Label %s ", ptr->codePtr, ptr->label);
#endif
        ptr->addend = (uint)ptr->codePtr + instSize - (uint)encoder_get_cur_operand_offset(0);
        ptr->codePtr = encoder_get_cur_operand_offset(0);
#ifdef DEBUG_NCG
        LOGI("Record rel info with the data ptr %p  %x\n", ptr->codePtr, ptr->addend);
#endif
        last_item = ptr;
        ptr = ptr->nextItem;
    }

    return;
}
#endif

//Work only for initNCG
void performLabelWorklist() {
    LabelMap* ptr = globalWorklist;
    while(ptr != NULL) {
#ifdef DEBUG_NCG
        LOGI("perform work global %p for label %s\n", ptr->codePtr, ptr->label);
#endif
        char* targetCode = findCodeForLabel(ptr->label);
        assert(targetCode != NULL);
        int relativeNCG = targetCode - ptr->codePtr;
        unsigned instSize = encoder_get_inst_size(ptr->codePtr);
        relativeNCG -= instSize; //size of the instruction
        updateJumpInst(ptr->codePtr, ptr->size, relativeNCG);
        globalWorklist = ptr->nextItem;
        free(ptr);
        ptr = globalWorklist;
    }
}
void freeLabelWorklist() {
    LabelMap* ptr = globalWorklist;
    while(ptr != NULL) {
        globalWorklist = ptr->nextItem;
        free(ptr);
        ptr = globalWorklist;
    }
}

///////////////////////////////////////////////////
/*!
\brief update value of the immediate in the given move instruction

*/
int updateImmRMInst(char* moveInst, const char* label, int relativeNCG) {
#ifdef DEBUG_NCG
    LOGI("perform work ImmRM inst @ %p for label %s with %d\n", moveInst, label, relativeNCG);
#endif
    dump_imm_update(relativeNCG, moveInst, true);
    return 0;
}
//! maximum instruction size for jump,jcc,call: 6 for jcc rel32
#define MAX_JCC_SIZE 6
//! minimum instruction size for jump,jcc,call: 2
#define MIN_JCC_SIZE 2
/*!
\brief estimate size of the immediate

Somehow, 16 bit jump does not work. This function will return either 8 bit or 32 bit
EXAMPLE:
  native code at A: ...
  native code at B: jump relOffset (target is A)
  native code at B':
  --> relOffset = A - B' = A - B - size of the jump instruction
  Argument "target" is equal to A - B. To determine size of the immediate, we check tha value of "target - size of the jump instructoin"
*/
OpndSize estOpndSizeFromImm(int target) {
    if(target-MIN_JCC_SIZE < 128 && target-MAX_JCC_SIZE >= -128) return OpndSize_8;
#ifdef SUPPORT_IMM_16
    if(target-MIN_JCC_SIZE < 32768 && target-MAX_JCC_SIZE >= -32768) return OpndSize_16;
#endif
    return OpndSize_32;
}
/*!
\brief return size of a jump or call instruction

*/
unsigned getJmpCallInstSize(OpndSize size, JmpCall_type type) {
    if(type == JmpCall_uncond) {
        if(size == OpndSize_8) return 2;
        if(size == OpndSize_16) return 4;
        return 5;
    }
    if(type == JmpCall_cond) {
        if(size == OpndSize_8) return 2;
        if(size == OpndSize_16) return 5;
        return 6;
    }
    if(type == JmpCall_reg) {
        assert(size == OpndSize_32);
        return JUMP_REG_SIZE;
    }
    if(type == JmpCall_call) {
        assert(size != OpndSize_8);
        if(size == OpndSize_16) return 4;
        return 5;
    }
    return 0;
}
/*!
\brief check whether a branch target is already handled, if yes, return the size of the immediate; otherwise, call insertShortWorklist or insertLabelWorklist.

If the branch target is not handled, call insertShortWorklist or insertLabelWorklist depending on isShortTerm, unknown is set to true, immSize is set to 32 if isShortTerm is false, set to 32 if isShortTerm is true and target is check_cast_null, set to 8 otherwise.

If the branch target is handled, call estOpndSizeFromImm to set immSize for jump instruction, returns the value of the immediate
*/
int getRelativeOffset(const char* target, bool isShortTerm, JmpCall_type type, bool* unknown, OpndSize* immSize) {
    char* targetPtrInStream = NULL;
    if(isShortTerm) targetPtrInStream = findCodeForShortLabel(target);
    else targetPtrInStream = findCodeForLabel(target);

    int relOffset;
    *unknown = false;
    if(targetPtrInStream == NULL) {
        //branch target is not handled yet
        relOffset = 0;
        *unknown = true;
        if(isShortTerm) {
            /* for backward jump, at this point, we don't know how far the target is from this jump
               since the lable is only used within a single bytecode, we assume OpndSize_8 is big enough
               but there are special cases where we should use 32 bit offset
            */
            if(!strcmp(target, ".check_cast_null") || !strcmp(target, ".stackOverflow") ||
               !strcmp(target, ".invokeChain") ||
               !strcmp(target, ".new_instance_done") ||
               !strcmp(target, ".new_array_done") ||
               !strcmp(target, ".fill_array_data_done") ||
               !strcmp(target, ".inlined_string_compare_done") ||
               !strncmp(target, "after_exception", 15)) {
#ifdef SUPPORT_IMM_16
                *immSize = OpndSize_16;
#else
                *immSize = OpndSize_32;
#endif
            } else {
                *immSize = OpndSize_8;
            }
#ifdef DEBUG_NCG_JUMP
            LOGI("insert to short worklist %s %d\n", target, *immSize);
#endif
            insertShortWorklist(target, *immSize);
        }
        else {
#ifdef SUPPORT_IMM_16
            *immSize = OpndSize_16;
#else
            *immSize = OpndSize_32;
#endif
            insertLabelWorklist(target, *immSize);
        }
        if(type == JmpCall_call) { //call sz16 does not work in gdb
            *immSize = OpndSize_32;
        }
        return 0;
    }
    else if (!isShortTerm) {
#ifdef SUPPORT_IMM_16
        *immSize = OpndSize_16;
#else
        *immSize = OpndSize_32;
#endif
        insertLabelWorklist(target, *immSize);
    }

#ifdef DEBUG_NCG
    LOGI("backward branch @ %p for label %s\n", stream, target);
#endif
    relOffset = targetPtrInStream - stream;
    if(type == JmpCall_call) *immSize = OpndSize_32;
    else
        *immSize = estOpndSizeFromImm(relOffset);

    relOffset -= getJmpCallInstSize(*immSize, type);
    return relOffset;
}

/*!
\brief generate a single native instruction "jcc imm" to jump to a label

*/
void conditional_jump(ConditionCode cc, const char* target, bool isShortTerm) {
#if defined(WITH_JIT)
    if(jumpToException(target) && currentExceptionBlockIdx >= 0) { //jump to the exceptionThrow block
        condJumpToBasicBlock(stream, cc, currentExceptionBlockIdx);
        return;
    }
#endif
    Mnemonic m = (Mnemonic)(Mnemonic_Jcc + cc);
    bool unknown;
    OpndSize size;
    int imm = 0;
    if(!gDvmJit.scheduling)
        imm = getRelativeOffset(target, isShortTerm, JmpCall_cond, &unknown, &size);
    dump_label(m, size, imm, target, isShortTerm);
}
/*!
\brief generate a single native instruction "jmp imm" to jump to ".invokeArgsDone"

*/
void goto_invokeArgsDone() {
    unconditional_jump_global_API(".invokeArgsDone", false);
}
/*!
\brief generate a single native instruction "jmp imm" to jump to a label

If the target is ".invokeArgsDone" and mode is NCG O1, extra work is performed to dump content of virtual registers to memory.
*/
void unconditional_jump(const char* target, bool isShortTerm) {
#if defined(WITH_JIT)
    if(jumpToException(target) && currentExceptionBlockIdx >= 0) { //jump to the exceptionThrow block
        jumpToBasicBlock(stream, currentExceptionBlockIdx);
        return;
    }
#endif
    Mnemonic m = Mnemonic_JMP;
    bool unknown;
    OpndSize size;
    if(gDvm.executionMode == kExecutionModeNcgO1) {
        //for other three labels used by JIT: invokeArgsDone_formal, _native, _jit
        if(!strncmp(target, ".invokeArgsDone", 15)) {
            touchEcx(); //keep ecx live, if ecx was spilled, it is loaded here
            beforeCall(target); //
        }
        if(!strcmp(target, ".invokeArgsDone")) {
            nextVersionOfHardReg(PhysicalReg_EDX, 1); //edx will be used in a function
            call("ncgGetEIP"); //must be immediately before JMP
        }
    }
    int imm = 0;
    if(!gDvmJit.scheduling)
        imm = getRelativeOffset(target, isShortTerm, JmpCall_uncond, &unknown, &size);
    dump_label(m, size, imm, target, isShortTerm);
    if(gDvm.executionMode == kExecutionModeNcgO1) {
        if(!strncmp(target, ".invokeArgsDone", 15)) {
            afterCall(target); //un-spill before executing the next bytecode
        }
    }
}
/*!
\brief generate a single native instruction "jcc imm"

*/
void conditional_jump_int(ConditionCode cc, int target, OpndSize size) {
    Mnemonic m = (Mnemonic)(Mnemonic_Jcc + cc);
    dump_imm(m, size, target);
}
/*!
\brief generate a single native instruction "jmp imm"

*/
void unconditional_jump_int(int target, OpndSize size) {
    Mnemonic m = Mnemonic_JMP;
    dump_imm(m, size, target);
}

//! Used to generate a single native instruction for conditionally
//! jumping to a block when the immediate is not yet known.
//! This should only be used when instruction scheduling is enabled.
//! \param cc type of conditional jump
//! \param targetBlockId id of MIR basic block
void conditional_jump_block(ConditionCode cc, int targetBlockId) {
    Mnemonic m = (Mnemonic)(Mnemonic_Jcc + cc);
    dump_blockid_imm(m, targetBlockId);
}

//! Used to generate a single native instruction for unconditionally
//! jumping to a block when the immediate is not yet known.
//! This should only be used when instruction scheduling is enabled.
//! \param targetBlockId id of MIR basic block
void unconditional_jump_block(int targetBlockId) {
    Mnemonic m = Mnemonic_JMP;
    dump_blockid_imm(m, targetBlockId);
}

/*!
\brief generate a single native instruction "jmp reg"

*/
void unconditional_jump_reg(int reg, bool isPhysical) {
    dump_reg(Mnemonic_JMP, ATOM_NORMAL, OpndSize_32, reg, isPhysical, LowOpndRegType_gp);
}

/*!
\brief generate a single native instruction to call a function

If mode is NCG O1, extra work is performed to dump content of virtual registers to memory.
*/
void call(const char* target) {
    if(gDvm.executionMode == kExecutionModeNcgO1) {
        beforeCall(target);
    }
    Mnemonic m = Mnemonic_CALL;
    bool dummy;
    OpndSize size;
    int relOffset = 0;
    if(!gDvmJit.scheduling)
        relOffset = getRelativeOffset(target, false, JmpCall_call, &dummy, &size);
    dump_label(m, size, relOffset, target, false);
    if(gDvm.executionMode == kExecutionModeNcgO1) {
        afterCall(target);
    }
}
/*!
\brief generate a single native instruction to call a function

*/
void call_reg(int reg, bool isPhysical) {
    Mnemonic m = Mnemonic_CALL;
    dump_reg(m, ATOM_NORMAL, OpndSize_32, reg, isPhysical, LowOpndRegType_gp);
}
void call_reg_noalloc(int reg, bool isPhysical) {
    Mnemonic m = Mnemonic_CALL;
    dump_reg_noalloc(m, OpndSize_32, reg, isPhysical, LowOpndRegType_gp);
}

/*!
\brief generate a single native instruction to call a function

*/
void call_mem(int disp, int reg, bool isPhysical) {
    Mnemonic m = Mnemonic_CALL;
    dump_mem(m, ATOM_NORMAL, OpndSize_32, disp, reg, isPhysical);
}

/*!
\brief insert an entry to globalNCGWorklist

*/
int insertNCGWorklist(s4 relativePC, OpndSize immSize) {
    int offsetNCG2 = stream - streamMethodStart;
#ifdef DEBUG_NCG
    LOGI("insert NCGWorklist (goto forward) @ %p offsetPC %x relativePC %x offsetNCG %x\n", stream, offsetPC, relativePC, offsetNCG2);
#endif
    NCGWorklist* item = (NCGWorklist*)malloc(sizeof(NCGWorklist));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    item->relativePC = relativePC;
    item->offsetPC = offsetPC;
    item->offsetNCG = offsetNCG2;
    item->codePtr = stream;
    item->size = immSize;
    item->nextItem = globalNCGWorklist;
    globalNCGWorklist = item;
    return 0;
}
#ifdef ENABLE_TRACING
int insertMapWorklist(s4 BCOffset, s4 NCGOffset, int isStartOfPC) {
#if !defined(WITH_JIT)
    if(NCGOffset < 0) {
        LOGI("ERROR: NCGOffset is negative %x in insertMapWorklist\n", NCGOffset);
        exit(-1);
    }
    if(BCOffset < 0) {
        LOGI("ERROR: BCOffset is negative %x in insertMapWorklist\n", BCOffset);
        exit(-1);
    }
    MapWorklist* item = (MapWorklist*)malloc(sizeof(MapWorklist));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    item->offsetPC = (u4)BCOffset;
    item->offsetNCG = (u4)NCGOffset;//stream - streamMethodStart;
    item->isStartOfPC = isStartOfPC;
    item->nextItem = methodMapWorklist;
    methodMapWorklist = item;
#endif
    return 0;
}
#endif
/*!
\brief insert an entry to methodDataWorklist

This function is used by bytecode FILL_ARRAY_DATA, PACKED_SWITCH, SPARSE_SWITCH
*/
int insertDataWorklist(s4 relativePC, char* codePtr1) {
    //insert according to offsetPC+relativePC, smallest at the head
    DataWorklist* item = (DataWorklist*)malloc(sizeof(DataWorklist));
    if(item == NULL) {
        LOGE("Memory allocation failed");
        return -1;
    }
    item->relativePC = relativePC;
    item->offsetPC = offsetPC;
    item->codePtr = codePtr1;
    item->codePtr2 = stream; //jump_reg for switch
    DataWorklist* ptr = methodDataWorklist;
    DataWorklist* prev_ptr = NULL;
    while(ptr != NULL) {
        int tmpPC = ptr->offsetPC + ptr->relativePC;
        int tmpPC2 = relativePC + offsetPC;
        if(tmpPC2 < tmpPC) {
            break;
        }
        prev_ptr = ptr;
        ptr = ptr->nextItem;
    }
    //insert item before ptr
    if(prev_ptr != NULL) {
        prev_ptr->nextItem = item;
    }
    else methodDataWorklist = item;
    item->nextItem = ptr;
    return 0;
}

/*!
\brief work on globalNCGWorklist

*/
int performNCGWorklist() {
    NCGWorklist* ptr = globalNCGWorklist;
    while(ptr != NULL) {
#if !defined(WITH_JIT)
        int tmpPC = ptr->offsetPC + ptr->relativePC;
        int tmpNCG = mapFromBCtoNCG[tmpPC];
#else
        LOGV("perform NCG worklist: @ %p target block %d target NCG %x\n",
             ptr->codePtr, ptr->relativePC, traceLabelList[ptr->relativePC].lop.generic.offset);
        int tmpNCG = traceLabelList[ptr->relativePC].lop.generic.offset;
#endif
        assert(tmpNCG >= 0);
        int relativeNCG = tmpNCG - ptr->offsetNCG;
        unsigned instSize = encoder_get_inst_size(ptr->codePtr);
        relativeNCG -= instSize;
        updateJumpInst(ptr->codePtr, ptr->size, relativeNCG);
        globalNCGWorklist = ptr->nextItem;
        free(ptr);
        ptr = globalNCGWorklist;
    }
    return 0;
}
void freeNCGWorklist() {
    NCGWorklist* ptr = globalNCGWorklist;
    while(ptr != NULL) {
        globalNCGWorklist = ptr->nextItem;
        free(ptr);
        ptr = globalNCGWorklist;
    }
}

/*!
\brief used by bytecode SWITCH

targetPC points to start of the data section
Code sequence for SWITCH
  call ncgGetEIP
  @codeInst: add_reg_reg %eax, %edx
  jump_reg %edx
This function returns the offset in native code between add_reg_reg and the data section
*/
int getRelativeNCGForSwitch(int targetPC, char* codeInst) {
    int tmpNCG = mapFromBCtoNCG[targetPC];
    int offsetNCG2 = codeInst - streamMethodStart;
    int relativeOff = tmpNCG - offsetNCG2;
    return relativeOff;
}
/*!
\brief work on methodDataWorklist

*/
int performDataWorklist() {
    DataWorklist* ptr = methodDataWorklist;
    if(ptr == NULL) return 0;

    char* codeCacheEnd = ((char *) gDvmJit.codeCache) + gDvmJit.codeCacheSize - CODE_CACHE_PADDING;
    u2 insnsSize = dvmGetMethodInsnsSize(currentMethod); //bytecode
    //align stream to multiple of 4
    int alignBytes = (int)stream & 3;
    if(alignBytes != 0) alignBytes = 4-alignBytes;
    stream += alignBytes;

    while(ptr != NULL) {
        int tmpPC = ptr->offsetPC + ptr->relativePC;
        int endPC = insnsSize;
        if(ptr->nextItem != NULL) endPC = ptr->nextItem->offsetPC + ptr->nextItem->relativePC;
        mapFromBCtoNCG[tmpPC] = stream - streamMethodStart; //offsetNCG in byte

        //handle fill_array_data, packed switch & sparse switch
        u2 tmpInst = *(currentMethod->insns + ptr->offsetPC);
        u2* sizePtr;
        s4* entryPtr_bytecode;
        u2 tSize, iVer;
        u4 sz;

        if (gDvmJit.codeCacheFull == true) {
            // We are out of code cache space. Skip writing data/code to
            //   code cache. Simply free the item.
            methodDataWorklist = ptr->nextItem;
            free(ptr);
            ptr = methodDataWorklist;
        }

        switch (INST_INST(tmpInst)) {
        case OP_FILL_ARRAY_DATA:
            sz = (endPC-tmpPC)*sizeof(u2);
            if ((stream + sz) < codeCacheEnd) {
                memcpy(stream, (u2*)currentMethod->insns+tmpPC, sz);
#ifdef DEBUG_NCG_CODE_SIZE
                LOGI("copy data section to stream %p: start at %d, %d bytes\n", stream, tmpPC, sz);
#endif
#ifdef DEBUG_NCG
                LOGI("update data section at %p with %d\n", ptr->codePtr, stream-ptr->codePtr);
#endif
                updateImmRMInst(ptr->codePtr, "", stream - ptr->codePtr);
                stream += sz;
            } else {
                gDvmJit.codeCacheFull = true;
            }
            break;
        case OP_PACKED_SWITCH:
            updateImmRMInst(ptr->codePtr, "", stream-ptr->codePtr);
            sizePtr = (u2*)currentMethod->insns+tmpPC + 1 /*signature*/;
            entryPtr_bytecode = (s4*)(sizePtr + 1 /*size*/ + 2 /*firstKey*/);
            tSize = *(sizePtr);
            sz = tSize * 4;     /* expected size needed in stream */
            if ((stream + sz) < codeCacheEnd) {
                for(iVer = 0; iVer < tSize; iVer++) {
                    //update entries
                    s4 relativePC = *entryPtr_bytecode; //relative to ptr->offsetPC
                    //need stream, offsetPC,
                    int relativeNCG = getRelativeNCGForSwitch(relativePC+ptr->offsetPC, ptr->codePtr2);
#ifdef DEBUG_NCG_CODE_SIZE
                    LOGI("convert target from %d to %d\n", relativePC+ptr->offsetPC, relativeNCG);
#endif
                    *((s4*)stream) = relativeNCG;
                    stream += 4;
                    entryPtr_bytecode++;
                }
            } else {
                gDvmJit.codeCacheFull = true;
            }
            break;
        case OP_SPARSE_SWITCH:
            updateImmRMInst(ptr->codePtr, "", stream-ptr->codePtr);
            sizePtr = (u2*)currentMethod->insns+tmpPC + 1 /*signature*/;
            s4* keyPtr_bytecode = (s4*)(sizePtr + 1 /*size*/);
            tSize = *(sizePtr);
            entryPtr_bytecode = (s4*)(keyPtr_bytecode + tSize);
            sz = tSize * (sizeof(s4) + 4); /* expected size needed in stream */
            if ((stream + sz) < codeCacheEnd) {
                memcpy(stream, keyPtr_bytecode, tSize*sizeof(s4));
                stream += tSize*sizeof(s4);
                for(iVer = 0; iVer < tSize; iVer++) {
                    //update entries
                    s4 relativePC = *entryPtr_bytecode; //relative to ptr->offsetPC
                    //need stream, offsetPC,
                    int relativeNCG = getRelativeNCGForSwitch(relativePC+ptr->offsetPC, ptr->codePtr2);
                    *((s4*)stream) = relativeNCG;
                    stream += 4;
                    entryPtr_bytecode++;
                }
            } else {
                gDvmJit.codeCacheFull = true;
            }
            break;
        }

        //remove the item
        methodDataWorklist = ptr->nextItem;
        free(ptr);
        ptr = methodDataWorklist;
    }
    return 0;
}
void freeDataWorklist() {
    DataWorklist* ptr = methodDataWorklist;
    while(ptr != NULL) {
        methodDataWorklist = ptr->nextItem;
        free(ptr);
        ptr = methodDataWorklist;
    }
}

//////////////////////////
/*!
\brief check whether a branch target (specified by relative offset in bytecode) is already handled, if yes, return the size of the immediate; otherwise, call insertNCGWorklist.

If the branch target is not handled, call insertNCGWorklist, unknown is set to true, immSize is set to 32.

If the branch target is handled, call estOpndSizeFromImm to set immSize for jump instruction, returns the value of the immediate
*/
int getRelativeNCG(s4 tmp, JmpCall_type type, bool* unknown, OpndSize* size) {//tmp: relativePC
#if defined(WITH_JIT) //tmp: target basic block id
    int tmpNCG = traceLabelList[tmp].lop.generic.offset;
#else
    int tmpNCG = mapFromBCtoNCG[offsetPC + tmp];
#endif

    *unknown = false;
    if(tmpNCG <0) {
        *unknown = true;
#ifdef SUPPORT_IMM_16
        *size = OpndSize_16;
#else
        *size = OpndSize_32;
#endif
        insertNCGWorklist(tmp, *size);
        return 0;
    }
    int offsetNCG2 = stream - streamMethodStart;
#ifdef DEBUG_NCG
    LOGI("goto backward @ %p offsetPC %d relativePC %d offsetNCG %d relativeNCG %d\n", stream, offsetPC, tmp, offsetNCG2, tmpNCG-offsetNCG2);
#endif
    int relativeOff = tmpNCG - offsetNCG2;
    *size = estOpndSizeFromImm(relativeOff);
    return relativeOff - getJmpCallInstSize(*size, type);
}
/*!
\brief a helper function to handle backward branch

input: jump target in %eax; at end of the function, jump to %eax
*/
int common_backwardBranch() {
    insertLabel("common_backwardBranch", false);

#if defined VTUNE_DALVIK
     int startStreamPtr = (int)stream;
#endif

    spill_reg(PhysicalReg_EAX, true);
    call("common_periodicChecks_entry");
    unspill_reg(PhysicalReg_EAX, true);
    unconditional_jump_reg(PhysicalReg_EAX, true);

#if defined(VTUNE_DALVIK)
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_backwardBranch");
    }
#endif
    return 0;
}
#if !defined(WITH_JIT)
/*!
\brief common code to handle GOTO

If it is a backward branch, call common_periodicChecks4 to handle GC request.
Since this is the end of a basic block, constVREndOfBB and globalVREndOfBB are called right before the jump instruction.
*/
int common_goto(s4 tmp) { //tmp: relativePC
    if(tmp < 0) {
#ifdef ENABLE_TRACING
#if !defined(TRACING_OPTION2)
        insertMapWorklist(offsetPC + tmp, mapFromBCtoNCG[offsetPC+tmp], 1);
#endif
        //(target offsetPC * 2)
        move_imm_to_reg(OpndSize_32, 2*(offsetPC+tmp), PhysicalReg_EDX, true);
#endif
        //call( ... ) will dump VRs to memory first
        //potential garbage collection will work as designed
        call_helper_API("common_periodicChecks4");
    }
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    bool unknown;
    OpndSize size;
    int relativeNCG = tmp;
    if(!gDvmJit.scheduling)
        relativeNCG = getRelativeNCG(tmp, JmpCall_uncond, &unknown, &size);
    unconditional_jump_int(relativeNCG, size);
    return 0;
}
//the common code to lower a if bytecode
int common_if(s4 tmp, ConditionCode cc_next, ConditionCode cc_taken) {
    if(tmp < 0) { //backward
        conditional_jump(cc_next, ".if_next", true);
        common_goto(tmp);
        insertLabel(".if_next", true);
    }
    else {
        //if(tmp < 0) LOGI("skip periodicCheck for if");
        bool unknown;
        OpndSize size;
        int relativeNCG = tmp;
        if(!gDvmJit.scheduling)
            relativeNCG = getRelativeNCG(tmp, JmpCall_cond, &unknown, &size); //must be known
        conditional_jump_int(cc_taken, relativeNCG, size); //CHECK
    }
    return 0;
}
#else
//when this is called from JIT, there is no need to check GC
int common_goto(s4 targetBlockId) {
    bool unknown;
    OpndSize size;
    constVREndOfBB();
    globalVREndOfBB(currentMethod);

    if(gDvmJit.scheduling) {
        unconditional_jump_block((int)targetBlockId);
    } else {
        int relativeNCG = getRelativeNCG(targetBlockId, JmpCall_uncond, &unknown, &size);
        unconditional_jump_int(relativeNCG, size);
    }
    return 1;
}

int common_if(s4 tmp, ConditionCode cc_next, ConditionCode cc) {
    bool unknown;
    OpndSize size;
    int relativeNCG;

    if (traceMode == kJitLoop && !branchInLoop && hasVRStoreExitOfLoop()) {
        if (traceCurrentBB->taken && traceCurrentBB->taken->blockType == kChainingCellNormal) {
            conditional_jump(cc, ".vr_store_at_loop_exit", true);

            if(gDvmJit.scheduling && traceCurrentBB->fallThrough) {
                unconditional_jump_block(traceCurrentBB->fallThrough->id);
            } else {
                relativeNCG = traceCurrentBB->fallThrough ? traceCurrentBB->fallThrough->id : 0;
                if(traceCurrentBB->fallThrough)
                    relativeNCG = getRelativeNCG(traceCurrentBB->fallThrough->id, JmpCall_uncond, &unknown, &size);
                unconditional_jump_int(relativeNCG, size);
            }

            insertLabel(".vr_store_at_loop_exit", true);
            storeVRExitOfLoop();

            if(gDvmJit.scheduling && traceCurrentBB->taken) {
                unconditional_jump_block(traceCurrentBB->taken->id);
            } else {
                relativeNCG = traceCurrentBB->taken ? traceCurrentBB->taken->id : 0;
                if(traceCurrentBB->taken)
                    relativeNCG = getRelativeNCG(traceCurrentBB->taken->id, JmpCall_uncond, &unknown, &size);
                unconditional_jump_int(relativeNCG, size);
            }
        }
        else if (traceCurrentBB->fallThrough && traceCurrentBB->fallThrough->blockType == kChainingCellNormal) {
            conditional_jump(cc_next, ".vr_store_at_loop_exit", true);

            if(gDvmJit.scheduling && traceCurrentBB->taken) {
                unconditional_jump_block(traceCurrentBB->taken->id);
            } else {
                relativeNCG = traceCurrentBB->taken ? traceCurrentBB->taken->id : 0;
                if(traceCurrentBB->taken)
                    relativeNCG = getRelativeNCG(traceCurrentBB->taken->id, JmpCall_uncond, &unknown, &size);
                unconditional_jump_int(relativeNCG, size);
            }

            insertLabel(".vr_store_at_loop_exit", true);
            storeVRExitOfLoop();

            if(gDvmJit.scheduling && traceCurrentBB->fallThrough) {
                unconditional_jump_block(traceCurrentBB->fallThrough->id);
            } else {
                relativeNCG = traceCurrentBB->fallThrough ? traceCurrentBB->fallThrough->id : 0;
                if(traceCurrentBB->fallThrough)
                    relativeNCG = getRelativeNCG(traceCurrentBB->fallThrough->id, JmpCall_uncond, &unknown, &size);
                unconditional_jump_int(relativeNCG, size);
            }
        }
        else
           LOGE("ERROR in common_if\n");
    }
    else {
        if(gDvmJit.scheduling) {
            if(traceCurrentBB->taken)
                conditional_jump_block(cc, traceCurrentBB->taken->id);
            if(traceCurrentBB->fallThrough)
                unconditional_jump_block(traceCurrentBB->fallThrough->id);
        } else {
            relativeNCG = traceCurrentBB->taken ? traceCurrentBB->taken->id : 0;
            if(traceCurrentBB->taken)
                relativeNCG = getRelativeNCG(traceCurrentBB->taken->id, JmpCall_cond, &unknown, &size);
            conditional_jump_int(cc, relativeNCG, size);

            relativeNCG = traceCurrentBB->fallThrough ? traceCurrentBB->fallThrough->id : 0;
            if(traceCurrentBB->fallThrough)
                relativeNCG = getRelativeNCG(traceCurrentBB->fallThrough->id, JmpCall_uncond, &unknown, &size);
            unconditional_jump_int(relativeNCG, size);
        }
    }
    return 2;
}
#endif

/*!
\brief helper function to handle null object error

*/
int common_errNullObject() {
    insertLabel("common_errNullObject", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif

    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, (int) gDvm.exNullPointerException, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errNullObject");
    }
#endif

    return 0;
}
/*!
\brief helper function to handle string index error

*/
int common_errStringIndexOutOfBounds() {
    insertLabel("common_errStringIndexOutOfBounds", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, (int)gDvm.exStringIndexOutOfBoundsException, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errStringIndexOutOfBounds");
    }
#endif
    return 0;
}

/*!
\brief helper function to handle array index error

*/
int common_errArrayIndex() {
    insertLabel("common_errArrayIndex", false);
#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif

    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, LstrArrayIndexException, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errArrayIndex");
    }
#endif
    return 0;
}
/*!
\brief helper function to handle array store error

*/
int common_errArrayStore() {
    insertLabel("common_errArrayStore", false);
#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif

    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, LstrArrayStoreException, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errArrayStore");
    }
#endif
    return 0;
}
/*!
\brief helper function to handle negative array size error

*/
int common_errNegArraySize() {
    insertLabel("common_errNegArraySize", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, LstrNegativeArraySizeException, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errNegArraySize");
    }
#endif
    return 0;
}
/*!
\brief helper function to handle divide-by-zero error

*/
int common_errDivideByZero() {
    insertLabel("common_errDivideByZero", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    move_imm_to_reg(OpndSize_32, LstrDivideByZero, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, LstrArithmeticException, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errDivideByZero");
    }
#endif
    return 0;
}
/*!
\brief helper function to handle no such method error

*/
int common_errNoSuchMethod() {
    insertLabel("common_errNoSuchMethod", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
    move_imm_to_reg(OpndSize_32, LstrNoSuchMethodError, PhysicalReg_ECX, true);
    unconditional_jump("common_throw", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_errNoSuchMethod");
    }
#endif
    return 0;
}
int call_dvmFindCatchBlock();

#define P_GPR_1 PhysicalReg_ESI //self callee-saved
#define P_GPR_2 PhysicalReg_EBX //exception callee-saved
#define P_GPR_3 PhysicalReg_EAX //method that caused exception
/*!
\brief helper function common_exceptionThrown

*/
int common_exceptionThrown() {
    insertLabel("common_exceptionThrown", false);
#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    typedef void (*vmHelper)(int);
    vmHelper funcPtr = dvmJitToExceptionThrown;
    move_imm_to_reg(OpndSize_32, (int)funcPtr, C_SCRATCH_1, isScratchPhysical);
    unconditional_jump_reg(C_SCRATCH_1, isScratchPhysical);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_exceptionThrown");
    }
#endif
    return 0;
}
#undef P_GPR_1
#undef P_GPR_2
#undef P_GPR_3

/*!
\brief helper function to throw an exception with message

INPUT: obj_reg(%eax), exceptionPtrReg(%ecx)
SCRATCH: C_SCRATCH_1(%esi) & C_SCRATCH_2(%edx)
OUTPUT: no
*/
int throw_exception_message(int exceptionPtrReg, int obj_reg, bool isPhysical,
                            int startLR/*logical register index*/, bool startPhysical) {
    insertLabel("common_throw_message", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
 #endif
    scratchRegs[0] = PhysicalReg_ESI; scratchRegs[1] = PhysicalReg_EDX;
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;

    move_mem_to_reg(OpndSize_32, offObject_clazz, obj_reg, isPhysical, C_SCRATCH_1, isScratchPhysical);
    move_mem_to_reg(OpndSize_32, offClassObject_descriptor, C_SCRATCH_1, isScratchPhysical, C_SCRATCH_2, isScratchPhysical);
    load_effective_addr(-8, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    move_reg_to_mem(OpndSize_32, C_SCRATCH_2, isScratchPhysical, 4, PhysicalReg_ESP, true);
    move_reg_to_mem(OpndSize_32, exceptionPtrReg, true, 0, PhysicalReg_ESP, true);
    call_dvmThrowWithMessage();
    load_effective_addr(8, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    unconditional_jump("common_exceptionThrown", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_throw_message");
    }
#endif
    return 0;
}
/*!
\brief helper function to throw an exception

scratch: C_SCRATCH_1(%edx)
*/
int throw_exception(int exceptionPtrReg, int immReg,
                    int startLR/*logical register index*/, bool startPhysical) {
    insertLabel("common_throw", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif

    scratchRegs[0] = PhysicalReg_EDX; scratchRegs[1] = PhysicalReg_Null;
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;

    load_effective_addr(-8, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    move_reg_to_mem(OpndSize_32, immReg, true, 4, PhysicalReg_ESP, true);
    move_reg_to_mem(OpndSize_32, exceptionPtrReg, true, 0, PhysicalReg_ESP, true);
    call_dvmThrow();
    load_effective_addr(8, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    unconditional_jump("common_exceptionThrown", false);

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_throw");
    }
#endif
    return 0;
}

/**
 * @brief Generate native code for bytecode goto
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_goto(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_GOTO);
#if !defined(WITH_JIT)
    u2 tt = INST_AA(inst);
    s2 tmp = (s2)((s2)tt << 8) >> 8; //00AA --> AA00 --> xxAA
#else
    s2 tmp = traceCurrentBB->taken->id;
#endif
    int retval = common_goto(tmp);
    return retval;
}

/**
 * @brief Generate native code for bytecode goto/16
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_goto_16(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_GOTO_16);
#if !defined(WITH_JIT)
    s2 tmp = (s2)FETCH(1);
#else
    s2 tmp = traceCurrentBB->taken->id;
#endif
    int retval = common_goto(tmp);
    return retval;
}

/**
 * @brief Generate native code for bytecode goto/32
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_goto_32(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_GOTO_32);
#if !defined(WITH_JIT)
    u4 tmp = (u4)FETCH(1);
    tmp |= (u4)FETCH(2) << 16;
#else
    s2 tmp = traceCurrentBB->taken->id;
#endif
    int retval = common_goto((s4)tmp);
    return retval;
}
#define P_GPR_1 PhysicalReg_EBX

/**
 * @brief Generate native code for bytecode packed-switch
 * @param mir bytecode representation
 * @param dalvikPC program counter for Dalvik bytecode
 * @return value >= 0 when handled
 */
int op_packed_switch(const MIR * mir, const u2 * dalvikPC) {
    assert(mir->dalvikInsn.opcode == OP_PACKED_SWITCH);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;

#ifdef DEBUG_EACH_BYTECODE
    u2 tSize = 0;
    s4 firstKey = 0;
    s4* entries = NULL;
#else
    u2* switchData = const_cast<u2 *>(dalvikPC) + (s4)tmp;
    if (*switchData++ != kPackedSwitchSignature) {
        /* should have been caught by verifier */
        dvmThrowInternalError(
                          "bad packed switch magic");
        return 0; //no_op
    }
    u2 tSize = *switchData++;
    assert(tSize > 0);
    s4 firstKey = *switchData++;
    firstKey |= (*switchData++) << 16;
    s4* entries = (s4*) switchData;
    assert(((u4)entries & 0x3) == 0);
#endif

    get_virtual_reg(vA, OpndSize_32, 1, false);
    //dvmNcgHandlePackedSwitch: testVal, size, first_key, targets
    load_effective_addr(-16, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    move_imm_to_mem(OpndSize_32, tSize, 8, PhysicalReg_ESP, true);
    move_imm_to_mem(OpndSize_32, firstKey, 4, PhysicalReg_ESP, true);

#if defined(WITH_JIT)
    /* "entries" is constant for JIT
       it is the 1st argument to dvmJitHandlePackedSwitch */
    move_imm_to_mem(OpndSize_32, (int)entries, 0, PhysicalReg_ESP, true);
#else
    get_eip_API();

    char* codePtr1 = stream;
    alu_binary_imm_reg(OpndSize_32, add_opc, 0, PhysicalReg_EDX, true);
    //pointer to switch data
    move_reg_to_mem(OpndSize_32, PhysicalReg_EDX, true, 0, PhysicalReg_ESP, true);
#endif
    move_reg_to_mem(OpndSize_32, 1, false, 12, PhysicalReg_ESP, true);

    //if value out of range, fall through (no_op)
    //return targets[testVal - first_key]
    scratchRegs[0] = PhysicalReg_SCRATCH_1;
#if defined(WITH_JIT)
    call_dvmJitHandlePackedSwitch();
#else
    call_dvmNcgHandlePackedSwitch();
#endif
    load_effective_addr(16, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
#if !defined(WITH_JIT)
    //%eax: the relative NCG (relative to the instruction alu_binary_reg_reg
    compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
#endif
    //TODO: eax should be absolute address, call globalVREndOfBB, constVREndOfBB
    //conditional_jump_global_API(Condition_LE, "common_backwardBranch", false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod); //update GG VRs
#if defined(WITH_JIT)
    //get rPC, %eax has the relative PC offset
    alu_binary_imm_reg(OpndSize_32, add_opc, (int)dalvikPC, PhysicalReg_EAX, true);
    scratchRegs[0] = PhysicalReg_SCRATCH_2;
#if defined(WITH_JIT_TUNING)
    /* Fall back to interpreter after resolving address of switch target.
     * Indicate a kSwitchOverflow. Note: This is not an "overflow". But it helps
     * count the times we return from a Switch
     */
    move_imm_to_mem(OpndSize_32, kSwitchOverflow, 0, PhysicalReg_ESP, true);
#endif
    jumpToInterpNoChain();
#else
    get_eip_API();

    //codePtr2 points to alu_reg_reg
    insertDataWorklist((s4)tmp, codePtr1); //stream: codePtr2
    alu_binary_reg_reg(OpndSize_32, add_opc, PhysicalReg_EAX, true, PhysicalReg_EDX, true);
    unconditional_jump_reg(PhysicalReg_EDX, true);
#endif
    return 0;
}
#undef P_GPR_1

#define P_GPR_1 PhysicalReg_EBX

/**
 * @brief Generate native code for bytecode sparse-switch
 * @param mir bytecode representation
 * @param dalvikPC program counter for Dalvik bytecode
 * @return value >= 0 when handled
 */
int op_sparse_switch(const MIR * mir, const u2 * dalvikPC) {
    assert(mir->dalvikInsn.opcode == OP_SPARSE_SWITCH);
    u2 vA = mir->dalvikInsn.vA;
    u4 tmp = mir->dalvikInsn.vB;
#ifdef DEBUG_EACH_BYTECODE
    u2 tSize = 0;
    const s4* keys = NULL;
    s4* entries = NULL;
#else
    u2* switchData = const_cast<u2 *>(dalvikPC) + (s4)tmp;

    if (*switchData++ != kSparseSwitchSignature) {
        /* should have been caught by verifier */
        dvmThrowInternalError(
                          "bad sparse switch magic");
        return 0; //no_op
    }
    u2 tSize = *switchData++;
    assert(tSize > 0);
    const s4* keys = (const s4*) switchData;
    assert(((u4)keys & 0x3) == 0);
    s4* entries = (s4*)switchData + tSize;
    assert(((u4)entries & 0x3) == 0);
#endif

    get_virtual_reg(vA, OpndSize_32, 1, false);
    //dvmNcgHandleSparseSwitch: keys, size, testVal
    load_effective_addr(-12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    move_imm_to_mem(OpndSize_32, tSize, 4, PhysicalReg_ESP, true);

#if defined(WITH_JIT)
    /* "keys" is constant for JIT
       it is the 1st argument to dvmJitHandleSparseSwitch */
    move_imm_to_mem(OpndSize_32, (int)keys, 0, PhysicalReg_ESP, true);
#else
    //keys and entries relocatable: eip + relativePC
    //must update -4(%esp) after ncgGetEIP
    get_eip_API();

    char* codePtr1 = stream;
    alu_binary_imm_reg(OpndSize_32, add_opc, 0, PhysicalReg_EDX, true);
    move_reg_to_mem(OpndSize_32, PhysicalReg_EDX, true, 0, PhysicalReg_ESP, true);
#endif
    move_reg_to_mem(OpndSize_32, 1, false, 8, PhysicalReg_ESP, true);

    scratchRegs[0] = PhysicalReg_SCRATCH_1;
    //if testVal is in keys, return the corresponding target
    //otherwise, fall through (no_op)
#if defined(WITH_JIT)
    call_dvmJitHandleSparseSwitch();
#else
    call_dvmNcgHandleSparseSwitch();
#endif
    load_effective_addr(12, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
#if !defined(WITH_JIT)
    compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true);
#endif
    //TODO: eax should be absolute address, call globalVREndOfBB constVREndOfBB
    //conditional_jump_global_API(Condition_LE, "common_backwardBranch", false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
#if defined(WITH_JIT)
    //get rPC, %eax has the relative PC offset
    alu_binary_imm_reg(OpndSize_32, add_opc, (int)dalvikPC, PhysicalReg_EAX, true);
    scratchRegs[0] = PhysicalReg_SCRATCH_2;
#if defined(WITH_JIT_TUNING)
    /* Fall back to interpreter after resolving address of switch target.
     * Indicate a kSwitchOverflow. Note: This is not an "overflow". But it helps
     * count the times we return from a Switch
     */
    move_imm_to_mem(OpndSize_32, kSwitchOverflow, 0, PhysicalReg_ESP, true);
#endif
    jumpToInterpNoChain();
#else
    get_eip_API();

    //codePtr2 points to alu_reg_reg
    insertDataWorklist((s4)tmp, codePtr1); //stream: codePtr2
    alu_binary_reg_reg(OpndSize_32, add_opc, PhysicalReg_EAX, true, PhysicalReg_EDX, true);
    unconditional_jump_reg(PhysicalReg_EDX, true);
#endif
    return 0;
}

#undef P_GPR_1

#define P_GPR_1 PhysicalReg_EBX

/**
 * @brief Generate native code for bytecode if-eq
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_eq(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_EQ);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    s2 tmp = mir->dalvikInsn.vC;
    get_virtual_reg(vA, OpndSize_32, 1, false);
    compare_VR_reg(OpndSize_32, vB, 1, false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_NE, Condition_E);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-ne
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_ne(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_NE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    s2 tmp = mir->dalvikInsn.vC;
    get_virtual_reg(vA, OpndSize_32, 1, false);
    compare_VR_reg(OpndSize_32, vB, 1, false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_E, Condition_NE);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-lt
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_lt(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_LT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    s2 tmp = mir->dalvikInsn.vC;
    get_virtual_reg(vA, OpndSize_32, 1, false);
    compare_VR_reg(OpndSize_32, vB, 1, false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_GE, Condition_L);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-ge
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_ge(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_GE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    s2 tmp = mir->dalvikInsn.vC;
    get_virtual_reg(vA, OpndSize_32, 1, false);
    compare_VR_reg(OpndSize_32, vB, 1, false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_L, Condition_GE);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-gt
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_gt(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_GT);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    s2 tmp = mir->dalvikInsn.vC;
    get_virtual_reg(vA, OpndSize_32, 1, false);
    compare_VR_reg(OpndSize_32, vB, 1, false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_LE, Condition_G);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-le
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_le(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_LE);
    u2 vA = mir->dalvikInsn.vA;
    u2 vB = mir->dalvikInsn.vB;
    s2 tmp = mir->dalvikInsn.vC;
    get_virtual_reg(vA, OpndSize_32, 1, false);
    compare_VR_reg(OpndSize_32, vB, 1, false);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_G, Condition_LE);
    return 0;
}
#undef P_GPR_1

/**
 * @brief Generate native code for bytecode if-eqz
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_eqz(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_EQZ);
    u2 vA = mir->dalvikInsn.vA;
    s2 tmp = mir->dalvikInsn.vB;
    compare_imm_VR(OpndSize_32, 0, vA);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_NE, Condition_E);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-nez
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_nez(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_NEZ);
    u2 vA = mir->dalvikInsn.vA;
    s2 tmp = mir->dalvikInsn.vB;
    compare_imm_VR(OpndSize_32, 0, vA);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_E, Condition_NE);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-ltz
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_ltz(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_LTZ);
    u2 vA = mir->dalvikInsn.vA;
    s2 tmp = mir->dalvikInsn.vB;
    compare_imm_VR(OpndSize_32, 0, vA);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_GE, Condition_L);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-gez
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_gez(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_GEZ);
    u2 vA = mir->dalvikInsn.vA;
    s2 tmp = mir->dalvikInsn.vB;
    compare_imm_VR(OpndSize_32, 0, vA);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_L, Condition_GE);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-gtz
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_gtz(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_GTZ);
    u2 vA = mir->dalvikInsn.vA;
    s2 tmp = mir->dalvikInsn.vB;
    compare_imm_VR(OpndSize_32, 0, vA);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_LE, Condition_G);
    return 0;
}

/**
 * @brief Generate native code for bytecode if-lez
 * @param mir bytecode representation
 * @return value >= 0 when handled
 */
int op_if_lez(const MIR * mir) {
    assert(mir->dalvikInsn.opcode == OP_IF_LEZ);
    u2 vA = mir->dalvikInsn.vA;
    s2 tmp = mir->dalvikInsn.vB;
    compare_imm_VR(OpndSize_32, 0, vA);
    constVREndOfBB();
    globalVREndOfBB(currentMethod);
    common_if(tmp, Condition_G, Condition_LE);
    return 0;
}

#define P_GPR_1 PhysicalReg_ECX
#define P_GPR_2 PhysicalReg_EBX
/*!
\brief helper function common_periodicChecks4 to check GC request
BCOffset in %edx
*/
int common_periodicChecks4() {
    insertLabel("common_periodicChecks4", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif

#if (!defined(ENABLE_TRACING))
    get_self_pointer(PhysicalReg_ECX, true);
    move_mem_to_reg(OpndSize_32, offsetof(Thread, suspendCount), PhysicalReg_ECX, true, PhysicalReg_EAX, true);
    compare_imm_reg(OpndSize_32, 0, PhysicalReg_EAX, true); //suspendCount
    conditional_jump(Condition_NE, "common_handleSuspend4", true); //called once
    x86_return();

    insertLabel("common_handleSuspend4", true);
    push_reg_to_stack(OpndSize_32, PhysicalReg_ECX, true);
    call_dvmCheckSuspendPending();
    load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    x86_return();

#else
    ///////////////////
    //get debuggerActive: 3 memory accesses, and $7
    move_mem_to_reg(OpndSize_32, offGlue_pSelfSuspendCount, PhysicalReg_Glue, true, P_GPR_1, true);
    move_mem_to_reg(OpndSize_32, offGlue_pIntoDebugger, PhysicalReg_Glue, true, P_GPR_2, true);

    compare_imm_mem(OpndSize_32, 0, 0, P_GPR_1, true); //suspendCount
    conditional_jump(Condition_NE, "common_handleSuspend4_1", true); //called once

    compare_imm_mem(OpndSize_32, 0, 0, P_GPR_2, true); //debugger active

    conditional_jump(Condition_NE, "common_debuggerActive4", true);

    //recover registers and return
    x86_return();

    insertLabel("common_handleSuspend4_1", true);
    push_mem_to_stack(OpndSize_32, offGlue_self, PhysicalReg_Glue, true);
    call_dvmCheckSuspendPending();
    load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    x86_return();

    insertLabel("common_debuggerActive4", true);
    //%edx: offsetBC (at run time, get method->insns_bytecode, then calculate BCPointer)
    move_mem_to_reg(OpndSize_32, offGlue_method, PhysicalReg_Glue, true, P_GPR_1, true);
    move_mem_to_reg(OpndSize_32, offMethod_insns_bytecode, P_GPR_1, true, P_GPR_2, true);
    alu_binary_reg_reg(OpndSize_32, add_opc, P_GPR_2, true, PhysicalReg_EDX, true);
    move_imm_to_mem(OpndSize_32, 0, offGlue_entryPoint, PhysicalReg_Glue, true);
    unconditional_jump("common_gotoBail", false); //update glue->rPC with edx
#endif

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_periodicChecks4");
    }
#endif
    return 0;
}
//input: %edx PC adjustment
//CHECK: should %edx be saved before calling dvmCheckSuspendPending?
/*!
\brief helper function common_periodicChecks_entry to check GC request

*/
int common_periodicChecks_entry() {
    insertLabel("common_periodicChecks_entry", false);
#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    scratchRegs[0] = PhysicalReg_ESI; scratchRegs[1] = PhysicalReg_EAX;
    scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;
    get_suspendCount(P_GPR_1, true);

    //get debuggerActive: 3 memory accesses, and $7
#if 0 //defined(WITH_DEBUGGER)
    get_debuggerActive(P_GPR_2, true);
#endif

    compare_imm_reg(OpndSize_32, 0, P_GPR_1, true); //suspendCount
    conditional_jump(Condition_NE, "common_handleSuspend", true); //called once

#if 0 //defined(WITH_DEBUGGER)
#ifdef NCG_DEBUG
    compare_imm_reg(OpndSize_32, 0, P_GPR_2, true); //debugger active
    conditional_jump(Condition_NE, "common_debuggerActive", true);
#endif
#endif

    //recover registers and return
    x86_return();
    insertLabel("common_handleSuspend", true);
    get_self_pointer(P_GPR_1, true);
    load_effective_addr(-4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    move_reg_to_mem(OpndSize_32, P_GPR_1, true, 0, PhysicalReg_ESP, true);
    call_dvmCheckSuspendPending();
    load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    x86_return();
#ifdef NCG_DEBUG
    insertLabel("common_debuggerActive", true);
    //adjust PC!!! use 0(%esp) TODO
    set_glue_entryPoint_imm(0); //kInterpEntryInstr);
    unconditional_jump("common_gotoBail", false);
#endif

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_periodicChecks_entry");
    }
#endif

    return 0;
}
#undef P_GPR_1
#undef P_GPR_2
/*!
\brief helper function common_gotoBail
  input: %edx: BCPointer %esi: Glue
  set %eax to 1 (switch interpreter = true), recover the callee-saved registers and return
*/
int common_gotoBail() {
    insertLabel("common_gotoBail", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif
    //scratchRegs[0] = PhysicalReg_EDX; scratchRegs[1] = PhysicalReg_ESI;
    //scratchRegs[2] = PhysicalReg_Null; scratchRegs[3] = PhysicalReg_Null;
    //save_pc_fp_to_glue();
    get_self_pointer(PhysicalReg_EAX, true);
    move_reg_to_mem(OpndSize_32, PhysicalReg_FP, true, offsetof(Thread, interpSave.curFrame), PhysicalReg_EAX, true);
    move_reg_to_mem(OpndSize_32, PhysicalReg_EDX, true, offsetof(Thread, interpSave.pc), PhysicalReg_EAX, true);

    move_mem_to_reg(OpndSize_32, offsetof(Thread, interpSave.bailPtr), PhysicalReg_EAX, true, PhysicalReg_ESP, true);
    move_reg_to_reg(OpndSize_32, PhysicalReg_ESP, true, PhysicalReg_EBP, true);
    load_effective_addr(FRAME_SIZE-4, PhysicalReg_EBP, true, PhysicalReg_EBP, true);
    move_imm_to_reg(OpndSize_32, 1, PhysicalReg_EAX, true); //return value
    move_mem_to_reg(OpndSize_32, -4, PhysicalReg_EBP, true, PhysicalReg_EDI, true);
    move_mem_to_reg(OpndSize_32, -8, PhysicalReg_EBP, true, PhysicalReg_ESI, true);
    move_mem_to_reg(OpndSize_32, -12, PhysicalReg_EBP, true, PhysicalReg_EBX, true);
    move_reg_to_reg(OpndSize_32, PhysicalReg_EBP, true, PhysicalReg_ESP, true);
    move_mem_to_reg(OpndSize_32, 0, PhysicalReg_ESP, true, PhysicalReg_EBP, true);
    load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    x86_return();

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_gotoBail");
    }
#endif
    return 0;
}
/*!
\brief helper function common_gotoBail_0

  set %eax to 0, recover the callee-saved registers and return
*/
int common_gotoBail_0() {
    insertLabel("common_gotoBail_0", false);

#if defined VTUNE_DALVIK
    int startStreamPtr = (int)stream;
#endif

    get_self_pointer(PhysicalReg_EAX, true);
    move_reg_to_mem(OpndSize_32, PhysicalReg_FP, true, offsetof(Thread, interpSave.curFrame), PhysicalReg_EAX, true);
    move_reg_to_mem(OpndSize_32, PhysicalReg_EDX, true, offsetof(Thread, interpSave.pc), PhysicalReg_EAX, true);

    /*
    movl    offThread_bailPtr(%ecx),%esp # Restore "setjmp" esp
    movl    %esp,%ebp
    addl    $(FRAME_SIZE-4), %ebp       # Restore %ebp at point of setjmp
    movl    EDI_SPILL(%ebp),%edi
    movl    ESI_SPILL(%ebp),%esi
    movl    EBX_SPILL(%ebp),%ebx
    movl    %ebp, %esp                   # strip frame
    pop     %ebp                         # restore caller's ebp
    ret                                  # return to dvmMterpStdRun's caller
    */
    move_mem_to_reg(OpndSize_32, offsetof(Thread, interpSave.bailPtr), PhysicalReg_EAX, true, PhysicalReg_ESP, true);
    move_reg_to_reg(OpndSize_32, PhysicalReg_ESP, true, PhysicalReg_EBP, true);
    load_effective_addr(FRAME_SIZE-4, PhysicalReg_EBP, true, PhysicalReg_EBP, true);
    move_imm_to_reg(OpndSize_32, 0, PhysicalReg_EAX, true); //return value
    move_mem_to_reg(OpndSize_32, -4, PhysicalReg_EBP, true, PhysicalReg_EDI, true);
    move_mem_to_reg(OpndSize_32, -8, PhysicalReg_EBP, true, PhysicalReg_ESI, true);
    move_mem_to_reg(OpndSize_32, -12, PhysicalReg_EBP, true, PhysicalReg_EBX, true);
    move_reg_to_reg(OpndSize_32, PhysicalReg_EBP, true, PhysicalReg_ESP, true);
    move_mem_to_reg(OpndSize_32, 0, PhysicalReg_ESP, true, PhysicalReg_EBP, true);
    load_effective_addr(4, PhysicalReg_ESP, true, PhysicalReg_ESP, true);
    x86_return();

#if defined VTUNE_DALVIK
    if(gDvmJit.vtuneInfo != kVTuneInfoDisabled) {
        int endStreamPtr = (int)stream;
        sendLabelInfoToVTune(startStreamPtr, endStreamPtr, "common_gotoBail_0");
    }
#endif
    return 0;
}


