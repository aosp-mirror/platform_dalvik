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

package org.apache.harmony.sql.tests.java.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TestHelper_ClassLoader extends ClassLoader {

    public TestHelper_ClassLoader() {
        super(null);
    }

    /**
     * Loads a class specified by its name
     * <p>
     * This classloader makes the assumption that any class it is asked to load
     * is in the current directory....
     */
    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        Class<?> theClass = null;

        if (!className.equals("org.apache.harmony.sql.tests.java.sql.TestHelper_DriverManager")) {
            return null;
        }

        String classNameAsFile = className.replace('.', '/') + ".class";
        // System.out.println("findClass - class filename = " + classNameAsFile
        // );

        String classPath = System.getProperty("java.class.path");
        // System.out.println("Test class loader - classpath = " + classPath );

        String theSeparator = String.valueOf(File.pathSeparatorChar);
        String[] theClassPaths = classPath.split(theSeparator);
        for (int i = 0; (i < theClassPaths.length) && (theClass == null); i++) {
            // Ignore jar files...
            if (theClassPaths[i].endsWith(".jar")) {
                theClass = loadClassFromJar(theClassPaths[i], className,
                        classNameAsFile);
            } else {
                theClass = loadClassFromFile(theClassPaths[i], className,
                        classNameAsFile);
            } // end if
        } // end for

        return theClass;
    } // end method findClass( String )

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        // Allowed classes:
        String[] disallowedClasses = { "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver1",
                "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver2",
                "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver4",
                "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver5" };

        Class<?> theClass;

        theClass = findLoadedClass(className);
        if (theClass != null) {
            return theClass;
        }

        theClass = this.findClass(className);

        if (theClass == null) {
            for (String element : disallowedClasses) {
                if (element.equals(className)) {
                    return null;
                } // end if
            } // end for
            theClass = Class.forName(className);
        } // end if

        return theClass;
    } // end method loadClass( String )

    private Class<?> loadClassFromFile(String pathName, String className,
            String classNameAsFile) {
        Class<?> theClass = null;
        FileInputStream theInput = null;
        File theFile = null;
        try {
            theFile = new File(pathName, classNameAsFile);
            if (theFile.exists()) {
                int length = (int) theFile.length();
                theInput = new FileInputStream(theFile);
                byte[] theBytes = new byte[length + 100];
                int dataRead = 0;
                while (dataRead < length) {
                    int count = theInput.read(theBytes, dataRead,
                            theBytes.length - dataRead);
                    if (count == -1) {
                        break;
                    }
                    dataRead += count;
                }

                if (dataRead > 0) {
                    // Create the class from the bytes read in...
                    theClass = this.defineClass(className, theBytes, 0, dataRead);
                    ClassLoader testClassLoader = theClass.getClassLoader();
                    if (testClassLoader != this) {
                        System.out.println("findClass - wrong classloader!!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("findClass - exception reading class file.");
            e.printStackTrace();
        } finally {
            try {
                if (theInput != null) {
                    theInput.close();
                }
            } catch (Exception e) {
            }
        }
        return theClass;
    }

    /*
     * Loads a named class from a specified JAR file
     */
    private Class<?> loadClassFromJar(String jarfileName, String className,
            String classNameAsFile) {
        Class<?> theClass = null;

        // First, try to open the Jar file
        JarFile theJar = null;
        try {
            theJar = new JarFile(jarfileName);
            JarEntry theEntry = theJar.getJarEntry(classNameAsFile);

            if (theEntry == null) {
                // System.out.println("TestHelper_Classloader - did not find
                // class file in Jar " + jarfileName );
                return theClass;
            } // end if

            theEntry.getMethod();
            InputStream theStream = theJar.getInputStream(theEntry);

            long size = theEntry.getSize();
            if (size < 0) {
                size = 100000;
            }
            byte[] theBytes = new byte[(int) size + 100];

            int dataRead = 0;
            while (dataRead < size) {
                int count = theStream.read(theBytes, dataRead, theBytes.length
                        - dataRead);
                if (count == -1) {
                    break;
                }
                dataRead += count;
            } // end while

            // System.out.println("loadClassFromJar: read " + dataRead + " bytes
            // from class file");
            if (dataRead > 0) {
                // Create the class from the bytes read in...
                theClass = this.defineClass(className, theBytes, 0, dataRead);
                /* System.out.println("findClass: created Class object."); */
                ClassLoader testClassLoader = theClass.getClassLoader();
                if (testClassLoader != this) {
                    System.out.println("findClass - wrong classloader!!");
                } else {
                    System.out
                            .println("Testclassloader loaded class from jar: "
                                    + className);
                } // end if
            } // end if
        } catch (IOException ie) {
            System.out
                    .println("TestHelper_ClassLoader: IOException opening Jar "
                            + jarfileName);
        } catch (Exception e) {
            System.out
                    .println("TestHelper_ClassLoader: Exception loading class from Jar ");
        } catch (ClassFormatError ce) {
            System.out
                    .println("TestHelper_ClassLoader: ClassFormatException loading class from Jar ");
        } finally {
            try {
                if (theJar != null) {
                    theJar.close();
                }
            } catch (Exception e) {
            } // end try
        } // end try

        return theClass;
    } // end method loadClassFromJar(

} // end class TestHelper_ClassLoader

