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

package org.apache.harmony.nio.tests.java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Set;

public class MockAbstractSelector extends AbstractSelector {
    
    class MockSelectionKey extends AbstractSelectionKey {

        boolean cancelled = false;
        Selector selector;
        SelectableChannel channel;
        
        MockSelectionKey(Selector sel, SelectableChannel chan) {
            selector = sel;
            channel = chan;
        }

        @Override
        public SelectableChannel channel() {
            return channel;
        }

        @Override
        public int interestOps() {
            return 0;
        }

        @Override
        public SelectionKey interestOps(int operations) {
            return null;
        }

        @Override
        public int readyOps() {
            return 0;
        }

        @Override
        public Selector selector() {
            return selector;
        }
    }

    public boolean isImplCloseSelectorCalled = false;
    private Set<SelectionKey> keys = new HashSet<SelectionKey>();
    public boolean isRegisterCalled = false;
    
    public MockAbstractSelector(SelectorProvider arg0) {
        super(arg0);
    }

    public static MockAbstractSelector openSelector() {
        return new MockAbstractSelector(SelectorProvider.provider());
    }

    public Set<SelectionKey> getCancelledKeys() {
        return super.cancelledKeys();
    }

    protected void implCloseSelector() throws IOException {
        isImplCloseSelectorCalled = true;
    }

    protected SelectionKey register(AbstractSelectableChannel arg0, int arg1,
            Object arg2) {
        isRegisterCalled = true;
        
        SelectionKey key = new MockSelectionKey(this, arg0);
        keys.add(key);
        return key;
    }

    public void superBegin() {
        super.begin();
    }

    public void superEnd() {
        super.end();
    }

    public void mockDeregister(AbstractSelectionKey key) {
        super.deregister(key);
    }

    public Set<SelectionKey> keys() {
        return keys;
    }

    public Set<SelectionKey> selectedKeys() {
        return null;
    }

    public int selectNow() throws IOException {
        return 0;
    }

    public int select(long arg0) throws IOException {
        return 0;
    }

    public int select() throws IOException {
        return 0;
    }

    public Selector wakeup() {
        return null;
    }

}
