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

import java.io.IOException;
import java.io.InputStream;

public interface Sequencer extends MidiDevice {
    int LOOP_CONTINUOUSLY = -1;

    class SyncMode {
        public static final SyncMode INTERNAL_CLOCK = new SyncMode("INTERNAL_CLOCK"); //$NON-NLS-1$

        public static final SyncMode MIDI_SYNC = new SyncMode("MIDI_SYNC"); //$NON-NLS-1$

        public static final SyncMode MIDI_TIME_CODE = new SyncMode("MIDI_TIME_CODE"); //$NON-NLS-1$

        public static final SyncMode NO_SYNC = new SyncMode("NO_SYNC"); //$NON-NLS-1$

        private String name;

        protected SyncMode(String name) {
            this.name = name;
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SyncMode other = (SyncMode) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public final int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public final String toString() {
            return name;
        }
    }

    int[] addControllerEventListener(ControllerEventListener listener, int[] controllers);

    boolean addMetaEventListener(MetaEventListener listener);

    int getLoopCount();

    long getLoopEndPoint();

    long getLoopStartPoint();

    Sequencer.SyncMode getMasterSyncMode();

    Sequencer.SyncMode[] getMasterSyncModes();

    long getMicrosecondLength();

    long getMicrosecondPosition();

    Sequence getSequence();

    Sequencer.SyncMode getSlaveSyncMode();

    Sequencer.SyncMode[] getSlaveSyncModes();

    float getTempoFactor();

    float getTempoInBPM();

    float getTempoInMPQ();

    long getTickLength();

    long getTickPosition();

    boolean getTrackMute(int track);

    boolean getTrackSolo(int track);

    boolean isRecording();

    boolean isRunning();

    void recordDisable(Track track);

    void recordEnable(Track track, int channel);

    int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers);

    void removeMetaEventListener(MetaEventListener listener);

    void setLoopCount(int count);

    void setLoopEndPoint(long tick);

    void setLoopStartPoint(long tick);

    void setMasterSyncMode(Sequencer.SyncMode sync);

    void setMicrosecondPosition(long microseconds);

    void setSequence(InputStream stream) throws IOException, InvalidMidiDataException;

    void setSequence(Sequence sequence) throws InvalidMidiDataException;

    void setSlaveSyncMode(Sequencer.SyncMode sync);

    void setTempoFactor(float factor);

    void setTempoInBPM(float bpm);

    void setTempoInMPQ(float mpq);

    void setTickPosition(long tick);

    void setTrackMute(int track, boolean mute);

    void setTrackSolo(int track, boolean solo);

    void start();

    void startRecording();

    void stop();

    void stopRecording();

}
