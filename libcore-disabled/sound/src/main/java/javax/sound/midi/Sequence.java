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

import java.util.Vector;

import org.apache.harmony.sound.internal.nls.Messages;

public class Sequence {
    public static final float PPQ = 0.0f;

    public static final float SMPTE_24 = 24.0f;

    public static final float SMPTE_25 = 25.0f;

    public static final float SMPTE_30 = 30.0f;

    public static final float SMPTE_30DROP = 29.969999313354492f;
    
    protected float divisionType;

    protected int resolution;

    protected Vector<Track> tracks;
    
    private Vector<Patch> patches;

    public Sequence(float divisionType, int resolution) throws InvalidMidiDataException {
        if (divisionType != Sequence.PPQ &&
                divisionType != Sequence.SMPTE_24 &&
                divisionType != Sequence.SMPTE_25 &&
                divisionType != Sequence.SMPTE_30 &&
                divisionType != Sequence.SMPTE_30DROP ) {
            // sound.0B=Unsupported division type: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.0B", divisionType));       //$NON-NLS-1$
        }
        this.divisionType = divisionType;
        this.resolution = resolution;
        this.tracks = new Vector<Track>();
        this.patches = new Vector<Patch>();
        
    }

    public Sequence(float divisionType, int resolution, int numTracks)
            throws InvalidMidiDataException {
        if (divisionType != Sequence.PPQ &&
                divisionType != Sequence.SMPTE_24 &&
                divisionType != Sequence.SMPTE_25 &&
                divisionType != Sequence.SMPTE_30 &&
                divisionType != Sequence.SMPTE_30DROP ) {
            // sound.0B=Unsupported division type: {0}
            throw new InvalidMidiDataException(Messages.getString("sound.0B", divisionType));       //$NON-NLS-1$
        }
        this.divisionType = divisionType;
        this.resolution = resolution;
        this.patches = new Vector<Patch>();
        this.tracks = new Vector<Track>();
        if (numTracks > 0) {
            for (int i = 0; i < numTracks; i++) {
                tracks.add(new Track());
            }
        }
    }

    public Track createTrack() {
        /*
         * new Tracks accrue to the end of vector
         */
        Track tr = new Track();
        tracks.add(tr);
        return tr;
    }

    public boolean deleteTrack(Track track) {
        return tracks.remove(track);
    }

    public float getDivisionType() {
        return divisionType;
    }

    public long getMicrosecondLength() {
        float divisionType;
        if (this.divisionType == 0.0f) {
            divisionType = 2;
        } else {
            divisionType = this.divisionType;
        }
        return (long) (1000000.0 * getTickLength() / 
                (divisionType * this.resolution * 1.0f));
    }

    public Patch[] getPatchList() {
        //FIXME
        /*
         * I don't understand how to works this method, and so
         * I simply return an empty array. 'patches' initializes
         * in the constructor as empty vector 
         */
        Patch[] patch = new Patch[patches.size()];
        patches.toArray(patch);
        return patch;
    }

    public int getResolution() {
        return resolution;
    }

    public long getTickLength() {
        /*
         * this method return the biggest value of tick of 
         * all tracks contain in the Sequence
         */
        long maxTick = 0;
        for (int i = 0; i < tracks.size(); i++) {
            if (maxTick < tracks.get(i).ticks()) {
                maxTick = tracks.get(i).ticks();
            }
        }
        return maxTick;
    }

    public Track[] getTracks() {
        Track[] track = new Track[tracks.size()];
        tracks.toArray(track);
        return track;
    }

}
