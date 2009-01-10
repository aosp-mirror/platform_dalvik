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

package org.apache.harmony.security.tests.support.acl;

import java.security.Principal;
import java.security.acl.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Additional class for verification AclEntry interface 
 */
public class AclEntryImpl implements AclEntry {
    
    private Principal user;
    private Vector permissionSet;
    private boolean negative;

    public AclEntryImpl(Principal principal) {
        user = null;
        permissionSet = new Vector(10, 10);
        negative = false;
        user = principal;
    }

    public AclEntryImpl() {
        user = null;
        permissionSet = new Vector(10, 10);
        negative = false;
    }

    public boolean setPrincipal(Principal principal) {
        if(user != null) {
            return false;
        } else {
            user = principal;
            return true;
        }
    }

    public void setNegativePermissions() {
        negative = true;
    }

    public boolean isNegative() {
        return negative;
    }

    public boolean addPermission(Permission permission) {
        if(permissionSet.contains(permission)) {
            return false;
        } else {
            permissionSet.addElement(permission);
            return true;
        }
    }

    public boolean removePermission(Permission permission) {
        return permissionSet.removeElement(permission);
    }

    public boolean checkPermission(Permission permission) {
        return permissionSet.contains(permission);
    }

    public Enumeration permissions() {
        return permissionSet.elements();
    }

    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();
        if(negative)
            stringbuffer.append("-");
        else
            stringbuffer.append("+");
        if(user instanceof Group)
            stringbuffer.append("Group.");
        else
            stringbuffer.append("User.");
        stringbuffer.append((new StringBuilder()).append(user).append("=").toString());
        Enumeration enumeration = permissions();
        do {
            if(!enumeration.hasMoreElements())
                break;
            Permission permission = (Permission)enumeration.nextElement();
            stringbuffer.append(permission);
            if(enumeration.hasMoreElements())
                stringbuffer.append(",");
        } while(true);
        return new String(stringbuffer);
    }

    public synchronized Object clone() {
        AclEntryImpl aclentryimpl = new AclEntryImpl(user);
        aclentryimpl.permissionSet = (Vector)permissionSet.clone();
        aclentryimpl.negative = negative;
        return aclentryimpl;
    }

    public Principal getPrincipal() {
        return user;
    }
}