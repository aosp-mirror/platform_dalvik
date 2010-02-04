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
package org.apache.harmony.archive.tests.java.util.jar;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import tests.support.resource.Support_Resources;

@TestTargetClass(Manifest.class)
public class ManifestTest extends TestCase {

    private final String jarName = "hyts_patch.jar";

    private final String attJarName = "hyts_att.jar";

    private Manifest m;

    private Manifest m2;

    private final String ATT_ENTRY_NAME = "HasAttributes.txt";

    private final String MANIFEST_NAME = "manifest/hyts_MANIFEST.MF";

    private File resources;

    @Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
        try {
            Support_Resources.copyFile(resources, null, jarName);
            JarFile jarFile = new JarFile(new File(resources, jarName));
            m = jarFile.getManifest();
            jarFile.close();
            Support_Resources.copyFile(resources, null, attJarName);
            jarFile = new JarFile(new File(resources, attJarName));
            m2 = jarFile.getManifest();
            jarFile.close();
        } catch (Exception e) {
            fail("Exception during setup: " + e.toString());
        }
    }

    private Manifest getManifest(String fileName) {
        try {
            Support_Resources.copyFile(resources, null, fileName);
            JarFile jarFile = new JarFile(new File(resources, fileName));
            Manifest m = jarFile.getManifest();
            jarFile.close();
            return m;
        } catch (Exception e) {
            fail("Exception during setup: " + e.toString());
            return null;
        }
    }

    /**
     * @tests java.util.jar.Manifest#Manifest()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Manifest",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.util.jar.Manifest()
        Manifest emptyManifest = new Manifest();
        assertTrue("Should have no entries", emptyManifest.getEntries()
                .isEmpty());
        assertTrue("Should have no main attributes", emptyManifest
                .getMainAttributes().isEmpty());
    }

    /**
     * @tests java.util.jar.Manifest#Manifest(java.util.jar.Manifest)
     */
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "Manifest",
            args = {java.util.jar.Manifest.class}
    )
    public void testCopyingConstructor() throws IOException {
        Manifest firstManifest = new Manifest(new URL(Support_Resources
                .getURL(MANIFEST_NAME)).openStream());
        Manifest secondManifest = new Manifest(firstManifest);
        assertEquals(firstManifest, secondManifest);
    }

    /**
     * @tests java.util.jar.Manifest#Manifest(Manifest)
     */
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "Manifest",
            args = {java.util.jar.Manifest.class}
    )
    public void test_ConstructorLjava_util_jar_Manifest() {
        // Test for method java.util.jar.Manifest()
        Manifest emptyManifest = new Manifest();
        Manifest emptyClone = new Manifest(emptyManifest);
        assertTrue("Should have no entries", emptyClone.getEntries().isEmpty());
        assertTrue("Should have no main attributes", emptyClone
                .getMainAttributes().isEmpty());
        assertEquals(emptyClone, emptyManifest);
        assertEquals(emptyClone, emptyManifest.clone());
    }

    private void assertAttribute(Attributes attr, String name, String value) {
        assertEquals("Incorrect " + name, value, attr.getValue(name));
    }

    private void checkManifest(Manifest manifest) {
        Attributes main = manifest.getMainAttributes();
        assertAttribute(main, "Bundle-Name", "ClientSupport");
        assertAttribute(main, "Bundle-Description",
                "Provides SessionService, AuthenticationService. Extends RegistryService.");
        assertAttribute(main, "Bundle-Activator",
                "com.ibm.ive.eccomm.client.support.ClientSupportActivator");
        assertAttribute(
                main,
                "Import-Package",
                "com.ibm.ive.eccomm.client.services.log,com.ibm.ive.eccomm.client.services.registry,com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,com.ibm.ive.eccomm.service.session; specification-version=1.0.0,com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,org.osgi.framework; specification-version=1.0.0,org.osgi.service.log; specification-version=1.0.0,com.ibm.ive.eccomm.flash; specification-version=1.2.0,com.ibm.ive.eccomm.client.xml,com.ibm.ive.eccomm.client.http.common,com.ibm.ive.eccomm.client.http.client");
        assertAttribute(
                main,
                "Import-Service",
                "org.osgi.service.log.LogReaderServiceorg.osgi.service.log.LogService,com.ibm.ive.eccomm.service.registry.RegistryService");
        assertAttribute(
                main,
                "Export-Package",
                "com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.common; specification-version=1.0.0,com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0");
        assertAttribute(
                main,
                "Export-Service",
                "com.ibm.ive.eccomm.service.authentication.AuthenticationService,com.ibm.ive.eccomm.service.session.SessionService");
        assertAttribute(main, "Bundle-Vendor", "IBM");
        assertAttribute(main, "Bundle-Version", "1.2.0");
    }

    /**
     * @tests java.util.jar.Manifest#Manifest(java.io.InputStream)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IOException checking missed.",
        method = "Manifest",
        args = {java.io.InputStream.class}
    )
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        Manifest m = getManifest(attJarName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.write(baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        Manifest mCopy = new Manifest(is);
        assertEquals(m, mCopy);

        Manifest manifest = new Manifest(new URL(Support_Resources
                .getURL(MANIFEST_NAME)).openStream());
        checkManifest(manifest);

        // regression test for HARMONY-5424
        String manifestContent = "Manifest-Version: 1.0\nCreated-By: Apache\nPackage: \nBuild-Jdk: 1.4.1_01\n\n"
                + "Name: \nSpecification-Title: foo\nSpecification-Version: 1.0\nSpecification-Vendor: \n"
                + "Implementation-Title: \nImplementation-Version: 1.0\nImplementation-Vendor: \n\n";
        ByteArrayInputStream bis = new ByteArrayInputStream(manifestContent
                .getBytes("ISO-8859-1"));


        Manifest mf = new Manifest(bis);
        assertEquals("Should be 4 main attributes", 4, mf.getMainAttributes()
                .size());

        Map<String, Attributes> entries = mf.getEntries();
        assertEquals("Should be one named entry", 1, entries.size());

        Attributes namedEntryAttributes = (Attributes) (entries.get(""));
        assertEquals("Should be 6 named entry attributes", 6,
                namedEntryAttributes.size());
    }

    /**
     * @tests java.util.jar.Manifest#clear()
     */
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "clear",
            args = {}
    )
    public void test_clear() {
        m2.clear();
        assertTrue("Should have no entries", m2.getEntries().isEmpty());
        assertTrue("Should have no main attributes", m2.getMainAttributes()
                .isEmpty());
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "clone",
            args = {}
    )
    public void test_clone() throws IOException {
        Manifest emptyManifest = new Manifest();
        Manifest emptyClone = (Manifest) emptyManifest.clone();
        assertTrue("Should have no entries", emptyClone.getEntries().isEmpty());
        assertTrue("Should have no main attributes", emptyClone
                .getMainAttributes().isEmpty());
        assertEquals(emptyClone, emptyManifest);
        assertEquals(emptyManifest.clone().getClass().getName(),
                "java.util.jar.Manifest");

        Manifest manifest = new Manifest(new URL(Support_Resources
                .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifestClone = (Manifest) manifest.clone();
        manifestClone.getMainAttributes();
        checkManifest(manifestClone);
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "equals",
            args = {java.lang.Object.class}
    )
    public void test_equals() throws IOException {
        Manifest manifest1 = new Manifest(new URL(Support_Resources.getURL(
                "manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifest2 = new Manifest(new URL(Support_Resources.getURL(
                "manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifest3 = new Manifest();

        assertTrue(manifest1.equals(manifest1));
        assertTrue(manifest1.equals(manifest2));
        assertFalse(manifest1.equals(manifest3));
        assertFalse(manifest1.equals(this));
    }

    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "hashCode",
            args = {}
    )
    public void test_hashCode() throws IOException {
        Manifest manifest1 = new Manifest(new URL(Support_Resources
                .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifest2 = new Manifest();
        assertEquals(manifest1.hashCode(), manifest1.hashCode());
        assertNotSame(manifest1.hashCode(), manifest2.hashCode());
    }

	/**
	 * @tests java.util.jar.Manifest#getAttributes(java.lang.String)
	 */
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAttributes",
            args = {String.class}
    )
	public void test_getAttributesLjava_lang_String() {
		assertNull("Should not exist",
				m2.getAttributes("Doesn't Exist"));
		assertEquals("Should exist", "OK", m2.getAttributes("HasAttributes.txt").get(
				new Attributes.Name("MyAttribute")));
	}

	/**
	 * @tests java.util.jar.Manifest#getEntries()
	 */
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEntries",
            args = {}
    )
	public void test_getEntries() {
		Map<String, Attributes> myMap = m2.getEntries();
		assertNull("Shouldn't exist", myMap.get("Doesn't exist"));
		assertEquals("Should exist",
				"OK", myMap.get("HasAttributes.txt").get(
						new Attributes.Name("MyAttribute")));
	}

	/**
	 * @tests java.util.jar.Manifest#getMainAttributes()
	 */
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMainAttributes",
            args = {}
    )
	public void test_getMainAttributes() {
		// Test for method java.util.jar.Attributes
		// java.util.jar.Manifest.getMainAttributes()
		Attributes a = m.getMainAttributes();
		assertEquals("Manifest_Version should return 1.0", "1.0", a.get(
				Attributes.Name.MANIFEST_VERSION));
	}

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {java.io.OutputStream.class}
    )
    public void test_writeLjava_io_OutputStream() throws IOException {
        byte b[] = null;
        Manifest manifest1 = null;
        Manifest manifest2 = null;
        InputStream is = null;
        try {
            manifest1 = new Manifest(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        manifest1.write(baos);

        b = baos.toByteArray();

        File f = File.createTempFile("111", "111");
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        try {
            manifest1.write(fos);
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }
        f.delete();

        ByteArrayInputStream bais = new ByteArrayInputStream(b);

        try {
            manifest2 = new Manifest(bais);
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        }

        assertTrue(manifest1.equals(manifest2));
    }

    /**
     * Ensures compatibility with manifests produced by gcc.
     *
     * @see <a
     *      href="http://issues.apache.org/jira/browse/HARMONY-5662">HARMONY-5662</a>
     */
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "Manifest",
            args = {InputStream.class}
    )
    public void testNul() throws IOException {
        String manifestContent =
                "Manifest-Version: 1.0\nCreated-By: nasty gcc tool\n\n\0";

        byte[] bytes = manifestContent.getBytes("ISO-8859-1");
        new Manifest(new ByteArrayInputStream(bytes)); // the last NUL is ok

        bytes[bytes.length - 1] = 26;
        new Manifest(new ByteArrayInputStream(bytes)); // the last EOF is ok

        bytes[bytes.length - 1] = 'A'; // the last line ignored
        new Manifest(new ByteArrayInputStream(bytes));

        bytes[2] = 0; // NUL char in Manifest
        try {
            new Manifest(new ByteArrayInputStream(bytes));
            fail("IOException expected");
        } catch (IOException e) {
            // desired
        }
    }

    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "Manifest",
            args = {InputStream.class}
    )
    public void testDecoding() throws IOException {
        Manifest m = getManifest(attJarName);
        final byte[] bVendor = new byte[] { (byte) 0xd0, (byte) 0x9C,
                (byte) 0xd0, (byte) 0xb8, (byte) 0xd0, (byte) 0xbb,
                (byte) 0xd0, (byte) 0xb0, (byte) 0xd1, (byte) 0x8f, ' ',
                (byte) 0xd0, (byte) 0xb4, (byte) 0xd0, (byte) 0xbe,
                (byte) 0xd1, (byte) 0x87, (byte) 0xd1, (byte) 0x83,
                (byte) 0xd0, (byte) 0xbd, (byte) 0xd1, (byte) 0x8C,
                (byte) 0xd0, (byte) 0xba, (byte) 0xd0, (byte) 0xb0, ' ',
                (byte) 0xd0, (byte) 0x9C, (byte) 0xd0, (byte) 0xb0,
                (byte) 0xd1, (byte) 0x88, (byte) 0xd0, (byte) 0xb0 };

        final byte[] bSpec = new byte[] { (byte) 0xe1, (byte) 0x88,
                (byte) 0xb0, (byte) 0xe1, (byte) 0x88, (byte) 0x8b,
                (byte) 0xe1, (byte) 0x88, (byte) 0x9d, ' ', (byte) 0xe1,
                (byte) 0x9a, (byte) 0xa0, (byte) 0xe1, (byte) 0x9a,
                (byte) 0xb1, (byte) 0xe1, (byte) 0x9b, (byte) 0x81,
                (byte) 0xe1, (byte) 0x9a, (byte) 0xa6, ' ', (byte) 0xd8,
                (byte) 0xb3, (byte) 0xd9, (byte) 0x84, (byte) 0xd8,
                (byte) 0xa7, (byte) 0xd9, (byte) 0x85, ' ', (byte) 0xd8,
                (byte) 0xb9, (byte) 0xd8, (byte) 0xb3, (byte) 0xd9,
                (byte) 0x84, (byte) 0xd8, (byte) 0xa7, (byte) 0xd9,
                (byte) 0x85, (byte) 0xd8, (byte) 0xa9, ' ', (byte) 0xdc,
                (byte) 0xab, (byte) 0xdc, (byte) 0xa0, (byte) 0xdc,
                (byte) 0xa1, (byte) 0xdc, (byte) 0x90, ' ', (byte) 0xe0,
                (byte) 0xa6, (byte) 0xb6, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbe, (byte) 0xe0, (byte) 0xa6, (byte) 0xa8,
                (byte) 0xe0, (byte) 0xa7, (byte) 0x8d, (byte) 0xe0,
                (byte) 0xa6, (byte) 0xa4, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbf, ' ', (byte) 0xd0, (byte) 0xa0, (byte) 0xd0,
                (byte) 0xb5, (byte) 0xd0, (byte) 0xba, (byte) 0xd1,
                (byte) 0x8a, (byte) 0xd0, (byte) 0xb5, (byte) 0xd0,
                (byte) 0xbb, ' ', (byte) 0xd0, (byte) 0x9c, (byte) 0xd0,
                (byte) 0xb8, (byte) 0xd1, (byte) 0x80, ' ', (byte) 0xe0,
                (byte) 0xa6, (byte) 0xb6, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbe, (byte) 0xe0, (byte) 0xa6, (byte) 0xa8,
                (byte) 0xe0, (byte) 0xa7, (byte) 0x8d, (byte) 0xe0,
                (byte) 0xa6, (byte) 0xa4, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbf, ' ', (byte) 0xe0, (byte) 0xbd, (byte) 0x9e,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xb2, (byte) 0xe0,
                (byte) 0xbc, (byte) 0x8b, (byte) 0xe0, (byte) 0xbd,
                (byte) 0x96, (byte) 0xe0, (byte) 0xbd, (byte) 0x91,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xba, ' ', (byte) 0xd0,
                (byte) 0x9c, (byte) 0xd0, (byte) 0xb0, (byte) 0xd1,
                (byte) 0x88, (byte) 0xd0, (byte) 0xb0, (byte) 0xd1,
                (byte) 0x80, ' ', (byte) 0xe1, (byte) 0x8f, (byte) 0x99,
                (byte) 0xe1, (byte) 0x8e, (byte) 0xaf, (byte) 0xe1,
                (byte) 0x8f, (byte) 0xb1, ' ', (byte) 0xcf, (byte) 0xa8,
                (byte) 0xce, (byte) 0xb9, (byte) 0xcf, (byte) 0x81,
                (byte) 0xce, (byte) 0xb7, (byte) 0xce, (byte) 0xbd,
                (byte) 0xce, (byte) 0xb7, ' ', (byte) 0xde, (byte) 0x90,
                (byte) 0xde, (byte) 0xaa, (byte) 0xde, (byte) 0x85,
                (byte) 0xde, (byte) 0xa6, ' ', (byte) 0xe0, (byte) 0xbd,
                (byte) 0x82, (byte) 0xe0, (byte) 0xbd, (byte) 0x9e,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xb2, (byte) 0xe0,
                (byte) 0xbc, (byte) 0x8b, (byte) 0xe0, (byte) 0xbd,
                (byte) 0x96, (byte) 0xe0, (byte) 0xbd, (byte) 0x91,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xba, ' ', (byte) 0xce,
                (byte) 0x95, (byte) 0xce, (byte) 0xb9, (byte) 0xcf,
                (byte) 0x81, (byte) 0xce, (byte) 0xae, (byte) 0xce,
                (byte) 0xbd, (byte) 0xce, (byte) 0xb7, ' ', (byte) 0xd8,
                (byte) 0xb5, (byte) 0xd9, (byte) 0x84, (byte) 0xd8,
                (byte) 0xad, ' ', (byte) 0xe0, (byte) 0xaa, (byte) 0xb6,
                (byte) 0xe0, (byte) 0xaa, (byte) 0xbe, (byte) 0xe0,
                (byte) 0xaa, (byte) 0x82, (byte) 0xe0, (byte) 0xaa,
                (byte) 0xa4, (byte) 0xe0, (byte) 0xaa, (byte) 0xbf, ' ',
                (byte) 0xe5, (byte) 0xb9, (byte) 0xb3, (byte) 0xe5,
                (byte) 0x92, (byte) 0x8c, ' ', (byte) 0xd7, (byte) 0xa9,
                (byte) 0xd7, (byte) 0x9c, (byte) 0xd7, (byte) 0x95,
                (byte) 0xd7, (byte) 0x9d, ' ', (byte) 0xd7, (byte) 0xa4,
                (byte) 0xd7, (byte) 0xa8, (byte) 0xd7, (byte) 0x99,
                (byte) 0xd7, (byte) 0x93, (byte) 0xd7, (byte) 0x9f, ' ',
                (byte) 0xe5, (byte) 0x92, (byte) 0x8c, (byte) 0xe5,
                (byte) 0xb9, (byte) 0xb3, ' ', (byte) 0xe5, (byte) 0x92,
                (byte) 0x8c, (byte) 0xe5, (byte) 0xb9, (byte) 0xb3, ' ',
                (byte) 0xd8, (byte) 0xaa, (byte) 0xd9, (byte) 0x89,
                (byte) 0xd9, (byte) 0x86, (byte) 0xda, (byte) 0x86,
                (byte) 0xd9, (byte) 0x84, (byte) 0xd9, (byte) 0x89,
                (byte) 0xd9, (byte) 0x82, ' ', (byte) 0xe0, (byte) 0xae,
                (byte) 0x85, (byte) 0xe0, (byte) 0xae, (byte) 0xae,
                (byte) 0xe0, (byte) 0xaf, (byte) 0x88, (byte) 0xe0,
                (byte) 0xae, (byte) 0xa4, (byte) 0xe0, (byte) 0xae,
                (byte) 0xbf, ' ', (byte) 0xe0, (byte) 0xb0, (byte) 0xb6,
                (byte) 0xe0, (byte) 0xb0, (byte) 0xbe, (byte) 0xe0,
                (byte) 0xb0, (byte) 0x82, (byte) 0xe0, (byte) 0xb0,
                (byte) 0xa4, (byte) 0xe0, (byte) 0xb0, (byte) 0xbf, ' ',
                (byte) 0xe0, (byte) 0xb8, (byte) 0xaa, (byte) 0xe0,
                (byte) 0xb8, (byte) 0xb1, (byte) 0xe0, (byte) 0xb8,
                (byte) 0x99, (byte) 0xe0, (byte) 0xb8, (byte) 0x95,
                (byte) 0xe0, (byte) 0xb8, (byte) 0xb4, (byte) 0xe0,
                (byte) 0xb8, (byte) 0xa0, (byte) 0xe0, (byte) 0xb8,
                (byte) 0xb2, (byte) 0xe0, (byte) 0xb8, (byte) 0x9e, ' ',
                (byte) 0xe1, (byte) 0x88, (byte) 0xb0, (byte) 0xe1,
                (byte) 0x88, (byte) 0x8b, (byte) 0xe1, (byte) 0x88,
                (byte) 0x9d, ' ', (byte) 0xe0, (byte) 0xb7, (byte) 0x83,
                (byte) 0xe0, (byte) 0xb7, (byte) 0x8f, (byte) 0xe0,
                (byte) 0xb6, (byte) 0xb8, (byte) 0xe0, (byte) 0xb6,
                (byte) 0xba, ' ', (byte) 0xe0, (byte) 0xa4, (byte) 0xb6,
                (byte) 0xe0, (byte) 0xa4, (byte) 0xbe, (byte) 0xe0,
                (byte) 0xa4, (byte) 0xa8, (byte) 0xe0, (byte) 0xa5,
                (byte) 0x8d, (byte) 0xe0, (byte) 0xa4, (byte) 0xa4,
                (byte) 0xe0, (byte) 0xa4, (byte) 0xbf, (byte) 0xe0,
                (byte) 0xa4, (byte) 0x83, ' ', (byte) 0xe1, (byte) 0x83,
                (byte) 0x9b, (byte) 0xe1, (byte) 0x83, (byte) 0xa8,
                (byte) 0xe1, (byte) 0x83, (byte) 0x95, (byte) 0xe1,
                (byte) 0x83, (byte) 0x98, (byte) 0xe1, (byte) 0x83,
                (byte) 0x93, (byte) 0xe1, (byte) 0x83, (byte) 0x9d,
                (byte) 0xe1, (byte) 0x83, (byte) 0x91, (byte) 0xe1,
                (byte) 0x83, (byte) 0x90 };
        // TODO Cannot make the following word work, encoder changes needed
        // (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbc, (byte) 0xb2, (byte) 0xed,
        // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
        // (byte) 0xb0, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbd, (byte) 0x85, (byte) 0xed,
        // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
        // (byte) 0xb0, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbc, (byte) 0xb9, (byte) 0xed,
        // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
        // (byte) 0xb8, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbc, (byte) 0xb9, ' '

        final String vendor = new String(bVendor, "UTF-8");
        final String spec = new String(bSpec, "UTF-8");
        m.getMainAttributes()
                .put(Attributes.Name.IMPLEMENTATION_VENDOR, vendor);
        m.getAttributes(ATT_ENTRY_NAME).put(
                Attributes.Name.IMPLEMENTATION_VENDOR, vendor);
        m.getEntries().get(ATT_ENTRY_NAME).put(
                Attributes.Name.SPECIFICATION_TITLE, spec);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.write(baos);
        m = new Manifest(new ByteArrayInputStream(baos.toByteArray()));

        assertEquals(vendor, m.getMainAttributes().get(
                Attributes.Name.IMPLEMENTATION_VENDOR));
        assertEquals(vendor, m.getEntries().get(ATT_ENTRY_NAME).get(
                Attributes.Name.IMPLEMENTATION_VENDOR));
        assertEquals(spec, m.getAttributes(ATT_ENTRY_NAME).get(
                Attributes.Name.SPECIFICATION_TITLE));
    }

    /**
     * @tests {@link java.util.jar.Manifest#read(java.io.InputStream)
     */
    @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "",
            method = "read",
            args = {InputStream.class}
    )
    public void testRead() {
        // Regression for HARMONY-89
        InputStream is = new InputStreamImpl();
        try {
            new Manifest().read(is);
            fail("IOException expected");
        } catch (IOException e) {
            // desired
        }
    }

    // helper class
    private class InputStreamImpl extends InputStream {
        public InputStreamImpl() {
            super();
        }

        @Override
        public int read() {
            return 0;
        }
    }
}
