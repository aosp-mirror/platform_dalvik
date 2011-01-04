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

package com.android.dx.print;

import com.android.dx.dex.SizeOf;
import com.android.dx.util.DexReader;
import com.android.dx.dex.TableOfContents;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Executable that prints all indices of a dex file.
 */
public final class DexIndexPrinter implements Closeable {
    private final DexReader dexReader;
    private final TableOfContents tableOfContents;

    public DexIndexPrinter(File file) throws IOException {
        this.dexReader = new DexReader(file);
        this.tableOfContents = dexReader.getTableOfContents();
    }

    private void printMap() {
        for (TableOfContents.Section section : tableOfContents.sections) {
            if (section.off != -1) {
                System.out.println("section " + Integer.toHexString(section.type)
                        + " off=" + Integer.toHexString(section.off)
                        + " size=" + Integer.toHexString(section.size)
                        + " byteCount=" + Integer.toHexString(section.byteCount));
            }
        }
    }

    private void printStrings() throws IOException {
        for (int i = 0; i < tableOfContents.stringIds.size; i++) {
            String s = dexReader.readString(i);
            System.out.println("string " + i + ": " + s);
        }
    }

    private void printTypeIds() throws IOException {
        dexReader.seek(tableOfContents.typeIds.off);
        for (int i = 0; i < tableOfContents.typeIds.size; i++) {
            int stringIndex = dexReader.readInt();
            System.out.println("type " + i + ": " + dexReader.readString(stringIndex));
        }
    }

    private String readProto(int protoIndex) throws IOException {
        int position = dexReader.getPosition();
        dexReader.seek(tableOfContents.protoIds.off + (SizeOf.PROTO_ID_ITEM * protoIndex));
        int shortyIdx = dexReader.readInt(); // string ID
        int returnTypeIdx = dexReader.readInt(); //type ID
        int parametersOff = dexReader.readInt(); // type list, or 0 for no parameters
        String shorty = dexReader.readString(shortyIdx);
        String returnType = readType(returnTypeIdx);
        StringBuilder result = new StringBuilder()
                .append(shorty)
                .append(": ")
                .append(returnType)
                .append(" (");

        if (parametersOff != 0) {
            dexReader.seek(parametersOff);
            int typeListSize = dexReader.readInt();
            for (int j = 0; j < typeListSize; j++) {
                if (j > 0) {
                    result.append(", ");
                }
                dexReader.seek(parametersOff + 4 + (j * SizeOf.TYPE_ITEM));
                int typeIndex = dexReader.readShort();
                result.append(readType(typeIndex));
            }
        }
        result.append(")");
        dexReader.seek(position);
        return result.toString();
    }

    private void printProtoIds() throws IOException {
        for (int i = 0; i < tableOfContents.protoIds.size; i++) {
            String proto = readProto(i);
            System.out.println("proto " + i + ": " + proto);
        }
        dexReader.seek(tableOfContents.protoIds.off
                + (SizeOf.PROTO_ID_ITEM * tableOfContents.protoIds.size));
    }

    private void printFieldIds() throws IOException {
        for (int i = 0; i < tableOfContents.fieldIds.size; i++) {
            int declaringClass = dexReader.readShort();
            int type = dexReader.readShort();
            int nameIndex = dexReader.readInt();
            System.out.println("field " + i + ": " + readType(declaringClass)
                    + " { " + readType(type) + " " + dexReader.readString(nameIndex) + " }");
        }
    }

    private void printMethodIds() throws IOException {
        for (int i = 0; i < tableOfContents.methodIds.size; i++) {
            int declaringClass = dexReader.readShort();
            int protoIndex = dexReader.readShort();
            int name = dexReader.readInt();
            String proto = readProto(protoIndex);
            System.out.println("method " + i + ": " + readType(declaringClass)
                    + " " + proto + " " + dexReader.readString(name));
        }
    }

    private void printTypeLists() throws IOException {
        if (tableOfContents.typeLists.off == -1) {
            System.out.println("No type lists");
            return;
        }
        int position = dexReader.getPosition();
        dexReader.seek(tableOfContents.typeLists.off);
        for (int i = 0; i < tableOfContents.typeLists.size; i++) {
            int size = dexReader.readInt();
            System.out.print("Type list i=" + i + ", size=" + size + ", elements=");
            for (int t = 0; t < size; t++) {
                System.out.print(" " + readType(dexReader.readShort()));
            }
            if (size % 2 == 1) {
                dexReader.readShort(); // retain alignment
            }
            System.out.println();
        }
        dexReader.seek(position);
    }

    private String readType(int index) throws IOException {
        if (index < 0 || index >= tableOfContents.typeIds.size) {
            throw new IllegalArgumentException("type index out of range: "
                    + index + " " + tableOfContents.typeIds.size);
        }
        int position = dexReader.getPosition();
        dexReader.seek(tableOfContents.typeIds.off + (index * SizeOf.TYPE_ID_ITEM));
        int stringIndex = dexReader.readInt();
        String result = dexReader.readString(stringIndex);
        dexReader.seek(position);
        return result;
    }

    public void close() throws IOException {
        dexReader.close();
    }

    public static void main(String[] args) throws IOException {
        DexIndexPrinter indexPrinter = new DexIndexPrinter(new File(args[0]));
        indexPrinter.printMap();
        indexPrinter.printStrings();
        indexPrinter.printTypeIds();
        indexPrinter.printProtoIds();
        indexPrinter.printFieldIds();
        indexPrinter.printMethodIds();
        indexPrinter.printTypeLists();
    }
}
