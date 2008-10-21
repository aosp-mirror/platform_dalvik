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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.WriteAbortedException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Automated Test Suite for class java.io.ObjectOutputStream
 * 
 */
public class SerializationStressTest extends junit.framework.TestCase implements
		Serializable {

	// protected static final String MODE_XLOAD = "xload";

	// protected static final String MODE_XDUMP = "xdump";

	static final String FOO = "foo";

	static final String MSG_TEST_FAILED = "Failed to write/read/assertion checking: ";

	protected static final boolean DEBUG = false;

	protected static boolean xload = false;

	protected static boolean xdump = false;

	protected static String xFileName = null;

	protected transient int dumpCount = 0;

	protected transient ObjectInputStream ois;

	protected transient ObjectOutputStream oos;

	protected transient ByteArrayOutputStream bao;

	// -----------------------------------------------------------------------------------

	private static class ObjectInputStreamSubclass extends ObjectInputStream {
		private Vector resolvedClasses = new Vector();

		public ObjectInputStreamSubclass(InputStream in) throws IOException,
				StreamCorruptedException {
			super(in);
		}

		public Class resolveClass(ObjectStreamClass osClass)
				throws IOException, ClassNotFoundException {
			Class result = super.resolveClass(osClass);
			resolvedClasses.addElement(result);
			return result;
		}

		public Class[] resolvedClasses() {
			return (Class[]) resolvedClasses.toArray(new Class[resolvedClasses
					.size()]);
		}
	}

	static final Map TABLE = new Hashtable();

	static final Map MAP = new HashMap();

	static final SortedMap TREE = new TreeMap();

	static final LinkedHashMap LINKEDMAP = new LinkedHashMap();

	static final LinkedHashSet LINKEDSET = new LinkedHashSet();

	static final IdentityHashMap IDENTITYMAP = new IdentityHashMap();

	static final List ALIST = Arrays.asList(new String[] { "a", "list", "of",
			"strings" });

	static final List LIST = new ArrayList(ALIST);

	static final Set SET = new HashSet(Arrays.asList(new String[] { "one",
			"two", "three" }));

	static final Permission PERM = new PropertyPermission("file.encoding",
			"write");

	static final PermissionCollection PERMCOL = PERM.newPermissionCollection();

	static final SortedSet SORTSET = new TreeSet(Arrays.asList(new String[] {
			"one", "two", "three" }));

	static final java.text.DateFormat DATEFORM = java.text.DateFormat
			.getInstance();

	static final java.text.ChoiceFormat CHOICE = new java.text.ChoiceFormat(
			"1#one|2#two|3#three");

	static final java.text.NumberFormat NUMBERFORM = java.text.NumberFormat
			.getInstance();

	static final java.text.MessageFormat MESSAGE = new java.text.MessageFormat(
			"the time: {0,time} and date {0,date}");

	static final LinkedList LINKEDLIST = new LinkedList(Arrays
			.asList(new String[] { "a", "linked", "list", "of", "strings" }));

	static final SimpleTimeZone TIME_ZONE = new SimpleTimeZone(3600000,
			"S-TEST");

	static final Calendar CALENDAR = new GregorianCalendar(TIME_ZONE);

	static {
		TABLE.put("one", "1");
		TABLE.put("two", "2");
		TABLE.put("three", "3");
		MAP.put("one", "1");
		MAP.put("two", "2");
		MAP.put("three", "3");
		LINKEDMAP.put("one", "1");
		LINKEDMAP.put("two", "2");
		LINKEDMAP.put("three", "3");
		IDENTITYMAP.put("one", "1");
		IDENTITYMAP.put("two", "2");
		IDENTITYMAP.put("three", "3");
		LINKEDSET.add("one");
		LINKEDSET.add("two");
		LINKEDSET.add("three");
		TREE.put("one", "1");
		TREE.put("two", "2");
		TREE.put("three", "3");
		PERMCOL.add(PERM);
		// To make sure they all use the same Calendar
		CALENDAR.setTimeZone(new SimpleTimeZone(0, "GMT"));
		CALENDAR.set(1999, Calendar.JUNE, 23, 15, 47, 13);
		CALENDAR.set(Calendar.MILLISECOND, 553);
		DATEFORM.setCalendar(CALENDAR);
		java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols();
		symbols.setZoneStrings(new String[][] { { "a", "b", "c", "d" },
				{ "e", "f", "g", "h" } });
		((java.text.SimpleDateFormat) DATEFORM).setDateFormatSymbols(symbols);
		DATEFORM.setNumberFormat(new java.text.DecimalFormat("#.#;'-'#.#"));
		DATEFORM.setTimeZone(TimeZone.getTimeZone("EST"));
		((java.text.DecimalFormat) NUMBERFORM).applyPattern("#.#;'-'#.#");
		MESSAGE.setFormat(0, DATEFORM);
		MESSAGE.setFormat(1, DATEFORM);
	}

	public SerializationStressTest() {
	}

	public SerializationStressTest(String name) {
		super(name);
	}

	public String getDumpName() {
		return getName() + dumpCount;
	}

	protected void dump(Object o) throws IOException, ClassNotFoundException {
		if (dumpCount > 0)
			setUp();
		// Dump the object
		try {
			oos.writeObject(o);
		} finally {
			oos.close();
		}
	}

	protected Object dumpAndReload(Object o) throws IOException,
			ClassNotFoundException {
		dump(o);
		return reload();
	}

	protected InputStream loadStream() throws IOException {
		// Choose the load stream
		if (xload || xdump) {
			// Load from pre-existing file
			return new FileInputStream(xFileName + "-" + getDumpName() + ".ser");
		} else {
			// Just load from memory, we dumped to memory
			return new ByteArrayInputStream(bao.toByteArray());
		}
	}

	protected Object reload() throws IOException, ClassNotFoundException {
		ois = new ObjectInputStream(loadStream());
		dumpCount++;
		try {
			return ois.readObject();
		} finally {
			ois.close();
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		try {
			if (xdump) {
				oos = new ObjectOutputStream(new FileOutputStream(xFileName
						+ "-" + getDumpName() + ".ser"));
			} else {
				oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
			}
		} catch (Exception e) {
			fail("Exception thrown during setup : " + e.getMessage());
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		if (oos != null) {
			try {
				oos.close();
			} catch (Exception e) {
			}
		}
	}

	public void test_1_Constructor() {
		// Test for method java.io.ObjectOutputStream(java.io.OutputStream)

		try {
			oos.close();
			oos = new ObjectOutputStream(new ByteArrayOutputStream());
			oos.close();
		} catch (Exception e) {
			fail("Failed to create ObjectOutputStream : " + e.getMessage());
		}
	}

	public void test_2_close() {
		// Test for method void java.io.ObjectOutputStream.close()
		try {
			oos.close();
			oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
			oos.close();
			oos.writeChar('T');
			oos.writeObject(FOO);
			// Writing to a closed stream does not cause problems. This is
			// the expected behavior
		} catch (IOException e) {
			fail("Operation on closed stream threw IOException : "
					+ e.getMessage());
		}
	}

	public void test_3_defaultWriteObject() {
		// Test for method void java.io.ObjectOutputStream.defaultWriteObject()

		try {
			oos.defaultWriteObject();
		} catch (NotActiveException e) {
			// Correct
			return;
		} catch (IOException e) {
		}
		fail(
				"Failed to throw NotActiveException when invoked outside readObject");
	}

	public void test_4_flush() {
		// Test for method void java.io.ObjectOutputStream.flush()
		try {
			oos.close();
			oos = new ObjectOutputStream(bao = new ByteArrayOutputStream());
			int size = bao.size();
			oos.writeByte(127);
			assertTrue("Data flushed already", bao.size() == size);
			oos.flush();
			assertTrue("Failed to flush data", bao.size() > size);
			// we don't know how many bytes are actually written for 1 byte,
			// so we test > <before>
			oos.close();
			oos = null;
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_5_reset() {
		// Test for method void java.io.ObjectOutputStream.reset()
		try {
			String o = "HelloWorld";
			oos.writeObject(o);
			oos.writeObject(o);
			oos.reset();
			oos.writeObject(o);
			ois = new ObjectInputStream(loadStream());
			ois.close();
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_6_write() {
		// Test for method void java.io.ObjectOutputStream.write(byte [], int,
		// int)
		try {
			byte[] buf = new byte[255];
			byte[] output = new byte[255];
			for (int i = 0; i < output.length; i++)
				output[i] = (byte) i;
			oos.write(output, 0, output.length);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			ois.readFully(buf);
			ois.close();
			for (int i = 0; i < output.length; i++)
				if (buf[i] != output[i])
					fail("Read incorrect byte: " + i);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_6a_write() {
		// Test for method void java.io.ObjectOutputStream.write(byte [], int,
		// int)
		try {
			byte[] buf = new byte[256];
			byte[] output = new byte[256];
			for (int i = 0; i < output.length; i++)
				output[i] = (byte) (i & 0xff);
			oos.write(output, 0, output.length);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			ois.readFully(buf);
			ois.close();
			for (int i = 0; i < output.length; i++)
				if (buf[i] != output[i])
					fail("Read incorrect byte: " + i);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_7_write() {
		// Test for method void java.io.ObjectOutputStream.write(int)
		try {
			byte[] buf = new byte[10];
			oos.write('T');
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertEquals("Read incorrect byte", 'T', ois.read());
			ois.close();
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_8_write() {
		// Test for method void java.io.ObjectOutputStream.write(byte [])
		try {
			byte[] buf = new byte[10];
			oos.write("HelloWorld".getBytes());
			oos.close();
			ois = new ObjectInputStream(loadStream());
			ois.read(buf, 0, 10);
			ois.close();
			assertEquals("Read incorrect bytes", "HelloWorld", new String(buf, 0, 10)
					);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_9_writeBoolean() {
		// Test for method void java.io.ObjectOutputStream.writeBoolean(boolean)
		try {
			oos.writeBoolean(true);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertTrue("Wrote incorrect byte value", ois.readBoolean());
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_10_writeByte() {
		// Test for method void java.io.ObjectOutputStream.writeByte(int)
		try {
			oos.writeByte(127);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertEquals("Wrote incorrect byte value", 127, ois.readByte());
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_11_writeBytes() {
		// Test for method void
		// java.io.ObjectOutputStream.writeBytes(java.lang.String)
		try {
			byte[] buf = new byte[10];
			oos.writeBytes("HelloWorld");
			oos.close();
			ois = new ObjectInputStream(loadStream());
			ois.readFully(buf);
			ois.close();
			assertEquals("Wrote incorrect bytes value", "HelloWorld", new String(buf, 0, 10)
					);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_12_writeChar() {
		// Test for method void java.io.ObjectOutputStream.writeChar(int)
		try {
			oos.writeChar('T');
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertEquals("Wrote incorrect char value", 'T', ois.readChar());
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_13_writeChars() {
		// Test for method void
		// java.io.ObjectOutputStream.writeChars(java.lang.String)
		try {
			int avail = 0;
			char[] buf = new char[10];
			oos.writeChars("HelloWorld");
			oos.close();
			ois = new ObjectInputStream(loadStream());
			// Number of prim data bytes in stream / 2 to give char index
			avail = ois.available() / 2;
			for (int i = 0; i < avail; ++i)
				buf[i] = ois.readChar();
			ois.close();
			assertEquals("Wrote incorrect chars", "HelloWorld", new String(buf, 0, 10)
					);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_14_writeDouble() {
		// Test for method void java.io.ObjectOutputStream.writeDouble(double)
		try {
			oos.writeDouble(Double.MAX_VALUE);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertTrue("Wrote incorrect double value",
					ois.readDouble() == Double.MAX_VALUE);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_15_writeFloat() {
		// Test for method void java.io.ObjectOutputStream.writeFloat(float)
		try {
			oos.writeFloat(Float.MAX_VALUE);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertTrue("Wrote incorrect double value",
					ois.readFloat() == Float.MAX_VALUE);
			ois.close();
			ois = null;
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_16_writeInt() {
		// Test for method void java.io.ObjectOutputStream.writeInt(int)
		try {
			oos.writeInt(Integer.MAX_VALUE);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertTrue("Wrote incorrect double value",
					ois.readInt() == Integer.MAX_VALUE);
			ois.close();
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_17_writeLong() {
		// Test for method void java.io.ObjectOutputStream.writeLong(long)
		try {
			oos.writeLong(Long.MAX_VALUE);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertTrue("Wrote incorrect double value",
					ois.readLong() == Long.MAX_VALUE);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_19_writeShort() {
		// Test for method void java.io.ObjectOutputStream.writeShort(int)
		try {
			oos.writeShort(127);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertEquals("Wrote incorrect short value", 127, ois.readShort());
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_20_writeUTF() {
		// Test for method void
		// java.io.ObjectOutputStream.writeUTF(java.lang.String)
		try {
			oos.writeUTF("HelloWorld");
			oos.close();
			ois = new ObjectInputStream(loadStream());
			assertEquals("Wrote incorrect UTF value", 
					"HelloWorld", ois.readUTF());
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_25_available() {
		try {
			oos.writeObject(FOO);
			oos.writeObject(FOO);
			oos.flush();
			int available1 = 0;
			int available2 = 0;
			Object obj1 = null;
			Object obj2 = null;
			ObjectInputStream ois = new ObjectInputStream(loadStream());
			available1 = ois.available();
			obj1 = ois.readObject();
			available2 = ois.available();
			obj2 = ois.readObject();

			assertEquals("available returned incorrect value", 0, available1);
			assertEquals("available returned incorrect value", 0, available2);

			assertTrue("available caused incorrect reading", FOO.equals(obj1));
			assertTrue("available returned incorrect value", FOO.equals(obj2));

		} catch (IOException e) {
			fail("IOException serializing object : " + e.getMessage());
		} catch (ClassNotFoundException e) {
			fail("Unable to read Object type : " + e.toString());
		} catch (Error err) {
			System.out.println("Error " + err);
			throw err;
		}

	}

	protected void t_MixPrimitivesAndObjects() throws IOException,
			ClassNotFoundException {
		int i = 7;
		String s1 = "string 1";
		String s2 = "string 2";
		byte[] bytes = { 1, 2, 3 };

		oos.writeInt(i);
		oos.writeObject(s1);
		oos.writeUTF(s2);
		oos.writeObject(bytes);
		oos.close();
		try {
			ois = new ObjectInputStream(loadStream());

			int j = ois.readInt();
			assertTrue("Wrong int :" + j, i == j);

			String l1 = (String) ois.readObject();
			assertTrue("Wrong obj String :" + l1, s1.equals(l1));

			String l2 = (String) ois.readUTF();
			assertTrue("Wrong UTF String :" + l2, s2.equals(l2));

			byte[] bytes2 = (byte[]) ois.readObject();
			assertTrue("Wrong byte[]", Arrays.equals(bytes, bytes2));

		} finally {
			ois.close();
		}
	}

	public void test_resolveClass() {
		try {
			oos.writeObject(new Object[] { Integer.class, new Integer(1) });
			oos.close();

			ois = new ObjectInputStreamSubclass(loadStream());
			ois.readObject();
			ois.close();
		} catch (IOException e1) {
			fail("IOException : " + e1.getMessage());
		} catch (ClassNotFoundException e2) {
			fail("ClassNotFoundException : " + e2.getMessage());
		}

		Class[] resolvedClasses = ((ObjectInputStreamSubclass) ois)
				.resolvedClasses();
		assertEquals("missing resolved", 3, resolvedClasses.length);
		assertTrue("resolved class 1", resolvedClasses[0] == Object[].class);
		assertTrue("resolved class 2", resolvedClasses[1] == Integer.class);
		assertTrue("resolved class 3", resolvedClasses[2] == Number.class);
	}

	public void test_reset() {
		try {
			oos.reset();
			oos.writeObject("R");
			oos.reset();
			oos.writeByte(24);
			oos.close();

			DataInputStream dis = new DataInputStream(loadStream());
			byte[] input = new byte[dis.available()];
			dis.readFully(input);
			byte[] result = new byte[] { (byte) 0xac, (byte) 0xed, (byte) 0,
					(byte) 5, (byte) 0x79, (byte) 0x74, (byte) 0, (byte) 1,
					(byte) 'R', (byte) 0x79, (byte) 0x77, (byte) 1, (byte) 24 };
			assertTrue("incorrect output", Arrays.equals(input, result));

			ois = new ObjectInputStreamSubclass(loadStream());
			assertEquals("Wrong result from readObject()", "R", ois.readObject()
					);
			assertEquals("Wrong result from readByte()", 24, ois.readByte());
			ois.close();
		} catch (IOException e1) {
			fail("IOException : " + e1.getMessage());
		} catch (ClassNotFoundException e2) {
			fail("ClassNotFoundException : " + e2.getMessage());
		}
	}

	public void test_serialVersionUID(Class clazz, long svUID) {
		final String idWrong = "serialVersionUID is wrong for: ";
		long reflectedSvUID = 0L;
		try {
			reflectedSvUID = clazz.getField("serialVersionUID").getLong(null);
		} catch (Exception e) {
			fail("Unable to determine serialVersionUID of " + clazz);
		}
		assertTrue(idWrong + clazz + ": " + reflectedSvUID + " does not equal "
				+ svUID, reflectedSvUID == svUID);
	}

	private static class ResolveObjectTest implements Serializable {
		Object field1, field2;
	}

	private static class ResolveObjectInputStream extends ObjectInputStream {
		ResolveObjectInputStream(InputStream in)
				throws StreamCorruptedException, IOException {
			super(in);
		}

		public void enableResolve() {
			enableResolveObject(true);
		}

		public Object resolveObject(Object obj) {
			if (obj instanceof Vector) // test_1_resolveObject()
				return new Hashtable();
			else if ("abc".equals(obj)) // test_2_resolveObject()
				return "ABC";
			else if (obj instanceof String) // test_3_resolveObject()
				return String.valueOf(((String) obj).length());
			else if (obj instanceof int[]) // test_4_resolveObject()
				return new Object[1];
			else if (obj instanceof Object[] && ((Object[]) obj).length == 2) // test_5_resolveObject()
				return new char[1];
			return obj;
		}
	}

	public void test_1_resolveObject() {
		try {
			ResolveObjectTest obj = new ResolveObjectTest();
			obj.field1 = new Vector();
			obj.field2 = obj.field1;
			oos.writeObject(obj);
			oos.close();
			ois = new ResolveObjectInputStream(loadStream());
			((ResolveObjectInputStream) ois).enableResolve();
			ResolveObjectTest result = null;
			try {
				result = (ResolveObjectTest) ois.readObject();
			} catch (ClassNotFoundException e) {
				fail(e.toString());
			}
			assertTrue("Object not resolved",
					result.field1 instanceof Hashtable);
			assertTrue("Second reference not resolved",
					result.field1 == result.field2);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_2_resolveObject() {
		try {
			ResolveObjectTest obj = new ResolveObjectTest();
			obj.field1 = "abc";
			obj.field2 = obj.field1;
			oos.writeObject(obj);
			oos.close();
			ois = new ResolveObjectInputStream(loadStream());
			((ResolveObjectInputStream) ois).enableResolve();
			ResolveObjectTest result = null;
			try {
				result = (ResolveObjectTest) ois.readObject();
			} catch (ClassNotFoundException e) {
				fail(e.toString());
			}
			assertEquals("String not resolved", "ABC", result.field1);
			assertTrue("Second reference not resolved",
					result.field1 == result.field2);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_3_resolveObject() {
		try {
			ResolveObjectTest obj = new ResolveObjectTest();
			char[] lchars = new char[70000];
			obj.field1 = new String(lchars);
			obj.field2 = obj.field1;
			oos.writeObject(obj);
			oos.close();
			ois = new ResolveObjectInputStream(loadStream());
			((ResolveObjectInputStream) ois).enableResolve();
			ResolveObjectTest result = null;
			try {
				result = (ResolveObjectTest) ois.readObject();
			} catch (ClassNotFoundException e) {
				fail(e.toString());
			}
			assertTrue("Long String not resolved", "70000"
					.equals(result.field1));
			assertTrue("Second reference not resolved",
					result.field1 == result.field2);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_4_resolveObject() {
		try {
			ResolveObjectTest obj = new ResolveObjectTest();
			obj.field1 = new int[5];
			obj.field2 = obj.field1;
			oos.writeObject(obj);
			oos.close();
			ois = new ResolveObjectInputStream(loadStream());
			((ResolveObjectInputStream) ois).enableResolve();
			ResolveObjectTest result = null;
			try {
				result = (ResolveObjectTest) ois.readObject();
			} catch (ClassNotFoundException e) {
				fail(e.toString());
			}
			Class cl = new Object[0].getClass();
			assertTrue("int[] not resolved", result.field1.getClass() == cl);
			assertTrue("Second reference not resolved",
					result.field1 == result.field2);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	public void test_5_resolveObject() {
		try {
			ResolveObjectTest obj = new ResolveObjectTest();
			obj.field1 = new Object[2];
			obj.field2 = obj.field1;
			oos.writeObject(obj);
			oos.close();
			ois = new ResolveObjectInputStream(loadStream());
			((ResolveObjectInputStream) ois).enableResolve();
			ResolveObjectTest result = null;
			try {
				result = (ResolveObjectTest) ois.readObject();
			} catch (ClassNotFoundException e) {
				fail(e.toString());
			}
			Class cl = new char[0].getClass();
			assertTrue("int[] not resolved", result.field1.getClass() == cl);
			assertTrue("Second reference not resolved",
					result.field1 == result.field2);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		}
	}

	static class WriteReplaceTestA implements Serializable {
		public Object writeReplace() throws ObjectStreamException {
			return new ReadResolveTestB();
		}
	}

	static class WriteReplaceTestB extends WriteReplaceTestA {
	}

	static class WriteReplaceTestC extends WriteReplaceTestA {
		public Object writeReplace() throws ObjectStreamException {
			return new ReadResolveTestC();
		}
	}

	static class WriteReplaceTestD implements Serializable {
		private Object writeReplace() throws ObjectStreamException {
			return new ReadResolveTestD();
		}
	}

	static class WriteReplaceTestE extends WriteReplaceTestD {
	}

	static class WriteReplaceTestF implements Serializable {
		int type, readType;

		public WriteReplaceTestF(int type, int readType) {
			this.type = type;
			this.readType = readType;
		}

		public Object writeReplace() throws ObjectStreamException {
			switch (type) {
			case 0:
				throw new InvalidObjectException("invalid");
			case 1:
				throw new RuntimeException("runtime");
			case 2:
				throw new Error("error");
			default:
				return new ReadResolveTestE(readType);
			}
		}
	}

	static class ReadResolveTestA implements Serializable {
		public Object readResolve() throws ObjectStreamException {
			return new ReadResolveTestA();
		}
	}

	static class ReadResolveTestB extends ReadResolveTestA {
	}

	static class ReadResolveTestC implements Serializable {
		private Object readResolve() throws ObjectStreamException {
			return new ReadResolveTestB();
		}
	}

	static class ReadResolveTestD extends ReadResolveTestC {
	}

	static class ReadResolveTestE implements Serializable {
		int type;

		public ReadResolveTestE(int type) {
			this.type = type;
		}

		public Object readResolve() throws ObjectStreamException {
			switch (type) {
			case 0:
				throw new InvalidObjectException("invalid");
			case 1:
				throw new RuntimeException("runtime");
			case 2:
				throw new Error("error");
			case 3:
				return this;
			default:
				return new ReadResolveTestF();
			}
		}
	}

	static class ReadResolveTestF implements Serializable {
	}

	public void test_1_writeReplace() {
		try {
			Vector v = new Vector();
			v.addElement(new WriteReplaceTestA());
			v.addElement(new WriteReplaceTestB());
			v.addElement(new WriteReplaceTestB());
			v.addElement(new WriteReplaceTestC());
			v.addElement(new WriteReplaceTestD());
			v.addElement(new WriteReplaceTestE());
			oos.writeObject(v);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			Vector result = (Vector) ois.readObject();
			assertTrue("invalid 0 : " + result.elementAt(0), result
					.elementAt(0).getClass() == ReadResolveTestA.class);
			assertTrue("invalid 1 : " + result.elementAt(1), result
					.elementAt(1).getClass() == ReadResolveTestA.class);
			assertTrue("invalid 2 : " + result.elementAt(2), result
					.elementAt(2).getClass() == ReadResolveTestA.class);
			assertTrue("invalid 3 : " + result.elementAt(3), result
					.elementAt(3).getClass() == ReadResolveTestB.class);
			assertTrue("invalid 4 : " + result.elementAt(4), result
					.elementAt(4).getClass() == ReadResolveTestD.class);
			assertTrue("invalid 5 : " + result.elementAt(5), result
					.elementAt(5).getClass() == WriteReplaceTestE.class);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException serializing data : " + e.getMessage());
		}
	}

	public void test_2_writeReplace() {
		try {
			boolean exception = false;
			try {
				oos.writeObject(new WriteReplaceTestF(0, -1));
			} catch (ObjectStreamException e) {
				exception = true;
			}
			assertTrue("Should throw ObjectStreamException", exception);
			exception = false;
			try {
				oos.writeObject(new WriteReplaceTestF(1, -1));
			} catch (RuntimeException e) {
				exception = true;
			}
			assertTrue("Should throw RuntimeException", exception);
			exception = false;
			try {
				oos.writeObject(new WriteReplaceTestF(2, -1));
			} catch (Error e) {
				exception = true;
			}
			assertTrue("Should throw Error", exception);

			oos.writeObject(new WriteReplaceTestF(3, 0));
			oos.writeObject(new WriteReplaceTestF(3, 1));
			oos.writeObject(new WriteReplaceTestF(3, 2));
			WriteReplaceTestF test = new WriteReplaceTestF(3, 3);
			oos.writeObject(test);
			oos.writeObject(test);
			WriteReplaceTestF test2 = new WriteReplaceTestF(3, 4);
			oos.writeObject(test2);
			oos.writeObject(test2);
			oos.close();
			ois = new ObjectInputStream(loadStream());
			try {
				ois.readObject();
			} catch (WriteAbortedException e) {
			}

			exception = false;
			try {
				ois.readObject();
			} catch (ObjectStreamException e) {
				exception = true;
			}
			assertTrue("Expected ObjectStreamException", exception);
			exception = false;
			try {
				ois.readObject();
			} catch (RuntimeException e) {
				exception = true;
			}
			assertTrue("Expected RuntimeException", exception);
			exception = false;
			try {
				ois.readObject();
			} catch (Error e) {
				exception = true;
			}
			assertTrue("Expected Error", exception);

			Object readE1 = ois.readObject();
			Object readE2 = ois.readObject();
			assertTrue("Replaced objects should be identical", readE1 == readE2);
			Object readF1 = ois.readObject();
			Object readF2 = ois.readObject();
			assertTrue("Replaced resolved objects should be identical: "
					+ readF1 + " " + readF2, readF1 == readF2);
		} catch (IOException e) {
			fail("IOException serializing data : " + e.getMessage());
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException serializing data : " + e.getMessage());
		}
	}
}
