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

public abstract class CompoundControl extends Control {
    public static class Type extends Control.Type {
        protected Type(String name) {
            super(name);
        }
    }

    private Control[] memberControls;

    protected CompoundControl(CompoundControl.Type type,
            Control[] memberControls) {
        super(type);
        this.memberControls = memberControls;
    }

    public Control[] getMemberControls() {
        return this.memberControls;
    }

    public String toString() {
        return getType() + "CompoundControl containing "  //$NON-NLS-1$
            + String.valueOf(memberControls) + " Controls."; //$NON-NLS-1$
    }
}
