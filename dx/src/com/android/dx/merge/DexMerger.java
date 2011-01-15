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
import com.android.dx.io.ClassDef;
import com.android.dx.io.DexBuffer;
import com.android.dx.io.DexHasher;
import com.android.dx.io.FieldId;
import com.android.dx.io.MethodId;
import com.android.dx.io.ProtoId;
import com.android.dx.util.Uint;
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
    private final DexBuffer.Section annotationSetItemWriter;
    private final DexBuffer.Section classDataItemWriter;
    private final DexBuffer.Section codeItemWriter;
    private final DexBuffer.Section stringDataItemWriter;
    private final DexBuffer.Section debugInfoItemWriter;
    private final DexBuffer.Section annotationItemWriter;
    private final DexBuffer.Section encodedArrayItemWriter;
    private final DexBuffer.Section annotationsDirectoryItemWriter;
    private final TableOfContents contentsOut;

    private final DexBuffer dexA = new DexBuffer();
    private final DexBuffer dexB = new DexBuffer();
    private final IndexMap aIndexMap;
    private final IndexMap bIndexMap;

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
         * classDataItemWriter: uleb references to code items are larger than
         *     expected. We should use old & new code_item section offsets to
         *     pick an appropriate blow up size
         *
         * stringDataItemWriter: this shouldn't have to be larger, but it is
         *
         * encodedArrayItemWriter: this shouldn't have to be larger, but it is
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
        annotationSetItemWriter = dexWriter.appendSection(SizeOf.UINT, "annotation set item");

        contentsOut.classDatas.off = dexWriter.getLength();
        contentsOut.classDatas.size = 0;
        int maxClassDataItemBytes = aContents.classDatas.byteCount + bContents.classDatas.byteCount;
        classDataItemWriter = dexWriter.appendSection(maxClassDataItemBytes * 2, "class data");

        contentsOut.codes.off = dexWriter.getLength();
        contentsOut.codes.size = 0;
        int maxCodeItemBytes = aContents.codes.byteCount + bContents.codes.byteCount;
        codeItemWriter = dexWriter.appendSection(maxCodeItemBytes, "code item");

        contentsOut.stringDatas.off = dexWriter.getLength();
        contentsOut.stringDatas.size = 0;
        int maxStringDataItemBytes = aContents.stringDatas.byteCount
                + bContents.stringDatas.byteCount;
        stringDataItemWriter = dexWriter.appendSection(maxStringDataItemBytes * 2, "string data");

        contentsOut.debugInfos.off = dexWriter.getLength();
        contentsOut.debugInfos.size = 0;
        int maxDebugInfoItemBytes = aContents.debugInfos.byteCount + bContents.debugInfos.byteCount;
        debugInfoItemWriter = dexWriter.appendSection(maxDebugInfoItemBytes, "debug info");

        contentsOut.annotations.off = dexWriter.getLength();
        contentsOut.annotations.size = 0;
        int maxAnnotationItemBytes = aContents.annotations.byteCount
                + bContents.annotations.byteCount;
        annotationItemWriter = dexWriter.appendSection(maxAnnotationItemBytes, "annotation");

        contentsOut.encodedArrays.off = dexWriter.getLength();
        contentsOut.encodedArrays.size = 0;
        int maxEncodedArrayItemBytes = aContents.encodedArrays.byteCount
                + bContents.encodedArrays.byteCount;
        encodedArrayItemWriter = dexWriter.appendSection(
                maxEncodedArrayItemBytes * 2, "encoded array");

        contentsOut.annotationsDirectories.off = dexWriter.getLength();
        contentsOut.annotationsDirectories.size = 0;
        int maxAnnotationsDirectoryItemBytes = aContents.annotationsDirectories.byteCount
                + bContents.annotationsDirectories.byteCount;
        annotationsDirectoryItemWriter = dexWriter.appendSection(
                maxAnnotationsDirectoryItemBytes, "annotations");

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
        public final void merge() throws IOException {
            TableOfContents.Section aSection = getSection(dexA.getTableOfContents());
            TableOfContents.Section bSection = getSection(dexB.getTableOfContents());
            DexBuffer.Section aIn = dexA.open(aSection.off);
            DexBuffer.Section bIn = dexB.open(bSection.off);
            getSection(contentsOut).off = idsDefsWriter.getPosition();

            int aIndex = 0;
            int bIndex = 0;
            int outCount = 0;
            T a = null;
            T b = null;

            while (true) {
                if (a == null && aIndex < aSection.size) {
                    a = read(aIn, aIndexMap, aIndex);
                }
                if (b == null && bIndex < bSection.size) {
                    b = read(bIn, bIndexMap, bIndex);
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
        abstract T read(DexBuffer.Section in, IndexMap indexMap, int index) throws IOException;
        abstract void updateIndex(IndexMap indexMap, int oldIndex, int newIndex);
        abstract void write(T value) throws IOException;
    }

    private void mergeStringIds() throws IOException {
        new IdMerger<String>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.stringIds;
            }

            @Override String read(DexBuffer.Section in, IndexMap indexMap, int index)
                    throws IOException {
                return in.readString(index);
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.stringIds[oldIndex] = newIndex;
            }

            @Override void write(String value) throws IOException {
                contentsOut.stringDatas.size++;
                idsDefsWriter.writeInt(stringDataItemWriter.getPosition());
                stringDataItemWriter.writeStringDataItem(value);
            }
        }.merge();
    }

    private void mergeTypeIds() throws IOException {
        new IdMerger<Uint>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeIds;
            }

            @Override Uint read(DexBuffer.Section in, IndexMap indexMap, int index)
                    throws IOException {
                return new Uint(indexMap.adjustString(in.readInt()));
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.typeIds[oldIndex] = (short) newIndex;
            }

            @Override void write(Uint value) throws IOException {
                idsDefsWriter.writeInt(value.intValue);
            }
        }.merge();
    }

    private void mergeProtoIds() throws IOException {
        new IdMerger<ProtoId>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.protoIds;
            }

            @Override ProtoId read(DexBuffer.Section in, IndexMap indexMap, int index)
                    throws IOException {
                return indexMap.adjust(in.readProtoId());
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.protoIds[oldIndex] = (short) newIndex;
            }

            @Override void write(ProtoId value) throws IOException {
                int typeListPosition = writeTypeList(value.getParameters());
                value.writeTo(idsDefsWriter, typeListPosition);
            }
        }.merge();
    }

    private void mergeFieldIds() throws IOException {
        new IdMerger<FieldId>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.fieldIds;
            }

            @Override
            FieldId read(DexBuffer.Section in, IndexMap indexMap, int index) throws IOException {
                return indexMap.adjust(in.readFieldId());
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.fieldIds[oldIndex] = (short) newIndex;
            }

            @Override void write(FieldId value) throws IOException {
                value.writeTo(idsDefsWriter);
            }
        }.merge();
    }

    private void mergeMethodIds() throws IOException {
        new IdMerger<MethodId>() {
            @Override TableOfContents.Section getSection(TableOfContents tableOfContents) {
                return tableOfContents.methodIds;
            }

            @Override MethodId read(DexBuffer.Section in, IndexMap indexMap, int index)
                    throws IOException {
                return indexMap.adjust(in.readMethodId());
            }

            @Override void updateIndex(IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.methodIds[oldIndex] = (short) newIndex;
            }

            @Override void write(MethodId methodId) throws IOException {
                methodId.writeTo(idsDefsWriter);
            }
        }.merge();
    }

    private void mergeClassDefs() throws IOException {
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
    private SortableType[] getSortedTypes() throws IOException {
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
            IndexMap indexMap) throws IOException {
        TableOfContents tableOfContents = buffer.getTableOfContents();
        DexBuffer.Section classDefsIn = buffer.open(tableOfContents.classDefs.off);

        for (int i = 0; i < tableOfContents.classDefs.size; i++) {
            SortableType sortableType = indexMap.adjust(
                    new SortableType(buffer, classDefsIn.readClassDef()));
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
    private void transformClassDef(DexBuffer in, ClassDef classDef, IndexMap indexMap)
            throws IOException {
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
            annotationsDirectoryItemWriter.alignToFourBytes();
            idsDefsWriter.writeInt(annotationsDirectoryItemWriter.getPosition());
            transformAnnotations(annotationsIn, indexMap);
        }

        int classDataOff = classDef.getClassDataOffset();
        if (classDataOff == 0) {
            idsDefsWriter.writeInt(0);
        } else {
            DexBuffer.Section classDataIn = in.open(classDataOff);
            idsDefsWriter.writeInt(classDataItemWriter.getPosition());
            transformClassData(classDataIn, indexMap);
        }

        int staticValuesOff = classDef.getStaticValuesOffset();
        if (staticValuesOff == 0) {
            idsDefsWriter.writeInt(0);
        } else {
            DexBuffer.Section staticValuesIn = in.open(staticValuesOff);
            idsDefsWriter.writeInt(encodedArrayItemWriter.getPosition());
            transformStaticValues(staticValuesIn, indexMap);
        }
    }

    private int writeTypeList(short[] interfaces) throws IOException {
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

    private void transformAnnotations(DexBuffer.Section in, IndexMap indexMap) throws IOException {
        contentsOut.annotationsDirectories.size++;

        // TODO: retain annotations
        annotationsDirectoryItemWriter.assertFourByteAligned();
        in.readInt(); // class annotations off
        in.readInt(); // fields size
        in.readInt(); // annotated methods size
        in.readInt(); // annotated parameters size

        annotationsDirectoryItemWriter.writeInt(0);
        annotationsDirectoryItemWriter.writeInt(0);
        annotationsDirectoryItemWriter.writeInt(0);
        annotationsDirectoryItemWriter.writeInt(0);
    }

    private void transformClassData(DexBuffer.Section in, IndexMap indexMap) throws IOException {
        contentsOut.classDatas.size++;

        int staticFieldsSize = in.readUleb128();
        classDataItemWriter.writeUleb128(staticFieldsSize);

        int instanceFieldsSize = in.readUleb128();
        classDataItemWriter.writeUleb128(instanceFieldsSize);

        int directMethodsSize = in.readUleb128();
        classDataItemWriter.writeUleb128(directMethodsSize);

        int virtualMethodsSize = in.readUleb128();
        classDataItemWriter.writeUleb128(virtualMethodsSize);

        transformEncodedFields(in, indexMap, staticFieldsSize);
        transformEncodedFields(in, indexMap, instanceFieldsSize);

        transformEncodedMethods(in, indexMap, directMethodsSize);
        transformEncodedMethods(in, indexMap, virtualMethodsSize);
    }

    private void transformEncodedFields(DexBuffer.Section in, IndexMap indexMap, int count)
            throws IOException {
        int inFieldIndex = 0;
        int lastOutFieldIndex = 0;
        for (int i = 0; i < count; i++) {
            inFieldIndex += in.readUleb128(); // field idx diff
            int outFieldIndex = indexMap.adjustField(inFieldIndex);
            classDataItemWriter.writeUleb128(outFieldIndex - lastOutFieldIndex);
            lastOutFieldIndex = outFieldIndex;

            classDataItemWriter.writeUleb128(in.readUleb128()); // access flags
        }
    }

    /**
     * Transforms a list of encoded methods.
     */
    private void transformEncodedMethods(DexBuffer.Section in, IndexMap indexMap, int count)
            throws IOException {
        int inMethodIndex = 0;
        int lastOutMethodIndex = 0;
        for (int i = 0; i < count; i++) {
            inMethodIndex += in.readUleb128(); // method idx diff
            int outMethodIndex = indexMap.adjustMethod(inMethodIndex);
            classDataItemWriter.writeUleb128(outMethodIndex - lastOutMethodIndex);
            lastOutMethodIndex = outMethodIndex;

            classDataItemWriter.writeUleb128(in.readUleb128()); // access flags

            int codeOff = in.readUleb128(); // code off
            if (codeOff == 0) {
                classDataItemWriter.writeUleb128(0);
            } else {
                codeItemWriter.alignToFourBytes();
                classDataItemWriter.writeUleb128(codeItemWriter.getPosition());
                transformCodeItem(in.open(codeOff), indexMap);
            }
        }
    }

    private void transformCodeItem(DexBuffer.Section in, IndexMap indexMap) throws IOException {
        contentsOut.codes.size++;
        codeItemWriter.assertFourByteAligned();

        short registersSize = in.readShort();
        codeItemWriter.writeShort(registersSize); // registers size
        short insSize = in.readShort();
        codeItemWriter.writeShort(insSize); // ins size
        short outsSize = in.readShort();
        codeItemWriter.writeShort(outsSize); // outs size
        short triesSize = in.readShort(); // tries size
        codeItemWriter.writeShort(triesSize);

        in.readInt(); // debug info off
        codeItemWriter.writeInt(0); // TODO: retain debug info

        int insnsSize = in.readInt(); // insns_size
        short[] insns = in.readShortArray(insnsSize); // insns
        short[] newInstructions = new InstructionTransformer(indexMap).transform(insns);
        codeItemWriter.writeInt(newInstructions.length);
        codeItemWriter.write(newInstructions);

        if (triesSize > 0) {
            // padding
            if (insns.length % 2 == 1) {
                in.readShort();
            }
            if (newInstructions.length % 2 == 1) {
                codeItemWriter.writeShort((short) 0);
            }

            // tries
            for (int i = 0; i < triesSize; i++) {
                transformTryItem(in, indexMap);
            }

            // handlers
            transformEncodedCatchHandlerList(in, indexMap);
        }
    }

    private void transformTryItem(DexBuffer.Section in, IndexMap indexMap) throws IOException {
        codeItemWriter.writeInt(in.readInt()); // start addr
        codeItemWriter.writeShort(in.readShort()); // insn count
        codeItemWriter.writeShort(in.readShort()); // handler off
    }

    private void transformEncodedCatchHandlerList(DexBuffer.Section in, IndexMap indexMap)
            throws IOException {
        int size = in.readUleb128(); // size
        codeItemWriter.writeUleb128(size);

        for (int i = 0; i < size; i++) {
            transformEncodedCatchHandler(in, indexMap);
        }
    }

    private void transformEncodedCatchHandler(DexBuffer.Section in, IndexMap indexMap)
            throws IOException {
        int size = in.readSleb128(); // size
        codeItemWriter.writeSleb128(size);

        int handlersCount = Math.abs(size);
        for (int i = 0; i < handlersCount; i++) {
            codeItemWriter.writeUleb128(indexMap.adjustType(in.readUleb128())); // type idx
            codeItemWriter.writeUleb128(in.readUleb128()); // addr
        }

        if (size <= 0) {
            codeItemWriter.writeUleb128(in.readUleb128()); // catch all addr
        }
    }

    private void transformStaticValues(DexBuffer.Section in, IndexMap indexMap) throws IOException {
        contentsOut.encodedArrays.size++;
        new EncodedValueTransformer(indexMap, in, encodedArrayItemWriter).transformArray();
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
