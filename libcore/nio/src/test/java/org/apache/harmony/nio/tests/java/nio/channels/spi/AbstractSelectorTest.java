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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.spi.AbstractSelector;
import java.util.Set;

import junit.framework.TestCase;
@TestTargetClass(AbstractSelector.class)
/**
 * Tests for AbstractSelector and register of its default implementation
 */ 
public class AbstractSelectorTest extends TestCase {

    /**
     * @tests AbstractSelector#provider()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "AbstractSelector",
            args = {SelectorProvider.class}
        )
    })
    public void test_provider() throws IOException {
        Selector mockSelector = new MockAbstractSelector(SelectorProvider
                .provider());
        assertTrue(mockSelector.isOpen());
        assertSame(SelectorProvider.provider(), mockSelector.provider());
        mockSelector = new MockAbstractSelector(null);
        assertNull(mockSelector.provider());
    }

    /**
     * @tests AbstractSelector#close()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implCloseSelector",
            args = {}
        )
    })
    public void test_close() throws IOException {
        MockAbstractSelector mockSelector = new MockAbstractSelector(
                SelectorProvider.provider());
        mockSelector.close();
        assertTrue(mockSelector.isImplCloseSelectorCalled);
    }

    /**
     * 
     * @tests AbstractSelector#begin/end()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "begin",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "end",
            args = {}
        )
    })
    public void test_begin_end() throws IOException {
        MockAbstractSelector mockSelector = new MockAbstractSelector(
                SelectorProvider.provider());     
        try {
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
        }
        
        mockSelector = new MockAbstractSelector(SelectorProvider.provider());
        try {
            mockSelector.superBegin();
            mockSelector.close();
        } finally {
            mockSelector.superEnd();
        }
       
        try {
            // begin twice
            mockSelector.superBegin();
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
        }
        
        try {
            mockSelector.superBegin();
        } finally {
            // end twice
            mockSelector.superEnd();
            mockSelector.superEnd();
        }

        mockSelector.close();
        try {
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
        }
    }
    
    /**
     * @tests AbstractSelector#isOpen()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isOpen",
        args = {}
    )
    public void test_isOpen() throws Exception {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        assertTrue(acceptSelector.isOpen());
        acceptSelector.close();
        assertFalse(acceptSelector.isOpen());
    }
    
    /**
     * @tests AbstractSelector()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "AbstractSelector",
        args = {SelectorProvider.class}
    )
    public void test_Constructor_LSelectorProvider() throws Exception {
        Selector acceptSelector = new MockAbstractSelector(
                SelectorProvider.provider());
        assertSame(SelectorProvider.provider(), acceptSelector.provider());
    }
    
    /**
     * @tests AbstractSelector#register(AbstractSelectableChannel,int,Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies register method from SelectableChannel class.",
        method = "register",
        args = {AbstractSelectableChannel.class, int.class, java.lang.Object.class}
    )   
    public void test_register_LAbstractSelectableChannelIObject() 
            throws Exception {
        Selector acceptSelector = new MockSelectorProvider().openSelector();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        assertFalse(ssc.isRegistered());
        ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
        assertTrue(ssc.isRegistered());
        assertTrue(((MockAbstractSelector)acceptSelector).isRegisterCalled);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "cancelledKeys",
        args = {}
    )    
    public void test_cancelledKeys() throws Exception {
        MockSelectorProvider prov = new MockSelectorProvider();
        Selector acceptSelector = prov.openSelector();
        SocketChannel sc = prov.openSocketChannel();
        sc.configureBlocking(false);

        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        acceptKey.cancel();
        Set<SelectionKey> cKeys = 
                ((MockAbstractSelector)acceptSelector).getCancelledKeys();
        assertTrue(cKeys.contains(acceptKey));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "deregister",
        args = {AbstractSelectionKey.class}
    )
    public void test_deregister() throws Exception {
        MockSelectorProvider prov = new MockSelectorProvider();
        AbstractSelector acceptSelector = prov.openSelector();
        SocketChannel sc = prov.openSocketChannel();
        sc.configureBlocking(false);

        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        assertTrue(sc.isRegistered());
        assertNotNull(acceptKey);
        ((MockAbstractSelector)acceptSelector).mockDeregister(
                (MockAbstractSelector.MockSelectionKey)acceptKey);
        assertFalse(sc.isRegistered());
    }

    static class MockSelectorProvider extends SelectorProvider {
        
        private  MockSelectorProvider() {
            // do nothing
        }

        @Override
        public DatagramChannel openDatagramChannel() {
            return null;
        }

        @Override
        public Pipe openPipe() {
            return null;
        }

        @Override
        public AbstractSelector openSelector() {
            return new MockAbstractSelector(provider());
        }

        @Override
        public ServerSocketChannel openServerSocketChannel() {
            return null;
        }

        @Override
        public SocketChannel openSocketChannel() throws IOException {
            return SocketChannel.open();
        }

        public static SelectorProvider provider() {
            return new MockSelectorProvider();
        }
    }
}
