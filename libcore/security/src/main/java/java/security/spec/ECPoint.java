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

package java.security.spec;

import java.math.BigInteger;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class ECPoint {
    /**
     * @com.intel.drl.spec_ref
     */
    public static final ECPoint POINT_INFINITY = new ECPoint();
    // affine X coordinate of this point
    private final BigInteger affineX;
    // affine Y coordinate of this point
    private final BigInteger affineY;

    // Private ctor for POINT_INFINITY
    private ECPoint() {
        affineX = null;
        affineY = null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ECPoint(BigInteger affineX, BigInteger affineY) {
        this.affineX = affineX;
        if (this.affineX == null) {
            throw new NullPointerException(Messages.getString("security.83", "X")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.affineY = affineY;
        if (this.affineY == null) {
            throw new NullPointerException(Messages.getString("security.83", "Y")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getAffineX() {
        return affineX;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getAffineY() {
        return affineY;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ECPoint) {
            if (this.affineX != null) {
                ECPoint otherPoint = (ECPoint)other;
                // no need to check for null in this case
                return this.affineX.equals(otherPoint.affineX) &&
                       this.affineY.equals(otherPoint.affineY);
            } else {
                return other == POINT_INFINITY;
            }
        }
        return false;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int hashCode() {
        if (this.affineX != null) {
            return affineX.hashCode() * 31 + affineY.hashCode();
        }
        return 11;
    }
}
