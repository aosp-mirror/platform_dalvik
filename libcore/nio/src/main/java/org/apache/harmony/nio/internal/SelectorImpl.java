/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.nio.internal;

// BEGIN android-note
// This class differs significantly from Harmony. They have adopted indices to
// track selection keys as-they-change; we avoid that cost by tracking keys on
// calls to select().
// END android-note

import org.apache.harmony.luni.platform.FileDescriptorHandler;
import org.apache.harmony.luni.platform.Platform;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.*;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * Default implementation of java.nio.channels.Selector
 */
final class SelectorImpl extends AbstractSelector {

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private static final FileDescriptor[] EMPTY_FILE_DESCRIPTORS_ARRAY
            = new FileDescriptor[0];
    private static final SelectionKeyImpl[] EMPTY_SELECTION_KEY_IMPLS_ARRAY
            = new SelectionKeyImpl[0];

    private static final int CONNECT_OR_WRITE = OP_CONNECT | OP_WRITE;

    private static final int ACCEPT_OR_READ = OP_ACCEPT | OP_READ;

    private static final int MOCK_WRITEBUF_SIZE = 1;

    private static final int MOCK_READBUF_SIZE = 8;

    private static final int NA = 0;

    private static final int READABLE = 1;

    private static final int WRITEABLE = 2;

    private static final int SELECT_BLOCK = -1;

    private static final int SELECT_NOW = 0;

    /**
     * Used to synchronize when a key's interest ops change.
     */
    private static class KeysLock {}
    final Object keysLock = new KeysLock();

    private final Set<SelectionKeyImpl> mutableKeys = new HashSet<SelectionKeyImpl>();

    /**
     * The unmodifiable set of keys as exposed to the user. This object is used
     * for synchronization.
     */
    private Set<SelectionKey> unmodifiableKeys = Collections
            .<SelectionKey>unmodifiableSet(mutableKeys);

    private final Set<SelectionKey> mutableSelectedKeys = new HashSet<SelectionKey>();

    /**
     * The unmodifiable set of selectable keys as seen by the user. This object
     * is used for synchronization.
     */
    private final Set<SelectionKey> selectedKeys
            = new UnaddableSet<SelectionKey>(mutableSelectedKeys);

    /**
     * File descriptors we're interested in reading from. When actively
     * selecting, the first element is always the mock channel's file
     * descriptor, and the other elements are user-specified file descriptors.
     * Otherwise, all elements are null.
     */
    private FileDescriptor[] readableFDs = EMPTY_FILE_DESCRIPTORS_ARRAY;

    /**
     * File descriptors we're interested in writing from. May be empty. When not
     * actively selecting, all elements are null.
     */
    private FileDescriptor[] writableFDs = EMPTY_FILE_DESCRIPTORS_ARRAY;

    /**
     * Selection keys that correspond to the concatenation of readableFDs and
     * writableFDs. This is used to interpret the results returned by select().
     * When not actively selecting, all elements are null.
     */
    private SelectionKeyImpl[] readyKeys = EMPTY_SELECTION_KEY_IMPLS_ARRAY;

    /**
     * Selection flags that define the ready ops on the ready keys. When not
     * actively selecting, all elements are 0. Corresponds to the ready keys
     * set.
     */
    private int[] flags = EMPTY_INT_ARRAY;

    /**
     * A mock channel is used to signal wakeups. Whenever the selector should
     * stop blocking on a select(), a byte is written to the sink and will be
     * picked up in source by the selecting thread.
     */
    private Pipe.SinkChannel sink;
    private Pipe.SourceChannel source;
    private FileDescriptor sourcefd;

    public SelectorImpl(SelectorProvider selectorProvider) {
        super(selectorProvider);
        try {
            Pipe mockSelector = selectorProvider.openPipe();
            sink = mockSelector.sink();
            source = mockSelector.source();
            sourcefd = ((FileDescriptorHandler) source).getFD();
            source.configureBlocking(false);
        } catch (IOException e) {
            // TODO: throw assertion error once IPv6+loopback is fixed
            e.printStackTrace();
        }
    }

    /**
     * @see java.nio.channels.spi.AbstractSelector#implCloseSelector()
     */
    @Override
    protected void implCloseSelector() throws IOException {
        wakeup();
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                synchronized (selectedKeys) {
                    doCancel();
                    for (SelectionKey sk : mutableKeys) {
                        deregister((AbstractSelectionKey) sk);
                    }
                }
            }
        }
    }

    /**
     * @see java.nio.channels.spi.AbstractSelector#register(java.nio.channels.spi.AbstractSelectableChannel,
     *      int, java.lang.Object)
     */
    @Override
    protected SelectionKey register(AbstractSelectableChannel channel,
            int operations, Object attachment) {
        if (!provider().equals(channel.provider())) {
            throw new IllegalSelectorException();
        }
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                SelectionKeyImpl selectionKey = new SelectionKeyImpl(
                        channel, operations, attachment, this);
                mutableKeys.add(selectionKey);
                return selectionKey;
            }
        }
    }

    /**
     * @see java.nio.channels.Selector#keys()
     */
    @Override
    public synchronized Set<SelectionKey> keys() {
        closeCheck();
        return unmodifiableKeys;
    }

    /*
     * Checks that the receiver is not closed. If it is throws an exception.
     */
    private void closeCheck() {
        if (!isOpen()) {
            throw new ClosedSelectorException();
        }
    }

    /**
     * @see java.nio.channels.Selector#select()
     */
    @Override
    public int select() throws IOException {
        return selectInternal(SELECT_BLOCK);
    }

    /**
     * @see java.nio.channels.Selector#select(long)
     */
    @Override
    public int select(long timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }
        return selectInternal((0 == timeout) ? SELECT_BLOCK : timeout);
    }

    /**
     * @see java.nio.channels.Selector#selectNow()
     */
    @Override
    public int selectNow() throws IOException {
        return selectInternal(SELECT_NOW);
    }

    private int selectInternal(long timeout) throws IOException {
        closeCheck();
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                synchronized (selectedKeys) {
                    doCancel();
                    boolean isBlock = (SELECT_NOW != timeout);
                    int readableKeysCount = 1; // first is always the mock channel
                    int writableKeysCount = 0;
                    synchronized (keysLock) {
                        for (SelectionKeyImpl key : mutableKeys) {
                            int ops = key.interestOpsNoCheck();
                            if ((ACCEPT_OR_READ & ops) != 0) {
                                readableKeysCount++;
                            }
                            if ((CONNECT_OR_WRITE & ops) != 0) {
                                writableKeysCount++;
                            }
                        }
                        prepareChannels(readableKeysCount, writableKeysCount);
                    }
                    boolean success;
                    try {
                        if (isBlock) {
                            begin();
                        }
                        success = Platform.getNetworkSystem().select(
                                readableFDs, writableFDs, readableKeysCount, writableKeysCount, timeout, flags);
                    } finally {
                        if (isBlock) {
                            end();
                        }
                    }

                    int selected = success ? processSelectResult() : 0;

                    Arrays.fill(readableFDs, null);
                    Arrays.fill(writableFDs, null);
                    Arrays.fill(readyKeys, null);
                    Arrays.fill(flags, 0);

                    selected -= doCancel();

                    return selected;
                }
            }
        }
    }

    /**
     * Prepare the readableFDs, writableFDs, readyKeys and flags arrays in
     * preparation for a call to {@code INetworkSystem#select()}. After they're
     * used, the array elements must be cleared.
     */
    private void prepareChannels(int numReadable, int numWritable) {
        // grow each array to sufficient capacity. Always grow to at least 1.5x
        // to avoid growing too frequently
        if (readableFDs.length < numReadable) {
            int newSize = Math.max((int) (readableFDs.length * 1.5f), numReadable);
            readableFDs = new FileDescriptor[newSize];
        }
        if (writableFDs.length < numWritable) {
            int newSize = Math.max((int) (writableFDs.length * 1.5f), numWritable);
            writableFDs = new FileDescriptor[newSize];
        }
        int total = numReadable + numWritable;
        if (readyKeys.length < total) {
            int newSize = Math.max((int) (readyKeys.length * 1.5f), total);
            readyKeys = new SelectionKeyImpl[newSize];
            flags = new int[newSize];
        }

        // populate the FDs, including the mock channel
        readableFDs[0] = sourcefd;
        int r = 1;
        int w = 0;
        for (SelectionKeyImpl key : mutableKeys) {
            int interestOps = key.interestOpsNoCheck();
            if ((ACCEPT_OR_READ & interestOps) != 0) {
                readableFDs[r] = ((FileDescriptorHandler) key.channel()).getFD();
                readyKeys[r] = key;
                r++;
            }
            if ((CONNECT_OR_WRITE & interestOps) != 0) {
                writableFDs[w] = ((FileDescriptorHandler) key.channel()).getFD();
                readyKeys[w + numReadable] = key;
                w++;
            }
        }
    }

    /**
     * Updates the key ready ops and selected key set with data from the flags
     * array.
     */
    private int processSelectResult() throws IOException {
        // if the mock channel is selected, read the content.
        if (READABLE == flags[0]) {
            ByteBuffer readbuf = ByteBuffer.allocate(MOCK_READBUF_SIZE);
            while (source.read(readbuf) > 0) {
                readbuf.flip();
            }
        }
        int selected = 0;

        for (int i = 1; i < flags.length; i++) {
            if (flags[i] == NA) {
                continue;
            }

            SelectionKeyImpl key = readyKeys[i];
            int ops = key.interestOpsNoCheck();
            int selectedOp = 0;

            switch (flags[i]) {
                case READABLE:
                    selectedOp = ACCEPT_OR_READ & ops;
                    break;
                case WRITEABLE:
                    if (key.isConnected()) {
                        selectedOp = OP_WRITE & ops;
                    } else {
                        selectedOp = OP_CONNECT & ops;
                    }
                    break;
            }

            if (0 != selectedOp) {
                boolean wasSelected = mutableSelectedKeys.contains(key);
                if (wasSelected && key.readyOps() != selectedOp) {
                    key.setReadyOps(key.readyOps() | selectedOp);
                    selected++;
                } else if (!wasSelected) {
                    key.setReadyOps(selectedOp);
                    mutableSelectedKeys.add(key);
                    selected++;
                }
            }
        }

        return selected;
    }

    /**
     * @see java.nio.channels.Selector#selectedKeys()
     */
    @Override
    public synchronized Set<SelectionKey> selectedKeys() {
        closeCheck();
        return selectedKeys;
    }

    /**
     * Removes cancelled keys from the key set and selected key set, and
     * deregisters the corresponding channels. Returns the number of keys
     * removed from the selected key set.
     */
    private int doCancel() {
        int deselected = 0;

        Set<SelectionKey> cancelledKeys = cancelledKeys();
        synchronized (cancelledKeys) {
            if (cancelledKeys.size() > 0) {
                for (SelectionKey currentkey : cancelledKeys) {
                    mutableKeys.remove(currentkey);
                    deregister((AbstractSelectionKey) currentkey);
                    if (mutableSelectedKeys.remove(currentkey)) {
                        deselected++;
                    }
                }
                cancelledKeys.clear();
            }
        }

        return deselected;
    }

    /**
     * @see java.nio.channels.Selector#wakeup()
     */
    @Override
    public Selector wakeup() {
        try {
            sink.write(ByteBuffer.allocate(MOCK_WRITEBUF_SIZE));
        } catch (IOException e) {
            // do nothing
        }
        return this;
    }

    private static class UnaddableSet<E> implements Set<E> {

        private final Set<E> set;

        UnaddableSet(Set<E> set) {
            this.set = set;
        }

        @Override
        public boolean equals(Object object) {
            return set.equals(object);
        }

        @Override
        public int hashCode() {
            return set.hashCode();
        }

        public boolean add(E object) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            set.clear();
        }

        public boolean contains(Object object) {
            return set.contains(object);
        }

        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        public boolean isEmpty() {
            return set.isEmpty();
        }

        public Iterator<E> iterator() {
            return set.iterator();
        }

        public boolean remove(Object object) {
            return set.remove(object);
        }

        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }

        public int size() {
            return set.size();
        }

        public Object[] toArray() {
            return set.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }
    }
}
