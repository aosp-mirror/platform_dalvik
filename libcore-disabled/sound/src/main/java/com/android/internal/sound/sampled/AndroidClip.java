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

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Control.Type;

/**
 * Implements an audio Clip for Android. Since Android's MediaPlayer is somewhat
 * limited, we only support sample playback, but not recording or the querying
 * of sample information. Many of the methods hence throw
 * {@link java.lang.UnsupportedOperationException} or return dummy results.
 */
public class AndroidClip implements Clip {

    /**
     * Holds the Android MediaPlayer we use.
     */
    private MediaPlayer player;
    
    /**
     * Holds the AndroidAudioInputStream we want to play.
     */
    private AndroidAudioInputStream stream;
    
    public int getFrameLength() {
        throw new UnsupportedOperationException();
    }

    public long getMicrosecondLength() {
        throw new UnsupportedOperationException();
    }

    public void loop(int count) {
        throw new UnsupportedOperationException();
    }

    public void open(AudioFormat format, byte[] data, int offset, int bufferSize)
            throws LineUnavailableException {
        InputStream stream = new ByteArrayInputStream(data, offset, bufferSize);

        open();
        
        try {
            this.stream = (AndroidAudioInputStream)(new AndroidAudioFileReader().getAudioInputStream(stream));
        } catch (Exception ex) {
            throw new LineUnavailableException(ex.toString());
        }
    }

    public void open(AudioInputStream stream) throws LineUnavailableException, IOException {
        open();
        
        if (!(stream instanceof AndroidAudioInputStream)) {
            try {
                stream = new AndroidAudioFileReader().getAudioInputStream(stream);
            } catch (Exception ex) {
                throw new LineUnavailableException(ex.toString());
            }
        }
        
        this.stream = (AndroidAudioInputStream)stream;
    }

    public void setFramePosition(int frames) {
        throw new UnsupportedOperationException();
    }

    public void setLoopPoints(int start, int end) {
        throw new UnsupportedOperationException();
    }

    public void setMicrosecondPosition(long microseconds) {
        if (!isOpen()) {
            throw new IllegalStateException("Clip must be open");
        }
        
        player.seekTo((int)(microseconds / 1000));
    }

    public int available() {
        throw new UnsupportedOperationException();
    }

    public void drain() {
    }

    public void flush() {
    }

    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    public AudioFormat getFormat() {
        throw new UnsupportedOperationException();
    }

    public int getFramePosition() {
        throw new UnsupportedOperationException();
    }

    public float getLevel() {
        throw new UnsupportedOperationException();
    }

    public long getLongFramePosition() {
        throw new UnsupportedOperationException();
    }

    public long getMicrosecondPosition() {
        if (isOpen()) {
            return player.getCurrentPosition() * 1000;
        } else {
            return 0;
        }
    }

    public boolean isActive() {
        return false;
    }

    public boolean isRunning() {
        return player != null && player.isPlaying();
    }

    public void start() {
        if (!isOpen()) {
            throw new IllegalStateException("Clip must be open");
        }
        
        if (stream == null) {
            throw new IllegalStateException("Need an AudioInputStream to play");
        }

        if (!isRunning()) {
            /*
             * This is ugly, but there is no way around it: The javax.sound API
             * doesn't expect to throw an exception at this point for illegal
             * MIDI sequences. Since we don't really construct the MIDI sequence
             * from the original binary data, but only refer to its URL, the
             * MediaPlayer can actually bail out at this point. We wrap the
             * exception into a RuntimeException, to at least keep the API
             * contract.
             */
            try {
                String s = this.stream.getURL().toExternalForm();
                
                /*
                 * TODO Workaround for 1107794: MediaPlayer doesn't handle
                 * "file:" URLs. Get rid of this.
                 */
                if (s.startsWith("file:")) {
                    s = s.substring(5);
                }
                
                player.setDataSource(s);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepare();
            } catch (IOException ex) {
                throw new RuntimeException(ex.toString());
            }
            
            player.start();
        }
    }

    public void stop() {
        if (!isOpen()) {
            throw new IllegalStateException("Clip must be open");
        }
        
        if (isRunning()) {
            player.stop();
        }
    }

    public void addLineListener(LineListener listener) {
        throw new UnsupportedOperationException();
    }

    public void close() {
        if (isOpen()) {
            stop();
            player = null;
        }
    }

    public Control getControl(Type control) {
        throw new IllegalArgumentException("No controls available");
    }

    public Control[] getControls() {
        return new Control[0];
    }

    public javax.sound.sampled.Line.Info getLineInfo() {
        return new Line.Info(this.getClass());
    }

    public boolean isControlSupported(Type control) {
        return false;
    }

    public boolean isOpen() {
        return player != null;
    }

    public void open() throws LineUnavailableException {
        try {
            player = new MediaPlayer();
        } catch (Exception ex) {
            throw new LineUnavailableException(ex.toString());
        }
    }

    public void removeLineListener(LineListener listener) {
        throw new UnsupportedOperationException();
    }

}
