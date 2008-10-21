/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.harmony.text.internal.nls.Messages;

/**
 * AttributedCharacterIterator
 */
public interface AttributedCharacterIterator extends CharacterIterator {

    public static class Attribute implements Serializable {

        private static final long serialVersionUID = -9142742483513960612L;

        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute(
                "input_method_segment"); //$NON-NLS-1$

        public static final Attribute LANGUAGE = new Attribute("language"); //$NON-NLS-1$

        public static final Attribute READING = new Attribute("reading"); //$NON-NLS-1$

        private String name;

        protected Attribute(String name) {
            this.name = name;
        }

        @Override
        public final boolean equals(Object object) {
            if (object == null || !(object.getClass().equals(this.getClass()))) {
                return false;
            }
            return name.equals(((Attribute) object).name);
        }

        protected String getName() {
            return name;
        }

        @Override
        public final int hashCode() {
            return name.hashCode();
        }

        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Attribute.class) {
                // text.0C=cannot resolve subclasses
                throw new InvalidObjectException(Messages.getString("text.0C")); //$NON-NLS-1$
            }
            if (this.equals(INPUT_METHOD_SEGMENT)) {
                return INPUT_METHOD_SEGMENT;
            }
            if (this.equals(LANGUAGE)) {
                return LANGUAGE;
            }
            if (this.equals(READING)) {
                return READING;
            }
            // text.02=Unknown attribute
            throw new InvalidObjectException(Messages.getString("text.02")); //$NON-NLS-1$
        }

        @Override
        public String toString() {
            return getClass().getName() + '(' + getName() + ')';
        }
    }

    public Set<Attribute> getAllAttributeKeys();

    public Object getAttribute(Attribute attribute);

    public Map<Attribute, Object> getAttributes();

    public int getRunLimit();

    public int getRunLimit(Attribute attribute);

    public int getRunLimit(Set<? extends Attribute> attributes);

    public int getRunStart();

    public int getRunStart(Attribute attribute);

    public int getRunStart(Set<? extends Attribute> attributes);
}
