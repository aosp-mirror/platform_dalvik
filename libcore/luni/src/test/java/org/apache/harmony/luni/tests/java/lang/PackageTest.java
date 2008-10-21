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
package org.apache.harmony.luni.tests.java.lang;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import tests.support.resource.Support_Resources;

public class PackageTest extends junit.framework.TestCase {

    private File resources;

    private String resPath;

    Package getTestPackage(String resourceJar, String className)
            throws Exception {
        Support_Resources.copyFile(resources, "Package", resourceJar);
        URL resourceURL = new URL("file:/" + resPath + "/Package/"
                + resourceJar);

        URLClassLoader ucl = new URLClassLoader(new URL[] { resourceURL }, null);
        return Class.forName(className, true, ucl).getPackage();
    }

    @Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
        resPath = resources.toString();
        if (resPath.charAt(0) == '/' || resPath.charAt(0) == '\\')
            resPath = resPath.substring(1);
    }

    /**
     * There is a newer version of this class with some actual tests but since
     * the class is not implemented they all fail. For now use the stub test
     * methods.
     */

    /**
     * @tests java.lang.Package#getImplementationVendor()
     * @tests java.lang.Package#getImplementationVersion()
     * @tests java.lang.Package#getSpecificationTitle()
     * @tests java.lang.Package#getSpecificationVendor()
     * @tests java.lang.Package#getSpecificationVersion()
     * @tests java.lang.Package#getImplementationTitle()
     */
    public void test_helper_Attributes() throws Exception {

        Package p = getTestPackage("hyts_all_attributes.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (1)",
                "p Implementation-Title", p.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (1)",
                "p Implementation-Vendor", p.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (1)",
                "2.2.2", p.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (1)",
                "p Specification-Title", p.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (1)",
                "p Specification-Vendor", p.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (1)",
                "2.2.2", p.getSpecificationVersion());

        // No entry for the package
        Package p2 = getTestPackage("hyts_no_entry.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (2)",
                "MF Implementation-Title", p2.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (2)",
                "MF Implementation-Vendor", p2.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (2)",
                "5.3.b1", p2.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (2)",
                "MF Specification-Title", p2.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (2)",
                "MF Specification-Vendor", p2.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (2)",
                "1.2.3", p2.getSpecificationVersion());

        // No attributes in the package entry
        Package p3 = getTestPackage("hyts_no_attributes.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (3)",
                "MF Implementation-Title", p3.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (3)",
                "MF Implementation-Vendor", p3.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (3)",
                "5.3.b1", p3.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (3)",
                "MF Specification-Title", p3.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (3)",
                "MF Specification-Vendor", p3.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (3)",
                "1.2.3", p3.getSpecificationVersion());

        // Some attributes in the package entry
        Package p4 = getTestPackage("hyts_some_attributes.jar", "p.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (4)",
                "p Implementation-Title", p4.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (4)",
                "MF Implementation-Vendor", p4.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (4)",
                "2.2.2", p4.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (4)",
                "MF Specification-Title", p4.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (4)",
                "p Specification-Vendor", p4.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (4)",
                "2.2.2", p4.getSpecificationVersion());

        // subdirectory Package
        Package p5 = getTestPackage("hyts_pq.jar", "p.q.C");
        assertEquals(
                "Package getImplementationTitle returns a wrong string (5)",
                "p Implementation-Title", p5.getImplementationTitle());
        assertEquals(
                "Package getImplementationVendor returns a wrong string (5)",
                "p Implementation-Vendor", p5.getImplementationVendor());
        assertEquals(
                "Package getImplementationVersion returns a wrong string (5)",
                "1.1.3", p5.getImplementationVersion());
        assertEquals(
                "Package getSpecificationTitle returns a wrong string (5)",
                "p Specification-Title", p5.getSpecificationTitle());
        assertEquals(
                "Package getSpecificationVendor returns a wrong string (5)",
                "p Specification-Vendor", p5.getSpecificationVendor());
        assertEquals(
                "Package getSpecificationVersion returns a wrong string (5)",
                "2.2.0.0.0.0.0.0.0.0.0", p5.getSpecificationVersion());
    }

    /**
     * @tests java.lang.Package#getName()
     */
    public void test_getName() throws Exception {
        Package p = getTestPackage("hyts_pq.jar", "p.q.C");
        assertEquals("Package getName returns a wrong string", "p.q", p
                .getName());
    }

    /**
     * @tests java.lang.Package#getPackage(java.lang.String)
     */
    public void test_getPackageLjava_lang_String() {
        assertSame("Package getPackage failed for java.lang", Package
                .getPackage("java.lang"), Package.getPackage("java.lang"));

        assertSame("Package getPackage failed for java.lang", Package
                .getPackage("java.lang"), Object.class.getPackage());
    }

    /**
     * @tests java.lang.Package#getPackages()
     */
    public void test_getPackages() throws Exception {
        Package[] pckgs = Package.getPackages();
        boolean found = false;
        for (int i = 0; i < pckgs.length; i++) {
            if (pckgs[i].getName().equals("java.util")) {
                found = true;
                break;
            }
        }
        assertTrue("Package getPackages failed to retrieve a package", found);
    }

    /**
     * @tests java.lang.Package#hashCode()
     */
    public void test_hashCode() {
        Package p1 = Package.getPackage("java.lang");
        if (p1 != null) {
            assertEquals(p1.hashCode(), "java.lang".hashCode());
        }
    }

    /**
     * @tests java.lang.Package#isCompatibleWith(java.lang.String)
     */
    public void test_isCompatibleWithLjava_lang_String() throws Exception {
        Package p = getTestPackage("hyts_c.jar", "p.C");

        assertTrue("Package isCompatibleWith fails with lower version", p
                .isCompatibleWith("2.1.9.9"));
        assertTrue("Package isCompatibleWith fails with same version (1)", p
                .isCompatibleWith("2.2.0"));
        assertTrue("Package isCompatibleWith fails with same version (2)", p
                .isCompatibleWith("2.2"));
        assertFalse("Package isCompatibleWith fails with higher version", p
                .isCompatibleWith("2.2.0.0.1"));
        try {
            p.isCompatibleWith(null);
            fail("Null version is illegal");
        } catch (NumberFormatException ok) {
        } catch (NullPointerException compatible) {
            /*
             * RI throws NPE instead of NFE...
             */ 
        }

        try {
            p.isCompatibleWith("");
            fail("Empty version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith(".");
            fail("'.' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith("1.2.");
            fail("'1.2.' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith(".9");
            fail("'.9' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith("2.4..5");
            fail("'2.4..5' version is illegal");
        } catch (NumberFormatException ok) {}
        try {
            p.isCompatibleWith("20.-4");
            fail("'20.-4' version is illegal");
        } catch (NumberFormatException ok) {}
    }

    /**
     * @tests java.lang.Package#isSealed()
     */
    public void test_isSealed() throws Exception {
        Package p = getTestPackage("hyts_pq.jar", "p.q.C");
        assertTrue("Package isSealed returns wrong boolean", p.isSealed());
    }

    /**
     * @tests java.lang.Package#isSealed(java.net.URL)
     */
    public void test_isSealedLjava_net_URL() throws Exception {
        Package p = getTestPackage("hyts_c.jar", "p.C");
        assertFalse("Package isSealed returns wrong boolean (1)", p
                .isSealed(new URL("file:/" + resPath + "/")));
        assertTrue("Package isSealed returns wrong boolean (2)", p
                .isSealed(new URL("file:/" + resPath + "/Package/hyts_c.jar")));
    }

    /**
     * @tests java.lang.Package#toString()
     */
    public void test_toString() throws Exception {
        Package p = getTestPackage("hyts_c.jar", "p.C");
        assertTrue("Package toString returns wrong string", p.toString()
                .length() > 0);
    }
}
