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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MidiFileFormat {
    public static final int UNKNOWN_LENGTH = -1;

    protected int byteLength;

    protected float divisionType;

    protected long microsecondLength;

    protected int resolution;

    protected int type;
    
    private HashMap<String, Object> properties;

    public MidiFileFormat(int type, float divisionType, int resolution, int bytes,
            long microseconds) {
        this.type = type;
        this.divisionType = divisionType;
        this.resolution = resolution;
        this.byteLength = bytes;
        this.microsecondLength = microseconds;
        this.properties = new HashMap<String, Object>();
    }

    public MidiFileFormat(int type, float divisionType, int resolution, int bytes,
            long microseconds, Map<String, Object> properties) {
        this.type = type;
        this.divisionType = divisionType;
        this.resolution = resolution;
        this.byteLength = bytes;
        this.microsecondLength = microseconds;
        
        this.properties = new HashMap<String, Object>();
        this.properties.putAll(properties);
    }

    public int getByteLength() {
        return byteLength;
    }

    public float getDivisionType() {
        return divisionType;
    }

    public long getMicrosecondLength() {
        return microsecondLength;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public int getResolution() {
        return resolution;
    }

    public int getType() {
        return type;
    }

    public Map<String, Object> properties() {
        return Collections.unmodifiableMap(properties);
        
    }
}
