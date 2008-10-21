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

package java.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class DigestInputStream extends FilterInputStream {

    /**
     * @com.intel.drl.spec_ref
     */
    protected MessageDigest digest;

    // Indicates whether digest functionality is on or off
    private boolean isOn = true;

    /**
     * @com.intel.drl.spec_ref
     */
    public DigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream);
        this.digest = digest;
    }

    /**
     * Returns the MessageDigest which the receiver uses when computing the
     * hash.
     * 
     * 
     * @return MessageDigest the digest the receiver uses when computing the
     *         hash.
     * 
     */
    public MessageDigest getMessageDigest() {
        return digest;
    }

    /**
     * Sets the MessageDigest which the receiver will use when computing the
     * hash.
     * 
     * 
     * @param digest
     *            MessageDigest the digest to use when computing the hash.
     * 
     * @see MessageDigest
     * @see #on
     */
    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    /**
     * Reads the next byte and returns it as an int. Updates the digest for the
     * byte if this function is enabled.
     * 
     * 
     * @return int the byte which was read or -1 at end of stream.
     * 
     * @exception java.io.IOException
     *                If reading the source stream causes an IOException.
     */
    public int read() throws IOException {
        // read the next byte
        int byteRead = in.read();
        // update digest only if
        // - digest functionality is on
        // - eos has not been reached
        if (isOn && (byteRead != -1)) {
            digest.update((byte)byteRead);
        }
        // return byte read
        return byteRead;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int read(byte[] b, int off, int len) throws IOException {
        // read next up to len bytes
        int bytesRead = in.read(b, off, len);
        // update digest only if
        // - digest functionality is on
        // - eos has not been reached
        if (isOn && (bytesRead != -1)) {
            digest.update(b, off, bytesRead);
        }
        // return number of bytes read
        return bytesRead;
    }

    /**
     * Enables or disables the digest function (default is on).
     * 
     * 
     * @param on
     *            boolean true if the digest should be computed, and false
     *            otherwise.
     * 
     * @see MessageDigest
     */
    public void on(boolean on) {
        isOn = on;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * 
     * @return String a printable representation for the receiver.
     */
    public String toString() {
        return super.toString() + ", " + digest.toString() + //$NON-NLS-1$
            (isOn ? ", is on" : ", is off"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
