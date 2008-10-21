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

package org.apache.harmony.luni.tests.java.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import junit.framework.TestCase;

public class WeakHashMapTest extends TestCase {

    Object[] KEY_ARRAY;

    Object[] VALUE_ARRAY;

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
}
