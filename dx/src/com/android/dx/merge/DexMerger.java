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
    private final DexBuffer dexWriter = new DexBuffer();
    private final DexBuffer.Section headerWriter;
    /** All IDs and definitions sections */
    private final DexBuffer.Section idsDefsWriter;
    private final DexBuffer.Section mapListWriter;
    private final DexBuffer.Section typeListWriter;
    private final DexBuffer.Section annotationSetRefListWriter;
    private final DexBuffer.Section annotationSetWriter;
    private final DexBuffer.Section classDataWriter;
    private final DexBuffer.Section codeWriter;
    private final DexBuffer.Section stringDataWriter;
    private final DexBuffer.Section debugInfoWriter;
    private final DexBuffer.Section annotationWriter;
    private final DexBuffer.Section encodedArrayWriter;
    private final DexBuffer.Section annotationsDirectoryWriter;
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
        aIndexMap = new IndexMap(dexWriter, aContents);
        bIndexMap = new IndexMap(dexWriter, bContents);
        aInstructionTransformer = new InstructionTransformer(aIndexMap);
        bInstructionTransformer = new InstructionTransformer(bIndexMap);

        headerWriter = dexWriter.appendSection(writerSizes.header, "header");
        idsDefsWriter = dexWriter.appendSection(writerSizes.idsDefs, "ids defs");

        contentsOut = dexWriter.getTableOfContents();
        contentsOut.dataOff = dexWriter.getLength();

        contentsOut.mapList.off = dexWriter.getLength();
        contentsOut.mapList.size = 1;
        mapListWriter = dexWriter.appendSection(writerSizes.mapList, "map list");

        contentsOut.typeLists.off = dexWriter.getLength();
        contentsOut.typeLists.size = 0;
        typeListWriter = dexWriter.appendSection(writerSizes.typeList, "type list");

        contentsOut.annotationSetRefLists.off = dexWriter.getLength();
        contentsOut.annotationSetRefLists.size = 0;
        annotationSetRefListWriter = dexWriter.appendSection(
                writerSizes.annotationSetRefList, "annotation set ref list");

        contentsOut.annotationSets.off = dexWriter.getLength();
        contentsOut.annotationSets.size = 0;
        annotationSetWriter = dexWriter.appendSection(writerSizes.annotationSet, "annotation set");

        contentsOut.classDatas.off = dexWriter.getLength();
        contentsOut.classDatas.size = 0;
        classDataWriter = dexWriter.appendSection(writerSizes.classData, "class data");

        contentsOut.codes.off = dexWriter.getLength();
        contentsOut.codes.size = 0;
        codeWriter = dexWriter.appendSection(writerSizes.code, "code");

        contentsOut.stringDatas.off = dexWriter.getLength();
        contentsOut.stringDatas.size = 0;
        stringDataWriter = dexWriter.appendSection(writerSizes.stringData, "string data");

        contentsOut.debugInfos.off = dexWriter.getLength();
        contentsOut.debugInfos.size = 0;
        debugInfoWriter = dexWriter.appendSection(writerSizes.debugInfo, "debug info");

        contentsOut.annotations.off = dexWriter.getLength();
        contentsOut.annotations.size = 0;
        annotationWriter = dexWriter.appendSection(writerSizes.annotation, "annotation");

        contentsOut.encodedArrays.off = dexWriter.getLength();
        contentsOut.encodedArrays.size = 0;
        encodedArrayWriter = dexWriter.appendSection(writerSizes.encodedArray, "encoded array");

        contentsOut.annotationsDirectories.off = dexWriter.getLength();
        contentsOut.annotationsDirectories.size = 0;
        annotationsDirectoryWriter = dexWriter.appendSection(
                writerSizes.annotationsDirectory, "annotations directory");

        dexWriter.noMoreSections();
        contentsOut.dataSize = dexWriter.getLength() - contentsOut.dataOff;
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
        mergeClassDefs();

        // write the header
        contentsOut.header.off = 0;
        contentsOut.header.size = 1;
        contentsOut.fileSize = dexWriter.getLength();
        contentsOut.computeSizesFromOffsets();
        contentsOut.writeHeader(headerWriter);
        contentsOut.writeMap(mapListWriter);

        // generate and write the hashes
        new DexHasher().writeHashes(dexWriter);

        return dexWriter;
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
            DexMerger compacter = new DexMerger(dexWriter, dexWriter, compactedSizes);
            result = compacter.mergeDexBuffers();
            System.out.printf("Result compacted from %.1fKiB to %.1fKiB to save %.1fKiB%n",
                    dexWriter.getLength() / 1024f,
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

            DexBuffer.Section inA = dexA.open(aSection.off);
            DexBuffer.Section inB = dexB.open(bSection.off);
            int aIndex = 0;
            int bIndex = 0;
            int outCount = 0;
            T a = null;
            T b = null;

            while (true) {
                int aOffset = inA.getPosition();
                if (a == null && aIndex < aSection.size) {
                    a = read(inA, aIndexMap, aIndex);
                }
                int bOffset = inB.getPosition();
                if (b == null && bIndex < bSection.size) {
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
        new IdMerger<String>(idsDefsWriter) {
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
                idsDefsWriter.writeInt(stringDataWriter.getPosition());
                stringDataWriter.writeStringData(value);
            }
        }.merge();
    }

    private void mergeTypeIds() {
        new IdMerger<Integer>(idsDefsWriter) {
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
                idsDefsWriter.writeInt(value);
            }
        }.merge();
    }

    private void mergeProtoIds() {
        new IdMerger<ProtoId>(idsDefsWriter) {
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
                value.writeTo(idsDefsWriter);
            }
        }.merge();
    }

    private void mergeFieldIds() {
        new IdMerger<FieldId>(idsDefsWriter) {
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
                value.writeTo(idsDefsWriter);
            }
        }.merge();
    }

    private void mergeMethodIds() {
        new IdMerger<MethodId>(idsDefsWriter) {
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
                methodId.writeTo(idsDefsWriter);
            }
        }.merge();
    }

    private void mergeTypeLists() {
        new IdMerger<TypeList>(typeListWriter) {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeLists;
            }

            @Override TypeList read(DexBuffer.Section in, IndexMap indexMap, int index) {
                return indexMap.adjustTypeList(in.readTypeList());
            }

            @Override void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.typeListOffsets.put(offset, typeListWriter.getPosition());
            }

            @Override void write(TypeList value) {
                typeListWriter.writeTypeList(value);
            }
        }.merge();
    }

    private void mergeClassDefs() {
        SortableType[] types = getSortedTypes();
        contentsOut.classDefs.off = idsDefsWriter.getPosition();
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
     * Reads a class_def_item beginning at {@code in} and writes the index and
     * data.
     */
    private void transformClassDef(DexBuffer in, ClassDef classDef, IndexMap indexMap) {
        idsDefsWriter.assertFourByteAligned();
        idsDefsWriter.writeInt(classDef.getTypeIndex());
        idsDefsWriter.writeInt(classDef.getAccessFlags());
        idsDefsWriter.writeInt(classDef.getSupertypeIndex());
        idsDefsWriter.writeInt(classDef.getInterfacesOffset());

        int sourceFileIndex = indexMap.adjustString(
                classDef.getSourceFileIndex()); // source file idx
        idsDefsWriter.writeInt(sourceFileIndex);

        int annotationsOff = classDef.getAnnotationsOffset();
        if (annotationsOff == 0) {
            idsDefsWriter.writeInt(0);
        } else {
            DexBuffer.Section annotationsIn = in.open(annotationsOff);
            annotationsDirectoryWriter.alignToFourBytes();
            idsDefsWriter.writeInt(annotationsDirectoryWriter.getPosition());
            transformAnnotations(annotationsIn, indexMap);
        }

        int classDataOff = classDef.getClassDataOffset();
        if (classDataOff == 0) {
            idsDefsWriter.writeInt(0);
        } else {
            idsDefsWriter.writeInt(classDataWriter.getPosition());
            ClassData classData = in.readClassData(classDef);
            transformClassData(in, classData, indexMap);
        }

        int staticValuesOff = classDef.getStaticValuesOffset();
        if (staticValuesOff == 0) {
            idsDefsWriter.writeInt(0);
        } else {
            DexBuffer.Section staticValuesIn = in.open(staticValuesOff);
            idsDefsWriter.writeInt(encodedArrayWriter.getPosition());
            transformStaticValues(staticValuesIn, indexMap);
        }
    }

    private void transformAnnotations(DexBuffer.Section in, IndexMap indexMap) {
        contentsOut.annotationsDirectories.size++;

        // TODO: retain annotations
        annotationsDirectoryWriter.assertFourByteAligned();
        in.readInt(); // class annotations off
        in.readInt(); // fields size
        in.readInt(); // annotated methods size
        in.readInt(); // annotated parameters size

        annotationsDirectoryWriter.writeInt(0);
        annotationsDirectoryWriter.writeInt(0);
        annotationsDirectoryWriter.writeInt(0);
        annotationsDirectoryWriter.writeInt(0);
    }

    private void transformClassData(DexBuffer in, ClassData classData, IndexMap indexMap) {
        contentsOut.classDatas.size++;

        ClassData.Field[] staticFields = classData.getStaticFields();
        ClassData.Field[] instanceFields = classData.getInstanceFields();
        ClassData.Method[] directMethods = classData.getDirectMethods();
        ClassData.Method[] virtualMethods = classData.getVirtualMethods();

        classDataWriter.writeUleb128(staticFields.length);
        classDataWriter.writeUleb128(instanceFields.length);
        classDataWriter.writeUleb128(directMethods.length);
        classDataWriter.writeUleb128(virtualMethods.length);

        transformFields(indexMap, staticFields);
        transformFields(indexMap, instanceFields);
        transformMethods(in, indexMap, directMethods);
        transformMethods(in, indexMap, virtualMethods);
    }

    private void transformFields(IndexMap indexMap, ClassData.Field[] fields) {
        int lastOutFieldIndex = 0;
        for (ClassData.Field field : fields) {
            int outFieldIndex = indexMap.adjustField(field.getFieldIndex());
            classDataWriter.writeUleb128(outFieldIndex - lastOutFieldIndex);
            lastOutFieldIndex = outFieldIndex;
            classDataWriter.writeUleb128(field.getAccessFlags());
        }
    }

    private void transformMethods(DexBuffer in, IndexMap indexMap, ClassData.Method[] methods) {
        int lastOutMethodIndex = 0;
        for (ClassData.Method method : methods) {
            int outMethodIndex = indexMap.adjustMethod(method.getMethodIndex());
            classDataWriter.writeUleb128(outMethodIndex - lastOutMethodIndex);
            lastOutMethodIndex = outMethodIndex;

            classDataWriter.writeUleb128(method.getAccessFlags());

            if (method.getCodeOffset() == 0) {
                classDataWriter.writeUleb128(0);
            } else {
                codeWriter.alignToFourBytes();
                classDataWriter.writeUleb128(codeWriter.getPosition());
                transformCode(in, in.readCode(method), indexMap);
            }
        }
    }

    private void transformCode(DexBuffer in, Code code, IndexMap indexMap) {
        contentsOut.codes.size++;
        codeWriter.assertFourByteAligned();

        codeWriter.writeShort(code.getRegistersSize());
        codeWriter.writeShort(code.getInsSize());
        codeWriter.writeShort(code.getOutsSize());

        Code.Try[] tries = code.getTries();
        codeWriter.writeShort((short) tries.length);

        // TODO: retain debug info
        // code.getDebugInfoOffset();
        codeWriter.writeInt(0);

        short[] instructions = code.getInstructions();
        InstructionTransformer transformer = (in == dexA)
                ? aInstructionTransformer
                : bInstructionTransformer;
        short[] newInstructions = transformer.transform(instructions);
        codeWriter.writeInt(newInstructions.length);
        codeWriter.write(newInstructions);

        if (tries.length > 0) {
            if (newInstructions.length % 2 == 1) {
                codeWriter.writeShort((short) 0); // padding
            }
            for (Code.Try tryItem : tries) {
                codeWriter.writeInt(tryItem.getStartAddress());
                codeWriter.writeShort(tryItem.getInstructionCount());
                codeWriter.writeShort(tryItem.getHandlerOffset());
            }
            Code.CatchHandler[] catchHandlers = code.getCatchHandlers();
            codeWriter.writeUleb128(catchHandlers.length);
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
            codeWriter.writeSleb128(-typeIndexes.length);
        } else {
            codeWriter.writeSleb128(typeIndexes.length);
        }

        for (int i = 0; i < typeIndexes.length; i++) {
            codeWriter.writeUleb128(indexMap.adjustType(typeIndexes[i]));
            codeWriter.writeUleb128(addresses[i]);
        }

        if (catchAllAddress != -1) {
            codeWriter.writeUleb128(catchAllAddress);
        }
    }

    private void transformStaticValues(DexBuffer.Section in, IndexMap indexMap) {
        contentsOut.encodedArrays.size++;
        new EncodedValueTransformer(indexMap, in, encodedArrayWriter).transformArray();
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
        private int annotationSetRefList = SizeOf.UINT;
        private int annotationSet = SizeOf.UINT;
        private int classData;
        private int code;
        private int stringData;
        private int debugInfo;
        private int annotation;
        private int encodedArray;
        private int annotationsDirectory;

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
            annotation += contents.annotations.byteCount;
            annotationsDirectory += contents.annotationsDirectories.byteCount;

            if (exact) {
                classData += contents.classDatas.byteCount;
                encodedArray += contents.encodedArrays.byteCount;
            } else {
                classData += (int) Math.ceil(contents.classDatas.byteCount * 1.34);
                encodedArray += (contents.encodedArrays.byteCount * 2);
            }
        }

        public void minusWaste(DexMerger dexMerger) {
            header -= dexMerger.headerWriter.remaining();
            idsDefs -= dexMerger.idsDefsWriter.remaining();
            mapList -= dexMerger.mapListWriter.remaining();
            typeList -= dexMerger.typeListWriter.remaining();
            annotationSetRefList -= dexMerger.annotationSetRefListWriter.remaining();
            annotationSet -= dexMerger.annotationSetWriter.remaining();
            classData -= dexMerger.classDataWriter.remaining();
            code -= dexMerger.codeWriter.remaining();
            stringData -= dexMerger.stringDataWriter.remaining();
            debugInfo -= dexMerger.debugInfoWriter.remaining();
            annotation -= dexMerger.annotationWriter.remaining();
            encodedArray -= dexMerger.encodedArrayWriter.remaining();
            annotationsDirectory -= dexMerger.annotationsDirectoryWriter.remaining();
        }

        public int size() {
            return header + idsDefs + mapList + typeList + annotationSetRefList + annotationSet
                    + classData + code + stringData + debugInfo + annotation + encodedArray
                    + annotationsDirectory;
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
