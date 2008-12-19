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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.io.IOException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.spi.AbstractSelector;

import junit.framework.TestCase;
@TestTargetClass(AbstractSelector.class)
/**
 * Tests for AbstractSelector and register of its default implementation
 */ 
public class AbstractSelectorTest extends TestCase {

    /**
     * @tests AbstractSelector#provider()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "provider",
          methodArgs = {}
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
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "close",
          methodArgs = {}
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
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "begin",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "end",
          methodArgs = {}
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
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isOpen",
          methodArgs = {}
        )
    })
    public void test_isOpen() throws Exception {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        assertTrue(acceptSelector.isOpen());
        acceptSelector.close();
        assertFalse(acceptSelector.isOpen());
    }
    
    /**
     * @tests AbstractSelector#register(Selector,int)
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies register method from SelectableChannel " +
                    "class.",
            targets = {
              @TestTarget(
                methodName = "register",
                methodArgs = {Selector.class, int.class}
              )
     })   
    public void test_register_LSelectorI() throws Exception {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        assertFalse(ssc.isRegistered());
        SelectionKey acceptKey = ssc.register(acceptSelector,
                SelectionKey.OP_ACCEPT);
        assertTrue(ssc.isRegistered());
        assertNotNull(acceptKey);
        assertTrue(acceptSelector.keys().contains(acceptKey));
    }

    /**
     * @tests AbstractSelector#register(Selector,int)
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies register method from SelectableChannel " +
                    "class.",
            targets = {
              @TestTarget(
                methodName = "register",
                methodArgs = {Selector.class, int.class}
              )
     })    
    public void test_register_LSelectorI_error() throws IOException {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        acceptSelector.close();

        assertFalse(acceptSelector.isOpen());
        try {
            ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        assertFalse(ssc.isRegistered());

        acceptSelector = Selector.open();
        ssc.configureBlocking(true);
        try {
            ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        assertFalse(ssc.isRegistered());
        ssc.configureBlocking(false);
        SelectionKey acceptKey = ssc.register(acceptSelector,
                SelectionKey.OP_ACCEPT);
        assertNotNull(acceptKey);
        assertTrue(acceptSelector.keys().contains(acceptKey));
        assertTrue(ssc.isRegistered());
    }    
}
