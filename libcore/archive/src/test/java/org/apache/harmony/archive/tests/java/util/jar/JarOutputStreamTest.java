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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import tests.support.Support_Exec;
import tests.support.resource.Support_Resources;

public class JarOutputStreamTest extends junit.framework.TestCase {

    /**
     * @tests java.util.jar.JarOutputStream#putNextEntry(java.util.zip.ZipEntry)
     */
    public void test_putNextEntryLjava_util_zip_ZipEntry() {
        // testClass file`s actual extension is .class, since having .class
        // extension files in source dir causes
        // problems on eclipse, the extension is changed into .ser or it can be
        // anything. The file is being
        // read by inputstream and being written to other file,
        // as long as the content of the file is not changed, the extension does
        // not matter
        final String testClass = "hyts_mainClass.ser";
        final String entryName = "foo/bar/execjartest/MainClass.class";

        // test whether specifying the main class in the manifest
        // works using either /'s or .'s as a separator
        final String[] manifestMain = { "foo.bar.execjartest.MainClass",
                "foo/bar/execjartest/MainClass" };

        for (String element : manifestMain) {

            // create the manifest
            Manifest newman = new Manifest();
            Attributes att = newman.getMainAttributes();
            att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            att.put(Attributes.Name.MAIN_CLASS, element);

            File outputJar = null;
            JarOutputStream jout = null;

            try {
                // open the output jarfile
                outputJar = File.createTempFile("hyts_", ".jar");
                jout = new JarOutputStream(new FileOutputStream(outputJar),
                        newman);
                jout.putNextEntry(new JarEntry(entryName));
            } catch (Exception e) {
                fail("Error creating JarOutputStream: " + e);
            }
            File resources = Support_Resources.createTempFolder();
            try {
                // read in the class file, and output it to the jar
                Support_Resources.copyFile(resources, null, testClass);
                URL jarURL = new URL((new File(resources, testClass)).toURL()
                        .toString());
                InputStream jis = jarURL.openStream();

                byte[] bytes = new byte[1024];
                int len;
                while ((len = jis.read(bytes)) != -1) {
                    jout.write(bytes, 0, len);
                }

                jout.flush();
                jout.close();
                jis.close();
            } catch (Exception e) {
                fail("Error writing JAR file for testing: " + e);
            }
            String res = null;
            // set up the VM parameters
            String[] args = new String[2];
            args[0] = "-jar";
            args[1] = outputJar.getAbsolutePath();

            try {
                // execute the JAR and read the result
                res = Support_Exec.execJava(args, null, true);
            } catch (Exception e) {
                fail("Exception executing test JAR: " + e);
            }

            assertTrue("Error executing JAR test on: " + element
                    + ". Result returned was incorrect.", res
                    .startsWith("TEST"));
            outputJar.delete();
        }
    }

    @Override
    protected void setUp() {
    }

    @Override
    protected void tearDown() {
    }

}
