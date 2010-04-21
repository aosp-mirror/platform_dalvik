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

package org.apache.harmony.luni.internal.net.www.protocol.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A pool of HTTP connections. This class exposes its tuning parameters as
 * system properties:
 * <ul>
 *   <li>{@code http.keepAlive} true if HTTP connections should be pooled at
 *       all. Default is true.
 *   <li>{@code http.maxConnections} maximum number of connections to each URI.
 *       Default is 5.
 * </ul>
 *
 * <p>This class <i>doesn't</i> adjust its configuration as system properties
 * are changed. This assumes that the applications that set these parameters do
 * so before making HTTP connections, and that this class is initialized lazily.
 *
 * <p>If a security manager is in place, HTTP connection pooling will be
 * disabled and these system properties will be ignored.
 */
public final class HttpConnectionPool {

    public static final HttpConnectionPool INSTANCE = new HttpConnectionPool();

    private final int maxConnections;
    private final HashMap<HttpConfiguration, List<HttpConnection>> connectionPool
            = new HashMap<HttpConfiguration, List<HttpConnection>>();

    private HttpConnectionPool() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            maxConnections = 0;
            return;
        }

        String keepAlive = System.getProperty("http.keepAlive");
        if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
            maxConnections = 0;
            return;
        }

        String maxConnectionsString = System.getProperty("http.maxConnections");
        this.maxConnections = maxConnectionsString != null
                ? Integer.parseInt(maxConnectionsString)
                : 5;
    }

    public HttpConnection get(HttpConfiguration config, int connectTimeout) throws IOException {
        // First try to reuse an existing HTTP connection.
        synchronized (connectionPool) {
            List<HttpConnection> connections = connectionPool.get(config);
            if (connections != null) {
                while (!connections.isEmpty()) {
                    HttpConnection connection = connections.remove(connections.size() - 1);
                    if (!connection.isStale()) {
                        return connection;
                    }
                }
                connectionPool.remove(config);
            }
        }

        /*
         * We couldn't find a reusable connection, so we need to create a new
         * connection. We're careful not to do so while holding a lock!
         */
        return new HttpConnection(config, connectTimeout);
    }

    public void recycle(HttpConnection connection) {
        if (maxConnections > 0 && connection.isEligibleForRecycling()) {
            HttpConfiguration config = connection.getHttpConfiguration();
            synchronized (connectionPool) {
                List<HttpConnection> connections = connectionPool.get(config);
                if (connections == null) {
                    connections = new ArrayList<HttpConnection>();
                    connectionPool.put(config, connections);
                }
                if (connections.size() < maxConnections) {
                    connections.add(connection);
                    return; // keep the connection open
                }
            }
        }

        // don't close streams while holding a lock!
        connection.closeSocketAndStreams();
    }
}