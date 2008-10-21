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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

/**
 * Implements an AudioFileReader for Android. We need to cache data coming from
 * an arbitrary InputStream, since the Android MediaPlayer expects us to pass in
 * a file or URL.
 */
public class AndroidAudioFileReader extends AudioFileReader {

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException,
            IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException,
            IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException,
            IOException {
        return new AndroidAudioInputStream(file.toURL());
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        File file = File.createTempFile("javax.sound.sampled-", null);
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

        return getAudioInputStream(file);
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException,
            IOException {
        return new AndroidAudioInputStream(url);
    }

}
