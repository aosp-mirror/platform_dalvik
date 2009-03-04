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

package javax.sound.sampled;

public interface Mixer extends Line {

    public static class Info {
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
        
        @Override
        public final boolean equals(Object another) {
            return this == another;
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
            return name.hashCode() + vendor.hashCode() + description.hashCode() + version.hashCode();
        }
        
        @Override
        public final String toString() {
            return name + ", version " + version; //$NON-NLS-1$
        }
    }

    Line getLine(Line.Info info) throws LineUnavailableException;
    
    int getMaxLines(Line.Info info);
    
    Mixer.Info getMixerInfo();
    
    Line.Info[] getSourceLineInfo();
    
    Line.Info[] getSourceLineInfo(Line.Info info);
    
    Line[] getSourceLines();
    
    Line.Info[] getTargetLineInfo();
    
    Line.Info[] getTargetLineInfo(Line.Info info);
    
    Line[] getTargetLines();
    
    boolean isLineSupported(Line.Info info);
    
    boolean isSynchronizationSupported(Line[] lines, boolean maintainSync);
    
    void synchronize(Line[] lines, boolean maintainSync);

    void unsynchronize(Line[] lines);
}
