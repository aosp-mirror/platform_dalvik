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
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * A controller for selection of SelectableChannel objects.
 * 
 * Selectable channels can be registered with a selector, and get SelectionKey
 * as a linkage. The keys are also added to the selector's keyset. The
 * SelectionKey can be cancelled so that the corresponding channel is no longer
 * registered with the selector.
 * 
 * By invoking the select operation, the keyset is checked and all keys that are
 * cancelled since last select operation are moved to cancelledKey set. During
 * the select operation, the channels registered with this selector are checked
 * to see whether they are ready for operation according to their interesting
 * operation.
 * 
 */
public abstract class Selector {

    /**
     * The factory method for selector.
     * 
     * @return a new selector
     * @throws IOException
     *             if I/O error occurs
     */
    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    /**
     * The constructor.
     */
    protected Selector() {
        super();
    }

    /**
     * Closes this selector.
     * 
     * @throws IOException
     *             if I/O error occurs
     */
    public abstract void close() throws IOException;

    /**
     * Tells whether this selector is open.
     * 
     * @return true if this selector is not closed
     */
    public abstract boolean isOpen();

    /**
     * Gets the set of registered keys.
     * 
     * @return the keyset of registered keys
     */
    public abstract Set<SelectionKey> keys();

    /**
     * Gets the provider of this selector.
     * 
     * @return the provider of this selector
     */
    public abstract SelectorProvider provider();

    /**
     * Detects if any of the registered channels are ready for I/O operations
     * according to their interesting operation. This operation will not return
     * until some of the channels are ready or wakeup is invoked.
     * 
     * @return the number of channels that are ready for operation
     * @throws IOException
     *             if I/O error occurs
     * @throws ClosedSelectorException
     *             If the selector is closed
     */
    public abstract int select() throws IOException;

    /**
     * Detects if any of the registered channels are ready for I/O operations
     * according to their interesting operation.This operation will not return
     * until some of the channels are ready or wakeup is invoked or timeout
     * expired.
     * 
     * @param timeout
     *            the timeout in millisecond
     * @return the number of channels that are ready for operation
     * @throws IOException
     *             if I/O error occurs
     * @throws ClosedSelectorException
     *             If the selector is closed
     * @throws IllegalArgumentException
     *             If the given timeout argument is less than zero
     */
    public abstract int select(long timeout) throws IOException;

    /**
     * Gets the keys whose channels are ready for operation.
     * 
     * @return the keys whose channels are ready for operation
     */
    public abstract Set<SelectionKey> selectedKeys();

    /**
     * Detects if any of the registered channels are ready for I/O operations
     * according to their interesting operation.This operation will not return
     * immediately.
     * 
     * @return the number of channels that are ready for operation
     * @throws IOException
     *             if I/O error occur
     * @throws ClosedSelectorException
     *             If the selector is closed
     */
    public abstract int selectNow() throws IOException;

    /**
     * Forces the blocked select operation to return immediately. If no select
     * operation is blocked currently, the next select operation shall return
     * immediately.
     * 
     * @return this selector
     * @throws ClosedSelectorException
     *             If the selector is closed
     */
    public abstract Selector wakeup();
}
