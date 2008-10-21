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

/**
* @author Alexey V. Varlamov
* @version $Revision$
*/

package java.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Abstract superclass of classes which are collections of Permission objects.
 * 
 */
public abstract class PermissionCollection implements Serializable {

    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -6727011328946861783L;

    /**
     * @com.intel.drl.spec_ref
     */
    private boolean readOnly; // = false;

    /**
     * Adds the argument to the collection.
     * 
     * 
     * @param permission
     *            java.security.Permission the permission to add to the
     *            collection.
     * @exception IllegalStateException
     *                if the collection is read only.
     */
    public abstract void add(Permission permission);

    /**
     * Returns an enumeration of the permissions in the receiver.
     * 
     * 
     * @return Enumeration the permissions in the receiver.
     */
    public abstract Enumeration<Permission> elements();

    /**
     * Indicates whether the argument permission is implied by the permissions
     * contained in the receiver.
     * 
     * 
     * @return boolean <code>true</code> if the argument permission is implied
     *         by the permissions in the receiver, and <code>false</code> if
     *         it is not.
     * @param permission
     *            java.security.Permission the permission to check
     */
    public abstract boolean implies(Permission permission);

    /**
     * Indicates whether new permissions can be added to the receiver.
     * 
     * 
     * @return boolean <code>true</code> if the receiver is read only
     *         <code>false</code> if new elements can still be added to the
     *         receiver.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Marks the receiver as read only, so that no new permissions can be added
     * to it.
     * 
     */
    public void setReadOnly() {
        readOnly = true;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * 
     * @return a printable representation for the receiver.
     */
    public String toString() {
        List elist = new ArrayList(100);
        Enumeration elenum = elements();
        String superStr = super.toString();
        int totalLength = superStr.length() + 5;
        if (elenum != null) {
            while (elenum.hasMoreElements()) {
                String el = elenum.nextElement().toString();
                totalLength += el.length();
                elist.add(el);
            }
        }
        int esize = elist.size();
        totalLength += esize * 4;
        //FIXME StringBuffer --> StringBuilder
        StringBuffer result = new StringBuffer(totalLength).append(superStr)
            .append(" ("); //$NON-NLS-1$
        for (int i = 0; i < esize; i++) {
            result.append("\n  ").append(elist.get(i).toString()); //$NON-NLS-1$
        }
        return result.append("\n)").toString(); //$NON-NLS-1$
    }
}
