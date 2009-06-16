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

#include "Dalvik.h"
#include "Armv5teLIR.h"
#include "Codegen.h"

bool dvmCompilerGenArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                                int vSrc1, int vSrc2)
{
    TemplateOpCode opCode;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_FLOAT_2ADDR:
        case OP_ADD_FLOAT:
            opCode = TEMPLATE_ADD_FLOAT_VFP;
            break;
        case OP_SUB_FLOAT_2ADDR:
        case OP_SUB_FLOAT:
            opCode = TEMPLATE_SUB_FLOAT_VFP;
        case OP_DIV_FLOAT_2ADDR:
        case OP_DIV_FLOAT:
            opCode = TEMPLATE_DIV_FLOAT_VFP;
            break;
        case OP_MUL_FLOAT_2ADDR:
        case OP_MUL_FLOAT:
            opCode = TEMPLATE_MUL_FLOAT_VFP;
            break;
        case OP_REM_FLOAT_2ADDR:
        case OP_REM_FLOAT:
        case OP_NEG_FLOAT: {
            return dvmCompilerGenArithOpFloatPortable(cUnit, mir, vDest,
                                                      vSrc1, vSrc2);
        }
        default:
            return true;
    }
    dvmCompilerLoadValueAddress(cUnit, vDest, r0);
    dvmCompilerLoadValueAddress(cUnit, vSrc1, r1);
    dvmCompilerLoadValueAddress(cUnit, vSrc2, r2);
    dvmCompilerGenDispatchToHandler(cUnit, opCode);
    return false;
}

bool dvmCompilerGenArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                                 int vSrc1, int vSrc2)
{
    TemplateOpCode opCode;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_DOUBLE_2ADDR:
        case OP_ADD_DOUBLE:
            opCode = TEMPLATE_ADD_DOUBLE_VFP;
            break;
        case OP_SUB_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE:
            opCode = TEMPLATE_SUB_DOUBLE_VFP;
            break;
        case OP_DIV_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE:
            opCode = TEMPLATE_DIV_DOUBLE_VFP;
            break;
        case OP_MUL_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE:
            opCode = TEMPLATE_MUL_DOUBLE_VFP;
            break;
        case OP_REM_DOUBLE_2ADDR:
        case OP_REM_DOUBLE:
        case OP_NEG_DOUBLE: {
            return dvmCompilerGenArithOpDoublePortable(cUnit, mir, vDest,
                                                       vSrc1, vSrc2);
        }
        default:
            return true;
    }
    dvmCompilerLoadValueAddress(cUnit, vDest, r0);
    dvmCompilerLoadValueAddress(cUnit, vSrc1, r1);
    dvmCompilerLoadValueAddress(cUnit, vSrc2, r2);
    dvmCompilerGenDispatchToHandler(cUnit, opCode);
    return false;
}

bool dvmCompilerGenConversion(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vSrc1Dest = mir->dalvikInsn.vA;
    int vSrc2 = mir->dalvikInsn.vB;
    TemplateOpCode template;

    switch (opCode) {
        case OP_INT_TO_FLOAT:
            template = TEMPLATE_INT_TO_FLOAT_VFP;
            break;
        case OP_FLOAT_TO_INT:
            template = TEMPLATE_FLOAT_TO_INT_VFP;
            break;
        case OP_DOUBLE_TO_FLOAT:
            template = TEMPLATE_DOUBLE_TO_FLOAT_VFP;
            break;
        case OP_FLOAT_TO_DOUBLE:
            template = TEMPLATE_FLOAT_TO_DOUBLE_VFP;
            break;
        case OP_INT_TO_DOUBLE:
            template = TEMPLATE_INT_TO_DOUBLE_VFP;
            break;
        case OP_DOUBLE_TO_INT:
            template = TEMPLATE_DOUBLE_TO_INT_VFP;
            break;
        case OP_FLOAT_TO_LONG:
        case OP_LONG_TO_FLOAT:
        case OP_DOUBLE_TO_LONG:
        case OP_LONG_TO_DOUBLE:
            return dvmCompilerGenConversionPortable(cUnit, mir);
        default:
            return true;
    }
    dvmCompilerLoadValueAddress(cUnit, vSrc1Dest, r0);
    dvmCompilerLoadValueAddress(cUnit, vSrc2, r1);
    dvmCompilerGenDispatchToHandler(cUnit, template);
    return false;
}

bool dvmCompilerGenCmpX(CompilationUnit *cUnit, MIR *mir, int vDest,
                                int vSrc1, int vSrc2)
{
    TemplateOpCode template;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch(mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            template = TEMPLATE_CMPL_FLOAT_VFP;
            break;
        case OP_CMPG_FLOAT:
            template = TEMPLATE_CMPG_FLOAT_VFP;
            break;
        case OP_CMPL_DOUBLE:
            template = TEMPLATE_CMPL_DOUBLE_VFP;
            break;
        case OP_CMPG_DOUBLE:
            template = TEMPLATE_CMPG_DOUBLE_VFP;
            break;
        default:
            return true;
    }
    dvmCompilerLoadValueAddress(cUnit, vSrc1, r0);
    dvmCompilerLoadValueAddress(cUnit, vSrc2, r1);
    dvmCompilerGenDispatchToHandler(cUnit, template);
    dvmCompilerStoreValue(cUnit, r0, vDest, r1);
    return false;
}
