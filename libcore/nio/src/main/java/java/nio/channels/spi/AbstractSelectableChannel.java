/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for selectable channels.
 * <p>
 * In this class, there are methods about registering/deregistering a channel,
 * about channel closing. It realize the multi-thread safe.
 * </p>
 * 
 */
public abstract class AbstractSelectableChannel extends SelectableChannel {

    private final SelectorProvider provider;

    /*
     * The collection of key.
     */
    private List<SelectionKey> keyList = new ArrayList<SelectionKey>();

    private class BlockingLock {
    }

    private final Object blockingLock = new BlockingLock();

    boolean isBlocking = true;

    /**
     * Constructor for this class.
     * 
     * @param selectorProvider
     *            A instance of SelectorProvider
     */
    protected AbstractSelectableChannel(SelectorProvider selectorProvider) {
        super();
        provider = selectorProvider;
    }

    /**
     * Answer the SelectorProvider of this channel.
     * 
     * @see java.nio.channels.SelectableChannel#provider()
     * @return The provider of this channel.
     */
    public final SelectorProvider provider() {
        return provider;
    }

    /**
     * @see java.nio.channels.SelectableChannel#isRegistered()
     */
    synchronized public final boolean isRegistered() {
        return !keyList.isEmpty();
    }

    /**
     * @see java.nio.channels.SelectableChannel#keyFor(java.nio.channels.Selector)
     */
    synchronized public final SelectionKey keyFor(Selector selector) {
        for (int i = 0; i < keyList.size(); i++) {
            SelectionKey key = keyList.get(i);
            if (null != key && key.selector() == selector) {
                return key;
            }
        }
        return null;
    }

    /**
     * Realize the register function.
     * <p>
     * It registers current channel to the selector, then answer the selection
     * key. The channel must be open and the interest op set must be valid. If
     * the current channel is already registered to the selector, the method
     * only set the new interest op set; otherwise it will call the
     * <code>register</code> in <code>selector</code>, and add the relative
     * key to the key set of the current channel.
     * </p>
     * 
     * @see java.nio.channels.SelectableChannel#register(java.nio.channels.Selector,
     *      int, java.lang.Object)
     */
    public final SelectionKey register(Selector selector, int interestSet,
            Object attachment) throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!((interestSet & ~validOps()) == 0)) {
            throw new IllegalArgumentException();
        }

        synchronized (blockingLock) {
            if (isBlocking) {
                throw new IllegalBlockingModeException();
            }
            if (!selector.isOpen()) {
                if (0 == interestSet) {
                    // throw ISE exactly to keep consistency
                    throw new IllegalSelectorException();
                }
                // throw NPE exactly to keep consistency
                throw new NullPointerException();
            }
            if (0 == interestSet) {
                // throw ISE exactly to keep consistency
                throw new IllegalSelectorException();
            }
            SelectionKey key = keyFor(selector);
            if (null == key) {
                key = ((AbstractSelector) selector).register(this, interestSet,
                        attachment);
                keyList.add(key);
            } else {
                if (!key.isValid()) {
                    throw new CancelledKeyException();
                }
                key.interestOps(interestSet);
                key.attach(attachment);
            }
            return key;
        }
    }

    /**
     * Implement the closing function.
     * 
     * @see java.nio.channels.spi.AbstractInterruptibleChannel#implCloseChannel()
     */
    synchronized protected final void implCloseChannel() throws IOException {
        implCloseSelectableChannel();
        for (int i = 0; i < keyList.size(); i++) {
            SelectionKey key = keyList.get(i);
            if (null != key) {
                key.cancel();
            }
        }
    }

    /**
     * Implement the closing function of the SelectableChannel.
     * 
     * @throws IOException
     *             If some I/O exception occurred.
     */
    protected abstract void implCloseSelectableChannel() throws IOException;

    /**
     * @see java.nio.channels.SelectableChannel#isBlocking()
     */
    public final boolean isBlocking() {
        synchronized (blockingLock) {
            return isBlocking;
        }
    }

    /**
     * @see java.nio.channels.SelectableChannel#blockingLock()
     */
    public final Object blockingLock() {
        return blockingLock;
    }

    /**
     * Set the blocking mode of this channel.
     * 
     * @see java.nio.channels.SelectableChannel#configureBlocking(boolean)
     * @param blockingMode
     *            <code>true</code> for blocking mode; <code>false</code>
     *            for non-blocking mode.
     */
    public final SelectableChannel configureBlocking(boolean blockingMode)
            throws IOException {
        if (isOpen()) {
            synchronized (blockingLock) {
                if (isBlocking == blockingMode) {
                    return this;
                }
                if (blockingMode && isRegistered()) {
                    throw new IllegalBlockingModeException();
                }
                implConfigureBlocking(blockingMode);
                isBlocking = blockingMode;
            }
            return this;
        }
        throw new ClosedChannelException();

    }

    /**
     * Implement the setting of blocking mode.
     * 
     * @param blockingMode
     *            <code>true</code> for blocking mode; <code>false</code>
     *            for non-blocking mode.
     * @throws IOException
     *             If some I/O exception occurred.
     */
    protected abstract void implConfigureBlocking(boolean blockingMode)
            throws IOException;

    /*
     * package private for deregister method in AbstractSelector.
     */
    synchronized void deregister(SelectionKey k) {
        if (null != keyList) {
            keyList.remove(k);
        }
    }

}
