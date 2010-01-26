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
 * $Id: QueuedEvents.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.util.Vector;

import org.apache.xml.utils.MutableAttrListImpl;


/**
 * This class acts as a base for ResultTreeHandler, and keeps
 * queud stack events.  In truth, we don't need a stack,
 * so I may change this down the line a bit.
 */
public abstract class QueuedEvents
{

  /** The number of events queued          */
  protected int m_eventCount = 0;

  /** Queued start document          */
  // QueuedStartDocument m_startDoc = new QueuedStartDocument();

  /** Queued start element          */
  // QueuedStartElement m_startElement = new QueuedStartElement();
  
  public boolean m_docPending = false;
  protected boolean m_docEnded = false;
  
  /** Flag indicating that an event is pending.  Public for 
   *  fast access by ElemForEach.         */
  public boolean m_elemIsPending = false;

  /** Flag indicating that an event is ended          */
  public boolean m_elemIsEnded = false;
  
  /**
   * The pending attributes.  We have to delay the call to
   * m_flistener.startElement(name, atts) because of the
   * xsl:attribute and xsl:copy calls.  In other words,
   * the attributes have to be fully collected before you
   * can call startElement.
   */
  protected MutableAttrListImpl m_attributes = new MutableAttrListImpl();

  /**
   * Flag to try and get the xmlns decls to the attribute list
   * before other attributes are added.
   */
  protected boolean m_nsDeclsHaveBeenAdded = false;

  /**
   * The pending element, namespace, and local name.
   */
  protected String m_name;

  /** Namespace URL of the element          */
  protected String m_url;

  /** Local part of qualified name of the element           */
  protected String m_localName;
  
  
  /** Vector of namespaces for this element          */
  protected Vector m_namespaces = null;

//  /**
//   * Get the queued element.
//   *
//   * @return the queued element.
//   */
//  QueuedStartElement getQueuedElem()
//  {
//    return (m_eventCount > 1) ? m_startElement : null;
//  }

  /**
   * To re-initialize the document and element events 
   *
   */
  protected void reInitEvents()
  {
  }

  /**
   * Push document event and re-initialize events  
   *
   */
  public void reset()
  {
    pushDocumentEvent();
    reInitEvents();
  }

  /**
   * Push the document event.  This never gets popped.
   */
  void pushDocumentEvent()
  {

    // m_startDoc.setPending(true);
    // initQSE(m_startDoc);
    m_docPending = true;

    m_eventCount++;
  }

  /**
   * Pop element event 
   *
   */
  void popEvent()
  {
    m_elemIsPending = false;
    m_attributes.clear();

    m_nsDeclsHaveBeenAdded = false;
    m_name = null;
    m_url = null;
    m_localName = null;
    m_namespaces = null;

    m_eventCount--;
  }

  /** Instance of a serializer          */
  private org.apache.xml.serializer.Serializer m_serializer;

  /**
   * This is only for use of object pooling, so that
   * it can be reset.
   *
   * @param s non-null instance of a serializer 
   */
  void setSerializer(org.apache.xml.serializer.Serializer s)
  {
    m_serializer = s;
  }

  /**
   * This is only for use of object pooling, so the that
   * it can be reset.
   *
   * @return The serializer
   */
  org.apache.xml.serializer.Serializer getSerializer()
  {
    return m_serializer;
  }
}
