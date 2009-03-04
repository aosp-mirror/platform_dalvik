/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.luni.tests.java.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

import tests.support.Support_ASimpleInputStream;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(ObjectInputStream.class)
public class ObjectInputStreamTest extends TestCase {

    ObjectInputStream ois;

    ObjectOutputStream oos;

    ByteArrayOutputStream bao;
    
    private final String testString = "Lorem ipsum...";

    protected void setUp() throws Exception {
        super.setUp();
        oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks ObjectStreamException and OptionalDataException.",
        method = "readUnshared",
        args = {}
    )
    public void test_readUnshared_1() throws IOException, ClassNotFoundException {
        oos.writeObject(testString);
        oos.writeObject(testString);
        oos.writeInt(42);
        oos.close();

        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.readUnshared();
            ois.readObject();
            fail("Test 1: ObjectStreamException expected.");
        } catch (ObjectStreamException e) {
            // Expected.
        }
        
        try {
            ois.readUnshared();
            fail("Test 2: OptionalDataException expected.");
        } catch (OptionalDataException e) {
            // Expected.
        }
        ois.close();
    } 

    /**
     * @tests java.io.ObjectInputStream#readUnshared()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks StreamCorruptedException.",
        method = "readUnshared",
        args = {}
    )    
    public void test_readUnshared_2() throws IOException, ClassNotFoundException {
        oos.close();
        bao.write(testString.getBytes());
        
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.readUnshared();
            fail("Test 1: StreamCorruptedException expected.");
        } catch (StreamCorruptedException e) {
            // Expected.
        }
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readUnshared()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "readUnshared",
        args = {}
    )    
    public void test_readUnshared_3() throws IOException, ClassNotFoundException {
        bao.write(testString.getBytes());
        oos.close();

        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(bao.toByteArray());
        ois = new ObjectInputStream(sis);
        sis.throwExceptionOnNextUse = true;
        try {
            ois.readUnshared();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        sis.throwExceptionOnNextUse = false;
        ois.close();
    }

    /**
     * Micro-scenario of de/serialization of an object with non-serializable superclass.
     * The super-constructor only should be invoked on the deserialized instance.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "readObject",
        args = {}
    )
    public void test_readObject_Hierarchy() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 

        ObjectOutputStream oos = new ObjectOutputStream(baos); 
        oos.writeObject(new B());
        oos.close(); 

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())); 
        B b = (B) ois.readObject();
        ois.close();
        
        assertTrue("should construct super", A.list.contains(b));
        assertFalse("should not construct self", B.list.contains(b));
        assertEquals("super field A.s", A.DEFAULT, ((A)b).s);
        assertNull("transient field B.s", b.s);
    }
    
    /**
     * @tests {@link java.io.ObjectInputStream#readNewLongString()}
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_readNewLongString() throws Exception {
        LongString longString = new LongString();
        SerializationTest.verifySelf(longString);
    }
    
    private static class LongString implements Serializable{
        private static final long serialVersionUID = 1L;
        String lString;
        
        public LongString() {
            StringBuilder builder = new StringBuilder();
            // construct a string whose length > 64K
            for (int i = 0; i < 65636; i++) {
                builder.append('1');
            }
            lString = builder.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof LongString) {
                LongString l = (LongString) o;
                return l.lString.equals(l.lString);
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return lString.hashCode();
        }
    }

    static class A { 
        static final ArrayList<A> list = new ArrayList<A>();  
        String s;
        public static final String DEFAULT = "aaa";
        public A() {
            s = DEFAULT;
            list.add(this);
        }
    } 

    static class B extends A implements Serializable { 
        private static final long serialVersionUID = 1L;
        static final ArrayList<A> list = new ArrayList<A>();  
        transient String s;
        public B() {
            s = "bbb";
            list.add(this);
        }
    }     
    
    class OIS extends ObjectInputStream {
        
        OIS () throws IOException {
            super();
         }
        
        void test() throws ClassNotFoundException,IOException {
            readClassDescriptor();
        }
        
    }
 
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "readClassDescriptor",
        args = {}
    )
    public void test_readClassDescriptor() throws ClassNotFoundException, IOException {
        try {
            new OIS().test();
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }

    static class TestObjectInputStream extends ObjectInputStream {
        public TestObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        protected Class<?> resolveClass(ObjectStreamClass desc)
                throws IOException, ClassNotFoundException {
            if (desc.getName().endsWith("ObjectInputStreamTest$TestClass1")) {
                return TestClass2.class;
            }
            return super.resolveClass(desc);
        }
    }

    static class TestClass1 implements Serializable { 
        private static final long serialVersionUID = 11111L;
        int i = 0;
    }

    static class TestClass2 implements Serializable {
        private static final long serialVersionUID = 11111L;
        int i = 0;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks InvalidClassException.",
        method = "resolveClass",
        args = {java.io.ObjectStreamClass.class}
    )
    public void test_resolveClass_invalidClassName()
            throws Exception {
        // Regression test for HARMONY-1920
        TestClass1 to1 = new TestClass1();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ByteArrayInputStream bais;
        ObjectInputStream ois;

        to1.i = 555;
        oos.writeObject(to1);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        bais = new ByteArrayInputStream(bytes);
        ois = new TestObjectInputStream(bais);

        try {
            ois.readObject();
            fail("Test 1: InvalidClassException expected.");
        } catch (InvalidClassException ice) {
            // Expected.
        }
    }
}


