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

public interface MidiChannel {
    void allNotesOff();

    void allSoundOff();

    void controlChange(int controller, int value);

    int getChannelPressure();

    int getController(int controller);

    boolean getMono();

    boolean getMute();

    boolean getOmni();

    int getPitchBend();

    int getPolyPressure(int noteNumber);

    int getProgram();

    boolean getSolo();

    boolean localControl(boolean on);

    void noteOff(int noteNumber);

    void noteOff(int noteNumber, int velocity);

    void noteOn(int noteNumber, int velocity);

    void programChange(int program);

    void programChange(int bank, int program);

    void resetAllControllers();

    void setChannelPressure(int pressure);

    void setMono(boolean on);

    void setMute(boolean mute);

    void setOmni(boolean on);

    void setPitchBend(int bend);

    void setPolyPressure(int noteNumber, int pressure);

    void setSolo(boolean soloState);
}
