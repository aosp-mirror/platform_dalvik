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

package java.io;

/**
 * FilenameFilter is an interface which declares methods for filtering file
 * names in the <code>list</code> method of File.
 * 
 * @see File
 * @see File#list(FilenameFilter)
 */
public interface FilenameFilter {

    /**
     * Returns a boolean if a specific filename matches a filter.
     * 
     * @param dir
     *            the directory in which the <code>filename</code> was found.
     * @param filename
     *            the name of the file in <code>dir</dir> to test.
     * @return boolean <code>true</code> if the filename matches the filter 
     *            and can be included in the list, <code>false</code> 
     *            otherwise.
     */
    public abstract boolean accept(File dir, String filename);
}
