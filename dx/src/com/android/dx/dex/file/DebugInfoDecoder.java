/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.dx.dex.file;

import com.android.dx.dex.code.LocalList;
import com.android.dx.dex.code.PositionList;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.Prototype;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import com.android.dx.util.ExceptionWithContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.android.dx.dex.file.DebugInfoConstants.*;

/**
 * A decoder for the dex debug info state machine format.
 * This code exists mostly as a reference implementation and test for
 * for the <code>DebugInfoEncoder</code>
 */
public class DebugInfoDecoder {
    /** encoded debug info */
    private final byte[] encoded;
    /** positions decoded */
    private final ArrayList<PositionEntry> positions;
    /** locals decoded */
    private final ArrayList<LocalEntry> locals;
    /** size of code block in code units */
    private final int codesize;
    /** indexed by register, the last local variable live in a reg */
    private final LocalEntry[] lastEntryForReg;
    /** method descriptor of method this debug info is for */
    private final Prototype desc;
    /** true if method is static */
    private final boolean isStatic;
    /** dex file this debug info will be stored in */
    private final DexFile file;
    /**
     * register size, in register units, of the register space
     * used by this method
     */
    private final int regSize;

    /** current decoding state: line number */
    private int line = 1;
    
    /** current decoding state: bytecode address */
    private int address = 0;

    /** string index of the string "this" */
    private final int thisStringIdx;

    /**
     * Constructs an instance.
     *
     * @param encoded encoded debug info
     * @param codesize size of code block in code units
     * @param regSize register size, in register units, of the register space
     * used by this method
     * @param isStatic true if method is static
     * @param ref method descriptor of method this debug info is for
     * @param file dex file this debug info will be stored in
     */
    DebugInfoDecoder (byte[] encoded, int codesize, int regSize,
            boolean isStatic, CstMethodRef ref, DexFile file) {

        this.encoded = encoded;
        this.isStatic = isStatic;
        this.desc = ref.getPrototype();
        this.file = file;
        this.regSize = regSize;
        
        positions = new ArrayList();
        locals = new ArrayList();
        this.codesize = codesize;
        lastEntryForReg = new LocalEntry[regSize];

        thisStringIdx = file.getStringIds().indexOf(new CstUtf8("this"));
    }

    /**
     * An entry in the resulting postions table
     */
    static class PositionEntry {
        /** bytecode address */
        int address;
        /** line number */
        int line;

        PositionEntry(int address, int line) {
            this.address = address;
            this.line = line;
        }
    }

    /**
     * An entry in the resulting locals table
     */
    static class LocalEntry {
        LocalEntry(int start, int reg, int nameIndex, int typeIndex,
                int signatureIndex) {
            this.start          = start;
            this.reg            = reg;
            this.nameIndex      = nameIndex;
            this.typeIndex      = typeIndex;
            this.signatureIndex = signatureIndex;
        }

        /** start of address range */
        int start;

        /**
         * End of address range. Initialized to MAX_VALUE here but will
         * be set to no more than 1 + max bytecode address of method.
         */
        int end = Integer.MAX_VALUE;

        /** register number */
        int reg;

        /** index of name in strings table */
        int nameIndex;

        /** index of type in types table */
        int typeIndex;

        /** index of type signature in strings table */
        int signatureIndex;

    }

    /**
     * Gets the decoded positions list.
     * Valid after calling <code>decode</code>.
     *
     * @return positions list in ascending address order.
     */
    public List<PositionEntry> getPositionList() {
        return positions;
    }

    /**
     * Gets the decoded locals list, in ascending start-address order.
     * Valid after calling <code>decode</code>.
     *
     * @return locals list in ascending address order.
     */
    public List<LocalEntry> getLocals() {
        // TODO move this loop:
        // Any variable that didnt end ends now
        for (LocalEntry local: locals) {
            if (local.end == Integer.MAX_VALUE) {
                local.end = codesize;
            }
        }
        return locals;
    }

    /**
     * Decodes the debug info sequence.
     */
    public void decode() {
        try {
            decode0();
        } catch (Exception ex) {
            throw ExceptionWithContext.withContext(ex,
                    "...while decoding debug info");
        }
    }

    /**
     * Reads a string index. String indicies are offset by 1, and a 0 value
     * in the stream (-1 as returned by this method) means "null"
     *
     * @param bs
     * @return index into file's string ids table, -1 means null
     * @throws IOException
     */
    private int readStringIndex(InputStream bs) throws IOException {
        int offsetIndex = readUnsignedLeb128(bs);

        return offsetIndex - 1;
    }

    /**
     * Gets the register that begins the method's parameter range (including
     * the 'this' parameter for non-static methods). The range continues until
     * <code>regSize</code>
     *
     * @return register as noted above.
     */
    private int getParamBase() {
        return regSize
                - desc.getParameterTypes().getWordCount() - (isStatic? 0 : 1);
    }

    private void decode0() throws IOException {
        ByteArrayInputStream bs = new ByteArrayInputStream(encoded);

        line = readUnsignedLeb128(bs);
        int szParams = readUnsignedLeb128(bs);
        StdTypeList params = desc.getParameterTypes();
        int curReg = getParamBase();

        if (szParams != params.size()) {
            throw new RuntimeException(
                    "Mismatch between parameters_size and prototype");
        }
        
        if (!isStatic) {
            // Start off with implicit 'this' entry
            LocalEntry thisEntry
                    = new LocalEntry(0, curReg, thisStringIdx, 0, 0);
            locals.add(thisEntry);
            lastEntryForReg[curReg] = thisEntry;
            curReg++;
        }

        for (int i = 0; i < szParams; i++) {
            Type paramType = params.getType(i);
            LocalEntry le;

            int nameIdx = readStringIndex(bs);

            if(nameIdx == -1) {
                // unnamed parameter
            } else {
                // final '0' should be idx of paramType.getDescriptor()
                le = new LocalEntry(0, curReg, nameIdx, 0, 0);
                locals.add(le);
                lastEntryForReg[curReg] = le;
            }

            curReg += paramType.getCategory();
        }

        for (;;) {
            int opcode = bs.read();

            if (opcode < 0) {
                throw new RuntimeException
                        ("Reached end of debug stream without "
                                + "encountering end marker");
            }

            switch (opcode) {
                case DBG_START_LOCAL: {
                    int reg = readUnsignedLeb128(bs);
                    int nameIdx = readStringIndex(bs);
                    int typeIdx = readStringIndex(bs);
                    LocalEntry le = new LocalEntry(
                            address, reg, nameIdx, typeIdx, 0);

                    // a "start" is implicitly the "end" of whatever was
                    // previously defined in the register
                    if (lastEntryForReg[reg] != null
                            && lastEntryForReg[reg].end == Integer.MAX_VALUE) {

                        lastEntryForReg[reg].end = address;
                    }

                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;

                case DBG_START_LOCAL_EXTENDED: {
                    int reg = readUnsignedLeb128(bs);
                    int nameIdx = readStringIndex(bs);
                    int typeIdx = readStringIndex(bs);
                    int sigIdx = readStringIndex(bs);
                    LocalEntry le = new LocalEntry(
                            address, reg, nameIdx, typeIdx, sigIdx);

                    // a "start" is implicitly the "end" of whatever was
                    // previously defined in the register
                    if (lastEntryForReg[reg] != null
                            && lastEntryForReg[reg].end == Integer.MAX_VALUE) {

                        lastEntryForReg[reg].end = address;

                        // A 0-length entry. Almost certainly a "this"
                        // with a signature.
                        if (lastEntryForReg[reg].start == address) {
                            locals.remove(lastEntryForReg[reg]);
                        }
                    }

                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;

                case DBG_RESTART_LOCAL: {
                    int reg = readUnsignedLeb128(bs);
                    LocalEntry prevle;
                    LocalEntry le;

                    try {
                        prevle = lastEntryForReg[reg];

                        if (lastEntryForReg[reg].end == Integer.MAX_VALUE) {
                            throw new RuntimeException ("nonsensical "
                                    + "RESTART_LOCAL on live register v"+reg);
                        }
                        le = new LocalEntry(address, reg,
                                prevle.nameIndex, prevle.typeIndex, 0);

                    } catch (NullPointerException ex) {
                        throw new RuntimeException
                                ("Encountered RESTART_LOCAL on new v" +reg);
                    }

                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;

                case DBG_END_LOCAL: {
                    int reg = readUnsignedLeb128(bs);
                    boolean found = false;
                    for (int i = locals.size() - 1; i >= 0; i--) {
                        if (locals.get(i).reg == reg) {
                            locals.get(i).end = address;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        throw new RuntimeException(
                                "Encountered LOCAL_END without local start: v"
                                        + reg);
                    }
                }
                break;

                case DBG_END_SEQUENCE:
                    // all done
                return;

                case DBG_ADVANCE_PC:
                    address += readUnsignedLeb128(bs);
                break;

                case DBG_ADVANCE_LINE:
                    line += readSignedLeb128(bs);
                break;

                case DBG_SET_PROLOGUE_END:
                    //TODO do something with this.
                break;

                case DBG_SET_EPILOGUE_BEGIN:
                    //TODO do something with this.
                break;

                case DBG_SET_FILE:
                    //TODO do something with this.
                break;

                default:
                    if (opcode < DBG_FIRST_SPECIAL) {
                        throw new RuntimeException(
                                "Invalid extended opcode encountered "
                                        + opcode);
                    }

                    int adjopcode = opcode - DBG_FIRST_SPECIAL;

                    address += adjopcode / DBG_LINE_RANGE;
                    line += DBG_LINE_BASE + (adjopcode % DBG_LINE_RANGE);

                    positions.add(new PositionEntry(address, line));
                break;

            }
        }
    }

    /**
     * Validates an encoded debug info stream against data used to encode it,
     * throwing an exception if they do not match. Used to validate the
     * encoder.
     *
     * @param linecodes encoded debug info
     * @param codeSize size of insn block in code units
     * @param countRegisters size of used register block in register units
     * @param pl position list to verify against
     * @param ll locals list to verify against.
     */
    public static void validateEncode(byte[] linecodes, int codeSize,
            int countRegisters, PositionList pl, LocalList ll,
            boolean isStatic, CstMethodRef ref, DexFile file) {

        try {
            validateEncode0(linecodes, codeSize, countRegisters,
                    isStatic, ref, file, pl, ll);
        } catch (RuntimeException ex) {
//            System.err.println(ex.toString()
//                    + " while processing " + ref.toHuman());
            throw ExceptionWithContext.withContext(ex,
                    "while processing " + ref.toHuman());
        }
    }

    
    private static void validateEncode0(byte[] linecodes, int codeSize,
            int countRegisters, boolean isStatic, CstMethodRef ref,
            DexFile file, PositionList pl, LocalList ll) {
        DebugInfoDecoder decoder
                = new DebugInfoDecoder(linecodes, codeSize, countRegisters,
                    isStatic, ref, file);

        decoder.decode();

        List<PositionEntry> decodedEntries;

        decodedEntries = decoder.getPositionList();

        if (decodedEntries.size() != pl.size()) {
            throw new RuntimeException(
                    "Decoded positions table not same size was "
                    + decodedEntries.size() + " expected " + pl.size());
        }

        for (PositionEntry entry: decodedEntries) {
            boolean found = false;
            for (int i = pl.size() - 1; i >= 0; i--) {
                PositionList.Entry ple = pl.get(i);

                if (entry.line == ple.getPosition().getLine()
                        && entry.address == ple.getAddress()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException ("Could not match position entry: "
                        + entry.address + ", " + entry.line);
            }
        }

        List<LocalEntry> decodedLocals;

        decodedLocals = decoder.getLocals();

        int paramBase = decoder.getParamBase();

        int matchedLocalsEntries = 0;

        for (LocalEntry entry: decodedLocals) {
            boolean found = false;
            for (int i = ll.size() - 1; i >= 0; i--) {
                LocalList.Entry le = ll.get(i);

                /*
                 * If an entry is a method parameter, then the original
                 * entry may not be marked as starting at 0. However, the
                 * end address shuld still match.
                 */
                if ((entry.start == le.getStart()
                        || (entry.start == 0 && entry.reg >= paramBase))
                        && entry.end == le.getEnd()
                        && entry.reg == le.getRegister()) {
                    found = true;
                    matchedLocalsEntries++;
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException("Could not match local entry");
            }
        }

        if (matchedLocalsEntries != ll.size()) {
            throw new RuntimeException("Locals tables did not match");
        }
    }

    /**
     * Reads a DWARFv3-style signed LEB128 integer to the specified stream.
     * See DWARF v3 section 7.6. An invalid sequence produces an IOException.
     *
     * @param bs stream to input from
     * @return read value
     * @throws IOException on invalid sequence in addition to
     * those caused by the InputStream
     */
    public static int readSignedLeb128(InputStream bs) throws IOException {
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;

        do {
            cur = bs.read();
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);

        if ((cur & 0x80) == 0x80) {
            throw new IOException ("invalid LEB128 sequence");
        }

        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0 ) {
            result |= signBits;
        }

        return result;
    }

    /**
     * Reads a DWARFv3-style unsigned LEB128 integer to the specified stream.
     * See DWARF v3 section 7.6. An invalid sequence produces an IOException.
     *
     * @param bs stream to input from
     * @return read value, which should be treated as an unsigned value.
     * @throws IOException on invalid sequence in addition to
     * those caused by the InputStream
     */
    public static int readUnsignedLeb128(InputStream bs) throws IOException {
        int result = 0;
        int cur;
        int count = 0;

        do {
            cur = bs.read();
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);

        if ((cur & 0x80) == 0x80) {
            throw new IOException ("invalid LEB128 sequence");
        }

        return result;
    }
}
