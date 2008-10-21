/*
 * Copyright (C) 2008 The Android Open Source Project
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
/*
 * Dalvik bytecode verifier.
 */
#ifndef _DALVIK_CODEVERIFY
#define _DALVIK_CODEVERIFY


/*
 * InsnFlags is a 32-bit integer with the following layout:
 *  0-15  instruction length (or 0 if this address doesn't hold an opcode)
 *  16    opcode flag (indicating this address holds an opcode)
 *  17    try block (indicating exceptions thrown here may be caught locally)
 *  30    visited (verifier has examined this instruction at least once)
 *  31    changed (set/cleared as bytecode verifier runs)
 */
typedef u4 InsnFlags;

#define kInsnFlagWidthMask      0x0000ffff
#define kInsnFlagInTry          (1 << 16)
#define kInsnFlagBranchTarget   (1 << 17)
#define kInsnFlagVisited        (1 << 30)
#define kInsnFlagChanged        (1 << 31)

/*
 * Returns "true" if the flags indicate that this address holds the start
 * of an instruction.
 */
INLINE bool dvmInsnIsOpcode(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagWidthMask) != 0;
}

/*
 * Extract the unsigned 16-bit instruction width from "flags".
 */
INLINE int dvmInsnGetWidth(const InsnFlags* insnFlags, int addr) {
    return insnFlags[addr] & kInsnFlagWidthMask;
}

/*
 * Changed?
 */
INLINE bool dvmInsnIsChanged(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagChanged) != 0;
}
INLINE void dvmInsnSetChanged(InsnFlags* insnFlags, int addr, bool changed)
{
    if (changed)
        insnFlags[addr] |= kInsnFlagChanged;
    else
        insnFlags[addr] &= ~kInsnFlagChanged;
}

/*
 * Visited?
 */
INLINE bool dvmInsnIsVisited(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagVisited) != 0;
}
INLINE void dvmInsnSetVisited(InsnFlags* insnFlags, int addr, bool changed)
{
    if (changed)
        insnFlags[addr] |= kInsnFlagVisited;
    else
        insnFlags[addr] &= ~kInsnFlagVisited;
}

/*
 * Visited or changed?
 */
INLINE bool dvmInsnIsVisitedOrChanged(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & (kInsnFlagVisited|kInsnFlagChanged)) != 0;
}

/*
 * In a "try" block?
 */
INLINE bool dvmInsnIsInTry(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagInTry) != 0;
}
INLINE void dvmInsnSetInTry(InsnFlags* insnFlags, int addr, bool inTry)
{
    assert(inTry);
    //if (inTry)
        insnFlags[addr] |= kInsnFlagInTry;
    //else
    //    insnFlags[addr] &= ~kInsnFlagInTry;
}

/*
 * Instruction is a branch target or exception handler?
 */
INLINE bool dvmInsnIsBranchTarget(const InsnFlags* insnFlags, int addr) {
    return (insnFlags[addr] & kInsnFlagBranchTarget) != 0;
}
INLINE void dvmInsnSetBranchTarget(InsnFlags* insnFlags, int addr,
    bool isBranch)
{
    assert(isBranch);
    //if (isBranch)
        insnFlags[addr] |= kInsnFlagBranchTarget;
    //else
    //    insnFlags[addr] &= ~kInsnFlagBranchTarget;
}


/*
 * Table that maps uninitialized instances to classes, based on the
 * address of the new-instance instruction.
 */
typedef struct UninitInstanceMap {
    int numEntries;
    struct {
        int             addr;   /* code offset, or -1 for method arg ("this") */
        ClassObject*    clazz;  /* class created at this address */
    } map[1];
} UninitInstanceMap;
#define kUninitThisArgAddr  (-1)
#define kUninitThisArgSlot  0

/*
 * Create a new UninitInstanceMap.
 */
UninitInstanceMap* dvmCreateUninitInstanceMap(const Method* meth,
    const InsnFlags* insnFlags, int newInstanceCount);

/*
 * Release the storage associated with an UninitInstanceMap.
 */
void dvmFreeUninitInstanceMap(UninitInstanceMap* uninitMap);

/*
 * Associate a class with an address.  Returns the map slot index, or -1
 * if the address isn't listed in the map (shouldn't happen) or if a
 * different class is already associated with the address (shouldn't
 * happen either).
 */
int dvmSetUninitInstance(UninitInstanceMap* uninitMap, int addr, 
    ClassObject* clazz);

/*
 * Return the class associated with an uninitialized reference.  Pass in
 * the map index.
 */
ClassObject* dvmGetUninitInstance(const UninitInstanceMap* uninitMap, int idx);

/*
 * Clear the class associated with an uninitialized reference.  Pass in
 * the map index.
 */
//void dvmClearUninitInstance(UninitInstanceMap* uninitMap, int idx);


/*
 * Verify bytecode in "meth".  "insnFlags" should be populated with
 * instruction widths and "in try" flags.
 */
bool dvmVerifyCodeFlow(const Method* meth, InsnFlags* insnFlags,
    UninitInstanceMap* uninitMap);

/*
 * Log standard method info for rejection message.
 */
void dvmLogVerifyFailure(const Method* meth, const char* format, ...);

/*
 * Extract the relative branch target from a branch instruction.
 */
bool dvmGetBranchTarget(const Method* meth, InsnFlags* insnFlags,
    int curOffset, int* pOffset, bool* pConditional);

#endif /*_DALVIK_CODEVERIFY*/
