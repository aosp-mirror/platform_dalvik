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

package org.apache.harmony.luni.util;


import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.harmony.kernel.vm.VM;

public class DeleteOnExit {
    private static Vector<String> deleteList = new Vector<String>();

    static {
        VM.deleteOnExit();
    }

    public static void addFile(String toDelete) {
        deleteList.addElement(toDelete);
    }

    public static void deleteOnExit() {
        java.util.Collections.sort(deleteList,
                new java.util.Comparator<String>() {
                    public int compare(String s1, String s2) {
                        return s2.length() - s1.length();
                    }
                });
        for (int i = 0; i < deleteList.size(); i++) {
            String name = deleteList.elementAt(i);
            new File(name).delete();
        }
    }
}
