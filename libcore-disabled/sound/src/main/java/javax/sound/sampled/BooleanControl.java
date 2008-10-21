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

package javax.sound.sampled;

public abstract class BooleanControl extends Control {

    public static class Type extends Control.Type {
        public static final Type APPLY_REVERB = new Type("Apply Reverb"); //$NON-NLS-1$

        public static final Type MUTE = new Type("Mute"); //$NON-NLS-1$

        protected Type(String name) {
            super(name);
        }
    }

    private boolean value;

    private String trueStateLabel;

    private String falseStateLabel;

    protected BooleanControl(BooleanControl.Type type, boolean initialValue,
            String trueStateLabel, String falseStateLabel) {
        super(type);
        this.value = initialValue;
        this.trueStateLabel = trueStateLabel;
        this.falseStateLabel = falseStateLabel;
    }

    protected BooleanControl(BooleanControl.Type type, boolean initialValue) {
        this(type, initialValue, "true", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public String getStateLabel(boolean state) {
        if (state) {
            return this.trueStateLabel;
        } else {
            return this.falseStateLabel;
        }
    }

    public String toString() {
        return getType() + " Control with current value: " + getStateLabel(value); //$NON-NLS-1$
    }
}
