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

package org.apache.harmony.luni.internal.net.www.protocol.file;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.harmony.luni.util.Msg;

/**
 * This is the handler that is responsible for reading files from the file
 * system.
 */
public class Handler extends URLStreamHandler {

    /**
     * Returns a connection to the a file pointed by this <code>URL</code> in
     * the file system
     * 
     * @return A connection to the resource pointed by this url.
     * @param url
     *            URL The URL to which the connection is pointing to
     * 
     */
    @Override
    public URLConnection openConnection(URL url) {
        return new FileURLConnection(url);
    }

    /**
     * The behaviour of this method is the same as openConnection(URL).
     * <code>proxy</code> is not used in FileURLConnection.
     * 
     * @param u
     *            the URL which the connection is pointing to
     * @param proxy
     *            Proxy
     * @return a connection to the resource pointed by this url.
     * 
     * @throws IOException
     *             if this handler fails to establish a connection.
     * @throws IllegalArgumentException
     *             if any argument is null or of an invalid type.
     * @throws UnsupportedOperationException
     *             if the protocol handler doesn't support this method.
     */
    @Override
    public URLConnection openConnection(URL url, Proxy proxy)
            throws IOException {
        if (null == url || null == proxy) {
            // K034b=url and proxy can not be null
            throw new IllegalArgumentException(Msg.getString("K034b")); //$NON-NLS-1$
        }
        return new FileURLConnection(url);
    }

    /**
     * Parse the <code>string</code>str into <code>URL</code> u which
     * already have the context properties. The string generally have the
     * following format: <code><center>/c:/windows/win.ini</center></code>.
     * 
     * @param u
     *            The URL object that's parsed into
     * @param str
     *            The string equivalent of the specification URL
     * @param start
     *            The index in the spec string from which to begin parsing
     * @param end
     *            The index to stop parsing
     * 
     * @see java.net.URLStreamHandler#toExternalForm(URL)
     * @see java.net.URL
     */
    @Override
    protected void parseURL(URL u, String str, int start, int end) {
        if (end < start) {
            return;
        }
        String parseString = ""; //$NON-NLS-1$
        if (start < end) {
            parseString = str.substring(start, end).replace('\\', '/');
        }
        super.parseURL(u, parseString, 0, parseString.length());
    }
}
