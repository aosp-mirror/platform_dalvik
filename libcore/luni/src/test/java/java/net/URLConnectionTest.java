/*
 * Copyright (C) 2009 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

import java.net.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import tests.support.Support_PortManager;
import tests.support.Support_TestWebServer;

public class URLConnectionTest extends junit.framework.TestCase {
    private int mPort;
    private Support_TestWebServer mServer;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mPort = Support_PortManager.getNextPort();
        mServer = new Support_TestWebServer();
        mServer.initServer(mPort, true);
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mServer.close();
    }
    
    private String readFirstLine() throws Exception {
        URLConnection connection = new URL("http://localhost:" + mPort + "/test1").openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result = in.readLine();
        in.close();
        return result;
    }
    
    // Check that if we don't read to the end of a response, the next request on the
    // recycled connection doesn't get the unread tail of the first request's response.
    // http://code.google.com/p/android/issues/detail?id=2939
    public void test_2939() throws Exception {
        mServer.setChunked(true);
        mServer.setMaxChunkSize(8);
        assertTrue(readFirstLine().equals("<html>"));
        assertTrue(readFirstLine().equals("<html>"));
    }

    enum UploadKind { CHUNKED, FIXED_LENGTH };
    enum WriteKind { BYTE_BY_BYTE, SMALL_BUFFERS, LARGE_BUFFERS };

    public void test_chunkedUpload_byteByByte() throws Exception {
        doUpload(UploadKind.CHUNKED, WriteKind.BYTE_BY_BYTE);
    }

    public void test_chunkedUpload_smallBuffers() throws Exception {
        doUpload(UploadKind.CHUNKED, WriteKind.SMALL_BUFFERS);
    }

    public void test_chunkedUpload_largeBuffers() throws Exception {
        doUpload(UploadKind.CHUNKED, WriteKind.LARGE_BUFFERS);
    }

    public void test_fixedLengthUpload_byteByByte() throws Exception {
        doUpload(UploadKind.FIXED_LENGTH, WriteKind.BYTE_BY_BYTE);
    }

    public void test_fixedLengthUpload_smallBuffers() throws Exception {
        doUpload(UploadKind.FIXED_LENGTH, WriteKind.SMALL_BUFFERS);
    }

    public void test_fixedLengthUpload_largeBuffers() throws Exception {
        doUpload(UploadKind.FIXED_LENGTH, WriteKind.LARGE_BUFFERS);
    }

    private void doUpload(UploadKind uploadKind, WriteKind writeKind) throws Exception {
        int n = 512*1024;
        AtomicInteger total = new AtomicInteger(0);
        ServerSocket ss = startSinkServer(total);
        URL url = new URL("http://localhost:" + ss.getLocalPort() + "/test1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        if (uploadKind == UploadKind.CHUNKED) {
            conn.setChunkedStreamingMode(-1);
        } else {
            conn.setFixedLengthStreamingMode(n);
        }
        OutputStream out = conn.getOutputStream();
        if (writeKind == WriteKind.BYTE_BY_BYTE) {
            for (int i = 0; i < n; ++i) {
                out.write('x');
            }
        } else {
            byte[] buf = new byte[writeKind == WriteKind.SMALL_BUFFERS ? 256 : 64*1024];
            Arrays.fill(buf, (byte) 'x');
            for (int i = 0; i < n; i += buf.length) {
                out.write(buf, 0, Math.min(buf.length, n - i));
            }
        }
        out.close();
        assertTrue(conn.getResponseCode() > 0);
        assertEquals(uploadKind == UploadKind.CHUNKED ? -1 : n, total.get());
    }

    private ServerSocket startSinkServer(final AtomicInteger totalByteCount) throws Exception {
        final ServerSocket ss = new ServerSocket(0);
        ss.setReuseAddress(true);
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket s = ss.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    int contentLength = -1;
                    String line;
                    int emptyLineCount = 0;
                    // read the headers
                    while ((line = in.readLine()) != null) {
                        if (contentLength == -1 && line.toLowerCase().startsWith("content-length: ")) {
                            contentLength = Integer.parseInt(line.substring(16));
                        }
                        if (line.length() == 0) {
                            ++emptyLineCount;
                            // If we had a content length, the first empty line we see marks the
                            // start of the payload. The loop below then skips over that.
                            // If we didn't get a content length, we're using chunked encoding.
                            // The first empty line again marks the start of the payload, and the
                            // second empty line is a consequence of both the last chunk ending
                            // CRLF and the chunked-body itself ending with a CRLF. (The fact that
                            // a chunk of size 0 is used to mark the end isn't sufficient because
                            // there may also be a "trailer": header fields deferred until after
                            // the payload.)
                            if (contentLength != -1 || emptyLineCount == 2) {
                                break;
                            }
                        }
                    }
                    // Skip the payload in the setFixedLengthStreamingMode case.
                    // In the chunked case, we read all the chunked data in the loop above.
                    long left = contentLength;
                    while (left > 0) {
                        left -= in.skip(left);
                    }
                    // Send a response to unblock the client.
                    totalByteCount.set(contentLength);
                    OutputStream out = s.getOutputStream();
                    out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                    out.flush();
                    out.close();
                    // Check there wasn't junk at the end.
                    try {
                        assertEquals(-1, in.read());
                    } catch (SocketException expected) {
                        // The client already closed the connection.
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("server died unexpectedly", ex);
                }
            }
        });
        t.start();
        return ss;
    }
}
