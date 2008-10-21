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

import java.security.BasicPermission;

import org.apache.harmony.auth.internal.nls.Messages;

public final class AuthPermission extends BasicPermission {

    private static final long serialVersionUID = 5806031445061587174L;

    private static final String CREATE_LOGIN_CONTEXT = "createLoginContext"; //$NON-NLS-1$

    private static final String CREATE_LOGIN_CONTEXT_ANY = "createLoginContext.*"; //$NON-NLS-1$

    // inits permission name.
    private static String init(String name) {

        if (name == null) {
            throw new NullPointerException(Messages.getString("auth.13")); //$NON-NLS-1$
        }

        if (CREATE_LOGIN_CONTEXT.equals(name)) {
            return CREATE_LOGIN_CONTEXT_ANY;
        }
        return name;
    }

    public AuthPermission(String name) {
        super(init(name));
    }

    public AuthPermission(String name, String actions) {
        super(init(name), actions);
    }
}