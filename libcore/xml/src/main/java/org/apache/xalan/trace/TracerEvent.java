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
 * $Id: TracerEvent.java 468644 2006-10-28 06:56:42Z minchau $
 */
package org.apache.xalan.trace;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parent class of events generated for tracing the
 * progress of the XSL processor.
 * @xsl.usage advanced
 */
public class TracerEvent implements java.util.EventListener
{

  /**
   * The node in the style tree where the event occurs.
   */
  public final ElemTemplateElement m_styleNode;

  /**
   * The XSLT processor instance.
   */
  public final TransformerImpl m_processor;

  /**
   * The current context node.
   */
  public final Node m_sourceNode;

  /**
   * The current mode.
   */
  public final QName m_mode;

  /**
   * Create an event originating at the given node of the style tree.
   * @param processor The XSLT TransformerFactory.
   * @param sourceNode The current context node.
   * @param mode The current mode.
   * @param styleNode The stylesheet element that is executing.
   */
  public TracerEvent(TransformerImpl processor, Node sourceNode, QName mode,
                     ElemTemplateElement styleNode)
  {

    this.m_processor = processor;
    this.m_sourceNode = sourceNode;
    this.m_mode = mode;
    this.m_styleNode = styleNode;
  }

  /**
   * Returns a string representation of the node.
   * The string returned for elements will contain the element name
   * and any attributes enclosed in angle brackets.
   * The string returned for attributes will be of form, "name=value."
   *
   * @param n any DOM node. Must not be null.
   *
   * @return a string representation of the given node.
   */
  public static String printNode(Node n)
  {

    String r = n.hashCode() + " ";

    if (n instanceof Element)
    {
      r += "<" + n.getNodeName();

      Node c = n.getFirstChild();

      while (null != c)
      {
        if (c instanceof Attr)
        {
          r += printNode(c) + " ";
        }

        c = c.getNextSibling();
      }

      r += ">";
    }
    else
    {
      if (n instanceof Attr)
      {
        r += n.getNodeName() + "=" + n.getNodeValue();
      }
      else
      {
        r += n.getNodeName();
      }
    }

    return r;
  }

  /**
   * Returns a string representation of the node list.
   * The string will contain the list of nodes inside square braces.
   * Elements will contain the element name
   * and any attributes enclosed in angle brackets.
   * Attributes will be of form, "name=value."
   *
   * @param l any DOM node list. Must not be null.
   *
   * @return a string representation of the given node list.
   */
  public static String printNodeList(NodeList l)
  {

    String r = l.hashCode() + "[";
    int len = l.getLength() - 1;
    int i = 0;

    while (i < len)
    {
      Node n = l.item(i);

      if (null != n)
      {
        r += printNode(n) + ", ";
      }

      ++i;
    }

    if (i == len)
    {
      Node n = l.item(len);

      if (null != n)
      {
        r += printNode(n);
      }
    }

    return r + "]";
  }
}
