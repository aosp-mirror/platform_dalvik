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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;
import javax.sound.midi.spi.MidiFileReader;

/**
 * Implements a MidiFileReader for Android. We need to cache data coming from an
 * arbitrary InputStream, since the Android MediaPlayer expects us to pass in a
 * file or a URL.
 */
public class AndroidMidiFileReader extends MidiFileReader {

    @Override
    public MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException,
            IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
        return new AndroidSequence(file.toURL());
    }

    @Override
    public Sequence getSequence(InputStream stream) throws InvalidMidiDataException, IOException {
        File file = File.createTempFile("javax.sound.midi-", null);
        file.deleteOnExit();

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buffer = new byte[1024];

        int count = stream.read(buffer);
        while (count >= 0) {
            out.write(buffer, 0, count);
            count = stream.read(buffer);
        }

        out.flush();
        out.close();

        return getSequence(file);
    }

    @Override
    public Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
        return new AndroidSequence(url);
    }

}
