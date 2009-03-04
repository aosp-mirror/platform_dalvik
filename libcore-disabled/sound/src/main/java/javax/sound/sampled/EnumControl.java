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

import org.apache.harmony.sound.internal.nls.Messages;

public abstract class EnumControl extends Control {
    public static class Type extends Control.Type {
        public static final Type REVERB = new Type("Reverb"); //$NON-NLS-1$

        protected Type(String name) {
            super(name);
        }
    }

    private Object[] values;

    private Object value;

    protected EnumControl(EnumControl.Type type, Object[] values, Object value) {
        super(type);
        this.value = value;
        this.values = values;
    }

    public void setValue(Object value) {
        for (Object val : values) {
            if (val.equals(value)) {
                this.value = value;
                return;
            }
        }
        // sound.0D=The value is not supported
        throw new IllegalArgumentException(Messages.getString("sound.0D")); //$NON-NLS-1$
    }

    public Object getValue() {
        return value;
    }

    public Object[] getValues() {
        return values;
    }

    public String toString() {
        return getType() + " with current value: " + value; //$NON-NLS-1$
    }
}
