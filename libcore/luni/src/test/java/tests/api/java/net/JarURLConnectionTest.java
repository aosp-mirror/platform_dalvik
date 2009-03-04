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

package tests.api.java.net;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestLevel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tests.support.resource.Support_Resources;

@TestTargetClass(JarURLConnection.class) 
public class JarURLConnectionTest extends junit.framework.TestCase {

    JarURLConnection juc;

    URLConnection uc;
    
    private static final URL BASE = getBaseURL();
    
    private static URL getBaseURL() {
        String clazz = JarURLConnectionTest.class.getName();
        int p = clazz.lastIndexOf(".");
        String pack = (p == -1 ? "" : clazz.substring(0, p)).replace('.', File.separatorChar);
       
        return JarURLConnectionTest.class.getClassLoader().getResource(pack);
    }
    
    private URL createContent(String jarFile, String inFile) 
                                                throws MalformedURLException {
        
        File resources = Support_Resources.createTempFolder();

        Support_Resources.copyFile(resources, "net", jarFile);
        File file = new File(resources.toString() + "/net/" + jarFile);
        URL fUrl1 = new URL("jar:file:" + file.getPath() + "!/" + inFile);
        
        return fUrl1;
    }

    /**
     * @tests java.net.JarURLConnection#getAttributes()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getAttributes",
      args = {}
    )
    public void test_getAttributes() throws Exception {
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/swt.dll");
        
        URL u = createContent("lf.jar", "swt.dll");
        juc = (JarURLConnection) u.openConnection();
        java.util.jar.Attributes a = juc.getAttributes();
        assertEquals("Returned incorrect Attributes", "SHA MD5", a
                .get(new java.util.jar.Attributes.Name("Digest-Algorithms")));
        
        //URL invURL = new URL("jar:"
        //        + BASE.toString()+"/InvalidJar.jar!/Test.class");
        
        URL invURL = createContent("InvalidJar.jar", "Test.class");
               
        JarURLConnection juConn = (JarURLConnection) invURL.openConnection();
        try {
            juConn.getAttributes();
            fail("IOException was not thrown.");
        } catch(java.io.IOException io) {
            //expected
        }
    }
    
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getCertificates",
      args = {}
    )
    public void test_getCertificates() throws Exception {
        
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/TestCodeSigners.jar!/Test.class");
        
        URL u = createContent("TestCodeSigners.jar", "Test.class");
        
        juc = (JarURLConnection) u.openConnection();
        assertNull(juc.getCertificates());
        
        JarEntry je = juc.getJarEntry();
        JarFile jf = juc.getJarFile();
        InputStream is = jf.getInputStream(je);
        is.skip(je.getSize());
        
        Certificate [] certs = juc.getCertificates();
        assertEquals(3, certs.length);
        
        //URL invURL = new URL("jar:"
        //        + BASE.toString()+"/InvalidJar.jar!/Test.class");
        
        URL invURL = createContent("InvalidJar.jar", "Test.class");        
        
        JarURLConnection juConn = (JarURLConnection) invURL.openConnection();
        try {
            juConn.getCertificates();
            fail("IOException was not thrown.");
        } catch(java.io.IOException io) {
            //expected
        }
    }
    
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getManifest",
      args = {}
    )
    public void test_getManifest() throws Exception {
        
        String [] expected = {"plus.bmp", "swt.dll"};
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/swt.dll");
        
        URL u = createContent("lf.jar", "swt.dll");
        
        juc = (JarURLConnection) u.openConnection();
        Manifest manifest = juc.getManifest();
        Map<String, Attributes> attr = manifest.getEntries();
        assertEquals(expected.length, attr.size());
        Set<String> keys = attr.keySet();
        String [] result = new String[expected.length];
        keys.toArray(result);

        for(int i = 0; i < result.length; i++) {
            assertEquals(expected[i], result[i]);
        }
        
        //URL invURL = new URL("jar:"
        //        + BASE.toString()+"/InvalidJar.jar!/Test.class");
        
        URL invURL = createContent("InvalidJar.jar", "Test.class");
        
        JarURLConnection juConn = (JarURLConnection) invURL.openConnection();
        try {
            juConn.getManifest();
            fail("IOException was not thrown.");
        } catch(java.io.IOException io) {
            //expected
        }
    }

    /**
     * @throws Exception 
     * @tests java.net.JarURLConnection#getEntryName()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getEntryName",
      args = {}
    )
    public void test_getEntryName() throws Exception {
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/plus.bmp");
        
        URL u = createContent("lf.jar", "plus.bmp");
        
        juc = (JarURLConnection) u.openConnection();
        assertEquals("Returned incorrect entryName", "plus.bmp", juc
                .getEntryName());
        
        //u = new URL("jar:" + BASE.toString()+"/lf.jar!/");
        
        u = createContent("lf.jar", "");
        
        juc = (JarURLConnection) u.openConnection();
        assertNull("Returned incorrect entryName", juc.getEntryName());
//      Regression test for harmony-3053
        
        URL url = new URL("jar:file:///bar.jar!/foo.jar!/Bugs/HelloWorld.class");
        assertEquals("foo.jar!/Bugs/HelloWorld.class",((JarURLConnection)url.openConnection()).getEntryName());
    }

    /**
     * @tests java.net.JarURLConnection#getJarEntry()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getJarEntry",
      args = {}
    )
    public void test_getJarEntry() throws Exception {
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/plus.bmp");
        
        URL u = createContent("lf.jar", "plus.bmp");
        
        juc = (JarURLConnection) u.openConnection();
        assertEquals("Returned incorrect JarEntry", "plus.bmp", juc
                .getJarEntry().getName());
        
        //u = new URL("jar:" + BASE.toString()+"/lf.jar!/");
        u = createContent("lf.jar", "");
        
        juc = (JarURLConnection) u.openConnection();
        assertNull("Returned incorrect JarEntry", juc.getJarEntry());
        
        //URL invURL = new URL("jar:"
        //        + BASE.toString()+"/InvalidJar.jar!/Test.class");
        
        URL invURL = createContent("InvalidJar.jar", "Test.class");
        
        JarURLConnection juConn = (JarURLConnection) invURL.openConnection();
        try {
            juConn.getJarEntry();
            fail("IOException was not thrown.");
        } catch(java.io.IOException io) {
            //expected
        }
    }

    /**
     * @tests java.net.JarURLConnection#getJarFile()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getJarFile",
      args = {}
    )
    public void test_getJarFile() throws MalformedURLException, IOException {
        URL url = null;
        //url = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/missing");
        
        url = createContent("lf.jar", "missing");

        JarURLConnection connection = null;
        connection = (JarURLConnection) url.openConnection();
        try {
            connection.connect();
            fail("Did not throw exception on connect");
        } catch (IOException e) {
            // expected
        }

        try {
            connection.getJarFile();
            fail("Did not throw exception after connect");
        } catch (IOException e) {
            // expected
        }
        
        //URL invURL = new URL("jar:"
        //        + BASE.toString()+"/InvalidJar.jar!/Test.class");
        
        URL invURL = createContent("InvalidJar.jar", "Test.class");
        
        JarURLConnection juConn = (JarURLConnection) invURL.openConnection();
        try {
            juConn.getJarFile();
            fail("IOException was not thrown.");
        } catch(java.io.IOException io) {
            //expected
        }
        
        File resources = Support_Resources.createTempFolder();

        Support_Resources.copyFile(resources, null, "hyts_att.jar");
        File file = new File(resources.toString() + "/hyts_att.jar");
        URL fUrl1 = new URL("jar:file:" + file.getPath() + "!/");
        JarURLConnection con1 = (JarURLConnection) fUrl1.openConnection();
        ZipFile jf1 = con1.getJarFile();
        JarURLConnection con2 = (JarURLConnection) fUrl1.openConnection();
        ZipFile jf2 = con2.getJarFile();
        assertTrue("file: JarFiles not the same", jf1 == jf2);
        jf1.close();
        assertTrue("File should exist", file.exists());
        
        fUrl1 = createContent("lf.jar", "");
        //new URL("jar:" + BASE.toString()+"/lf.jar!/");
        
        con1 = (JarURLConnection) fUrl1.openConnection();
        jf1 = con1.getJarFile();
        con2 = (JarURLConnection) fUrl1.openConnection();
        jf2 = con2.getJarFile();
        assertTrue("http: JarFiles not the same", jf1 == jf2);
        jf1.close();
    }

    /**
     * @tests java.net.JarURLConnection.getJarFile()
     * 
     * Regression test for HARMONY-29
     */
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "Regression test.",
      method = "getJarFile",
      args = {}
    )
    public void test_getJarFile29() throws Exception {
        File jarFile = File.createTempFile("1+2 3", "test.jar");
        jarFile.deleteOnExit();
        JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile));
        out.putNextEntry(new ZipEntry("test"));
        out.closeEntry();
        out.close();

        JarURLConnection conn = (JarURLConnection) new URL("jar:file:"
                + jarFile.getAbsolutePath().replaceAll(" ", "%20") + "!/")
                .openConnection();
        conn.getJarFile().entries();
    }
    
    //Regression for HARMONY-3436
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed.",
        method = "setUseCaches",
        args = {boolean.class}
    )
    public void test_setUseCaches() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "hyts_att.jar");
        File file = new File(resources.toString() + "/hyts_att.jar");
        URL url = new URL("jar:file:" + file.getPath() + "!/HasAttributes.txt");

        JarURLConnection connection = (JarURLConnection) url.openConnection();
        connection.setUseCaches(false);
        InputStream in = connection.getInputStream();
        in = connection.getInputStream();
        JarFile jarFile1 = connection.getJarFile();
        JarEntry jarEntry1 = connection.getJarEntry();
        in.read();   
        in.close();
        JarFile jarFile2 = connection.getJarFile();
        JarEntry jarEntry2 = connection.getJarEntry();
        assertSame(jarFile1, jarFile2);
        assertSame(jarEntry1, jarEntry2);
        
        try {
            connection.getInputStream();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * @tests java.net.JarURLConnection#getJarFileURL()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getJarFileURL",
      args = {}
    )
    public void test_getJarFileURL() throws Exception {
        //URL fileURL = new URL(BASE.toString()+"/lf.jar");
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/plus.bmp");
        
        URL u = createContent("lf.jar", "plus.bmp");
        
        URL fileURL = new URL(u.getPath().substring(0, u.getPath().indexOf("!")));
        
        juc = (JarURLConnection) u.openConnection();
        assertTrue("Returned incorrect file URL", juc.getJarFileURL().equals(
                fileURL));
        // Regression test for harmony-3053
        URL url = new URL("jar:file:///bar.jar!/foo.jar!/Bugs/HelloWorld.class");
        assertEquals("file:/bar.jar",((JarURLConnection)url.openConnection()).getJarFileURL().toString());
    }

    /**
     * @tests java.net.JarURLConnection#getMainAttributes()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getMainAttributes",
      args = {}
    )
    public void test_getMainAttributes() throws Exception {
        //URL u = new URL("jar:"
        //        + BASE.toString()+"/lf.jar!/swt.dll");
        URL u = createContent("lf.jar", "swt.dll");
        
        juc = (JarURLConnection) u.openConnection();
        java.util.jar.Attributes a = juc.getMainAttributes();
        assertEquals("Returned incorrect Attributes", "1.0", a
                .get(java.util.jar.Attributes.Name.MANIFEST_VERSION));
        
        //URL invURL = new URL("jar:"
        //        + BASE.toString()+"/InvalidJar.jar!/Test.class");
        URL invURL = createContent("InvalidJar.jar", "Test.class");
        
        JarURLConnection juConn = (JarURLConnection) invURL.openConnection();
        try {
            juConn.getMainAttributes();
            fail("IOException was not thrown.");
        } catch(java.io.IOException io) {
            //expected
        }
    }
    
    /**
     * @tests java.net.JarURLConnection#getInputStream()
     */
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "Test fails: IOException expected but IllegalStateException is thrown: ticket 128",
      method = "getInputStream",
      args = {}
    )
    public void test_getInputStream_DeleteJarFileUsingURLConnection()
            throws Exception {
        String jarFileName = "";
        String entry = "text.txt";
        String cts = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(cts);
        File jarFile = tmpDir.createTempFile("file", ".jar", tmpDir);
        jarFileName = jarFile.getPath();
        FileOutputStream jarFileOutputStream = new FileOutputStream(jarFileName);
        JarOutputStream out = new JarOutputStream(new BufferedOutputStream(
                jarFileOutputStream));
        JarEntry jarEntry = new JarEntry(entry);
        out.putNextEntry(jarEntry);
        out.write(new byte[] { 'a', 'b', 'c' });
        out.close();

        URL url = new URL("jar:file:" + jarFileName + "!/" + entry);
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        InputStream is = conn.getInputStream();
        is.close();
        
        assertTrue(jarFile.delete());
        
        /*
        try {
            conn.getInputStream();
            fail("Exception was not thrown.");
        } catch (IOException e) {
            //ok
        }
        
        */
        
    }

    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "JarURLConnection",
      args = {java.net.URL.class}
    )
    public void test_Constructor() {
        try {
            String jarFileName = "file.jar";
            String entry = "text.txt";
            URL url = new URL("jar:file:" + jarFileName + "!/" + entry);
            TestJarURLConnection jarConn = new TestJarURLConnection(url);
            assertEquals(new URL("file:file.jar"), jarConn.getJarFileURL());
        } catch(MalformedURLException me) {
            fail("MalformedURLException was thrown.");
        }
        
        try {
        URL [] urls = {new URL("file:file.jar"),  
                       new URL("http://foo.com/foo/foo.jar")}; 
        
        for(URL url:urls) {
            try {
                new TestJarURLConnection(url);
                fail("MalformedURLException was not thrown.");
            } catch(MalformedURLException me) {
                //expected
            }
        }
        } catch(MalformedURLException me) {
            fail("MalformedURLException was thrown.");
        }
    }
    
    
    protected void setUp() {
    }

    protected void tearDown() {
    }
    
    class TestJarURLConnection extends JarURLConnection {

        protected TestJarURLConnection(URL arg0) throws MalformedURLException {
            super(arg0);
        }

        @Override
        public JarFile getJarFile() throws IOException {
            return null;
        }

        @Override
        public void connect() throws IOException {
          
        }
        
    }
}

