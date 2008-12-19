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

package java.security.acl;

import java.security.Principal;
import java.util.Enumeration;

/**
 * The <i>Access Control List Entry</i> interface definition.
 * <p>
 * An {@code AclEntry} is a list of the {@link Permission}s that are 
 *  granted (<i>positive</i>) or denied (<i>negative</i>) to a {@link Principal}.
 * </p>
 * 
 * @since Android 1.0
 */
public interface AclEntry extends Cloneable {

    /**
     * Set the principal for this ACL entry.
     * <p>
     * The principal for an ACL entry can only be set once.
     * </p>
     * 
     * @param user
     *            the principal for this ACL entry.
     * @return {@code true} on success, {@code false} if there is a principal already set for
     *         this entry.
     * @since Android 1.0
     */
    boolean setPrincipal(Principal user);
    
    /**
     * Returns the principal of this ACL entry.
     * 
     * @return the principal of this ACL entry, or null if none is set.
     * @since Android 1.0
     */
    Principal getPrincipal();
    
    /**
     * Sets this ACL entry to be <i>negative</i>.
     * <p>
     * The permissions in this ACL entry will be denied to the principal
     * associated with this entry.
     * </p>
     * <p>
     * Note: An ACL entry is <i>positive</i> by default and can only become
     * <i>negative</i> by calling this method.
     * </p>
     * 
     * @since Android 1.0
     */
    void setNegativePermissions();
    
    /**
     * Returns whether this ACL entry is <i>negative</i>.
     * 
     * @return {@code true} if this ACL entry is negative, {@code false} if it's positive.
     * @since Android 1.0
     */
    boolean isNegative();
    
    /**
     * Adds the specified permission to this ACL entry.
     * 
     * @param permission
     *            the permission to be added.
     * @return {@code true} if the specified permission is added, {@code false} if the
     *         permission was already in this entry.
     * @since Android 1.0
     */
    boolean addPermission(Permission permission);
    
    /**
     * Removes the specified permission from this ACL entry.
     * 
     * @param permission
     *            the permission to be removed.
     * @return {@code true} if the permission is removed, {@code false} if the permission was
     *         not in this entry.
     * @since Android 1.0
     */
    boolean removePermission(Permission permission);
    
    /**
     * Checks whether the specified permission is in this ACL entry.
     * 
     * @param permission
     *            the permission to check.
     * @return {@code true} if the permission is in this entry, otherwise {@code false}.
     * @since Android 1.0
     */
    boolean checkPermission(Permission permission);
    
    /**
     * Returns the list of permissions of this ACL entry.
     * 
     * @return the list of permissions of this ACL entry,
     * @since Android 1.0
     */
    Enumeration<Permission> permissions();
    
    /**
     * Returns the string representation of this ACL entry.
     * 
     * @return the string representation of this ACL entry.
     * @since Android 1.0
     */
    String toString();
    
    /**
     * Clones this ACL entry instance.
     * 
     * @return a copy of this entry.
     * @since Android 1.0
     */
    Object clone();
    
}
