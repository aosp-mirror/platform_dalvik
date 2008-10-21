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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;

import org.apache.harmony.archive.internal.nls.Messages;
import org.apache.harmony.luni.util.Base64;
import org.apache.harmony.security.utils.JarUtils;

import org.apache.harmony.archive.util.Util;

// BEGIN android-added
import org.apache.harmony.xnet.provider.jsse.OpenSSLMessageDigestJDK;
// END android-added

/**
 * Non-public class used by {@link JarFile} and
 * {@link JarInputStream} to manage the verification of signed
 * jars. <code>JarFile</code> and <code>JarInputStream</code> objects will
 * be expected to have a <code>JarVerifier</code> instance member which can be
 * used to carry out the tasks associated with verifying a signed jar. These
 * tasks would typically include:
 * <ul>
 * <li>verification of all signed signature files
 * <li>confirmation that all signed data was signed only by the party or
 * parties specified in the signature block data
 * <li>verification that the contents of all signature files (i.e.
 * <code>.SF</code> files) agree with the jar entries information found in the
 * jar manifest.
 * </ul>
 */
class JarVerifier {

    private final String jarName;

    private Manifest man;

    private HashMap<String, byte[]> metaEntries = new HashMap<String, byte[]>(5);

    private final Hashtable<String, HashMap<String, Attributes>> signatures =
        new Hashtable<String, HashMap<String, Attributes>>(5);

    private final Hashtable<String, Certificate[]> certificates =
        new Hashtable<String, Certificate[]>(5);

    private final Hashtable<String, Certificate[]> verifiedEntries =
        new Hashtable<String, Certificate[]>();

    byte[] mainAttributesChunk;

    // BEGIN android-added
    private static long measureCount = 0;
    
    private static long averageTime = 0;
    // END android-added
    
    /**
     * TODO Type description
     */
    static class VerifierEntry extends OutputStream {

        MessageDigest digest;

        byte[] hash;

        Certificate[] certificates;

        VerifierEntry(MessageDigest digest, byte[] hash,
                Certificate[] certificates) {
            this.digest = digest;
            this.hash = hash;
            this.certificates = certificates;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int value) {
            digest.update((byte) value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        @Override
        public void write(byte[] buf, int off, int nbytes) {
            digest.update(buf, off, nbytes);
        }
    }

    /**
     * Constructs and returns a new instance of JarVerifier.
     * 
     * @param name
     *            the name of the jar file being verified.
     */
    JarVerifier(String name) {
        jarName = name;
    }

    /**
     * Called for each new jar entry read in from the input stream. This method
     * constructs and returns a new {@link VerifierEntry} which contains the
     * certificates used to sign the entry and its hash value as specified in
     * the jar manifest.
     * 
     * @param name
     *            the name of an entry in a jar file which is <b>not</b> in the
     *            <code>META-INF</code> directory.
     * @return a new instance of {@link VerifierEntry} which can be used by
     *         callers as an {@link OutputStream}.
     */
    VerifierEntry initEntry(String name) {
        // If no manifest is present by the time an entry is found,
        // verification cannot occur. If no signature files have
        // been found, do not verify.
        if (man == null || signatures.size() == 0) {
            return null;
        }

        Attributes attributes = man.getAttributes(name);
        // entry has no digest
        if (attributes == null) {
            return null;
        }

        Vector<Certificate> certs = new Vector<Certificate>();
        Iterator<Map.Entry<String, HashMap<String, Attributes>>> it =
            signatures.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, HashMap<String, Attributes>> entry = it.next();
            HashMap<String, Attributes> hm = entry.getValue();
            if (hm.get(name) != null) {
                // Found an entry for entry name in .SF file
                String signatureFile = entry.getKey();

                Vector<Certificate> newCerts = getSignerCertificates(
                        signatureFile, certificates);
                Iterator<Certificate> iter = newCerts.iterator();
                while (iter.hasNext()) {
                    certs.add(iter.next());
                }
            }
        }

        // entry is not signed
        if (certs.size() == 0) {
            return null;
        }
        Certificate[] certificatesArray = new Certificate[certs.size()];
        certs.toArray(certificatesArray);

        String algorithms = attributes.getValue("Digest-Algorithms"); //$NON-NLS-1$
        if (algorithms == null) {
            algorithms = "SHA SHA1"; //$NON-NLS-1$
        }
        StringTokenizer tokens = new StringTokenizer(algorithms);
        while (tokens.hasMoreTokens()) {
            String algorithm = tokens.nextToken();
            String hash = attributes.getValue(algorithm + "-Digest"); //$NON-NLS-1$
            if (hash == null) {
                continue;
            }
            byte[] hashBytes;
            try {
                hashBytes = hash.getBytes("ISO8859_1"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString());
            }

            try {
                // BEGIN android-removed
                // return new VerifierEntry(MessageDigest.getInstance(algorithm),
                //        hashBytes, certificatesArray);
                // END android-removed
                // BEGIN android-added
                return new VerifierEntry(OpenSSLMessageDigestJDK.getInstance(algorithm),
                        hashBytes, certificatesArray);
                // END android-added
            } catch (NoSuchAlgorithmException e) {
                // Ignored
            }
        }
        return null;
    }

    /**
     * Add a new meta entry to the internal collection of data held on each jar
     * entry in the <code>META-INF</code> directory including the manifest
     * file itself. Files associated with the signing of a jar would also be
     * added to this collection.
     * 
     * @param name
     *            the name of the file located in the <code>META-INF</code>
     *            directory.
     * @param buf
     *            the file bytes for the file called <code>name</code>.
     * @see #removeMetaEntries()
     */
    void addMetaEntry(String name, byte[] buf) {
        metaEntries.put(Util.toASCIIUpperCase(name), buf);
    }

    /**
     * If the associated jar file is signed, check on the validity of all of the
     * known signatures.
     * 
     * @return <code>true</code> if the associated jar is signed and an
     *         internal check verifies the validity of the signature(s).
     *         <code>false</code> if the associated jar file has no entries at
     *         all in its <code>META-INF</code> directory. This situation is
     *         indicative of an invalid jar file.
     *         <p>
     *         Will also return true if the jar file is <i>not</i> signed.
     *         </p>
     * @throws SecurityException
     *             if the jar file is signed and it is determined that a
     *             signature block file contains an invalid signature for the
     *             corresponding signature file.
     */
    synchronized boolean readCertificates() {
        if (metaEntries == null) {
            return false;
        }
        Iterator<String> it = metaEntries.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.endsWith(".DSA") || key.endsWith(".RSA")) { //$NON-NLS-1$ //$NON-NLS-2$
                // BEGIN android-changed (temporary, will go away)
                //log("JarVerifier", "verifyCertificate() called for " + key);
                //long t0 = System.currentTimeMillis();
                verifyCertificate(key);
                //long t1 = System.currentTimeMillis();
                //log("JarVerifier", "verifyCertificate() took " + (t1 - t0) + " ms");
                
                //averageTime = ((measureCount * averageTime) + (t1 - t0)) / (measureCount + 1);
                //measureCount++;

                //log("JarVerifier", "verifyCertificate() average time is " + averageTime + " ms");
                // END android-changed (temporary, will go away)
                
                // Check for recursive class load
                if (metaEntries == null) {
                    return false;
                }
                it.remove();
            }
        }
        return true;
    }

    /**
     * @param certFile
     */
    private void verifyCertificate(String certFile) {
        // Found Digital Sig, .SF should already have been read
        String signatureFile = certFile.substring(0, certFile.lastIndexOf('.'))
                + ".SF"; //$NON-NLS-1$
        byte[] sfBytes = metaEntries.get(signatureFile);
        if (sfBytes == null) {
            return;
        }

        byte[] sBlockBytes = metaEntries.get(certFile);
        try {
            Certificate[] signerCertChain = JarUtils.verifySignature(
                    new ByteArrayInputStream(sfBytes),
                    new ByteArrayInputStream(sBlockBytes));
            /*
             * Recursive call in loading security provider related class which
             * is in a signed jar. 
             */
            if (null == metaEntries) {
                return;
            }
            if (signerCertChain != null) {
                certificates.put(signatureFile, signerCertChain);
            }
        } catch (IOException e) {
            return;
        } catch (GeneralSecurityException e) {
            /* [MSG "archive.30", "{0} failed verification of {1}"] */
            throw new SecurityException(
                    Messages.getString("archive.30", jarName, signatureFile)); //$NON-NLS-1$
        }

        // Verify manifest hash in .sf file
        Attributes attributes = new Attributes();
        HashMap<String, Attributes> hm = new HashMap<String, Attributes>();
        try {
            new InitManifest(new ByteArrayInputStream(sfBytes), attributes, hm,
                    null, "Signature-Version"); //$NON-NLS-1$
        } catch (IOException e) {
            return;
        }

        boolean createdBySigntool = false;
        String createdByValue = attributes.getValue("Created-By"); //$NON-NLS-1$
        if (createdByValue != null) {
            createdBySigntool = createdByValue.indexOf("signtool") != -1; //$NON-NLS-1$
        }

        // Use .SF to verify the mainAttributes of the manifest
        // If there is no -Digest-Manifest-Main-Attributes entry in .SF
        // file, such as those created before java 1.5, then we ignore
        // such verification.
        // FIXME: The meaning of createdBySigntool
        if (mainAttributesChunk != null && !createdBySigntool) {
            String digestAttribute = "-Digest-Manifest-Main-Attributes"; //$NON-NLS-1$
            if (!verify(attributes, digestAttribute, mainAttributesChunk,
                    false, true)) {
                /* [MSG "archive.30", "{0} failed verification of {1}"] */
                throw new SecurityException(
                        Messages.getString("archive.30", jarName, signatureFile)); //$NON-NLS-1$
            }
        }

        byte[] manifest = metaEntries.get(JarFile.MANIFEST_NAME);
        if (manifest == null) {
            return;
        }
        // Use .SF to verify the whole manifest
        String digestAttribute = createdBySigntool ? "-Digest" //$NON-NLS-1$
                : "-Digest-Manifest"; //$NON-NLS-1$
        if (!verify(attributes, digestAttribute, manifest, false, false)) {
            Iterator<Map.Entry<String, Attributes>> it = hm.entrySet()
                    .iterator();
            while (it.hasNext()) {
                Map.Entry<String, Attributes> entry = it.next();
                byte[] chunk = man.getChunk(entry.getKey());
                if (chunk == null) {
                    return;
                }
                if (!verify(entry.getValue(), "-Digest", chunk, //$NON-NLS-1$
                        createdBySigntool, false)) {
                    /* [MSG "archive.31", "{0} has invalid digest for {1} in {2}"] */
                    throw new SecurityException(
                        Messages.getString("archive.31", //$NON-NLS-1$
                            new Object[] { signatureFile, entry.getKey(), jarName }));
                }
            }
        }
        metaEntries.put(signatureFile, null);
        signatures.put(signatureFile, hm);
    }

    /**
     * Associate this verifier with the specified {@link Manifest} object.
     * 
     * @param mf
     *            a <code>java.util.jar.Manifest</code> object.
     */
    void setManifest(Manifest mf) {
        man = mf;
    }

    /**
     * Verifies that the digests stored in the manifest match the decrypted
     * digests from the .SF file. This indicates the validity of the signing,
     * not the integrity of the file, as it's digest must be calculated and
     * verified when its contents are read.
     * 
     * @param entry
     *            the {@link VerifierEntry} associated with the specified
     *            <code>zipEntry</code>.
     * @param zipEntry
     *            an entry in the jar file
     * @throws SecurityException
     *             if the digest value stored in the manifest does <i>not</i>
     *             agree with the decrypted digest as recovered from the
     *             <code>.SF</code> file.
     * @see #initEntry(String)
     */
    void verifySignatures(VerifierEntry entry, ZipEntry zipEntry) {
        byte[] digest = entry.digest.digest();
        if (!MessageDigest.isEqual(digest, Base64.decode(entry.hash))) {
            /* [MSG "archive.31", "{0} has invalid digest for {1} in {2}"] */
            throw new SecurityException(Messages.getString("archive.31", new Object[] { //$NON-NLS-1$
                    JarFile.MANIFEST_NAME, zipEntry.getName(), jarName }));
        }
        verifiedEntries.put(zipEntry.getName(), entry.certificates);
    }

    /**
     * Returns a <code>boolean</code> indication of whether or not the
     * associated jar file is signed.
     * 
     * @return <code>true</code> if the jar is signed, <code>false</code>
     *         otherwise.
     */
    boolean isSignedJar() {
        return certificates.size() > 0;
    }

    private boolean verify(Attributes attributes, String entry, byte[] data,
            boolean ignoreSecondEndline, boolean ignorable) {
        String algorithms = attributes.getValue("Digest-Algorithms"); //$NON-NLS-1$
        if (algorithms == null) {
            algorithms = "SHA SHA1"; //$NON-NLS-1$
        }
        StringTokenizer tokens = new StringTokenizer(algorithms);
        while (tokens.hasMoreTokens()) {
            String algorithm = tokens.nextToken();
            String hash = attributes.getValue(algorithm + entry);
            if (hash == null) {
                continue;
            }

            MessageDigest md;
            try {
                // BEGIN android-removed
                // md = MessageDigest.getInstance(algorithm);
                // END android-removed
                // BEGIN android-added
                md = OpenSSLMessageDigestJDK.getInstance(algorithm);
                // END android-added
            } catch (NoSuchAlgorithmException e) {
                continue;
            }
            if (ignoreSecondEndline && data[data.length - 1] == '\n'
                    && data[data.length - 2] == '\n') {
                md.update(data, 0, data.length - 1);
            } else {
                md.update(data, 0, data.length);
            }
            byte[] b = md.digest();
            byte[] hashBytes;
            try {
                hashBytes = hash.getBytes("ISO8859_1"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString());
            }
            return MessageDigest.isEqual(b, Base64.decode(hashBytes));
        }
        return ignorable;
    }

    /**
     * Returns all of the {@link java.security.cert.Certificate} instances that
     * were used to verify the signature on the jar entry called
     * <code>name</code>.
     * 
     * @param name
     *            the name of a jar entry.
     * @return an array of {@link java.security.cert.Certificate}.
     */
    Certificate[] getCertificates(String name) {
        Certificate[] verifiedCerts = verifiedEntries.get(name);
        if (verifiedCerts == null) {
            return null;
        }
        return verifiedCerts.clone();
    }

    /**
     * Remove all entries from the internal collection of data held about each
     * jar entry in the <code>META-INF</code> directory.
     * 
     * @see #addMetaEntry(String, byte[])
     */
    void removeMetaEntries() {
        metaEntries = null;
    }

    /**
     * Returns a <code>Vector</code> of all of the
     * {@link java.security.cert.Certificate}s that are associated with the
     * signing of the named signature file.
     * 
     * @param signatureFileName
     *            the name of a signature file
     * @param certificates
     *            a <code>Map</code> of all of the certificate chains
     *            discovered so far while attempting to verify the jar that
     *            contains the signature file <code>signatureFileName</code>.
     *            This object will have been previously set in the course of one
     *            or more calls to
     *            {@link #verifyJarSignatureFile(String, String, String, Map, Map)}
     *            where it was passed in as the last argument.
     * @return all of the <code>Certificate</code> entries for the signer of
     *         the jar whose actions led to the creation of the named signature
     *         file.
     */
    public static Vector<Certificate> getSignerCertificates(
            String signatureFileName, Map<String, Certificate[]> certificates) {
        Vector<Certificate> result = new Vector<Certificate>();
        Certificate[] certChain = certificates.get(signatureFileName);
        if (certChain != null) {
            for (Certificate element : certChain) {
                result.add(element);
            }
        }
        return result;
    }
    
    // TODO Just for debugging purposes, remove later.
    private static void log(String tag, String msg) {
        try {
            Class clazz = Class.forName("android.util.Log");
            java.lang.reflect.Method method = clazz.getMethod("d", new Class[] {
                    String.class, String.class
            });
            method.invoke(null, new Object[] {
                    tag, msg
            });
        } catch (Exception ex) {
            // Silently ignore.
        }
    }
    
}

