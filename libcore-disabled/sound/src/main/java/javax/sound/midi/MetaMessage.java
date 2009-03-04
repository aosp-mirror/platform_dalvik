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

public class MetaMessage extends MidiMessage {
    public static final int META = 255;
    
    private int dsp; //displacement from begin of array that 
                     //return by method getData() from begin
                     //of array that contain data
    
    public MetaMessage() {
        super(new byte[] {-1, 0});
    }

    protected MetaMessage(byte[] data) {
        super(data);
        if (data == null) {
            throw new NullPointerException();
        }
        if (super.length > 3) {
            int n = 3;
            while ((n <= super.length) && (super.data[n - 1] < 0)) {
                n++;
            }
            dsp = n;
        }
    }

    @Override
    public Object clone() {
        return new MetaMessage(this.getMessage());
    }

    public byte[] getData() {
        if ((super.data != null) && (super.length > 3)) {
            byte[] bt = new byte[super.length - dsp];
            for (int i = dsp; i < super.length; i++) {
                bt[i - dsp] = super.data[i];
            }
            return bt;
        }
        return new byte[0];
    }

    public int getType() {
        if ((super.data != null) && (super.length >= 2)) {
            return super.data[1] & 0xFF;
        }
        return 0;
    }

    public void setMessage(int type, byte[] data, int length) throws InvalidMidiDataException {
        if (type < 0 || type >= 128) {
            // sound.0A=Invalid meta event with type {0}
            throw new InvalidMidiDataException(Messages.getString("sound.0A", type)); //$NON-NLS-1$
        }
        if (length < 0 || (data != null && length > data.length)) {
            // sound.03=length out of bounds: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.03", length)); //$NON-NLS-1$
        }
        try {
            if (data == null) {
                if (length != 0) {
                    throw new NullPointerException();
                }
                super.setMessage(new byte[] { -1, (byte) type, 0 }, 3);
            } else {
                int div = 128;
                int n = 1;
                int ost;
                int sm = 0;
                while (length / div != 0) {
                    n++;
                    div *= 128;
                }
                int ln = n;
                byte[] tdata = new byte[length + ln + 2];
                div = 1;
                ost = (length / div) % 128;
                while (n != 0) {
                    tdata[n - 1 + 2] = (byte) (ost + sm);
                    n--;
                    div *= 128;
                    ost = (length / div) % 128;
                    sm = 128;
                }
                tdata[0] = -1;
                tdata[1] = (byte) type;
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        tdata[2 + ln + i] = data[i];
                    }
                }
                super.setMessage(tdata, length + 2 + ln);
                dsp = ln + 2;
            }
        } catch (InvalidMidiDataException e) {
            throw e;
        }
    }
}
