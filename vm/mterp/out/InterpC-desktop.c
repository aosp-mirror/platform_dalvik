/*
 * This file was generated automatically by gen-mterp.py for 'desktop'.
 *
 * --> DO NOT EDIT <--
 */

/* File: c/header.c */
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

/* common includes */
#include "Dalvik.h"
#include "interp/InterpDefs.h"
#include "mterp/Mterp.h"
#include <math.h>                   // needed for fmod, fmodf


#define GOTO_TARGET_DECL(_target, ...)                                      \
    void dvmMterp_##_target(MterpGlue* glue, ## __VA_ARGS__);

GOTO_TARGET_DECL(filledNewArray, bool methodCallRange);
GOTO_TARGET_DECL(invokeVirtual, bool methodCallRange);
GOTO_TARGET_DECL(invokeSuper, bool methodCallRange);
GOTO_TARGET_DECL(invokeInterface, bool methodCallRange);
GOTO_TARGET_DECL(invokeDirect, bool methodCallRange);
GOTO_TARGET_DECL(invokeStatic, bool methodCallRange);
GOTO_TARGET_DECL(invokeVirtualQuick, bool methodCallRange);
GOTO_TARGET_DECL(invokeSuperQuick, bool methodCallRange);
GOTO_TARGET_DECL(invokeMethod, bool methodCallRange, const Method* methodToCall,
    u2 count, u2 regs);
GOTO_TARGET_DECL(returnFromMethod);
GOTO_TARGET_DECL(exceptionThrown);


/* File: c/opcommon.c */
/*
 * Redefine what used to be local variable accesses into MterpGlue struct
 * references.  (These are undefined down in "footer.c".)
 */
#define retval                  glue->retval
#define pc                      glue->pc
#define fp                      glue->fp
#define method                  glue->method
#define methodClassDex          glue->methodClassDex
#define self                    glue->self
//#define entryPoint              glue->entryPoint
#define debugTrackedRefStart    glue->debugTrackedRefStart


/*
 * Replace the opcode definition macros.  Here, each opcode is a separate
 * function that takes a "glue" argument and returns void.  We can't declare
 * these "static" because they may be called from an assembly stub.
 */
#undef HANDLE_OPCODE
#undef OP_END
#undef FINISH

#define HANDLE_OPCODE(_op)                                                  \
    void dvmMterp_##_op(MterpGlue* glue) {                                  \
        u2 ref, vsrc1, vsrc2, vdst;                                         \
        u2 inst = FETCH(0);

#define OP_END }

/*
 * Like standard FINISH, but don't reload "inst", and return to caller
 * when done.
 */
#define FINISH(_offset) {                                                   \
        ADJUST_PC(_offset);                                                 \
        CHECK_DEBUG_AND_PROF();                                             \
        CHECK_TRACKED_REFS();                                               \
        return;                                                             \
    }


/*
 * The "goto label" statements turn into function calls followed by
 * return statements.  Some of the functions take arguments.
 */
#define GOTO(_target, ...)                                                  \
    do {                                                                    \
        dvmMterp_##_target(glue, ## __VA_ARGS__);                           \
        return;                                                             \
    } while(false)

/*
 * As a special case, "goto bail" turns into a longjmp.  "_switch" should be
 * "true" if we need to switch to the other interpreter upon our return.
 */
#define GOTO_BAIL(_switch)                                                  \
    dvmMterpStdBail(glue, _switch);

/* for now, mterp is always a "standard" interpreter */
#define INTERP_TYPE INTERP_STD

/*
 * Periodic checks macro, slightly modified.
 */
#define PERIODIC_CHECKS(_entryPoint, _pcadj) {                              \
        dvmCheckSuspendQuick(self);                                         \
        if (NEED_INTERP_SWITCH(INTERP_TYPE)) {                              \
            ADJUST_PC(_pcadj);                                              \
            glue->entryPoint = _entryPoint;                                 \
            LOGVV("threadid=%d: switch to STD ep=%d adj=%d\n",              \
                glue->self->threadId, (_entryPoint), (_pcadj));             \
            GOTO_BAIL(true);                                                \
        }                                                                   \
    }


/*
 * ===========================================================================
 *
 * What follows are the "common" opcode definitions copied & pasted from the
 * basic interpreter.  The only changes that need to be made to the original
 * sources are:
 *  - replace "goto exceptionThrown" with "GOTO(exceptionThrown)"
 *
 * ===========================================================================
 */


#define HANDLE_NUMCONV(_opcode, _opname, _fromtype, _totype)                \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s v%d,v%d", (_opname), vdst, vsrc1);                       \
        SET_REGISTER##_totype(vdst,                                         \
            GET_REGISTER##_fromtype(vsrc1));                                \
        FINISH(1);

#define HANDLE_FLOAT_TO_INT(_opcode, _opname, _fromvtype, _fromrtype,       \
        _tovtype, _tortype)                                                 \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
    {                                                                       \
        /* spec defines specific handling for +/- inf and NaN values */     \
        _fromvtype val;                                                     \
        _tovtype intMin, intMax, result;                                    \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s v%d,v%d", (_opname), vdst, vsrc1);                       \
        val = GET_REGISTER##_fromrtype(vsrc1);                              \
        intMin = (_tovtype) 1 << (sizeof(_tovtype) * 8 -1);                 \
        intMax = ~intMin;                                                   \
        result = (_tovtype) val;                                            \
        if (val >= intMax)          /* +inf */                              \
            result = intMax;                                                \
        else if (val <= intMin)     /* -inf */                              \
            result = intMin;                                                \
        else if (val != val)        /* NaN */                               \
            result = 0;                                                     \
        else                                                                \
            result = (_tovtype) val;                                        \
        SET_REGISTER##_tortype(vdst, result);                               \
    }                                                                       \
    FINISH(1);

#define HANDLE_INT_TO_SMALL(_opcode, _opname, _type)                        \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|int-to-%s v%d,v%d", (_opname), vdst, vsrc1);                \
        SET_REGISTER(vdst, (_type) GET_REGISTER(vsrc1));                    \
        FINISH(1);

/* NOTE: the comparison result is always a signed 4-byte integer */
#define HANDLE_OP_CMPX(_opcode, _opname, _varType, _type, _nanVal)          \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        int result;                                                         \
        u2 regs;                                                            \
        _varType val1, val2;                                                \
        vdst = INST_AA(inst);                                               \
        regs = FETCH(1);                                                    \
        vsrc1 = regs & 0xff;                                                \
        vsrc2 = regs >> 8;                                                  \
        ILOGV("|cmp%s v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);         \
        val1 = GET_REGISTER##_type(vsrc1);                                  \
        val2 = GET_REGISTER##_type(vsrc2);                                  \
        if (val1 == val2)                                                   \
            result = 0;                                                     \
        else if (val1 < val2)                                               \
            result = -1;                                                    \
        else if (val1 > val2)                                               \
            result = 1;                                                     \
        else                                                                \
            result = (_nanVal);                                             \
        ILOGV("+ result=%d\n", result);                                     \
        SET_REGISTER(vdst, result);                                         \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_IF_XX(_opcode, _opname, _cmp)                             \
    HANDLE_OPCODE(_opcode /*vA, vB, +CCCC*/)                                \
        vsrc1 = INST_A(inst);                                               \
        vsrc2 = INST_B(inst);                                               \
        if ((s4) GET_REGISTER(vsrc1) _cmp (s4) GET_REGISTER(vsrc2)) {       \
            int branchOffset = (s2)FETCH(1);    /* sign-extended */         \
            ILOGV("|if-%s v%d,v%d,+0x%04x", (_opname), vsrc1, vsrc2,        \
                branchOffset);                                              \
            ILOGV("> branch taken");                                        \
            if (branchOffset < 0)                                           \
                PERIODIC_CHECKS(kInterpEntryInstr, branchOffset);           \
            FINISH(branchOffset);                                           \
        } else {                                                            \
            ILOGV("|if-%s v%d,v%d,-", (_opname), vsrc1, vsrc2);             \
            FINISH(2);                                                      \
        }

#define HANDLE_OP_IF_XXZ(_opcode, _opname, _cmp)                            \
    HANDLE_OPCODE(_opcode /*vAA, +BBBB*/)                                   \
        vsrc1 = INST_AA(inst);                                              \
        if ((s4) GET_REGISTER(vsrc1) _cmp 0) {                              \
            int branchOffset = (s2)FETCH(1);    /* sign-extended */         \
            ILOGV("|if-%s v%d,+0x%04x", (_opname), vsrc1, branchOffset);    \
            ILOGV("> branch taken");                                        \
            if (branchOffset < 0)                                           \
                PERIODIC_CHECKS(kInterpEntryInstr, branchOffset);           \
            FINISH(branchOffset);                                           \
        } else {                                                            \
            ILOGV("|if-%s v%d,-", (_opname), vsrc1);                        \
            FINISH(2);                                                      \
        }

#define HANDLE_UNOP(_opcode, _opname, _pfx, _sfx, _type)                    \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s v%d,v%d", (_opname), vdst, vsrc1);                       \
        SET_REGISTER##_type(vdst, _pfx GET_REGISTER##_type(vsrc1) _sfx);    \
        FINISH(1);

#define HANDLE_OP_X_INT(_opcode, _opname, _op, _chkdiv)                     \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-int v%d,v%d", (_opname), vdst, vsrc1);                   \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER(vsrc2) == 0) {                                 \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                GOTO(exceptionThrown);                                      \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            (s4) GET_REGISTER(vsrc1) _op (s4) GET_REGISTER(vsrc2));         \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_SHX_INT(_opcode, _opname, _cast, _op)                     \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-int v%d,v%d", (_opname), vdst, vsrc1);                   \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vsrc1) _op (GET_REGISTER(vsrc2) & 0x1f));    \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_X_INT_LIT16(_opcode, _opname, _cast, _op, _chkdiv)        \
    HANDLE_OPCODE(_opcode /*vA, vB, #+CCCC*/)                               \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        vsrc2 = FETCH(1);                                                   \
        ILOGV("|%s-int/lit16 v%d,v%d,#+0x%04x",                             \
            (_opname), vdst, vsrc1, vsrc2);                                 \
        if (_chkdiv) {                                                      \
            if ((s2) vsrc2 == 0) {                                          \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                GOTO(exceptionThrown);                                      \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vsrc1) _op (s2) vsrc2);                      \
        FINISH(2);

#define HANDLE_OP_X_INT_LIT8(_opcode, _opname, _op, _chkdiv)                \
    HANDLE_OPCODE(_opcode /*vAA, vBB, #+CC*/)                               \
    {                                                                       \
        u2 litInfo;                                                         \
        vdst = INST_AA(inst);                                               \
        litInfo = FETCH(1);                                                 \
        vsrc1 = litInfo & 0xff;                                             \
        vsrc2 = litInfo >> 8;       /* constant */                          \
        ILOGV("|%s-int/lit8 v%d,v%d,#+0x%02x",                              \
            (_opname), vdst, vsrc1, vsrc2);                                 \
        if (_chkdiv) {                                                      \
            if ((s1) vsrc2 == 0) {                                          \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                GOTO(exceptionThrown);                                      \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            (s4) GET_REGISTER(vsrc1) _op (s1) vsrc2);                       \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_SHX_INT_LIT8(_opcode, _opname, _cast, _op)                \
    HANDLE_OPCODE(_opcode /*vAA, vBB, #+CC*/)                               \
    {                                                                       \
        u2 litInfo;                                                         \
        vdst = INST_AA(inst);                                               \
        litInfo = FETCH(1);                                                 \
        vsrc1 = litInfo & 0xff;                                             \
        vsrc2 = litInfo >> 8;       /* constant */                          \
        ILOGV("|%s-int/lit8 v%d,v%d,#+0x%02x",                              \
            (_opname), vdst, vsrc1, vsrc2);                                 \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vsrc1) _op (vsrc2 & 0x1f));                  \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_X_INT_2ADDR(_opcode, _opname, _op, _chkdiv)               \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-int-2addr v%d,v%d", (_opname), vdst, vsrc1);             \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER(vsrc1) == 0) {                                 \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                GOTO(exceptionThrown);                                      \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            (s4) GET_REGISTER(vdst) _op (s4) GET_REGISTER(vsrc1));          \
        FINISH(1);

#define HANDLE_OP_SHX_INT_2ADDR(_opcode, _opname, _cast, _op)               \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-int-2addr v%d,v%d", (_opname), vdst, vsrc1);             \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vdst) _op (GET_REGISTER(vsrc1) & 0x1f));     \
        FINISH(1);

#define HANDLE_OP_X_LONG(_opcode, _opname, _op, _chkdiv)                    \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-long v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);       \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER_WIDE(vsrc2) == 0) {                            \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                GOTO(exceptionThrown);                                      \
            }                                                               \
        }                                                                   \
        SET_REGISTER_WIDE(vdst,                                             \
            (s8) GET_REGISTER_WIDE(vsrc1) _op (s8) GET_REGISTER_WIDE(vsrc2)); \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_SHX_LONG(_opcode, _opname, _cast, _op)                    \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-long v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);       \
        SET_REGISTER_WIDE(vdst,                                             \
            _cast GET_REGISTER_WIDE(vsrc1) _op (GET_REGISTER(vsrc2) & 0x3f)); \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_X_LONG_2ADDR(_opcode, _opname, _op, _chkdiv)              \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-long-2addr v%d,v%d", (_opname), vdst, vsrc1);            \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER_WIDE(vsrc1) == 0) {                            \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                GOTO(exceptionThrown);                                      \
            }                                                               \
        }                                                                   \
        SET_REGISTER_WIDE(vdst,                                             \
            (s8) GET_REGISTER_WIDE(vdst) _op (s8)GET_REGISTER_WIDE(vsrc1)); \
        FINISH(1);

#define HANDLE_OP_SHX_LONG_2ADDR(_opcode, _opname, _cast, _op)              \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-long-2addr v%d,v%d", (_opname), vdst, vsrc1);            \
        SET_REGISTER_WIDE(vdst,                                             \
            _cast GET_REGISTER_WIDE(vdst) _op (GET_REGISTER(vsrc1) & 0x3f)); \
        FINISH(1);

#define HANDLE_OP_X_FLOAT(_opcode, _opname, _op)                            \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-float v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);      \
        SET_REGISTER_FLOAT(vdst,                                            \
            GET_REGISTER_FLOAT(vsrc1) _op GET_REGISTER_FLOAT(vsrc2));       \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_X_DOUBLE(_opcode, _opname, _op)                           \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-double v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);     \
        SET_REGISTER_DOUBLE(vdst,                                           \
            GET_REGISTER_DOUBLE(vsrc1) _op GET_REGISTER_DOUBLE(vsrc2));     \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_X_FLOAT_2ADDR(_opcode, _opname, _op)                      \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-float-2addr v%d,v%d", (_opname), vdst, vsrc1);           \
        SET_REGISTER_FLOAT(vdst,                                            \
            GET_REGISTER_FLOAT(vdst) _op GET_REGISTER_FLOAT(vsrc1));        \
        FINISH(1);

#define HANDLE_OP_X_DOUBLE_2ADDR(_opcode, _opname, _op)                     \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-double-2addr v%d,v%d", (_opname), vdst, vsrc1);          \
        SET_REGISTER_DOUBLE(vdst,                                           \
            GET_REGISTER_DOUBLE(vdst) _op GET_REGISTER_DOUBLE(vsrc1));      \
        FINISH(1);

#define HANDLE_OP_AGET(_opcode, _opname, _type, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        ArrayObject* arrayObj;                                              \
        u2 arrayInfo;                                                       \
        EXPORT_PC();                                                        \
        vdst = INST_AA(inst);                                               \
        arrayInfo = FETCH(1);                                               \
        vsrc1 = arrayInfo & 0xff;    /* array ptr */                        \
        vsrc2 = arrayInfo >> 8;      /* index */                            \
        ILOGV("|aget%s v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);        \
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);                      \
        if (!checkForNull((Object*) arrayObj))                              \
            GOTO(exceptionThrown);                                          \
        if (GET_REGISTER(vsrc2) >= arrayObj->length) {                      \
            LOGV("Invalid array access: %p %d (len=%d)\n",                  \
                arrayObj, vsrc2, arrayObj->length);                         \
            dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;", \
                NULL);                                                      \
            GOTO(exceptionThrown);                                          \
        }                                                                   \
        SET_REGISTER##_regsize(vdst,                                        \
            ((_type*) arrayObj->contents)[GET_REGISTER(vsrc2)]);            \
        ILOGV("+ AGET[%d]=0x%x", GET_REGISTER(vsrc2), GET_REGISTER(vdst));  \
    }                                                                       \
    FINISH(2);

#define HANDLE_OP_APUT(_opcode, _opname, _type, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        ArrayObject* arrayObj;                                              \
        u2 arrayInfo;                                                       \
        EXPORT_PC();                                                        \
        vdst = INST_AA(inst);       /* AA: source value */                  \
        arrayInfo = FETCH(1);                                               \
        vsrc1 = arrayInfo & 0xff;   /* BB: array ptr */                     \
        vsrc2 = arrayInfo >> 8;     /* CC: index */                         \
        ILOGV("|aput%s v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);        \
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);                      \
        if (!checkForNull((Object*) arrayObj))                              \
            GOTO(exceptionThrown);                                          \
        if (GET_REGISTER(vsrc2) >= arrayObj->length) {                      \
            dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;", \
                NULL);                                                      \
            GOTO(exceptionThrown);                                          \
        }                                                                   \
        ILOGV("+ APUT[%d]=0x%08x", GET_REGISTER(vsrc2), GET_REGISTER(vdst));\
        ((_type*) arrayObj->contents)[GET_REGISTER(vsrc2)] =                \
            GET_REGISTER##_regsize(vdst);                                   \
    }                                                                       \
    FINISH(2);

/*
 * It's possible to get a bad value out of a field with sub-32-bit stores
 * because the -quick versions always operate on 32 bits.  Consider:
 *   short foo = -1  (sets a 32-bit register to 0xffffffff)
 *   iput-quick foo  (writes all 32 bits to the field)
 *   short bar = 1   (sets a 32-bit register to 0x00000001)
 *   iput-short      (writes the low 16 bits to the field)
 *   iget-quick foo  (reads all 32 bits from the field, yielding 0xffff0001)
 * This can only happen when optimized and non-optimized code has interleaved
 * access to the same field.  This is unlikely but possible.
 *
 * The easiest way to fix this is to always read/write 32 bits at a time.  On
 * a device with a 16-bit data bus this is sub-optimal.  (The alternative
 * approach is to have sub-int versions of iget-quick, but now we're wasting
 * Dalvik instruction space and making it less likely that handler code will
 * already be in the CPU i-cache.)
 */
#define HANDLE_IGET_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        InstField* ifield;                                                  \
        Object* obj;                                                        \
        EXPORT_PC();                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|iget%s v%d,v%d,field@0x%04x", (_opname), vdst, vsrc1, ref); \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNull(obj))                                             \
            GOTO(exceptionThrown);                                          \
        ifield = (InstField*) dvmDexGetResolvedField(methodClassDex, ref);  \
        if (ifield == NULL) {                                               \
            ifield = dvmResolveInstField(method->clazz, ref);               \
            if (ifield == NULL)                                             \
                GOTO(exceptionThrown);                                      \
        }                                                                   \
        SET_REGISTER##_regsize(vdst,                                        \
            dvmGetField##_ftype(obj, ifield->byteOffset));                  \
        ILOGV("+ IGET '%s'=0x%08llx", ifield->field.name,                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
        UPDATE_FIELD_GET(&ifield->field);                                   \
    }                                                                       \
    FINISH(2);

#define HANDLE_IGET_X_QUICK(_opcode, _opname, _ftype, _regsize)             \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        Object* obj;                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field offset */                          \
        ILOGV("|iget%s-quick v%d,v%d,field@+%u",                            \
            (_opname), vdst, vsrc1, ref);                                   \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNullExportPC(obj, fp, pc))                             \
            GOTO(exceptionThrown);                                          \
        SET_REGISTER##_regsize(vdst, dvmGetField##_ftype(obj, ref));        \
        ILOGV("+ IGETQ %d=0x%08llx", ref,                                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
    }                                                                       \
    FINISH(2);

#define HANDLE_IPUT_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        InstField* ifield;                                                  \
        Object* obj;                                                        \
        EXPORT_PC();                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|iput%s v%d,v%d,field@0x%04x", (_opname), vdst, vsrc1, ref); \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNull(obj))                                             \
            GOTO(exceptionThrown);                                          \
        ifield = (InstField*) dvmDexGetResolvedField(methodClassDex, ref);  \
        if (ifield == NULL) {                                               \
            ifield = dvmResolveInstField(method->clazz, ref);               \
            if (ifield == NULL)                                             \
                GOTO(exceptionThrown);                                      \
        }                                                                   \
        dvmSetField##_ftype(obj, ifield->byteOffset,                        \
            GET_REGISTER##_regsize(vdst));                                  \
        ILOGV("+ IPUT '%s'=0x%08llx", ifield->field.name,                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
        UPDATE_FIELD_PUT(&ifield->field);                                   \
    }                                                                       \
    FINISH(2);

#define HANDLE_IPUT_X_QUICK(_opcode, _opname, _ftype, _regsize)             \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        Object* obj;                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field offset */                          \
        ILOGV("|iput%s-quick v%d,v%d,field@0x%04x",                         \
            (_opname), vdst, vsrc1, ref);                                   \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNullExportPC(obj, fp, pc))                             \
            GOTO(exceptionThrown);                                          \
        dvmSetField##_ftype(obj, ref, GET_REGISTER##_regsize(vdst));        \
        ILOGV("+ IPUTQ %d=0x%08llx", ref,                                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
    }                                                                       \
    FINISH(2);

#define HANDLE_SGET_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, field@BBBB*/)                              \
    {                                                                       \
        StaticField* sfield;                                                \
        vdst = INST_AA(inst);                                               \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|sget%s v%d,sfield@0x%04x", (_opname), vdst, ref);           \
        sfield = (StaticField*)dvmDexGetResolvedField(methodClassDex, ref); \
        if (sfield == NULL) {                                               \
            EXPORT_PC();                                                    \
            sfield = dvmResolveStaticField(method->clazz, ref);             \
            if (sfield == NULL)                                             \
                GOTO(exceptionThrown);                                      \
        }                                                                   \
        SET_REGISTER##_regsize(vdst, dvmGetStaticField##_ftype(sfield));    \
        ILOGV("+ SGET '%s'=0x%08llx",                                       \
            sfield->field.name, (u8)GET_REGISTER##_regsize(vdst));          \
        UPDATE_FIELD_GET(&sfield->field);                                   \
    }                                                                       \
    FINISH(2);

#define HANDLE_SPUT_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, field@BBBB*/)                              \
    {                                                                       \
        StaticField* sfield;                                                \
        vdst = INST_AA(inst);                                               \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|sput%s v%d,sfield@0x%04x", (_opname), vdst, ref);           \
        sfield = (StaticField*)dvmDexGetResolvedField(methodClassDex, ref); \
        if (sfield == NULL) {                                               \
            EXPORT_PC();                                                    \
            sfield = dvmResolveStaticField(method->clazz, ref);             \
            if (sfield == NULL)                                             \
                GOTO(exceptionThrown);                                      \
        }                                                                   \
        dvmSetStaticField##_ftype(sfield, GET_REGISTER##_regsize(vdst));    \
        ILOGV("+ SPUT '%s'=0x%08llx",                                       \
            sfield->field.name, (u8)GET_REGISTER##_regsize(vdst));          \
        UPDATE_FIELD_PUT(&sfield->field);                                   \
    }                                                                       \
    FINISH(2);


/* File: c/OP_NOP.c */
HANDLE_OPCODE(OP_NOP)
    FINISH(1);
OP_END

/* File: c/OP_MOVE.c */
HANDLE_OPCODE(OP_MOVE /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|move%s v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(1);
OP_END

/* File: c/OP_MOVE_FROM16.c */
HANDLE_OPCODE(OP_MOVE_FROM16 /*vAA, vBBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|move%s/from16 v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE_FROM16) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(2);
OP_END

/* File: c/OP_MOVE_16.c */
HANDLE_OPCODE(OP_MOVE_16 /*vAAAA, vBBBB*/)
    vdst = FETCH(1);
    vsrc1 = FETCH(2);
    ILOGV("|move%s/16 v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE_16) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(3);
OP_END

/* File: c/OP_MOVE_WIDE.c */
HANDLE_OPCODE(OP_MOVE_WIDE /*vA, vB*/)
    /* IMPORTANT: must correctly handle overlapping registers, e.g. both
     * "move-wide v6, v7" and "move-wide v7, v6" */
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|move-wide v%d,v%d %s(v%d=0x%08llx)", vdst, vsrc1,
        kSpacing+5, vdst, GET_REGISTER_WIDE(vsrc1));
    SET_REGISTER_WIDE(vdst, GET_REGISTER_WIDE(vsrc1));
    FINISH(1);
OP_END

/* File: c/OP_MOVE_WIDE_FROM16.c */
HANDLE_OPCODE(OP_MOVE_WIDE_FROM16 /*vAA, vBBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|move-wide/from16 v%d,v%d  (v%d=0x%08llx)", vdst, vsrc1,
        vdst, GET_REGISTER_WIDE(vsrc1));
    SET_REGISTER_WIDE(vdst, GET_REGISTER_WIDE(vsrc1));
    FINISH(2);
OP_END

/* File: c/OP_MOVE_WIDE_16.c */
HANDLE_OPCODE(OP_MOVE_WIDE_16 /*vAAAA, vBBBB*/)
    vdst = FETCH(1);
    vsrc1 = FETCH(2);
    ILOGV("|move-wide/16 v%d,v%d %s(v%d=0x%08llx)", vdst, vsrc1,
        kSpacing+8, vdst, GET_REGISTER_WIDE(vsrc1));
    SET_REGISTER_WIDE(vdst, GET_REGISTER_WIDE(vsrc1));
    FINISH(3);
OP_END

/* File: c/OP_MOVE_OBJECT.c */
/* File: c/OP_MOVE.c */
HANDLE_OPCODE(OP_MOVE_OBJECT /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|move%s v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(1);
OP_END

//OP_END

/* File: c/OP_MOVE_OBJECT_FROM16.c */
/* File: c/OP_MOVE_FROM16.c */
HANDLE_OPCODE(OP_MOVE_OBJECT_FROM16 /*vAA, vBBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|move%s/from16 v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE_FROM16) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(2);
OP_END

//OP_END

/* File: c/OP_MOVE_OBJECT_16.c */
/* File: c/OP_MOVE_16.c */
HANDLE_OPCODE(OP_MOVE_OBJECT_16 /*vAAAA, vBBBB*/)
    vdst = FETCH(1);
    vsrc1 = FETCH(2);
    ILOGV("|move%s/16 v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE_16) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(3);
OP_END

//OP_END

/* File: c/OP_MOVE_RESULT.c */
HANDLE_OPCODE(OP_MOVE_RESULT /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-result%s v%d %s(v%d=0x%08x)",
         (INST_INST(inst) == OP_MOVE_RESULT) ? "" : "-object",
         vdst, kSpacing+4, vdst,retval.i);
    SET_REGISTER(vdst, retval.i);
    FINISH(1);
OP_END

/* File: c/OP_MOVE_RESULT_WIDE.c */
HANDLE_OPCODE(OP_MOVE_RESULT_WIDE /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-result-wide v%d %s(0x%08llx)", vdst, kSpacing, retval.j);
    SET_REGISTER_WIDE(vdst, retval.j);
    FINISH(1);
OP_END

/* File: c/OP_MOVE_RESULT_OBJECT.c */
/* File: c/OP_MOVE_RESULT.c */
HANDLE_OPCODE(OP_MOVE_RESULT_OBJECT /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-result%s v%d %s(v%d=0x%08x)",
         (INST_INST(inst) == OP_MOVE_RESULT) ? "" : "-object",
         vdst, kSpacing+4, vdst,retval.i);
    SET_REGISTER(vdst, retval.i);
    FINISH(1);
OP_END

//OP_END

/* File: c/OP_MOVE_EXCEPTION.c */
HANDLE_OPCODE(OP_MOVE_EXCEPTION /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-exception v%d", vdst);
    assert(self->exception != NULL);
    SET_REGISTER(vdst, (u4)self->exception);
    dvmClearException(self);
    FINISH(1);
OP_END

/* File: c/OP_RETURN_VOID.c */
HANDLE_OPCODE(OP_RETURN_VOID /**/)
    ILOGV("|return-void");
#ifndef NDEBUG
    retval.j = 0xababababULL;    // placate valgrind
#endif
    GOTO(returnFromMethod);
OP_END

/* File: c/OP_RETURN.c */
HANDLE_OPCODE(OP_RETURN /*vAA*/)
    vsrc1 = INST_AA(inst);
    ILOGV("|return%s v%d",
        (INST_INST(inst) == OP_RETURN) ? "" : "-object", vsrc1);
    retval.i = GET_REGISTER(vsrc1);
    GOTO(returnFromMethod);
OP_END

/* File: c/OP_RETURN_WIDE.c */
HANDLE_OPCODE(OP_RETURN_WIDE /*vAA*/)
    vsrc1 = INST_AA(inst);
    ILOGV("|return-wide v%d", vsrc1);
    retval.j = GET_REGISTER_WIDE(vsrc1);
    GOTO(returnFromMethod);
OP_END

/* File: c/OP_RETURN_OBJECT.c */
/* File: c/OP_RETURN.c */
HANDLE_OPCODE(OP_RETURN_OBJECT /*vAA*/)
    vsrc1 = INST_AA(inst);
    ILOGV("|return%s v%d",
        (INST_INST(inst) == OP_RETURN) ? "" : "-object", vsrc1);
    retval.i = GET_REGISTER(vsrc1);
    GOTO(returnFromMethod);
OP_END

//OP_END

/* File: c/OP_CONST_4.c */
HANDLE_OPCODE(OP_CONST_4 /*vA, #+B*/)
    {
        s4 tmp;

        vdst = INST_A(inst);
        tmp = (s4) (INST_B(inst) << 28) >> 28;  // sign extend 4-bit value
        ILOGV("|const/4 v%d,#0x%02x", vdst, (s4)tmp);
        SET_REGISTER(vdst, tmp);
    }
    FINISH(1);
OP_END

/* File: c/OP_CONST_16.c */
HANDLE_OPCODE(OP_CONST_16 /*vAA, #+BBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const/16 v%d,#0x%04x", vdst, (s2)vsrc1);
    SET_REGISTER(vdst, (s2) vsrc1);
    FINISH(2);
OP_END

/* File: c/OP_CONST.c */
HANDLE_OPCODE(OP_CONST /*vAA, #+BBBBBBBB*/)
    {
        u4 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u4)FETCH(2) << 16;
        ILOGV("|const v%d,#0x%08x", vdst, tmp);
        SET_REGISTER(vdst, tmp);
    }
    FINISH(3);
OP_END

/* File: c/OP_CONST_HIGH16.c */
HANDLE_OPCODE(OP_CONST_HIGH16 /*vAA, #+BBBB0000*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const/high16 v%d,#0x%04x0000", vdst, vsrc1);
    SET_REGISTER(vdst, vsrc1 << 16);
    FINISH(2);
OP_END

/* File: c/OP_CONST_WIDE_16.c */
HANDLE_OPCODE(OP_CONST_WIDE_16 /*vAA, #+BBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const-wide/16 v%d,#0x%04x", vdst, (s2)vsrc1);
    SET_REGISTER_WIDE(vdst, (s2)vsrc1);
    FINISH(2);
OP_END

/* File: c/OP_CONST_WIDE_32.c */
HANDLE_OPCODE(OP_CONST_WIDE_32 /*vAA, #+BBBBBBBB*/)
    {
        u4 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u4)FETCH(2) << 16;
        ILOGV("|const-wide/32 v%d,#0x%08x", vdst, tmp);
        SET_REGISTER_WIDE(vdst, (s4) tmp);
    }
    FINISH(3);
OP_END

/* File: c/OP_CONST_WIDE.c */
HANDLE_OPCODE(OP_CONST_WIDE /*vAA, #+BBBBBBBBBBBBBBBB*/)
    {
        u8 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u8)FETCH(2) << 16;
        tmp |= (u8)FETCH(3) << 32;
        tmp |= (u8)FETCH(4) << 48;
        ILOGV("|const-wide v%d,#0x%08llx", vdst, tmp);
        SET_REGISTER_WIDE(vdst, tmp);
    }
    FINISH(5);
OP_END

/* File: c/OP_CONST_WIDE_HIGH16.c */
HANDLE_OPCODE(OP_CONST_WIDE_HIGH16 /*vAA, #+BBBB000000000000*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const-wide/high16 v%d,#0x%04x000000000000", vdst, vsrc1);
    SET_REGISTER_WIDE(vdst, ((u8) vsrc1) << 48);
    FINISH(2);
OP_END

/* File: c/OP_CONST_STRING.c */
HANDLE_OPCODE(OP_CONST_STRING /*vAA, string@BBBB*/)
    {
        StringObject* strObj;

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|const-string v%d string@0x%04x", vdst, ref);
        strObj = dvmDexGetResolvedString(methodClassDex, ref);
        if (strObj == NULL) {
            EXPORT_PC();
            strObj = dvmResolveString(method->clazz, ref);
            if (strObj == NULL)
                GOTO(exceptionThrown);
        }
        SET_REGISTER(vdst, (u4) strObj);
    }
    FINISH(2);
OP_END

/* File: c/OP_CONST_STRING_JUMBO.c */
HANDLE_OPCODE(OP_CONST_STRING_JUMBO /*vAA, string@BBBBBBBB*/)
    {
        StringObject* strObj;
        u4 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u4)FETCH(2) << 16;
        ILOGV("|const-string/jumbo v%d string@0x%08x", vdst, tmp);
        strObj = dvmDexGetResolvedString(methodClassDex, tmp);
        if (strObj == NULL) {
            EXPORT_PC();
            strObj = dvmResolveString(method->clazz, tmp);
            if (strObj == NULL)
                GOTO(exceptionThrown);
        }
        SET_REGISTER(vdst, (u4) strObj);
    }
    FINISH(3);
OP_END

/* File: c/OP_CONST_CLASS.c */
HANDLE_OPCODE(OP_CONST_CLASS /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|const-class v%d class@0x%04x", vdst, ref);
        clazz = dvmDexGetResolvedClass(methodClassDex, ref);
        if (clazz == NULL) {
            EXPORT_PC();
            clazz = dvmResolveClass(method->clazz, ref, true);
            if (clazz == NULL)
                GOTO(exceptionThrown);
        }
        SET_REGISTER(vdst, (u4) clazz);
    }
    FINISH(2);
OP_END

/* File: c/OP_MONITOR_ENTER.c */
HANDLE_OPCODE(OP_MONITOR_ENTER /*vAA*/)
    {
        Object* obj;

        vsrc1 = INST_AA(inst);
        ILOGV("|monitor-enter v%d %s(0x%08x)",
            vsrc1, kSpacing+6, GET_REGISTER(vsrc1));
        obj = (Object*)GET_REGISTER(vsrc1);
        if (!checkForNullExportPC(obj, fp, pc))
            GOTO(exceptionThrown);
        ILOGV("+ locking %p %s\n", obj, obj->clazz->descriptor);
#ifdef WITH_MONITOR_TRACKING
        EXPORT_PC();        /* need for stack trace */
#endif
        dvmLockObject(self, obj);
#ifdef WITH_DEADLOCK_PREDICTION
        if (dvmCheckException(self))
            GOTO(exceptionThrown);
#endif
    }
    FINISH(1);
OP_END

/* File: c/OP_MONITOR_EXIT.c */
HANDLE_OPCODE(OP_MONITOR_EXIT /*vAA*/)
    {
        Object* obj;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);
        ILOGV("|monitor-exit v%d %s(0x%08x)",
            vsrc1, kSpacing+5, GET_REGISTER(vsrc1));
        obj = (Object*)GET_REGISTER(vsrc1);
        if (!checkForNull(obj)) {
            /*
             * The exception needs to be processed at the *following*
             * instruction, not the current instruction (see the Dalvik
             * spec).  Because we're jumping to an exception handler,
             * we're not actually at risk of skipping an instruction
             * by doing so.
             */
            ADJUST_PC(1);           /* monitor-exit width is 1 */
            GOTO(exceptionThrown);
        }
        ILOGV("+ unlocking %p %s\n", obj, obj->clazz->descriptor);
        if (!dvmUnlockObject(self, obj)) {
            assert(dvmCheckException(self));
            ADJUST_PC(1);
            GOTO(exceptionThrown);
        }
    }
    FINISH(1);
OP_END

/* File: c/OP_CHECK_CAST.c */
HANDLE_OPCODE(OP_CHECK_CAST /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;
        Object* obj;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);
        ref = FETCH(1);         /* class to check against */
        ILOGV("|check-cast v%d,class@0x%04x", vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj != NULL) {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNull(obj))
                GOTO(exceptionThrown);
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                clazz = dvmResolveClass(method->clazz, ref, false);
                if (clazz == NULL)
                    GOTO(exceptionThrown);
            }
            if (!dvmInstanceof(obj->clazz, clazz)) {
                dvmThrowExceptionWithClassMessage(
                    "Ljava/lang/ClassCastException;", obj->clazz->descriptor);
                GOTO(exceptionThrown);
            }
        }
    }
    FINISH(2);
OP_END

/* File: c/OP_INSTANCE_OF.c */
HANDLE_OPCODE(OP_INSTANCE_OF /*vA, vB, class@CCCC*/)
    {
        ClassObject* clazz;
        Object* obj;

        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);   /* object to check */
        ref = FETCH(1);         /* class to check against */
        ILOGV("|instance-of v%d,v%d,class@0x%04x", vdst, vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj == NULL) {
            SET_REGISTER(vdst, 0);
        } else {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNullExportPC(obj, fp, pc))
                GOTO(exceptionThrown);
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                EXPORT_PC();
                clazz = dvmResolveClass(method->clazz, ref, true);
                if (clazz == NULL)
                    GOTO(exceptionThrown);
            }
            SET_REGISTER(vdst, dvmInstanceof(obj->clazz, clazz));
        }
    }
    FINISH(2);
OP_END

/* File: c/OP_ARRAY_LENGTH.c */
HANDLE_OPCODE(OP_ARRAY_LENGTH /*vA, vB*/)
    {
        ArrayObject* arrayObj;

        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        ILOGV("|array-length v%d,v%d  (%p)", vdst, vsrc1, arrayObj);
        if (!checkForNullExportPC((Object*) arrayObj, fp, pc))
            GOTO(exceptionThrown);
        /* verifier guarantees this is an array reference */
        SET_REGISTER(vdst, arrayObj->length);
    }
    FINISH(1);
OP_END

/* File: c/OP_NEW_INSTANCE.c */
HANDLE_OPCODE(OP_NEW_INSTANCE /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;
        Object* newObj;

        EXPORT_PC();

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|new-instance v%d,class@0x%04x", vdst, ref);
        clazz = dvmDexGetResolvedClass(methodClassDex, ref);
        if (clazz == NULL) {
            clazz = dvmResolveClass(method->clazz, ref, false);
            if (clazz == NULL)
                GOTO(exceptionThrown);
        }

        if (!dvmIsClassInitialized(clazz) && !dvmInitClass(clazz))
            GOTO(exceptionThrown);

        /*
         * Note: the verifier can ensure that this never happens, allowing us
         * to remove the check.  However, the spec requires we throw the
         * exception at runtime, not verify time, so the verifier would
         * need to replace the new-instance call with a magic "throw
         * InstantiationError" instruction.
         *
         * Since this relies on the verifier, which is optional, we would
         * also need a "new-instance-quick" instruction to identify instances
         * that don't require the check.
         */
        if (dvmIsInterfaceClass(clazz) || dvmIsAbstractClass(clazz)) {
            dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationError;",
                clazz->descriptor);
            GOTO(exceptionThrown);
        }
        newObj = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
        if (newObj == NULL)
            GOTO(exceptionThrown);
        SET_REGISTER(vdst, (u4) newObj);
    }
    FINISH(2);
OP_END

/* File: c/OP_NEW_ARRAY.c */
HANDLE_OPCODE(OP_NEW_ARRAY /*vA, vB, class@CCCC*/)
    {
        ClassObject* arrayClass;
        ArrayObject* newArray;
        s4 length;

        EXPORT_PC();

        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);       /* length reg */
        ref = FETCH(1);
        ILOGV("|new-array v%d,v%d,class@0x%04x  (%d elements)",
            vdst, vsrc1, ref, (s4) GET_REGISTER(vsrc1));
        length = (s4) GET_REGISTER(vsrc1);
        if (length < 0) {
            dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
            GOTO(exceptionThrown);
        }
        arrayClass = dvmDexGetResolvedClass(methodClassDex, ref);
        if (arrayClass == NULL) {
            arrayClass = dvmResolveClass(method->clazz, ref, false);
            if (arrayClass == NULL)
                GOTO(exceptionThrown);
        }
        /* verifier guarantees this is an array class */
        assert(dvmIsArrayClass(arrayClass));
        assert(dvmIsClassInitialized(arrayClass));

        newArray = dvmAllocArrayByClass(arrayClass, length, ALLOC_DONT_TRACK);
        if (newArray == NULL)
            GOTO(exceptionThrown);
        SET_REGISTER(vdst, (u4) newArray);
    }
    FINISH(2);
OP_END


/* File: c/OP_FILLED_NEW_ARRAY.c */
HANDLE_OPCODE(OP_FILLED_NEW_ARRAY /*vB, {vD, vE, vF, vG, vA}, class@CCCC*/)
    GOTO(filledNewArray, false);
OP_END

/* File: c/OP_FILLED_NEW_ARRAY_RANGE.c */
HANDLE_OPCODE(OP_FILLED_NEW_ARRAY_RANGE /*{vCCCC..v(CCCC+AA-1)}, class@BBBB*/)
    GOTO(filledNewArray, true);
OP_END

/* File: c/OP_FILL_ARRAY_DATA.c */
HANDLE_OPCODE(OP_FILL_ARRAY_DATA)   /*vAA, +BBBBBBBB*/
    {
        const u2* arrayData;
        s4 offset;
        ArrayObject* arrayObj;

        EXPORT_PC();
        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|fill-array-data v%d +0x%04x", vsrc1, offset);
        arrayData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (arrayData < method->insns ||
            arrayData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            dvmThrowException("Ljava/lang/InternalError;", 
                              "bad fill array data");
            GOTO(exceptionThrown);
        }
#endif
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        if (!dvmInterpHandleFillArrayData(arrayObj, arrayData)) {
            GOTO(exceptionThrown);
        }
        FINISH(3);
    }
OP_END

/* File: c/OP_THROW.c */
HANDLE_OPCODE(OP_THROW /*vAA*/)
    {
        Object* obj;

        vsrc1 = INST_AA(inst);
        ILOGV("|throw v%d  (%p)", vsrc1, (void*)GET_REGISTER(vsrc1));
        obj = (Object*) GET_REGISTER(vsrc1);
        if (!checkForNullExportPC(obj, fp, pc)) {
            /* will throw a null pointer exception */
            LOGVV("Bad exception\n");
        } else {
            /* use the requested exception */
            dvmSetException(self, obj);
        }
        GOTO(exceptionThrown);
    }
OP_END

/* File: c/OP_GOTO.c */
HANDLE_OPCODE(OP_GOTO /*+AA*/)
    vdst = INST_AA(inst);
    if ((s1)vdst < 0)
        ILOGV("|goto -0x%02x", -((s1)vdst));
    else
        ILOGV("|goto +0x%02x", ((s1)vdst));
    ILOGV("> branch taken");
    if ((s1)vdst < 0)
        PERIODIC_CHECKS(kInterpEntryInstr, (s1)vdst);
    FINISH((s1)vdst);
OP_END

/* File: c/OP_GOTO_16.c */
HANDLE_OPCODE(OP_GOTO_16 /*+AAAA*/)
    {
        s4 offset = (s2) FETCH(1);          /* sign-extend next code unit */

        if (offset < 0)
            ILOGV("|goto/16 -0x%04x", -offset);
        else
            ILOGV("|goto/16 +0x%04x", offset);
        ILOGV("> branch taken");
        if (offset < 0)
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }
OP_END

/* File: c/OP_GOTO_32.c */
HANDLE_OPCODE(OP_GOTO_32 /*+AAAAAAAA*/)
    {
        s4 offset = FETCH(1);               /* low-order 16 bits */
        offset |= ((s4) FETCH(2)) << 16;    /* high-order 16 bits */

        if (offset < 0)
            ILOGV("|goto/32 -0x%08x", -offset);
        else
            ILOGV("|goto/32 +0x%08x", offset);
        ILOGV("> branch taken");
        if (offset <= 0)    /* allowed to branch to self */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }
OP_END

/* File: c/OP_PACKED_SWITCH.c */
HANDLE_OPCODE(OP_PACKED_SWITCH /*vAA, +BBBB*/)
    {
        const u2* switchData;
        u4 testVal;
        s4 offset;

        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|packed-switch v%d +0x%04x", vsrc1, vsrc2);
        switchData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (switchData < method->insns ||
            switchData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            EXPORT_PC();
            dvmThrowException("Ljava/lang/InternalError;", "bad packed switch");
            GOTO(exceptionThrown);
        }
#endif
        testVal = GET_REGISTER(vsrc1);

        offset = dvmInterpHandlePackedSwitch(switchData, testVal);
        ILOGV("> branch taken (0x%04x)\n", offset);
        if (offset <= 0)  /* uncommon */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }
OP_END

/* File: c/OP_SPARSE_SWITCH.c */
HANDLE_OPCODE(OP_SPARSE_SWITCH /*vAA, +BBBB*/)
    {
        const u2* switchData;
        u4 testVal;
        s4 offset;

        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|sparse-switch v%d +0x%04x", vsrc1, vsrc2);
        switchData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (switchData < method->insns ||
            switchData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            EXPORT_PC();
            dvmThrowException("Ljava/lang/InternalError;", "bad sparse switch");
            GOTO(exceptionThrown);
        }
#endif
        testVal = GET_REGISTER(vsrc1);

        offset = dvmInterpHandleSparseSwitch(switchData, testVal);
        ILOGV("> branch taken (0x%04x)\n", offset);
        if (offset <= 0)  /* uncommon */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }
OP_END

/* File: c/OP_CMPL_FLOAT.c */
HANDLE_OP_CMPX(OP_CMPL_FLOAT, "l-float", float, _FLOAT, -1)
OP_END

/* File: c/OP_CMPG_FLOAT.c */
HANDLE_OP_CMPX(OP_CMPG_FLOAT, "g-float", float, _FLOAT, 1)
OP_END

/* File: c/OP_CMPL_DOUBLE.c */
HANDLE_OP_CMPX(OP_CMPL_DOUBLE, "l-double", double, _DOUBLE, -1)
OP_END

/* File: c/OP_CMPG_DOUBLE.c */
HANDLE_OP_CMPX(OP_CMPG_DOUBLE, "g-double", double, _DOUBLE, 1)
OP_END

/* File: c/OP_CMP_LONG.c */
HANDLE_OP_CMPX(OP_CMP_LONG, "-long", s8, _WIDE, 0)
OP_END

/* File: c/OP_IF_EQ.c */
HANDLE_OP_IF_XX(OP_IF_EQ, "eq", ==)
OP_END

/* File: c/OP_IF_NE.c */
HANDLE_OP_IF_XX(OP_IF_NE, "ne", !=)
OP_END

/* File: c/OP_IF_LT.c */
HANDLE_OP_IF_XX(OP_IF_LT, "lt", <)
OP_END

/* File: c/OP_IF_GE.c */
HANDLE_OP_IF_XX(OP_IF_GE, "ge", >=)
OP_END

/* File: c/OP_IF_GT.c */
HANDLE_OP_IF_XX(OP_IF_GT, "gt", >)
OP_END

/* File: c/OP_IF_LE.c */
HANDLE_OP_IF_XX(OP_IF_LE, "le", <=)
OP_END

/* File: c/OP_IF_EQZ.c */
HANDLE_OP_IF_XXZ(OP_IF_EQZ, "eqz", ==)
OP_END

/* File: c/OP_IF_NEZ.c */
HANDLE_OP_IF_XXZ(OP_IF_NEZ, "nez", !=)
OP_END

/* File: c/OP_IF_LTZ.c */
HANDLE_OP_IF_XXZ(OP_IF_LTZ, "ltz", <)
OP_END

/* File: c/OP_IF_GEZ.c */
HANDLE_OP_IF_XXZ(OP_IF_GEZ, "gez", >=)
OP_END

/* File: c/OP_IF_GTZ.c */
HANDLE_OP_IF_XXZ(OP_IF_GTZ, "gtz", >)
OP_END

/* File: c/OP_IF_LEZ.c */
HANDLE_OP_IF_XXZ(OP_IF_LEZ, "lez", <=)
OP_END

/* File: c/OP_UNUSED_3E.c */
HANDLE_OPCODE(OP_UNUSED_3E)
OP_END

/* File: c/OP_UNUSED_3F.c */
HANDLE_OPCODE(OP_UNUSED_3F)
OP_END

/* File: c/OP_UNUSED_40.c */
HANDLE_OPCODE(OP_UNUSED_40)
OP_END

/* File: c/OP_UNUSED_41.c */
HANDLE_OPCODE(OP_UNUSED_41)
OP_END

/* File: c/OP_UNUSED_42.c */
HANDLE_OPCODE(OP_UNUSED_42)
OP_END

/* File: c/OP_UNUSED_43.c */
HANDLE_OPCODE(OP_UNUSED_43)
OP_END

/* File: c/OP_AGET.c */
HANDLE_OP_AGET(OP_AGET, "", u4, )
OP_END

/* File: c/OP_AGET_WIDE.c */
HANDLE_OP_AGET(OP_AGET_WIDE, "-wide", s8, _WIDE)
OP_END

/* File: c/OP_AGET_OBJECT.c */
HANDLE_OP_AGET(OP_AGET_OBJECT, "-object", u4, )
OP_END

/* File: c/OP_AGET_BOOLEAN.c */
HANDLE_OP_AGET(OP_AGET_BOOLEAN, "-boolean", u1, )
OP_END

/* File: c/OP_AGET_BYTE.c */
HANDLE_OP_AGET(OP_AGET_BYTE, "-byte", s1, )
OP_END

/* File: c/OP_AGET_CHAR.c */
HANDLE_OP_AGET(OP_AGET_CHAR, "-char", u2, )
OP_END

/* File: c/OP_AGET_SHORT.c */
HANDLE_OP_AGET(OP_AGET_SHORT, "-short", s2, )
OP_END

/* File: c/OP_APUT.c */
HANDLE_OP_APUT(OP_APUT, "", u4, )
OP_END

/* File: c/OP_APUT_WIDE.c */
HANDLE_OP_APUT(OP_APUT_WIDE, "-wide", s8, _WIDE)
OP_END

/* File: c/OP_APUT_OBJECT.c */
HANDLE_OPCODE(OP_APUT_OBJECT /*vAA, vBB, vCC*/)
    {
        ArrayObject* arrayObj;
        Object* obj;
        u2 arrayInfo;
        EXPORT_PC();
        vdst = INST_AA(inst);       /* AA: source value */
        arrayInfo = FETCH(1);
        vsrc1 = arrayInfo & 0xff;   /* BB: array ptr */
        vsrc2 = arrayInfo >> 8;     /* CC: index */
        ILOGV("|aput%s v%d,v%d,v%d", "-object", vdst, vsrc1, vsrc2);
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        if (!checkForNull((Object*) arrayObj))
            GOTO(exceptionThrown);
        if (GET_REGISTER(vsrc2) >= arrayObj->length) {
            dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;",
                NULL);
            GOTO(exceptionThrown);
        }
        obj = (Object*) GET_REGISTER(vdst);
        if (obj != NULL) {
            if (!checkForNull(obj))
                GOTO(exceptionThrown);
            if (!dvmCanPutArrayElement(obj->clazz, arrayObj->obj.clazz)) {
                LOGV("Can't put a '%s'(%p) into array type='%s'(%p)\n",
                    obj->clazz->descriptor, obj,
                    arrayObj->obj.clazz->descriptor, arrayObj);
                //dvmDumpClass(obj->clazz);
                //dvmDumpClass(arrayObj->obj.clazz);
                dvmThrowException("Ljava/lang/ArrayStoreException;", NULL);
                GOTO(exceptionThrown);
            }
        }
        ILOGV("+ APUT[%d]=0x%08x", GET_REGISTER(vsrc2), GET_REGISTER(vdst));
        ((u4*) arrayObj->contents)[GET_REGISTER(vsrc2)] =
            GET_REGISTER(vdst);
    }
    FINISH(2);
OP_END

/* File: c/OP_APUT_BOOLEAN.c */
HANDLE_OP_APUT(OP_APUT_BOOLEAN, "-boolean", u1, )
OP_END

/* File: c/OP_APUT_BYTE.c */
HANDLE_OP_APUT(OP_APUT_BYTE, "-byte", s1, )
OP_END

/* File: c/OP_APUT_CHAR.c */
HANDLE_OP_APUT(OP_APUT_CHAR, "-char", u2, )
OP_END

/* File: c/OP_APUT_SHORT.c */
HANDLE_OP_APUT(OP_APUT_SHORT, "-short", s2, )
OP_END

/* File: c/OP_IGET.c */
HANDLE_IGET_X(OP_IGET,                  "", Int, )
OP_END

/* File: c/OP_IGET_WIDE.c */
HANDLE_IGET_X(OP_IGET_WIDE,             "-wide", Long, _WIDE)
OP_END

/* File: c/OP_IGET_OBJECT.c */
HANDLE_IGET_X(OP_IGET_OBJECT,           "-object", Object, _AS_OBJECT)
OP_END

/* File: c/OP_IGET_BOOLEAN.c */
HANDLE_IGET_X(OP_IGET_BOOLEAN,          "", Int, )
OP_END

/* File: c/OP_IGET_BYTE.c */
HANDLE_IGET_X(OP_IGET_BYTE,             "", Int, )
OP_END

/* File: c/OP_IGET_CHAR.c */
HANDLE_IGET_X(OP_IGET_CHAR,             "", Int, )
OP_END

/* File: c/OP_IGET_SHORT.c */
HANDLE_IGET_X(OP_IGET_SHORT,            "", Int, )
OP_END

/* File: c/OP_IPUT.c */
HANDLE_IPUT_X(OP_IPUT,                  "", Int, )
OP_END

/* File: c/OP_IPUT_WIDE.c */
HANDLE_IPUT_X(OP_IPUT_WIDE,             "-wide", Long, _WIDE)
OP_END

/* File: c/OP_IPUT_OBJECT.c */
HANDLE_IPUT_X(OP_IPUT_OBJECT,           "-object", Object, _AS_OBJECT)
OP_END

/* File: c/OP_IPUT_BOOLEAN.c */
HANDLE_IPUT_X(OP_IPUT_BOOLEAN,          "", Int, )
OP_END

/* File: c/OP_IPUT_BYTE.c */
HANDLE_IPUT_X(OP_IPUT_BYTE,             "", Int, )
OP_END

/* File: c/OP_IPUT_CHAR.c */
HANDLE_IPUT_X(OP_IPUT_CHAR,             "", Int, )
OP_END

/* File: c/OP_IPUT_SHORT.c */
HANDLE_IPUT_X(OP_IPUT_SHORT,            "", Int, )
OP_END

/* File: c/OP_SGET.c */
HANDLE_SGET_X(OP_SGET,                  "", Int, )
OP_END

/* File: c/OP_SGET_WIDE.c */
HANDLE_SGET_X(OP_SGET_WIDE,             "-wide", Long, _WIDE)
OP_END

/* File: c/OP_SGET_OBJECT.c */
HANDLE_SGET_X(OP_SGET_OBJECT,           "-object", Object, _AS_OBJECT)
OP_END

/* File: c/OP_SGET_BOOLEAN.c */
HANDLE_SGET_X(OP_SGET_BOOLEAN,          "", Int, )
OP_END

/* File: c/OP_SGET_BYTE.c */
HANDLE_SGET_X(OP_SGET_BYTE,             "", Int, )
OP_END

/* File: c/OP_SGET_CHAR.c */
HANDLE_SGET_X(OP_SGET_CHAR,             "", Int, )
OP_END

/* File: c/OP_SGET_SHORT.c */
HANDLE_SGET_X(OP_SGET_SHORT,            "", Int, )
OP_END

/* File: c/OP_SPUT.c */
HANDLE_SPUT_X(OP_SPUT,                  "", Int, )
OP_END

/* File: c/OP_SPUT_WIDE.c */
HANDLE_SPUT_X(OP_SPUT_WIDE,             "-wide", Long, _WIDE)
OP_END

/* File: c/OP_SPUT_OBJECT.c */
HANDLE_SPUT_X(OP_SPUT_OBJECT,           "-object", Object, _AS_OBJECT)
OP_END

/* File: c/OP_SPUT_BOOLEAN.c */
HANDLE_SPUT_X(OP_SPUT_BOOLEAN,          "", Int, )
OP_END

/* File: c/OP_SPUT_BYTE.c */
HANDLE_SPUT_X(OP_SPUT_BYTE,             "", Int, )
OP_END

/* File: c/OP_SPUT_CHAR.c */
HANDLE_SPUT_X(OP_SPUT_CHAR,             "", Int, )
OP_END

/* File: c/OP_SPUT_SHORT.c */
HANDLE_SPUT_X(OP_SPUT_SHORT,            "", Int, )
OP_END

/* File: c/OP_INVOKE_VIRTUAL.c */
HANDLE_OPCODE(OP_INVOKE_VIRTUAL /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeVirtual, false);
OP_END

/* File: c/OP_INVOKE_SUPER.c */
HANDLE_OPCODE(OP_INVOKE_SUPER /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeSuper, false);
OP_END

/* File: c/OP_INVOKE_DIRECT.c */
HANDLE_OPCODE(OP_INVOKE_DIRECT /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeDirect, false);
OP_END

/* File: c/OP_INVOKE_STATIC.c */
HANDLE_OPCODE(OP_INVOKE_STATIC /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeStatic, false);
OP_END

/* File: c/OP_INVOKE_INTERFACE.c */
HANDLE_OPCODE(OP_INVOKE_INTERFACE /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeInterface, false);
OP_END

/* File: c/OP_UNUSED_73.c */
HANDLE_OPCODE(OP_UNUSED_73)
OP_END

/* File: c/OP_INVOKE_VIRTUAL_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_VIRTUAL_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeVirtual, true);
OP_END

/* File: c/OP_INVOKE_SUPER_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_SUPER_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeSuper, true);
OP_END

/* File: c/OP_INVOKE_DIRECT_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_DIRECT_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeDirect, true);
OP_END

/* File: c/OP_INVOKE_STATIC_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_STATIC_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeStatic, true);
OP_END

/* File: c/OP_INVOKE_INTERFACE_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_INTERFACE_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeInterface, true);
OP_END

/* File: c/OP_UNUSED_79.c */
HANDLE_OPCODE(OP_UNUSED_79)
OP_END

/* File: c/OP_UNUSED_7A.c */
HANDLE_OPCODE(OP_UNUSED_7A)
OP_END

/* File: c/OP_NEG_INT.c */
HANDLE_UNOP(OP_NEG_INT, "neg-int", -, , )
OP_END

/* File: c/OP_NOT_INT.c */
HANDLE_UNOP(OP_NOT_INT, "not-int", , ^ 0xffffffff, )
OP_END

/* File: c/OP_NEG_LONG.c */
HANDLE_UNOP(OP_NEG_LONG, "neg-long", -, , _WIDE)
OP_END

/* File: c/OP_NOT_LONG.c */
HANDLE_UNOP(OP_NOT_LONG, "not-long", , & 0xffffffffffffffffULL, _WIDE)
OP_END

/* File: c/OP_NEG_FLOAT.c */
HANDLE_UNOP(OP_NEG_FLOAT, "neg-float", -, , _FLOAT)
OP_END

/* File: c/OP_NEG_DOUBLE.c */
HANDLE_UNOP(OP_NEG_DOUBLE, "neg-double", -, , _DOUBLE)
OP_END

/* File: c/OP_INT_TO_LONG.c */
HANDLE_NUMCONV(OP_INT_TO_LONG,          "int-to-long", _INT, _WIDE)
OP_END

/* File: c/OP_INT_TO_FLOAT.c */
HANDLE_NUMCONV(OP_INT_TO_FLOAT,         "int-to-float", _INT, _FLOAT)
OP_END

/* File: c/OP_INT_TO_DOUBLE.c */
HANDLE_NUMCONV(OP_INT_TO_DOUBLE,        "int-to-double", _INT, _DOUBLE)
OP_END

/* File: c/OP_LONG_TO_INT.c */
HANDLE_NUMCONV(OP_LONG_TO_INT,          "long-to-int", _WIDE, _INT)
OP_END

/* File: c/OP_LONG_TO_FLOAT.c */
HANDLE_NUMCONV(OP_LONG_TO_FLOAT,        "long-to-float", _WIDE, _FLOAT)
OP_END

/* File: c/OP_LONG_TO_DOUBLE.c */
HANDLE_NUMCONV(OP_LONG_TO_DOUBLE,       "long-to-double", _WIDE, _DOUBLE)
OP_END

/* File: c/OP_FLOAT_TO_INT.c */
HANDLE_FLOAT_TO_INT(OP_FLOAT_TO_INT,    "float-to-int",
    float, _FLOAT, s4, _INT)
OP_END

/* File: c/OP_FLOAT_TO_LONG.c */
HANDLE_FLOAT_TO_INT(OP_FLOAT_TO_LONG,   "float-to-long",
    float, _FLOAT, s8, _WIDE)
OP_END

/* File: c/OP_FLOAT_TO_DOUBLE.c */
HANDLE_NUMCONV(OP_FLOAT_TO_DOUBLE,      "float-to-double", _FLOAT, _DOUBLE)
OP_END

/* File: c/OP_DOUBLE_TO_INT.c */
HANDLE_FLOAT_TO_INT(OP_DOUBLE_TO_INT,   "double-to-int",
    double, _DOUBLE, s4, _INT)
OP_END

/* File: c/OP_DOUBLE_TO_LONG.c */
HANDLE_FLOAT_TO_INT(OP_DOUBLE_TO_LONG,  "double-to-long",
    double, _DOUBLE, s8, _WIDE)
OP_END

/* File: c/OP_DOUBLE_TO_FLOAT.c */
HANDLE_NUMCONV(OP_DOUBLE_TO_FLOAT,      "double-to-float", _DOUBLE, _FLOAT)
OP_END

/* File: c/OP_INT_TO_BYTE.c */
HANDLE_INT_TO_SMALL(OP_INT_TO_BYTE,     "byte", s1)
OP_END

/* File: c/OP_INT_TO_CHAR.c */
HANDLE_INT_TO_SMALL(OP_INT_TO_CHAR,     "char", u2)
OP_END

/* File: c/OP_INT_TO_SHORT.c */
HANDLE_INT_TO_SMALL(OP_INT_TO_SHORT,    "short", s2)    /* want sign bit */
OP_END

/* File: c/OP_ADD_INT.c */
HANDLE_OP_X_INT(OP_ADD_INT, "add", +, false)
OP_END

/* File: c/OP_SUB_INT.c */
HANDLE_OP_X_INT(OP_SUB_INT, "sub", -, false)
OP_END

/* File: c/OP_MUL_INT.c */
HANDLE_OP_X_INT(OP_MUL_INT, "mul", *, false)
OP_END

/* File: c/OP_DIV_INT.c */
HANDLE_OP_X_INT(OP_DIV_INT, "div", /, true)
OP_END

/* File: c/OP_REM_INT.c */
HANDLE_OP_X_INT(OP_REM_INT, "rem", %, true)
OP_END

/* File: c/OP_AND_INT.c */
HANDLE_OP_X_INT(OP_AND_INT, "and", &, false)
OP_END

/* File: c/OP_OR_INT.c */
HANDLE_OP_X_INT(OP_OR_INT,  "or",  |, false)
OP_END

/* File: c/OP_XOR_INT.c */
HANDLE_OP_X_INT(OP_XOR_INT, "xor", ^, false)
OP_END

/* File: c/OP_SHL_INT.c */
HANDLE_OP_SHX_INT(OP_SHL_INT, "shl", (s4), <<)
OP_END

/* File: c/OP_SHR_INT.c */
HANDLE_OP_SHX_INT(OP_SHR_INT, "shr", (s4), >>)
OP_END

/* File: c/OP_USHR_INT.c */
HANDLE_OP_SHX_INT(OP_USHR_INT, "ushr", (u4), >>)
OP_END

/* File: c/OP_ADD_LONG.c */
HANDLE_OP_X_LONG(OP_ADD_LONG, "add", +, false)
OP_END

/* File: c/OP_SUB_LONG.c */
HANDLE_OP_X_LONG(OP_SUB_LONG, "sub", -, false)
OP_END

/* File: c/OP_MUL_LONG.c */
HANDLE_OP_X_LONG(OP_MUL_LONG, "mul", *, false)
OP_END

/* File: c/OP_DIV_LONG.c */
HANDLE_OP_X_LONG(OP_DIV_LONG, "div", /, true)
OP_END

/* File: c/OP_REM_LONG.c */
HANDLE_OP_X_LONG(OP_REM_LONG, "rem", %, true)
OP_END

/* File: c/OP_AND_LONG.c */
HANDLE_OP_X_LONG(OP_AND_LONG, "and", &, false)
OP_END

/* File: c/OP_OR_LONG.c */
HANDLE_OP_X_LONG(OP_OR_LONG,  "or", |, false)
OP_END

/* File: c/OP_XOR_LONG.c */
HANDLE_OP_X_LONG(OP_XOR_LONG, "xor", ^, false)
OP_END

/* File: c/OP_SHL_LONG.c */
HANDLE_OP_SHX_LONG(OP_SHL_LONG, "shl", (s8), <<)
OP_END

/* File: c/OP_SHR_LONG.c */
HANDLE_OP_SHX_LONG(OP_SHR_LONG, "shr", (s8), >>)
OP_END

/* File: c/OP_USHR_LONG.c */
HANDLE_OP_SHX_LONG(OP_USHR_LONG, "ushr", (u8), >>)
OP_END

/* File: c/OP_ADD_FLOAT.c */
HANDLE_OP_X_FLOAT(OP_ADD_FLOAT, "add", +)
OP_END

/* File: c/OP_SUB_FLOAT.c */
HANDLE_OP_X_FLOAT(OP_SUB_FLOAT, "sub", -)
OP_END

/* File: c/OP_MUL_FLOAT.c */
HANDLE_OP_X_FLOAT(OP_MUL_FLOAT, "mul", *)
OP_END

/* File: c/OP_DIV_FLOAT.c */
HANDLE_OP_X_FLOAT(OP_DIV_FLOAT, "div", /)
OP_END

/* File: c/OP_REM_FLOAT.c */
HANDLE_OPCODE(OP_REM_FLOAT /*vAA, vBB, vCC*/)
    {
        u2 srcRegs;
        vdst = INST_AA(inst);
        srcRegs = FETCH(1);
        vsrc1 = srcRegs & 0xff;
        vsrc2 = srcRegs >> 8;
        ILOGV("|%s-float v%d,v%d,v%d", "mod", vdst, vsrc1, vsrc2);
        SET_REGISTER_FLOAT(vdst,
            fmodf(GET_REGISTER_FLOAT(vsrc1), GET_REGISTER_FLOAT(vsrc2)));
    }
    FINISH(2);
OP_END

/* File: c/OP_ADD_DOUBLE.c */
HANDLE_OP_X_DOUBLE(OP_ADD_DOUBLE, "add", +)
OP_END

/* File: c/OP_SUB_DOUBLE.c */
HANDLE_OP_X_DOUBLE(OP_SUB_DOUBLE, "sub", -)
OP_END

/* File: c/OP_MUL_DOUBLE.c */
HANDLE_OP_X_DOUBLE(OP_MUL_DOUBLE, "mul", *)
OP_END

/* File: c/OP_DIV_DOUBLE.c */
HANDLE_OP_X_DOUBLE(OP_DIV_DOUBLE, "div", /)
OP_END

/* File: c/OP_REM_DOUBLE.c */
HANDLE_OPCODE(OP_REM_DOUBLE /*vAA, vBB, vCC*/)
    {
        u2 srcRegs;
        vdst = INST_AA(inst);
        srcRegs = FETCH(1);
        vsrc1 = srcRegs & 0xff;
        vsrc2 = srcRegs >> 8;
        ILOGV("|%s-double v%d,v%d,v%d", "mod", vdst, vsrc1, vsrc2);
        SET_REGISTER_DOUBLE(vdst,
            fmod(GET_REGISTER_DOUBLE(vsrc1), GET_REGISTER_DOUBLE(vsrc2)));
    }
    FINISH(2);
OP_END

/* File: c/OP_ADD_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_ADD_INT_2ADDR, "add", +, false)
OP_END

/* File: c/OP_SUB_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_SUB_INT_2ADDR, "sub", -, false)
OP_END

/* File: c/OP_MUL_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_MUL_INT_2ADDR, "mul", *, false)
OP_END

/* File: c/OP_DIV_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_DIV_INT_2ADDR, "div", /, true)
OP_END

/* File: c/OP_REM_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_REM_INT_2ADDR, "rem", %, true)
OP_END

/* File: c/OP_AND_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_AND_INT_2ADDR, "and", &, false)
OP_END

/* File: c/OP_OR_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_OR_INT_2ADDR,  "or", |, false)
OP_END

/* File: c/OP_XOR_INT_2ADDR.c */
HANDLE_OP_X_INT_2ADDR(OP_XOR_INT_2ADDR, "xor", ^, false)
OP_END

/* File: c/OP_SHL_INT_2ADDR.c */
HANDLE_OP_SHX_INT_2ADDR(OP_SHL_INT_2ADDR, "shl", (s4), <<)
OP_END

/* File: c/OP_SHR_INT_2ADDR.c */
HANDLE_OP_SHX_INT_2ADDR(OP_SHR_INT_2ADDR, "shr", (s4), >>)
OP_END

/* File: c/OP_USHR_INT_2ADDR.c */
HANDLE_OP_SHX_INT_2ADDR(OP_USHR_INT_2ADDR, "ushr", (u4), >>)
OP_END

/* File: c/OP_ADD_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_ADD_LONG_2ADDR, "add", +, false)
OP_END

/* File: c/OP_SUB_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_SUB_LONG_2ADDR, "sub", -, false)
OP_END

/* File: c/OP_MUL_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_MUL_LONG_2ADDR, "mul", *, false)
OP_END

/* File: c/OP_DIV_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_DIV_LONG_2ADDR, "div", /, true)
OP_END

/* File: c/OP_REM_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_REM_LONG_2ADDR, "rem", %, true)
OP_END

/* File: c/OP_AND_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_AND_LONG_2ADDR, "and", &, false)
OP_END

/* File: c/OP_OR_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_OR_LONG_2ADDR,  "or", |, false)
OP_END

/* File: c/OP_XOR_LONG_2ADDR.c */
HANDLE_OP_X_LONG_2ADDR(OP_XOR_LONG_2ADDR, "xor", ^, false)
OP_END

/* File: c/OP_SHL_LONG_2ADDR.c */
HANDLE_OP_SHX_LONG_2ADDR(OP_SHL_LONG_2ADDR, "shl", (s8), <<)
OP_END

/* File: c/OP_SHR_LONG_2ADDR.c */
HANDLE_OP_SHX_LONG_2ADDR(OP_SHR_LONG_2ADDR, "shr", (s8), >>)
OP_END

/* File: c/OP_USHR_LONG_2ADDR.c */
HANDLE_OP_SHX_LONG_2ADDR(OP_USHR_LONG_2ADDR, "ushr", (u8), >>)
OP_END

/* File: c/OP_ADD_FLOAT_2ADDR.c */
HANDLE_OP_X_FLOAT_2ADDR(OP_ADD_FLOAT_2ADDR, "add", +)
OP_END

/* File: c/OP_SUB_FLOAT_2ADDR.c */
HANDLE_OP_X_FLOAT_2ADDR(OP_SUB_FLOAT_2ADDR, "sub", -)
OP_END

/* File: c/OP_MUL_FLOAT_2ADDR.c */
HANDLE_OP_X_FLOAT_2ADDR(OP_MUL_FLOAT_2ADDR, "mul", *)
OP_END

/* File: c/OP_DIV_FLOAT_2ADDR.c */
HANDLE_OP_X_FLOAT_2ADDR(OP_DIV_FLOAT_2ADDR, "div", /)
OP_END

/* File: c/OP_REM_FLOAT_2ADDR.c */
HANDLE_OPCODE(OP_REM_FLOAT_2ADDR /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|%s-float-2addr v%d,v%d", "mod", vdst, vsrc1);
    SET_REGISTER_FLOAT(vdst,
        fmodf(GET_REGISTER_FLOAT(vdst), GET_REGISTER_FLOAT(vsrc1)));
    FINISH(1);
OP_END

/* File: c/OP_ADD_DOUBLE_2ADDR.c */
HANDLE_OP_X_DOUBLE_2ADDR(OP_ADD_DOUBLE_2ADDR, "add", +)
OP_END

/* File: c/OP_SUB_DOUBLE_2ADDR.c */
HANDLE_OP_X_DOUBLE_2ADDR(OP_SUB_DOUBLE_2ADDR, "sub", -)
OP_END

/* File: c/OP_MUL_DOUBLE_2ADDR.c */
HANDLE_OP_X_DOUBLE_2ADDR(OP_MUL_DOUBLE_2ADDR, "mul", *)
OP_END

/* File: c/OP_DIV_DOUBLE_2ADDR.c */
HANDLE_OP_X_DOUBLE_2ADDR(OP_DIV_DOUBLE_2ADDR, "div", /)
OP_END

/* File: c/OP_REM_DOUBLE_2ADDR.c */
HANDLE_OPCODE(OP_REM_DOUBLE_2ADDR /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|%s-double-2addr v%d,v%d", "mod", vdst, vsrc1);
    SET_REGISTER_DOUBLE(vdst,
        fmod(GET_REGISTER_DOUBLE(vdst), GET_REGISTER_DOUBLE(vsrc1)));
    FINISH(1);
OP_END

/* File: c/OP_ADD_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_ADD_INT_LIT16, "add", (s4), +, false)
OP_END

/* File: c/OP_RSUB_INT.c */
HANDLE_OPCODE(OP_RSUB_INT /*vA, vB, #+CCCC*/)
    {
        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);
        vsrc2 = FETCH(1);
        ILOGV("|rsub-int v%d,v%d,#+0x%04x", vdst, vsrc1, vsrc2);
        SET_REGISTER(vdst, (s2) vsrc2 - (s4) GET_REGISTER(vsrc1));
    }
    FINISH(2);
OP_END

/* File: c/OP_MUL_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_MUL_INT_LIT16, "mul", (s4), *, false)
OP_END

/* File: c/OP_DIV_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_DIV_INT_LIT16, "div", (s4), /, true)
OP_END

/* File: c/OP_REM_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_REM_INT_LIT16, "rem", (s4), %, true)
OP_END

/* File: c/OP_AND_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_AND_INT_LIT16, "and", (s4), &, false)
OP_END

/* File: c/OP_OR_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_OR_INT_LIT16,  "or",  (s4), |, false)
OP_END

/* File: c/OP_XOR_INT_LIT16.c */
HANDLE_OP_X_INT_LIT16(OP_XOR_INT_LIT16, "xor", (s4), ^, false)
OP_END

/* File: c/OP_ADD_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_ADD_INT_LIT8,   "add", +, false)
OP_END

/* File: c/OP_RSUB_INT_LIT8.c */
HANDLE_OPCODE(OP_RSUB_INT_LIT8 /*vAA, vBB, #+CC*/)
    {
        u2 litInfo;
        vdst = INST_AA(inst);
        litInfo = FETCH(1);
        vsrc1 = litInfo & 0xff;
        vsrc2 = litInfo >> 8;
        ILOGV("|%s-int/lit8 v%d,v%d,#+0x%02x", "rsub", vdst, vsrc1, vsrc2);
        SET_REGISTER(vdst, (s1) vsrc2 - (s4) GET_REGISTER(vsrc1));
    }
    FINISH(2);
OP_END

/* File: c/OP_MUL_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_MUL_INT_LIT8,   "mul", *, false)
OP_END

/* File: c/OP_DIV_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_DIV_INT_LIT8,   "div", /, true)
OP_END

/* File: c/OP_REM_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_REM_INT_LIT8,   "rem", %, true)
OP_END

/* File: c/OP_AND_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_AND_INT_LIT8,   "and", &, false)
OP_END

/* File: c/OP_OR_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_OR_INT_LIT8,    "or",  |, false)
OP_END

/* File: c/OP_XOR_INT_LIT8.c */
HANDLE_OP_X_INT_LIT8(OP_XOR_INT_LIT8,   "xor", ^, false)
OP_END

/* File: c/OP_SHL_INT_LIT8.c */
HANDLE_OP_SHX_INT_LIT8(OP_SHL_INT_LIT8,   "shl", (s4), <<)
OP_END

/* File: c/OP_SHR_INT_LIT8.c */
HANDLE_OP_SHX_INT_LIT8(OP_SHR_INT_LIT8,   "shr", (s4), >>)
OP_END

/* File: c/OP_USHR_INT_LIT8.c */
HANDLE_OP_SHX_INT_LIT8(OP_USHR_INT_LIT8,  "ushr", (u4), >>)
OP_END

/* File: c/OP_UNUSED_E3.c */
HANDLE_OPCODE(OP_UNUSED_E3)
OP_END

/* File: c/OP_UNUSED_E4.c */
HANDLE_OPCODE(OP_UNUSED_E4)
OP_END

/* File: c/OP_UNUSED_E5.c */
HANDLE_OPCODE(OP_UNUSED_E5)
OP_END

/* File: c/OP_UNUSED_E6.c */
HANDLE_OPCODE(OP_UNUSED_E6)
OP_END

/* File: c/OP_UNUSED_E7.c */
HANDLE_OPCODE(OP_UNUSED_E7)
OP_END

/* File: c/OP_UNUSED_E8.c */
HANDLE_OPCODE(OP_UNUSED_E8)
OP_END

/* File: c/OP_UNUSED_E9.c */
HANDLE_OPCODE(OP_UNUSED_E9)
OP_END

/* File: c/OP_UNUSED_EA.c */
HANDLE_OPCODE(OP_UNUSED_EA)
OP_END

/* File: c/OP_UNUSED_EB.c */
HANDLE_OPCODE(OP_UNUSED_EB)
OP_END

/* File: c/OP_UNUSED_EC.c */
HANDLE_OPCODE(OP_UNUSED_EC)
OP_END

/* File: c/OP_UNUSED_ED.c */
HANDLE_OPCODE(OP_UNUSED_ED)
OP_END

/* File: c/OP_EXECUTE_INLINE.c */
HANDLE_OPCODE(OP_EXECUTE_INLINE /*vB, {vD, vE, vF, vG}, inline@CCCC*/)
    {
        /*
         * This has the same form as other method calls, but we ignore
         * the 5th argument (vA).  This is chiefly because the first four
         * arguments to a function on ARM are in registers.
         *
         * We only set the arguments that are actually used, leaving
         * the rest uninitialized.  We're assuming that, if the method
         * needs them, they'll be specified in the call.
         *
         * This annoys gcc when optimizations are enabled, causing a
         * "may be used uninitialized" warning.  We can quiet the warnings
         * for a slight penalty (5%: 373ns vs. 393ns on empty method).  Note
         * that valgrind is perfectly happy with this arrangement, because
         * the uninitialiezd values are never actually used.
         */
        u4 arg0, arg1, arg2, arg3;
        //arg0 = arg1 = arg2 = arg3 = 0;

        EXPORT_PC();

        vsrc1 = INST_B(inst);       /* #of args */
        ref = FETCH(1);             /* inline call "ref" */
        vdst = FETCH(2);            /* 0-4 register indices */
        ILOGV("|execute-inline args=%d @%d {regs=0x%04x}",
            vsrc1, ref, vdst);

        assert((vdst >> 16) == 0);  // 16-bit type -or- high 16 bits clear
        assert(vsrc1 <= 4);

        switch (vsrc1) {
        case 4:
            arg3 = GET_REGISTER(vdst >> 12);
            /* fall through */
        case 3:
            arg2 = GET_REGISTER((vdst & 0x0f00) >> 8);
            /* fall through */
        case 2:
            arg1 = GET_REGISTER((vdst & 0x00f0) >> 4);
            /* fall through */
        case 1:
            arg0 = GET_REGISTER(vdst & 0x0f);
            /* fall through */
        default:        // case 0
            ;
        }

#if INTERP_TYPE == INTERP_DBG
        if (!dvmPerformInlineOp4Dbg(arg0, arg1, arg2, arg3, &retval, ref))
            GOTO(exceptionThrown);
#else
        if (!dvmPerformInlineOp4Std(arg0, arg1, arg2, arg3, &retval, ref))
            GOTO(exceptionThrown);
#endif
    }
    FINISH(3);
OP_END

/* File: c/OP_UNUSED_EF.c */
HANDLE_OPCODE(OP_UNUSED_EF)
OP_END

/* File: c/OP_INVOKE_DIRECT_EMPTY.c */
HANDLE_OPCODE(OP_INVOKE_DIRECT_EMPTY /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    //LOGI("Ignoring empty\n");
    FINISH(3);
OP_END

/* File: c/OP_UNUSED_F1.c */
HANDLE_OPCODE(OP_UNUSED_F1)
OP_END

/* File: c/OP_IGET_QUICK.c */
HANDLE_IGET_X_QUICK(OP_IGET_QUICK,          "", Int, )
OP_END

/* File: c/OP_IGET_WIDE_QUICK.c */
HANDLE_IGET_X_QUICK(OP_IGET_WIDE_QUICK,     "-wide", Long, _WIDE)
OP_END

/* File: c/OP_IGET_OBJECT_QUICK.c */
HANDLE_IGET_X_QUICK(OP_IGET_OBJECT_QUICK,   "-object", Object, _AS_OBJECT)
OP_END

/* File: c/OP_IPUT_QUICK.c */
HANDLE_IPUT_X_QUICK(OP_IPUT_QUICK,          "", Int, )
OP_END

/* File: c/OP_IPUT_WIDE_QUICK.c */
HANDLE_IPUT_X_QUICK(OP_IPUT_WIDE_QUICK,     "-wide", Long, _WIDE)
OP_END

/* File: c/OP_IPUT_OBJECT_QUICK.c */
HANDLE_IPUT_X_QUICK(OP_IPUT_OBJECT_QUICK,   "-object", Object, _AS_OBJECT)
OP_END

/* File: c/OP_INVOKE_VIRTUAL_QUICK.c */
HANDLE_OPCODE(OP_INVOKE_VIRTUAL_QUICK /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeVirtualQuick, false);
OP_END

/* File: c/OP_INVOKE_VIRTUAL_QUICK_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_VIRTUAL_QUICK_RANGE/*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeVirtualQuick, true);
OP_END

/* File: c/OP_INVOKE_SUPER_QUICK.c */
HANDLE_OPCODE(OP_INVOKE_SUPER_QUICK /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    GOTO(invokeSuperQuick, false);
OP_END

/* File: c/OP_INVOKE_SUPER_QUICK_RANGE.c */
HANDLE_OPCODE(OP_INVOKE_SUPER_QUICK_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    GOTO(invokeSuperQuick, true);
OP_END

/* File: c/OP_UNUSED_FC.c */
HANDLE_OPCODE(OP_UNUSED_FC)
OP_END

/* File: c/OP_UNUSED_FD.c */
HANDLE_OPCODE(OP_UNUSED_FD)
OP_END

/* File: c/OP_UNUSED_FE.c */
HANDLE_OPCODE(OP_UNUSED_FE)
OP_END

/* File: c/OP_UNUSED_FF.c */
HANDLE_OPCODE(OP_UNUSED_FF)
OP_END

/* File: desktop/entry.c */
/*
 * Handler function table, one entry per opcode.
 */
#undef H
#define H(_op) dvmMterp_##_op
DEFINE_GOTO_TABLE(gDvmMterpHandlers)

#undef H
#define H(_op) #_op
DEFINE_GOTO_TABLE(gDvmMterpHandlerNames)

#include <setjmp.h>

/*
 * C mterp entry point.  This just calls the various C fallbacks, making
 * this a slow but portable interpeter.
 */
bool dvmMterpStdRun(MterpGlue* glue)
{
    jmp_buf jmpBuf;
    int changeInterp;

    glue->bailPtr = &jmpBuf;

    /*
     * We want to return "changeInterp" as a boolean, but we can't return
     * zero through longjmp, so we return (boolean+1).
     */
    changeInterp = setjmp(jmpBuf) -1;
    if (changeInterp >= 0) {
        Thread* threadSelf = dvmThreadSelf();
        LOGVV("mterp threadid=%d returning %d\n",
            threadSelf->threadId, changeInterp);
        return changeInterp;
    }

    /*
     * We may not be starting at a point where we're executing instructions.
     * We need to pick up where the other interpreter left off.
     *
     * In some cases we need to call into a throw/return handler which
     * will do some processing and then either return to us (updating "glue")
     * or longjmp back out.
     */
    switch (glue->entryPoint) {
    case kInterpEntryInstr:
        /* just start at the start */
        break;
    case kInterpEntryReturn:
        dvmMterp_returnFromMethod(glue);
        break;
    case kInterpEntryThrow:
        dvmMterp_exceptionThrown(glue);
        break;
    default:
        dvmAbort();
    }

    /* run until somebody longjmp()s out */
    while (true) {
        typedef void (*Handler)(MterpGlue* glue);

        u2 inst = /*glue->*/pc[0];
        Handler handler = (Handler) gDvmMterpHandlers[inst & 0xff];
        LOGVV("handler %p %s\n",
            handler, (const char*) gDvmMterpHandlerNames[inst & 0xff]);
        (*handler)(glue);
    }
}

/*
 * C mterp exit point.  Call here to bail out of the interpreter.
 */
void dvmMterpStdBail(MterpGlue* glue, bool changeInterp)
{
    jmp_buf* pJmpBuf = glue->bailPtr;
    longjmp(*pJmpBuf, ((int)changeInterp)+1);
}


/* File: c/footer.c */
/*
 * C footer.  This has some common code shared by the various targets.
 */

#define GOTO_TARGET(_target, ...)                                           \
    void dvmMterp_##_target(MterpGlue* glue, ## __VA_ARGS__) {              \
        u2 ref, vsrc1, vsrc2, vdst;                                         \
        u2 inst = FETCH(0);                                                 \
        const Method* methodToCall;                                         \
        StackSaveArea* debugSaveArea;

#define GOTO_TARGET_END }


/*
 * Everything from here on is a "goto target".  In the basic interpreter
 * we jump into these targets and then jump directly to the handler for
 * next instruction.  Here, these are subroutines that return to the caller.
 */

GOTO_TARGET(filledNewArray, bool methodCallRange)
    {
        ClassObject* arrayClass;
        ArrayObject* newArray;
        int* contents;
        char typeCh;
        int i;
        u4 arg5;

        EXPORT_PC();

        ref = FETCH(1);             /* class ref */
        vdst = FETCH(2);            /* first 4 regs -or- range base */

        if (methodCallRange) {
            vsrc1 = INST_AA(inst);  /* #of elements */
            arg5 = -1;              /* silence compiler warning */
            ILOGV("|filled-new-array-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
        } else {
            arg5 = INST_A(inst);
            vsrc1 = INST_B(inst);   /* #of elements */
            ILOGV("|filled-new-array args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1, ref, vdst, arg5);
        }

        /*
         * Resolve the array class.
         */
        arrayClass = dvmDexGetResolvedClass(methodClassDex, ref);
        if (arrayClass == NULL) {
            arrayClass = dvmResolveClass(method->clazz, ref, false);
            if (arrayClass == NULL)
                GOTO(exceptionThrown);
        }
        /*
        if (!dvmIsArrayClass(arrayClass)) {
            dvmThrowException("Ljava/lang/RuntimeError;",
                "filled-new-array needs array class");
            GOTO(exceptionThrown);
        }
        */
        /* verifier guarantees this is an array class */
        assert(dvmIsArrayClass(arrayClass));
        assert(dvmIsClassInitialized(arrayClass));

        /*
         * Create an array of the specified type.
         */
        LOGVV("+++ filled-new-array type is '%s'\n", arrayClass->descriptor);
        typeCh = arrayClass->descriptor[1];
        if (typeCh == 'D' || typeCh == 'J') {
            /* category 2 primitives not allowed */
            dvmThrowException("Ljava/lang/RuntimeError;",
                "bad filled array req");
            GOTO(exceptionThrown);
        } else if (typeCh == 'L' || typeCh == '[') {
            /* create array of objects or array of arrays */
            /* TODO: need some work in the verifier before we allow this */
            LOGE("fnao not implemented\n");
            dvmThrowException("Ljava/lang/InternalError;",
                "filled-new-array not implemented for reference types");
            GOTO(exceptionThrown);
        } else if (typeCh != 'I') {
            /* TODO: requires multiple "fill in" loops with different widths */
            LOGE("non-int not implemented\n");
            dvmThrowException("Ljava/lang/InternalError;",
                "filled-new-array not implemented for anything but 'int'");
            GOTO(exceptionThrown);
        }

        assert(strchr("BCIFZ", typeCh) != NULL);
        newArray = dvmAllocPrimitiveArray(arrayClass->descriptor[1], vsrc1,
                    ALLOC_DONT_TRACK);
        if (newArray == NULL)
            GOTO(exceptionThrown);

        /*
         * Fill in the elements.  It's legal for vsrc1 to be zero.
         */
        contents = (int*) newArray->contents;
        if (methodCallRange) {
            for (i = 0; i < vsrc1; i++)
                contents[i] = GET_REGISTER(vdst+i);
        } else {
            assert(vsrc1 <= 5);
            if (vsrc1 == 5) {
                contents[4] = GET_REGISTER(arg5);
                vsrc1--;
            }
            for (i = 0; i < vsrc1; i++) {
                contents[i] = GET_REGISTER(vdst & 0x0f);
                vdst >>= 4;
            }
        }

        retval.l = newArray;
    }
    FINISH(3);
GOTO_TARGET_END


GOTO_TARGET(invokeVirtual, bool methodCallRange)
    {
        Method* baseMethod;
        Object* thisPtr;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            /*
             * The object against which we are executing a method is always
             * in the first argument.
             */
            assert(vsrc1 > 0);
            ILOGV("|invoke-virtual-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisPtr = (Object*) GET_REGISTER(vdst);
        } else {
            /*
             * The object against which we are executing a method is always
             * in the first argument.
             */
            assert((vsrc1>>4) > 0);
            ILOGV("|invoke-virtual args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisPtr = (Object*) GET_REGISTER(vdst & 0x0f);
        }

        if (!checkForNull(thisPtr))
            GOTO(exceptionThrown);

        /*
         * Resolve the method.  This is the correct method for the static
         * type of the object.  We also verify access permissions here.
         */
        baseMethod = dvmDexGetResolvedMethod(methodClassDex, ref);
        if (baseMethod == NULL) {
            baseMethod = dvmResolveMethod(method->clazz, ref, METHOD_VIRTUAL);
            if (baseMethod == NULL) {
                ILOGV("+ unknown method or access denied\n");
                GOTO(exceptionThrown);
            }
        }

        /*
         * Combine the object we found with the vtable offset in the
         * method.
         */
        assert(baseMethod->methodIndex < thisPtr->clazz->vtableCount);
        methodToCall = thisPtr->clazz->vtable[baseMethod->methodIndex];

#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            /*
             * This can happen if you create two classes, Base and Sub, where
             * Sub is a sub-class of Base.  Declare a protected abstract
             * method foo() in Base, and invoke foo() from a method in Base.
             * Base is an "abstract base class" and is never instantiated
             * directly.  Now, Override foo() in Sub, and use Sub.  This
             * Works fine unless Sub stops providing an implementation of
             * the method.
             */
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            GOTO(exceptionThrown);
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif

        LOGVV("+++ base=%s.%s virtual[%d]=%s.%s\n",
            baseMethod->clazz->descriptor, baseMethod->name,
            (u4) baseMethod->methodIndex,
            methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

#if 0
        if (vsrc1 != methodToCall->insSize) {
            LOGW("WRONG METHOD: base=%s.%s virtual[%d]=%s.%s\n",
                baseMethod->clazz->descriptor, baseMethod->name,
                (u4) baseMethod->methodIndex,
                methodToCall->clazz->descriptor, methodToCall->name);
            //dvmDumpClass(baseMethod->clazz);
            //dvmDumpClass(methodToCall->clazz);
            dvmDumpAllClasses(0);
        }
#endif

        GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
    }
GOTO_TARGET_END

GOTO_TARGET(invokeSuper, bool methodCallRange)
    {
        Method* baseMethod;
        u2 thisReg;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            ILOGV("|invoke-super-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisReg = vdst;
        } else {
            ILOGV("|invoke-super args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisReg = vdst & 0x0f;
        }
        /* impossible in well-formed code, but we must check nevertheless */
        if (!checkForNull((Object*) GET_REGISTER(thisReg)))
            GOTO(exceptionThrown);

        /*
         * Resolve the method.  This is the correct method for the static
         * type of the object.  We also verify access permissions here.
         * The first arg to dvmResolveMethod() is just the referring class
         * (used for class loaders and such), so we don't want to pass
         * the superclass into the resolution call.
         */
        baseMethod = dvmDexGetResolvedMethod(methodClassDex, ref);
        if (baseMethod == NULL) {
            baseMethod = dvmResolveMethod(method->clazz, ref, METHOD_VIRTUAL);
            if (baseMethod == NULL) {
                ILOGV("+ unknown method or access denied\n");
                GOTO(exceptionThrown);
            }
        }

        /*
         * Combine the object we found with the vtable offset in the
         * method's class.
         *
         * We're using the current method's class' superclass, not the
         * superclass of "this".  This is because we might be executing
         * in a method inherited from a superclass, and we want to run
         * in that class' superclass.
         */
        if (baseMethod->methodIndex >= method->clazz->super->vtableCount) {
            /*
             * Method does not exist in the superclass.  Could happen if
             * superclass gets updated.
             */
            dvmThrowException("Ljava/lang/NoSuchMethodError;",
                baseMethod->name);
            GOTO(exceptionThrown);
        }
        methodToCall = method->clazz->super->vtable[baseMethod->methodIndex];
#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            GOTO(exceptionThrown);
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif
        LOGVV("+++ base=%s.%s super-virtual=%s.%s\n",
            baseMethod->clazz->descriptor, baseMethod->name,
            methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

        GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
    }
GOTO_TARGET_END

GOTO_TARGET(invokeInterface, bool methodCallRange)
    {
        Object* thisPtr;
        ClassObject* thisClass;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            /*
             * The object against which we are executing a method is always
             * in the first argument.
             */
            assert(vsrc1 > 0);
            ILOGV("|invoke-interface-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisPtr = (Object*) GET_REGISTER(vdst);
        } else {
            /*
             * The object against which we are executing a method is always
             * in the first argument.
             */
            assert((vsrc1>>4) > 0);
            ILOGV("|invoke-interface args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisPtr = (Object*) GET_REGISTER(vdst & 0x0f);
        }

        if (!checkForNull(thisPtr))
            GOTO(exceptionThrown);
        thisClass = thisPtr->clazz;

        /*
         * Given a class and a method index, find the Method* with the
         * actual code we want to execute.
         */
        methodToCall = dvmFindInterfaceMethodInCache(thisClass, ref, method,
                        methodClassDex);
        if (methodToCall == NULL) {
            assert(dvmCheckException(self));
            GOTO(exceptionThrown);
        }

        GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
    }
GOTO_TARGET_END

GOTO_TARGET(invokeDirect, bool methodCallRange)
    {
        u2 thisReg;

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        EXPORT_PC();

        if (methodCallRange) {
            ILOGV("|invoke-direct-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisReg = vdst;
        } else {
            ILOGV("|invoke-direct args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisReg = vdst & 0x0f;
        }
        if (!checkForNull((Object*) GET_REGISTER(thisReg)))
            GOTO(exceptionThrown);

        methodToCall = dvmDexGetResolvedMethod(methodClassDex, ref);
        if (methodToCall == NULL) {
            methodToCall = dvmResolveMethod(method->clazz, ref, METHOD_DIRECT);
            if (methodToCall == NULL) {
                ILOGV("+ unknown direct method\n");     // should be impossible
                GOTO(exceptionThrown);
            }
        }
        GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
    }
GOTO_TARGET_END

GOTO_TARGET(invokeStatic, bool methodCallRange)
    vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
    ref = FETCH(1);             /* method ref */
    vdst = FETCH(2);            /* 4 regs -or- first reg */

    EXPORT_PC();

    if (methodCallRange)
        ILOGV("|invoke-static-range args=%d @0x%04x {regs=v%d-v%d}",
            vsrc1, ref, vdst, vdst+vsrc1-1);
    else
        ILOGV("|invoke-static args=%d @0x%04x {regs=0x%04x %x}",
            vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
    methodToCall = dvmDexGetResolvedMethod(methodClassDex, ref);
    if (methodToCall == NULL) {
        methodToCall = dvmResolveMethod(method->clazz, ref, METHOD_STATIC);
        if (methodToCall == NULL) {
            ILOGV("+ unknown method\n");
            GOTO(exceptionThrown);
        }
    }
    GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
GOTO_TARGET_END

GOTO_TARGET(invokeVirtualQuick, bool methodCallRange)
    {
        Object* thisPtr;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* vtable index */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            /*
             * The object against which we are executing a method is always
             * in the first argument.
             */
            assert(vsrc1 > 0);
            ILOGV("|invoke-virtual-quick-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisPtr = (Object*) GET_REGISTER(vdst);
        } else {
            /*
             * The object against which we are executing a method is always
             * in the first argument.
             */
            assert((vsrc1>>4) > 0);
            ILOGV("|invoke-virtual-quick args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisPtr = (Object*) GET_REGISTER(vdst & 0x0f);
        }

        if (!checkForNull(thisPtr))
            GOTO(exceptionThrown);

        /*
         * Combine the object we found with the vtable offset in the
         * method.
         */
        assert(ref < thisPtr->clazz->vtableCount);
        methodToCall = thisPtr->clazz->vtable[ref];

#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            GOTO(exceptionThrown);
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif

        LOGVV("+++ virtual[%d]=%s.%s\n",
            ref, methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

        GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
    }
GOTO_TARGET_END

GOTO_TARGET(invokeSuperQuick, bool methodCallRange)
    {
        u2 thisReg;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* vtable index */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            ILOGV("|invoke-super-quick-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisReg = vdst;
        } else {
            ILOGV("|invoke-super-quick args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisReg = vdst & 0x0f;
        }
        /* impossible in well-formed code, but we must check nevertheless */
        if (!checkForNull((Object*) GET_REGISTER(thisReg)))
            GOTO(exceptionThrown);

#if 0   /* impossible in optimized + verified code */
        if (ref >= method->clazz->super->vtableCount) {
            dvmThrowException("Ljava/lang/NoSuchMethodError;", NULL);
            GOTO(exceptionThrown);
        }
#else
        assert(ref < method->clazz->super->vtableCount);
#endif

        /*
         * Combine the object we found with the vtable offset in the
         * method's class.
         *
         * We're using the current method's class' superclass, not the
         * superclass of "this".  This is because we might be executing
         * in a method inherited from a superclass, and we want to run
         * in the method's class' superclass.
         */
        methodToCall = method->clazz->super->vtable[ref];

#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            GOTO(exceptionThrown);
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif
        LOGVV("+++ super-virtual[%d]=%s.%s\n",
            ref, methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

        GOTO(invokeMethod, methodCallRange, methodToCall, vsrc1, vdst);
    }
GOTO_TARGET_END



    /*
     * General handling for return-void, return, and return-wide.  Put the
     * return value in "retval" before jumping here.
     */
GOTO_TARGET(returnFromMethod)
    {
        StackSaveArea* saveArea;

        /*
         * We must do this BEFORE we pop the previous stack frame off, so
         * that the GC can see the return value (if any) in the local vars.
         *
         * Since this is now an interpreter switch point, we must do it before
         * we do anything at all.
         */
        PERIODIC_CHECKS(kInterpEntryReturn, 0);

        ILOGV("> retval=0x%llx (leaving %s.%s %s)",
            retval.j, method->clazz->descriptor, method->name,
            method->signature);
        //DUMP_REGS(method, fp);

        saveArea = SAVEAREA_FROM_FP(fp);

#ifdef EASY_GDB
        debugSaveArea = saveArea;
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_PROFILER)
        TRACE_METHOD_EXIT(self, method);
#endif

        /* back up to previous frame and see if we hit a break */
        fp = saveArea->prevFrame;
        assert(fp != NULL);
        if (dvmIsBreakFrame(fp)) {
            /* bail without popping the method frame from stack */
            LOGVV("+++ returned into break frame\n");
            GOTO_BAIL(false);
        }

        /* update thread FP, and reset local variables */
        self->curFrame = fp;
        method =
#undef method       // ARRGH!
            SAVEAREA_FROM_FP(fp)->method;
#define method glue->method
        //methodClass = method->clazz;
        methodClassDex = method->clazz->pDvmDex;
        pc = saveArea->savedPc;
        ILOGD("> (return to %s.%s %s)", method->clazz->descriptor,
            method->name, method->signature);

        /* use FINISH on the caller's invoke instruction */
        //u2 invokeInstr = INST_INST(FETCH(0));
        if (true /*invokeInstr >= OP_INVOKE_VIRTUAL &&
            invokeInstr <= OP_INVOKE_INTERFACE*/)
        {
            FINISH(3);
        } else {
            //LOGE("Unknown invoke instr %02x at %d\n",
            //    invokeInstr, (int) (pc - method->insns));
            assert(false);
        }
    }
GOTO_TARGET_END


    /*
     * Jump here when the code throws an exception.
     *
     * By the time we get here, the Throwable has been created and the stack
     * trace has been saved off.
     */
GOTO_TARGET(exceptionThrown)
    {
        Object* exception;
        int catchRelPc;

        /*
         * Since this is now an interpreter switch point, we must do it before
         * we do anything at all.
         */
        PERIODIC_CHECKS(kInterpEntryThrow, 0);

        /*
         * We save off the exception and clear the exception status.  While
         * processing the exception we might need to load some Throwable
         * classes, and we don't want class loader exceptions to get
         * confused with this one.
         */
        assert(dvmCheckException(self));
        exception = dvmGetException(self);
        dvmAddTrackedAlloc(exception, self);
        dvmClearException(self);

        LOGV("Handling exception %s at %s:%d\n",
            exception->clazz->descriptor, method->name,
            dvmLineNumFromPC(method, pc - method->insns));

#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_DEBUGGER)
        /*
         * Tell the debugger about it.
         *
         * TODO: if the exception was thrown by interpreted code, control
         * fell through native, and then back to us, we will report the
         * exception at the point of the throw and again here.  We can avoid
         * this by not reporting exceptions when we jump here directly from
         * the native call code above, but then we won't report exceptions
         * that were thrown *from* the JNI code (as opposed to *through* it).
         *
         * The correct solution is probably to ignore from-native exceptions
         * here, and have the JNI exception code do the reporting to the
         * debugger.
         */
        if (gDvm.debuggerActive) {
            void* catchFrame;
            catchRelPc = dvmFindCatchBlock(self, pc - method->insns,
                        exception, true, &catchFrame);
            dvmDbgPostException(fp, pc - method->insns, catchFrame, catchRelPc,
                exception);
        }
#endif

        /*
         * We need to unroll to the catch block or the nearest "break"
         * frame.
         *
         * A break frame could indicate that we have reached an intermediate
         * native call, or have gone off the top of the stack and the thread
         * needs to exit.  Either way, we return from here, leaving the
         * exception raised.
         *
         * If we do find a catch block, we want to transfer execution to
         * that point.
         */
        catchRelPc = dvmFindCatchBlock(self, pc - method->insns,
                    exception, false, (void*)&fp);

        /*
         * Restore the stack bounds after an overflow.  This isn't going to
         * be correct in all circumstances, e.g. if JNI code devours the
         * exception this won't happen until some other exception gets
         * thrown.  If the code keeps pushing the stack bounds we'll end
         * up aborting the VM.
         */
        if (self->stackOverflowed)
            dvmCleanupStackOverflow(self);

        if (catchRelPc < 0) {
            /* falling through to JNI code or off the bottom of the stack */
#if DVM_SHOW_EXCEPTION >= 2
            LOGD("Exception %s from %s:%d not caught locally\n",
                exception->clazz->descriptor, dvmGetMethodSourceFile(method),
                dvmLineNumFromPC(method, pc - method->insns));
#endif
            dvmSetException(self, exception);
            dvmReleaseTrackedAlloc(exception, self);
            GOTO_BAIL(false);
        }

#if DVM_SHOW_EXCEPTION >= 3
        {
            const Method* catchMethod =
#undef method
                SAVEAREA_FROM_FP(fp)->method;
#define method glue->method
            LOGD("Exception %s thrown from %s:%d to %s:%d\n",
                exception->clazz->descriptor, dvmGetMethodSourceFile(method),
                dvmLineNumFromPC(method, pc - method->insns),
                dvmGetMethodSourceFile(catchMethod),
                dvmLineNumFromPC(catchMethod, catchRelPc));
        }
#endif

        /*
         * Adjust local variables to match self->curFrame and the
         * updated PC.
         */
        //fp = (u4*) self->curFrame;
        method =
#undef method
            SAVEAREA_FROM_FP(fp)->method;
#define method glue->method
        //methodClass = method->clazz;
        methodClassDex = method->clazz->pDvmDex;
        pc = method->insns + catchRelPc;
        ILOGV("> pc <-- %s.%s %s", method->clazz->descriptor, method->name,
            method->signature);
        DUMP_REGS(method, fp, false);               // show all regs

        /*
         * Restore the exception if the handler wants it.
         *
         * The Dalvik spec mandates that, if an exception handler wants to
         * do something with the exception, the first instruction executed
         * must be "move-exception".  We can pass the exception along
         * through the thread struct, and let the move-exception instruction
         * clear it for us.
         *
         * If the handler doesn't call move-exception, we don't want to
         * finish here with an exception still pending.
         */
        if (INST_INST(FETCH(0)) == OP_MOVE_EXCEPTION)
            dvmSetException(self, exception);

        dvmReleaseTrackedAlloc(exception, self);
        FINISH(0);
    }
GOTO_TARGET_END


    /*
     * General handling for invoke-{virtual,super,direct,static,interface},
     * including "quick" variants.
     *
     * Set "methodToCall" to the Method we're calling, and "methodCallRange"
     * depending on whether this is a "/range" instruction.
     *
     * For a range call:
     *  "vsrc1" holds the argument count (8 bits)
     *  "vdst" holds the first argument in the range
     * For a non-range call:
     *  "vsrc1" holds the argument count (4 bits) and the 5th argument index
     *  "vdst" holds four 4-bit register indices
     *
     * The caller must EXPORT_PC before jumping here, because any method
     * call can throw a stack overflow exception.
     */
GOTO_TARGET(invokeMethod, bool methodCallRange, const Method* _methodToCall,
    u2 count, u2 regs)
    {
        vsrc1 = count; vdst = regs; methodToCall = _methodToCall;  /* ADDED */

        //printf("range=%d call=%p count=%d regs=0x%04x\n",
        //    methodCallRange, methodToCall, count, regs);
        //printf(" --> %s.%s %s\n", methodToCall->clazz->descriptor,
        //    methodToCall->name, methodToCall->signature);

        u4* outs;
        int i;

        /*
         * Copy args.  This may corrupt vsrc1/vdst.
         */
        if (methodCallRange) {
            // could use memcpy or a "Duff's device"; most functions have
            // so few args it won't matter much
            assert(vsrc1 <= method->outsSize);
            assert(vsrc1 == methodToCall->insSize);
            outs = OUTS_FROM_FP(fp, vsrc1);
            for (i = 0; i < vsrc1; i++)
                outs[i] = GET_REGISTER(vdst+i);
        } else {
            u4 count = vsrc1 >> 4;

            assert(count <= method->outsSize);
            assert(count == methodToCall->insSize);
            assert(count <= 5);

            outs = OUTS_FROM_FP(fp, count);
#if 0
            if (count == 5) {
                outs[4] = GET_REGISTER(vsrc1 & 0x0f);
                count--;
            }
            for (i = 0; i < (int) count; i++) {
                outs[i] = GET_REGISTER(vdst & 0x0f);
                vdst >>= 4;
            }
#else
            // This version executes fewer instructions but is larger
            // overall.  Seems to be a teensy bit faster.
            assert((vdst >> 16) == 0);  // 16 bits -or- high 16 bits clear
            switch (count) {
            case 5:
                outs[4] = GET_REGISTER(vsrc1 & 0x0f);
            case 4:
                outs[3] = GET_REGISTER(vdst >> 12);
            case 3:
                outs[2] = GET_REGISTER((vdst & 0x0f00) >> 8);
            case 2:
                outs[1] = GET_REGISTER((vdst & 0x00f0) >> 4);
            case 1:
                outs[0] = GET_REGISTER(vdst & 0x0f);
            default:
                ;
            }
#endif
        }
    }

    /*
     * (This was originally a "goto" target; I've kept it separate from the
     * stuff above in case we want to refactor things again.)
     *
     * At this point, we have the arguments stored in the "outs" area of
     * the current method's stack frame, and the method to call in
     * "methodToCall".  Push a new stack frame.
     */
    {
        StackSaveArea* newSaveArea;
        u4* newFp;

        ILOGV("> %s%s.%s %s",
            dvmIsNativeMethod(methodToCall) ? "(NATIVE) " : "",
            methodToCall->clazz->descriptor, methodToCall->name,
            methodToCall->signature);

        newFp = (u4*) SAVEAREA_FROM_FP(fp) - methodToCall->registersSize;
        newSaveArea = SAVEAREA_FROM_FP(newFp);

        /* verify that we have enough space */
        if (true) {
            u1* bottom;
            bottom = (u1*) newSaveArea - methodToCall->outsSize * sizeof(u4);
            if (bottom < self->interpStackEnd) {
                /* stack overflow */
                LOGV("Stack overflow on method call (start=%p end=%p newBot=%p size=%d '%s')\n",
                    self->interpStackStart, self->interpStackEnd, bottom,
                    self->interpStackSize, methodToCall->name);
                dvmHandleStackOverflow(self);
                assert(dvmCheckException(self));
                GOTO(exceptionThrown);
            }
            //LOGD("+++ fp=%p newFp=%p newSave=%p bottom=%p\n",
            //    fp, newFp, newSaveArea, bottom);
        }

#ifdef LOG_INSTR
        if (methodToCall->registersSize > methodToCall->insSize) {
            /*
             * This makes valgrind quiet when we print registers that
             * haven't been initialized.  Turn it off when the debug
             * messages are disabled -- we want valgrind to report any
             * used-before-initialized issues.
             */
            memset(newFp, 0xcc,
                (methodToCall->registersSize - methodToCall->insSize) * 4);
        }
#endif

#ifdef EASY_GDB
        newSaveArea->prevSave = SAVEAREA_FROM_FP(fp);
#endif
        newSaveArea->prevFrame = fp;
        newSaveArea->savedPc = pc;
#undef method
        newSaveArea->method = methodToCall;
#define method glue->method

        if (!dvmIsNativeMethod(methodToCall)) {
            /*
             * "Call" interpreted code.  Reposition the PC, update the
             * frame pointer and other local state, and continue.
             */
            method = methodToCall;
            methodClassDex = method->clazz->pDvmDex;
            pc = methodToCall->insns;
            fp = self->curFrame = newFp;
#ifdef EASY_GDB
            debugSaveArea = SAVEAREA_FROM_FP(newFp);
#endif
#if INTERP_TYPE == INTERP_DBG
            debugIsMethodEntry = true;              // profiling, debugging
#endif
            ILOGD("> pc <-- %s.%s %s", method->clazz->descriptor, method->name,
                method->signature);
            DUMP_REGS(method, fp, true);            // show input args
            FINISH(0);                              // jump to method start
        } else {
            /* set this up for JNI locals, even if not a JNI native */
            newSaveArea->xtra.localRefTop = self->jniLocalRefTable.nextEntry;

            self->curFrame = newFp;

            DUMP_REGS(methodToCall, newFp, true);   // show input args

#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_DEBUGGER)
            if (gDvm.debuggerActive) {
                dvmDbgPostLocationEvent(methodToCall, -1,
                    dvmGetThisPtr(method, fp), DBG_METHOD_ENTRY);
            }
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_PROFILER)
            TRACE_METHOD_ENTER(self, methodToCall);
#endif

            ILOGD("> native <-- %s.%s %s", methodToCall->clazz->descriptor,
                methodToCall->name, methodToCall->signature);

            /*
             * Jump through native call bridge.  Because we leave no
             * space for locals on native calls, "newFp" points directly
             * to the method arguments.
             */
            (*methodToCall->nativeFunc)(newFp, &retval, methodToCall, self);

#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_DEBUGGER)
            if (gDvm.debuggerActive) {
                dvmDbgPostLocationEvent(methodToCall, -1,
                    dvmGetThisPtr(method, fp), DBG_METHOD_EXIT);
            }
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_PROFILER)
            TRACE_METHOD_EXIT(self, methodToCall);
#endif

            /* pop frame off */
            dvmPopJniLocals(self, newSaveArea);
            self->curFrame = fp;

            /*
             * If the native code threw an exception, or interpreted code
             * invoked by the native call threw one and nobody has cleared
             * it, jump to our local exception handling.
             */
            if (dvmCheckException(self)) {
                LOGV("Exception thrown by/below native code\n");
                GOTO(exceptionThrown);
            }

            ILOGD("> retval=0x%llx (leaving native)", retval.j);
            ILOGD("> (return from native %s.%s to %s.%s %s)",
                methodToCall->clazz->descriptor, methodToCall->name,
                method->clazz->descriptor, method->name,
                method->signature);

            //u2 invokeInstr = INST_INST(FETCH(0));
            if (true /*invokeInstr >= OP_INVOKE_VIRTUAL &&
                invokeInstr <= OP_INVOKE_INTERFACE*/)
            {
                FINISH(3);
            } else {
                //LOGE("Unknown invoke instr %02x at %d\n",
                //    invokeInstr, (int) (pc - method->insns));
                assert(false);
            }
        }
    }
    assert(false);      // should not get here
GOTO_TARGET_END


/* undefine "magic" name remapping */
#undef retval
#undef pc
#undef fp
#undef method
#undef methodClassDex
#undef self
#undef debugTrackedRefStart

