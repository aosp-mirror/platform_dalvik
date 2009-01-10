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
import java.util.*;

/**
 * Additional class for verification Acl interface 
 */
public class AclImpl extends OwnerImpl implements Acl {
    
    private Hashtable allowedUsersTable;
    private Hashtable allowedGroupsTable;
    private Hashtable deniedUsersTable;
    private Hashtable deniedGroupsTable;
    private String aclName;
    private Vector zeroSet;

    public AclImpl(Principal principal, String s) {
        super(principal);
        allowedUsersTable = new Hashtable(23);
        allowedGroupsTable = new Hashtable(23);
        deniedUsersTable = new Hashtable(23);
        deniedGroupsTable = new Hashtable(23);
        aclName = null;
        zeroSet = new Vector(1, 1);
        try {
            setName(principal, s);
        } catch(Exception exception) { }
    }

    public void setName(Principal principal, String s)
                throws NotOwnerException {
        if(!isOwner(principal)) {
            throw new NotOwnerException();
        } else {
            aclName = s;
            return;
        }
    }

    public String getName() {
        return aclName;
    }

    public synchronized boolean addEntry(Principal principal, AclEntry aclentry)
                                throws NotOwnerException {
        if(!isOwner(principal)) throw new NotOwnerException();
        Hashtable hashtable = findTable(aclentry);
        Principal principal1 = aclentry.getPrincipal();
        if(hashtable.get(principal1) != null) {
            return false;
        } else {
            hashtable.put(principal1, aclentry);
            return true;
        }
    }

    public synchronized boolean removeEntry(Principal principal, AclEntry aclentry)
                                throws NotOwnerException {
        if(!isOwner(principal)) {
            throw new NotOwnerException();
        } else {
            Hashtable hashtable = findTable(aclentry);
            Principal principal1 = aclentry.getPrincipal();
            Object obj = hashtable.remove(principal1);
            return obj != null;
        }
    }

    public synchronized Enumeration getPermissions(Principal principal) {
        Enumeration enumeration2 = subtract(getGroupPositive(principal), getGroupNegative(principal));
        Enumeration enumeration3 = subtract(getGroupNegative(principal), getGroupPositive(principal));
        Enumeration enumeration = subtract(getIndividualPositive(principal), getIndividualNegative(principal));
        Enumeration enumeration1 = subtract(getIndividualNegative(principal), getIndividualPositive(principal));
        Enumeration enumeration4 = subtract(enumeration2, enumeration1);
        Enumeration enumeration5 = union(enumeration, enumeration4);
        enumeration = subtract(getIndividualPositive(principal), getIndividualNegative(principal));
        enumeration1 = subtract(getIndividualNegative(principal), getIndividualPositive(principal));
        enumeration4 = subtract(enumeration3, enumeration);
        Enumeration enumeration6 = union(enumeration1, enumeration4);
        return subtract(enumeration5, enumeration6);
    }

    public boolean checkPermission(Principal principal, Permission permission) {
        for(Enumeration enumeration = getPermissions(principal); enumeration.hasMoreElements();) {
            Permission permission1 = (Permission)enumeration.nextElement();
            if(permission1.equals(permission))
                return true;
        }
        return false;
    }

    public synchronized Enumeration entries() {
        return new AclEnumerator(this, allowedUsersTable, allowedGroupsTable, deniedUsersTable, deniedGroupsTable);
    }

    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();
        for(Enumeration enumeration = entries(); enumeration.hasMoreElements(); stringbuffer.append("\n")) {
            AclEntry aclentry = (AclEntry)enumeration.nextElement();
            stringbuffer.append(aclentry.toString().trim());
        }
        return stringbuffer.toString();
    }

    private Hashtable findTable(AclEntry aclentry) {
        Hashtable hashtable = null;
        Principal principal = aclentry.getPrincipal();
        if(principal instanceof Group) {
            if(aclentry.isNegative())
                hashtable = deniedGroupsTable;
            else
                hashtable = allowedGroupsTable;
        } else
        if(aclentry.isNegative())
            hashtable = deniedUsersTable;
        else
            hashtable = allowedUsersTable;
        return hashtable;
    }

    private static Enumeration union(Enumeration enumeration, Enumeration enumeration1) {
        Vector vector = new Vector(20, 20);
        for(; enumeration.hasMoreElements(); vector.addElement(enumeration.nextElement()));
        do {
            if(!enumeration1.hasMoreElements())
                break;
            Object obj = enumeration1.nextElement();
            if(!vector.contains(obj))
                vector.addElement(obj);
        } while(true);
        return vector.elements();
    }

    private Enumeration subtract(Enumeration enumeration, Enumeration enumeration1) {
        Vector vector = new Vector(20, 20);
        for(; enumeration.hasMoreElements(); vector.addElement(enumeration.nextElement()));
        do {
            if(!enumeration1.hasMoreElements())
                break;
            Object obj = enumeration1.nextElement();
            if(vector.contains(obj))
                vector.removeElement(obj);
        } while(true);
        return vector.elements();
    }

    private Enumeration getGroupPositive(Principal principal) {
        Enumeration enumeration = zeroSet.elements();
        Enumeration enumeration1 = allowedGroupsTable.keys();
        do {
            if(!enumeration1.hasMoreElements())
                break;
            Group group = (Group)enumeration1.nextElement();
            if(group.isMember(principal)) {
                AclEntry aclentry = (AclEntry)allowedGroupsTable.get(group);
                enumeration = union(aclentry.permissions(), enumeration);
            }
        } while(true);
        return enumeration;
    }

    private Enumeration getGroupNegative(Principal principal) {
        Enumeration enumeration = zeroSet.elements();
        Enumeration enumeration1 = deniedGroupsTable.keys();
        do {
            if(!enumeration1.hasMoreElements())
                break;
            Group group = (Group)enumeration1.nextElement();
            if(group.isMember(principal)) {
                AclEntry aclentry = (AclEntry)deniedGroupsTable.get(group);
                enumeration = union(aclentry.permissions(), enumeration);
            }
        } while(true);
        return enumeration;
    }

    private Enumeration getIndividualPositive(Principal principal) {
        Enumeration enumeration = zeroSet.elements();
        AclEntry aclentry = (AclEntry)allowedUsersTable.get(principal);
        if(aclentry != null)
            enumeration = aclentry.permissions();
        return enumeration;
    }

    private Enumeration getIndividualNegative(Principal principal) {
        Enumeration enumeration = zeroSet.elements();
        AclEntry aclentry = (AclEntry)deniedUsersTable.get(principal);
        if(aclentry != null)
            enumeration = aclentry.permissions();
        return enumeration;
    }
}