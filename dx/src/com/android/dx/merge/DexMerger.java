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

package com.android.dx.merge;

import com.android.dx.dex.SizeOf;
import com.android.dx.dex.TableOfContents;
import com.android.dx.io.ClassData;
import com.android.dx.io.ClassDef;
import com.android.dx.io.Code;
import com.android.dx.io.DexBuffer;
import com.android.dx.io.DexHasher;
import com.android.dx.io.FieldId;
import com.android.dx.io.MethodId;
import com.android.dx.io.ProtoId;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Combine two dex files into one.
 */
public final class DexMerger {
    private final WriterSizes writerSizes;
    private final DexBuffer dexOut = new DexBuffer();
    private final DexBuffer.Section headerOut;
    /** All IDs and definitions sections */
    private final DexBuffer.Section idsDefsOut;
    private final DexBuffer.Section mapListOut;
    private final DexBuffer.Section typeListOut;
    private final DexBuffer.Section classDataOut;
    private final DexBuffer.Section codeOut;
    private final DexBuffer.Section stringDataOut;
    private final DexBuffer.Section debugInfoOut;
    private final DexBuffer.Section encodedArrayOut;

    /** annotations directory on a type */
    private final DexBuffer.Section annotationsDirectoryOut;
    /** sets of annotations on a member, parameter or type */
    private final DexBuffer.Section annotationSetOut;
    /** parameter lists */
    private final DexBuffer.Section annotationSetRefListOut;
    /** individual annotations, each containing zero or more fields */
    private final DexBuffer.Section annotationOut;

    private final TableOfContents contentsOut;
    private final DexBuffer dexA;
    private final DexBuffer dexB;
    private final IndexMap aIndexMap;
    private final IndexMap bIndexMap;
    private final InstructionTransformer aInstructionTransformer;
    private final InstructionTransformer bInstructionTransformer;

    /** minimum number of wasted bytes before it's worthwhile to compact the result */
    private int compactWasteThreshold = 1024 * 1024; // 1MiB
    /** minimum number of wasted bytes before it's worthwhile to emit a warning. */
    private final int warnWasteThreshold = 100 * 1024; // 100KiB

    public DexMerger(DexBuffer dexA, DexBuffer dexB) throws IOException {
        this(dexA, dexB, new WriterSizes(dexA, dexB));
    }

    private DexMerger(DexBuffer dexA, DexBuffer dexB, WriterSizes writerSizes) throws IOException {
        this.dexA = dexA;
        this.dexB = dexB;
        this.writerSizes = writerSizes;

        TableOfContents aContents = dexA.getTableOfContents();
        TableOfContents bContents = dexB.getTableOfContents();
        aIndexMap = new IndexMap(dexOut, aContents);
        bIndexMap = new IndexMap(dexOut, bContents);
        aInstructionTransformer = new InstructionTransformer(aIndexMap);
        bInstructionTransformer = new InstructionTransformer(bIndexMap);

        headerOut = dexOut.appendSection(writerSizes.header, "header");
        idsDefsOut = dexOut.appendSection(writerSizes.idsDefs, "ids defs");

        contentsOut = dexOut.getTableOfContents();
        contentsOut.dataOff = dexOut.getLength();

        contentsOut.mapList.off = dexOut.getLength();
        contentsOut.mapList.size = 1;
        mapListOut = dexOut.appendSection(writerSizes.mapList, "map list");

        contentsOut.typeLists.off = dexOut.getLength();
        contentsOut.typeLists.size = 0;
        typeListOut = dexOut.appendSection(writerSizes.typeList, "type list");

        contentsOut.annotationSetRefLists.off = dexOut.getLength();
        contentsOut.annotationSetRefLists.size = 0;
        annotationSetRefListOut = dexOut.appendSection(
                writerSizes.annotationsSetRefList, "annotation set ref list");

        contentsOut.annotationSets.off = dexOut.getLength();
        contentsOut.annotationSets.size = 0;
        annotationSetOut = dexOut.appendSection(
                writerSizes.annotationsSet, "annotation sets");

        contentsOut.classDatas.off = dexOut.getLength();
        contentsOut.classDatas.size = 0;
        classDataOut = dexOut.appendSection(writerSizes.classData, "class data");

        contentsOut.codes.off = dexOut.getLength();
        contentsOut.codes.size = 0;
        codeOut = dexOut.appendSection(writerSizes.code, "code");

        contentsOut.stringDatas.off = dexOut.getLength();
        contentsOut.stringDatas.size = 0;
        stringDataOut = dexOut.appendSection(writerSizes.stringData, "string data");

        contentsOut.debugInfos.off = dexOut.getLength();
        contentsOut.debugInfos.size = 0;
        debugInfoOut = dexOut.appendSection(writerSizes.debugInfo, "debug info");

        contentsOut.annotations.off = dexOut.getLength();
        contentsOut.annotations.size = 0;
        annotationOut = dexOut.appendSection(writerSizes.annotation, "annotation");

        contentsOut.encodedArrays.off = dexOut.getLength();
        contentsOut.encodedArrays.size = 0;
        encodedArrayOut = dexOut.appendSection(writerSizes.encodedArray, "encoded array");

        contentsOut.annotationsDirectories.off = dexOut.getLength();
        contentsOut.annotationsDirectories.size = 0;
        annotationsDirectoryOut = dexOut.appendSection(
                writerSizes.annotationsDirectory, "annotations directory");

        dexOut.noMoreSections();
        contentsOut.dataSize = dexOut.getLength() - contentsOut.dataOff;
    }

    public void setCompactWasteThreshold(int compactWasteThreshold) {
        this.compactWasteThreshold = compactWasteThreshold;
    }

    private DexBuffer mergeDexBuffers() throws IOException {
        mergeStringIds();
        mergeTypeIds();
        mergeTypeLists();
        mergeProtoIds();
        mergeFieldIds();
        mergeMethodIds();
        unionAnnotations();
        mergeClassDefs();

        // write the header
        contentsOut.header.off = 0;
        contentsOut.header.size = 1;
        contentsOut.fileSize = dexOut.getLength();
        contentsOut.computeSizesFromOffsets();
        contentsOut.writeHeader(headerOut);
        contentsOut.writeMap(mapListOut);

        // generate and write the hashes
        new DexHasher().writeHashes(dexOut);

        return dexOut;
    }

    public DexBuffer merge() throws IOException {
        long start = System.nanoTime();
        DexBuffer result = mergeDexBuffers();

        /*
         * We use pessimistic sizes when merging dex files. If those sizes
         * result in too many bytes wasted, compact the result. To compact,
         * simply merge the result with itself.
         */
        WriterSizes compactedSizes = writerSizes.clone();
        compactedSizes.minusWaste(this);
        int wastedByteCount = writerSizes.size() - compactedSizes.size();
        if (wastedByteCount >  + compactWasteThreshold) {
            DexMerger compacter = new DexMerger(dexOut, dexOut, compactedSizes);
            result = compacter.mergeDexBuffers();
            System.out.printf("Result compacted from %.1fKiB to %.1fKiB to save %.1fKiB%n",
                    dexOut.getLength() / 1024f,
                    result.getLength() / 1024f,
                    wastedByteCount / 1024f);
        } else if (wastedByteCount >= warnWasteThreshold) {
            System.out.printf("Result includes %.1fKiB of wasted space",
                    wastedByteCount / 1024f);
        }

        long elapsed = System.nanoTime() - start;
        System.out.printf("Merged dex A (%d defs/%.1fKiB) with dex B "
                + "(%d defs/%.1fKiB). Result is %d defs/%.1fKiB. Took %.1fs%n",
                dexA.getTableOfContents().classDefs.size,
                dexA.getLength() / 1024f,
                dexB.getTableOfContents().classDefs.size,
                dexB.getLength() / 1024f,
                result.getTableOfContents().classDefs.size,
                result.getLength() / 1024f,
                elapsed / 1000000000f);

        return result;
    }

    /**
     * Reads an IDs section of two dex files and writes an IDs section of a
     * merged dex file. Populates maps from old to new indices in the process.
     */
    abstract class IdMerger<T extends Comparable<T>> {
        private final DexBuffer.Section out;

        protected IdMerger(DexBuffer.Section out) {
            this.out = out;
        }

        public final void merge() {
            TableOfContents.Section aSection = getSection(dexA.getTableOfContents());
            TableOfContents.Section bSection = getSection(dexB.getTableOfContents());
            getSection(contentsOut).off = out.getPosition();

            DexBuffer.Section inA = aSection.exists() ? dexA.open(aSection.off) : null;
            DexBuffer.Section inB = bSection.exists() ? dexB.open(bSection.off) : null;
            int aIndex = 0;
            int bIndex = 0;
            int outCount = 0;
            T a = null;
            T b = null;

            while (true) {
                int aOffset = -1;
                if (a == null && aIndex < aSection.size) {
                    aOffset = inA.getPosition();
                    a = read(inA, aIndexMap, aIndex);
                }
                int bOffset = -1;
                if (b == null && bIndex < bSection.size) {
                    bOffset = inB.getPosition();
                    b = read(inB, bIndexMap, bIndex);
                }

                // Write the smaller of a and b. If they're equal, write only once
                boolean advanceA;
                boolean advanceB;
                if (a != null && b != null) {
                    int compare = a.compareTo(b);
                    advanceA = compare <= 0;
                    advanceB = compare >= 0;
                } else {
                    advanceA = (a != null);
                    advanceB = (b != null);
                }

                T toWrite = null;
                if (advanceA) {
                    toWrite = a;
                    updateIndex(aOffset, aIndexMap, aIndex++, outCount);
                    a = null;
                }
                if (advanceB) {
                    toWrite = b;
                    updateIndex(bOffset, bIndexMap, bIndex++, outCount);
                    b = null;
                }
                if (toWrite == null) {
                    break; // advanceA == false && advanceB == false
                }
                write(toWrite);
                outCount++;
            }

            getSection(contentsOut).size = outCount;
        }

        abstract TableOfContents.Section getSection(TableOfContents tableOfContents);
        abstract T read(DexBuffer.Section in, IndexMap indexMap, int index);
        abstract void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex);
        abstract void write(T value);
    }

    private void mergeStringIds() {
        new IdMerger<String>(idsDefsOut) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.stringIds;
            }

            @Override String read(DexBuffer.Section in, IndexMap indexMap, int index) {
                return in.readString();
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.stringIds[oldIndex] = newIndex;
            }

            @Override void write(String value) {
                contentsOut.stringDatas.size++;
                idsDefsOut.writeInt(stringDataOut.getPosition());
                stringDataOut.writeStringData(value);
            }
        }.merge();
    }

    private void mergeTypeIds() {
        new IdMerger<Integer>(idsDefsOut) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeIds;
            }

            @Override Integer read(DexBuffer.Section in, IndexMap indexMap, int index) {
                int stringIndex = in.readInt();
                return indexMap.adjustString(stringIndex);
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.typeIds[oldIndex] = (short) newIndex;
            }

            @Override void write(Integer value) {
                idsDefsOut.writeInt(value);
            }
        }.merge();
    }

    private void mergeTypeLists() {
        new IdMerger<TypeList>(typeListOut) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeLists;
            }

            @Override TypeList read(DexBuffer.Section in, IndexMap indexMap, int index) {
                return indexMap.adjustTypeList(in.readTypeList());
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.typeListOffsets.put(offset, typeListOut.getPosition());
            }

            @Override void write(TypeList value) {
                typeListOut.writeTypeList(value);
            }
        }.merge();
    }

    private void mergeProtoIds() {
        new IdMerger<ProtoId>(idsDefsOut) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.protoIds;
            }

            @Override ProtoId read(DexBuffer.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readProtoId());
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.protoIds[oldIndex] = (short) newIndex;
            }

            @Override void write(ProtoId value) {
                value.writeTo(idsDefsOut);
            }
        }.merge();
    }

    private void mergeFieldIds() {
        new IdMerger<FieldId>(idsDefsOut) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.fieldIds;
            }

            @Override FieldId read(DexBuffer.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readFieldId());
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.fieldIds[oldIndex] = (short) newIndex;
            }

            @Override void write(FieldId value) {
                value.writeTo(idsDefsOut);
            }
        }.merge();
    }

    private void mergeMethodIds() {
        new IdMerger<MethodId>(idsDefsOut) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.methodIds;
            }

            @Override MethodId read(DexBuffer.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readMethodId());
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.methodIds[oldIndex] = (short) newIndex;
            }

            @Override void write(MethodId methodId) {
                methodId.writeTo(idsDefsOut);
            }
        }.merge();
    }

    private void mergeClassDefs() {
        SortableType[] types = getSortedTypes();
        contentsOut.classDefs.off = idsDefsOut.getPosition();
        contentsOut.classDefs.size = types.length;

        for (SortableType type : types) {
            DexBuffer in = type.getBuffer();
            IndexMap indexMap = (in == dexA) ? aIndexMap : bIndexMap;
            transformClassDef(in, type.getClassDef(), indexMap);
        }
    }

    /**
     * Returns the union of classes from both files, sorted in order such that
     * a class is always preceded by its supertype and implemented interfaces.
     */
    private SortableType[] getSortedTypes() {
        // size is pessimistic; doesn't include arrays
        SortableType[] sortableTypes = new SortableType[contentsOut.typeIds.size];
        readSortableTypes(sortableTypes, dexA, aIndexMap);
        readSortableTypes(sortableTypes, dexB, bIndexMap);

        /*
         * Populate the depths of each sortable type. This makes D iterations
         * through all N types, where 'D' is the depth of the deepest type. For
         * example, the deepest class in libcore is Xalan's KeyIterator, which
         * is 11 types deep.
         */
        while (true) {
            boolean allDone = true;
            for (SortableType sortableType : sortableTypes) {
                if (sortableType != null && !sortableType.isDepthAssigned()) {
                    allDone &= sortableType.tryAssignDepth(sortableTypes);
                }
            }
            if (allDone) {
                break;
            }
        }

        // Now that all types have depth information, the result can be sorted
        Arrays.sort(sortableTypes, SortableType.NULLS_LAST_ORDER);

        // Strip nulls from the end
        int firstNull = Arrays.asList(sortableTypes).indexOf(null);
        return firstNull != -1
                ? Arrays.copyOfRange(sortableTypes, 0, firstNull)
                : sortableTypes;
    }

    /**
     * Reads just enough data on each class so that we can sort it and then find
     * it later.
     */
    private void readSortableTypes(SortableType[] sortableTypes, DexBuffer buffer,
            IndexMap indexMap) {
        for (ClassDef classDef : buffer.classDefs()) {
            SortableType sortableType = indexMap.adjust(new SortableType(buffer, classDef));
            int t = sortableType.getTypeIndex();
            if (sortableTypes[t] == null) {
                sortableTypes[t] = sortableType;
            }
        }
    }

    /**
     * Copy annotation sets from each input to the output.
     *
     * TODO: this may write multiple copies of the same annotation.
     * This should shrink the output by merging rather than unioning
     */
    private void unionAnnotations() {
        transformAnnotationSets(dexA, aIndexMap);
        transformAnnotationSets(dexB, bIndexMap);
        transformAnnotationDirectories(dexA, aIndexMap);
        transformAnnotationDirectories(dexB, bIndexMap);
    }

    private void transformAnnotationSets(DexBuffer in, IndexMap indexMap) {
        TableOfContents.Section section = in.getTableOfContents().annotationSets;
        if (section.exists()) {
            DexBuffer.Section setIn = in.open(section.off);
            for (int i = 0; i < section.size; i++) {
                transformAnnotationSet(in, indexMap, setIn);
            }
        }
    }

    private void transformAnnotationDirectories(DexBuffer in, IndexMap indexMap) {
        TableOfContents.Section section = in.getTableOfContents().annotationsDirectories;
        if (section.exists()) {
            DexBuffer.Section directoryIn = in.open(section.off);
            for (int i = 0; i < section.size; i++) {
                transformAnnotationDirectory(in, directoryIn, indexMap);
            }
        }
    }

    /**
     * Reads a class_def_item beginning at {@code in} and writes the index and
     * data.
     */
    private void transformClassDef(DexBuffer in, ClassDef classDef, IndexMap indexMap) {
        idsDefsOut.assertFourByteAligned();
        idsDefsOut.writeInt(classDef.getTypeIndex());
        idsDefsOut.writeInt(classDef.getAccessFlags());
        idsDefsOut.writeInt(classDef.getSupertypeIndex());
        idsDefsOut.writeInt(classDef.getInterfacesOffset());

        int sourceFileIndex = indexMap.adjustString(classDef.getSourceFileIndex());
        idsDefsOut.writeInt(sourceFileIndex);

        int annotationsOff = classDef.getAnnotationsOffset();
        idsDefsOut.writeInt(indexMap.adjustAnnotationDirectory(annotationsOff));

        int classDataOff = classDef.getClassDataOffset();
        if (classDataOff == 0) {
            idsDefsOut.writeInt(0);
        } else {
            idsDefsOut.writeInt(classDataOut.getPosition());
            ClassData classData = in.readClassData(classDef);
            transformClassData(in, classData, indexMap);
        }

        int staticValuesOff = classDef.getStaticValuesOffset();
        if (staticValuesOff == 0) {
            idsDefsOut.writeInt(0);
        } else {
            DexBuffer.Section staticValuesIn = in.open(staticValuesOff);
            idsDefsOut.writeInt(encodedArrayOut.getPosition());
            transformStaticValues(staticValuesIn, indexMap);
        }
    }

    /**
     * Transform all annotations on a class.
     */
    private void transformAnnotationDirectory(
            DexBuffer in, DexBuffer.Section directoryIn, IndexMap indexMap) {
        contentsOut.annotationsDirectories.size++;
        annotationsDirectoryOut.assertFourByteAligned();
        indexMap.annotationDirectoryOffsets.put(
                directoryIn.getPosition(), annotationsDirectoryOut.getPosition());

        int classAnnotationsOffset = indexMap.adjustAnnotationSet(directoryIn.readInt());
        annotationsDirectoryOut.writeInt(classAnnotationsOffset);

        int fieldsSize = directoryIn.readInt();
        annotationsDirectoryOut.writeInt(fieldsSize);

        int methodsSize = directoryIn.readInt();
        annotationsDirectoryOut.writeInt(methodsSize);

        int parameterListSize = directoryIn.readInt();
        annotationsDirectoryOut.writeInt(parameterListSize);

        for (int i = 0; i < fieldsSize; i++) {
            // field index
            annotationsDirectoryOut.writeInt(indexMap.adjustField(directoryIn.readInt()));

            // annotations offset
            annotationsDirectoryOut.writeInt(indexMap.adjustAnnotationSet(directoryIn.readInt()));
        }

        for (int i = 0; i < methodsSize; i++) {
            // method index
            annotationsDirectoryOut.writeInt(indexMap.adjustMethod(directoryIn.readInt()));

            // annotation set offset
            annotationsDirectoryOut.writeInt(
                    indexMap.adjustAnnotationSet(directoryIn.readInt()));
        }

        for (int i = 0; i < parameterListSize; i++) {
            contentsOut.annotationSetRefLists.size++;
            annotationSetRefListOut.assertFourByteAligned();

            // method index
            annotationsDirectoryOut.writeInt(indexMap.adjustMethod(directoryIn.readInt()));

            // annotations offset
            annotationsDirectoryOut.writeInt(annotationSetRefListOut.getPosition());
            DexBuffer.Section refListIn = in.open(directoryIn.readInt());

            // parameters
            int parameterCount = refListIn.readInt();
            annotationSetRefListOut.writeInt(parameterCount);
            for (int p = 0; p < parameterCount; p++) {
                annotationSetRefListOut.writeInt(indexMap.adjustAnnotationSet(refListIn.readInt()));
            }
        }
    }

    /**
     * Transform all annotations on a single type, member or parameter.
     */
    private void transformAnnotationSet(DexBuffer in, IndexMap indexMap, DexBuffer.Section setIn) {
        contentsOut.annotationSets.size++;
        annotationSetOut.assertFourByteAligned();
        indexMap.annotationSetOffsets.put(setIn.getPosition(), annotationSetOut.getPosition());

        int size = setIn.readInt();
        annotationSetOut.writeInt(size);

        for (int j = 0; j < size; j++) {
            // annotation offset
            annotationSetOut.writeInt(annotationOut.getPosition());
            transformAnnotation(in.open(setIn.readInt()), indexMap);
        }
    }

    /**
     * Transform one annotation, which may have multiple fields.
     */
    private void transformAnnotation(DexBuffer.Section in, IndexMap indexMap) {
        contentsOut.annotations.size++;

        // visibility
        annotationOut.writeByte(in.readByte());

        // type index
        annotationOut.writeUleb128((int) indexMap.adjustType(in.readUleb128()));

        // size
        int size = in.readUleb128();
        annotationOut.writeUleb128(size);

        // elements
        for (int i = 0; i < size; i++) {
            annotationOut.writeUleb128(indexMap.adjustString(in.readUleb128())); // name
            new EncodedValueTransformer(indexMap, in, annotationOut).transformValue(); // value
        }
    }

    private void transformClassData(DexBuffer in, ClassData classData, IndexMap indexMap) {
        contentsOut.classDatas.size++;

        ClassData.Field[] staticFields = classData.getStaticFields();
        ClassData.Field[] instanceFields = classData.getInstanceFields();
        ClassData.Method[] directMethods = classData.getDirectMethods();
        ClassData.Method[] virtualMethods = classData.getVirtualMethods();

        classDataOut.writeUleb128(staticFields.length);
        classDataOut.writeUleb128(instanceFields.length);
        classDataOut.writeUleb128(directMethods.length);
        classDataOut.writeUleb128(virtualMethods.length);

        transformFields(indexMap, staticFields);
        transformFields(indexMap, instanceFields);
        transformMethods(in, indexMap, directMethods);
        transformMethods(in, indexMap, virtualMethods);
    }

    private void transformFields(IndexMap indexMap, ClassData.Field[] fields) {
        int lastOutFieldIndex = 0;
        for (ClassData.Field field : fields) {
            int outFieldIndex = indexMap.adjustField(field.getFieldIndex());
            classDataOut.writeUleb128(outFieldIndex - lastOutFieldIndex);
            lastOutFieldIndex = outFieldIndex;
            classDataOut.writeUleb128(field.getAccessFlags());
        }
    }

    private void transformMethods(DexBuffer in, IndexMap indexMap, ClassData.Method[] methods) {
        int lastOutMethodIndex = 0;
        for (ClassData.Method method : methods) {
            int outMethodIndex = indexMap.adjustMethod(method.getMethodIndex());
            classDataOut.writeUleb128(outMethodIndex - lastOutMethodIndex);
            lastOutMethodIndex = outMethodIndex;

            classDataOut.writeUleb128(method.getAccessFlags());

            if (method.getCodeOffset() == 0) {
                classDataOut.writeUleb128(0);
            } else {
                codeOut.alignToFourBytes();
                classDataOut.writeUleb128(codeOut.getPosition());
                transformCode(in, in.readCode(method), indexMap);
            }
        }
    }

    private void transformCode(DexBuffer in, Code code, IndexMap indexMap) {
        contentsOut.codes.size++;
        codeOut.assertFourByteAligned();

        codeOut.writeShort(code.getRegistersSize());
        codeOut.writeShort(code.getInsSize());
        codeOut.writeShort(code.getOutsSize());

        Code.Try[] tries = code.getTries();
        codeOut.writeShort((short) tries.length);

        // TODO: retain debug info
        // code.getDebugInfoOffset();
        codeOut.writeInt(0);

        short[] instructions = code.getInstructions();
        InstructionTransformer transformer = (in == dexA)
                ? aInstructionTransformer
                : bInstructionTransformer;
        short[] newInstructions = transformer.transform(instructions);
        codeOut.writeInt(newInstructions.length);
        codeOut.write(newInstructions);

        if (tries.length > 0) {
            if (newInstructions.length % 2 == 1) {
                codeOut.writeShort((short) 0); // padding
            }
            for (Code.Try tryItem : tries) {
                codeOut.writeInt(tryItem.getStartAddress());
                codeOut.writeShort(tryItem.getInstructionCount());
                codeOut.writeShort(tryItem.getHandlerOffset());
            }
            Code.CatchHandler[] catchHandlers = code.getCatchHandlers();
            codeOut.writeUleb128(catchHandlers.length);
            for (Code.CatchHandler catchHandler : catchHandlers) {
                transformEncodedCatchHandler(catchHandler, indexMap);
            }
        }
    }

    private void transformEncodedCatchHandler(Code.CatchHandler catchHandler, IndexMap indexMap) {
        int catchAllAddress = catchHandler.getCatchAllAddress();
        int[] typeIndexes = catchHandler.getTypeIndexes();
        int[] addresses = catchHandler.getAddresses();

        if (catchAllAddress != -1) {
            codeOut.writeSleb128(-typeIndexes.length);
        } else {
            codeOut.writeSleb128(typeIndexes.length);
        }

        for (int i = 0; i < typeIndexes.length; i++) {
            codeOut.writeUleb128(indexMap.adjustType(typeIndexes[i]));
            codeOut.writeUleb128(addresses[i]);
        }

        if (catchAllAddress != -1) {
            codeOut.writeUleb128(catchAllAddress);
        }
    }

    private void transformStaticValues(DexBuffer.Section in, IndexMap indexMap) {
        contentsOut.encodedArrays.size++;
        new EncodedValueTransformer(indexMap, in, encodedArrayOut).transformArray();
    }

    /**
     * Byte counts for the sections written when creating a dex. Target sizes
     * are defined in one of two ways:
     * <ul>
     * <li>By pessimistically guessing how large the union of dex files will be.
     *     We're pessimistic because we can't predict the amount of duplication
     *     between dex files, nor can we predict the length of ULEB-encoded
     *     offsets or indices.
     * <li>By exactly measuring an existing dex.
     * </ul>
     */
    private static class WriterSizes implements Cloneable {
        private int header = SizeOf.HEADER_ITEM;
        private int idsDefs;
        private int mapList;
        private int typeList;
        private int classData;
        private int code;
        private int stringData;
        private int debugInfo;
        private int encodedArray;
        private int annotationsDirectory;
        private int annotationsSet;
        private int annotationsSetRefList;
        private int annotation;

        /**
         * Compute sizes for merging a and b.
         */
        public WriterSizes(DexBuffer a, DexBuffer b) {
            plus(a.getTableOfContents(), false);
            plus(b.getTableOfContents(), false);
        }

        @Override public WriterSizes clone() {
            try {
                return (WriterSizes) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        public void plus(TableOfContents contents, boolean exact) {
            idsDefs += contents.stringIds.size * SizeOf.STRING_ID_ITEM
                    + contents.typeIds.size * SizeOf.TYPE_ID_ITEM
                    + contents.protoIds.size * SizeOf.PROTO_ID_ITEM
                    + contents.fieldIds.size * SizeOf.MEMBER_ID_ITEM
                    + contents.methodIds.size * SizeOf.MEMBER_ID_ITEM
                    + contents.classDefs.size * SizeOf.CLASS_DEF_ITEM;
            mapList = SizeOf.UINT + (contents.sections.length * SizeOf.MAP_ITEM);
            typeList += contents.typeLists.byteCount;
            code += contents.codes.byteCount;
            stringData += contents.stringDatas.byteCount;
            debugInfo += contents.debugInfos.byteCount;
            annotationsDirectory += contents.annotationsDirectories.byteCount;
            annotationsSet += contents.annotationSets.byteCount;
            annotationsSetRefList += contents.annotationSetRefLists.byteCount;

            if (exact) {
                classData += contents.classDatas.byteCount;
                encodedArray += contents.encodedArrays.byteCount;
                annotation += contents.annotations.byteCount;
            } else {
                classData += (int) Math.ceil(contents.classDatas.byteCount * 1.34);
                encodedArray += contents.encodedArrays.byteCount * 2;
                annotation += contents.annotations.byteCount * 2;
            }
        }

        public void minusWaste(DexMerger dexMerger) {
            header -= dexMerger.headerOut.remaining();
            idsDefs -= dexMerger.idsDefsOut.remaining();
            mapList -= dexMerger.mapListOut.remaining();
            typeList -= dexMerger.typeListOut.remaining();
            classData -= dexMerger.classDataOut.remaining();
            code -= dexMerger.codeOut.remaining();
            stringData -= dexMerger.stringDataOut.remaining();
            debugInfo -= dexMerger.debugInfoOut.remaining();
            encodedArray -= dexMerger.encodedArrayOut.remaining();
            annotationsDirectory -= dexMerger.annotationsDirectoryOut.remaining();
            annotationsSet -= dexMerger.annotationSetOut.remaining();
            annotationsSetRefList -= dexMerger.annotationSetRefListOut.remaining();
            annotation -= dexMerger.annotationOut.remaining();
        }

        public int size() {
            return header + idsDefs + mapList + typeList + classData + code + stringData + debugInfo
                    + encodedArray + annotationsDirectory + annotationsSet + annotationsSetRefList
                    + annotation;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            printUsage();
            return;
        }

        DexBuffer dexA = new DexBuffer();
        dexA.loadFrom(new File(args[1]));
        DexBuffer dexB = new DexBuffer();
        dexB.loadFrom(new File(args[2]));

        DexBuffer merged = new DexMerger(dexA, dexB).merge();
        merged.writeTo(new File(args[0]));
    }

    private static void printUsage() {
        System.out.println("Usage: DexMerger <out.dex> <a.dex> <b.dex>");
        System.out.println();
        System.out.println("If both a and b define the same classes, a's copy will be used.");
    }
}
