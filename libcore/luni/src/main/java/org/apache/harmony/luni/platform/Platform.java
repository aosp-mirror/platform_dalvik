/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.luni.platform;


import org.apache.harmony.kernel.vm.VM;

/**
 * Platform
 *  
 */
public class Platform {

    static final IAdapterManager ADAPTER_MANAGER = new AdapterManager();

    static final IFileSystem FILE_SYSTEM = OSComponentFactory.getFileSystem();

    static final IMemorySystem MEMORY_SYSTEM = OSComponentFactory
            .getMemorySystem();

    static final INetworkSystem NETWORK_SYSTEM = OSComponentFactory
            .getNetworkSystem();

    public static IAdapterManager getAdapterManager() {
        return ADAPTER_MANAGER;
    }

    private static final void accessCheck() {
        if (VM.callerClassLoader() != null) {
            throw new SecurityException();
        }
    }

    public static IFileSystem getFileSystem() {
        accessCheck();
        return FILE_SYSTEM;
    }

    public static IMemorySystem getMemorySystem() {
        accessCheck();
        return MEMORY_SYSTEM;
    }

    public static INetworkSystem getNetworkSystem() {
        accessCheck();
        return NETWORK_SYSTEM;
    }
}
