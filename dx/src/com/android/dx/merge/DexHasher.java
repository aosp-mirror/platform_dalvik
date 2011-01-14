/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dx.merge;

import com.android.dx.dex.SizeOf;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

/**
 * Generates and stores the checksum and signature of a dex file.
 */
public final class DexHasher {

    /**
     * Returns the signature of all but the first 32 bytes of {@code dex}. The
     * first 32 bytes of dex files are not specified to be included in the
     * signature.
     */
    public byte[] computeSignature(File dex) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }

        FileInputStream in = new FileInputStream(dex);
        skipFully(in, 32);

        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) != -1) {
            digest.update(buffer, 0, count);
        }

        return digest.digest();
    }

    /**
     * Returns the checksum of all but the first 12 bytes of {@code dex}.
     *
     * @param signature either a 20-byte signature of {@code dex}, or null. If
     *     non-null, the checksum will be computed using {@code signature}
     *     rather than the corresponding bytes in the file.
     */
    public int computeChecksum(File dex, byte[] signature) throws IOException {
        Adler32 adler32 = new Adler32();

        FileInputStream in = new FileInputStream(dex);
        skipFully(in, 12);

        if (signature != null) {
            adler32.update(signature);
            skipFully(in, SizeOf.SIGNATURE);
        }

        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) != -1) {
            adler32.update(buffer, 0, count);
        }

        return (int) adler32.getValue();
    }

    /**
     * Generates the signature and checksum of the dex file {@code out} and
     * writes them to the file.
     */
    public void writeHashes(File out) throws IOException {
        byte[] signature = computeSignature(out);
        int checksum = computeChecksum(out, signature);

        RandomAccessFile file = new RandomAccessFile(out, "rw");
        file.seek(8);
        file.writeInt(Integer.reverseBytes(checksum));
        file.write(signature);
        file.close();
    }

    private void skipFully(InputStream in, int count) throws IOException {
        int total = 0;
        while (total < count) {
            total += in.skip(count);
        }
    }
}
