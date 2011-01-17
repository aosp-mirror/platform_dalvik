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
import java.util.logging.Logger;

/**
 * Combine two dex files into one.
 */
public final class DexMerger {
    private static final Logger logger = Logger.getLogger(DexMerger.class.getName());

    private final File dexOut;
    private final DexBuffer dexWriter = new DexBuffer();
    private final DexBuffer.Section headerWriter;
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

    private final DexBuffer dexA = new DexBuffer();
    private final DexBuffer dexB = new DexBuffer();
    private final IndexMap aIndexMap;
    private final IndexMap bIndexMap;
    private final InstructionTransformer aInstructionTransformer;
    private final InstructionTransformer bInstructionTransformer;

    public DexMerger(File dexOut, File a, File b) throws IOException {
        if (!a.exists() || !b.exists()) {
            throw new IllegalArgumentException();
        }

        this.dexOut = dexOut;

        dexA.loadFrom(a);
        dexB.loadFrom(b);

        TableOfContents aContents = dexA.getTableOfContents();
        TableOfContents bContents = dexB.getTableOfContents();

        aIndexMap = new IndexMap(dexWriter, aContents);
        bIndexMap = new IndexMap(dexWriter, bContents);
        aInstructionTransformer = new InstructionTransformer(aIndexMap);
        bInstructionTransformer = new InstructionTransformer(bIndexMap);

        headerWriter = dexWriter.appendSection(SizeOf.HEADER_ITEM, "header");

        // All IDs and definitions sections
        int idsDefsMaxSize
                = (aContents.stringIds.size + bContents.stringIds.size) * SizeOf.STRING_ID_ITEM
                + (aContents.typeIds.size + bContents.typeIds.size) * SizeOf.TYPE_ID_ITEM
                + (aContents.protoIds.size + bContents.protoIds.size) * SizeOf.PROTO_ID_ITEM
                + (aContents.fieldIds.size + bContents.fieldIds.size) * SizeOf.MEMBER_ID_ITEM
                + (aContents.methodIds.size + bContents.methodIds.size) * SizeOf.MEMBER_ID_ITEM
                + (aContents.classDefs.size + bContents.classDefs.size) * SizeOf.CLASS_DEF_ITEM;
        idsDefsWriter = dexWriter.appendSection(idsDefsMaxSize, "ids defs");

        // data section
        contentsOut = dexWriter.getTableOfContents();
        contentsOut.dataOff = dexWriter.getLength();

        contentsOut.mapList.off = dexWriter.getLength();
        contentsOut.mapList.size = 1;
        mapListWriter = dexWriter.appendSection(SizeOf.UINT
                + (contentsOut.sections.length * SizeOf.MAP_ITEM), "map list");

        /*
         * TODO: several of these sections are far too large than they need to be.
         *
         * typeList: we don't deduplicate identical type lists. This should be fixed.
         *
         * classDataWriter: uleb references to code items are larger than
         *     expected. We should use old & new code_item section offsets to
         *     pick an appropriate blow up size
         *
         * stringDataWriter: this shouldn't have to be larger, but it is
         *
         * encodedArrayWriter: this shouldn't have to be larger, but it is
         */

        contentsOut.typeLists.off = dexWriter.getLength();
        contentsOut.typeLists.size = 0;
        int maxTypeListBytes = aContents.typeLists.byteCount + bContents.typeLists.byteCount;
        typeListWriter = dexWriter.appendSection(maxTypeListBytes * 5, "type list");

        contentsOut.annotationSetRefLists.off = dexWriter.getLength();
        contentsOut.annotationSetRefLists.size = 0;
        annotationSetRefListWriter = dexWriter.appendSection(SizeOf.UINT, "annotation set ref list");

        contentsOut.annotationSets.off = dexWriter.getLength();
        contentsOut.annotationSets.size = 0;
        annotationSetWriter = dexWriter.appendSection(SizeOf.UINT, "annotation set");

        contentsOut.classDatas.off = dexWriter.getLength();
        contentsOut.classDatas.size = 0;
        int maxClassDataBytes = aContents.classDatas.byteCount + bContents.classDatas.byteCount;
        classDataWriter = dexWriter.appendSection(maxClassDataBytes * 2, "class data");

        contentsOut.codes.off = dexWriter.getLength();
        contentsOut.codes.size = 0;
        int maxCodeBytes = aContents.codes.byteCount + bContents.codes.byteCount;
        codeWriter = dexWriter.appendSection(maxCodeBytes, "code");

        contentsOut.stringDatas.off = dexWriter.getLength();
        contentsOut.stringDatas.size = 0;
        int maxStringDataBytes = aContents.stringDatas.byteCount
                + bContents.stringDatas.byteCount;
        stringDataWriter = dexWriter.appendSection(maxStringDataBytes * 2, "string data");

        contentsOut.debugInfos.off = dexWriter.getLength();
        contentsOut.debugInfos.size = 0;
        int maxDebugInfoBytes = aContents.debugInfos.byteCount + bContents.debugInfos.byteCount;
        debugInfoWriter = dexWriter.appendSection(maxDebugInfoBytes, "debug info");

        contentsOut.annotations.off = dexWriter.getLength();
        contentsOut.annotations.size = 0;
        int maxAnnotationBytes = aContents.annotations.byteCount
                + bContents.annotations.byteCount;
        annotationWriter = dexWriter.appendSection(maxAnnotationBytes, "annotation");

        contentsOut.encodedArrays.off = dexWriter.getLength();
        contentsOut.encodedArrays.size = 0;
        int maxEncodedArrayBytes = aContents.encodedArrays.byteCount
                + bContents.encodedArrays.byteCount;
        encodedArrayWriter = dexWriter.appendSection(
                maxEncodedArrayBytes * 2, "encoded array");

        contentsOut.annotationsDirectories.off = dexWriter.getLength();
        contentsOut.annotationsDirectories.size = 0;
        int maxAnnotationsDirectoryBytes = aContents.annotationsDirectories.byteCount
                + bContents.annotationsDirectories.byteCount;
        annotationsDirectoryWriter = dexWriter.appendSection(
                maxAnnotationsDirectoryBytes, "annotations");

        dexWriter.noMoreSections();
        contentsOut.dataSize = dexWriter.getLength() - contentsOut.dataOff;
    }

    public void merge() throws IOException {
        long start = System.nanoTime();

        mergeStringIds();
        mergeTypeIds();
        mergeProtoIds();
        mergeFieldIds();
        mergeMethodIds();
        mergeClassDefs();

        // write the header
        contentsOut.header.off = 0;
        contentsOut.header.size = 1;
        contentsOut.fileSize = dexWriter.getLength();
        contentsOut.writeHeader(headerWriter);
        contentsOut.writeMap(mapListWriter);

        // close (and flush) the result, then reopen to generate and write the hashes
        new DexHasher().writeHashes(dexWriter);
        dexWriter.writeTo(dexOut);

        long elapsed = System.nanoTime() - start;
        logger.info(String.format("Merged. Result length=%.1fKiB. Took %.1fs",
                dexOut.length() / 1024f, elapsed / 1000000000f));
    }

    /**
     * Reads an IDs section of two dex files and writes an IDs section of a
     * merged dex file. Populates maps from old to new indices in the process.
     */
    abstract class IdMerger<T extends Comparable<T>> {
        public final void merge() {
            TableOfContents.Section aSection = getSection(dexA.getTableOfContents());
            TableOfContents.Section bSection = getSection(dexB.getTableOfContents());
            getSection(contentsOut).off = idsDefsWriter.getPosition();

            int aIndex = 0;
            int bIndex = 0;
            int outCount = 0;
            T a = null;
            T b = null;

            while (true) {
                if (a == null && aIndex < aSection.size) {
                    a = read(dexA, aIndexMap, aIndex);
                }
                if (b == null && bIndex < bSection.size) {
                    b = read(dexB, bIndexMap, bIndex);
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
                    updateIndex(aIndexMap, aIndex++, outCount);
                    a = null;
                }
                if (advanceB) {
                    toWrite = b;
                    updateIndex(bIndexMap, bIndex++, outCount);
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
        abstract T read(DexBuffer dexBuffer, IndexMap indexMap, int index);
        abstract void updateIndex(IndexMap indexMap, int oldIndex, int newIndex);
        abstract void write(T value);
    }

    private void mergeStringIds() {
        new IdMerger<String>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.stringIds;
            }

            @Override String read(DexBuffer dexBuffer, IndexMap indexMap, int index) {
                return dexBuffer.strings().get(index);
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
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
        new IdMerger<Integer>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeIds;
            }

            @Override Integer read(DexBuffer dexBuffer, IndexMap indexMap, int index) {
                Integer stringIndex = dexBuffer.typeIds().get(index);
                return indexMap.adjustString(stringIndex);
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.typeIds[oldIndex] = (short) newIndex;
            }

            @Override void write(Integer value) {
                idsDefsWriter.writeInt(value);
            }
        }.merge();
    }

    private void mergeProtoIds() {
        new IdMerger<ProtoId>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.protoIds;
            }

            @Override ProtoId read(DexBuffer dexBuffer, IndexMap indexMap, int index) {
                return indexMap.adjust(dexBuffer.protoIds().get(index));
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.protoIds[oldIndex] = (short) newIndex;
            }

            @Override void write(ProtoId value) {
                int typeListPosition = writeTypeList(value.getParameters());
                value.writeTo(idsDefsWriter, typeListPosition);
            }
        }.merge();
    }

    private void mergeFieldIds() {
        new IdMerger<FieldId>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.fieldIds;
            }

            @Override FieldId read(DexBuffer dexBuffer, IndexMap indexMap, int index) {
                return indexMap.adjust(dexBuffer.fieldIds().get(index));
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.fieldIds[oldIndex] = (short) newIndex;
            }

            @Override void write(FieldId value) {
                value.writeTo(idsDefsWriter);
            }
        }.merge();
    }

    private void mergeMethodIds() {
        new IdMerger<MethodId>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.methodIds;
            }

            @Override MethodId read(DexBuffer dexBuffer, IndexMap indexMap, int index) {
                return indexMap.adjust(dexBuffer.methodIds().get(index));
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.methodIds[oldIndex] = (short) newIndex;
            }

            @Override void write(MethodId methodId) {
                methodId.writeTo(idsDefsWriter);
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
        return Arrays.copyOfRange(sortableTypes, 0, firstNull);
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

        short[] interfaces = classDef.getInterfaces();
        int typeListPosition = writeTypeList(interfaces);
        idsDefsWriter.writeInt(typeListPosition);

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

    private int writeTypeList(short[] interfaces) {
        if (interfaces.length == 0) {
            return 0;
        }
        contentsOut.typeLists.size++;
        typeListWriter.alignToFourBytes();
        int cursor = typeListWriter.getPosition();
        typeListWriter.writeInt(interfaces.length);
        typeListWriter.write(interfaces);
        return cursor;
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

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            printUsage();
            return;
        }

        new DexMerger(new File(args[0]), new File(args[1]), new File(args[2])).merge();
    }

    private static void printUsage() {
        System.out.println("Usage: DexMerger <out.dex> <a.dex> <b.dex>");
        System.out.println();
        System.out.println("If both a and b define the same classes, a's copy will be used.");
    }
}
