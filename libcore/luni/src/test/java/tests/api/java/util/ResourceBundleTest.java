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

package tests.api.java.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import tests.support.resource.Support_Resources;
import tests.api.java.util.support.B;

public class ResourceBundleTest extends junit.framework.TestCase {

    /**
     * @tests java.util.ResourceBundle#getBundle(java.lang.String,
     *        java.util.Locale)
     */
    public void test_getBundleLjava_lang_StringLjava_util_Locale() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale defLocale = Locale.getDefault();

        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        assertEquals("Wrong bundle fr_FR_VAR", "frFRVARValue4", bundle.getString("parent4")
                );
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "v1"));
        assertEquals("Wrong bundle fr_FR_v1", 
                "frFRValue4", bundle.getString("parent4"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "US", "VAR"));
        assertEquals("Wrong bundle fr_US_var", "frValue4", bundle.getString("parent4")
                );
        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "VAR"));
        assertEquals("Wrong bundle de_FR_var", "enUSValue4", bundle.getString("parent4")
                );

        Locale.setDefault(new Locale("fr", "FR", "VAR"));
        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "v1"));
        assertEquals("Wrong bundle de_FR_var 2", "frFRVARValue4", bundle.getString("parent4")
                );

        Locale.setDefault(new Locale("de", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("de", "FR", "var"));
        assertEquals("Wrong bundle de_FR_var 2", "parentValue4", bundle.getString("parent4")
                );

        // Test with a security manager
        Locale.setDefault(new Locale("en", "US"));
        System.setSecurityManager(new SecurityManager());
        try {
            bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR",
                    "VAR"));
            assertEquals("Security: Wrong bundle fr_FR_VAR", "frFRVARValue4", bundle.getString(
                    "parent4"));
            bundle = ResourceBundle.getBundle(name,
                    new Locale("fr", "FR", "v1"));
            assertEquals("Security: Wrong bundle fr_FR_v1", "frFRValue4", bundle.getString(
                    "parent4"));
            bundle = ResourceBundle.getBundle(name, new Locale("fr", "US",
                    "VAR"));
            assertEquals("Security: Wrong bundle fr_US_var", "frValue4", bundle.getString(
                    "parent4"));
            bundle = ResourceBundle.getBundle(name, new Locale("de", "FR",
                    "VAR"));
            assertTrue("Security: Wrong bundle de_FR_var: "
                    + bundle.getString("parent4"), bundle.getString("parent4")
                    .equals("enUSValue4"));
        } finally {
            System.setSecurityManager(null);
        }

        Locale.setDefault(defLocale);
    }

    /**
     * @tests java.util.ResourceBundle#getBundle(java.lang.String,
     *        java.util.Locale, java.lang.ClassLoader)
     */
    public void test_getBundleLjava_lang_StringLjava_util_LocaleLjava_lang_ClassLoader() {
        String classPath = System.getProperty("java.class.path");
        StringTokenizer tok = new StringTokenizer(classPath, File.pathSeparator);
        Vector urlVec = new Vector();
        String resPackage = Support_Resources.RESOURCE_PACKAGE;
        try {
            while (tok.hasMoreTokens()) {
                String path = (String) tok.nextToken();
                String url;
                if (new File(path).isDirectory())
                    url = "file:" + path + resPackage + "subfolder/";
                else
                    url = "jar:file:" + path + "!" + resPackage + "subfolder/";
                urlVec.addElement(new URL(url));
            }
        } catch (MalformedURLException e) {
        }
        URL[] urls = new URL[urlVec.size()];
        for (int i = 0; i < urlVec.size(); i++)
            urls[i] = (URL) urlVec.elementAt(i);
        URLClassLoader loader = new URLClassLoader(urls, null);

        String name = Support_Resources.RESOURCE_PACKAGE_NAME
                + ".hyts_resource";
        ResourceBundle bundle = ResourceBundle.getBundle(name, Locale
                .getDefault());
            assertEquals("Wrong value read", "parent", bundle.getString("property"));
        bundle = ResourceBundle.getBundle(name, Locale.getDefault(), loader);
        assertEquals("Wrong cached value", 
                "resource", bundle.getString("property"));

        // Regression test for Harmony-3823
        B bb = new B();
        String s = bb.find("nonexistent");
        s = bb.find("name");
        assertEquals("Wrong property got", "Name", s);
    }

    /**
     * @tests java.util.ResourceBundle#getString(java.lang.String)
     */
    public void test_getStringLjava_lang_String() {
        ResourceBundle bundle;
        String name = "tests.support.Support_TestResource";
        Locale.setDefault(new Locale("en", "US"));
        bundle = ResourceBundle.getBundle(name, new Locale("fr", "FR", "VAR"));
        assertEquals("Wrong value parent4", 
                "frFRVARValue4", bundle.getString("parent4"));
        assertEquals("Wrong value parent3", 
                "frFRValue3", bundle.getString("parent3"));
        assertEquals("Wrong value parent2", 
                "frValue2", bundle.getString("parent2"));
        assertEquals("Wrong value parent1", 
                "parentValue1", bundle.getString("parent1"));
        assertEquals("Wrong value child3", 
                "frFRVARChildValue3", bundle.getString("child3"));
        assertEquals("Wrong value child2", 
                "frFRVARChildValue2", bundle.getString("child2"));
        assertEquals("Wrong value child1", 
                "frFRVARChildValue1", bundle.getString("child1"));
    }

    public void test_getBundle_getClassName() {
        // Regression test for Harmony-1759
        Locale locale = Locale.GERMAN;
        String nonExistentBundle = "Non-ExistentBundle";
        try {
            ResourceBundle.getBundle(nonExistentBundle, locale, this.getClass()
                    .getClassLoader());
            fail("MissingResourceException expected!");
        } catch (MissingResourceException e) {
            assertEquals(nonExistentBundle + "_" + locale, e.getClassName());
        }
        
        try {
            ResourceBundle.getBundle(nonExistentBundle, locale);
            fail("MissingResourceException expected!");
        } catch (MissingResourceException e) {
            assertEquals(nonExistentBundle + "_" + locale, e.getClassName());
        }

        locale = Locale.getDefault();
        try {
            ResourceBundle.getBundle(nonExistentBundle);
            fail("MissingResourceException expected!");
        } catch (MissingResourceException e) {
            assertEquals(nonExistentBundle + "_" + locale, e.getClassName());
        }

    }

    protected void setUp() {
    }

    protected void tearDown() {
    }
}
