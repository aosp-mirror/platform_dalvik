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

import java.security.acl.Acl;
import java.util.*;

final class AclEnumerator implements Enumeration {
    
    Acl acl;
    Enumeration u1;
    Enumeration u2;
    Enumeration g1;
    Enumeration g2;

    AclEnumerator(Acl acl1, Hashtable hashtable, Hashtable hashtable1, Hashtable hashtable2, Hashtable hashtable3) {
        acl = acl1;
        u1 = hashtable.elements();
        u2 = hashtable2.elements();
        g1 = hashtable1.elements();
        g2 = hashtable3.elements();
    }

    public boolean hasMoreElements() {
        return u1.hasMoreElements() || u2.hasMoreElements() || g1.hasMoreElements() || g2.hasMoreElements();
    }

    public Object nextElement() {
        Acl acl1 = acl;
        if(u2.hasMoreElements()) return u2.nextElement();
        if(g1.hasMoreElements()) return g1.nextElement();
        if(u1.hasMoreElements()) return u1.nextElement();
        if(g2.hasMoreElements()) return g2.nextElement();
        return acl1;
    }
}