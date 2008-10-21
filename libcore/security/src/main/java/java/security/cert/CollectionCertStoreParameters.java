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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security.cert;

import java.util.Collection;
import java.util.Collections;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class CollectionCertStoreParameters implements CertStoreParameters {
    // Default empty and immutable collection.
    // Used if <code>CollectionCertStoreParameters</code>instance
    // created by the no arg constructor
    private static final Collection defaultCollection = Collections.EMPTY_SET;
    // A <code>Collection</code> of <code>Certificate</code>s
    // and <code>CRL</code>s
    private final Collection collection;

    /**
     * @com.intel.drl.spec_ref
     */
    public CollectionCertStoreParameters() {
        this.collection = defaultCollection;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public CollectionCertStoreParameters(Collection<?> collection) {
        this.collection = collection;
        if (this.collection == null) {
            throw new NullPointerException();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Object clone() {
        return new CollectionCertStoreParameters(collection);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Collection<?> getCollection() {
        return collection;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb =
            new StringBuffer("CollectionCertStoreParameters: [\ncollection: "); //$NON-NLS-1$
        sb.append(getCollection().toString());
        sb.append("\n]"); //$NON-NLS-1$
        return sb.toString();
    }
}
