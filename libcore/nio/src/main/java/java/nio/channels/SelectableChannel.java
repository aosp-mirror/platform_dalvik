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

package java.nio.channels;


import java.io.IOException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A channel that can be detected by a selector. The channel can be registered
 * with some selectors, and when invoke select method of the selectors, the
 * channel can be checked if it is readable, writable, connectable or
 * acceptable according to its interesting operation.
 * 
 */
public abstract class SelectableChannel extends AbstractInterruptibleChannel
        implements Channel {

    /**
     * Default constructor, can be overridden.
     */
    protected SelectableChannel() {
        super();
    }

    /**
     * Gets the blocking lock which synchronizes the configureBlocking and
     * register methods.
     * 
     * @return the blocking object as lock
     */
    public abstract Object blockingLock();

    /**
     * Sets blocking mode of the channel.
     * 
     * @param block
     *            true as blocking, false as non-blocking
     * @return this channel
     * @throws ClosedChannelException
     *             If this channel has been closed
     * @throws IllegalBlockingModeException
     *             If the channel has been registered
     * @throws IOException
     *             if I/O error occurs
     */
    public abstract SelectableChannel configureBlocking(boolean block)
            throws IOException;

    /**
     * Returns if channel is in blocking mode.
     * 
     * @return true if channel is blocking
     */
    public abstract boolean isBlocking();

    /**
     * Returns if channel is registered.
     * 
     * @return true if channel is registered
     */
    public abstract boolean isRegistered();

    /**
     * Gets the selection key for the channel with the given selector.
     * 
     * @param sel
     *            the selector with which this channel may register
     * @return the selection key for the channel according to the given selector
     */
    public abstract SelectionKey keyFor(Selector sel);

    /**
     * Gets the provider of the channel.
     * 
     * @return the provider of the channel
     */
    public abstract SelectorProvider provider();

    /**
     * Registers with the given selector with a certain interesting operation.
     * 
     * @param selector
     *            the selector with which this channel shall be registered
     * @param operations
     *            the interesting operation
     * @return the selection key indicates the channel
     * @throws ClosedChannelException
     *             if the channel is closed
     * @throws IllegalBlockingModeException
     *             If the channel is in blocking mode
     * @throws IllegalSelectorException
     *             If this channel does not have the same provider as the
     *             given selector
     * @throws CancelledKeyException
     *             If this channel is registered but its key has been cancelled
     * @throws IllegalArgumentException
     *             If the operation given is unsupported by this channel
     */
    public final SelectionKey register(Selector selector, int operations)
            throws ClosedChannelException {
        return register(selector, operations, null);
    }

    /**
     * Registers with the given selector with a certain interesting operation
     * and an attached object.
     * 
     * @param sel
     *            the selector with which this channel shall be registered
     * @param ops
     *            the interesting operation
     * @param att
     *            The attached object, which can be null
     * @return the selection key indicates the channel
     * @throws ClosedChannelException
     *             if the channel is closed
     * @throws IllegalBlockingModeException
     *             If the channel is in blocking mode
     * @throws IllegalSelectorException
     *             If this channel does not have the same provider with the
     *             given selector
     * @throws CancelledKeyException
     *             If this channel is registered but its key has been cancelled
     * @throws IllegalArgumentException
     *             If the operation given is unsupported by this channel
     */
    public abstract SelectionKey register(Selector sel, int ops, Object att)
            throws ClosedChannelException;

    /**
     * Gets the possible interesting operation of the channel.
     * 
     * @return the possible interesting operation of the channel
     */
    public abstract int validOps();

}
