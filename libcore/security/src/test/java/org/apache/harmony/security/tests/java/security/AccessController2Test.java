/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.security.tests.java.security;

import java.security.AccessController;
import java.security.AllPermission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class AccessController2Test extends junit.framework.TestCase {

    /**
     * @tests java.security.AccessController#doPrivileged(java.security.PrivilegedAction,
     *        java.security.AccessControlContext))
     */
    public void testDoPrivilegedLjava_security_PrivilegedActionLjava_security_AccessControlContext() {
        Boolean pass;

        pass = (Boolean) AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        try {
                            AccessController
                                    .checkPermission(new AllPermission());
                            return new Boolean(false);
                        } catch (SecurityException ex) {
                            return new Boolean(true);
                        }
                    }
                }, null);
        assertTrue("Got AllPermission by passing in a null PD", pass
                .booleanValue());

        pass = (Boolean) AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        try {
                            AccessController
                                    .checkPermission(new AllPermission());
                            return new Boolean(false);
                        } catch (SecurityException ex) {
                            return new Boolean(true);
                        }
                    }
                }, AccessController.getContext());
        assertTrue("Got AllPermission by passing in not null PD", pass
                .booleanValue());
    }

    /**
     * @tests java.security.AccessController#doPrivileged(java.security.PrivilegedAction))
     */
    public void testDoPrivilegedLjava_security_PrivilegedAction() {
        Boolean pass;

        pass = (Boolean) AccessController
                .doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        try {
                            AccessController
                                    .checkPermission(new AllPermission());
                            return new Boolean(false);
                        } catch (SecurityException ex) {
                            return new Boolean(true);
                        }
                    }
                });
        assertTrue("Got AllPermission by passing in a null PD", pass
                .booleanValue());

    }

    /**
     * @tests java.security.AccessController#doPrivileged(java.security.PrivilegedExceptionAction,
     *        java.security.AccessControlContext))
     */
    public void testDoPrivilegedLjava_security_PrivilegedExceptionActionLjava_security_AccessControlContext() {
        Boolean pass;
        try {
            pass = (Boolean) AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Boolean>() {
                        public Boolean run() {
                            try {
                                AccessController
                                        .checkPermission(new AllPermission());
                                return new Boolean(false);
                            } catch (SecurityException ex) {
                                return new Boolean(true);
                            }
                        }
                    }, null);
            assertTrue("Got AllPermission by passing in a null PD", pass
                    .booleanValue());
        } catch (PrivilegedActionException e) {
            fail("Unexpected exception " + e.getMessage());
        }

        pass = (Boolean) AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        try {
                            AccessController
                                    .checkPermission(new AllPermission());
                            return new Boolean(false);
                        } catch (SecurityException ex) {
                            return new Boolean(true);
                        }
                    }
                }, AccessController.getContext());
        assertTrue("Got AllPermission by passing in not null PD", pass
                .booleanValue());
    }

    /**
     * @tests java.security.AccessController#doPrivileged(java.security.PrivilegedExceptionAction))
     */
    public void testDoPrivilegedLjava_security_PrivilegedExceptionAction() {
        Boolean pass;
        try {
            pass = (Boolean) AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                        public Boolean run() {
                            try {
                                AccessController
                                        .checkPermission(new AllPermission());
                                return new Boolean(false);
                            } catch (SecurityException ex) {
                                return new Boolean(true);
                            }
                        }
                    });
            assertTrue("Got AllPermission by passing in a null PD", pass
                    .booleanValue());
        } catch (PrivilegedActionException e) {
            fail("Unexpected exception " + e.getMessage());
        }

    }
}