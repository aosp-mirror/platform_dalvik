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

package org.apache.harmony.luni.tests.java.lang;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;

class MutableSecurityManager extends SecurityManager {

    static final RuntimePermission SET_SECURITY_MANAGER = new RuntimePermission("setSecurityManager");
    
    private PermissionCollection enabled;
    
    private PermissionCollection denied;

    public MutableSecurityManager() {
        super();
        this.enabled = new Permissions();
    }
    
    public MutableSecurityManager(Permission... permissions) {
        this();
        for (int i = 0; i < permissions.length; i++) {
            this.enabled.add(permissions[i]);
        }
    }

    void addPermission(Permission permission) {
        enabled.add(permission);
    }

    void clearPermissions() {
        enabled = new Permissions();
    }
    
    void denyPermission(Permission p) {
        if (denied == null) {
            denied = new Permissions();
        }
        denied.add(p);
    }

    @Override
    public void checkPermission(Permission permission) 
    {
        // denied should take precedence over allowed
        if (denied != null && denied.implies(permission)){
            throw new SecurityException("Denied " + permission);
        }

        if (enabled.implies(permission)) {
            return;
        }
        
        super.checkPermission(permission);
    }
}
