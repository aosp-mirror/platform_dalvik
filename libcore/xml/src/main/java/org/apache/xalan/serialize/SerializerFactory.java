/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: SerializerFactory.java 468642 2006-10-28 06:55:10Z minchau $
 */
package org.apache.xalan.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Factory for creating serializers.
 * @deprecated The new class to use is 
 * org.apache.xml.serializer.SerializerFactory
 */
public abstract class SerializerFactory
{

    private SerializerFactory()
    {
    }
    /**
     * Returns a serializer for the specified output method. Returns
     * null if no implementation exists that supports the specified
     * output method. For a list of the default output methods see
     * {@link org.apache.xml.serializer.Method}.
     *
     * @param format The output format
     * @return A suitable serializer, or null
     * @throws IllegalArgumentException (apparently -sc) if method is
     * null or an appropriate serializer can't be found
     * @throws WrappedRuntimeException (apparently -sc) if an
     * exception is thrown while trying to find serializer
     * @deprecated Use org.apache.xml.serializer.SerializerFactory
     */
    public static Serializer getSerializer(Properties format)
    {
        org.apache.xml.serializer.Serializer ser;
        ser = org.apache.xml.serializer.SerializerFactory.getSerializer(format);
        SerializerFactory.SerializerWrapper si = new SerializerWrapper(ser);
        return si;

    }
    
    /**
     * This class just exists to wrap a new Serializer in the new package by
     * an old one.
     * @deprecated
     */

    private static class SerializerWrapper implements Serializer
    {
        private final org.apache.xml.serializer.Serializer m_serializer;
        private DOMSerializer m_old_DOMSerializer;

        SerializerWrapper(org.apache.xml.serializer.Serializer ser)
        {
            m_serializer = ser;

        }

        public void setOutputStream(OutputStream output)
        {
            m_serializer.setOutputStream(output);
        }

        public OutputStream getOutputStream()
        {
            return m_serializer.getOutputStream();
        }

        public void setWriter(Writer writer)
        {
            m_serializer.setWriter(writer);
        }

        public Writer getWriter()
        {
            return m_serializer.getWriter();
        }

        public void setOutputFormat(Properties format)
        {
            m_serializer.setOutputFormat(format);
        }

        public Properties getOutputFormat()
        {
            return m_serializer.getOutputFormat();
        }

        public ContentHandler asContentHandler() throws IOException
        {
            return m_serializer.asContentHandler();
        }

        /**
         * @return an old style DOMSerializer that wraps a new one.
         * @see org.apache.xalan.serialize.Serializer#asDOMSerializer()
         */
        public DOMSerializer asDOMSerializer() throws IOException
        {
            if (m_old_DOMSerializer == null)
            {
                m_old_DOMSerializer =
                    new DOMSerializerWrapper(m_serializer.asDOMSerializer());
            }
            return m_old_DOMSerializer;
        }
        /**
         * @see org.apache.xalan.serialize.Serializer#reset()
         */
        public boolean reset()
        {
            return m_serializer.reset();
        }

    }

    /**
     * This class just wraps a new DOMSerializer with an old style one for
     * migration purposes. 
  *
     */
    private static class DOMSerializerWrapper implements DOMSerializer
    {
        private final org.apache.xml.serializer.DOMSerializer m_dom;
        DOMSerializerWrapper(org.apache.xml.serializer.DOMSerializer domser)
        {
            m_dom = domser;
        }

        public void serialize(Node node) throws IOException
        {
            m_dom.serialize(node);
        }
    }

}
