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

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

/**
 * Implements a MIDI Sequencer for Android. Since Android's MediaPlayer is
 * somewhat limited, we only support MIDI playback, but not recording or the
 * querying of MIDI information. Many of the methods hence throw
 * {@link java.lang.UnsupportedOperationException} or return dummy results.
 */
public class AndroidSequencer implements Sequencer {

    /**
     * Defines the DeviceInfo for our AndroidSequencer.
     */
    private class Info extends MidiDevice.Info {
        public Info() {
            super("Android Sequencer", "Android Sequencer", "The Android Project", "1.0");        
        }
    }
    
    /**
     * Holds the Android MediaPlayer we use.
     */
    private MediaPlayer player;
    
    /**
     * Holds the Android Sequence we want to play.
     */
    private AndroidSequence sequence;

    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
        throw new UnsupportedOperationException();
    }

    public boolean addMetaEventListener(MetaEventListener listener) {
        throw new UnsupportedOperationException();
    }

    public int getLoopCount() {
        throw new UnsupportedOperationException();
    }

    public long getLoopEndPoint() {
        throw new UnsupportedOperationException();
    }

    public long getLoopStartPoint() {
        throw new UnsupportedOperationException();
    }

    public SyncMode getMasterSyncMode() {
        throw new UnsupportedOperationException();
    }

    public SyncMode[] getMasterSyncModes() {
        throw new UnsupportedOperationException();
    }

    public long getMicrosecondLength() {
        throw new UnsupportedOperationException();
    }

    public long getMicrosecondPosition() {
        throw new UnsupportedOperationException();
    }

    public Sequence getSequence() {
        return sequence;
    }

    public SyncMode getSlaveSyncMode() {
        throw new UnsupportedOperationException();
    }

    public SyncMode[] getSlaveSyncModes() {
        throw new UnsupportedOperationException();
    }

    public float getTempoFactor() {
        throw new UnsupportedOperationException();
    }

    public float getTempoInBPM() {
        throw new UnsupportedOperationException();
    }

    public float getTempoInMPQ() {
        throw new UnsupportedOperationException();
    }

    public long getTickLength() {
        throw new UnsupportedOperationException();
    }

    public long getTickPosition() {
        throw new UnsupportedOperationException();
    }

    public boolean getTrackMute(int track) {
        throw new UnsupportedOperationException();
    }

    public boolean getTrackSolo(int track) {
        throw new UnsupportedOperationException();
    }

    public boolean isRecording() {
        return false;
    }

    public boolean isRunning() {
        return player != null && player.isPlaying();
    }

    public void recordDisable(Track track) {
        throw new UnsupportedOperationException();
    }

    public void recordEnable(Track track, int channel) {
        throw new UnsupportedOperationException();
    }

    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
        throw new UnsupportedOperationException();
    }

    public void removeMetaEventListener(MetaEventListener listener) {
        throw new UnsupportedOperationException();
    }

    public void setLoopCount(int count) {
        throw new UnsupportedOperationException();
    }

    public void setLoopEndPoint(long tick) {
        throw new UnsupportedOperationException();
    }

    public void setLoopStartPoint(long tick) {
        throw new UnsupportedOperationException();
    }

    public void setMasterSyncMode(SyncMode sync) {
        throw new UnsupportedOperationException();
    }

    public void setMicrosecondPosition(long microseconds) {
        throw new UnsupportedOperationException();
    }

    public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
        setSequence(new AndroidMidiFileReader().getSequence(stream));
    }

    public void setSequence(Sequence sequence) throws InvalidMidiDataException {
        if (!(sequence instanceof AndroidSequence)) {
            throw new InvalidMidiDataException("Sequence must be an AndroidSequence");
        }
        
        if (isRunning()) {
            stop();
        }
        
        this.sequence = (AndroidSequence)sequence;
    }

    public void setSlaveSyncMode(SyncMode sync) {
        throw new UnsupportedOperationException();
    }

    public void setTempoFactor(float factor) {
        throw new UnsupportedOperationException();
    }

    public void setTempoInBPM(float bpm) {
        throw new UnsupportedOperationException();
    }

    public void setTempoInMPQ(float mpq) {
        throw new UnsupportedOperationException();
    }

    public void setTickPosition(long tick) {
        throw new UnsupportedOperationException();
    }

    public void setTrackMute(int track, boolean mute) {
        throw new UnsupportedOperationException();
    }

    public void setTrackSolo(int track, boolean solo) {
        throw new UnsupportedOperationException();
    }

    public void start() {
        if (!isOpen()) {
            throw new IllegalStateException("Sequencer must be open");
        }
        
        if (sequence == null) {
            throw new IllegalStateException("Need a Sequence to play");
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
                String s = this.sequence.getURL().toExternalForm();
                
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

    public void startRecording() {
        throw new UnsupportedOperationException();
    }

    public void stop() {
        if (!isOpen()) {
            throw new IllegalStateException("Sequencer must be open");
        }
        
        if (isRunning()) {
            player.stop();
        }
    }

    public void stopRecording() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        if (isOpen()) {
            stop();
            player = null;
        }
    }
    
    public Info getDeviceInfo() {
        return new Info();
    }

    public int getMaxReceivers() {
        return 0;
    }

    public int getMaxTransmitters() {
        return 0;
    }

    public Receiver getReceiver() throws MidiUnavailableException {
        throw new MidiUnavailableException("No receiver available");
    }

    public List<Receiver> getReceivers() {
        return new ArrayList<Receiver>();
    }

    public Transmitter getTransmitter() throws MidiUnavailableException {
        throw new MidiUnavailableException("No receiver available");
    }

    public List<Transmitter> getTransmitters() {
        return new ArrayList<Transmitter>();
    }

    public boolean isOpen() {
        return player != null;
    }

    public void open() throws MidiUnavailableException {
        try {
            player = new MediaPlayer();
        } catch (Exception ex) {
            throw new MidiUnavailableException(ex.toString());
        }
    }

}
