/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * This class provides random read access to a <i>ZIP-archive</i> file.
 * <p>
 * While {@code ZipInputStream} provides stream based read access to a
 * <i>ZIP-archive</i>, this class implements more efficient (file based) access
 * and makes use of the <i>central directory</i> within a <i>ZIP-archive</i>.
 * </p>
 * <p>
 * Use {@code ZipOutputStream} if you want to create an archive.
 * </p>
 * <p>
 * A temporary ZIP file can be marked for automatic deletion upon closing it.
 * </p>
 * 
 * @see ZipEntry
 * @see ZipOutputStream
 * @since Android 1.0
 */
public class ZipFile implements ZipConstants {

    String fileName;

    File fileToDeleteOnClose;

    /**
     * Open zip file for read.
     * 
     * @since Android 1.0
     */
    public static final int OPEN_READ = 1;

    /**
     * Delete zip file when closed.
     * 
     * @since Android 1.0
     */
    public static final int OPEN_DELETE = 4;

    /**
     * Constructs a new {@code ZipFile} with the specified file.
     * 
     * @param file
     *            the file to read from.
     * @throws ZipException
     *             if a ZIP error occurs.
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

    /**
     * Opens a file as <i>ZIP-archive</i>. "mode" must be {@code OPEN_READ} or
     * {@code OPEN_DELETE} . The latter sets the "delete on exit" flag through a
     * file.
     * 
     * @param file
     *            the ZIP file to read.
     * @param mode
     *            the mode of the file open operation.
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    public ZipFile(File file, int mode) throws IOException {
        if (mode == (OPEN_READ | OPEN_DELETE))
            fileToDeleteOnClose = file; // file.deleteOnExit();
        else if (mode != OPEN_READ)
            throw new IllegalArgumentException("invalid mode");

        fileName = file.getPath();
        mRaf = new RandomAccessFile(fileName, "r");

        mEntryList = new ArrayList<ZipEntry>();

        readCentralDir();

        /*
         * No LinkedHashMap yet, so optimize lookup-by-name by creating
         * a parallel data structure.
         */
        mFastLookup = new HashMap<String, ZipEntry>(mEntryList.size() * 2);
        for (int i = 0; i < mEntryList.size(); i++) {
            ZipEntry entry = mEntryList.get(i);

            mFastLookup.put(entry.getName(), entry);
        }
    }

    /**
     * Opens a ZIP archived file.
     * 
     * @param name
     *            the name of the ZIP file.
     * @throws IOException
     *             if an IOException occurs.
     * @since Android 1.0
     */
    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }

    @Override
    protected void finalize() throws IOException {
        close();
    }

    /**
     * Closes this ZIP file.
     * 
     * @throws IOException
     *             if an IOException occurs.
     * @since Android 1.0
     */
    public void close() throws IOException {
        RandomAccessFile raf = mRaf;

        if (raf != null) { // Only close initialized instances
            synchronized(raf) {
                mRaf = null;
                raf.close();
            }
            if (fileToDeleteOnClose != null) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        new File(fileName).delete();
                        return null;
                    }
                });
                // fileToDeleteOnClose.delete();
                fileToDeleteOnClose = null;
            }
        }
    }

    /**
     * Returns an enumeration of the entries. The entries are listed in the
     * order in which they appear in the ZIP archive.
     * 
     * @return the enumeration of the entries.
     * @since Android 1.0
     */
    public Enumeration<? extends ZipEntry> entries() {
        return new Enumeration<ZipEntry>() {
            private int i = 0;

            public boolean hasMoreElements() {
                if (mRaf == null) throw new IllegalStateException("Zip File closed.");
                return i < mEntryList.size();
            }

            public ZipEntry nextElement() {
                if (mRaf == null) throw new IllegalStateException("Zip File closed.");
                if (i >= mEntryList.size())
                    throw new NoSuchElementException();
                return (ZipEntry) mEntryList.get(i++);
            }
        };
    }

    /**
     * Gets the ZIP entry with the specified name from this {@code ZipFile}.
     * 
     * @param entryName
     *            the name of the entry in the ZIP file.
     * @return a {@code ZipEntry} or {@code null} if the entry name does not
     *         exist in the ZIP file.
     * @since Android 1.0
     */
    public ZipEntry getEntry(String entryName) {
        if (entryName != null) {
            ZipEntry ze = mFastLookup.get(entryName);
            if (ze == null) ze = mFastLookup.get(entryName + "/");
            return ze;
        }
        throw new NullPointerException();
    }

    /**
     * Returns an input stream on the data of the specified {@code ZipEntry}.
     * 
     * @param entry
     *            the ZipEntry.
     * @return an input stream of the data contained in the {@code ZipEntry}.
     * @throws IOException
     *             if an {@code IOException} occurs.
     * @since Android 1.0
     */
    public InputStream getInputStream(ZipEntry entry) throws IOException {
        /*
         * Make sure this ZipEntry is in this Zip file.  We run it through
         * the name lookup.
         */
        entry = getEntry(entry.getName());
        if (entry == null)
            return null;

        /*
         * Create a ZipInputStream at the right part of the file.
         */
        RandomAccessFile raf = mRaf;
        if (raf != null) {
            synchronized (raf) {
                // Unfortunately we don't know the entry data's start position.
                // All we have is the position of the entry's local header.
                // At position 28 we find the length of the extra data.
                // In some cases this length differs from the one coming in
                // the central header!!!
                RAFStream rafstrm = new RAFStream(raf, entry.mLocalHeaderRelOffset + 28);
                int localExtraLenOrWhatever = ler.readShortLE(rafstrm);
                // Now we need to skip the name
                // and this "extra" data or whatever it is:
                rafstrm.skip(entry.nameLen + localExtraLenOrWhatever);
                rafstrm.mLength = rafstrm.mOffset + entry.compressedSize;
                if (entry.compressionMethod == ZipEntry.DEFLATED) {
                    return new InflaterInputStream(rafstrm, new Inflater(true));
                } else {
                    return rafstrm;
                }
            }
        }
        throw new IllegalStateException("Zip File closed");
    }

    /**
     * Gets the file name of this {@code ZipFile}.
     * 
     * @return the file name of this {@code ZipFile}.
     * @since Android 1.0
     */
    public String getName() {
        return fileName;
    }

    /**
     * Returns the number of {@code ZipEntries} in this {@code ZipFile}.
     * 
     * @return the number of entries in this file.
     * @since Android 1.0
     */
    public int size() {
        return mEntryList.size();
    }

    /*
     * Find the central directory and read the contents.
     *
     * The central directory can be followed by a variable-length comment
     * field, so we have to scan through it backwards.  The comment is at
     * most 64K, plus we have 18 bytes for the end-of-central-dir stuff
     * itself, plus apparently sometimes people throw random junk on the end
     * just for the fun of it.
     *  
     * This is all a little wobbly.  If the wrong value ends up in the EOCD
     * area, we're hosed.  This appears to be the way that everbody handles
     * it though, so we're in pretty good company if this fails.
     */
    private void readCentralDir() throws IOException {
        long scanOffset, stopOffset;
        long sig;

        /*
         * Scan back, looking for the End Of Central Directory field.  If
         * the archive doesn't have a comment, we'll hit it on the first
         * try.
         *
         * No need to synchronize mRaf here -- we only do this when we
         * first open the Zip file.
         */
        scanOffset = mRaf.length() - ENDHDR;
        if (scanOffset < 0)
            throw new ZipException("too short to be Zip");

        stopOffset = scanOffset - 65536;
        if (stopOffset < 0)
            stopOffset = 0;

        while (true) {
            mRaf.seek(scanOffset);
            if (ZipEntry.readIntLE(mRaf) == 101010256L)
                break;

            //System.out.println("not found at " + scanOffset);
            scanOffset--;
            if (scanOffset < stopOffset)
                throw new ZipException("EOCD not found; not a Zip archive?");
        }

        /*
         * Found it, read the EOCD.
         *
         * For performance we want to use buffered I/O when reading the
         * file.  We wrap a buffered stream around the random-access file
         * object.  If we just read from the RandomAccessFile we'll be
         * doing a read() system call every time.
         */
        RAFStream rafs = new RAFStream(mRaf, mRaf.getFilePointer());
        BufferedInputStream bin = new BufferedInputStream(rafs, ENDHDR);
        int diskNumber, diskWithCentralDir, numEntries, totalNumEntries;
        //long centralDirSize;
        long centralDirOffset;
        //int commentLen;

        diskNumber = ler.readShortLE(bin);
        diskWithCentralDir = ler.readShortLE(bin);
        numEntries = ler.readShortLE(bin);
        totalNumEntries = ler.readShortLE(bin);
        /*centralDirSize =*/ ler.readIntLE(bin);
        centralDirOffset = ler.readIntLE(bin);
        /*commentLen =*/ ler.readShortLE(bin);

        if (numEntries != totalNumEntries ||
            diskNumber != 0 ||
            diskWithCentralDir != 0)
            throw new ZipException("spanned archives not supported");

        /*
         * Seek to the first CDE and read all entries.
         */
        rafs = new RAFStream(mRaf, centralDirOffset);
        bin = new BufferedInputStream(rafs, 4096);
        for (int i = 0; i < numEntries; i++) {
            ZipEntry newEntry;

            newEntry = new ZipEntry(ler, bin);
            mEntryList.add(newEntry);
        }
    }

    /*
     * Local data items.
     */
    private RandomAccessFile mRaf;

    ZipEntry.LittleEndianReader ler = new ZipEntry.LittleEndianReader();

    /*
     * What we really want here is a LinkedHashMap, because we want fast
     * lookups by name, but we want to preserve the ordering of the archive
     * entries.  Unfortunately we don't yet have a LinkedHashMap
     * implementation.
     */
    private ArrayList<ZipEntry> mEntryList;
    private HashMap<String, ZipEntry> mFastLookup;

    /*
     * Wrap a stream around a RandomAccessFile.  The RandomAccessFile
     * is shared among all streams returned by getInputStream(), so we
     * have to synchronize access to it.  (We can optimize this by
     * adding buffering here to reduce collisions.)
     *
     * We could support mark/reset, but we don't currently need them.
     */
    static class RAFStream extends InputStream {
        public RAFStream(RandomAccessFile raf, long pos) throws IOException {
            mSharedRaf = raf;
            mOffset = pos;
            mLength = raf.length();
        }

        @Override
        public int available() throws IOException {
            return (mOffset < mLength ? 1 : 0);
        }

        public int read() throws IOException {
            if (read(singleByteBuf, 0, 1) == 1) return singleByteBuf[0] & 0XFF;
            else return -1;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int count;
            synchronized (mSharedRaf) {
                mSharedRaf.seek(mOffset);
                if (mOffset + len > mLength) len = (int) (mLength - mOffset);
                count = mSharedRaf.read(b, off, len);
                if (count > 0) {
                    mOffset += count;
                }
                else return -1;
            }
            return count;
        }

        @Override
        public long skip(long n) throws IOException {
            if (mOffset + n > mLength)
                n = mLength - mOffset;
            mOffset += n;
            return n;
        }

        RandomAccessFile mSharedRaf;
        long mOffset;
        long mLength;
        private byte[] singleByteBuf = new byte[1];
    }
}
