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

public class SysexMessage extends MidiMessage {
    public static final int SPECIAL_SYSTEM_EXCLUSIVE = 247;

    public static final int SYSTEM_EXCLUSIVE = 240;

    public SysexMessage() {
        super(new byte[] {-16, -9});
    }

    protected SysexMessage(byte[] data) {
        super(data);
    }

    @Override
    public Object clone() {
        return new SysexMessage(this.getMessage());
    }

    public byte[] getData() {
        byte[] bt = new byte[super.length - 1];
        for(int i = 1; i < super.length; i++) {
            bt[i-1] = super.data[i];
        }
        return bt;
    }

    @Override
    public void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        //FIXME
        /*
         * if this exception throw out, the value of wrong status byte
         * should be the hexadecimal value
         */
        if(((data[0] & 0xFF) != SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) &&
                ((data[0] & 0xFF) != SysexMessage.SYSTEM_EXCLUSIVE)) {
            // sound.09=Invalid status byte for sysex message: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.09",  //$NON-NLS-1$
                    data[0] & 0xFF));
        }
        super.setMessage(data, length);
    }

    public void setMessage(int status, byte[] data, int length) throws InvalidMidiDataException {
        //FIXME
        /*
         * if this exception throw out, the value of wrong status byte
         * should be the hexadecimal value
         */
        if((status != SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) &&
                (status != SysexMessage.SYSTEM_EXCLUSIVE)) {
            // sound.09=Invalid status byte for sysex message: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.09",  //$NON-NLS-1$
                    status));
        }
        if((length < 0) || (length > data.length)) {
            // sound.03=length out of bounds: {0}
            throw new IndexOutOfBoundsException(Messages.getString("sound.03", length)); //$NON-NLS-1$
        }
        byte[] bt = new byte[length + 1];
        bt[0] = (byte) status;
        for(int i = 0; i < length; i++) {
            bt[i+1] = data[i];
        }
        super.setMessage(bt, length + 1);
    }
}
