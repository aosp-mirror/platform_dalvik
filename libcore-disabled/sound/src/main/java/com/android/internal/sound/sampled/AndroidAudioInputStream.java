/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.sound.sampled;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * Implements an AudioInputStream for Android. Its internal InputStream is
 * unused (at least we don't care about the contents). Instead, we store the
 * URL to the original audio data in it, so we can feed it into the Android
 * MediaPlayer.
 */
public class AndroidAudioInputStream extends AudioInputStream {

    /**
     * Holds the URL to the MIDI data.
     */
    private URL url;
    
    /**
     * Creates a new AndroidAudioInputStream.
     * 
     * @param url The URL that points to the audio data.
     */
    public AndroidAudioInputStream(URL url) {
        super(null, new AudioFormat(0.0f, 0, 0, false, false), 0);
        
        this.url = url;
    }

    /**
     * Returns the URL pointing to the audio data.
     * 
     * @return The URL.
     */
    URL getURL() {
        return url;
    }
 
}
