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

package com.android.dx.dex.code;

import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.code.RegisterSpecSet;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.Type;
import com.android.dx.util.FixedSizeList;

import java.util.ArrayList;

/**
 * List of local variables. Each local variable entry indicates a
 * range of code which it is valid for, a register number, a name,
 * and a type.
 */
public final class LocalList extends FixedSizeList {
    /** non-null; empty instance */
    public static final LocalList EMPTY = new LocalList(0);

    /**
     * Constructs an instance for the given method, based on the given
     * block order and intermediate local information.
     * 
     * @param insns non-null; instructions to convert
     * @return non-null; the constructed list 
     */
    public static LocalList make(DalvInsnList insns) {
        ArrayList<Entry> result = new ArrayList<Entry>(100);
        int codeSize = insns.codeSize();
        int sz = insns.size();
        RegisterSpecSet state = null;
        int stateMax = 0;

        for (int i = 0; i < sz; i++) {
            DalvInsn insn = insns.get(i);

            if (insn instanceof LocalSnapshot) {
                RegisterSpecSet newState = ((LocalSnapshot) insn).getLocals();
                boolean first = (state == null);

                if (first) {
                    stateMax = newState.getMaxSize();
                }

                for (int j = 0; j < stateMax; j++) {
                    RegisterSpec oldSpec = first ? null : state.get(j);
                    RegisterSpec newSpec = newState.get(j);
                    boolean oldEnds = false;
                    boolean newStarts = false;

                    if (oldSpec == null) {
                        if (newSpec != null) {
                            /*
                             * This is a newly-introduced local, not
                             * replacing an existing local.
                             */
                            newStarts = true;
                        }
                    } else if (newSpec == null) {
                        /*
                         * This is a local going out of scope, with no
                         * replacement.
                         */
                        oldEnds = true;
                    } else if (!oldSpec.equals(newSpec)) {
                        /*
                         * This is a local going out of scope, immediately
                         * replaced by a different local.
                         */
                        oldEnds = true;
                        newStarts = true;
                    }

                    if (oldEnds) {
                        endScope(result, oldSpec, insn.getAddress());
                    }

                    if (newStarts) {
                        startScope(result, newSpec, insn.getAddress(),
                                   codeSize);
                    }
                }

                state = newState;
            } else if (insn instanceof LocalStart) {
                RegisterSpec newSpec = ((LocalStart) insn).getLocal();
                RegisterSpec oldSpec = state.get(newSpec);

                boolean oldEnds = false;
                boolean newStarts = false;

                if (oldSpec == null) {
                    /*
                     * This is a newly-introduced local, not replacing an
                     * existing local.
                     */
                    newStarts = true;
                } else if (!oldSpec.equals(newSpec)) {
                    /*
                     * This is a local going out of scope, immediately
                     * replaced by a different local.
                     */
                    oldEnds = true;
                    newStarts = true;
                }

                if (newStarts) {
                    int address = insn.getAddress();

                    if (oldEnds) {
                        endScope(result, oldSpec, address);
                    }

                    startScope(result, newSpec, address, codeSize);

                    if (state.isImmutable()) {
                        state = state.mutableCopy();
                    }

                    state.put(newSpec);
                }
            }
        }

        int resultSz = result.size();

        if (resultSz == 0) {
            return EMPTY;
        }

        LocalList resultList = new LocalList(resultSz);

        for (int i = 0; i < resultSz; i++) {
            resultList.set(i, result.get(i));
        }

        resultList.setImmutable();
        return resultList;
    }

    /**
     * Helper for {@link #make}, to indicate that the given variable has
     * been introduced.
     * 
     * @param result non-null; result in-progress
     * @param spec non-null; register spec for the variable in question
     * @param startAddress &gt;= 0; address at which the scope starts
     * (inclusive)
     * @param endAddress &gt; startAddress; initial scope end address
     * (exclusive)
     */
    private static void startScope(ArrayList<Entry> result, RegisterSpec spec,
                                   int startAddress, int endAddress) {
        result.add(new Entry(startAddress, endAddress, spec));
    }

    /**
     * Helper for {@link #make}, to indicate that the given variable's
     * scope has closed.
     * 
     * @param result non-null; result in-progress
     * @param spec non-null; register spec for the variable in question
     * @param endAddress &gt;= 0; address at which the scope ends (exclusive)
     */
    private static void endScope(ArrayList<Entry> result, RegisterSpec spec,
                                 int endAddress) {
        int sz = result.size();

        for (int i = sz - 1; i >= 0; i--) {
            Entry e = result.get(i);
            if (e.matches(spec)) {
                if (e.getStart() == endAddress) {
                    /*
                     * It turns out that the indicated entry doesn't actually
                     * cover any code.
                     */
                    result.remove(i);
                } else {
                    result.set(i, e.withEnd(endAddress));
                }
                return;
            }
        }

        throw new RuntimeException("unmatched variable: " + spec);
    }

    /**
     * Constructs an instance. All indices initially contain <code>null</code>.
     * 
     * @param size &gt;= 0; the size of the list
     */
    public LocalList(int size) {
        super(size);
    }

    /**
     * Gets the element at the given index. It is an error to call
     * this with the index for an element which was never set; if you
     * do that, this will throw <code>NullPointerException</code>.
     * 
     * @param n &gt;= 0, &lt; size(); which index
     * @return non-null; element at that index
     */
    public Entry get(int n) {
        return (Entry) get0(n);
    }

    /**
     * Sets the entry at the given index.
     * 
     * @param n &gt;= 0, &lt; size(); which index
     * @param start &gt;= 0; start address 
     * @param end &gt; start; end address (exclusive)
     * @param spec non-null; register spec representing the variable
     */
    public void set(int n, int start, int end, RegisterSpec spec) {
        set0(n, new Entry(start, end, spec));
    }

    /**
     * Sets the entry at the given index.
     * 
     * @param n &gt;= 0, &lt; size(); which index
     * @param entry non-null; the entry to set at <code>n</code>
     */
    public void set(int n, Entry entry) {
        set0(n, entry);
    }

    /**
     * Entry in a local list.
     */
    public static class Entry {
        /** &gt;= 0; start address */
        private final int start;

        /** &gt; start; end address (exclusive) */
        private final int end;

        /** non-null; register spec representing the variable */
        private final RegisterSpec spec;

        /** non-null; variable type */
        private final CstType type;

        /**
         * Constructs an instance.
         * 
         * @param start &gt;= 0; start address 
         * @param end &gt; start; end address (exclusive)
         * @param spec non-null; register spec representing the variable
         */
        public Entry(int start, int end, RegisterSpec spec) {
            if (start < 0) {
                throw new IllegalArgumentException("start < 0");
            }

            if (end <= start) {
                throw new IllegalArgumentException("end <= start");
            }

            try {
                if (spec.getLocalItem() == null) {
                    throw new NullPointerException(
                            "spec.getLocalItem() == null");
                }
            } catch (NullPointerException ex) {
                // Elucidate the exception.
                throw new NullPointerException("spec == null");
            }

            this.start = start;
            this.end = end;
            this.spec = spec;

            if (spec.getType() == Type.KNOWN_NULL) {
                /*
                 * KNOWN_NULL's descriptor is '<null>', which we do
                 * not want to emit. Everything else is as expected.
                 */
                this.type = CstType.OBJECT;
            } else {
                this.type = CstType.intern(spec.getType());
            }
        }

        /**
         * Gets the start address.
         * 
         * @return &gt;= 0; the start address
         */
        public int getStart() {
            return start;
        }

        /**
         * Gets the end address (exclusive).
         * 
         * @return &gt; start; the end address (exclusive)
         */
        public int getEnd() {
            return end;
        }

        /**
         * Gets the variable name.
         * 
         * @return null-ok; the variable name
         */
        public CstUtf8 getName() {
            return spec.getLocalItem().getName();
        }

        /**
         * Gets the variable signature.
         *
         * @return null-ok; the variable signature
         */
        public CstUtf8 getSignature() {
            return spec.getLocalItem().getSignature();
        }

        /**
         * Gets the variable's type.
         * 
         * @return non-null; the type
         */
        public CstType getType() {
            return type;
        }

        /**
         * Gets the number of the register holding the variable.
         * 
         * @return &gt;= 0; the number fo the register holding the variable
         */
        public int getRegister() {
            return spec.getReg();
        }

        /**
         * Gets the RegisterSpec of the register holding the variable.
         *
         * @return non-null; RegisterSpec of the holding register.
         */
        public RegisterSpec getRegisterSpec() {
            return spec;
        }

        /**
         * Returns whether or not this instance matches the given spec.
         * 
         * @param spec non-null; the spec in question
         * @return <code>true</code> iff this instance matches
         * <code>spec</code>
         */
        public boolean matches(RegisterSpec spec) {
            return spec.equals(this.spec);
        }

        /**
         * Returns whether or not this instance matches the spec in
         * the given instance.
         *
         * @param other non-null; another entry
         * @return <code>true</code> iff this instance's spec matches
         * <code>other</code>
         */
        public boolean matches(Entry other) {
            return other.spec.equals(this.spec);
        }

        /**
         * Returns an instance just like this one, except with the end
         * address altered to be the one given.
         * 
         * @param newEnd &gt; getStart(); the end address of the new instance
         * @return non-null; an appropriately-constructed instance
         */
        public Entry withEnd(int newEnd) {
            return new Entry(start, newEnd, spec);
        }
    }
}
