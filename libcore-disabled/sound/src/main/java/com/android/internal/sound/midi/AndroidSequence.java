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

package com.android.internal.sound.midi;

import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

/**
 * Implements a MIDI Sequence for Android. Its set of tracks etc. will always
 * be empty (at least we don't care about the contents). Instead, we store the
 * URL to the original MIDI data in it, so we can feed it into the Android
 * MediaPlayer.
 */
public class AndroidSequence extends Sequence {

    /**
     * Holds the URL to the MIDI data.
     */
    private URL url;
    
    /**
     * Creates a new AndroidSequence.
     * 
     * @param url The URL that points to the MIDI data.
     *  
     * @throws InvalidMidiDataException If the MIDI data is invalid (which we
     *         actually don't check at all).
     */
    public AndroidSequence(URL url) throws InvalidMidiDataException {
        super(0.0f, 1);
        
        this.url = url;
    }

    /**
     * Returns the URL pointing to the MIDI data.
     * 
     * @return The URL.
     */
    URL getURL() {
        return url;
    }

}
