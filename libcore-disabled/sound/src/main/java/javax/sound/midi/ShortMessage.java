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

import org.apache.harmony.sound.internal.nls.Messages;

public class ShortMessage extends MidiMessage {

    public static final int ACTIVE_SENSING = 254;

    public static final int CHANNEL_PRESSURE = 208;

    public static final int CONTINUE = 251;

    public static final int CONTROL_CHANGE = 176;

    public static final int END_OF_EXCLUSIVE = 247;

    public static final int MIDI_TIME_CODE = 241;

    public static final int NOTE_OFF = 128;

    public static final int NOTE_ON = 144;

    public static final int PITCH_BEND = 224;

    public static final int POLY_PRESSURE = 160;

    public static final int PROGRAM_CHANGE = 192;

    public static final int SONG_POSITION_POINTER = 242;

    public static final int SONG_SELECT = 243;

    public static final int START = 250;

    public static final int STOP = 252;

    public static final int SYSTEM_RESET = 255;

    public static final int TIMING_CLOCK = 248;

    public static final int TUNE_REQUEST = 246;

    public ShortMessage() {
        super(new byte[] {-112, 64, 127});
    }

    protected ShortMessage(byte[] data) {
        super(data);
    }

    @Override
    public Object clone() {
        return new ShortMessage(this.getMessage());
    }

    public int getChannel() {
        /*
         * channel change from 0 up to 15
         */
        if ((data == null) || (data.length == 0)) {
            return 0;
        }
        return data[0] & 0x0F;
    }

    public int getCommand() {
       /*
        * command should be divisible by 16 without rest 
        */
        if ((data == null) || (data.length == 0)) {
            return 0;
        }
        return (data[0] & 0xFF) - getChannel();
    }

    public int getData1() {
        if ((data == null) || (data.length == 0)) {
            return 0;
        } else if (data.length < 2) {
            return 0;
        } else {
            return data[1] & 0xFF;
        }
    }

    public int getData2() {
        if ((data == null) || (data.length == 0)) {
            return 0;
        } else if (data.length < 3) {
            return 0;
        } else {
            return data[2] & 0xFF;
        }
    }

    protected final int getDataLength(int status)
            throws InvalidMidiDataException {
        // FIXME
        /*
         * I have some question about this method. I didn't understand how
         * should to work this method, but some results I get by experimental
         * method. From 0 up to 127, from 256 up to 383 and so on this method
         * throw out exception, i.e. after 256 begin cycle with some small
         * differences in the first lap, from 0 up to 255. From the second lap
         * and so on this method, getDataLenght(int), throw out exception with
         * value of status from 496 up to 511, from 752 up to 767 and so on,
         * i.e. on the last 16 number of 256-lap. And now differences in the
         * first lap. This method don't throw out exception with value of status
         * from 240 up to 255. It has next behavior: 
         * - value of status equals 240 -- throw out exception; 
         * - 241 -- return 1; 
         * - 242 -- return 2; 
         * - 243 -- return 1; 
         * - from 244 up to 245 -- throw out exception; 
         * - from 246 up to 255 -- return 0;
         */
        if (status < 0) {
            // sound.04=Invalid status byte: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); //$NON-NLS-1$
        }
        if (((status % 256) >= 0) && ((status % 256) <= 127)) {
            // sound.04=Invalid status byte: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); //$NON-NLS-1$
        }
        if (((status / 256) == 0)
                && ((status == 240) || (status == 244) || (status == 245))) {
            // sound.04=Invalid status byte: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); //$NON-NLS-1$
        }
        if (((status / 256) != 0) && ((status % 256) >= 244)
                && ((status % 256) <= 255)) {
            // sound.04=Invalid status byte: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); //$NON-NLS-1$
        }

        if ((status / 256) == 0) {
            if ((status == 241) || (status == 243)) {
                return 1;
            } else if (status == 242) {
                return 2;
            } else if ((status >= 246) && (status <= 255)) {
                return 0;
            }
        }
        if (((status % 256) >= 128) && ((status % 256) <= 191)) {
            return 2;
        } else if (((status % 256) >= 192) && ((status % 256) <= 223)) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setMessage(int status) throws InvalidMidiDataException {
        /*
         * value of variable status is more or equals 246 and less or equals 255
         */
        if ((status < 246) || (status > 255)) {
            // sound.04=Invalid status byte: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); //$NON-NLS-1$
        }
        super.setMessage(new byte[] {(byte) status}, 1);
    }

    public void setMessage(int status, int data1, int data2)
            throws InvalidMidiDataException {
        // FIXME
        /*
         * In the Sun's implementation variables data1 and data2 don't use; I
         * don't find situation when this variables influence on result of
         * functions getData1() and getData2(). But function getDataLength(int)
         * return 0 when I modify status byte from 246 up to 255, and so I think
         * it's true.
         */
        if ((status < 246) || (status > 255)) {
            // sound.04=Invalid status byte: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); //$NON-NLS-1$
        }
        super.setMessage(new byte[] {(byte) status}, 1);
    }

    public void setMessage(int command, int channel, int data1, int data2)
            throws InvalidMidiDataException {
        // FIXME
        /*
         * value of variable command is more or equals 128 and less or equals
         * 239
         */
        if ((command < 128) || (command > 239)) {
            /*
             * when this exception throw out, the value of variable command
             * should be the hexadecimal number
             */
            // sound.05=command out of range: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.05", command)); //$NON-NLS-1$
        }
        /*
         * value of variable channel is more or equals 0 and less or equals 15
         */
        if ((channel < 0) || (channel > 15)) {
            // sound.06=channel out of range: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.06", channel)); //$NON-NLS-1$
        }
        /*
         * value of data1 and data2 is more or equals 0 and less or equals 127,
         * but when command more or equals 192 and less or equals 223 the second
         * data, data2, is unused, because getDataLength(int) return 1 in this
         * case, and in other cases it return 2
         */
        if ((getDataLength(command) >= 1) && ((data1 < 0) || (data1 > 127))) {
            // sound.07=data1 out of range: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.07", data1)); //$NON-NLS-1$
        }
        if ((getDataLength(command) == 2) && ((data2 < 0) || (data2 > 127))) {
            // sound.08=data2 out of range: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.08", data2)); //$NON-NLS-1$
        }

        int tcom = command - (command % 16);
        if (getDataLength(command) == 1) {
            super.setMessage(new byte[] {(byte) (tcom + channel), (byte) data1}, 2);
        } else {
            super.setMessage(new byte[] {(byte) (tcom + channel), (byte) data1, 
                    (byte) data2}, 3);
        }
    }

}
