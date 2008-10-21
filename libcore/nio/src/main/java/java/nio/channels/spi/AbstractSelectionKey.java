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
 
package java.nio.channels.spi;

import java.nio.channels.SelectionKey;

/**
 * Abstract class for selection key.
 * <p>
 * The class takes charge of the validation and cancellation of key.
 * </p>
 * 
 */
public abstract class AbstractSelectionKey extends SelectionKey {

    /*
     * package private for deregister method in AbstractSelector.
     */
    boolean isValid = true;

    /**
     * Constructor for this class.
     */
    protected AbstractSelectionKey() {
        super();
    }

    /**
     * @see java.nio.channels.SelectionKey#isValid()
     */
    public final boolean isValid() {
        return isValid;
    }

    /**
     * Cancels this key and adds it to the cancelled key set.
     * 
     * @see java.nio.channels.SelectionKey#cancel()
     */
    public final void cancel() {
        if (isValid) {
            isValid = false;
            ((AbstractSelector) selector()).cancel(this);
        }
    }
}
