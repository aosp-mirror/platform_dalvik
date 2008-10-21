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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package javax.net.ssl;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class SSLEngineResult {
    
    // Store Status object
    private final SSLEngineResult.Status status;

    // Store HandshakeStatus object
    private final SSLEngineResult.HandshakeStatus handshakeStatus;

    // Store bytesConsumed
    private final int bytesConsumed;

    // Store bytesProduced
    private final int bytesProduced;

    /**
     * @com.intel.drl.spec_ref
     */
    public SSLEngineResult(SSLEngineResult.Status status,
            SSLEngineResult.HandshakeStatus handshakeStatus, int bytesConsumed,
            int bytesProduced) {
        if (status == null) {
            throw new IllegalArgumentException("status is null");
        }
        if (handshakeStatus == null) {
            throw new IllegalArgumentException("handshakeStatus is null");
        }
        if (bytesConsumed < 0) {
            throw new IllegalArgumentException("bytesConsumed is negative");
        }
        if (bytesProduced < 0) {
            throw new IllegalArgumentException("bytesProduced is negative");
        }
        this.status = status;
        this.handshakeStatus = handshakeStatus;
        this.bytesConsumed = bytesConsumed;
        this.bytesProduced = bytesProduced;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final HandshakeStatus getHandshakeStatus() {
        return handshakeStatus;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final int bytesConsumed() {
        return bytesConsumed;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final int bytesProduced() {
        return bytesProduced;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("SSLEngineReport: Status = ");
        sb.append(status.toString());
        sb.append("  HandshakeStatus = ");
        sb.append(handshakeStatus.toString());
        sb.append("\n                 bytesConsumed = ");
        sb.append(Integer.toString(bytesConsumed));
        sb.append(" bytesProduced = ");
        sb.append(Integer.toString(bytesProduced));
        return sb.toString();
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public enum HandshakeStatus {
        NOT_HANDSHAKING,
        FINISHED,
        NEED_TASK,
        NEED_WRAP,
        NEED_UNWRAP
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static enum Status {
        BUFFER_OVERFLOW,
        BUFFER_UNDERFLOW,
        CLOSED,
        OK
    }
}