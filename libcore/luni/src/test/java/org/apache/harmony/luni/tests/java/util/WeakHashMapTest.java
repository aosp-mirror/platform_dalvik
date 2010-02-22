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

package org.apache.harmony.luni.tests.java.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import tests.support.Support_MapTest2;

public class WeakHashMapTest extends junit.framework.TestCase {
	class MockMap extends AbstractMap {
		public Set entrySet() {
			return null;
		}
		public int size(){
			return 0;
		}
	}

	Object[] keyArray = new Object[100];

	Object[] valueArray = new Object[100];

	WeakHashMap whm;
	
    Object[] KEY_ARRAY;

    Object[] VALUE_ARRAY;

	/**
	 * @tests java.util.WeakHashMap#WeakHashMap()
	 */
	public void test_Constructor() {
		// Test for method java.util.WeakHashMap()
		new Support_MapTest2(new WeakHashMap()).runTest();

		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		for (int i = 0; i < 100; i++)
			assertTrue("Incorrect value retrieved",
					whm.get(keyArray[i]) == valueArray[i]);

	}

	/**
	 * @tests java.util.WeakHashMap#WeakHashMap(int)
	 */
	public void test_ConstructorI() {
		// Test for method java.util.WeakHashMap(int)
		whm = new WeakHashMap(50);
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		for (int i = 0; i < 100; i++)
			assertTrue("Incorrect value retrieved",
					whm.get(keyArray[i]) == valueArray[i]);

		WeakHashMap empty = new WeakHashMap(0);
		assertNull("Empty weakhashmap access", empty.get("nothing"));
		empty.put("something", "here");
		assertTrue("cannot get element", empty.get("something") == "here");
	}

	/**
	 * @tests java.util.WeakHashMap#WeakHashMap(int, float)
	 */
	public void test_ConstructorIF() {
		// Test for method java.util.WeakHashMap(int, float)
		whm = new WeakHashMap(50, 0.5f);
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		for (int i = 0; i < 100; i++)
			assertTrue("Incorrect value retrieved",
					whm.get(keyArray[i]) == valueArray[i]);

		WeakHashMap empty = new WeakHashMap(0, 0.75f);
		assertNull("Empty hashtable access", empty.get("nothing"));
		empty.put("something", "here");
		assertTrue("cannot get element", empty.get("something") == "here");
	}
	
	/**
	 * @tests java.util.WeakHashMap#WeakHashMap(java.util.Map)
	 */
	public void test_ConstructorLjava_util_Map() {
        Map mockMap = new MockMap();
        WeakHashMap map = new WeakHashMap(mockMap);
        assertEquals("Size should be 0", 0, map.size());
	}

	/**
	 * @tests java.util.WeakHashMap#clear()
	 */
	public void test_clear() {
		// Test for method boolean java.util.WeakHashMap.clear()
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		whm.clear();
		assertTrue("Cleared map should be empty", whm.isEmpty());
		for (int i = 0; i < 100; i++)
			assertNull("Cleared map should only return null", whm
					.get(keyArray[i]));

	}

	/**
	 * @tests java.util.WeakHashMap#containsKey(java.lang.Object)
	 */
	public void test_containsKeyLjava_lang_Object() {
		// Test for method boolean java.util.WeakHashMap.containsKey()
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		for (int i = 0; i < 100; i++)
			assertTrue("Should contain referenced key", whm
					.containsKey(keyArray[i]));
		keyArray[25] = null;
		keyArray[50] = null;
	}

	/**
	 * @tests java.util.WeakHashMap#containsValue(java.lang.Object)
	 */
	public void test_containsValueLjava_lang_Object() {
		// Test for method boolean java.util.WeakHashMap.containsValue()
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		for (int i = 0; i < 100; i++)
			assertTrue("Should contain referenced value", whm
					.containsValue(valueArray[i]));
		keyArray[25] = null;
		keyArray[50] = null;
	}

    /**
     * @tests java.util.WeakHashMap#entrySet()
     */
    public void test_entrySet() {
        WeakHashMap<Object, Object> weakMap = new WeakHashMap<Object, Object>();
        KEY_ARRAY = new Object[100];
        VALUE_ARRAY = new Object[100];
        for (int i = 0; i < 100; i++) {
            KEY_ARRAY[i] = new Integer(i);
            VALUE_ARRAY[i] = new Long(i);
            weakMap.put(KEY_ARRAY[i], VALUE_ARRAY[i]);
        }

        List<Object> keys = Arrays.asList(KEY_ARRAY);
        List<Object> values = Arrays.asList(VALUE_ARRAY);

        // Check the entry set has correct size & content
        Set<Map.Entry<Object, Object>> entrySet = weakMap.entrySet();
        assertEquals("Assert 0: Incorrect number of entries returned", 100,
                entrySet.size());
        Iterator<Map.Entry<Object, Object>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            assertTrue("Assert 1: Invalid map entry key returned", keys
                    .contains(entry.getKey()));
            assertTrue("Assert 2: Invalid map entry value returned", values
                    .contains(entry.getValue()));
            assertTrue("Assert 3: Entry not in entry set", entrySet
                    .contains(entry));
        }

        // Dereference list of key/value objects
        keys = values = null;

        // Dereference a single key, then try to
        // force a collection of the weak ref'd obj
        KEY_ARRAY[50] = null;
        int count = 0;
        do {
            System.gc();
            System.gc();
            Runtime.getRuntime().runFinalization();
            count++;
        } while (count <= 5 && entrySet.size() == 100);

        if ((count == 5) && (entrySet.size() == 100)) {
            // We failed (or entrySet broken), so further tests not valid.
            return;
        }

        assertEquals("Assert 4: Incorrect number of entries after gc", 99,
                entrySet.size());
        assertSame("Assert 5: Entries not identical", entrySet.iterator()
                .next(), entrySet.iterator().next());

        // remove alternate entries using the iterator, and ensure the
        // iteration count is consistent
        int size = entrySet.size();
        it = entrySet.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
            size--;
            if (it.hasNext()) {
                it.next();
            }

        }
        assertEquals("Assert 6: entry set count mismatch", size, entrySet
                .size());

        int entries = 0;
        it = entrySet.iterator();
        while (it.hasNext()) {
            it.next();
            entries++;
        }
        assertEquals("Assert 6: count mismatch", size, entries);

        it = entrySet.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertEquals("Assert 7: entry set not empty", 0, entrySet.size());
        assertTrue("Assert 8:  iterator not empty", !entrySet.iterator()
                .hasNext());
    }
    
	/**
	 * @tests java.util.WeakHashMap#entrySet()
	 */
	public void test_entrySet_2() {
		// Test for method java.util.Set java.util.WeakHashMap.entrySet()
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);
		List keys = Arrays.asList(keyArray);
		List values = Arrays.asList(valueArray);
		Set entrySet = whm.entrySet();
		assertTrue("Incorrect number of entries returned--wanted 100, got: "
				+ entrySet.size(), entrySet.size() == 100);
		Iterator it = entrySet.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			assertTrue("Invalid map entry returned--bad key", keys
					.contains(entry.getKey()));
			assertTrue("Invalid map entry returned--bad key", values
					.contains(entry.getValue()));
		}
		keys = null;
		values = null;
		keyArray[50] = null;

		int count = 0;
		do {
			System.gc();
			System.gc();
			Runtime.getRuntime().runFinalization();
			count++;
		} while (count <= 5 && entrySet.size() == 100);

		assertTrue(
				"Incorrect number of entries returned after gc--wanted 99, got: "
						+ entrySet.size(), entrySet.size() == 99);
	}

	/**
	 * @tests java.util.WeakHashMap#get(java.lang.Object)
	 */
	public void test_getLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.WeakHashMap.get(java.lang.Object)
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.WeakHashMap#isEmpty()
	 */
	public void test_isEmpty() {
		// Test for method boolean java.util.WeakHashMap.isEmpty()
		whm = new WeakHashMap();
		assertTrue("New map should be empty", whm.isEmpty());
		Object myObject = new Object();
		whm.put(myObject, myObject);
		assertTrue("Map should not be empty", !whm.isEmpty());
		whm.remove(myObject);
		assertTrue("Map with elements removed should be empty", whm.isEmpty());
	}

	/**
	 * @tests java.util.WeakHashMap#put(java.lang.Object, java.lang.Object)
	 */
	public void test_putLjava_lang_ObjectLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.WeakHashMap.put(java.lang.Object, java.lang.Object)
		WeakHashMap map = new WeakHashMap();
		map.put(null, "value"); // add null key
		System.gc();
		System.runFinalization();
		map.remove("nothing"); // Cause objects in queue to be removed
		assertEquals("null key was removed", 1, map.size());
	}
    
    /**
     * @tests java.util.WeakHashMap#putAll(java.util.Map)
     */
    public void test_putAllLjava_util_Map() {
        Map mockMap=new MockMap();
        WeakHashMap map = new WeakHashMap();
        map.putAll(mockMap);
        assertEquals("Size should be 0", 0, map.size());
    }

	/**
	 * @tests java.util.WeakHashMap#remove(java.lang.Object)
	 */
	public void test_removeLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.WeakHashMap.remove(java.lang.Object)
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);

		assertTrue("Remove returned incorrect value",
				whm.remove(keyArray[25]) == valueArray[25]);
		assertNull("Remove returned incorrect value",
				whm.remove(keyArray[25]));
		assertEquals("Size should be 99 after remove", 99, whm.size());
	}

	/**
	 * @tests java.util.WeakHashMap#size()
	 */
	public void test_size() {
		// Test for method int java.util.WeakHashMap.size()
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.WeakHashMap#keySet()
	 */
	public void test_keySet() {
		// Test for method java.util.Set java.util.WeakHashMap.keySet()
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);

		List keys = Arrays.asList(keyArray);
		List values = Arrays.asList(valueArray);

		Set keySet = whm.keySet();
		assertEquals("Incorrect number of keys returned,", 100, keySet.size());
		Iterator it = keySet.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			assertTrue("Invalid map entry returned--bad key", keys
					.contains(key));
		}
		keys = null;
		values = null;
		keyArray[50] = null;

		int count = 0;
		do {
			System.gc();
			System.gc();
			Runtime.getRuntime().runFinalization();
			count++;
		} while (count <= 5 && keySet.size() == 100);

		assertEquals("Incorrect number of keys returned after gc,", 99, keySet
				.size());
	}

    /**
     * Regression test for HARMONY-3883
     * @tests java.util.WeakHashMap#keySet()
     */
    public void test_keySet_hasNext() {
        WeakHashMap map = new WeakHashMap();
        ConstantHashClass cl = new ConstantHashClass(2);
        map.put(new ConstantHashClass(1), null);
        map.put(cl, null);
        map.put(new ConstantHashClass(3), null);
        Iterator iter = map.keySet().iterator();
        iter.next();
        iter.next();
        System.gc();
        assertFalse("Wrong hasNext() value", iter.hasNext());
    }

    static class ConstantHashClass {
        private int id = 0;

        public ConstantHashClass(int id) {
            this.id = id;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "ConstantHashClass[id=" + id + "]";
        }
    }


	/**
	 * @tests java.util.WeakHashMap#values()
	 */
	public void test_values() {
		// Test for method java.util.Set java.util.WeakHashMap.values()
		whm = new WeakHashMap();
		for (int i = 0; i < 100; i++)
			whm.put(keyArray[i], valueArray[i]);

		List keys = Arrays.asList(keyArray);
		List values = Arrays.asList(valueArray);

		Collection valuesCollection = whm.values();
		assertEquals("Incorrect number of keys returned,", 100,
				valuesCollection.size());
		Iterator it = valuesCollection.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			assertTrue("Invalid map entry returned--bad value", values
					.contains(value));
		}
		keys = null;
		values = null;
		keyArray[50] = null;

		int count = 0;
		do {
			System.gc();
			System.gc();
			Runtime.getRuntime().runFinalization();
			count++;
		} while (count <= 5 && valuesCollection.size() == 100);

		assertEquals("Incorrect number of keys returned after gc,", 99,
				valuesCollection.size());
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		for (int i = 0; i < 100; i++) {
			keyArray[i] = new Object();
			valueArray[i] = new Object();
		}

	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
