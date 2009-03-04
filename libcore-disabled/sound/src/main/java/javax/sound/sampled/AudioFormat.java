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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AudioFormat {
    public static class Encoding {

        public static final Encoding ALAW = new Encoding("ALAW"); //$NON-NLS-1$

        public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED"); //$NON-NLS-1$

        public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED"); //$NON-NLS-1$

        public static final Encoding ULAW = new Encoding("ULAW"); //$NON-NLS-1$

        private String name;

        public Encoding(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object another) {
            if (this == another) {
                return true;
            }

            if (another == null || !(another instanceof Encoding)) {
                return false;
            }

            Encoding obj = (Encoding) another;
            return name == null ? obj.name == null : name.equals(obj.name);
        }

        @Override
        public final int hashCode() {
            return name == null ? 0 : name.hashCode();
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    protected boolean bigEndian;

    protected int channels;

    protected Encoding encoding;

    protected float frameRate;

    protected int frameSize;

    protected float sampleRate;

    protected int sampleSizeInBits;

    private HashMap<String, Object> prop;

    public AudioFormat(AudioFormat.Encoding encoding,
            float sampleRate,
            int sampleSizeInBits,
            int channels,
            int frameSize,
            float frameRate,
            boolean bigEndian) {

        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;

    }

    public AudioFormat(AudioFormat.Encoding encoding,
            float sampleRate,
            int sampleSizeInBits,
            int channels,
            int frameSize,
            float frameRate,
            boolean bigEndian,
            Map<String,Object> properties) {

        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
        prop = new HashMap<String, Object>();
        prop.putAll(properties);

    }

    public AudioFormat(float sampleRate,
            int sampleSizeInBits,
            int channels,
            boolean signed,
            boolean bigEndian) {

        this.encoding = (signed?  Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED);
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = sampleSizeInBits >> 3;
        if ((sampleSizeInBits & 0x7) != 0) {
            this.frameSize++;
        }
        this.frameSize *= channels;
        this.frameRate = sampleRate;
        this.bigEndian = bigEndian;

    }

    public Encoding getEncoding() {
        return encoding;
    }

    public float getSampleRate() {
        return sampleRate;
    }
    
    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public Map<String,Object> properties() {
        if (prop != null) {
            return Collections.unmodifiableMap(prop);
        } else {
            return Collections.emptyMap();
        }
    }

    public Object getProperty(String key) {
        if (prop == null) {
            return null;
        }
        return prop.get(key);
    }

    public boolean matches(AudioFormat format) {
        if (!encoding.equals(format.getEncoding()) ||
                channels != format.getChannels() ||
                sampleSizeInBits != format.getSampleSizeInBits() ||
                frameSize != format.getFrameSize()) {
            return false;
        }
        if (format.getSampleRate() != AudioSystem.NOT_SPECIFIED &&
                sampleRate != format.getSampleRate()) {
            return false;
        }
        
        if (format.getFrameRate() != AudioSystem.NOT_SPECIFIED &&
                frameRate != format.getFrameRate()) {
            return false;
        }
        
        if ((sampleSizeInBits > 8) 
                && (bigEndian != format.isBigEndian())) {
            return false;
        }
        return true;
        
    }

    public String toString() {

        String ch;
        switch (channels) {
        case 1:
            ch = "mono,"; //$NON-NLS-1$
            break;
        case 2:
            ch = "stereo,"; //$NON-NLS-1$
        default:
            ch = channels + " channels, "; //$NON-NLS-1$
        }       

        return encoding + " " + sampleRate + " Hz, " + sampleSizeInBits + " bit, " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + ch + frameSize + " bytes/frame, " + frameRate + " frames/second"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
}
