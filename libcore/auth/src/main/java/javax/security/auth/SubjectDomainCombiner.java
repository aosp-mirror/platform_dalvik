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

package javax.security.auth;

import java.security.DomainCombiner;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Set;

public class SubjectDomainCombiner implements DomainCombiner {

    // subject to be associated
    private Subject subject;

    // permission required to get a subject object
    private static final AuthPermission _GET = new AuthPermission(
            "getSubjectFromDomainCombiner"); //$NON-NLS-1$

    public SubjectDomainCombiner(Subject subject) {
        super();
        if (subject == null) {
            throw new NullPointerException();
        }
        this.subject = subject;
    }

    public Subject getSubject() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(_GET);
        }

        return subject;
    }

    public ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
            ProtectionDomain[] assignedDomains) {
        // get array length for combining protection domains
        int len = 0;
        if (currentDomains != null) {
            len += currentDomains.length;
        }
        if (assignedDomains != null) {
            len += assignedDomains.length;
        }
        if (len == 0) {
            return null;
        }

        ProtectionDomain[] pd = new ProtectionDomain[len];

        // for each current domain substitute set of principal with subject's
        int cur = 0;
        if (currentDomains != null) {

            Set<Principal> s = subject.getPrincipals();
            Principal[] p = s.toArray(new Principal[s.size()]);

            for (cur = 0; cur < currentDomains.length; cur++) {
                ProtectionDomain newPD;
                newPD = new ProtectionDomain(currentDomains[cur].getCodeSource(),
                        currentDomains[cur].getPermissions(), currentDomains[cur]
                                .getClassLoader(), p);
                pd[cur] = newPD;
            }
        }

        // copy assigned domains
        if (assignedDomains != null) {
            System.arraycopy(assignedDomains, 0, pd, cur, assignedDomains.length);
        }

        return pd;
    }
}
