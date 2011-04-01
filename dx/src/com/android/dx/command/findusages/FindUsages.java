/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dx.command.findusages;

import com.android.dx.io.ClassData;
import com.android.dx.io.ClassDef;
import com.android.dx.io.CodeReader;
import com.android.dx.io.DexBuffer;
import com.android.dx.io.FieldId;
import com.android.dx.io.MethodId;
import com.android.dx.io.OpcodeInfo;
import com.android.dx.io.instructions.DecodedInstruction;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class FindUsages {
    private final DexBuffer dex;
    private final Set<Integer> methodIds;
    private final Set<Integer> fieldIds;
    private final CodeReader codeReader = new CodeReader();
    private final PrintWriter out;

    private ClassDef currentClass;
    private ClassData.Method currentMethod;

    public FindUsages(DexBuffer dex, String declaredBy, String memberName, final PrintWriter out) {
        this.dex = dex;
        this.out = out;

        int typeStringIndex = Collections.binarySearch(dex.strings(), declaredBy);
        int memberNameIndex = Collections.binarySearch(dex.strings(), memberName);
        if (typeStringIndex < 0 || memberNameIndex < 0) {
            methodIds = null;
            fieldIds = null;
            return; // these symbols are not mentioned in this dex
        }

        int typeIndex = Collections.binarySearch(dex.typeIds(), typeStringIndex);
        if (typeIndex < 0) {
            methodIds = null;
            fieldIds = null;
            return; // this type name isn't used as a type in this dex
        }

        methodIds = getMethodIds(dex, memberNameIndex, typeIndex);
        fieldIds = getFieldIds(dex, memberNameIndex, typeIndex);

        codeReader.setFieldVisitor(new CodeReader.Visitor() {
            public void visit(DecodedInstruction[] all,
                    DecodedInstruction one) {
                int fieldId = one.getIndex();
                if (fieldIds.contains(fieldId)) {
                    out.println(location() + ": field reference ("
                            + OpcodeInfo.getName(one.getOpcode()) + ")");
                }
            }
        });

        codeReader.setMethodVisitor(new CodeReader.Visitor() {
            public void visit(DecodedInstruction[] all,
                    DecodedInstruction one) {
                int methodId = one.getIndex();
                if (methodIds.contains(methodId)) {
                    out.println(location() + ": method reference ("
                            + OpcodeInfo.getName(one.getOpcode()) + ")");
                }
            }
        });
    }

    private String location() {
        String className = dex.typeNames().get(currentClass.getTypeIndex());
        if (currentMethod != null) {
            MethodId methodId = dex.methodIds().get(currentMethod.getMethodIndex());
            return className + "." + dex.strings().get(methodId.getNameIndex());
        } else {
            return className;
        }
    }

    /**
     * Prints usages to out.
     */
    public void findUsages() {
        if (fieldIds == null || methodIds == null) {
            return;
        }

        for (ClassDef classDef : dex.classDefs()) {
            currentClass = classDef;
            currentMethod = null;

            if (classDef.getClassDataOffset() == 0) {
                continue;
            }

            ClassData classData = dex.readClassData(classDef);
            for (ClassData.Field field : classData.allFields()) {
                if (fieldIds.contains(field.getFieldIndex())) {
                    out.println(location() + " field declared");
                }
            }

            for (ClassData.Method method : classData.allMethods()) {
                currentMethod = method;
                if (methodIds.contains(method.getMethodIndex())) {
                    out.println(location() + " method declared");
                }
                if (method.getCodeOffset() != 0) {
                    codeReader.visitAll(dex.readCode(method).getInstructions());
                }
            }
        }

        currentClass = null;
        currentMethod = null;
    }

    /**
     * Returns the fields with {@code memberNameIndex} declared by {@code
     * declaringType}.
     */
    private Set<Integer> getFieldIds(DexBuffer dex, int memberNameIndex, int declaringType) {
        Set<Integer> fields = new HashSet<Integer>();
        int fieldIndex = 0;
        for (FieldId fieldId : dex.fieldIds()) {
            if (fieldId.getNameIndex() == memberNameIndex
                    && declaringType == fieldId.getDeclaringClassIndex()) {
                fields.add(fieldIndex);
            }
            fieldIndex++;
        }
        return fields;
    }

    /**
     * Returns the methods with {@code memberNameIndex} declared by {@code
     * declaringType} and its subtypes.
     */
    private Set<Integer> getMethodIds(DexBuffer dex, int memberNameIndex, int declaringType) {
        Set<Integer> subtypes = findAssignableTypes(dex, declaringType);

        Set<Integer> methods = new HashSet<Integer>();
        int methodIndex = 0;
        for (MethodId method : dex.methodIds()) {
            if (method.getNameIndex() == memberNameIndex
                    && subtypes.contains(method.getDeclaringClassIndex())) {
                methods.add(methodIndex);
            }
            methodIndex++;
        }
        return methods;
    }

    /**
     * Returns the set of types that can be assigned to {@code typeIndex}.
     */
    private Set<Integer> findAssignableTypes(DexBuffer dex, int typeIndex) {
        Set<Integer> assignableTypes = new HashSet<Integer>();
        assignableTypes.add(typeIndex);

        for (ClassDef classDef : dex.classDefs()) {
            if (assignableTypes.contains(classDef.getSupertypeIndex())) {
                assignableTypes.add(classDef.getTypeIndex());
                continue;
            }

            for (int implemented : classDef.getInterfaces()) {
                if (assignableTypes.contains(implemented)) {
                    assignableTypes.add(classDef.getTypeIndex());
                    break;
                }
            }
        }

        return assignableTypes;
    }
}
