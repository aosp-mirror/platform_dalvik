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

package java.util;


/**
 * ListResourceBundle is the abstract superclass of classes which provide
 * resources by implementing the <code>getContents()</code> method to return
 * the list of resources.
 * 
 * @see ResourceBundle
 * @since 1.1
 */
public abstract class ListResourceBundle extends ResourceBundle {
    Hashtable<String, Object> table;

    /**
     * Constructs a new instance of this class.
     */
    public ListResourceBundle() {
        super();
    }

    /**
     * Returns an Object array which contains the resources of this
     * ListResourceBundle. Each element in the array is an array of two
     * elements, the first is the resource key and the second is the resource.
     * 
     * @return a Object array containing the resources
     */
    protected abstract Object[][] getContents();

    /**
     * Returns the names of the resources contained in this ListResourceBundle.
     * 
     * @return an Enumeration of the resource names
     */
    @Override
    public Enumeration<String> getKeys() {
        if (table == null) {
            initializeTable();
        }
        if (parent == null) {
            return table.keys();
        }
        return new Enumeration<String>() {
            Enumeration<String> local = table.keys();

            Enumeration<String> pEnum = parent.getKeys();

            String nextElement;

            private boolean findNext() {
                if (nextElement != null) {
                    return true;
                }
                while (pEnum.hasMoreElements()) {
                    String next = pEnum.nextElement();
                    if (!table.containsKey(next)) {
                        nextElement = next;
                        return true;
                    }
                }
                return false;
            }

            public boolean hasMoreElements() {
                if (local.hasMoreElements()) {
                    return true;
                }
                return findNext();
            }

            public String nextElement() {
                if (local.hasMoreElements()) {
                    return local.nextElement();
                }
                if (findNext()) {
                    String result = nextElement;
                    nextElement = null;
                    return result;
                }
                // Cause an exception
                return pEnum.nextElement();
            }
        };
    }

    /**
     * Returns the named resource from this ResourceBundle, or null if the
     * resource is not found.
     * 
     * @param key
     *            the name of the resource
     * @return the resource object
     */
    @Override
    public final Object handleGetObject(String key) {
        if (table == null) {
            initializeTable();
        }
        return table.get(key);
    }

    private synchronized void initializeTable() {
        if (table == null) {
            Object[][] contents = getContents();
            table = new Hashtable<String, Object>(contents.length / 3 * 4 + 3);
            for (int i = 0; i < contents.length; i++) {
                table.put((String)contents[i][0], contents[i][1]);
            }
        }
    }
}
