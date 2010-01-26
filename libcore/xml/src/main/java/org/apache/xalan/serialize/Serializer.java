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
 * $Id: Serializer.java 468642 2006-10-28 06:55:10Z minchau $
 */
package org.apache.xalan.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import org.xml.sax.ContentHandler;

/**
 * The Serializer interface is implemented by Serializers to publish methods
 * to get and set streams and writers, to set the output properties, and
 * get the Serializer as a ContentHandler or DOMSerializer.
 * @deprecated Use org.apache.xml.serializer.Serializer
 */
public interface Serializer
{

  /**
   * Specifies an output stream to which the document should be
   * serialized. This method should not be called while the
   * serializer is in the process of serializing a document.
   * <p>
   * The encoding specified in the output {@link Properties} is used, or
   * if no encoding was specified, the default for the selected
   * output method.
   *
   * @param output The output stream
   * 
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public void setOutputStream(OutputStream output);

  /**
   * Get the output stream where the events will be serialized to.
   *
   * @return reference to the result stream, or null of only a writer was
   * set.
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public OutputStream getOutputStream();

  /**
   * Specifies a writer to which the document should be serialized.
   * This method should not be called while the serializer is in
   * the process of serializing a document.
   * <p>
   * The encoding specified for the output {@link Properties} must be
   * identical to the output format used with the writer.
   *
   * @param writer The output writer stream
   * 
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public void setWriter(Writer writer);

  /**
   * Get the character stream where the events will be serialized to.
   *
   * @return Reference to the result Writer, or null.
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public Writer getWriter();

  /**
   * Specifies an output format for this serializer. It the
   * serializer has already been associated with an output format,
   * it will switch to the new format. This method should not be
   * called while the serializer is in the process of serializing
   * a document.
   *
   * @param format The output format to use
   * 
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public void setOutputFormat(Properties format);

  /**
   * Returns the output format for this serializer.
   *
   * @return The output format in use
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public Properties getOutputFormat();

  /**
   * Return a {@link ContentHandler} interface into this serializer.
   * If the serializer does not support the {@link ContentHandler}
   * interface, it should return null.
   *
   * @return A {@link ContentHandler} interface into this serializer,
   *  or null if the serializer is not SAX 2 capable
   * @throws IOException An I/O exception occured
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public ContentHandler asContentHandler() throws IOException;

  /**
   * Return a {@link DOMSerializer} interface into this serializer.
   * If the serializer does not support the {@link DOMSerializer}
   * interface, it should return null.
   *
   * @return A {@link DOMSerializer} interface into this serializer,
   *  or null if the serializer is not DOM capable
   * @throws IOException An I/O exception occured
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public DOMSerializer asDOMSerializer() throws IOException;

  /**
   * Resets the serializer. If this method returns true, the
   * serializer may be used for subsequent serialization of new
   * documents. It is possible to change the output format and
   * output stream prior to serializing, or to use the existing
   * output format and output stream.
   *
   * @return True if serializer has been reset and can be reused
   * 
   * @deprecated Use org.apache.xml.serializer.Serializer
   */
  public boolean reset();
}
