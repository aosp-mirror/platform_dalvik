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
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class MyPermissionCollection extends PermissionCollection {

    private static final long serialVersionUID = -8462474212761656528L;

     public MyPermissionCollection(boolean readOnly) {
         if (readOnly) {
             setReadOnly();
         }
     }

     public void add(Permission permission) {}

     public Enumeration<Permission> elements() {

         return new Enumeration<Permission>() {
             public boolean hasMoreElements() {
                 return false;
             }

             public Permission nextElement() {
                 throw new NoSuchElementException();
             }
         };
     }

     public boolean implies(Permission permission) {
         return false;
     }
 }
