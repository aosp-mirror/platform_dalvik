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
 * $Id: TraceManager.java 468644 2006-10-28 06:56:42Z minchau $
 */
package org.apache.xalan.trace;

import java.lang.reflect.Method;
import java.util.TooManyListenersException;
import java.util.Vector;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;

/**
 * This class manages trace listeners, and acts as an
 * interface for the tracing functionality in Xalan.
 */
public class TraceManager
{

  /** A transformer instance */
  private TransformerImpl m_transformer;

  /**
   * Constructor for the trace manager.
   *
   * @param transformer a non-null instance of a transformer
   */
  public TraceManager(TransformerImpl transformer)
  {
    m_transformer = transformer;
  }

  /**
   * List of listeners who are interested in tracing what's
   * being generated.
   */
  private Vector m_traceListeners = null;

  /**
   * Add a trace listener for the purposes of debugging and diagnosis.
   * @param tl Trace listener to be added.
   *
   * @throws TooManyListenersException
   */
  public void addTraceListener(TraceListener tl)
          throws TooManyListenersException
  {

    m_transformer.setDebug(true);

    if (null == m_traceListeners)
      m_traceListeners = new Vector();

    m_traceListeners.addElement(tl);
  }

  /**
   * Remove a trace listener.
   * @param tl Trace listener to be removed.
   */
  public void removeTraceListener(TraceListener tl)
  {

    if (null != m_traceListeners)
    {
      m_traceListeners.removeElement(tl);
      
      // The following line added to fix the bug#5140: hasTraceListeners() returns true
      // after adding and removing a listener.
      // Check: if m_traceListeners is empty, then set it to NULL.
      if (0 == m_traceListeners.size()) m_traceListeners = null;
    }
  }

  /**
   * Fire a generate event.
   *
   * @param te Generate Event to fire
   */
  public void fireGenerateEvent(GenerateEvent te)
  {

    if (null != m_traceListeners)
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);

        tl.generated(te);
      }
    }
  }

  /**
   * Tell if trace listeners are present.
   *
   * @return True if there are trace listeners
   */
  public boolean hasTraceListeners()
  {
    return (null != m_traceListeners);
  }

  /**
   * Fire a trace event.
   *
   * @param styleNode Stylesheet template node
   */
  public void fireTraceEvent(ElemTemplateElement styleNode)
  {

    if (hasTraceListeners())
    {
      int sourceNode = m_transformer.getXPathContext().getCurrentNode();
      Node source = getDOMNodeFromDTM(sourceNode);

      fireTraceEvent(new TracerEvent(m_transformer, source, 
                     m_transformer.getMode(),  /*sourceNode, mode,*/
                                     styleNode));
    }
  }

  /**
   * Fire a end trace event, after all children of an element have been
   * executed.
   *
   * @param styleNode Stylesheet template node
   */
  public void fireTraceEndEvent(ElemTemplateElement styleNode)
  {

    if (hasTraceListeners())
    {
      int sourceNode = m_transformer.getXPathContext().getCurrentNode();
      Node source = getDOMNodeFromDTM(sourceNode);

      fireTraceEndEvent(new TracerEvent(m_transformer, source,
                     m_transformer.getMode(),  /*sourceNode, mode,*/
                                     styleNode));
    }
  }

  /**
   * Fire a trace event.
   *
   * @param te Trace event to fire
   */
  public void fireTraceEndEvent(TracerEvent te)
  {

    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);
        if(tl instanceof TraceListenerEx2)
        {
          ((TraceListenerEx2)tl).traceEnd(te);
        }
      }
    }
  }



  /**
   * Fire a trace event.
   *
   * @param te Trace event to fire
   */
  public void fireTraceEvent(TracerEvent te)
  {

    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);

        tl.trace(te);
      }
    }
  }

  /**
   * Fire a selection event.
   *
   * @param sourceNode Current source node
   * @param styleNode node in the style tree reference for the event.
   * @param attributeName The attribute name from which the selection is made.
   * @param xpath The XPath that executed the selection.
   * @param selection The result of the selection.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void fireSelectedEvent(
          int sourceNode, ElemTemplateElement styleNode, String attributeName, 
          XPath xpath, XObject selection)
            throws javax.xml.transform.TransformerException
  {

    if (hasTraceListeners())
    {
      Node source = getDOMNodeFromDTM(sourceNode);
        
      fireSelectedEvent(new SelectionEvent(m_transformer, source, styleNode,
                                           attributeName, xpath, selection));
    }
  }
  
  /**
   * Fire a selection event.
   *
   * @param sourceNode Current source node
   * @param styleNode node in the style tree reference for the event.
   * @param attributeName The attribute name from which the selection is made.
   * @param xpath The XPath that executed the selection.
   * @param selection The result of the selection.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void fireSelectedEndEvent(
          int sourceNode, ElemTemplateElement styleNode, String attributeName, 
          XPath xpath, XObject selection)
            throws javax.xml.transform.TransformerException
  {

    if (hasTraceListeners())
    {
      Node source = getDOMNodeFromDTM(sourceNode);
        
      fireSelectedEndEvent(new EndSelectionEvent(m_transformer, source, styleNode,
                                           attributeName, xpath, selection));
    }
  }
  
  /**
   * Fire a selection event.
   *
   * @param se Selection event to fire
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void fireSelectedEndEvent(EndSelectionEvent se)
          throws javax.xml.transform.TransformerException
  {

    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);

        if(tl instanceof TraceListenerEx)
          ((TraceListenerEx)tl).selectEnd(se);
      }
    }
  }

  /**
   * Fire a selection event.
   *
   * @param se Selection event to fire
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void fireSelectedEvent(SelectionEvent se)
          throws javax.xml.transform.TransformerException
  {

    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);

        tl.selected(se);
      }
    }
  }
  

  /**
   * Fire an end extension event.
   *
   * @see java.lang.reflect.Method#invoke
   * 
   * @param method The java method about to be executed
   * @param instance The instance the method will be executed on
   * @param arguments Parameters passed to the method.
   */
  public void fireExtensionEndEvent(Method method, Object instance, Object[] arguments)
  {
      ExtensionEvent ee = new ExtensionEvent(m_transformer, method, instance, arguments);

    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);
        if(tl instanceof TraceListenerEx3)
        {
          ((TraceListenerEx3)tl).extensionEnd(ee);
        }
      }
    }
  }

  /**
   * Fire an end extension event.
   *
   * @see java.lang.reflect.Method#invoke
   * 
   * @param method The java method about to be executed
   * @param instance The instance the method will be executed on
   * @param arguments Parameters passed to the method.
   */
  public void fireExtensionEvent(Method method, Object instance, Object[] arguments)
  {
    ExtensionEvent ee = new ExtensionEvent(m_transformer, method, instance, arguments);

    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);
        if(tl instanceof TraceListenerEx3)
        {
          ((TraceListenerEx3)tl).extension(ee);
        }
      }
    }
  }

  /**
   * Fire an end extension event.
   *
   * @see java.lang.reflect.Method#invoke
   * 
   * @param ee the ExtensionEvent to fire
   */
  public void fireExtensionEndEvent(ExtensionEvent ee)
  {
    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);
        if(tl instanceof TraceListenerEx3)
        {
          ((TraceListenerEx3)tl).extensionEnd(ee);
        }
      }
    }
  }

  /**
   * Fire an end extension event.
   *
   * @see java.lang.reflect.Method#invoke
   * 
   * @param ee the ExtensionEvent to fire
   */
  public void fireExtensionEvent(ExtensionEvent ee)
  {    
      
    if (hasTraceListeners())
    {
      int nListeners = m_traceListeners.size();

      for (int i = 0; i < nListeners; i++)
      {
        TraceListener tl = (TraceListener) m_traceListeners.elementAt(i);
        if(tl instanceof TraceListenerEx3)
        {
          ((TraceListenerEx3)tl).extension(ee);
        }
      }
    }
  }
  
  /**
   * Get the DOM Node of the current XPath context, which is possibly null.
   * @param sourceNode the handle on the node used by a DTM.
   */
  private Node getDOMNodeFromDTM(int sourceNode) {
    org.apache.xml.dtm.DTM dtm = m_transformer.getXPathContext().getDTM(sourceNode);
    final Node source = (dtm == null) ? null : dtm.getNode(sourceNode);
    return source;
  }
}
