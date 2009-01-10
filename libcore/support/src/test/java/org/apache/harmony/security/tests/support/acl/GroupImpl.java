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
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Additional class for verification Group interface 
 */
public class GroupImpl implements Group {
    
    private Vector groupMembers;
    private String group;

    public GroupImpl(String s) {
        groupMembers = new Vector(50, 100);
        group = s;
    }

    public boolean addMember(Principal principal) {
        if(groupMembers.contains(principal))
            return false;
        if(group.equals(principal.toString())) {
            throw new IllegalArgumentException();
        } else {
            groupMembers.addElement(principal);
            return true;
        }
    }

    public boolean removeMember(Principal principal) {
        return groupMembers.removeElement(principal);
    }

    public Enumeration members() {
        return groupMembers.elements();
    }

    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!(obj instanceof Group)) {
            return false;
        } else {
            Group group1 = (Group)obj;
            return group.equals(group1.toString());
        }
    }

    public boolean equals(Group group1) {
        return equals(group1);
    }

    public String toString() {
        return group;
    }

    public int hashCode() {
        return group.hashCode();
    }

    public boolean isMember(Principal principal) {
        if(groupMembers.contains(principal)) {
            return true;
        } else {
            Vector vector = new Vector(10);
            return isMemberRecurse(principal, vector);
        }
    }

    public String getName() {
        return group;
    }

    boolean isMemberRecurse(Principal principal, Vector vector) {
        for(Enumeration enumeration = members(); enumeration.hasMoreElements();) {
            boolean flag = false;
            Principal principal1 = (Principal)enumeration.nextElement();
            if(principal1.equals(principal))
                return true;
            if(principal1 instanceof GroupImpl) {
                GroupImpl groupimpl = (GroupImpl)principal1;
                vector.addElement(this);
                if(!vector.contains(groupimpl))
                    flag = groupimpl.isMemberRecurse(principal, vector);
            } else if(principal1 instanceof Group) {
                Group group1 = (Group)principal1;
                if(!vector.contains(group1)) flag = group1.isMember(principal);
            } 
            if(flag) return flag;
        }
        return false;
    }
}