/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.harmony.archive.internal.nls.Messages;
import org.apache.harmony.luni.util.PriviAction;
import org.apache.harmony.luni.util.Util;

class InitManifest {
    private final byte[] inbuf = new byte[1024];

    private int inbufCount = 0, inbufPos = 0;

    private byte[] buffer = new byte[5];

    private char[] charbuf = new char[0];

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    private String encoding;

    private boolean usingUTF8 = true;

    private final Map<String, Attributes.Name> attributeNames = new HashMap<String, Attributes.Name>();

    private final byte[] mainAttributesChunk;

    InitManifest(InputStream is, Attributes main, Map<String, Attributes> entries, Map<String, byte[]> chunks,
            String verString) throws IOException {
        encoding = AccessController.doPrivileged(new PriviAction<String>(
                "manifest.read.encoding")); //$NON-NLS-1$
        if ("".equals(encoding)) { //$NON-NLS-1$
            encoding = null;
        }

        Attributes current = main;
        ArrayList<String> list = new ArrayList<String>();

        // Return the chunk of main attributes in the manifest.
        mainAttributesChunk = nextChunk(is, list);

        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            addAttribute(it.next(), current);
        }

        // Check for version attribute
        if (verString != null && main.getValue(verString) == null) {
            throw new IOException(Messages.getString("archive.2D", verString)); //$NON-NLS-1$
        }

        list.clear();
        byte[] chunk = null;
        while (chunks == null ? readLines(is, list) : (chunk = nextChunk(is,
                list)) != null) {
            it = list.iterator();
            String line = it.next();
            if (line.length() < 7
                    || !Util.toASCIILowerCase(line.substring(0, 5)).equals("name:")) { //$NON-NLS-1$
                throw new IOException(Messages.getString("archive.23")); //$NON-NLS-1$
            }
            // Name: length required space char
            String name = line.substring(6, line.length());
            current = new Attributes(12);
            if (chunks != null) {
                chunks.put(name, chunk);
            }
            entries.put(name, current);
            while (it.hasNext()) {
                addAttribute(it.next(), current);
            }
            list.clear();
        }

    }

    byte[] getMainAttributesChunk() {
        return mainAttributesChunk;
    }

    private void addLine(int length, List<String> lines) throws IOException {
        if (encoding != null) {
            lines.add(new String(buffer, 0, length, encoding));
        } else {
            if (usingUTF8) {
                try {
                    if (charbuf.length < length) {
                        charbuf = new char[length];
                    }
                    lines.add(Util.convertUTF8WithBuf(buffer, charbuf, 0,
                            length));
                } catch (UTFDataFormatException e) {
                    usingUTF8 = false;
                }
            }
            if (!usingUTF8) {
                if (charbuf.length < length) {
                    charbuf = new char[length];
                }
                // If invalid UTF8, convert bytes to chars setting the upper
                // bytes to zeros
                int charOffset = 0;
                int offset = 0;
                for (int i = length; --i >= 0;) {
                    charbuf[charOffset++] = (char) (buffer[offset++] & 0xff);
                }
                lines.add(new String(charbuf, 0, length));
            }
        }
    }

    private byte[] nextChunk(InputStream in, List<String> lines)
            throws IOException {
        if (inbufCount == -1) {
            return null;
        }
        byte next;
        int pos = 0;
        boolean blankline = false, lastCr = false;
        out.reset();
        while (true) {
            if (inbufPos == inbufCount) {
                if ((inbufCount = in.read(inbuf)) == -1) {
                    if (out.size() == 0) {
                        return null;
                    }
                    if (blankline) {
                        addLine(pos, lines);
                    }
                    return out.toByteArray();
                }
                if (inbufCount == inbuf.length && in.available() == 0) {
                    /* archive.2E = "line too long" */
                    throw new IOException(Messages.getString("archive.2E")); //$NON-NLS-1$
                }
                inbufPos = 0;
            }
            next = inbuf[inbufPos++];
            if (lastCr) {
                if (next != '\n') {
                    inbufPos--;
                    next = '\r';
                } else {
                    if (out.size() == 0) {
                        continue;
                    }
                    out.write('\r');
                }
                lastCr = false;
            } else if (next == '\r') {
                lastCr = true;
                continue;
            }
            if (blankline) {
                if (next == ' ') {
                    out.write(next);
                    blankline = false;
                    continue;
                }
                addLine(pos, lines);
                if (next == '\n') {
                    out.write(next);
                    return out.toByteArray();
                }
                pos = 0;
            } else if (next == '\n') {
                if (out.size() == 0) {
                    continue;
                }
                out.write(next);
                blankline = true;
                continue;
            }
            blankline = false;
            out.write(next);
            if (pos == buffer.length) {
                byte[] newBuf = new byte[buffer.length * 2];
                System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
                buffer = newBuf;
            }
            buffer[pos++] = next;
        }
    }

    private boolean readLines(InputStream in, List<String> lines)
            throws IOException {
        if (inbufCount == -1) {
            return false;
        }
        byte next;
        int pos = 0;
        boolean blankline = false, lastCr = false;
        while (true) {
            if (inbufPos == inbufCount) {
                if ((inbufCount = in.read(inbuf)) == -1) {
                    if (blankline) {
                        addLine(pos, lines);
                    }
                    return lines.size() != 0;
                }
                if (inbufCount == inbuf.length && in.available() == 0) {
                    /* archive.2E = "line too long" */
                    throw new IOException(Messages.getString("archive.2E")); //$NON-NLS-1$
                }
                inbufPos = 0;
            }
            next = inbuf[inbufPos++];
            if (lastCr) {
                if (next != '\n') {
                    inbufPos--;
                    next = '\r';
                }
                lastCr = false;
            } else if (next == '\r') {
                lastCr = true;
                continue;
            }
            if (blankline) {
                if (next == ' ') {
                    blankline = false;
                    continue;
                }
                addLine(pos, lines);
                if (next == '\n') {
                    return true;
                }
                pos = 0;
            } else if (next == '\n') {
                if (pos == 0 && lines.size() == 0) {
                    continue;
                }
                blankline = true;
                continue;
            }
            blankline = false;
            if (pos == buffer.length) {
                byte[] newBuf = new byte[buffer.length * 2];
                System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
                buffer = newBuf;
            }
            buffer[pos++] = next;
        }
    }

    /* Get the next attribute and add it */
    private void addAttribute(String line, Attributes current)
            throws IOException {
        String header;
        int hdrIdx = line.indexOf(':');
        if (hdrIdx < 1) {
            throw new IOException(Messages.getString("archive.2F", line)); //$NON-NLS-1$
        }
        header = line.substring(0, hdrIdx);
        Attributes.Name name = attributeNames.get(header);
        if (name == null) {
            try {
                name = new Attributes.Name(header);
            } catch (IllegalArgumentException e) {
                throw new IOException(e.toString());
            }
            attributeNames.put(header, name);
        }
        if (hdrIdx + 1 >= line.length() || line.charAt(hdrIdx + 1) != ' ') {
            throw new IOException(Messages.getString("archive.2F", line)); //$NON-NLS-1$
        }
        // +2 due to required SPACE char
        current.put(name, line.substring(hdrIdx + 2, line.length()));
    }
}
