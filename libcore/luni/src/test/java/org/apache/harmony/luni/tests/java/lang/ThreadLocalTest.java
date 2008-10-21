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

package org.apache.harmony.luni.tests.java.lang;

import junit.framework.TestCase;

public class ThreadLocalTest extends TestCase {

    /**
     * @tests java.lang.ThreadLocal#ThreadLocal()
     */
    public void test_Constructor() {
        try {
            new ThreadLocal<Object>();
        } catch (Exception e) {
            fail("unexpected exception: " + e.toString());
        }
    }
    
    /**
     * @tests java.lang.ThreadLocal#remove()
     */
    public void test_remove() {
        ThreadLocal<String> tl = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "initial";
            }
        };
        
        assertEquals("initial", tl.get());
        tl.set("fixture");
        assertEquals("fixture", tl.get());
        tl.remove();
        assertEquals("initial", tl.get());
    }

    /**
     * @tests java.lang.ThreadLocal#get()
     */
    public void test_get() {
        // Test for method java.lang.Object java.lang.ThreadLocal.get()
        ThreadLocal<Object> l = new ThreadLocal<Object>();
        assertNull("ThreadLocal's initial value is null", l.get());

        // The ThreadLocal has to run once for each thread that touches the
        // ThreadLocal
        final Object INITIAL_VALUE = "'foo'";
        final ThreadLocal<Object> l1 = new ThreadLocal<Object>() {
            @Override
            protected Object initialValue() {
                return INITIAL_VALUE;
            }
        };

        assertTrue("ThreadLocal's initial value should be " + INITIAL_VALUE
                + " but is " + l1.get(), l1.get() == INITIAL_VALUE);

        // We need this because inner types cannot assign to variables in
        // container method. But assigning to object slots in the container
        // method is ok.
        class ResultSlot {
            public Object result = null;
        }

        final ResultSlot THREADVALUE = new ResultSlot();
        Thread t = new Thread() {
            @Override
            public void run() {
                THREADVALUE.result = l1.get();
            }
        };

        // Wait for the other Thread assign what it observes as the value of the
        // variable
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {
            fail("Interrupted!!");
        }

        assertTrue("ThreadLocal's initial value in other Thread should be "
                + INITIAL_VALUE, THREADVALUE.result == INITIAL_VALUE);

        /* Regression test for implementation vulnerability reported
         * on Harmony dev list.
         */
       ThreadLocal<Object> thrVar = new ThreadLocal<Object>() {
           public int hashCode() {
               fail("ThreadLocal should not be asked for it's hashCode");
               return 0; // never reached
           }
       };
       thrVar.get();
    }

    /**
     * @tests java.lang.ThreadLocal#set(java.lang.Object)
     */
    public void test_setLjava_lang_Object() {
        // Test for method void java.lang.ThreadLocal.set(java.lang.Object)

        final Object OBJ = new Object();
        final ThreadLocal<Object> l = new ThreadLocal<Object>();
        l.set(OBJ);
        assertTrue("ThreadLocal's initial value is " + OBJ, l.get() == OBJ);

        // We need this because inner types cannot assign to variables in
        // container method.
        // But assigning to object slots in the container method is ok.
        class ResultSlot {
            public Object result = null;
        }

        final ResultSlot THREADVALUE = new ResultSlot();
        Thread t = new Thread() {
            @Override
            public void run() {
                THREADVALUE.result = l.get();
            }
        };

        // Wait for the other Thread assign what it observes as the value of the
        // variable
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {
            fail("Interrupted!!");
        }

        // ThreadLocal is not inherited, so the other Thread should see it as
        // null
        assertNull("ThreadLocal's value in other Thread should be null",
                THREADVALUE.result);

    }

    /**
     * @tests java.lang.InheritableThreadLocal
     */
    public void test_Ljava_lang_InheritableThreadLocal()
            throws InterruptedException {
        final Object value = new Object();
        final Object inheritedValue = new Object();
        final ThreadLocal<Object> threadLocal
                = new InheritableThreadLocal<Object>() {
            @Override
            protected Object childValue(Object parentValue) {
                assertSame(value, parentValue);
                return inheritedValue;
            }
        };
        threadLocal.set(value);
        final Object[] holder = new Object[1];
        Thread thread = new Thread() {
            public void run() {
                holder[0] = threadLocal.get();
            }
        };
        thread.start();
        thread.join();
        assertSame(value, threadLocal.get());
        assertSame(inheritedValue, holder[0]);
    }
}
