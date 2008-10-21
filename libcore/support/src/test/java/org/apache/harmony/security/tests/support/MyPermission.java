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

package org.apache.harmony.security.tests.support;

import java.security.Permission;

public final class MyPermission extends Permission {

    private static final long serialVersionUID = 4208595188308189251L;

    public MyPermission(String name) {
        super(name);
    }

    public boolean equals(Object obj) {
        if (obj instanceof MyPermission) {
            String name = ((MyPermission) obj).getName();
            if (name == null) {
                return getName() == null;
            }
            return name.equals(getName());
        }
        return false;
    }

    public String getActions() {
        return null;
    }

    public int hashCode() {
        return 0;
    }

    public boolean implies(Permission permission) {
        return false;
    }
}
