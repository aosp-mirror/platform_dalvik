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

public interface Port extends Line {

    public static class Info extends Line.Info {

        private String name;

        private boolean isSource;

        public static final Info MICROPHONE = new Info(Port.class,
                "MICROPHONE", true); //$NON-NLS-1$

        public static final Info LINE_IN = new Info(Port.class, "LINE_IN", true); //$NON-NLS-1$

        public static final Info COMPACT_DISC = new Info(Port.class,
                "COMPACT_DISC", true); //$NON-NLS-1$

        public static final Info SPEAKER = new Info(Port.class, "SPEAKER", //$NON-NLS-1$
                false);

        public static final Info HEADPHONE = new Info(Port.class, "HEADPHONES", //$NON-NLS-1$
                false);

        public static final Info LINE_OUT = new Info(Port.class, "LINE_OUT", //$NON-NLS-1$
                false);

        public Info(Class<?> lineClass, String name, boolean isSource) {
            super(lineClass);
            this.name = name;
            this.isSource = isSource;
        }

        public String getName() {
            return this.name;
        }

        public boolean isSource() {
            return this.isSource;
        }

        public boolean matches(Line.Info info) {
            if (super.matches(info) && Port.Info.class.equals(info.getClass())
                    && name.equals(((Port.Info) info).getName())
                    && isSource == ((Port.Info) info).isSource()) {
                return true;
            }
            return false;
        }

        public final boolean equals(Object obj) {
            return this == obj;
        }

        public final int hashCode() {
            return name.hashCode() ^ getLineClass().hashCode();
        }

        public final String toString() {
            return name + (isSource ? " source port" : " target port"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
