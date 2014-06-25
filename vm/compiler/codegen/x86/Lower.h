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


/*! \file Lower.h
    \brief A header file to define interface between lowering, register allocator, and scheduling
*/

#ifndef _DALVIK_LOWER
#define _DALVIK_LOWER

#if defined(WITH_JIT)
#define CODE_CACHE_PADDING 1024 //code space for a single bytecode
// comment out for phase 1 porting
#define PREDICTED_CHAINING
#define JIT_CHAIN
#define WITH_JIT_INLINING
#endif

#define NCG_O1
//compilaton flags used by NCG O1
#ifdef NCG_O1
#define DUMP_EXCEPTION //to measure performance, required to have correct exception handling
/*! multiple versions for hardcoded registers */
#define HARDREG_OPT
#define CFG_OPT
/*! remove redundant move ops when accessing virtual registers */
#define MOVE_OPT
/*! remove redundant spill of virtual registers */
#define SPILL_OPT
#define XFER_OPT
//#define DSE_OPT //no perf improvement for cme
/*! use live range analysis to allocate registers */
#define LIVERANGE_OPT
/*! remove redundant null check */
#define NULLCHECK_OPT
//#define BOUNDCHECK_OPT
/*! optimize the access to glue structure */
#define GLUE_OPT
#define CALL_FIX
#define NATIVE_FIX
#define INVOKE_FIX //optimization
#define GETVR_FIX //optimization
#endif

#include "Dalvik.h"
#include "enc_wrapper.h"
#ifdef NCG_O1
#include "AnalysisO1.h"
#endif
#if defined(WITH_JIT)
#include "compiler/CompilerIR.h"
#endif

//compilation flags for debugging
//#define DEBUG_INFO
//#define DEBUG_CALL_STACK
//#define DEBUG_IGET_OBJ
//#define DEBUG_NCG_CODE_SIZE
//#define DEBUG_NCG
//#define DEBUG_NCG_1
//#define DEBUG_LOADING
//#define USE_INTERPRETER
//#define DEBUG_EACH_BYTECODE

/*! registers for functions are hardcoded */
#define HARDCODE_REG_CALL
#define HARDCODE_REG_SHARE
#define HARDCODE_REG_HELPER
#ifndef NCG_O1
/*! registers for NCG O0 are hardcoded */
  #define HARDCODE_REG
#endif

#define PhysicalReg_FP PhysicalReg_EDI
#define PhysicalReg_Glue PhysicalReg_EBP

//COPIED from interp/InterpDefs.h
#define FETCH(_offset) (rPC[(_offset)])
#define INST_INST(_inst) ((_inst) & 0xff)
#define INST_A(_inst)       (((_inst) >> 8) & 0x0f)
#define INST_B(_inst)       ((_inst) >> 12)
#define INST_AA(_inst)      ((_inst) >> 8)

//#include "vm/mterp/common/asm-constants.h"
#define offEBP_self 8
#define offEBP_spill -56
#define offThread_exception 68
#define offClassObject_descriptor 24
#define offArrayObject_length 8
#ifdef PROFILE_FIELD_ACCESS
#define offStaticField_value 24
#define offInstField_byteOffset 24
#else
#define offStaticField_value 16
#define offInstField_byteOffset 16
#endif

#ifdef EASY_GDB
#define offStackSaveArea_prevFrame 4
#define offStackSaveArea_savedPc 8
#define offStackSaveArea_method 12
#define offStackSaveArea_localRefTop 16 // -> StackSaveArea.xtra.locakRefCookie
#define offStackSaveArea_returnAddr 20
#define offStackSaveArea_isDebugInterpreted 24
#define sizeofStackSaveArea 24
#else
#define offStackSaveArea_prevFrame 0
#define offStackSaveArea_savedPc 4
#define offStackSaveArea_method 8
#define offStackSaveArea_localRefTop 12 // -> StackSaveArea.xtra.locakRefCookie
#define offStackSaveArea_returnAddr 16
#define offStackSaveArea_isDebugInterpreted 20
#define sizeofStackSaveArea 20
#endif

#define offClassObject_status 44
#define offClassObject_accessFlags 32
#ifdef MTERP_NO_UNALIGN_64
#define offArrayObject_contents 16
#else
#define offArrayObject_contents 12
#endif

#define offField_clazz 0
#define offObject_clazz 0
#define offClassObject_vtable 116
#define offClassObject_pDvmDex 40
#define offClassObject_super 72
#define offClassObject_vtableCount 112
#define offMethod_name 16
#define offMethod_accessFlags 4
#define offMethod_methodIndex 8
#define offMethod_registersSize 10
#define offMethod_outsSize 12
#define offGlue_interpStackEnd 32
#if defined(WITH_JIT)
#define offThread_inJitCodeCache 124
#define offThread_jniLocal_nextEntry 168
#else
#define offThread_jniLocal_nextEntry 100 // same as offThread_jniLocal_topCookie
#endif
#define offMethod_insns 32
#ifdef ENABLE_TRACING
#define offMethod_insns_bytecode 44
#define offMethod_insns_ncg 48
#endif

#define offGlue_pc     0
#define offGlue_fp     4
#define offGlue_retval 8

#define offThread_curFrame 4
#define offGlue_method 16
#define offGlue_methodClassDex 20
#define offGlue_self 24
#define offGlue_pSelfSuspendCount 36
#define offGlue_cardTable 40
#define offGlue_pDebuggerActive 44
#define offGlue_pActiveProfilers 48
#define offGlue_entryPoint 52
#if defined(WITH_JIT)
#define offGlue_icRechainCount 84
#define offGlue_espEntry 88
#define offGlue_spillRegion 92
#else
#define offGlue_espEntry 60
#define offGlue_spillRegion 64
#endif
#define offDvmDex_pResStrings 8
#define offDvmDex_pResClasses 12
#define offDvmDex_pResMethods 16
#define offDvmDex_pResFields  20
#define offMethod_clazz       0

// Definitions must be consistent with vm/mterp/x86-atom/header.S
#if defined(WITH_JIT)
#define FRAME_SIZE     124
#else
#define FRAME_SIZE     76
#endif

typedef enum ArgsDoneType {
    ArgsDone_Normal = 0,
    ArgsDone_Native,
    ArgsDone_Full
} ArgsDoneType;

/*! An enum type
    to list bytecodes for AGET, APUT
*/
typedef enum ArrayAccess {
    AGET, AGET_WIDE, AGET_CHAR, AGET_SHORT, AGET_BOOLEAN, AGET_BYTE,
    APUT, APUT_WIDE, APUT_CHAR, APUT_SHORT, APUT_BOOLEAN, APUT_BYTE
} ArrayAccess;
/*! An enum type
    to list bytecodes for IGET, IPUT
*/
typedef enum InstanceAccess {
    IGET, IGET_WIDE, IPUT, IPUT_WIDE
} InstanceAccess;
/*! An enum type
    to list bytecodes for SGET, SPUT
*/
typedef enum StaticAccess {
    SGET, SGET_WIDE, SPUT, SPUT_WIDE
} StaticAccess;

typedef enum JmpCall_type {
    JmpCall_uncond = 1,
    JmpCall_cond,
    JmpCall_reg, //jump reg32
    JmpCall_call
} JmpCall_type;

//! \enum AtomOpCode
//! \brief Pseudo-mnemonics for Atom
//! \details Initially included to be in sync with ArmOpCode which specifies
//! additional pseudo mnemonics for use during codegen, but it has
//! diverted. Although there are references to this everywhere,
//! very little of this is actually used for functionality.
//! \todo Either refactor to match ArmOpCode or remove dependency on this.
enum AtomOpCode {
    ATOM_PSEUDO_CHAINING_CELL_BACKWARD_BRANCH = -15,
    ATOM_NORMAL_ALU = -14,
    ATOM_PSEUDO_ENTRY_BLOCK = -13,
    ATOM_PSEUDO_EXIT_BLOCK = -12,
    ATOM_PSEUDO_TARGET_LABEL = -11,
    ATOM_PSEUDO_CHAINING_CELL_HOT = -10,
    ATOM_PSEUDO_CHAINING_CELL_INVOKE_PREDICTED = -9,
    ATOM_PSEUDO_CHAINING_CELL_INVOKE_SINGLETON = -8,
    ATOM_PSEUDO_CHAINING_CELL_NORMAL = -7,
    ATOM_PSEUDO_DALVIK_BYTECODE_BOUNDARY = -6,
    ATOM_PSEUDO_ALIGN4 = -5,
    ATOM_PSEUDO_PC_RECONSTRUCTION_CELL = -4,
    ATOM_PSEUDO_PC_RECONSTRUCTION_BLOCK_LABEL = -3,
    ATOM_PSEUDO_EH_BLOCK_LABEL = -2,
    ATOM_PSEUDO_NORMAL_BLOCK_LABEL = -1,
    ATOM_NORMAL,
};

//! \enum LowOpndType
//! \brief Defines types of operands that a LowOp can have.
//! \details The Imm, Mem, and Reg variants correspond literally to what
//! the final encoded x86 instruction will have. The others are used for
//! additional behavior needed before the x86 encoding.
//! \see LowOp
enum LowOpndType {
    //! \brief Immediate
    LowOpndType_Imm,
    //! \brief Register
    LowOpndType_Reg,
    //! \brief Memory access
    LowOpndType_Mem,
    //! \brief Used for jumps to labels
    LowOpndType_Label,
    //! \brief Used for jumps to other blocks
    LowOpndType_BlockId,
    //! \brief Used for chaining
    LowOpndType_Chain
};

//! \enum LowOpndDefUse
//! \brief Defines type of usage that a LowOpnd can have.
//! \see LowOpnd
enum LowOpndDefUse {
    //! \brief Definition
    LowOpndDefUse_Def,
    //! \brief Usage
    LowOpndDefUse_Use,
    //! \brief Usage and Definition
    LowOpndDefUse_UseDef
};

//! \enum MemoryAccessType
//! \brief Classifies type of memory access.
enum MemoryAccessType {
    //! \brief access Dalvik virtual register
    MemoryAccess_VR,
    //! \brief access spill region
    MemoryAccess_SPILL,
    //! \brief unclassified memory access
    MemoryAccess_Unknown
};

//! \enum UseDefEntryType
//! \brief Defines types of resources on which there can be a dependency.
enum UseDefEntryType {
    //! \brief Control flags
    UseDefType_Ctrl,
    //! \brief Floating-point stack
    UseDefType_Float,
    //! \brief Dalvik virtual register. Corresponds to MemoryAccess_VR
    UseDefType_MemVR,
    //! \brief Spill region. Corresponds to MemoryAccess_SPILL
    UseDefType_MemSpill,
    //! \brief Unclassified memory access. Corresponds to MemoryAccess_Unknown
    UseDefType_MemUnknown,
    //! \brief Register
    UseDefType_Reg
};

//! \enum DependencyType
//! \brief Defines types of dependencies on a resource.
enum DependencyType {
    //! \brief Read after Write
    Dependency_RAW,
    //! \brief Write after Write
    Dependency_WAW,
    //! \brief Write after Read
    Dependency_WAR,
};

//! \brief Defines a relationship between a resource and its producer.
struct UseDefProducerEntry {
    //! \brief Resource type on which there is a dependency.
    UseDefEntryType type;
    //! \brief Logical or physical register this resource is
    //! associated with.
    //! \details When physical, this is of enum type PhysicalReg.
    //! When logical, this is the index of the logical register.
    //! When there is no register related dependency, this is
    //! negative.
    //! \todo Is this correct? What about VRs?
    int regNum;
    //! \brief Corresponds to LowOp::slotId to keep track of producer.
    unsigned int producerSlot;
};

//! \brief Defines a relationship between a resource and its users.
struct UseDefUserEntry {
    //! \brief Resource type on which there is a dependency.
    UseDefEntryType type;
    //! \brief Logical or physical register this resource is
    //! associated with.
    //! \details When physical, this is of enum type PhysicalReg.
    //! When logical, this is the index of the logical register.
    //! When there is no register related dependency, this is
    //! negative.
    //! \todo Is this correct? What about VRs?
    int regNum;
    //! \brief A list of LowOp::slotId to keep track of all users
    //! of this resource.
    std::vector<unsigned int> useSlotsList;
};

//! \brief Holds information on the data dependencies
struct DependencyInformation {
    //! \brief Type of data hazard
    DependencyType dataHazard;
    //! \brief Holds the LowOp::slotId of the LIR that causes this
    //! data dependence.
    unsigned int lowopSlotId;
    //! \brief Holds latency information for edges in the
    //! dependency graph, not execute to execute latency for the
    //! instructions.
    int latency;
};

//! \brief Holds general information about an operand.
struct LowOpnd {
    //! \brief Classification of operand.
    LowOpndType type;
    //! \brief Size of operand.
    OpndSize size;
    //! \brief Usage, definition, or both of operand.
    LowOpndDefUse defuse;
};

//! \brief Holds information about a register operand.
struct LowOpndReg {
    //! \brief Classification on type of register.
    LowOpndRegType regType;
    //! \brief Register number, either logical or physical.
    int regNum;
    //! \brief When false, register is logical.
    bool isPhysical;
};

//! \brief Holds information about an immediate operand.
struct LowOpndImm {
    //! \brief Value of the immediate.
    s4 value;
};

//! \brief Holds information about an immediate operand where the immediate
//! has not been generated yet.
struct LowOpndBlock {
    //! \brief Holds id of MIR level basic block.
    s4 value;
};

//! \brief Defines maximum length of string holding label name.
#define LABEL_SIZE 256

//! \brief Holds information about an immediate operand where the immediate
//! has not been generated yet from label.
struct LowOpndLabel {
    //! \brief Name of the label for which to generate immediate.
    char label[LABEL_SIZE];
    //! \brief This is true when label is short term distance from caller
    //! and an 8-bit operand is sufficient.
    bool isLocal;
};

//! \brief Holds information about a memory operand.
struct LowOpndMem {
    //! \brief Displacement
    LowOpndImm m_disp;
    //! \brief Scaling
    LowOpndImm m_scale;
    //! \brief Index Register
    LowOpndReg m_index;
    //! \brief Base Register
    LowOpndReg m_base;
    //! \brief If true, must use the scaling value.
    bool hasScale;
    //! \brief Defines type of memory access.
    MemoryAccessType mType;
    //! \brief
    //! \todo What is this used for?
    int index;
};

//! \brief Data structure for an x86 LIR.
//! \todo Decouple fields used for scheduling from this struct.
//! is a good idea if using it throughout the trace JIT and never
//! actually passing it for scheduling.
struct LowOp {
    //! \brief Holds general LIR information (Google's implementation)
    //! \warning Only offset information is used for x86 and the other
    //! fields are not valid except in LowOpBlockLabel.
    LIR generic;
    //! \brief x86 mnemonic for instruction
    Mnemonic opCode;
    //! \brief x86 pseudo-mnemonic
    AtomOpCode opCode2;
    //! \brief Destination operand
    //! \details This is not used when there are only 0 or 1 operands.
    LowOpnd opndDest;
    //! \brief Source operand
    //! \details This is used when there is a single operand.
    LowOpnd opndSrc;
    //! \brief Holds number of operands for this LIR (0, 1, or 2)
    unsigned short numOperands;
    //! \brief Logical timestamp for ordering.
    //! \details This value should uniquely identify an LIR and also
    //! provide natural ordering depending on when it was requested.
    //! This is used during scheduling to hold original order for the
    //! native basic block.
    unsigned int slotId;
    //! \brief Logical time for when the LIR is ready.
    //! \details This field is used only for scheduling.
    int readyTime;
    //! \brief Logical time for when the LIR is scheduled.
    //! \details This field is used only for scheduling.
    int scheduledTime;
    //! \brief Execute to execute time for this instruction.
    //! \details This field is used only for scheduling.
    //! \see MachineModelEntry::executeToExecuteLatency
    int instructionLatency;
    //! \brief Issue port for this instruction.
    //! \details This field is used only for scheduling.
    //! \see MachineModelEntry::issuePortType
    int portType;
    //! \brief Holds information about LowOps on which current LowOp
    //! depends on (predecessors).
    //! \details For example, if a LowOp with slotId of 3 depends on
    //! LowOp with slotId of 2 because of a RAW, then the LowOp with
    //! slotId of 3 will have an entry in the predecessorDependencies
    //! with a Dependency_RAW and slotId of 2. This field is used
    //! only for scheduling.
    std::vector<DependencyInformation> predecessorDependencies;
    //! \brief Holds information about LowOps that depend on current
    //! LowOp (successors).
    //! \details For example, if a LowOp with slotId of 3 depends on
    //! LowOp with slotId of 2 because of a RAW, then the LowOp with
    //! slotId of 2 will have an entry in the successorDependencies
    //! with a Dependency_RAW and slotId of 3. This field is used
    //! only for scheduling.
    std::vector<DependencyInformation> successorDependencies;
    //! \brief Weight of longest path in dependency graph from
    //! current instruction to end of the basic block.
    //! \details This field is used only for scheduling.
    int longestPath;
};

//! \brief Specialized LowOp with known label operand but
//! whose offset immediate is not known yet.
struct LowOpLabel : LowOp {
    //! \brief Label operand whose immediate has not yet been
    //! generated.
    LowOpndLabel labelOpnd;
};

//! \brief Specialized LowOp for use with block operand whose id
//! is known but the offset immediate has not been generated yet.
struct LowOpBlock : LowOp {
    //! \brief Non-generated immediate operand
    LowOpndBlock blockIdOpnd;
};

//! \brief Specialized LowOp which is only used with
//! pseudo-mnemonic.
//! \see AtomOpCode
struct LowOpBlockLabel {
    //! \todo Does not use inheritance like the other LowOp
    //! data structures because of a git merge issue. In future,
    //! this can be safely updated.
    LowOp lop;
    //! \brief Holds offset information.
    LowOpndImm immOpnd;
};

//! \brief Specialized LowOp with an immediate operand.
struct LowOpImm : LowOp {
    //! \brief Immediate
    LowOpndImm immOpnd;
};

//! \brief Specialized LowOp with a memory operand.
struct LowOpMem : LowOp {
    //! \brief Memory Operand
    LowOpndMem memOpnd;
};

//! \brief Specialized LowOp with register operand.
struct LowOpReg : LowOp {
    //! \brief Register
    LowOpndReg regOpnd;
};

//! \brief Specialized LowOp for immediate to register.
struct LowOpImmReg : LowOp {
    //! \brief Immediate as source.
    LowOpndImm immSrc;
    //! \brief Register as destination.
    LowOpndReg regDest;
};

//! \brief Specialized LowOp for register to register.
struct LowOpRegReg : LowOp {
    //! \brief Register as source.
    LowOpndReg regSrc;
    //! \brief Register as destination.
    LowOpndReg regDest;
};

//! \brief Specialized LowOp for memory to register.
struct LowOpMemReg : LowOp {
    //! \brief Memory as source.
    LowOpndMem memSrc;
    //! \brief Register as destination.
    LowOpndReg regDest;
};

//! \brief Specialized LowOp for immediate to memory.
struct LowOpImmMem : LowOp {
    //! \brief Immediate as source.
    LowOpndImm immSrc;
    //! \brief Memory as destination.
    LowOpndMem memDest;
};

//! \brief Specialized LowOp for register to memory.
struct LowOpRegMem : LowOp {
    //! \brief Register as source.
    LowOpndReg regSrc;
    //! \brief Memory as destination.
    LowOpndMem memDest;
};



/*!
\brief data structure for labels used when lowering a method

four label maps are defined: globalMap globalShortMap globalWorklist globalShortWorklist
globalMap: global labels where codePtr points to the label
           freeLabelMap called in clearNCG
globalWorklist: global labels where codePtr points to an instruciton using the label
  standalone NCG -------
                accessed by insertLabelWorklist & performLabelWorklist
  code cache ------
                inserted by performLabelWorklist(false),
                handled & cleared by generateRelocation in NcgFile.c
globalShortMap: local labels where codePtr points to the label
                freeShortMap called after generation of one bytecode
globalShortWorklist: local labels where codePtr points to an instruction using the label
                accessed by insertShortWorklist & insertLabel
definition of local label: life time of the label is within a bytecode or within a helper function
extra label maps are used by code cache:
  globalDataWorklist VMAPIWorklist
*/
typedef struct LabelMap {
  char label[LABEL_SIZE];
  char* codePtr; //code corresponding to the label or code that uses the label
  struct LabelMap* nextItem;
  OpndSize size;
  uint  addend;
} LabelMap;
/*!
\brief data structure to handle forward jump (GOTO, IF)

accessed by insertNCGWorklist & performNCGWorklist
*/
typedef struct NCGWorklist {
  //when WITH_JIT, relativePC stores the target basic block id
  s4 relativePC; //relative offset in bytecode
  int offsetPC;  //PC in bytecode
  int offsetNCG; //PC in native code
  char* codePtr; //code for native jump instruction
  struct NCGWorklist* nextItem;
  OpndSize size;
}NCGWorklist;
/*!
\brief data structure to handle SWITCH & FILL_ARRAY_DATA

two data worklist are defined: globalDataWorklist (used by code cache) & methodDataWorklist
methodDataWorklist is accessed by insertDataWorklist & performDataWorklist
*/
typedef struct DataWorklist {
  s4 relativePC; //relative offset in bytecode to access the data
  int offsetPC;  //PC in bytecode
  int offsetNCG; //PC in native code
  char* codePtr; //code for native instruction add_imm_reg imm, %edx
  char* codePtr2;//code for native instruction add_reg_reg %eax, %edx for SWITCH
                 //                            add_imm_reg imm, %edx for FILL_ARRAY_DATA
  struct DataWorklist* nextItem;
}DataWorklist;
#ifdef ENABLE_TRACING
typedef struct MapWorklist {
  u4 offsetPC;
  u4 offsetNCG;
  int isStartOfPC; //1 --> true 0 --> false
  struct MapWorklist* nextItem;
} MapWorklist;
#endif

#define BUFFER_SIZE 1024 //# of Low Ops buffered
//the following three numbers are hardcoded, please CHECK
#define BYTECODE_SIZE_PER_METHOD 81920
#define NATIVE_SIZE_PER_DEX 19000000 //FIXME for core.jar: 16M --> 18M for O1
#define NATIVE_SIZE_FOR_VM_STUBS 100000
#define MAX_HANDLER_OFFSET 1024 //maximal number of handler offsets

extern int LstrClassCastExceptionPtr, LstrInstantiationErrorPtr, LstrInternalError, LstrFilledNewArrayNotImpl;
extern int LstrArithmeticException, LstrArrayIndexException, LstrArrayStoreException, LstrStringIndexOutOfBoundsException;
extern int LstrDivideByZero, LstrNegativeArraySizeException, LstrNoSuchMethodError, LstrNullPointerException;
extern int LdoubNeg, LvaluePosInfLong, LvalueNegInfLong, LvalueNanLong, LshiftMask, Lvalue64, L64bits, LintMax, LintMin;

extern LabelMap* globalMap;
extern LabelMap* globalShortMap;
extern LabelMap* globalWorklist;
extern LabelMap* globalShortWorklist;
extern NCGWorklist* globalNCGWorklist;
extern DataWorklist* methodDataWorklist;
#ifdef ENABLE_TRACING
extern MapWorklist* methodMapWorklist;
#endif
extern PhysicalReg scratchRegs[4];

#define C_SCRATCH_1 scratchRegs[0]
#define C_SCRATCH_2 scratchRegs[1]
#define C_SCRATCH_3 scratchRegs[2] //scratch reg inside callee

extern LowOp* ops[BUFFER_SIZE];
extern bool isScratchPhysical;
extern u2* rPC;
extern int offsetPC;
extern int offsetNCG;
extern int mapFromBCtoNCG[BYTECODE_SIZE_PER_METHOD];
extern char* streamStart;

extern char* streamCode;

extern char* streamMethodStart; //start of the method
extern char* stream; //current stream pointer
extern char* streamMisPred;
extern int lowOpTimeStamp;
extern Method* currentMethod;
#if defined(WITH_JIT)
extern int currentExceptionBlockIdx;
#endif

extern int globalMapNum;
extern int globalWorklistNum;
extern int globalDataWorklistNum;
extern int globalPCWorklistNum;
#if defined(WITH_JIT)
extern int chainingWorklistNum;
#endif
extern int VMAPIWorklistNum;

extern LabelMap* globalDataWorklist;
extern LabelMap* globalPCWorklist;
#if defined(WITH_JIT)
extern LabelMap* chainingWorklist;
#endif
extern LabelMap* VMAPIWorklist;

extern int ncgClassNum;
extern int ncgMethodNum;

class Scheduler;
extern Scheduler g_SchedulerInstance;

bool existATryBlock(Method* method, int startPC, int endPC);
#ifdef NCG_O1
// interface between register allocator & lowering
extern int num_removed_nullCheck;

int registerAlloc(int type, int reg, bool isPhysical, bool updateRef);
int registerAllocMove(int reg, int type, bool isPhysical, int srcReg);
int checkVirtualReg(int reg, LowOpndRegType type, int updateRef); //returns the physical register
int updateRefCount(int reg, LowOpndRegType type);
int updateRefCount2(int reg, int type, bool isPhysical);
int spillVirtualReg(int vrNum, LowOpndRegType type, bool updateTable);
int isVirtualRegConstant(int regNum, LowOpndRegType type, int* valuePtr, bool updateRef);
int checkTempReg(int reg, int type, bool isPhysical, int vA);
bool checkTempReg2(int reg, int type, bool isPhysical, int physicalRegForVR, u2 vB);
int freeReg(bool spillGL);
int nextVersionOfHardReg(PhysicalReg pReg, int refCount);
int updateVirtualReg(int reg, LowOpndRegType type);
void setVRNullCheck(int regNum, OpndSize size);
bool isVRNullCheck(int regNum, OpndSize size);
void setVRBoundCheck(int vr_array, int vr_index);
bool isVRBoundCheck(int vr_array, int vr_index);
int requestVRFreeDelay(int regNum, u4 reason);
void cancelVRFreeDelayRequest(int regNum, u4 reason);
bool getVRFreeDelayRequested(int regNum);
bool isGlueHandled(int glue_reg);
void resetGlue(int glue_reg);
void updateGlue(int reg, bool isPhysical, int glue_reg);
int updateVRAtUse(int reg, LowOpndRegType pType, int regAll);
int touchEcx();
int touchEax();
int touchEdx();
int beforeCall(const char* target);
int afterCall(const char* target);
void startBranch();
void endBranch();
void rememberState(int);
void goToState(int);
void transferToState(int);
void globalVREndOfBB(const Method*);
void constVREndOfBB();
bool hasVRStoreExitOfLoop();
void storeVRExitOfLoop();
void startNativeCode(int num, int type);
void endNativeCode();
void donotSpillReg(int physicalReg);
void doSpillReg(int physicalReg);
#endif

#define XMM_1 PhysicalReg_XMM0
#define XMM_2 PhysicalReg_XMM1
#define XMM_3 PhysicalReg_XMM2
#define XMM_4 PhysicalReg_XMM3

/////////////////////////////////////////////////////////////////////////////////
//LR[reg] = disp + PR[base_reg] or disp + LR[base_reg]
void load_effective_addr(int disp, int base_reg, bool isBasePhysical,
                          int reg, bool isPhysical);
void load_effective_addr_scale(int base_reg, bool isBasePhysical,
                                int index_reg, bool isIndexPhysical, int scale,
                                int reg, bool isPhysical);
//! lea reg, [base_reg + index_reg*scale + disp]
void load_effective_addr_scale_disp(int base_reg, bool isBasePhysical, int disp,
                int index_reg, bool isIndexPhysical, int scale,
                int reg, bool isPhysical);
void load_fpu_cw(int disp, int base_reg, bool isBasePhysical);
void store_fpu_cw(bool checkException, int disp, int base_reg, bool isBasePhysical);
void convert_integer(OpndSize srcSize, OpndSize dstSize);
void load_fp_stack(LowOp* op, OpndSize size, int disp, int base_reg, bool isBasePhysical);
void load_int_fp_stack(OpndSize size, int disp, int base_reg, bool isBasePhysical);
void load_int_fp_stack_imm(OpndSize size, int imm);
void store_fp_stack(LowOp* op, bool pop, OpndSize size, int disp, int base_reg, bool isBasePhysical);
void store_int_fp_stack(LowOp* op, bool pop, OpndSize size, int disp, int base_reg, bool isBasePhysical);

void load_fp_stack_VR(OpndSize size, int vA);
void load_int_fp_stack_VR(OpndSize size, int vA);
void store_fp_stack_VR(bool pop, OpndSize size, int vA);
void store_int_fp_stack_VR(bool pop, OpndSize size, int vA);
void compare_VR_ss_reg(int vA, int reg, bool isPhysical);
void compare_VR_sd_reg(int vA, int reg, bool isPhysical);
void fpu_VR(ALU_Opcode opc, OpndSize size, int vA);
void compare_reg_mem(LowOp* op, OpndSize size, int reg, bool isPhysical,
                           int disp, int base_reg, bool isBasePhysical);
void compare_mem_reg(OpndSize size,
                           int disp, int base_reg, bool isBasePhysical,
                           int reg, bool isPhysical);
void compare_VR_reg(OpndSize size,
                           int vA,
                           int reg, bool isPhysical);
void compare_imm_reg(OpndSize size, int imm,
                           int reg, bool isPhysical);
void compare_imm_mem(OpndSize size, int imm,
                           int disp, int base_reg, bool isBasePhysical);
void compare_imm_VR(OpndSize size, int imm,
                           int vA);
void compare_reg_reg(int reg1, bool isPhysical1,
                           int reg2, bool isPhysical2);
void compare_reg_reg_16(int reg1, bool isPhysical1,
                         int reg2, bool isPhysical2);
void compare_ss_mem_reg(LowOp* op, int disp, int base_reg, bool isBasePhysical,
                              int reg, bool isPhysical);
void compare_ss_reg_with_reg(LowOp* op, int reg1, bool isPhysical1,
                              int reg2, bool isPhysical2);
void compare_sd_mem_with_reg(LowOp* op, int disp, int base_reg, bool isBasePhysical,
                              int reg, bool isPhysical);
void compare_sd_reg_with_reg(LowOp* op, int reg1, bool isPhysical1,
                              int reg2, bool isPhysical2);
void compare_fp_stack(bool pop, int reg, bool isDouble);
void test_imm_reg(OpndSize size, int imm, int reg, bool isPhysical);
void test_imm_mem(OpndSize size, int imm, int disp, int reg, bool isPhysical);

void conditional_move_reg_to_reg(OpndSize size, ConditionCode cc, int reg1, bool isPhysical1, int reg, bool isPhysical);
void move_ss_mem_to_reg(LowOp* op, int disp, int base_reg, bool isBasePhysical,
                        int reg, bool isPhysical);
void move_ss_reg_to_mem(LowOp* op, int reg, bool isPhysical,
                         int disp, int base_reg, bool isBasePhysical);
LowOpMemReg* move_ss_mem_to_reg_noalloc(int disp, int base_reg, bool isBasePhysical,
                         MemoryAccessType mType, int mIndex,
                         int reg, bool isPhysical);
LowOpRegMem* move_ss_reg_to_mem_noalloc(int reg, bool isPhysical,
                         int disp, int base_reg, bool isBasePhysical,
                         MemoryAccessType mType, int mIndex);
void move_sd_mem_to_reg(int disp, int base_reg, bool isBasePhysical,
                         int reg, bool isPhysical);
void move_sd_reg_to_mem(LowOp* op, int reg, bool isPhysical,
                         int disp, int base_reg, bool isBasePhysical);

void conditional_jump(ConditionCode cc, const char* target, bool isShortTerm);
void unconditional_jump(const char* target, bool isShortTerm);
void conditional_jump_int(ConditionCode cc, int target, OpndSize size);
void unconditional_jump_int(int target, OpndSize size);
void conditional_jump_block(ConditionCode cc, int targetBlockId);
void unconditional_jump_block(int targetBlockId);
void unconditional_jump_reg(int reg, bool isPhysical);
void call(const char* target);
void call_reg(int reg, bool isPhysical);
void call_reg_noalloc(int reg, bool isPhysical);
void call_mem(int disp, int reg, bool isPhysical);
void x86_return();

void alu_unary_reg(OpndSize size, ALU_Opcode opc, int reg, bool isPhysical);
void alu_unary_mem(LowOp* op, OpndSize size, ALU_Opcode opc, int disp, int base_reg, bool isBasePhysical);

void alu_binary_imm_mem(OpndSize size, ALU_Opcode opc,
                         int imm, int disp, int base_reg, bool isBasePhysical);
void alu_binary_imm_reg(OpndSize size, ALU_Opcode opc, int imm, int reg, bool isPhysical);
void alu_binary_mem_reg(OpndSize size, ALU_Opcode opc,
                         int disp, int base_reg, bool isBasePhysical,
                         int reg, bool isPhysical);
void alu_binary_VR_reg(OpndSize size, ALU_Opcode opc, int vA, int reg, bool isPhysical);
void alu_sd_binary_VR_reg(ALU_Opcode opc, int vA, int reg, bool isPhysical, bool isSD);
void alu_binary_reg_reg(OpndSize size, ALU_Opcode opc,
                         int reg1, bool isPhysical1,
                         int reg2, bool isPhysical2);
void alu_binary_reg_mem(OpndSize size, ALU_Opcode opc,
                         int reg, bool isPhysical,
                         int disp, int base_reg, bool isBasePhysical);

void fpu_mem(LowOp* op, ALU_Opcode opc, OpndSize size, int disp, int base_reg, bool isBasePhysical);
void alu_ss_binary_reg_reg(ALU_Opcode opc, int reg, bool isPhysical,
                            int reg2, bool isPhysical2);
void alu_sd_binary_reg_reg(ALU_Opcode opc, int reg, bool isPhysical,
                            int reg2, bool isPhysical2);

void push_mem_to_stack(OpndSize size, int disp, int base_reg, bool isBasePhysical);
void push_reg_to_stack(OpndSize size, int reg, bool isPhysical);

//returns the pointer to end of the native code
void move_reg_to_mem(OpndSize size,
                      int reg, bool isPhysical,
                      int disp, int base_reg, bool isBasePhysical);
LowOpMemReg* move_mem_to_reg(OpndSize size,
                      int disp, int base_reg, bool isBasePhysical,
                      int reg, bool isPhysical);
void movez_mem_to_reg(OpndSize size,
                      int disp, int base_reg, bool isBasePhysical,
                      int reg, bool isPhysical);
void movez_reg_to_reg(OpndSize size,
                      int reg, bool isPhysical,
                      int reg2, bool isPhysical2);
void moves_mem_to_reg(LowOp* op, OpndSize size,
                      int disp, int base_reg, bool isBasePhysical,
                      int reg, bool isPhysical);
void movez_mem_disp_scale_to_reg(OpndSize size,
                      int base_reg, bool isBasePhysical,
                      int disp, int index_reg, bool isIndexPhysical, int scale,
                      int reg, bool isPhysical);
void moves_mem_disp_scale_to_reg(OpndSize size,
                      int base_reg, bool isBasePhysical,
                      int disp, int index_reg, bool isIndexPhysical, int scale,
                      int reg, bool isPhysical);
void move_reg_to_reg(OpndSize size,
                      int reg, bool isPhysical,
                      int reg2, bool isPhysical2);
void move_reg_to_reg_noalloc(OpndSize size,
                      int reg, bool isPhysical,
                      int reg2, bool isPhysical2);
void move_mem_scale_to_reg(OpndSize size,
                            int base_reg, bool isBasePhysical, int index_reg, bool isIndexPhysical, int scale,
                            int reg, bool isPhysical);
void move_mem_disp_scale_to_reg(OpndSize size,
                int base_reg, bool isBasePhysical, int disp, int index_reg, bool isIndexPhysical, int scale,
                int reg, bool isPhysical);
void move_reg_to_mem_scale(OpndSize size,
                            int reg, bool isPhysical,
                            int base_reg, bool isBasePhysical, int index_reg, bool isIndexPhysical, int scale);
void move_reg_to_mem_disp_scale(OpndSize size,
                            int reg, bool isPhysical,
                            int base_reg, bool isBasePhysical, int disp, int index_reg, bool isIndexPhysical, int scale);
void move_imm_to_mem(OpndSize size, int imm,
                      int disp, int base_reg, bool isBasePhysical);
void set_VR_to_imm(u2 vA, OpndSize size, int imm);
void set_VR_to_imm_noalloc(u2 vA, OpndSize size, int imm);
void set_VR_to_imm_noupdateref(LowOp* op, u2 vA, OpndSize size, int imm);
void move_imm_to_reg(OpndSize size, int imm, int reg, bool isPhysical);
void move_imm_to_reg_noalloc(OpndSize size, int imm, int reg, bool isPhysical);

//LR[reg] = VR[vB]
//or
//PR[reg] = VR[vB]
void get_virtual_reg(u2 vB, OpndSize size, int reg, bool isPhysical);
void get_virtual_reg_noalloc(u2 vB, OpndSize size, int reg, bool isPhysical);
//VR[v] = LR[reg]
//or
//VR[v] = PR[reg]
void set_virtual_reg(u2 vA, OpndSize size, int reg, bool isPhysical);
void set_virtual_reg_noalloc(u2 vA, OpndSize size, int reg, bool isPhysical);
void get_VR_ss(int vB, int reg, bool isPhysical);
void set_VR_ss(int vA, int reg, bool isPhysical);
void get_VR_sd(int vB, int reg, bool isPhysical);
void set_VR_sd(int vA, int reg, bool isPhysical);

int spill_reg(int reg, bool isPhysical);
int unspill_reg(int reg, bool isPhysical);

void move_reg_to_mem_noalloc(OpndSize size,
                      int reg, bool isPhysical,
                      int disp, int base_reg, bool isBasePhysical,
                      MemoryAccessType mType, int mIndex);
LowOpMemReg* move_mem_to_reg_noalloc(OpndSize size,
                      int disp, int base_reg, bool isBasePhysical,
                      MemoryAccessType mType, int mIndex,
                      int reg, bool isPhysical);

//////////////////////////////////////////////////////////////
int insertLabel(const char* label, bool checkDup);
int export_pc();
int simpleNullCheck(int reg, bool isPhysical, int vr);
int nullCheck(int reg, bool isPhysical, int exceptionNum, int vr);
int handlePotentialException(
                             ConditionCode code_excep, ConditionCode code_okay,
                             int exceptionNum, const char* errName);
int get_currentpc(int reg, bool isPhysical);
int get_self_pointer(int reg, bool isPhysical);
int get_res_strings(int reg, bool isPhysical);
int get_res_classes(int reg, bool isPhysical);
int get_res_fields(int reg, bool isPhysical);
int get_res_methods(int reg, bool isPhysical);
int get_glue_method_class(int reg, bool isPhysical);
int get_glue_method(int reg, bool isPhysical);
int set_glue_method(int reg, bool isPhysical);
int get_glue_dvmdex(int reg, bool isPhysical);
int set_glue_dvmdex(int reg, bool isPhysical);
int get_suspendCount(int reg, bool isPhysical);
int get_return_value(OpndSize size, int reg, bool isPhysical);
int set_return_value(OpndSize size, int reg, bool isPhysical);
int clear_exception();
int get_exception(int reg, bool isPhysical);
int set_exception(int reg, bool isPhysical);
int save_pc_fp_to_glue();
int savearea_from_fp(int reg, bool isPhysical);

int call_moddi3();
int call_divdi3();
int call_fmod();
int call_fmodf();
int call_dvmFindCatchBlock();
int call_dvmThrowVerificationError();
int call_dvmAllocObject();
int call_dvmAllocArrayByClass();
int call_dvmResolveMethod();
int call_dvmResolveClass();
int call_dvmInstanceofNonTrivial();
int call_dvmThrow();
int call_dvmThrowWithMessage();
int call_dvmCheckSuspendPending();
int call_dvmLockObject();
int call_dvmUnlockObject();
int call_dvmInitClass();
int call_dvmAllocPrimitiveArray();
int call_dvmInterpHandleFillArrayData();
int call_dvmNcgHandlePackedSwitch();
int call_dvmNcgHandleSparseSwitch();
#if defined(WITH_JIT)
/*!
 * These functions will generate the asm instructions
 * to call the named function.
 */
int call_dvmJitHandlePackedSwitch();
int call_dvmJitHandleSparseSwitch();
int call_dvmJitToInterpTraceSelectNoChain();
int call_dvmJitToPatchPredictedChain();
int call_dvmJitToInterpNormal();
int call_dvmJitToInterpBackwardBranch(void);
int call_dvmJitToInterpTraceSelect();
#endif
int call_dvmQuasiAtomicSwap64();
int call_dvmQuasiAtomicRead64();
int call_dvmCanPutArrayElement();
int call_dvmFindInterfaceMethodInCache();
int call_dvmHandleStackOverflow();
int call_dvmResolveString();
int call_dvmResolveInstField();
int call_dvmResolveStaticField();
#ifdef WITH_SELF_VERIFICATION
int call_selfVerificationLoad(void);
int call_selfVerificationStore(void);
int call_selfVerificationLoadDoubleword(void);
int call_selfVerificationStoreDoubleword(void);
#endif

//labels and branches
//shared branch to resolve class: 2 specialized versions
//OPTION 1: call & ret
//OPTION 2: store jump back label in a fixed register or memory
//jump to .class_resolve, then jump back
//OPTION 3: share translator code
/* global variables: ncg_rPC */
int resolve_class(
                  int startLR/*logical register index*/, bool isPhysical, int tmp/*const pool index*/,
                  int thirdArg);
/* EXPORT_PC; movl exceptionPtr, -8(%esp); movl descriptor, -4(%esp); lea; call; lea; jmp */
int throw_exception_message(int exceptionPtr, int obj_reg, bool isPhysical,
                            int startLR/*logical register index*/, bool startPhysical);
/* EXPORT_PC; movl exceptionPtr, -8(%esp); movl imm, -4(%esp); lea; call; lea; jmp */
int throw_exception(int exceptionPtr, int imm,
                    int startLR/*logical register index*/, bool startPhysical);

void freeShortMap();
int insertDataWorklist(s4 relativePC, char* codePtr1);
#ifdef ENABLE_TRACING
int insertMapWorklist(s4 BCOffset, s4 NCGOffset, int isStartOfPC);
#endif
int performNCGWorklist();
int performDataWorklist();
void performLabelWorklist();
void performMethodLabelWorklist();
void freeLabelMap();
void performSharedWorklist();
void performChainingWorklist();
void freeNCGWorklist();
void freeDataWorklist();
void freeLabelWorklist();
#if defined(WITH_JIT)
void freeChainingWorklist();
#endif

int common_backwardBranch();
int common_exceptionThrown();
int common_errNullObject();
int common_errArrayIndex();
int common_errArrayStore();
int common_errNegArraySize();
int common_errNoSuchMethod();
int common_errDivideByZero();
int common_periodicChecks_entry();
int common_periodicChecks4();
int common_gotoBail();
int common_gotoBail_0();
int common_errStringIndexOutOfBounds();
void goto_invokeArgsDone();

#if defined VTUNE_DALVIK
void sendLabelInfoToVTune(int startStreamPtr, int endStreamPtr, const char* labelName);
#endif

//lower a bytecode
int lowerByteCode(const Method* method, const MIR * mir, const u2 * dalvikPC);

int op_nop(const MIR * mir);
int op_move(const MIR * mir);
int op_move_from16(const MIR * mir);
int op_move_16(const MIR * mir);
int op_move_wide(const MIR * mir);
int op_move_wide_from16(const MIR * mir);
int op_move_wide_16(const MIR * mir);
int op_move_result(const MIR * mir);
int op_move_result_wide(const MIR * mir);
int op_move_exception(const MIR * mir);

int op_return_void(const MIR * mir);
int op_return(const MIR * mir);
int op_return_wide(const MIR * mir);
int op_const_4(const MIR * mir);
int op_const_16(const MIR * mir);
int op_const(const MIR * mir);
int op_const_high16(const MIR * mir);
int op_const_wide_16(const MIR * mir);
int op_const_wide_32(const MIR * mir);
int op_const_wide(const MIR * mir);
int op_const_wide_high16(const MIR * mir);
int op_const_string(const MIR * mir);
int op_const_string_jumbo(const MIR * mir);
int op_const_class(const MIR * mir);
int op_monitor_enter(const MIR * mir);
int op_monitor_exit(const MIR * mir);
int op_check_cast(const MIR * mir);
int op_instance_of(const MIR * mir);

int op_array_length(const MIR * mir);
int op_new_instance(const MIR * mir);
int op_new_array(const MIR * mir);
int op_filled_new_array(const MIR * mir);
int op_filled_new_array_range(const MIR * mir);
int op_fill_array_data(const MIR * mir, const u2 * dalvikPC);
int op_throw(const MIR * mir);
int op_throw_verification_error(const MIR * mir);
int op_goto(const MIR * mir);
int op_goto_16(const MIR * mir);
int op_goto_32(const MIR * mir);
int op_packed_switch(const MIR * mir, const u2 * dalvikPC);
int op_sparse_switch(const MIR * mir, const u2 * dalvikPC);
int op_if_ge(const MIR * mir);
int op_aget(const MIR * mir);
int op_aget_wide(const MIR * mir);
int op_aget_object(const MIR * mir);
int op_aget_boolean(const MIR * mir);
int op_aget_byte(const MIR * mir);
int op_aget_char(const MIR * mir);
int op_aget_short(const MIR * mir);
int op_aput(const MIR * mir);
int op_aput_wide(const MIR * mir);
int op_aput_object(const MIR * mir);
int op_aput_boolean(const MIR * mir);
int op_aput_byte(const MIR * mir);
int op_aput_char(const MIR * mir);
int op_aput_short(const MIR * mir);
int op_iget(const MIR * mir);
int op_iget_wide(const MIR * mir, bool isVolatile);
int op_iget_object(const MIR * mir);
int op_iget_boolean(const MIR * mir);
int op_iget_byte(const MIR * mir);
int op_iget_char(const MIR * mir);
int op_iget_short(const MIR * mir);
int op_iput(const MIR * mir);
int op_iput_wide(const MIR * mir, bool isVolatile);
int op_iput_object(const MIR * mir);
int op_iput_boolean(const MIR * mir);
int op_iput_byte(const MIR * mir);
int op_iput_char(const MIR * mir);
int op_iput_short(const MIR * mir);
int op_sget(const MIR * mir);
int op_sget_wide(const MIR * mir, bool isVolatile);
int op_sget_object(const MIR * mir);
int op_sget_boolean(const MIR * mir);
int op_sget_byte(const MIR * mir);
int op_sget_char(const MIR * mir);
int op_sget_short(const MIR * mir);
int op_sput(const MIR * mir, bool isObj);
int op_sput_wide(const MIR * mir, bool isVolatile);
int op_sput_object(const MIR * mir);
int op_sput_boolean(const MIR * mir);
int op_sput_byte(const MIR * mir);
int op_sput_char(const MIR * mir);
int op_sput_short(const MIR * mir);
int op_invoke_virtual(const MIR * mir);
int op_invoke_super(const MIR * mir);
int op_invoke_direct(const MIR * mir);
int op_invoke_static(const MIR * mir);
int op_invoke_interface(const MIR * mir);
int op_invoke_virtual_range(const MIR * mir);
int op_invoke_super_range(const MIR * mir);
int op_invoke_direct_range(const MIR * mir);
int op_invoke_static_range(const MIR * mir);
int op_invoke_interface_range(const MIR * mir);
int op_int_to_long(const MIR * mir);
int op_add_long_2addr(const MIR * mir);
int op_add_int_lit8(const MIR * mir);
int op_cmpl_float(const MIR * mir);
int op_cmpg_float(const MIR * mir);
int op_cmpl_double(const MIR * mir);
int op_cmpg_double(const MIR * mir);
int op_cmp_long(const MIR * mir);
int op_if_eq(const MIR * mir);
int op_if_ne(const MIR * mir);
int op_if_lt(const MIR * mir);
int op_if_gt(const MIR * mir);
int op_if_le(const MIR * mir);
int op_if_eqz(const MIR * mir);
int op_if_nez(const MIR * mir);
int op_if_ltz(const MIR * mir);
int op_if_gez(const MIR * mir);
int op_if_gtz(const MIR * mir);
int op_if_lez(const MIR * mir);
int op_neg_int(const MIR * mir);
int op_not_int(const MIR * mir);
int op_neg_long(const MIR * mir);
int op_not_long(const MIR * mir);
int op_neg_float(const MIR * mir);
int op_neg_double(const MIR * mir);
int op_int_to_float(const MIR * mir);
int op_int_to_double(const MIR * mir);
int op_long_to_int(const MIR * mir);
int op_long_to_float(const MIR * mir);
int op_long_to_double(const MIR * mir);
int op_float_to_int(const MIR * mir);
int op_float_to_long(const MIR * mir);
int op_float_to_double(const MIR * mir);
int op_double_to_int(const MIR * mir);
int op_double_to_long(const MIR * mir);
int op_double_to_float(const MIR * mir);
int op_int_to_byte(const MIR * mir);
int op_int_to_char(const MIR * mir);
int op_int_to_short(const MIR * mir);
int op_add_int(const MIR * mir);
int op_sub_int(const MIR * mir);
int op_mul_int(const MIR * mir);
int op_div_int(const MIR * mir);
int op_rem_int(const MIR * mir);
int op_and_int(const MIR * mir);
int op_or_int(const MIR * mir);
int op_xor_int(const MIR * mir);
int op_shl_int(const MIR * mir);
int op_shr_int(const MIR * mir);
int op_ushr_int(const MIR * mir);
int op_add_long(const MIR * mir);
int op_sub_long(const MIR * mir);
int op_mul_long(const MIR * mir);
int op_div_long(const MIR * mir);
int op_rem_long(const MIR * mir);
int op_and_long(const MIR * mir);
int op_or_long(const MIR * mir);
int op_xor_long(const MIR * mir);
int op_shl_long(const MIR * mir);
int op_shr_long(const MIR * mir);
int op_ushr_long(const MIR * mir);
int op_add_float(const MIR * mir);
int op_sub_float(const MIR * mir);
int op_mul_float(const MIR * mir);
int op_div_float(const MIR * mir);
int op_rem_float(const MIR * mir);
int op_add_double(const MIR * mir);
int op_sub_double(const MIR * mir);
int op_mul_double(const MIR * mir);
int op_div_double(const MIR * mir);
int op_rem_double(const MIR * mir);
int op_add_int_2addr(const MIR * mir);
int op_sub_int_2addr(const MIR * mir);
int op_mul_int_2addr(const MIR * mir);
int op_div_int_2addr(const MIR * mir);
int op_rem_int_2addr(const MIR * mir);
int op_and_int_2addr(const MIR * mir);
int op_or_int_2addr(const MIR * mir);
int op_xor_int_2addr(const MIR * mir);
int op_shl_int_2addr(const MIR * mir);
int op_shr_int_2addr(const MIR * mir);
int op_ushr_int_2addr(const MIR * mir);
int op_sub_long_2addr(const MIR * mir);
int op_mul_long_2addr(const MIR * mir);
int op_div_long_2addr(const MIR * mir);
int op_rem_long_2addr(const MIR * mir);
int op_and_long_2addr(const MIR * mir);
int op_or_long_2addr(const MIR * mir);
int op_xor_long_2addr(const MIR * mir);
int op_shl_long_2addr(const MIR * mir);
int op_shr_long_2addr(const MIR * mir);
int op_ushr_long_2addr(const MIR * mir);
int op_add_float_2addr(const MIR * mir);
int op_sub_float_2addr(const MIR * mir);
int op_mul_float_2addr(const MIR * mir);
int op_div_float_2addr(const MIR * mir);
int op_rem_float_2addr(const MIR * mir);
int op_add_double_2addr(const MIR * mir);
int op_sub_double_2addr(const MIR * mir);
int op_mul_double_2addr(const MIR * mir);
int op_div_double_2addr(const MIR * mir);
int op_rem_double_2addr(const MIR * mir);
int op_add_int_lit16(const MIR * mir);
int op_rsub_int(const MIR * mir);
int op_mul_int_lit16(const MIR * mir);
int op_div_int_lit16(const MIR * mir);
int op_rem_int_lit16(const MIR * mir);
int op_and_int_lit16(const MIR * mir);
int op_or_int_lit16(const MIR * mir);
int op_xor_int_lit16(const MIR * mir);
int op_rsub_int_lit8(const MIR * mir);
int op_mul_int_lit8(const MIR * mir);
int op_div_int_lit8(const MIR * mir);
int op_rem_int_lit8(const MIR * mir);
int op_and_int_lit8(const MIR * mir);
int op_or_int_lit8(const MIR * mir);
int op_xor_int_lit8(const MIR * mir);
int op_shl_int_lit8(const MIR * mir);
int op_shr_int_lit8(const MIR * mir);
int op_ushr_int_lit8(const MIR * mir);
int op_execute_inline(const MIR * mir, bool isRange);
int op_invoke_direct_empty(const MIR * mir);
int op_iget_quick(const MIR * mir);
int op_iget_wide_quick(const MIR * mir);
int op_iget_object_quick(const MIR * mir);
int op_iput_quick(const MIR * mir);
int op_iput_wide_quick(const MIR * mir);
int op_iput_object_quick(const MIR * mir);
int op_invoke_virtual_quick(const MIR * mir);
int op_invoke_virtual_quick_range(const MIR * mir);
int op_invoke_super_quick(const MIR * mir);
int op_invoke_super_quick_range(const MIR * mir);

///////////////////////////////////////////////
void set_reg_opnd(LowOpndReg* op_reg, int reg, bool isPhysical, LowOpndRegType type);
void set_mem_opnd(LowOpndMem* mem, int disp, int base, bool isPhysical);
void set_mem_opnd_scale(LowOpndMem* mem, int base, bool isPhysical, int disp, int index, bool indexPhysical, int scale);
LowOpImm* dump_imm(Mnemonic m, OpndSize size, int imm);
void dump_imm_update(int imm, char* codePtr, bool updateSecondOperand);
LowOpBlock* dump_blockid_imm(Mnemonic m, int targetBlockId);
LowOpMem* dump_mem(Mnemonic m, AtomOpCode m2, OpndSize size,
               int disp, int base_reg, bool isBasePhysical);
LowOpReg* dump_reg(Mnemonic m, AtomOpCode m2, OpndSize size,
               int reg, bool isPhysical, LowOpndRegType type);
LowOpReg* dump_reg_noalloc(Mnemonic m, OpndSize size,
               int reg, bool isPhysical, LowOpndRegType type);
LowOpImmMem* dump_imm_mem_noalloc(Mnemonic m, OpndSize size,
                           int imm,
                           int disp, int base_reg, bool isBasePhysical,
                           MemoryAccessType mType, int mIndex);
LowOpRegReg* dump_reg_reg(Mnemonic m, AtomOpCode m2, OpndSize size,
                   int reg, bool isPhysical,
                   int reg2, bool isPhysical2, LowOpndRegType type);
LowOpRegReg* dump_movez_reg_reg(Mnemonic m, OpndSize size,
                        int reg, bool isPhysical,
                        int reg2, bool isPhysical2);
LowOpMemReg* dump_mem_reg(Mnemonic m, AtomOpCode m2, OpndSize size,
                   int disp, int base_reg, bool isBasePhysical,
                   MemoryAccessType mType, int mIndex,
                   int reg, bool isPhysical, LowOpndRegType type);
LowOpMemReg* dump_mem_reg_noalloc(Mnemonic m, OpndSize size,
                           int disp, int base_reg, bool isBasePhysical,
                           MemoryAccessType mType, int mIndex,
                           int reg, bool isPhysical, LowOpndRegType type);
LowOpMemReg* dump_mem_scale_reg(Mnemonic m, OpndSize size,
                         int base_reg, bool isBasePhysical, int disp, int index_reg, bool isIndexPhysical, int scale,
                         int reg, bool isPhysical, LowOpndRegType type);
LowOpRegMem* dump_reg_mem_scale(Mnemonic m, OpndSize size,
                         int reg, bool isPhysical,
                         int base_reg, bool isBasePhysical, int disp, int index_reg, bool isIndexPhysical, int scale,
                         LowOpndRegType type);
LowOpRegMem* dump_reg_mem(Mnemonic m, AtomOpCode m2, OpndSize size,
                   int reg, bool isPhysical,
                   int disp, int base_reg, bool isBasePhysical,
                   MemoryAccessType mType, int mIndex, LowOpndRegType type);
LowOpRegMem* dump_reg_mem_noalloc(Mnemonic m, OpndSize size,
                           int reg, bool isPhysical,
                           int disp, int base_reg, bool isBasePhysical,
                           MemoryAccessType mType, int mIndex, LowOpndRegType type);
LowOpImmReg* dump_imm_reg(Mnemonic m, AtomOpCode m2, OpndSize size,
                   int imm, int reg, bool isPhysical, LowOpndRegType type, bool chaining);
LowOpImmMem* dump_imm_mem(Mnemonic m, AtomOpCode m2, OpndSize size,
                   int imm,
                   int disp, int base_reg, bool isBasePhysical,
                   MemoryAccessType mType, int mIndex, bool chaining);
LowOpRegMem* dump_fp_mem(Mnemonic m, OpndSize size, int reg,
                  int disp, int base_reg, bool isBasePhysical,
                  MemoryAccessType mType, int mIndex);
LowOpMemReg* dump_mem_fp(Mnemonic m, OpndSize size,
                  int disp, int base_reg, bool isBasePhysical,
                  MemoryAccessType mType, int mIndex,
                  int reg);
LowOpLabel* dump_label(Mnemonic m, OpndSize size, int imm,
               const char* label, bool isLocal);

unsigned getJmpCallInstSize(OpndSize size, JmpCall_type type);
#if defined(WITH_JIT)
bool lowerByteCodeJit(const Method* method, const MIR * mir, const u2 * dalvikPC);
void startOfBasicBlock(struct BasicBlock* bb);
extern LowOpBlockLabel* traceLabelList;
extern struct BasicBlock* traceCurrentBB;
extern JitMode traceMode;
extern bool branchInLoop;
void startOfTrace(const Method* method, LowOpBlockLabel* labelList, int, CompilationUnit*);
void endOfTrace(bool freeOnly);
LowOp* jumpToBasicBlock(char* instAddr, int targetId);
LowOp* condJumpToBasicBlock(char* instAddr, ConditionCode cc, int targetId);
bool jumpToException(const char* target);
int codeGenBasicBlockJit(const Method* method, BasicBlock* bb);
void endOfBasicBlock(struct BasicBlock* bb);
void handleExtendedMIR(CompilationUnit *cUnit, MIR *mir);
int insertChainingWorklist(int bbId, char * codeStart);
void startOfTraceO1(const Method* method, LowOpBlockLabel* labelList, int exceptionBlockId, CompilationUnit *cUnit);
void endOfTraceO1();
#endif
int isPowerOfTwo(int imm);
void move_chain_to_mem(OpndSize size, int imm,
                        int disp, int base_reg, bool isBasePhysical);
void move_chain_to_reg(OpndSize size, int imm, int reg, bool isPhysical);

void dumpImmToMem(int vrNum, OpndSize size, int value);
bool isInMemory(int regNum, OpndSize size);
int touchEbx();
int boundCheck(int vr_array, int reg_array, bool isPhysical_array,
               int vr_index, int reg_index, bool isPhysical_index,
               int exceptionNum);
int getRelativeOffset(const char* target, bool isShortTerm, JmpCall_type type, bool* unknown,
                      OpndSize* immSize);
int getRelativeNCG(s4 tmp, JmpCall_type type, bool* unknown, OpndSize* size);
void freeAtomMem();
OpndSize estOpndSizeFromImm(int target);

void preprocessingBB(BasicBlock* bb);
void preprocessingTrace();
void dump_nop(int size);
#endif

void pushCallerSavedRegs(void);
void popCallerSavedRegs(void);
