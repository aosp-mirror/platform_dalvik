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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ResponseCache;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.harmony.luni.util.Base64;
import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * This subclass extends <code>HttpURLConnection</code> which in turns extends
 * <code>URLConnection</code> This is the actual class that "does the work",
 * such as connecting, sending request and getting the content from the remote
 * server.
 */
public class HttpURLConnectionImpl extends HttpURLConnection {
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String HEAD = "HEAD";
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final byte[] HEX_DIGITS = new byte[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private final int defaultPort;

    private int httpVersion = 1; // Assume HTTP/1.1

    protected HttpConnection connection;

    private InputStream is;

    private InputStream uis;

    private OutputStream socketOut;

    private OutputStream cacheOut;

    private ResponseCache responseCache;

    private CacheResponse cacheResponse;

    private CacheRequest cacheRequest;

    private boolean hasTriedCache;

    private HttpOutputStream os;

    private boolean sentRequest;

    boolean sendChunked;

    private String proxyName;

    private int hostPort = -1;

    private String hostName;

    private InetAddress hostAddress;

    // proxy which is used to make the connection.
    private Proxy proxy;

    // the destination URI
    private URI uri;

    // default request header
    private static Header defaultReqHeader = new Header();

    // request header that will be sent to the server
    private Header reqHeader;

    // response header received from the server
    private Header resHeader;

    // BEGIN android-added
    /**
     * An <code>InputStream</code> wrapper that does <i>not</i> pass
     * <code>close()</code> calls to the wrapped stream but instead
     * treats it as a local shutoff.
     */
    private class LocalCloseInputStream extends InputStream {
        private boolean closed;

        public LocalCloseInputStream() {
            closed = false;
        }

        public int read() throws IOException {
            if (closed) {
                throwClosed();
            }

            int result = is.read();
            if (useCaches && cacheOut != null) {
                cacheOut.write(result);
            }
            return result;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throwClosed();
            }
            int result = is.read(b, off, len);
            if (result > 0) {
                // if user has set useCache to true and cache exists, writes to
                // it
                if (useCaches && cacheOut != null) {
                    cacheOut.write(b, off, result);
                }
            }
            return result;
        }

        public int read(byte[] b) throws IOException {
            if (closed) {
                throwClosed();
            }
            int result = is.read(b);
            if (result > 0) {
                // if user has set useCache to true and cache exists, writes to
                // it
                if (useCaches && cacheOut != null) {
                    cacheOut.write(b, 0, result);
                }
            }
            return result;
        }

        public long skip(long n) throws IOException {
            if (closed) {
                throwClosed();
            }

            return is.skip(n);
        }

        public int available() throws IOException {
            if (closed) {
                throwClosed();
            }

            return is.available();
        }

        public void close() {
            closed = true;
            if (useCaches && cacheRequest != null) {
                cacheRequest.abort();
            }
        }

        public void mark(int readLimit) {
            if (! closed) {
                is.mark(readLimit);
            }
        }

        public void reset() throws IOException {
            if (closed) {
                throwClosed();
            }

            is.reset();
        }

        public boolean markSupported() {
            return is.markSupported();
        }

        private void throwClosed() throws IOException {
            throw new IOException("stream closed");
        }
    }
    // END android-added

    private class LimitedInputStream extends InputStream {
        int bytesRemaining;

        public LimitedInputStream(int length) {
            bytesRemaining = length;
        }

        @Override
        public void close() throws IOException {
            if(bytesRemaining > 0) {
                bytesRemaining = 0;
                disconnect(true); // Should close the socket if client hasn't read all the data
            } else {
                disconnect(false);
            }
            /*
             * if user has set useCache to true and cache exists, aborts it when
             * closing
             */
            if (useCaches && null != cacheRequest) {
                cacheRequest.abort();
            }
        }

        @Override
        public int available() throws IOException {
            // BEGIN android-added
            if (bytesRemaining <= 0) {
                // There is nothing left to read, so don't bother asking "is".
                return 0;
            }
            // END android-added
            int result = is.available();
            if (result > bytesRemaining) {
                return bytesRemaining;
            }
            return result;
        }

        @Override
        public int read() throws IOException {
            if (bytesRemaining <= 0) {
                disconnect(false);
                return -1;
            }
            int result = is.read();
            // if user has set useCache to true and cache exists, writes to
            // cache
            if (useCaches && null != cacheOut) {
                cacheOut.write(result);
            }
            bytesRemaining--;
            if (bytesRemaining <= 0) {
                disconnect(false);
            }
            return result;
        }

        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            // Force buf null check first, and avoid int overflow
            if (offset < 0 || offset > buf.length) {
                // K002e=Offset out of bounds \: {0}
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset));
            }
            if (length < 0 || buf.length - offset < length) {
                // K0031=Length out of bounds \: {0}
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length));
            }
            if (bytesRemaining <= 0) {
                disconnect(false);
                return -1;
            }
            if (length > bytesRemaining) {
                length = bytesRemaining;
            }
            int result = is.read(buf, offset, length);
            if (result > 0) {
                bytesRemaining -= result;
                // if user has set useCache to true and cache exists, writes to
                // it
                if (useCaches && null != cacheOut) {
                    cacheOut.write(buf, offset, result);
                }
            }
            if (bytesRemaining <= 0) {
                disconnect(false);
            }
            return result;
        }

        public long skip(int amount) throws IOException {
            if (bytesRemaining <= 0) {
                disconnect(false);
                return -1;
            }
            if (amount > bytesRemaining) {
                amount = bytesRemaining;
            }
            long result = is.skip(amount);
            if (result > 0) {
                bytesRemaining -= result;
            }
            if (bytesRemaining <= 0) {
                disconnect(false);
            }
            return result;
        }
    }

    private class ChunkedInputStream extends InputStream {
        private int bytesRemaining = -1;
        private boolean atEnd;

        public ChunkedInputStream() throws IOException {
            readChunkSize();
        }

        @Override
        public void close() throws IOException {
            // BEGIN android-added
            if (atEnd) {
                return;
            }
            skipOutstandingChunks();
            // END android-added

            // BEGIN android-note
            // Removed "!atEnd" below because of the check added above.
            // END android-note
            if (available() > 0) {
                disconnect(true);
            } else {
                disconnect(false);
            }
            atEnd = true;
            // if user has set useCache to true and cache exists, abort
            if (useCaches && null != cacheRequest) {
                cacheRequest.abort();
            }
        }

        // BEGIN android-added
        // If we're asked to close a stream with unread chunks, we need to skip them.
        // Otherwise the next caller on this connection will receive that data.
        // See: http://code.google.com/p/android/issues/detail?id=2939
        private void skipOutstandingChunks() throws IOException {
            while (!atEnd) {
                while (bytesRemaining > 0) {
                    long skipped = is.skip(bytesRemaining);
                    bytesRemaining -= skipped;
                }
                readChunkSize();
            }
        }
        // END android-added

        @Override
        public int available() throws IOException {
            // BEGIN android-added
            if (atEnd) {
                return 0;
            }
            // END android-added

            int result = is.available();
            if (result > bytesRemaining) {
                return bytesRemaining;
            }
            return result;
        }

        private void readChunkSize() throws IOException {
            if (atEnd) {
                return;
            }
            if (bytesRemaining == 0) {
                readln(); // read CR/LF
            }
            String size = readln();
            int index = size.indexOf(";");
            if (index >= 0) {
                size = size.substring(0, index);
            }
            bytesRemaining = Integer.parseInt(size.trim(), 16);
            if (bytesRemaining == 0) {
                atEnd = true;
                // BEGIN android-note
                // What is the point of calling readHeaders() here?
                // END android-note
                readHeaders();
            }
        }

        @Override
        public int read() throws IOException {
            if (bytesRemaining <= 0) {
                readChunkSize();
            }
            if (atEnd) {
                disconnect(false);
                return -1;
            }
            bytesRemaining--;
            int result = is.read();
            // if user has set useCache to true and cache exists, write to cache
            if (useCaches && null != cacheOut) {
                cacheOut.write(result);
            }
            return result;
        }

        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            // Force buf null check first, and avoid int overflow
            if (offset > buf.length || offset < 0) {
                // K002e=Offset out of bounds \: {0}
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset));
            }
            if (length < 0 || buf.length - offset < length) {
                // K0031=Length out of bounds \: {0}
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length));
            }
            if (bytesRemaining <= 0) {
                readChunkSize();
            }
            if (atEnd) {
                disconnect(false);
                return -1;
            }
            if (length > bytesRemaining) {
                length = bytesRemaining;
            }
            int result = is.read(buf, offset, length);
            if (result > 0) {
                bytesRemaining -= result;
                // if user has set useCache to true and cache exists, write to
                // it
                if (useCaches && null != cacheOut) {
                    cacheOut.write(buf, offset, result);
                }
            }
            return result;
        }

        public long skip(int amount) throws IOException {
            if (atEnd) {
                // BEGIN android-deleted
                // disconnect(false);
                // END android-deleted
                return -1;
            }
            if (bytesRemaining <= 0) {
                readChunkSize();
            }
            // BEGIN android-added
            if (atEnd) {
                disconnect(false);
                return -1;
            }
            // END android-added
            if (amount > bytesRemaining) {
                amount = bytesRemaining;
            }
            long result = is.skip(amount);
            if (result > 0) {
                bytesRemaining -= result;
            }
            return result;
        }
    }

    /**
     * An HttpOutputStream used to implement setFixedLengthStreamingMode.
     */
    private class FixedLengthHttpOutputStream extends HttpOutputStream {
        private final int fixedLength;
        private int actualLength;

        public FixedLengthHttpOutputStream(int fixedLength) {
            this.fixedLength = fixedLength;
        }

        @Override public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            socketOut.flush();
            if (actualLength != fixedLength) {
                throw new IOException("actual length of " + actualLength +
                        " did not match declared fixed length of " + fixedLength);
            }
        }

        @Override public void flush() throws IOException {
            checkClosed();
            socketOut.flush();
        }

        @Override public void write(byte[] buffer, int offset, int count) throws IOException {
            checkClosed();
            if (buffer == null) {
                throw new NullPointerException();
            }
            if (offset < 0 || count < 0 || offset > buffer.length || buffer.length - offset < count) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f"));
            }
            checkSpace(count);
            socketOut.write(buffer, offset, count);
            actualLength += count;
        }

        @Override public void write(int oneByte) throws IOException {
            checkClosed();
            checkSpace(1);
            socketOut.write(oneByte);
            ++actualLength;
        }

        @Override public int size() {
            return fixedLength;
        }

        private void checkSpace(int byteCount) throws IOException {
            if (actualLength + byteCount > fixedLength) {
                throw new IOException("declared fixed content length of " + fixedLength +
                        " bytes exceeded");
            }
        }
    }

    private abstract class HttpOutputStream extends OutputStream {
        public boolean closed;

        protected void checkClosed() throws IOException {
            if (closed) {
                throw new IOException(Msg.getString("K0059"));
            }
        }

        public boolean isCached() {
            return false;
        }

        public boolean isChunked() {
            return false;
        }

        public void flushToSocket() throws IOException {
        }

        public abstract int size();
    }

    private static final byte[] FINAL_CHUNK = new byte[] { '0', '\r', '\n', '\r', '\n' };

    // TODO: pull ChunkedHttpOutputStream out of here.
    private class DefaultHttpOutputStream extends HttpOutputStream {
        private int cacheLength;
        private int defaultCacheSize = 1024;
        private ByteArrayOutputStream cache;
        private boolean writeToSocket;
        private int limit;

        public DefaultHttpOutputStream() {
            cacheLength = defaultCacheSize;
            cache = new ByteArrayOutputStream(cacheLength);
            limit = -1;
        }

        public DefaultHttpOutputStream(int limit, int chunkLength) {
            writeToSocket = true;
            this.limit = limit;
            if (limit > 0) {
                cacheLength = limit;
            } else {
                // chunkLength must be larger than 3
                if (chunkLength > 3) {
                    defaultCacheSize = chunkLength;
                }
                cacheLength = calculateChunkDataLength();
            }
            cache = new ByteArrayOutputStream(cacheLength);
        }

        /**
         * Calculates the exact size of chunk data, chunk data size is chunk
         * size minus chunk head (which writes chunk data size in HEX and
         * "\r\n") size. For example, a string "abcd" use chunk whose size is 5
         * must be written to socket as "2\r\nab","2\r\ncd" ...
         *
         */
        private int calculateChunkDataLength() {
            /*
             * chunk head size is the hex string length of the cache size plus 2
             * (which is the length of "\r\n"), it must be suitable to express
             * the size of chunk data, as short as possible. Notices that
             * according to RI, if chunklength is 19, chunk head length is 4
             * (expressed as "10\r\n"), chunk data length is 16 (which real sum
             * is 20,not 19); while if chunklength is 18, chunk head length is
             * 3. Thus the cacheSize = chunkdataSize + sizeof(string length of
             * chunk head in HEX) + sizeof("\r\n");
             */
            int bitSize = Integer.toHexString(defaultCacheSize).length();
            /*
             * here is the calculated head size, not real size (for 19, it
             * counts 3, not real size 4)
             */
            int headSize = (Integer.toHexString(defaultCacheSize - bitSize - 2).length()) + 2;
            return defaultCacheSize - headSize;
        }

        /**
         * Equivalent to, but cheaper than, Integer.toHexString().getBytes().
         */
        private void writeHex(int i) throws IOException {
            int cursor = 8;
            do {
                hex[--cursor] = HEX_DIGITS[i & 0xf];
            } while ((i >>>= 4) != 0);
            socketOut.write(hex, cursor, 8 - cursor);
        }
        private byte[] hex = new byte[8];

        private void sendCache(boolean close) throws IOException {
            int size = cache.size();
            if (size > 0 || close) {
                if (limit < 0) {
                    if (size > 0) {
                        writeHex(size);
                        socketOut.write(CRLF);
                        cache.writeTo(socketOut);
                        cache.reset();
                        socketOut.write(CRLF);
                    }
                    if (close) {
                        socketOut.write(FINAL_CHUNK);
                    }
                }
            }
        }

        @Override
        public synchronized void flush() throws IOException {
            checkClosed();
            if (writeToSocket) {
                sendCache(false);
                socketOut.flush();
            }
        }

        @Override
        public void flushToSocket() throws IOException  {
            if (isCached()) {
                cache.writeTo(socketOut);
            }
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            if (writeToSocket) {
                if (limit > 0) {
                    throw new IOException(Msg.getString("K00a4"));
                }
                sendCache(closed);
            }
            // BEGIN android-added
            /*
             * Note: We don't disconnect here, since that will either
             * cause the connection to be closed entirely or returned
             * to the connection pool. In the former case, we simply
             * won't be able to read the response at all. In the
             * latter, we might end up trying to read the response
             * while, meanwhile, the connection has been handed back
             * out and is in use for another request.
             */
            // END android-added
            // BEGIN android-deleted
            // disconnect(false);
            // END android-deleted
        }

        @Override
        public synchronized void write(int data) throws IOException {
            checkClosed();
            if (limit >= 0) {
                if (limit == 0) {
                    throw new IOException(Msg.getString("K00b2"));
                }
                limit--;
            }
            cache.write(data);
            if (writeToSocket && cache.size() >= cacheLength) {
                sendCache(false);
            }
        }

        @Override
        public synchronized void write(byte[] buffer, int offset, int count) throws IOException {
            checkClosed();
            if (buffer == null) {
                throw new NullPointerException();
            }
            // avoid int overflow
            if (offset < 0 || count < 0 || offset > buffer.length
                    || buffer.length - offset < count) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f"));
            }

            if (limit >= 0) {
                if (count > limit) {
                    throw new IOException(Msg.getString("K00b2"));
                }
                limit -= count;
                cache.write(buffer, offset, count);
                if (limit == 0) {
                    cache.writeTo(socketOut);
                }
            } else {
                if (!writeToSocket || cache.size() + count < cacheLength) {
                    cache.write(buffer, offset, count);
                } else {
                    writeHex(cacheLength);
                    socketOut.write(CRLF);
                    int writeNum = cacheLength - cache.size();
                    cache.write(buffer, offset, writeNum);
                    cache.writeTo(socketOut);
                    cache.reset();
                    socketOut.write(CRLF);
                    int left = count - writeNum;
                    int position = offset + writeNum;
                    while (left > cacheLength) {
                        writeHex(cacheLength);
                        socketOut.write(CRLF);
                        socketOut.write(buffer, position, cacheLength);
                        socketOut.write(CRLF);
                        left = left - cacheLength;
                        position = position + cacheLength;
                    }
                    cache.write(buffer, position, left);
                }
            }
        }

        @Override
        public synchronized int size() {
            return cache.size();
        }

        @Override public boolean isCached() {
            return !writeToSocket;
        }

        @Override public boolean isChunked() {
            return writeToSocket && limit == -1;
        }
    }

    /**
     * Creates an instance of the <code>HttpURLConnection</code> using default
     * port 80.
     *
     * @param url
     *            URL The URL this connection is connecting
     */
    protected HttpURLConnectionImpl(URL url) {
        this(url, 80);
    }

    /**
     * Creates an instance of the <code>HttpURLConnection</code>
     *
     * @param url
     *            URL The URL this connection is connecting
     * @param port
     *            int The default connection port
     */
    protected HttpURLConnectionImpl(URL url, int port) {
        super(url);
        defaultPort = port;
        reqHeader = (Header) defaultReqHeader.clone();

        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            // do nothing.
        }
        responseCache = AccessController
                .doPrivileged(new PrivilegedAction<ResponseCache>() {
                    public ResponseCache run() {
                        return ResponseCache.getDefault();
                    }
                });
    }

    /**
     * Creates an instance of the <code>HttpURLConnection</code>
     *
     * @param url
     *            URL The URL this connection is connecting
     * @param port
     *            int The default connection port
     * @param proxy
     *            Proxy The proxy which is used to make the connection
     */
    protected HttpURLConnectionImpl(URL url, int port, Proxy proxy) {
        this(url, port);
        this.proxy = proxy;
    }

    /**
     * Establishes the connection to the remote HTTP server
     *
     * Any methods that requires a valid connection to the resource will call
     * this method implicitly. After the connection is established,
     * <code>connected</code> is set to true.
     *
     *
     * @see #connected
     * @see java.io.IOException
     * @see URLStreamHandler
     */
    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        if (getFromCache()) {
            return;
        }
        // BEGIN android-changed
        // url.toURI(); throws an URISyntaxException if the url contains
        // illegal characters in e.g. the query.
        // Since the query is not needed for proxy selection, we just create an
        // URI that only contains the necessary information.
        try {
            uri = new URI(url.getProtocol(),
                          null,
                          url.getHost(),
                          url.getPort(),
                          null,
                          null,
                          null);
        } catch (URISyntaxException e1) {
            throw new IOException(e1.getMessage());
        }
        // END android-changed
        // socket to be used for connection
        connection = null;
        // try to determine: to use the proxy or not
        if (proxy != null) {
            // try to make the connection to the proxy
            // specified in constructor.
            // IOException will be thrown in the case of failure
            connection = getHTTPConnection(proxy);
        } else {
            // Use system-wide ProxySelect to select proxy list,
            // then try to connect via elements in the proxy list.
            ProxySelector selector = ProxySelector.getDefault();
            List<Proxy> proxyList = selector.select(uri);
            if (proxyList != null) {
                for (Proxy selectedProxy : proxyList) {
                    if (selectedProxy.type() == Proxy.Type.DIRECT) {
                        // the same as NO_PROXY
                        continue;
                    }
                    try {
                        connection = getHTTPConnection(selectedProxy);
                        proxy = selectedProxy;
                        break; // connected
                    } catch (IOException e) {
                        // failed to connect, tell it to the selector
                        selector.connectFailed(uri, selectedProxy.address(), e);
                    }
                }
            }
        }
        if (connection == null) {
            // make direct connection
            connection = getHTTPConnection(null);
        }
        connection.setSoTimeout(getReadTimeout());
        setUpTransportIO(connection);
        connected = true;
    }

    /**
     * Returns connected socket to be used for this HTTP connection.
     */
    protected HttpConnection getHTTPConnection(Proxy proxy) throws IOException {
        HttpConfiguration configuration;
        if (proxy == null || proxy.type() == Proxy.Type.DIRECT) {
            this.proxy = null; // not using proxy
            configuration = new HttpConfiguration(uri);
        } else {
            configuration = new HttpConfiguration(uri, proxy);
        }
        return HttpConnectionPool.INSTANCE.get(configuration, getConnectTimeout());
    }

    /**
     * Sets up the data streams used to send request[s] and read response[s].
     *
     * @param connection
     *            HttpConnection to be used
     */
    protected void setUpTransportIO(HttpConnection connection) throws IOException {
        socketOut = connection.getOutputStream();
        is = connection.getInputStream();
    }

    // Tries to get head and body from cache, return true if has got this time
    // or
    // already got before
    private boolean getFromCache() throws IOException {
        if (useCaches && null != responseCache && !hasTriedCache) {
            hasTriedCache = true;
            if (null == resHeader) {
                resHeader = new Header();
            }
            cacheResponse = responseCache.get(uri, method, resHeader
                    .getFieldMap());
            if (null != cacheResponse) {
                Map<String, List<String>> headMap = cacheResponse.getHeaders();
                if (null != headMap) {
                    resHeader = new Header(headMap);
                }
                is = cacheResponse.getBody();
                if (null != is) {
                    return true;
                }
            }
        }
        if (hasTriedCache && null != is) {
            return true;
        }
        return false;
    }

    // if user sets useCache to true, tries to put response to cache if cache
    // exists
    private void putToCache() throws IOException {
        if (useCaches && null != responseCache) {
            cacheRequest = responseCache.put(uri, this);
            if (null != cacheRequest) {
                cacheOut = cacheRequest.getBody();
            }
        }
    }

    /**
     * Closes the connection with the HTTP server
     *
     *
     * @see URLConnection#connect()
     */
    @Override
    public void disconnect() {
        disconnect(true);
    }

    // BEGIN android-changed
    private synchronized void disconnect(boolean closeSocket) {
        if (connection != null) {
            if (closeSocket || ((os != null) && !os.closed)) {
                /*
                 * In addition to closing the socket if explicitly
                 * requested to do so, we also close it if there was
                 * an output stream associated with the request and it
                 * wasn't cleanly closed.
                 */
                connection.closeSocketAndStreams();
            } else {
                HttpConnectionPool.INSTANCE.recycle(connection);
            }
            connection = null;
        }

        /*
         * Clear "is" and "os" to ensure that no further I/O attempts
         * from this instance make their way to the underlying
         * connection (which may get recycled).
         */
        is = null;
        os = null;
    }
    // END android-changed

    protected void endRequest() throws IOException {
        if (os != null) {
            os.close();
        }
        sentRequest = false;
    }

    /**
     * Returns the default value for the field specified by <code>field</code>,
     * null if there's no such a field.
     */
    public static String getDefaultRequestProperty(String field) {
        return defaultReqHeader.get(field);
    }

    /**
     * Returns an input stream from the server in the case of error such as the
     * requested file (txt, htm, html) is not found on the remote server.
     * <p>
     * If the content type is not what stated above,
     * <code>FileNotFoundException</code> is thrown.
     *
     * @return InputStream the error input stream returned by the server.
     */
    @Override
    public InputStream getErrorStream() {
        if (connected && method != HEAD && responseCode >= HTTP_BAD_REQUEST) {
            return uis;
        }
        return null;
    }

    /**
     * Returns the value of the field at position <code>pos<code>.
     * Returns <code>null</code> if there is fewer than <code>pos</code> fields
     * in the response header.
     *
     * @return java.lang.String     The value of the field
     * @param pos int               the position of the field from the top
     *
     * @see         #getHeaderField(String)
     * @see         #getHeaderFieldKey
     */
    @Override
    public String getHeaderField(int pos) {
        try {
            getInputStream();
        } catch (IOException e) {
            // ignore
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.get(pos);
    }

    /**
     * Returns the value of the field corresponding to the <code>key</code>
     * Returns <code>null</code> if there is no such field.
     *
     * If there are multiple fields with that key, the last field value is
     * returned.
     *
     * @return java.lang.String The value of the header field
     * @param key
     *            java.lang.String the name of the header field
     *
     * @see #getHeaderField(int)
     * @see #getHeaderFieldKey
     */
    @Override
    public String getHeaderField(String key) {
        try {
            getInputStream();
        } catch (IOException e) {
            // ignore
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.get(key);
    }

    @Override
    public String getHeaderFieldKey(int pos) {
        try {
            getInputStream();
        } catch (IOException e) {
            // ignore
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.getKey(pos);
    }

    /**
     * Provides an unmodifiable map of the connection header values. The map
     * keys are the String header field names. Each map value is a list of the
     * header field values associated with that key name.
     *
     * @return the mapping of header field names to values
     *
     * @since 1.4
     */
    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            // ensure that resHeader exists
            getInputStream();
        } catch (IOException e) {
            // ignore
        }
        if (null == resHeader) {
            return null;
        }
        return resHeader.getFieldMap();
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0091"));
        }
        return reqHeader.getFieldMap();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!doInput) {
            throw new ProtocolException(Msg.getString("K008d"));
        }

        // connect before sending requests
        connect();
        doRequest();

        /*
         * if the requested file does not exist, throw an exception formerly the
         * Error page from the server was returned if the requested file was
         * text/html this has changed to return FileNotFoundException for all
         * file types
         */
        if (responseCode >= HTTP_BAD_REQUEST) {
            throw new FileNotFoundException(url.toString());
        }

        return uis;
    }

    private InputStream getContentStream() throws IOException {
        if (uis != null) {
            return uis;
        }

        String encoding = resHeader.get("Transfer-Encoding");
        if (encoding != null && encoding.toLowerCase().equals("chunked")) {
            return uis = new ChunkedInputStream();
        }

        String sLength = resHeader.get("Content-Length");
        if (sLength != null) {
            try {
                int length = Integer.parseInt(sLength);
                return uis = new LimitedInputStream(length);
            } catch (NumberFormatException e) {
            }
        }

        // BEGIN android-changed
        /*
         * Wrap the input stream from the HttpConnection (rather than
         * just returning "is" directly here), so that we can control
         * its use after the reference escapes.
         */
        return uis = new LocalCloseInputStream();
        // END android-changed
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!doOutput) {
            throw new ProtocolException(Msg.getString("K008e"));
        }

        // you can't write after you read
        if (sentRequest) {
            throw new ProtocolException(Msg.getString("K0090"));
        }

        if (os != null) {
            return os;
        }

        // they are requesting a stream to write to. This implies a POST method
        if (method == GET) {
            method = POST;
        }

        // If the request method is neither PUT or POST, then you're not writing
        if (method != PUT && method != POST) {
            throw new ProtocolException(Msg.getString("K008f", method));
        }

        int limit = -1;
        String contentLength = reqHeader.get("Content-Length");
        if (contentLength != null) {
            limit = Integer.parseInt(contentLength);
        }

        String encoding = reqHeader.get("Transfer-Encoding");
        if (httpVersion > 0 && encoding != null) {
            encoding = encoding.toLowerCase();
            if ("chunked".equals(encoding)) {
                sendChunked = true;
                limit = -1;
            }
        }
        // if user has set chunk/fixedLength mode, use that value
        if (chunkLength > 0) {
            sendChunked = true;
            limit = -1;
        }
        if (fixedContentLength >= 0) {
            os = new FixedLengthHttpOutputStream(fixedContentLength);
            doRequest();
            return os;
        }
        if ((httpVersion > 0 && sendChunked) || limit >= 0) {
            os = new DefaultHttpOutputStream(limit, chunkLength);
            doRequest();
            return os;
        }
        if (!connected) {
            // connect and see if there is cache available.
            connect();
        }
        return os = new DefaultHttpOutputStream();
    }

    @Override
    public Permission getPermission() throws IOException {
        return new SocketPermission(getHostName() + ":" + getHostPort(), "connect, resolve");
    }

    @Override
    public String getRequestProperty(String field) {
        if (null == field) {
            return null;
        }
        return reqHeader.get(field);
    }

    /**
     * Returns a line read from the input stream. Does not include the \n
     *
     * @return The line that was read.
     */
    String readln() throws IOException {
        boolean lastCr = false;
        StringBuilder result = new StringBuilder(80);
        int c = is.read();
        if (c < 0) {
            return null;
        }
        while (c != '\n') {
            if (lastCr) {
                result.append('\r');
                lastCr = false;
            }
            if (c == '\r') {
                lastCr = true;
            } else {
                result.append((char) c);
            }
            c = is.read();
            if (c < 0) {
                break;
            }
        }
        return result.toString();
    }

    protected String requestString() {
        if (usingProxy() || proxyName != null) {
            return url.toString();
        }
        String file = url.getFile();
        if (file == null || file.length() == 0) {
            file = "/";
        }
        return file;
    }

    /**
     * Sends the request header to the remote HTTP server Not all of them are
     * guaranteed to have any effect on the content the server will return,
     * depending on if the server supports that field.
     *
     * Examples : Accept: text/*, text/html, text/html;level=1, Accept-Charset:
     * iso-8859-5, unicode-1-1;q=0.8
     */
    private boolean sendRequest() throws IOException {
        byte[] request = createRequest();

        // make sure we have a connection
        if (!connected) {
            connect();
        }
        if (null != cacheResponse) {
            // does not send if already has a response cache
            return true;
        }
        // send out the HTTP request
        socketOut.write(request);
        sentRequest = true;
        // send any output to the socket (i.e. POST data)
        if (os != null) {
            os.flushToSocket();
        }
        if (os == null || os.isCached()) {
            readServerResponse();
            return true;
        }
        return false;
    }

    void readServerResponse() throws IOException {
        socketOut.flush();
        do {
            responseCode = -1;
            responseMessage = null;
            resHeader = new Header();
            String line = readln();
            // Add the response, it may contain ':' which we ignore
            if (line != null) {
                resHeader.setStatusLine(line.trim());
                readHeaders();
            }
        } while (getResponseCode() == 100);

        if (method == HEAD || (responseCode >= 100 && responseCode < 200)
                || responseCode == HTTP_NO_CONTENT
                || responseCode == HTTP_NOT_MODIFIED) {
            disconnect();
            uis = new LimitedInputStream(0);
        }
        putToCache();
    }

    @Override
    public int getResponseCode() throws IOException {
        // Response Code Sample : "HTTP/1.0 200 OK"

        // Call connect() first since getHeaderField() doesn't return exceptions
        connect();
        doRequest();
        if (responseCode != -1) {
            return responseCode;
        }
        String response = resHeader.getStatusLine();
        if (response == null || !response.startsWith("HTTP/")) {
            return -1;
        }
        response = response.trim();
        int mark = response.indexOf(" ") + 1;
        if (mark == 0) {
            return -1;
        }
        if (response.charAt(mark - 2) != '1') {
            httpVersion = 0;
        }
        int last = mark + 3;
        if (last > response.length()) {
            last = response.length();
        }
        responseCode = Integer.parseInt(response.substring(mark, last));
        if (last + 1 <= response.length()) {
            responseMessage = response.substring(last + 1);
        }
        return responseCode;
    }

    void readHeaders() throws IOException {
        // parse the result headers until the first blank line
        String line;
        while (((line = readln()) != null) && (line.length() > 1)) {
            // Header parsing
            int idx;
            if ((idx = line.indexOf(":")) < 0) {
                resHeader.add("", line.trim());
            } else {
                resHeader.add(line.substring(0, idx), line.substring(idx + 1).trim());
            }
        }
    }

    private byte[] createRequest() throws IOException {
        StringBuilder output = new StringBuilder(256);
        output.append(method);
        output.append(' ');
        output.append(requestString());
        output.append(' ');
        output.append("HTTP/1.");
        if (httpVersion == 0) {
            output.append("0\r\n");
        } else {
            output.append("1\r\n");
        }
        // add user-specified request headers if any
        boolean hasContentLength = false;
        for (int i = 0; i < reqHeader.length(); i++) {
            String key = reqHeader.getKey(i);
            if (key != null) {
                String lKey = key.toLowerCase();
                if ((os != null && !os.isChunked())
                        || (!lKey.equals("transfer-encoding") && !lKey.equals("content-length"))) {
                    output.append(key);
                    String value = reqHeader.get(i);
                    /*
                     * duplicates are allowed under certain conditions see
                     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
                     */
                    if (lKey.equals("content-length")) {
                        hasContentLength = true;
                        /*
                         * if both setFixedLengthStreamingMode and
                         * content-length are set, use fixedContentLength first
                         */
                        if(fixedContentLength >= 0){
                            value = String.valueOf(fixedContentLength);
                        }
                    }
                    if (value != null) {
                        output.append(": ");
                        output.append(value);
                    }
                    output.append("\r\n");
                }
            }
        }
        if (fixedContentLength >= 0 && !hasContentLength) {
            output.append("content-length: ");
            output.append(String.valueOf(fixedContentLength));
            output.append("\r\n");
        }

        if (reqHeader.get("User-Agent") == null) {
            output.append("User-Agent: ");
            String agent = getSystemProperty("http.agent");
            if (agent == null) {
                output.append("Java");
                output.append(getSystemProperty("java.version"));
            } else {
                output.append(agent);
            }
            output.append("\r\n");
        }
        if (reqHeader.get("Host") == null) {
            output.append("Host: ");
            output.append(url.getHost());
            int port = url.getPort();
            if (port > 0 && port != defaultPort) {
                output.append(':');
                output.append(Integer.toString(port));
            }
            output.append("\r\n");
        }
        // BEGIN android-removed
        //     there's no utility in sending an "accept everything" header "*/*"
        // if (reqHeader.get("Accept") == null) {
        // }
        // END android-removed
        if (httpVersion > 0 && reqHeader.get("Connection") == null) {
            output.append("Connection: Keep-Alive\r\n");
        }

        // if we are doing output make sure the appropriate headers are sent
        if (os != null) {
            if (reqHeader.get("Content-Type") == null) {
                output.append("Content-Type: application/x-www-form-urlencoded\r\n");
            }
            if (os.isCached()) {
                if (reqHeader.get("Content-Length") == null) {
                    output.append("Content-Length: ");
                    output.append(Integer.toString(os.size()));
                    output.append("\r\n");
                }
            } else if (os.isChunked()) {
                output.append("Transfer-Encoding: chunked\r\n");
            }
        }
        // end the headers
        output.append("\r\n");
        return output.toString().getBytes("ISO8859_1");
    }

    /**
     * Sets the default request header fields to be sent to the remote server.
     * This does not affect the current URL Connection, only newly created ones.
     *
     * @param field
     *            java.lang.String The name of the field to be changed
     * @param value
     *            java.lang.String The new value of the field
     */
    public static void setDefaultRequestProperty(String field, String value) {
        defaultReqHeader.add(field, value);
    }

    /**
     * A slightly different implementation from this parent's
     * <code>setIfModifiedSince()</code> Since this HTTP impl supports
     * IfModifiedSince as one of the header field, the request header is updated
     * with the new value.
     *
     *
     * @param newValue
     *            the number of millisecond since epoch
     *
     * @throws IllegalStateException
     *             if already connected.
     */
    @Override
    public void setIfModifiedSince(long newValue) {
        super.setIfModifiedSince(newValue);
        // convert from millisecond since epoch to date string
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = sdf.format(new Date(newValue));
        reqHeader.add("If-Modified-Since", date);
    }

    @Override
    public void setRequestProperty(String field, String newValue) {
        if (connected) {
            throw new IllegalStateException(Msg.getString("K0092"));
        }
        if (field == null) {
            throw new NullPointerException();
        }
        reqHeader.set(field, newValue);
    }

    @Override
    public void addRequestProperty(String field, String value) {
        if (connected) {
            throw new IllegalAccessError(Msg.getString("K0092"));
        }
        if (field == null) {
            throw new NullPointerException();
        }
        reqHeader.add(field, value);
    }

    /**
     * Get the connection port. This is either the URL's port or the proxy port
     * if a proxy port has been set.
     */
    private int getHostPort() {
        if (hostPort < 0) {
            // the value was not set yet
            if (proxy != null) {
                hostPort = ((InetSocketAddress) proxy.address()).getPort();
            } else {
                hostPort = url.getPort();
            }
            if (hostPort < 0) {
                hostPort = defaultPort;
            }
        }
        return hostPort;
    }

    /**
     * Get the InetAddress of the connection machine. This is either the address
     * given in the URL or the address of the proxy server.
     */
    private InetAddress getHostAddress() throws IOException {
        if (hostAddress == null) {
            // the value was not set yet
            if (proxy != null && proxy.type() != Proxy.Type.DIRECT) {
                hostAddress = ((InetSocketAddress) proxy.address())
                        .getAddress();
            } else {
                hostAddress = InetAddress.getByName(url.getHost());
            }
        }
        return hostAddress;
    }

    /**
     * Get the hostname of the connection machine. This is either the name given
     * in the URL or the name of the proxy server.
     */
    private String getHostName() {
        if (hostName == null) {
            // the value was not set yet
            if (proxy != null) {
                hostName = ((InetSocketAddress) proxy.address()).getHostName();
            } else {
                hostName = url.getHost();
            }
        }
        return hostName;
    }

    private String getSystemProperty(final String property) {
        return AccessController.doPrivileged(new PriviAction<String>(property));
    }

    @Override
    public boolean usingProxy() {
        return (proxy != null && proxy.type() != Proxy.Type.DIRECT);
    }

    /**
     * Handles an HTTP request along with its redirects and authentication
     */
    protected void doRequest() throws IOException {
        // do nothing if we've already sent the request
        if (sentRequest) {
            // If necessary, finish the request by
            // closing the uncached output stream.
            if (resHeader == null && os != null) {
                os.close();
                readServerResponse();
                getContentStream();
            }
            return;
        }
        doRequestInternal();
    }

    void doRequestInternal() throws IOException {
        int redirect = 0;
        while (true) {
            // send the request and process the results
            if (!sendRequest()) {
                return;
            }
            // proxy authorization failed ?
            if (responseCode == HTTP_PROXY_AUTH) {
                if (!usingProxy()) {
                    // KA017=Received HTTP_PROXY_AUTH (407) code while not using
                    // proxy
                    throw new IOException(Msg.getString("KA017"));
                }
                // username/password
                // until authorized
                String challenge = resHeader.get("Proxy-Authenticate");
                if (challenge == null) {
                    // KA016=Received authentication challenge is null.
                    throw new IOException(Msg.getString("KA016"));
                }
                // drop everything and reconnect, might not be required for
                // HTTP/1.1
                endRequest();
                disconnect();
                connected = false;
                String credentials = getAuthorizationCredentials(challenge);
                if (credentials == null) {
                    // could not find credentials, end request cycle
                    break;
                }
                // set up the authorization credentials
                setRequestProperty("Proxy-Authorization", credentials);
                // continue to send request
                continue;
            }
            // HTTP authorization failed ?
            if (responseCode == HTTP_UNAUTHORIZED) {
                // keep asking for username/password until authorized
                String challenge = resHeader.get("WWW-Authenticate");
                if (challenge == null) {
                    // KA018=Received authentication challenge is null
                    throw new IOException(Msg.getString("KA018"));
                }
                // drop everything and reconnect, might not be required for
                // HTTP/1.1
                endRequest();
                disconnect();
                connected = false;
                String credentials = getAuthorizationCredentials(challenge);
                if (credentials == null) {
                    // could not find credentials, end request cycle
                    break;
                }
                // set up the authorization credentials
                setRequestProperty("Authorization", credentials);
                // continue to send request
                continue;
            }
            /*
             * See if there is a server redirect to the URL, but only handle 1
             * level of URL redirection from the server to avoid being caught in
             * an infinite loop
             */
            if (getInstanceFollowRedirects()) {
                if ((responseCode == HTTP_MULT_CHOICE
                        || responseCode == HTTP_MOVED_PERM
                        || responseCode == HTTP_MOVED_TEMP
                        || responseCode == HTTP_SEE_OTHER || responseCode == HTTP_USE_PROXY)
                        && os == null) {

                    if (++redirect > 4) {
                        throw new ProtocolException(Msg.getString("K0093"));
                    }
                    String location = getHeaderField("Location");
                    if (location != null) {
                        // start over
                        if (responseCode == HTTP_USE_PROXY) {
                            int start = 0;
                            if (location.startsWith(url.getProtocol() + ':')) {
                                start = url.getProtocol().length() + 1;
                            }
                            if (location.startsWith("//", start)) {
                                start += 2;
                            }
                            setProxy(location.substring(start));
                        } else {
                            url = new URL(url, location);
                            hostName = url.getHost();
                            // update the port
                            hostPort = -1;
                        }
                        endRequest();
                        disconnect();
                        connected = false;
                        continue;
                    }
                }
            }
            break;
        }
        // Cache the content stream and read the first chunked header
        getContentStream();
    }

    /**
     * Returns the authorization credentials on the base of provided
     * authorization challenge
     *
     * @param challenge
     * @return authorization credentials
     * @throws IOException
     */
    private String getAuthorizationCredentials(String challenge)
            throws IOException {

        int idx = challenge.indexOf(" ");
        String scheme = challenge.substring(0, idx);
        int realm = challenge.indexOf("realm=\"") + 7;
        String prompt = null;
        if (realm != -1) {
            int end = challenge.indexOf('"', realm);
            if (end != -1) {
                prompt = challenge.substring(realm, end);
            }
        }
        // The following will use the user-defined authenticator to get
        // the password
        PasswordAuthentication pa = Authenticator
                .requestPasswordAuthentication(getHostAddress(), getHostPort(),
                        url.getProtocol(), prompt, scheme);
        if (pa == null) {
            // could not retrieve the credentials
            return null;
        }
        // base64 encode the username and password
        byte[] bytes = (pa.getUserName() + ":" + new String(pa.getPassword()))
                .getBytes("ISO8859_1");
        String encoded = Base64.encode(bytes, "ISO8859_1");
        return scheme + " " + encoded;
    }

    private void setProxy(String proxy) {
        int index = proxy.indexOf(':');
        if (index == -1) {
            proxyName = proxy;
            hostPort = defaultPort;
        } else {
            proxyName = proxy.substring(0, index);
            String port = proxy.substring(index + 1);
            try {
                hostPort = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(Msg.getString("K00af", port));
            }
            if (hostPort < 0 || hostPort > 65535) {
                throw new IllegalArgumentException(Msg.getString("K00b0"));
            }
        }
    }
}
