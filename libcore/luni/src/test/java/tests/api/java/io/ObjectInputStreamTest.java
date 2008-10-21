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

package tests.api.java.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.SerializablePermission;
import java.io.StreamCorruptedException;
import java.security.Permission;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class ObjectInputStreamTest extends junit.framework.TestCase implements
        Serializable {

    ObjectInputStream ois;

    ObjectOutputStream oos;

    ByteArrayOutputStream bao;

    public class SerializableTestHelper implements Serializable {

        public String aField1;

        public String aField2;

        SerializableTestHelper() {
            aField1 = null;
            aField2 = null;
        }

        SerializableTestHelper(String s, String t) {
            aField1 = s;
            aField2 = t;
        }

        private void readObject(ObjectInputStream ois) throws Exception {
            // note aField2 is not read
            ObjectInputStream.GetField fields = ois.readFields();
            aField1 = (String) fields.get("aField1", "Zap");
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            // note aField2 is not written
            ObjectOutputStream.PutField fields = oos.putFields();
            fields.put("aField1", aField1);
            oos.writeFields();
        }

        public String getText1() {
            return aField1;
        }

        public void setText1(String s) {
            aField1 = s;
        }

        public String getText2() {
            return aField2;
        }

        public void setText2(String s) {
            aField2 = s;
        }
    }

    public static class A1 implements Serializable {

        static final long serialVersionUID = 5942584913446079661L;

        B1 b1 = new B1();

        B1 b2 = b1;

        Vector v = new Vector();
    }

    public static class B1 implements Serializable {

        int i = 5;

        Hashtable h = new Hashtable();
    }

    /**
     * @tests java.io.ObjectInputStream#readObject()
     */
    public void test_readObjectMissingClasses() throws Exception {
        SerializationTest.verifySelf(new A1(), new SerializableAssert() {
            public void assertDeserialized(Serializable initial,
                    Serializable deserialized) {
                assertEquals(5, ((A1) deserialized).b1.i);
            }
        });
    }

    /**
     * @tests java.io.ObjectInputStream#ObjectInputStream(java.io.InputStream)
     */
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        // Test for method java.io.ObjectInputStream(java.io.InputStream)
        oos.writeDouble(Double.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.close();
        oos.close();

        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(new byte[90]));
            fail("StreamCorruptedException expected");
        } catch (StreamCorruptedException e) {}
    }

    /**
     * @tests java.io.ObjectInputStream#ObjectInputStream(java.io.InputStream)
     */
    public void test_ConstructorLjava_io_InputStream_subtest0() throws IOException {
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            Permission golden = new SerializablePermission("enableSubclassImplementation");
            
            public void checkPermission(Permission p) {
                if (golden.equals(p)) {
                    throw new SecurityException();
                }
            }
        });

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream obout = new ObjectOutputStream(out);
            obout.write(0);
            obout.close();

            InputStream in = new ByteArrayInputStream(out.toByteArray());

            // should not cause SecurityException
            new ObjectInputStream(in);
            in.reset();

            // should not cause SecurityException
            new ObjectInputStream(in) {};
            in.reset();

            try {
                new ObjectInputStream(in) {
                    public Object readUnshared() throws IOException, ClassNotFoundException {
                        return null;
                    }
                };
                fail("should throw SecurityException 1");
            } catch (SecurityException e) {}

            in.reset();
            try {
                new ObjectInputStream(in) {
                    public GetField readFields() throws IOException,
                            ClassNotFoundException, NotActiveException {
                        return null;
                    }
                };
                fail("should throw SecurityException 2");
            } catch (SecurityException e) {}
        } finally {
            System.setSecurityManager(sm);
        }
    }

    /**
     * @tests java.io.ObjectInputStream#available()
     */
    public void test_available() throws IOException {
        // Test for method int java.io.ObjectInputStream.available()
        oos.writeBytes("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect bytes", 10, ois.available());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#close()
     */
    public void test_close() throws IOException {
        // Test for method void java.io.ObjectInputStream.close()
        oos.writeBytes("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#defaultReadObject()
     */
    public void test_defaultReadObject() throws Exception {
        // Test for method void java.io.ObjectInputStream.defaultReadObject()
        // SM. This method may as well be private, as if called directly it
        // throws an exception.
        String s = "HelloWorld";
        oos.writeObject(s);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        try {
            ois.defaultReadObject();
            fail("NotActiveException expected");
        } catch (NotActiveException e) {
            // Desired behavior
        } finally {
            ois.close();
        }
    }

    /**
     * @tests java.io.ObjectInputStream#read()
     */
    public void test_read() throws IOException {
        // Test for method int java.io.ObjectInputStream.read()
        oos.write('T');
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect byte value", 'T', ois.read());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws IOException {
        // Test for method int java.io.ObjectInputStream.read(byte [], int, int)
        byte[] buf = new byte[10];
        oos.writeBytes("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.read(buf, 0, 10);
        ois.close();
        assertEquals("Read incorrect bytes", "HelloWorld", new String(buf, 0,
                10));
    }

    /**
     * @tests java.io.ObjectInputStream#readBoolean()
     */
    public void test_readBoolean() throws IOException {
        // Test for method boolean java.io.ObjectInputStream.readBoolean()
        oos.writeBoolean(true);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertTrue("Read incorrect boolean value", ois.readBoolean());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readByte()
     */
    public void test_readByte() throws IOException {
        // Test for method byte java.io.ObjectInputStream.readByte()
        oos.writeByte(127);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect byte value", 127, ois.readByte());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readChar()
     */
    public void test_readChar() throws IOException {
        // Test for method char java.io.ObjectInputStream.readChar()
        oos.writeChar('T');
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect char value", 'T', ois.readChar());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readDouble()
     */
    public void test_readDouble() throws IOException {
        // Test for method double java.io.ObjectInputStream.readDouble()
        oos.writeDouble(Double.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertTrue("Read incorrect double value",
                ois.readDouble() == Double.MAX_VALUE);
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readFields()
     */
    public void test_readFields() throws Exception {
        // Test for method java.io.ObjectInputStream$GetField
        // java.io.ObjectInputStream.readFields()

        SerializableTestHelper sth;

        /*
         * "SerializableTestHelper" is an object created for these tests with
         * two fields (Strings) and simple implementations of readObject and
         * writeObject which simply read and write the first field but not the
         * second
         */

        oos.writeObject(new SerializableTestHelper("Gabba", "Jabba"));
        oos.flush();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        sth = (SerializableTestHelper) (ois.readObject());
        assertEquals("readFields / writeFields failed--first field not set",
                "Gabba", sth.getText1());
        assertNull(
                "readFields / writeFields failed--second field should not have been set",
                sth.getText2());
    }

    /**
     * @tests java.io.ObjectInputStream#readFloat()
     */
    public void test_readFloat() throws IOException {
        // Test for method float java.io.ObjectInputStream.readFloat()
        oos.writeFloat(Float.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertTrue("Read incorrect float value",
                ois.readFloat() == Float.MAX_VALUE);
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readFully(byte[])
     */
    public void test_readFully$B() throws IOException {
        // Test for method void java.io.ObjectInputStream.readFully(byte [])
        byte[] buf = new byte[10];
        oos.writeBytes("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.readFully(buf);
        ois.close();
        assertEquals("Read incorrect bytes", "HelloWorld", new String(buf, 0,
                10));
    }

    /**
     * @tests java.io.ObjectInputStream#readFully(byte[], int, int)
     */
    public void test_readFully$BII() throws IOException {
        // Test for method void java.io.ObjectInputStream.readFully(byte [],
        // int, int)
        byte[] buf = new byte[10];
        oos.writeBytes("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.readFully(buf, 0, 10);
        ois.close();
        assertEquals("Read incorrect bytes", "HelloWorld", new String(buf, 0,
                10));
    }

    /**
     * @tests java.io.ObjectInputStream#readInt()
     */
    public void test_readInt() throws IOException {
        // Test for method int java.io.ObjectInputStream.readInt()
        oos.writeInt(Integer.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertTrue("Read incorrect int value",
                ois.readInt() == Integer.MAX_VALUE);
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readLine()
     */
    public void test_readLine() throws IOException {
        // Test for method java.lang.String java.io.ObjectInputStream.readLine()
        oos.writeBytes("HelloWorld\nSecondLine");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.readLine();
        assertEquals("Read incorrect string value", "SecondLine", ois
                .readLine());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readLong()
     */
    public void test_readLong() throws IOException {
        // Test for method long java.io.ObjectInputStream.readLong()
        oos.writeLong(Long.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertTrue("Read incorrect long value",
                ois.readLong() == Long.MAX_VALUE);
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readObject()
     */
    public void test_readObject() throws Exception {
        // Test for method java.lang.Object
        // java.io.ObjectInputStream.readObject()
        String s = "HelloWorld";
        oos.writeObject(s);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect Object value", s, ois.readObject());
        ois.close();

        // Regression for HARMONY-91
        // dynamically create serialization byte array for the next hierarchy:
        // - class A implements Serializable
        // - class C extends A

        byte[] cName = C.class.getName().getBytes();
        byte[] aName = A.class.getName().getBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] begStream = new byte[] { (byte) 0xac, (byte) 0xed, // STREAM_MAGIC
                (byte) 0x00, (byte) 0x05, // STREAM_VERSION
                (byte) 0x73, // TC_OBJECT
                (byte) 0x72, // TC_CLASSDESC
                (byte) 0x00, // only first byte for C class name length
        };

        out.write(begStream, 0, begStream.length);
        out.write(cName.length); // second byte for C class name length
        out.write(cName, 0, cName.length); // C class name

        byte[] midStream = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x21, // serialVersionUID = 33L
                (byte) 0x02, // flags
                (byte) 0x00, (byte) 0x00, // fields : none
                (byte) 0x78, // TC_ENDBLOCKDATA
                (byte) 0x72, // Super class for C: TC_CLASSDESC for A class
                (byte) 0x00, // only first byte for A class name length
        };

        out.write(midStream, 0, midStream.length);
        out.write(aName.length); // second byte for A class name length
        out.write(aName, 0, aName.length); // A class name

        byte[] endStream = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x0b, // serialVersionUID = 11L
                (byte) 0x02, // flags
                (byte) 0x00, (byte) 0x01, // fields

                (byte) 0x4c, // field description: type L (object)
                (byte) 0x00, (byte) 0x04, // length
                // field = 'name'
                (byte) 0x6e, (byte) 0x61, (byte) 0x6d, (byte) 0x65,

                (byte) 0x74, // className1: TC_STRING
                (byte) 0x00, (byte) 0x12, // length
                //
                (byte) 0x4c, (byte) 0x6a, (byte) 0x61, (byte) 0x76,
                (byte) 0x61, (byte) 0x2f, (byte) 0x6c, (byte) 0x61,
                (byte) 0x6e, (byte) 0x67, (byte) 0x2f, (byte) 0x53,
                (byte) 0x74, (byte) 0x72, (byte) 0x69, (byte) 0x6e,
                (byte) 0x67, (byte) 0x3b,

                (byte) 0x78, // TC_ENDBLOCKDATA
                (byte) 0x70, // NULL super class for A class

                // classdata
                (byte) 0x74, // TC_STRING
                (byte) 0x00, (byte) 0x04, // length
                (byte) 0x6e, (byte) 0x61, (byte) 0x6d, (byte) 0x65, // value
        };

        out.write(endStream, 0, endStream.length);
        out.flush();

        // read created serial. form
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                out.toByteArray()));
        Object o = ois.readObject();
        assertEquals(C.class, o.getClass());

		// Regression for HARMONY-846
        assertNull(new ObjectInputStream() {}.readObject());
    }

    /**
     * @tests java.io.ObjectInputStream#readObjectOverride()
     */
    public void test_readObjectOverride() throws Exception {
        // Regression for HARMONY-846
        assertNull(new ObjectInputStream() {

            public Object readObjectOverride() throws IOException,
                    ClassNotFoundException {
                return super.readObjectOverride();
            }

        }.readObjectOverride());
    }

    public static class A implements Serializable {

        private static final long serialVersionUID = 11L;

        public String name = "name";
    }

    public static class B extends A {}

    public static class C extends B {

        private static final long serialVersionUID = 33L;
    }

    /**
     * @tests java.io.ObjectInputStream#readObject()
     */
    public void test_readObjectCorrupt() {
        byte[] bytes = { 00, 00, 00, 0x64, 0x43, 0x48, (byte) 0xFD, 0x71, 00,
                00, 0x0B, (byte) 0xB8, 0x4D, 0x65 };
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        boolean exception = false;
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            in.readObject();
            fail("Unexpected read of corrupted stream");
        } catch (StreamCorruptedException e) {
            exception = true;
        } catch (IOException e) {
            fail("Unexpected: " + e);
        } catch (ClassNotFoundException e) {
            fail("Unexpected: " + e);
        }
        assertTrue("Expected StreamCorruptedException", exception);
    }

    /**
     * @tests java.io.ObjectInputStream#readShort()
     */
    public void test_readShort() throws IOException {
        // Test for method short java.io.ObjectInputStream.readShort()
        oos.writeShort(Short.MAX_VALUE);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertTrue("Read incorrect short value",
                ois.readShort() == Short.MAX_VALUE);
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readUnsignedByte()
     */
    public void test_readUnsignedByte() throws IOException {
        // Test for method int java.io.ObjectInputStream.readUnsignedByte()
        oos.writeByte(-1);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect unsignedByte value", 255, ois
                .readUnsignedByte());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readUnsignedShort()
     */
    public void test_readUnsignedShort() throws IOException {
        // Test for method int java.io.ObjectInputStream.readUnsignedShort()
        oos.writeShort(-1);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect unsignedShort value", 65535, ois
                .readUnsignedShort());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#readUTF()
     */
    public void test_readUTF() throws IOException {
        // Test for method java.lang.String java.io.ObjectInputStream.readUTF()
        oos.writeUTF("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        assertEquals("Read incorrect utf value", "HelloWorld", ois.readUTF());
        ois.close();
    }

    /**
     * @tests java.io.ObjectInputStream#skipBytes(int)
     */
    public void test_skipBytesI() throws IOException {
        // Test for method int java.io.ObjectInputStream.skipBytes(int)
        byte[] buf = new byte[10];
        oos.writeBytes("HelloWorld");
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bao.toByteArray()));
        ois.skipBytes(5);
        ois.read(buf, 0, 5);
        ois.close();
        assertEquals("Skipped incorrect bytes", "World", new String(buf, 0, 5));

        // Regression for HARMONY-844
        try {
            new ObjectInputStream() {}.skipBytes(0);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {}
    }
    
    // Regression Test for JIRA 2192
	public void test_readObject_withPrimitiveClass() throws Exception {
		File file = new File("test.ser");
		file.deleteOnExit();
		Test test = new Test();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file));
		out.writeObject(test);
		out.close();

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		Test another = (Test) in.readObject();
		in.close();
		assertEquals(test, another);
	}
    
    //Regression Test for JIRA-2249
    public static class ObjectOutputStreamWithWriteDesc extends
            ObjectOutputStream {
        public ObjectOutputStreamWithWriteDesc(OutputStream os)
                throws IOException {
            super(os);
        }

        public void writeClassDescriptor(ObjectStreamClass desc)
                throws IOException {
        }
    }

    public static class ObjectIutputStreamWithReadDesc extends
            ObjectInputStream {
        private Class returnClass;

        public ObjectIutputStreamWithReadDesc(InputStream is, Class returnClass)
                throws IOException {
            super(is);
            this.returnClass = returnClass;
        }

        public ObjectStreamClass readClassDescriptor() throws IOException,
                ClassNotFoundException {
            return ObjectStreamClass.lookup(returnClass);

        }
    }
    
    static class TestClassForSerialization implements Serializable {
		private static final long serialVersionUID = 1L;
	}

    public void test_ClassDescriptor() throws IOException,
			ClassNotFoundException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStreamWithWriteDesc oos = new ObjectOutputStreamWithWriteDesc(
				baos);
		oos.writeObject(String.class);
		oos.close();
		Class cls = TestClassForSerialization.class;
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectIutputStreamWithReadDesc ois = new ObjectIutputStreamWithReadDesc(
				bais, cls);
		Object obj = ois.readObject();
		ois.close();
		assertEquals(cls, obj);
	}
        

	// Regression Test for JIRA-2340
    public static class ObjectOutputStreamWithWriteDesc1 extends
			ObjectOutputStream {
		public ObjectOutputStreamWithWriteDesc1(OutputStream os)
				throws IOException {
			super(os);
		}

		public void writeClassDescriptor(ObjectStreamClass desc)
				throws IOException {
			super.writeClassDescriptor(desc);
		}
	}

	public static class ObjectIutputStreamWithReadDesc1 extends
			ObjectInputStream {		

		public ObjectIutputStreamWithReadDesc1(InputStream is)
				throws IOException {
			super(is);			
		}

		public ObjectStreamClass readClassDescriptor() throws IOException,
				ClassNotFoundException {
			return super.readClassDescriptor();
		}
	}
    
    // Regression test for Harmony-1921
    public static class ObjectInputStreamWithResolve extends ObjectInputStream {
        public ObjectInputStreamWithResolve(InputStream in) throws IOException {
            super(in);
        }

        protected Class resolveClass(ObjectStreamClass desc)
                throws IOException, ClassNotFoundException {
            if (desc.getName().equals(
                    "org.apache.harmony.luni.tests.pkg1.TestClass")) {
                return org.apache.harmony.luni.tests.pkg2.TestClass.class;
            }
            return super.resolveClass(desc);
        }
    }

    public void test_resolveClass() throws Exception {
        org.apache.harmony.luni.tests.pkg1.TestClass to1 = new org.apache.harmony.luni.tests.pkg1.TestClass();
        to1.i = 555;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(to1);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStreamWithResolve(bais);
        org.apache.harmony.luni.tests.pkg2.TestClass to2 = (org.apache.harmony.luni.tests.pkg2.TestClass) ois
                .readObject();

        if (to2.i != to1.i) {
            fail("Wrong object read. Expected val: " + to1.i + ", got: "
                    + to2.i);
        }
    }
	
    static class ObjectInputStreamWithResolveObject extends ObjectInputStream {
        
        public static Integer intObj = Integer.valueOf(1000);
        
        public ObjectInputStreamWithResolveObject(InputStream in) throws IOException {
            super(in);
            enableResolveObject(true);
        }
        
        protected Object resolveObject(Object obj) throws IOException {
            if(obj instanceof Integer){
                obj = intObj;
            }
            return super.resolveObject(obj);
        }        
    }
    
    /**
     * @tests java.io.ObjectInputStream#resolveObject(Object)
     */
    public void test_resolveObjectLjava_lang_Object() throws Exception {
        // Write an Integer object into memory
        Integer original = new Integer(10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();
        oos.close();

        // Read the object from memory
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStreamWithResolveObject ois = 
            new ObjectInputStreamWithResolveObject(bais);
        Integer actual = (Integer) ois.readObject();
        ois.close();

        // object should be resolved from 10 to 1000 
        assertEquals(ObjectInputStreamWithResolveObject.intObj, actual);
    }
    
	public void test_readClassDescriptor() throws IOException,
			ClassNotFoundException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStreamWithWriteDesc1 oos = new ObjectOutputStreamWithWriteDesc1(
				baos);
		ObjectStreamClass desc = ObjectStreamClass
		.lookup(TestClassForSerialization.class);
		oos.writeClassDescriptor(desc);
		oos.close();
		
        byte[] bytes = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectIutputStreamWithReadDesc1 ois = new ObjectIutputStreamWithReadDesc1(
				bais);
		Object obj = ois.readClassDescriptor();
		ois.close();
		assertEquals(desc.getClass(), obj.getClass());
        
        //eof
        bais = new ByteArrayInputStream(bytes);
        ExceptionalBufferedInputStream bis = new ExceptionalBufferedInputStream(
                bais);
        ois = new ObjectIutputStreamWithReadDesc1(bis);

        bis.setEOF(true);
        
        try {
            obj = ois.readClassDescriptor();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            ois.close();
        }
        
        //throw exception
        bais = new ByteArrayInputStream(bytes);
        bis = new ExceptionalBufferedInputStream(bais);
        ois = new ObjectIutputStreamWithReadDesc1(bis);

        bis.setException(new IOException());

        try {
            obj = ois.readClassDescriptor();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            ois.close();
        }

        //corrupt
        bais = new ByteArrayInputStream(bytes);
        bis = new ExceptionalBufferedInputStream(bais);
        ois = new ObjectIutputStreamWithReadDesc1(bis);
        
        bis.setCorrupt(true);
        
        try {
            obj = ois.readClassDescriptor();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            ois.close();
        }

	}
    
    static class ExceptionalBufferedInputStream extends BufferedInputStream {
        private boolean eof = false;
        private IOException exception = null; 
        private boolean corrupt = false; 
        
        public ExceptionalBufferedInputStream(InputStream in) {
            super(in);
        }
        
        public int read() throws IOException {
            if (exception != null) {
                throw exception;
            }
            
            if (eof) {
                return -1;
            }
            
            if (corrupt) {
                return 0;
            }
            return super.read();
        }
        
        public void setEOF(boolean eof) {
            this.eof = eof;
        }
        
        public void setException(IOException exception) {
            this.exception = exception;
        }
        
        public void setCorrupt(boolean corrupt) {
            this.corrupt = corrupt;
        }
    }

    // Regression Test for Harmony-2402
    public void test_registerValidation() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray()));

        try {
            ois.registerValidation(null, 256);
            fail("NotActiveException should be thrown");
        } catch (NotActiveException nae) {
            // expected
        }
    }


    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
    }
}


class Test implements Serializable {
	private static final long serialVersionUID = 1L;

	Class classes[] = new Class[] { byte.class, short.class, int.class,
			long.class, boolean.class, char.class, float.class, double.class };

	public boolean equals(Object o) {
		if (!(o instanceof Test)) {
			return false;
		}
		return Arrays.equals(classes, ((Test) o).classes);
	}
}
