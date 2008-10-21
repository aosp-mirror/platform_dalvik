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

import java.nio.channels.Selector;

/**
 * A key that representing the relationship of a channel and the selector.
 * 
 */
public abstract class SelectionKey {

    /**
     * Interesting operation mask bit for socket-accept operations.
     */
    public static final int OP_ACCEPT = 16;

    /**
     * Interesting operation mask bit for socket-connect operations.
     */
    public static final int OP_CONNECT = 8;

    /**
     * Interesting operation mask bit for read operations.
     */
    public static final int OP_READ = 1;

    /**
     * Interesting operation mask bit for write operations.
     */
    public static final int OP_WRITE = 4;

    private volatile Object attachment = null;

    /**
     * The constructor.
     * 
     */
    protected SelectionKey() {
        super();
    }

    /**
     * Attaches an object to the key.
     * 
     * @param anObject
     *            the object to attach
     * @return the last attached object
     */
    public final Object attach(Object anObject) {
        Object oldAttachment = attachment;
        attachment = anObject;
        return oldAttachment;
    }

    /**
     * Gets the attached object.
     * 
     * @return the attached object or null if no object has been attached
     */
    public final Object attachment() {
        return attachment;
    }

    /**
     * Cancels this key.
     * 
     */
    public abstract void cancel();

    /**
     * Gets the channel of this key.
     * 
     * @return the channel of this key
     */
    public abstract SelectableChannel channel();

    /**
     * Gets the interesting operation of this key.
     * 
     * @return the interesting operation of this key
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public abstract int interestOps();

    /**
     * Sets the interesting operation for this key.
     * 
     * @param operations
     *            the interesting operation to set
     * @return this key
     * @throws IllegalArgumentException
     *             if the given operation is not in the key's interesting
     *             operation set
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public abstract SelectionKey interestOps(int operations);

    /**
     * Tells whether the channel of this key is interested in accept operation
     * and ready for acceptation.
     * 
     * @return true if the channel is interested in accept operation and ready
     *         for acceptation
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) == OP_ACCEPT;
    }

    /**
     * Tells whether the channel of this key is interested in connect operation
     * and ready for connection.
     * 
     * @return true if the channel is interested in connect operation and ready
     *         for connection
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) == OP_CONNECT;
    }

    /**
     * Tells whether the channel of this key is interested in read operation and
     * ready for reading.
     * 
     * @return true if the channel is interested in read operation and ready for
     *         reading
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public final boolean isReadable() {
        return (readyOps() & OP_READ) == OP_READ;
    }

    /**
     * Tells whether the key is valid.
     * 
     * @return true if the key has not been cancelled
     */
    public abstract boolean isValid();

    /**
     * Tells whether the channel of this key is interested in write operation
     * and ready for writing.
     * 
     * @return true if the channel is interested in write operation and ready
     *         for writing
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) == OP_WRITE;
    }

    /**
     * Gets the ready operation.
     * 
     * @return the ready operation
     * @throws CancelledKeyException
     *             If the key has been cancelled already
     */
    public abstract int readyOps();

    /**
     * Gets the related selector.
     * 
     * @return the related selector
     */
    public abstract Selector selector();
}
