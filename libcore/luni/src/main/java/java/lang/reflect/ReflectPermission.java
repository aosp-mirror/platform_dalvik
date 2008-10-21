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

package java.lang.reflect;

import java.security.BasicPermission;

/**
 * ReflectPermission objects represent permission to access dangerous operations
 * in the reflection layer.
 */
public final class ReflectPermission extends BasicPermission {

    private static final long serialVersionUID = 7412737110241507485L;

    /**
     * Creates an instance of this class with given name.
     * 
     * @param permissionName
     *            String the name of the new permission.
     */
    public ReflectPermission(String permissionName) {
        super(permissionName);
    }

    /**
     * Creates an instance of this class with the given name and action list.
     * The action list is ignored.
     * 
     * @param name
     *            String the name of the new permission.
     * @param actions
     *            String ignored.
     */
    public ReflectPermission(String name, String actions) {
        super(name, actions);
    }
}
