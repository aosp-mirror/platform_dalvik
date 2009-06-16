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

bool dvmCompilerGenConversion(CompilationUnit *cUnit, MIR *mir)
{
    return dvmCompilerGenConversionPortable(cUnit, mir);
}

bool dvmCompilerGenArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                        int vSrc1, int vSrc2)
{
    return dvmCompilerGenArithOpFloatPortable(cUnit, mir, vDest, vSrc1, vSrc2);
}

bool dvmCompilerGenArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                         int vSrc1, int vSrc2)
{
    return dvmCompilerGenArithOpDoublePortable(cUnit, mir, vDest, vSrc1, vSrc2);
}

bool dvmCompilerGenCmpX(CompilationUnit *cUnit, MIR *mir, int vDest,
                                int vSrc1, int vSrc2)
{
    switch (mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            dvmCompilerLoadValue(cUnit, vSrc1, r0);
            dvmCompilerLoadValue(cUnit, vSrc2, r1);
            dvmCompilerGenDispatchToHandler(cUnit, TEMPLATE_CMPL_FLOAT);
            dvmCompilerStoreValue(cUnit, r0, vDest, r1);
            break;
        case OP_CMPG_FLOAT:
            dvmCompilerLoadValue(cUnit, vSrc1, r0);
            dvmCompilerLoadValue(cUnit, vSrc2, r1);
            dvmCompilerGenDispatchToHandler(cUnit, TEMPLATE_CMPG_FLOAT);
            dvmCompilerStoreValue(cUnit, r0, vDest, r1);
            break;
        case OP_CMPL_DOUBLE:
            dvmCompilerLoadValueAddress(cUnit, vSrc1, r0);
            dvmCompilerLoadValueAddress(cUnit, vSrc2, r1);
            dvmCompilerGenDispatchToHandler(cUnit, TEMPLATE_CMPL_DOUBLE);
            dvmCompilerStoreValue(cUnit, r0, vDest, r1);
            break;
        case OP_CMPG_DOUBLE:
            dvmCompilerLoadValueAddress(cUnit, vSrc1, r0);
            dvmCompilerLoadValueAddress(cUnit, vSrc2, r1);
            dvmCompilerGenDispatchToHandler(cUnit, TEMPLATE_CMPG_DOUBLE);
            dvmCompilerStoreValue(cUnit, r0, vDest, r1);
            break;
        default:
            return true;
    }
    return false;
}
