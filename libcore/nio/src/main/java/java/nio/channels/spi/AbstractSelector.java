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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for selectors.
 * <p>
 * This class realizes the interruption of selection by <code>begin</code> and
 * <code>end</code>. It also holds the cancelled and the deletion of the key
 * set.
 * </p>
 * 
 */
public abstract class AbstractSelector extends Selector {
    private volatile boolean isOpen = true;

    private SelectorProvider provider = null;

    /*
     * Set of cancelled keys.
     */
    private Set<SelectionKey> cancelledKeysSet = new HashSet<SelectionKey>();

    /**
     * Constructor for this class.
     * 
     * @param selectorProvider
     *            A instance of SelectorProvider
     */
    protected AbstractSelector(SelectorProvider selectorProvider) {
        provider = selectorProvider;
    }

    /**
     * Closes this channel.
     * 
     * @see java.nio.channels.Selector#close()
     */
    public synchronized final void close() throws IOException {
        if (isOpen) {
            isOpen = false;
            implCloseSelector();
        }
    }

    /**
     * Implements the closing of this channel.
     * 
     * @throws IOException
     *             If some I/O exception occurs.
     */
    protected abstract void implCloseSelector() throws IOException;

    /**
     * @see java.nio.channels.Selector#isOpen()
     */
    public final boolean isOpen() {
        return isOpen;
    }

    /**
     * Returns the SelectorProvider of this channel.
     * 
     * @see java.nio.channels.Selector#provider()
     */
    public final SelectorProvider provider() {
        return provider;
    }

    /**
     * Returns the cancelled key set of this channel.
     * 
     * @return The cancelled key set.
     */
    protected final Set<SelectionKey> cancelledKeys() {
        return cancelledKeysSet;
    }

    /**
     * Registers a channel to this selector.
     * 
     * @param channel
     *            The channel to be registered.
     * @param operations
     *            The interest set.
     * @param attachment
     *            The attachment of the key.
     * @return The key related with the channel and the selector.
     */
    protected abstract SelectionKey register(AbstractSelectableChannel channel,
            int operations, Object attachment);

    /**
     * Deletes the key from channel's key set.
     * 
     * @param key
     *            The key.
     */
    protected final void deregister(AbstractSelectionKey key) {
        ((AbstractSelectableChannel) key.channel()).deregister(key);
        key.isValid = false;
    }

    /**
     * This starts a potentially blocking I/O operation
     */
    protected final void begin() {
        // FIXME: be accommodate before VM actually provides
        // setInterruptAction method
        if (AbstractInterruptibleChannel.setInterruptAction != null) {
            try {
                AbstractInterruptibleChannel.setInterruptAction.invoke(Thread
                        .currentThread(), new Object[] { new Runnable() {
                    public void run() {
                        AbstractSelector.this.wakeup();
                    }
                } });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This ends a potentially blocking I/O operation
     */
    protected final void end() {
        // FIXME: be accommodate before VM actually provides
        // setInterruptAction method
        if (AbstractInterruptibleChannel.setInterruptAction != null) {
            try {
                AbstractInterruptibleChannel.setInterruptAction.invoke(Thread
                        .currentThread(), new Object[] { null });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * package private method for AbstractSelectionKey.cancel()
     */
    void cancel(SelectionKey key) {
        synchronized (cancelledKeysSet) {
            cancelledKeysSet.add(key);
        }
    }
}
