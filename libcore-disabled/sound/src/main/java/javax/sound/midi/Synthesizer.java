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

public interface Synthesizer extends MidiDevice {
    Instrument[] getAvailableInstruments();

    MidiChannel[] getChannels();

    Soundbank getDefaultSoundbank();

    long getLatency();

    Instrument[] getLoadedInstruments();

    int getMaxPolyphony();

    VoiceStatus[] getVoiceStatus();

    boolean isSoundbankSupported(Soundbank soundbank);

    boolean loadAllInstruments(Soundbank soundbank);

    boolean loadInstrument(Instrument instrument);

    boolean loadInstruments(Soundbank soundbank, Patch[] patchList);

    boolean remapInstrument(Instrument from, Instrument to);

    void unloadAllInstruments(Soundbank soundbank);

    void unloadInstrument(Instrument instrument);

    void unloadInstruments(Soundbank soundbank, Patch[] patchList);
}
