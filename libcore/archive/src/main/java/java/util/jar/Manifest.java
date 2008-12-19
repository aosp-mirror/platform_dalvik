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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.harmony.luni.util.PriviAction;

/**
 * The {@code Manifest} class is used to obtain attribute information for a
 * {@code JarFile} and its entries.
 * 
 * @since Android 1.0
 */
public class Manifest implements Cloneable {
    private static final int LINE_LENGTH_LIMIT = 70;

    private static final byte[] LINE_SEPARATOR = new byte[] { '\r', '\n' };

    private static final Attributes.Name NAME_ATTRIBUTE = new Attributes.Name("Name"); //$NON-NLS-1$

    private Attributes mainAttributes = new Attributes();

    private HashMap<String, Attributes> entryAttributes = new HashMap<String, Attributes>();

    private HashMap<String, byte[]> chunks;

    /**
     * The data chunk of main attributes in the manifest is needed in
     * verification.
     */
    private byte[] mainAttributesChunk;

    /**
     * Creates a new {@code Manifest} instance.
     * 
     * @since Android 1.0
     */
    public Manifest() {
        super();
    }

    /**
     * Creates a new {@code Manifest} instance using the attributes obtained
     * from the input stream.
     * 
     * @param is
     *            {@code InputStream} to parse for attributes.
     * @throws IOException
     *             if an IO error occurs while creating this {@code Manifest}
     * @since Android 1.0
     */
    public Manifest(InputStream is) throws IOException {
        super();
        read(is);
    }

    /**
     * Creates a new {@code Manifest} instance. The new instance will have the
     * same attributes as those found in the parameter {@code Manifest}.
     * 
     * @param man
     *            {@code Manifest} instance to obtain attributes from.
     * @since Android 1.0
     */
    @SuppressWarnings("unchecked")
    public Manifest(Manifest man) {
        mainAttributes = (Attributes) man.mainAttributes.clone();
        entryAttributes = (HashMap<String, Attributes>) man.entryAttributes.clone();
    }

    Manifest(InputStream is, boolean readChunks) throws IOException {
        if (readChunks) {
            chunks = new HashMap<String, byte[]>();
        }
        read(is);
    }

    /**
     * Resets the both the main attributes as well as the entry attributes
     * associated with this {@code Manifest}.
     * 
     * @since Android 1.0
     */
    public void clear() {
        entryAttributes.clear();
        mainAttributes.clear();
    }

    /**
     * Returns the {@code Attributes} associated with the parameter entry
     * {@code name}.
     * 
     * @param name
     *            the name of the entry to obtain {@code Attributes} from.
     * @return the Attributes for the entry or {@code null} if the entry does
     *         not exist.
     * @since Android 1.0
     */
    public Attributes getAttributes(String name) {
        return getEntries().get(name);
    }

    /**
     * Returns a map containing the {@code Attributes} for each entry in the
     * {@code Manifest}.
     * 
     * @return the map of entry attributes.
     * @since Android 1.0
     */
    public Map<String, Attributes> getEntries() {
        return entryAttributes;
    }

    /**
     * Returns the main {@code Attributes} of the {@code JarFile}.
     * 
     * @return main {@code Attributes} associated with the source {@code
     *         JarFile}.
     * @since Android 1.0
     */
    public Attributes getMainAttributes() {
        return mainAttributes;
    }

    /**
     * Creates a copy of this {@code Manifest}. The returned {@code Manifest}
     * will equal the {@code Manifest} from which it was cloned.
     * 
     * @return a copy of this instance.
     * @since Android 1.0
     */
    @Override
    public Object clone() {
        return new Manifest(this);
    }

    /**
     * Writes out the attribute information of the receiver to the specified
     * {@code OutputStream}.
     * 
     * @param os
     *            The {@code OutputStream} to write to.
     * @throws IOException
     *             If an error occurs writing the {@code Manifest}.
     * @since Android 1.0
     */
    public void write(OutputStream os) throws IOException {
        write(this, os);
    }

    /**
     * Constructs a new {@code Manifest} instance obtaining attribute
     * information from the specified input stream.
     * 
     * @param is
     *            The {@code InputStream} to read from.
     * @throws IOException
     *             If an error occurs reading the {@code Manifest}.
     * @since Android 1.0
     */
    public void read(InputStream is) throws IOException {
        InitManifest initManifest = new InitManifest(is, mainAttributes, entryAttributes,
                chunks, null);
        mainAttributesChunk = initManifest.getMainAttributesChunk();
    }

    /**
     * Returns the hash code for this instance.
     * 
     * @return this {@code Manifest}'s hashCode.
     * @since Android 1.0
     */
    @Override
    public int hashCode() {
        return mainAttributes.hashCode() ^ entryAttributes.hashCode();
    }

    /**
     * Determines if the receiver is equal to the parameter object. Two {@code
     * Manifest}s are equal if they have identical main attributes as well as
     * identical entry attributes.
     * 
     * @param o
     *            the object to compare against.
     * @return {@code true} if the manifests are equal, {@code false} otherwise
     * @since Android 1.0
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        if (!mainAttributes.equals(((Manifest) o).mainAttributes)) {
            return false;
        }
        return entryAttributes.equals(((Manifest) o).entryAttributes);
    }

    byte[] getChunk(String name) {
        return chunks.get(name);
    }

    void removeChunks() {
        chunks = null;
    }

    byte[] getMainAttributesChunk() {
        return mainAttributesChunk;
    }

    /**
     * Writes out the attribute information of the specified manifest to the
     * specified {@code OutputStream}
     * 
     * @param manifest
     *            the manifest to write out.
     * @param out
     *            The {@code OutputStream} to write to.
     * @throws IOException
     *             If an error occurs writing the {@code Manifest}.
     * @since Android 1.0
     */
    static void write(Manifest manifest, OutputStream out) throws IOException {
        Charset charset = null;
        String encoding = AccessController.doPrivileged(new PriviAction<String>(
                "manifest.write.encoding")); //$NON-NLS-1$
        if (encoding != null) {
            if (encoding.length() == 0) {
                encoding = "UTF8"; //$NON-NLS-1$
            }
            charset = Charset.forName(encoding);
        }
        String version = manifest.mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION);
        if (version != null) {
            writeEntry(out, charset, Attributes.Name.MANIFEST_VERSION, version);
            Iterator<?> entries = manifest.mainAttributes.keySet().iterator();
            while (entries.hasNext()) {
                Attributes.Name name = (Attributes.Name) entries.next();
                if (!name.equals(Attributes.Name.MANIFEST_VERSION)) {
                    writeEntry(out, charset, name, manifest.mainAttributes.getValue(name));
                }
            }
        }
        out.write(LINE_SEPARATOR);
        Iterator<String> i = manifest.entryAttributes.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            writeEntry(out, charset, NAME_ATTRIBUTE, key);
            Attributes attrib = manifest.entryAttributes.get(key);
            Iterator<?> entries = attrib.keySet().iterator();
            while (entries.hasNext()) {
                Attributes.Name name = (Attributes.Name) entries.next();
                writeEntry(out, charset, name, attrib.getValue(name));
            }
            out.write(LINE_SEPARATOR);
        }
    }

    private static void writeEntry(OutputStream os, Charset charset, Attributes.Name name,
            String value) throws IOException {
        int offset = 0;
        int limit = LINE_LENGTH_LIMIT;
        byte[] out = (name.toString() + ": ").getBytes("ISO8859_1"); //$NON-NLS-1$ //$NON-NLS-2$
        if (out.length > limit) {
            while (out.length - offset >= limit) {
                int len = out.length - offset;
                if (len > limit) {
                    len = limit;
                }
                if (offset > 0) {
                    os.write(' ');
                }
                os.write(out, offset, len);
                os.write(LINE_SEPARATOR);
                offset += len;
                limit = LINE_LENGTH_LIMIT - 1;
            }
        }
        int size = out.length - offset;
        final byte[] outBuf = new byte[LINE_LENGTH_LIMIT];
        System.arraycopy(out, offset, outBuf, 0, size);
        for (int i = 0; i < value.length(); i++) {
            char[] oneChar = new char[1];
            oneChar[0] = value.charAt(i);
            byte[] buf;
            if (oneChar[0] < 128 || charset == null) {
                byte[] oneByte = new byte[1];
                oneByte[0] = (byte) oneChar[0];
                buf = oneByte;
            } else {
                buf = charset.encode(CharBuffer.wrap(oneChar, 0, 1)).array();
            }
            if (size + buf.length > limit) {
                if (limit != LINE_LENGTH_LIMIT) {
                    os.write(' ');
                }
                os.write(outBuf, 0, size);
                os.write(LINE_SEPARATOR);
                limit = LINE_LENGTH_LIMIT - 1;
                size = 0;
            }
            if (buf.length == 1) {
                outBuf[size] = buf[0];
            } else {
                System.arraycopy(buf, 0, outBuf, size, buf.length);
            }
            size += buf.length;
        }
        if (size > 0) {
            if (limit != LINE_LENGTH_LIMIT) {
                os.write(' ');
            }
            os.write(outBuf, 0, size);
            os.write(LINE_SEPARATOR);
        }
    }
}
