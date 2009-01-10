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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import org.apache.harmony.testframework.serialization.SerializationTest;

@TestTargetClass(HashMap.class) 
public class HashMapTest extends TestCase {
    class SubMap<K, V> extends HashMap<K, V> {
        public SubMap(Map<? extends K, ? extends V> m) {
            super(m);
        }

        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @tests java.util.HashMap#HashMap(java.util.Map)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "NullPointerException is not verified.",
        method = "HashMap",
        args = {java.util.Map.class}
    )
    public void test_ConstructorLjava_util_Map() {
        HashMap map = new HashMap();
        map.put("a", "a");
        SubMap map2 = new SubMap(map); 
        assertTrue(map2.containsKey("a"));
        assertTrue(map2.containsValue("a"));
    }

    /**
     * @tests serialization/deserialization.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("key", "value");

        SerializationTest.verifySelf(hm);        

        //  regression for HARMONY-1583
        hm.put(null, "null");
        SerializationTest.verifySelf(hm);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("key", "value");

        SerializationTest.verifyGolden(this, hm);
    }
}
