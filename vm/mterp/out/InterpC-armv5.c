/*
 * This file was generated automatically by gen-mterp.py for 'armv5'.
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

/* File: armv5/debug.c */
#include <inttypes.h>

/*
 * Dump the fixed-purpose ARM registers, along with some other info.
 *
 * This function MUST be compiled in ARM mode -- THUMB will yield bogus
 * results.
 *
 * This will NOT preserve r0-r3/ip.
 */
void dvmMterpDumpArmRegs(uint32_t r0, uint32_t r1, uint32_t r2, uint32_t r3)
{
    register uint32_t rPC       asm("r4");
    register uint32_t rFP       asm("r5");
    register uint32_t rGLUE     asm("r6");
    register uint32_t rIBASE    asm("r7");
    register uint32_t rINST     asm("r8");
    register uint32_t r9        asm("r9");
    register uint32_t r10       asm("r10");

    extern char dvmAsmInstructionStart[];

    printf("REGS: r0=%08x r1=%08x r2=%08x r3=%08x\n", r0, r1, r2, r3);
    printf("    : rPC=%08x rFP=%08x rGLUE=%08x rIBASE=%08x\n",
        rPC, rFP, rGLUE, rIBASE);
    printf("    : rINST=%08x r9=%08x r10=%08x\n", rINST, r9, r10);

    MterpGlue* glue = (MterpGlue*) rGLUE;
    const Method* method = glue->method;
    printf("    + self is %p\n", dvmThreadSelf());
    //printf("    + currently in %s.%s %s\n",
    //    method->clazz->descriptor, method->name, method->signature);
    //printf("    + dvmAsmInstructionStart = %p\n", dvmAsmInstructionStart);
    //printf("    + next handler for 0x%02x = %p\n",
    //    rINST & 0xff, dvmAsmInstructionStart + (rINST & 0xff) * 64);
}

/*
 * Dump the StackSaveArea for the specified frame pointer.
 */
void dvmDumpFp(void* fp, StackSaveArea* otherSaveArea)
{
    StackSaveArea* saveArea = SAVEAREA_FROM_FP(fp);
    printf("StackSaveArea for fp %p [%p/%p]:\n", fp, saveArea, otherSaveArea);
#ifdef EASY_GDB
    printf("  prevSave=%p, prevFrame=%p savedPc=%p meth=%p curPc=%p\n",
        saveArea->prevSave, saveArea->prevFrame, saveArea->savedPc,
        saveArea->method, saveArea->xtra.currentPc);
#else
    printf("  prevFrame=%p savedPc=%p meth=%p curPc=%p fp[0]=0x%08x\n",
        saveArea->prevFrame, saveArea->savedPc,
        saveArea->method, saveArea->xtra.currentPc,
        *(u4*)fp);
#endif
}

/*
 * Does the bulk of the work for common_printMethod().
 */
void dvmMterpPrintMethod(Method* method)
{
    /*
     * It is a direct (non-virtual) method if it is static, private,
     * or a constructor.
     */
    bool isDirect = 
        ((method->accessFlags & (ACC_STATIC|ACC_PRIVATE)) != 0) ||
        (method->name[0] == '<');

    char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        
    printf("<%c:%s.%s %s> ",
            isDirect ? 'D' : 'V',
            method->clazz->descriptor,
            method->name,
            desc);

    free(desc);
}

