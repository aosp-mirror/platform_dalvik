/*
 * Copyright (C) 2010 The Android Open Source Project
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

package java.util.zip;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeflaterOutputStreamTest extends TestCase {

    public void testSyncFlushEnabled() throws Exception {
        InflaterInputStream in = createInflaterStream(true);
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(3, in.read());
    }

    public void testSyncFlushDisabled() throws Exception {
        InflaterInputStream in = createInflaterStream(false);
        try {
            in.read();
            fail();
        } catch (IOException expected) {
        }
    }

    /**
     * Creates an optionally-flushing deflater stream, writes some bytes to it,
     * and flushes it. Returns an inflater stream that reads this deflater's
     * output.
     *
     * <p>These bytes are written on a separate thread so that when the inflater
     * stream is read, that read will fail when no bytes are available. Failing
     * takes 3 seconds, co-ordinated by PipedInputStream's 'broken pipe'
     * timeout. The 3 second delay is unfortunate but seems to be the easiest
     * way demonstrate that data is unavailable. Ie. other techniques will cause
     * the dry read to block indefinitely.
     */
    private InflaterInputStream createInflaterStream(final boolean flushing)
            throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream pout = new PipedOutputStream();
        PipedInputStream pin = new PipedInputStream(pout);

        executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                OutputStream out = new DeflaterOutputStream(pout, flushing);
                out.write(1);
                out.write(2);
                out.write(3);
                out.flush();
                return null;
            }
        }).get();
        executor.shutdown();

        return new InflaterInputStream(pin);
    }
}
