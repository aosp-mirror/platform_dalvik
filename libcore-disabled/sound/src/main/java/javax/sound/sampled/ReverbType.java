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

public class ReverbType {

    protected ReverbType(String name, int earlyReflectionDelay,
            float earlyReflectionIntensity, int lateReflectionDelay,
            float lateReflectionIntensity, int decayTime) {
        this.name = name;
        this.earlyReflectionDelay = earlyReflectionDelay;
        this.earlyReflectionIntensity = earlyReflectionIntensity;
        this.lateReflectionDelay = lateReflectionDelay;
        this.lateReflectionIntensity = lateReflectionIntensity;
        this.decayTime = decayTime;
    }

    private String name;

    private int earlyReflectionDelay;

    private float earlyReflectionIntensity;

    private int lateReflectionDelay;

    private float lateReflectionIntensity;

    private int decayTime;

    public String getName() {
        return this.name;
    }

    public final int getEarlyReflectionDelay() {
        return this.earlyReflectionDelay;
    }

    public final float getEarlyReflectionIntensity() {
        return this.earlyReflectionIntensity;
    }

    public final int getLateReflectionDelay() {
        return this.lateReflectionDelay;
    }

    public final float getLateReflectionIntensity() {
        return this.lateReflectionIntensity;
    }

    public final int getDecayTime() {
        return this.decayTime;
    }

    public final boolean equals(Object obj) {
        return this == obj;
    }

    public final int hashCode() {
        return toString().hashCode();
    }

    public final String toString() {
        return name + ", early reflection delay " + earlyReflectionDelay //$NON-NLS-1$
                + " ns, early reflection intensity " + earlyReflectionIntensity //$NON-NLS-1$
                + " dB, late deflection delay " + lateReflectionDelay //$NON-NLS-1$
                + " ns, late reflection intensity " + lateReflectionIntensity //$NON-NLS-1$
                + " dB, decay time " + decayTime; //$NON-NLS-1$
    }
}
