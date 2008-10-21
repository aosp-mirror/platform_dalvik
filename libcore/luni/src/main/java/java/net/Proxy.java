/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.net;

import org.apache.harmony.luni.util.Msg;

/**
 * This class is about proxy setting. A proxy contains <code>type</code>,
 * proxy host address information. There are three types of <code>Proxy</code>:
 * <li>Direct type proxy</li>
 * <li>HTTP type proxy</li>
 * <li>SOCKS type proxy</li>
 * 
 * A <code>Proxy</code> instance is immutable.
 * 
 */
public class Proxy {

    /**
     * Represents <code>Proxy.Type.DIRECT</code> type proxy setting. It tells
     * protocol handlers not to use any proxy.
     */
    public static final Proxy NO_PROXY = new Proxy();

    private Proxy.Type type;

    private SocketAddress address;

    /**
     * New a <code>Proxy</code> instance. SocketAddress must NOT be null when
     * <code>type</code> is either <code>Proxy.Type.HTTP</code> or
     * <code>Proxy.Type.SOCKS</code>. For <code>Proxy.Type.DIRECT</code>
     * type proxy, use <code>Proxy.NO_PROXY</code> directly instead of
     * constructing it.
     * 
     * @param type
     *            proxy type
     * @param sa
     *            proxy address
     * @throws IllegalArgumentException
     *             when <code>type</code> is <code>Proxy.Type.DIRECT</code>
     *             or SocketAddress is null.
     */
    public Proxy(Proxy.Type type, SocketAddress sa) {
        /*
         * Don't use DIRECT type to construct a proxy instance directly.
         * SocketAddress must NOT be null.
         */
        if (type == Type.DIRECT || null == sa) {
            // KA022=Illegal Proxy.Type or SocketAddress argument
            throw new IllegalArgumentException(Msg.getString("KA022")); //$NON-NLS-1$
        }
        this.type = type;
        address = sa;
    }

    /*
     * Constructs a Proxy instance, which is Proxy.DIRECT type with null
     * SocketAddress. This constructor is used for NO_PROXY.
     */
    private Proxy() {
        type = Type.DIRECT;
        address = null;
    }

    /**
     * Gets the proxy type.
     * 
     * @return the proxy type.
     */
    public Proxy.Type type() {
        return type;
    }

    /**
     * Gets the proxy address.
     * 
     * @return the proxy address for <code>HTTP</code> and <code>SOCKS</code>
     *         type proxy. Returns null for <code>DIRECT</code> type proxy.
     */
    public SocketAddress address() {
        return address;
    }

    /**
     * <p>
     * Representing string of the proxy. The string consists of
     * <code>type.toString()</code> and <code>address.toString()</code> if
     * <code>type</code> and <code>address</code> are not null.
     * </p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     * @return representing string of the proxy.
     */
    @Override
    public String toString() {
        String proxyString = String.valueOf(type);
        if (null != address) {
            proxyString += "/" + address.toString(); //$NON-NLS-1$
        }
        return proxyString;
    }

    /**
     * <p>
     * Compare <code>obj</code> with current proxy. Returns false if the
     * <code>obj</code> is not a <code>Proxy</code> object. Returns true if
     * and only if the <code>obj</code> has the same <code>address</code>
     * and <code>type</code> value as current proxy.
     * </p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     * @return true if <code>obj</code> represents the same proxy. Otherwise,
     *         returns false.
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Proxy)) {
            return false;
        }
        Proxy another = (Proxy) obj;
        // address is null when and only when it's NO_PROXY.
        return (type == another.type) && address.equals(another.address);
    }

    /**
     * gets the hash code of <code>Proxy</code>.
     * 
     * @see java.lang.Object#hashCode()
     * @return the hash code of <code>Proxy</code>.
     */
    @Override
    public final int hashCode() {
        int ret = 0;
        ret += type.hashCode();
        if (null != address) {
            ret += address.hashCode();
        }
        return ret;
    }

    /**
     * The proxy type, includes <code>DIRECT</code>, <code>HTTP</code> and
     * <code>SOCKS</code>.
     */
    public enum Type {
        /**
         * Direct connection. Connect without any proxy.
         */
        DIRECT,

        /**
         * HTTP type proxy. It's often used by protocol handlers such as HTTP,
         * HTTPS and FTP.
         */
        HTTP,

        /**
         * SOCKS type proxy.
         */
        SOCKS
    }
}
