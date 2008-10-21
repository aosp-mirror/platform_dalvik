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
package tests.support;


import java.net.NetworkInterface;

public class Support_NetworkInterface {

    /**
     * On windows platforms with IPV6 enabled there are a number of pseudo
     * interfaces which don't work with our tests. This function is called to
     * make sure we only use the non-pseudo interfaces
     */
    public static boolean useInterface(NetworkInterface theInterface) {
        boolean result = true;
        String platform = System.getProperty("os.name");
        // only use these on windows platforms
        if (platform.startsWith("Windows")) {
            if ((theInterface.getDisplayName()
                    .equals("Teredo Tunneling Pseudo-Interface"))
                    || (theInterface.getDisplayName()
                            .equals("6to4 Tunneling Pseudo-Interface"))
                    || (theInterface.getDisplayName()
                            .equals("Automatic Tunneling Pseudo-Interface"))
                    || (theInterface.getDisplayName()
                            .equals("Loopback Pseudo-Interface"))) {
                result = false;
            }
        }
        return result;
    }
}
