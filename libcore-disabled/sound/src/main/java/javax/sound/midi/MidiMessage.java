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

public abstract class MidiMessage implements Cloneable {

    protected byte[] data;

    protected int length;

    protected MidiMessage(byte[] data) {
        if (data == null) {
            length = 0;
        } else {
            length = data.length;
            this.data = data;
        }
    }

    @Override
    public abstract Object clone();

    public int getLength() {
        return length;
    }

    public byte[] getMessage() {
        if (data == null) {
            throw new NullPointerException();
        }
        return data.clone();
    }

    public int getStatus() {
        if ((data == null) || (length == 0)) {
            return 0;
        }
        return data[0] & 0xFF;
    }

    protected void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        if ((length < 0) || (length > data.length)) {
            // sound.03=length out of bounds: {0}
            throw new IndexOutOfBoundsException(Messages.getString("sound.03", length)); //$NON-NLS-1$
        }

        this.data = new byte[length];
        if (length != 0) {
            for (int i = 0; i < length; i++) {
                this.data[i] = data[i];
            }
        }
        this.length = length;
    }
}
