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

package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

import org.apache.harmony.sound.internal.nls.Messages;

public class AudioInputStream extends InputStream {

    protected AudioFormat format;

    protected long frameLength;

    protected long framePos;

    protected int frameSize;

    private InputStream stream;

    private TargetDataLine line;

    private byte[] oneByte = new byte[1];

    private long marketFramePos;

    public AudioInputStream(InputStream stream, AudioFormat format, long length) {
        this.stream = stream;
        this.format = format;
        this.frameLength = length;
        this.frameSize = format.getFrameSize();
    }

    public AudioInputStream(TargetDataLine line) {
        this.line = line;
        this.format = line.getFormat();
        this.frameLength = AudioSystem.NOT_SPECIFIED; //TODO
        this.frameSize = this.format.getFrameSize();
    }

    public AudioFormat getFormat() {
        return format;
    }

    public long getFrameLength() {
        return frameLength;
    }

    public int read() throws IOException {
        if (frameSize != 1) {
            // sound.0C=Frame size must be one byte
            throw new IOException(Messages.getString("sound.0C")); //$NON-NLS-1$
        }
        int res;
        if (stream != null) { // InputStream
            if (framePos == frameLength) {
                return 0;
            }
            res = stream.read();
            if (res == -1) {
                return -1;
            }
            framePos += 1;
            return res;
        } else { // TargetDataLine
            if (line.read(oneByte, 0, 1) == 0) {
                return -1;
            }
            framePos = line.getLongFramePosition();
            return oneByte[0];
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int l = Math.min(len, (int) ((frameLength - framePos) * frameSize));
        l = l - (l % frameSize);
        if (l == 0) {
            return 0;
        }
        int res;
        if (stream != null) { // InputStream
            res = stream.read(b, off, l);
            if (res == -1) {
                return -1;
            }
            framePos = framePos + res / frameSize;
            return res;
        } else { // TargetDataLine
            res = line.read(b, off, l);
            if (res == 0) {
                return -1;
            }
            framePos = line.getLongFramePosition();
            return res;
        }

    }

    public long skip(long n) throws IOException {

        if (n < frameSize) {
            return 0;
        }
        byte[] skipBuf = new byte[frameSize];
        long skipped = 0;
        while (skipped < n) {
            int read = read(skipBuf, 0, frameSize);
            if (read == -1) {
                return skipped;
            }
            skipped += read;
            if (n - skipped < frameSize) {
                return skipped;
            }
        }
        return skipped;

    }

    public int available() throws IOException {
        if (stream != null) { // InputStream
            return Math.min(stream.available(),
                    (int)((frameLength - framePos) * frameSize));
        } else { // TargetDataLine
            return line.available();
        }
    }

    public void close() throws IOException {
        if (stream != null) { // InputStream
            stream.close();
        } else { // TargetDataLine
            line.close();
        }
    }

    public void mark(int readlimit) {
        if (stream != null) { //InputStream
            stream.mark(readlimit);
            marketFramePos = framePos;
        } else { // TargetDataLine
            // do nothing
        }
    }

    public void reset() throws IOException {
        if (stream != null) { //InputStream
            stream.reset();
            framePos = marketFramePos;
        } else { // TargetDataLine
            // do nothing
        }
    }

    public boolean markSupported() {
        if (stream != null) { //InputStream
            return stream.markSupported();
        } else { // TargetDataLine
            return false;
        }
    }

}
