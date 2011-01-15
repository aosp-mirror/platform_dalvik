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

package com.android.dx.io;

import com.android.dx.dex.TableOfContents;
import java.io.File;
import java.io.IOException;

/**
 * Executable that prints all indices of a dex file.
 */
public final class DexIndexPrinter {
    private final DexBuffer dexBuffer;
    private final TableOfContents tableOfContents;

    public DexIndexPrinter(File file) throws IOException {
        this.dexBuffer = new DexBuffer();
        this.dexBuffer.loadFrom(file);
        this.tableOfContents = dexBuffer.getTableOfContents();
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
        DexBuffer.Section in = dexBuffer.open(tableOfContents.stringIds.off);
        for (int i = 0; i < tableOfContents.stringIds.size; i++) {
            String s = in.readString(i);
            System.out.println("string " + i + ": " + s);
        }
    }

    private void printTypeIds() throws IOException {
        DexBuffer.Section in = dexBuffer.open(tableOfContents.typeIds.off);
        for (int i = 0; i < tableOfContents.typeIds.size; i++) {
            int stringIndex = in.readInt();
            System.out.println("type " + i + ": " + in.readString(stringIndex));
        }
    }

    private void printProtoIds() throws IOException {
        DexBuffer.Section in = dexBuffer.open(tableOfContents.protoIds.off);
        for (int i = 0; i < tableOfContents.protoIds.size; i++) {
            System.out.println("proto " + i + ": " + in.readProtoId());
        }
    }

    private void printFieldIds() throws IOException {
        DexBuffer.Section in = dexBuffer.open(tableOfContents.fieldIds.off);
        for (int i = 0; i < tableOfContents.fieldIds.size; i++) {
            System.out.println("field " + i + ": " + in.readFieldId());
        }
    }

    private void printMethodIds() throws IOException {
        DexBuffer.Section in = dexBuffer.open(tableOfContents.methodIds.off);
        for (int i = 0; i < tableOfContents.methodIds.size; i++) {
            System.out.println("method " + i + ": " + in.readMethodId());
        }
    }

    private void printTypeLists() throws IOException {
        if (tableOfContents.typeLists.off == -1) {
            System.out.println("No type lists");
            return;
        }
        DexBuffer.Section in = dexBuffer.open(tableOfContents.typeLists.off);
        for (int i = 0; i < tableOfContents.typeLists.size; i++) {
            int size = in.readInt();
            System.out.print("Type list i=" + i + ", size=" + size + ", elements=");
            for (int t = 0; t < size; t++) {
                System.out.print(" " + in.readTypeName((int) in.readShort()));
            }
            if (size % 2 == 1) {
                in.readShort(); // retain alignment
            }
            System.out.println();
        }
    }

    private void printClassDefs() {
        DexBuffer.Section in = dexBuffer.open(tableOfContents.classDefs.off);
        for (int i = 0; i < tableOfContents.classDefs.size; i++) {
            System.out.println("class def " + i + ": " + in.readClassDef());
        }
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
        indexPrinter.printClassDefs();
    }
}
