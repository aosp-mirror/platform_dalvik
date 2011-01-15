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

package com.android.dx.dex;

import com.android.dx.io.DexBuffer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * The file header and map.
 */
public final class TableOfContents {

    /*
     * TODO: factor out ID constants.
     */

    public final Section header = new Section(0x0000);
    public final Section stringIds = new Section(0x0001);
    public final Section typeIds = new Section(0x0002);
    public final Section protoIds = new Section(0x0003);
    public final Section fieldIds = new Section(0x0004);
    public final Section methodIds = new Section(0x0005);
    public final Section classDefs = new Section(0x0006);
    public final Section mapList = new Section(0x1000);
    public final Section typeLists = new Section(0x1001);
    public final Section annotationSetRefLists = new Section(0x1002);
    public final Section annotationSets = new Section(0x1003);
    public final Section classDatas = new Section(0x2000);
    public final Section codes = new Section(0x2001);
    public final Section stringDatas = new Section(0x2002);
    public final Section debugInfos = new Section(0x2003);
    public final Section annotations = new Section(0x2004);
    public final Section encodedArrays = new Section(0x2005);
    public final Section annotationsDirectories = new Section(0x2006);
    public final Section[] sections = {
            header, stringIds, typeIds, protoIds, fieldIds, methodIds, classDefs, mapList,
            typeLists, annotationSetRefLists, annotationSets, classDatas, codes, stringDatas,
            debugInfos, annotations, encodedArrays, annotationsDirectories
    };

    public int checksum;
    public byte[] signature;
    public int fileSize;
    public int linkSize;
    public int linkOff;
    public int dataSize;
    public int dataOff;

    public TableOfContents() {
        signature = new byte[20];
    }

    public void readFrom(DexBuffer buffer) throws IOException {
        readHeader(buffer.open(0));
        readMap(buffer.open(mapList.off));
    }

    private void readHeader(DexBuffer.Section headerIn) throws UnsupportedEncodingException {
        byte[] magic = headerIn.readByteArray(8);
        if (!Arrays.equals(DexFormat.MAGIC.getBytes("UTF-8"), magic)) {
            throw new DexException("Unexpected magic: " + Arrays.toString(magic));
        }

        checksum = headerIn.readInt();
        signature = headerIn.readByteArray(20);
        fileSize = headerIn.readInt();
        int headerSize = headerIn.readInt();
        if (headerSize != SizeOf.HEADER_ITEM) {
            throw new DexException("Unexpected header: 0x" + Integer.toHexString(headerSize));
        }
        int endianTag = headerIn.readInt();
        if (endianTag != DexFormat.ENDIAN_TAG) {
            throw new DexException("Unexpected endian tag: 0x" + Integer.toHexString(endianTag));
        }
        linkSize = headerIn.readInt();
        linkOff = headerIn.readInt();
        mapList.off = headerIn.readInt();
        if (mapList.off == 0) {
            throw new DexException("Cannot merge dex files that do not contain a map");
        }
        stringIds.size = headerIn.readInt();
        stringIds.off = headerIn.readInt();
        typeIds.size = headerIn.readInt();
        typeIds.off = headerIn.readInt();
        protoIds.size = headerIn.readInt();
        protoIds.off = headerIn.readInt();
        fieldIds.size = headerIn.readInt();
        fieldIds.off = headerIn.readInt();
        methodIds.size = headerIn.readInt();
        methodIds.off = headerIn.readInt();
        classDefs.size = headerIn.readInt();
        classDefs.off = headerIn.readInt();
        dataSize = headerIn.readInt();
        dataOff = headerIn.readInt();
    }

    private void readMap(DexBuffer.Section in) throws IOException {
        int mapSize = in.readInt();

        Section previous = null;
        for (int i = 0; i < mapSize; i++) {
            short type = in.readShort();
            in.readShort(); // unused
            Section section = getSection(type);
            int size = in.readInt();
            int offset = in.readInt();

            if ((section.size != -1 && section.size != size)
                    || (section.off != -1 && section.off != offset)) {
                throw new DexException("Unexpected map value for 0x" + Integer.toHexString(type));
            }

            section.size = size;
            section.off = offset;

            if (previous != null) {
                if (previous.off > offset) {
                    throw new DexException("Map is unsorted at 0x" + Integer.toHexString(type));
                }
                previous.byteCount = offset - previous.off;
            }

            previous = section;
        }

        if (previous != null) {
            int endOfData = dataOff + dataSize;
            if (previous.off > endOfData) {
                throw new DexException("Map disagrees with data section offset and size");
            }
            previous.byteCount = endOfData - previous.off;
        }
    }

    private Section getSection(short type) {
        for (Section section : sections) {
            if (section.type == type) {
                return section;
            }
        }
        throw new IllegalArgumentException("No such map item: " + type);
    }

    public void writeHeader(DexBuffer.Section out) throws IOException {
        out.write(DexFormat.MAGIC.getBytes("UTF-8"));
        out.writeInt(checksum);
        out.write(signature);
        out.writeInt(fileSize);
        out.writeInt(SizeOf.HEADER_ITEM);
        out.writeInt(DexFormat.ENDIAN_TAG);
        out.writeInt(linkSize);
        out.writeInt(linkOff);
        out.writeInt(mapList.off);
        out.writeInt(stringIds.size);
        out.writeInt(stringIds.off);
        out.writeInt(typeIds.size);
        out.writeInt(typeIds.off);
        out.writeInt(protoIds.size);
        out.writeInt(protoIds.off);
        out.writeInt(fieldIds.size);
        out.writeInt(fieldIds.off);
        out.writeInt(methodIds.size);
        out.writeInt(methodIds.off);
        out.writeInt(classDefs.size);
        out.writeInt(classDefs.off);
        out.writeInt(dataSize);
        out.writeInt(dataOff);
    }

    public void writeMap(DexBuffer.Section out) throws IOException {
        int count = 0;
        for (Section s : sections) {
            if (s.size > 0) {
                count++;
            }
        }

        out.writeInt(count);
        for (Section s : sections) {
            if (s.size > 0) {
                if (false) {
                    System.out.println("Writing section " + s
                            + " type=0x" + Integer.toHexString(s.type)
                            + " offset=0x" + Integer.toHexString(s.off)
                            + " size=0x" + Integer.toHexString(s.size)
                    );
                }

                out.writeShort(s.type);
                out.writeShort((short) 0);
                out.writeInt(s.size);
                out.writeInt(s.off);
            }
        }
    }

    public static class Section {
        public final short type;
        public int size = -1;
        public int off = -1;
        public int byteCount = 0;

        public Section(int type) {
            this.type = (short) type;
        }
    }
}
