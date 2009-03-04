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

package org.apache.harmony.sound.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class ProviderService {

    // Properties from sound.propertie file
    private static Properties devices;

    static {
        devices = new Properties();

        FileInputStream fstream = AccessController
                .doPrivileged(new PrivilegedAction<FileInputStream>() {

                    public FileInputStream run() {
                        // obtain the path to the file sound.properties
                        String soundPropertiesPath = System
                                .getProperty("java.home") //$NON-NLS-1$
                                + File.separator + "lib" + File.separator //$NON-NLS-1$
                                + "sound.properties"; //$NON-NLS-1$
                        try {
                            return new FileInputStream(soundPropertiesPath);
                        } catch (FileNotFoundException e) {
                            return null;
                        }
                    }
                });

        if (fstream != null) {
            // reading file sound.properties
            try {
                devices.load(fstream);
            } catch (IOException e) {}
        }
    }

    /**
     * this method return information about default device
     * 
     * @param deviceName
     * @return
     */
    public static List<String> getDefaultDeviceDescription(String deviceName) {

        // variable that contain information about default device
        List<String> defaultDevice = new ArrayList<String>();
        String str;
        int index;

        /*
         * obtain the default device that describes by deviceName
         */
        str = devices.getProperty(deviceName);
        /*
         * if default device doesn't define, than return empty defaultDevice
         */
        if (str == null) {
            return defaultDevice;
        }
        /*
         * the separator between provider and name is '#'; find separator of
         * provider and name of device in the notation of default device
         */
        index = str.indexOf("#"); //$NON-NLS-1$
        /*
         * if separator doesn't find, so in the definition of default device
         * contain only name of provider, and so we add it
         */
        if (index == -1) {
            defaultDevice.add(str);
            defaultDevice.add(null);
            /*
             * if separator is the first symbol, so definition contain only name
             * of device
             */
        } else if (index == 0) {
            defaultDevice.add(null);
            defaultDevice.add(str.substring(index + 1));
            /*
             * if separator is not the first, so we find provider and name of
             * device
             */
        } else {
            defaultDevice.add(str.substring(0, index));
            defaultDevice.add(str.substring(index + 1));
        }
        return defaultDevice;
    }

    /**
     * this method return the list of providers
     * 
     * @param providerName
     * @return
     */
    public static List<?> getProviders(String providerName) {
        final String name = providerName;

        return AccessController
                .doPrivileged(new PrivilegedAction<List<Object>>() {

                    public List<Object> run() {
                        List<Object> providers = new ArrayList<Object>();
                        String className = null;
                        byte[] bytes;

                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        Enumeration<URL> urls = null;
                        try {
                            urls = cl.getResources(name);
                        } catch (IOException e) {
                            return providers;
                        }
                        for (; urls.hasMoreElements();) {
                            try {
                                InputStream in = urls.nextElement()
                                        .openStream();
                                bytes = new byte[in.available()];
                                in.read(bytes);
                                in.close();
                            } catch (IOException e) {
                                continue;
                            }
                            String[] astr = new String(bytes).split("\r\n"); //$NON-NLS-1$
                            for (String str : astr) {
                                className = str.trim();
                                if (!className.startsWith("#")) { // skip
                                                                    // comments
                                                                    // //$NON-NLS-1$
                                    try {
                                        providers.add(Class.forName(
                                                className.trim(), true, cl)
                                                .newInstance());
                                    } catch (IllegalAccessException e) {} catch (InstantiationException e) {} catch (ClassNotFoundException e) {}
                                }
                            }
                        }
                        return providers;
                    }
                });

    }

    public static Properties getSoundProperties() {
        return devices;
    }

}
