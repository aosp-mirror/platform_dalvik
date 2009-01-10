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

/**
 * Additional class for verification Owner interface 
 */
public class OwnerImpl implements Owner {
    
    private Group ownerGroup;
    
    public OwnerImpl(Principal principal) {
        ownerGroup = new GroupImpl("AclOwners");
        ownerGroup.addMember(principal);
    }

    public synchronized boolean addOwner(Principal principal, Principal principal1)
                                throws NotOwnerException {
        
        if(!isOwner(principal))
        {
            throw new NotOwnerException();
        } else {
            if (ownerGroup.isMember(principal1)) return false;
            if (!ownerGroup.isMember(principal1)) {
                ownerGroup.addMember(principal1);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteOwner(Principal principal, Principal principal1)
                                throws NotOwnerException, LastOwnerException {
        
        if(!isOwner(principal)) throw new NotOwnerException();
        Enumeration enumeration = ownerGroup.members();
        Object obj = enumeration.nextElement();
        if(enumeration.hasMoreElements()) {
            return ownerGroup.removeMember(principal1);
        } else {
            throw new LastOwnerException();
        }
    }

    public synchronized boolean isOwner(Principal principal)
    {
        return ownerGroup.isMember(principal);
    }
}
