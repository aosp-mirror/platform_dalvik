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

package javax.security.auth.x500;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;

import org.apache.harmony.auth.internal.nls.Messages;
import org.apache.harmony.security.x501.Name;

public final class X500Principal implements Serializable, Principal {

    private static final long serialVersionUID = -500463348111345721L;

    public static final String CANONICAL = "CANONICAL"; //$NON-NLS-1$

    public static final String RFC1779 = "RFC1779"; //$NON-NLS-1$

    public static final String RFC2253 = "RFC2253"; //$NON-NLS-1$

    //Distinguished Name
    private transient Name dn;

    public X500Principal(byte[] name) {
        super();
        if (name == null) {
            throw new IllegalArgumentException(Messages.getString("auth.00")); //$NON-NLS-1$
        }
        try {
            // FIXME dn = new Name(name);
            dn = (Name) Name.ASN1.decode(name);
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(Messages
                    .getString("auth.2B")); //$NON-NLS-1$
            iae.initCause(e);
            throw iae;
        }
    }

    public X500Principal(InputStream in) {
        super();
        if (in == null) {
            throw new NullPointerException(Messages.getString("auth.2C")); //$NON-NLS-1$
        }
        try {
            // FIXME dn = new Name(is);
            dn = (Name) Name.ASN1.decode(in);
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(Messages
                    .getString("auth.2B")); //$NON-NLS-1$
            iae.initCause(e);
            throw iae;
        }
    }

    public X500Principal(String name) {
        super();
        if (name == null) {
            throw new NullPointerException(Messages.getString("auth.00")); //$NON-NLS-1$
        }
        try {
            dn = new Name(name);
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(Messages
                    .getString("auth.2D")); //$NON-NLS-1$
            iae.initCause(e);
            throw iae;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        X500Principal principal = (X500Principal) o;
        return dn.getName(CANONICAL).equals(principal.dn.getName(CANONICAL));
    }

    public byte[] getEncoded() {
        byte[] src = dn.getEncoded();
        byte[] dst = new byte[src.length];
        System.arraycopy(src, 0, dst, 0, dst.length);
        return dst;
    }

    public String getName() {
        return dn.getName(RFC2253);
    }

    public String getName(String format) {
        return dn.getName(format);
    }

    @Override
    public int hashCode() {
        return dn.getName(CANONICAL).hashCode();
    }

    @Override
    public String toString() {
        return dn.getName(RFC1779);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(dn.getEncoded());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        dn = (Name) Name.ASN1.decode((byte[]) in.readObject());
    }
}
