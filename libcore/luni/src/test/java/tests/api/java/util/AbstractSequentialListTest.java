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
package tests.api.java.util;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import junit.framework.TestCase;

public class AbstractSequentialListTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    class ASLT<E> extends AbstractSequentialList<E> {

        LinkedList<E> l = new LinkedList<E>();

        @Override
        public ListIterator<E> listIterator(int index) {
            return l.listIterator(index);
        }

        @Override
        public int size() {
            return l.size();
        }
    }
    
    /**
     * @tests {@link java.util.AbstractSequentialList#addAll(int, java.util.Collection)}
     */
    public void test_addAll_ILCollection() {
        AbstractSequentialList<String> al = new ASLT<String>();
        String[] someList = { "Aardvark"  ,  //$NON-NLS-1$
                              "Bear"      ,  //$NON-NLS-1$
                              "Chimpanzee",  //$NON-NLS-1$
                              "Duck"      }; //$NON-NLS-1$
        Collection<String> c = Arrays.asList(someList);
        al.addAll(c);
        assertTrue("Should return true", al.addAll(2, c)); //$NON-NLS-1$
    }

}
