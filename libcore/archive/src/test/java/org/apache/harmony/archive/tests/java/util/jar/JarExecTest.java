/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.archive.tests.java.util.jar;

import static tests.support.Support_Exec.execAndGetOutput;
import static tests.support.Support_Exec.javaProcessBuilder;
import tests.support.resource.Support_Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * 
 * tests for various cases of java -jar ... execution
 * 
 */

public class JarExecTest extends junit.framework.TestCase {
    /**
     * regression test for HARMONY-1562 issue
     * 
     */
    public void test_1562() throws Exception {
        // create the manifest
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");

        File outputJar = File.createTempFile("hyts_", ".jar");
        outputJar.deleteOnExit();
        JarOutputStream jout = new JarOutputStream(new FileOutputStream(
                outputJar), man);
        File resources = Support_Resources.createTempFolder();

        for (String jarClass : new String[] {"Foo", "Bar"}) {
            jout.putNextEntry(new JarEntry("foo/bar/execjartest/" + jarClass
                    + ".class"));
            jout.write(getResource(resources, "hyts_" + jarClass + ".ser"));
        }

        jout.close();

        // execute the JAR and read the result
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-jar");
        builder.command().add(outputJar.getAbsolutePath());
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith("FOOBAR"));
    }

    /**
     * tests Class-Path entry in manifest
     * 
     * @throws Exception in case of troubles
     */
    public void test_jar_class_path() throws Exception {
        File fooJar = File.createTempFile("hyts_", ".jar");
        File barJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();
        barJar.deleteOnExit();

        // create the manifest
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barJar.getName());

        File resources = Support_Resources.createTempFolder();

        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(
                fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();

        JarOutputStream joutBar = new JarOutputStream(new FileOutputStream(
                barJar));
        joutBar.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        joutBar.write(getResource(resources, "hyts_Bar.ser"));
        joutBar.close();

        // execute the JAR and read the result
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-jar");
        builder.command().add(fooJar.getAbsolutePath());
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith("FOOBAR"));

        // rewrite manifest so it contains not only reference to bar but useless
        // entries as well
        att.put(Attributes.Name.CLASS_PATH, "xx yy zz " + barJar.getName());
        joutFoo = new JarOutputStream(new FileOutputStream(fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        // execute the JAR and read the result
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith( "FOOBAR"));

        // play with relative file names - put relative path as ../<parent dir
        // name>/xx.jar
        att.put(Attributes.Name.CLASS_PATH, ".." + File.separator
                + barJar.getParentFile().getName() + File.separator
                + barJar.getName());
        joutFoo = new JarOutputStream(new FileOutputStream(fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        // execute the JAR and read the result
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith( "FOOBAR"));
    }

    /**
     * tests case when Main-Class is not in the jar launched but in another jar
     * referenced by Class-Path
     * 
     * @throws Exception in case of troubles
     */
    public void test_main_class_in_another_jar() throws Exception {
        File fooJar = File.createTempFile("hyts_", ".jar");
        File barJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();
        barJar.deleteOnExit();

        // create the manifest
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, fooJar.getName());

        File resources = Support_Resources.createTempFolder();

        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(
                fooJar));
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();

        JarOutputStream joutBar = new JarOutputStream(new FileOutputStream(
                barJar), man);
        joutBar.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        joutBar.write(getResource(resources, "hyts_Bar.ser"));
        joutBar.close();

        // execute the JAR and read the result
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("-jar");
        builder.command().add(barJar.getAbsolutePath());
        assertTrue("Error executing JAR",
                execAndGetOutput(builder).startsWith("FOOBAR"));
    }

    public void test_classpath() throws Exception {
        File resources = Support_Resources.createTempFolder();

        File fooJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();

        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(
                fooJar));
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        joutFoo.write(getResource(resources, "hyts_Bar.ser"));
        joutFoo.close();

        // execute the JAR and read the result
        ProcessBuilder builder = javaProcessBuilder();
        builder.environment().put("CLASSPATH", fooJar.getAbsolutePath());
        builder.command().add("foo.bar.execjartest.Foo");

        assertTrue("Error executing class from ClassPath",
                execAndGetOutput(builder).startsWith("FOOBAR"));

        // ok - next try - add -cp to path - it should override env
        File booJar = File.createTempFile("hyts_", ".jar");
        booJar.deleteOnExit();

        JarOutputStream joutBoo = new JarOutputStream(new FileOutputStream(
                booJar));
        joutBoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        String booBody = new String(getResource(resources, "hyts_Foo.ser"),
                "iso-8859-1");
        booBody = booBody.replaceFirst("FOO", "BOO");
        joutBoo.write(booBody.getBytes("iso-8859-1"));
        joutBoo.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        String farBody = new String(getResource(resources, "hyts_Bar.ser"),
                "iso-8859-1");
        farBody = farBody.replaceFirst("BAR", "FAR");
        joutBoo.write(farBody.getBytes("iso-8859-1"));
        joutBoo.close();

        builder = javaProcessBuilder();
        builder.environment().put("CLASSPATH", fooJar.getAbsolutePath());
        builder.command().add("-cp");
        builder.command().add(booJar.getAbsolutePath());
        builder.command().add("foo.bar.execjartest.Foo");

        assertTrue("Error executing class specified by -cp",
                execAndGetOutput(builder).startsWith("BOOFAR"));

        // now add -jar option - it should override env and classpath
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");

        File zooJar = File.createTempFile("hyts_", ".jar");
        zooJar.deleteOnExit();

        JarOutputStream joutZoo = new JarOutputStream(new FileOutputStream(
                zooJar), man);
        joutZoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        String zooBody = new String(getResource(resources, "hyts_Foo.ser"),
                "iso-8859-1");
        zooBody = zooBody.replaceFirst("FOO", "ZOO");
        joutZoo.write(zooBody.getBytes("iso-8859-1"));
        joutZoo.putNextEntry(new JarEntry("foo/bar/execjartest/Bar.class"));
        String zarBody = new String(getResource(resources, "hyts_Bar.ser"),
                "iso-8859-1");
        zarBody = zarBody.replaceFirst("BAR", "ZAR");
        joutZoo.write(zarBody.getBytes("iso-8859-1"));
        joutZoo.close();

        builder = javaProcessBuilder();
        builder.environment().put("CLASSPATH", fooJar.getAbsolutePath());
        builder.command().add("-cp");
        builder.command().add(booJar.getAbsolutePath());
        builder.command().add("-jar");
        builder.command().add(zooJar.getAbsolutePath());

        assertTrue("Error executing class specified by -jar",
                execAndGetOutput(builder).startsWith("ZOOZAR"));
    }

    private static byte[] getResource(File tempDir, String resourceName)
            throws IOException {
        Support_Resources.copyFile(tempDir, null, resourceName);
        File resourceFile = new File(tempDir, resourceName);
        resourceFile.deleteOnExit();

        // read whole resource data into memory
        byte[] resourceBody = new byte[(int) resourceFile.length()];
        FileInputStream fis = new FileInputStream(resourceFile);
        fis.read(resourceBody);
        fis.close();

        return resourceBody;
    }

}
