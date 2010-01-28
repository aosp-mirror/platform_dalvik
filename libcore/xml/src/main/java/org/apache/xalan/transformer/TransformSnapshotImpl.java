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
 * $Id: TransformSnapshotImpl.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.util.Enumeration;
import java.util.Stack;

import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.NamespaceSupport2;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.ObjectStack;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;

import org.xml.sax.helpers.NamespaceSupport;

import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.SerializationHandler;
/**
 * This class holds a "snapshot" of it's current transformer state,
 * which can later be restored.
 *
 * This only saves state which can change over the course of the side-effect-free
 * (i.e. no extensions that call setURIResolver, etc.).
 * 
 * @deprecated  It doesn't look like this code, which is for tooling, has
 * functioned propery for a while, so it doesn't look like it is being used.
 */
class TransformSnapshotImpl implements TransformSnapshot
{

  /**
   * The stack of Variable stack frames.
   */
  private VariableStack m_variableStacks;

  /**
   * The stack of <a href="http://www.w3.org/TR/xslt#dt-current-node">current node</a> objects.
   *  Not to be confused with the current node list.  
   */
  private IntStack m_currentNodes;

  /** A stack of the current sub-expression nodes. */
  private IntStack m_currentExpressionNodes;

  /**
   * The current context node lists stack.
   */
  private Stack m_contextNodeLists;

  /**
   * The current context node list.
   */
  private DTMIterator m_contextNodeList;

  /**
   * Stack of AxesIterators.
   */
  private Stack m_axesIteratorStack;

  /**
   * Is > 0 when we're processing a for-each.
   */
  private BoolStack m_currentTemplateRuleIsNull;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplateElement.  Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the calling template. 
   */
  private ObjectStack m_currentTemplateElements;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplate that was matched, as well as the node that
   * was matched.  Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the matched template, and matched
   * node. 
   */
  private Stack m_currentMatchTemplates;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplate that was matched, as well as the node that
   * was matched.  Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the matched template, and matched
   * node. 
   */
  private NodeVector m_currentMatchNodes;

  /**
   * The table of counters for xsl:number support.
   * @see ElemNumber
   */
  private CountersTable m_countersTable;

  /**
   * Stack for the purposes of flagging infinite recursion with
   * attribute sets.
   */
  private Stack m_attrSetStack;

  /** Indicate whether a namespace context was pushed */
  boolean m_nsContextPushed;

  /**
   * Use the SAX2 helper class to track result namespaces.
   */
  private NamespaceMappings m_nsSupport;

  /** The number of events queued */
//  int m_eventCount;

  /**
   * Constructor TransformSnapshotImpl
   * Take a snapshot of the currently executing context.
   *
   * @param transformer Non null transformer instance
   * @deprecated  It doesn't look like this code, which is for tooling, has
   * functioned propery for a while, so it doesn't look like it is being used.
   */
  TransformSnapshotImpl(TransformerImpl transformer)
  {

    try
    {

      // Are all these clones deep enough?
      SerializationHandler rtf = transformer.getResultTreeHandler();

      {
        // save serializer fields
        m_nsSupport = (NamespaceMappings)rtf.getNamespaceMappings().clone();
        
        // Do other fields need to be saved/restored?
      }
 
      XPathContext xpc = transformer.getXPathContext();

      m_variableStacks = (VariableStack) xpc.getVarStack().clone();
      m_currentNodes = (IntStack) xpc.getCurrentNodeStack().clone();
      m_currentExpressionNodes =
        (IntStack) xpc.getCurrentExpressionNodeStack().clone();
      m_contextNodeLists = (Stack) xpc.getContextNodeListsStack().clone();

      if (!m_contextNodeLists.empty())
        m_contextNodeList =
          (DTMIterator) xpc.getContextNodeList().clone();

      m_axesIteratorStack = (Stack) xpc.getAxesIteratorStackStacks().clone();
      m_currentTemplateRuleIsNull =
        (BoolStack) transformer.m_currentTemplateRuleIsNull.clone();
      m_currentTemplateElements =
        (ObjectStack) transformer.m_currentTemplateElements.clone();
      m_currentMatchTemplates =
        (Stack) transformer.m_currentMatchTemplates.clone();
      m_currentMatchNodes =
        (NodeVector) transformer.m_currentMatchedNodes.clone();
      m_countersTable =
        (CountersTable) transformer.getCountersTable().clone();

      if (transformer.m_attrSetStack != null)
        m_attrSetStack = (Stack) transformer.m_attrSetStack.clone();
    }
    catch (CloneNotSupportedException cnse)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(cnse);
    }
  }

  /**
   * This will reset the stylesheet to a given execution context
   * based on some previously taken snapshot where we can then start execution 
   *
   * @param transformer Non null transformer instance
   * 
   * @deprecated  It doesn't look like this code, which is for tooling, has
   * functioned propery for a while, so it doesn't look like it is being used.
   */
  void apply(TransformerImpl transformer)
  {

    try
    {

      // Are all these clones deep enough?
      SerializationHandler rtf = transformer.getResultTreeHandler();

      if (rtf != null)
      {
        // restore serializer fields
         rtf.setNamespaceMappings((NamespaceMappings)m_nsSupport.clone());
      }

      XPathContext xpc = transformer.getXPathContext();

      xpc.setVarStack((VariableStack) m_variableStacks.clone());
      xpc.setCurrentNodeStack((IntStack) m_currentNodes.clone());
      xpc.setCurrentExpressionNodeStack(
        (IntStack) m_currentExpressionNodes.clone());
      xpc.setContextNodeListsStack((Stack) m_contextNodeLists.clone());

      if (m_contextNodeList != null)
        xpc.pushContextNodeList((DTMIterator) m_contextNodeList.clone());

      xpc.setAxesIteratorStackStacks((Stack) m_axesIteratorStack.clone());

      transformer.m_currentTemplateRuleIsNull =
        (BoolStack) m_currentTemplateRuleIsNull.clone();
      transformer.m_currentTemplateElements =
        (ObjectStack) m_currentTemplateElements.clone();
      transformer.m_currentMatchTemplates =
        (Stack) m_currentMatchTemplates.clone();
      transformer.m_currentMatchedNodes =
        (NodeVector) m_currentMatchNodes.clone();
      transformer.m_countersTable = (CountersTable) m_countersTable.clone();

      if (m_attrSetStack != null)
        transformer.m_attrSetStack = (Stack) m_attrSetStack.clone();
    }
    catch (CloneNotSupportedException cnse)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(cnse);
    }
  }
}
