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

import java.util.EventObject;

public class LineEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = -1274246333383880410L;
    
    private LineEvent.Type type;
    private long position;

    public static class Type {

        public static final Type CLOSE = new Type("Close"); //$NON-NLS-1$

        public static final Type OPEN = new Type("Open"); //$NON-NLS-1$

        public static final Type START = new Type("Start"); //$NON-NLS-1$

        public static final Type STOP = new Type("Stop"); //$NON-NLS-1$

        private String name;

        public Type(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object another) {
            if (this == another) {
                return true;
            }

            if (another == null || !(another instanceof Type)) {
                return false;
            }

            Type obj = (Type) another;
            return name == null ? obj.name == null : name.equals(obj.name);
        }

        @Override
        public final int hashCode() {
            return name == null ? 0 : name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public LineEvent(Line line, LineEvent.Type type, long position) {
        super(line);
        this.type = type;
        this.position = position;
    }
    
    public final Line getLine() {
        return (Line)getSource();
    }
    
    public final LineEvent.Type getType() {
        return type;
    }
    
    public final long getFramePosition() {
        return position;
    }
    
    public String toString() {
        return type + " event from line " + getLine(); //$NON-NLS-1$
    }
}
