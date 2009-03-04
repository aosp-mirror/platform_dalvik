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

package javax.sound.midi;

import java.util.List;

public interface MidiDevice {
    class Info {
        private String name;

        private String vendor;

        private String description;

        private String version;

        protected Info(String name, String vendor, String description, String version) {
            this.name = name;
            this.vendor = vendor;
            this.description = description;
            this.version = version;
        }

        /*
         * returns true when objects are the same
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public final boolean equals(Object obj) {
            return this == obj;
        }

        public final String getDescription() {
            return description;
        }

        public final String getName() {
            return name;
        }

        public final String getVendor() {
            return vendor;
        }

        public final String getVersion() {
            return version;
        }

        @Override
        public final int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + ((description == null) ? 0 : description.hashCode());
            result = PRIME * result + ((name == null) ? 0 : name.hashCode());
            result = PRIME * result + ((vendor == null) ? 0 : vendor.hashCode());
            result = PRIME * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    void close();

    MidiDevice.Info getDeviceInfo();

    int getMaxReceivers();

    int getMaxTransmitters();

    long getMicrosecondPosition();

    Receiver getReceiver() throws MidiUnavailableException;

    List<Receiver> getReceivers();

    Transmitter getTransmitter() throws MidiUnavailableException;

    List<Transmitter> getTransmitters();

    boolean isOpen();

    void open() throws MidiUnavailableException;
}
