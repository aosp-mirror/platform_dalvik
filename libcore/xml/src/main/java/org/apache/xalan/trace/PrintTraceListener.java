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
 * $Id: PrintTraceListener.java 468644 2006-10-28 06:56:42Z minchau $
 */
package org.apache.xalan.trace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.xml.transform.SourceLocator;

import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xml.serializer.SerializerTrace;

import org.w3c.dom.Node;

/**
 * Implementation of the TraceListener interface that
 * prints each event to standard out as it occurs.
 *
 * @see org.apache.xalan.trace.TracerEvent
 * @xsl.usage advanced
 */
public class PrintTraceListener implements TraceListenerEx3
{

  /**
   * Construct a trace listener.
   *
   * @param pw PrintWriter to use for tracing events
   */
  public PrintTraceListener(java.io.PrintWriter pw)
  {
    m_pw = pw;
  }

  /**
   * The print writer where the events should be written.
   */
  java.io.PrintWriter m_pw;

  /**
   * This needs to be set to true if the listener is to print an event whenever a template is invoked.
   */
  public boolean m_traceTemplates = false;

  /**
   * Set to true if the listener is to print events that occur as each node is 'executed' in the stylesheet.
   */
  public boolean m_traceElements = false;

  /**
   * Set to true if the listener is to print information after each result-tree generation event.
   */
  public boolean m_traceGeneration = false;

  /**
   * Set to true if the listener is to print information after each selection event.
   */
  public boolean m_traceSelection = false;

  /**
   * Set to true if the listener is to print information after each extension event.
   */
  public boolean m_traceExtension = false;

  /**
   * Print information about a TracerEvent.
   *
   * @param ev the trace event.
   */
  public void _trace(TracerEvent ev)
  {

    switch (ev.m_styleNode.getXSLToken())
    {
    case Constants.ELEMNAME_TEXTLITERALRESULT :
      if (m_traceElements)
      {
        m_pw.print(ev.m_styleNode.getSystemId()+ " Line #" + ev.m_styleNode.getLineNumber() + ", "
                   + "Column #" + ev.m_styleNode.getColumnNumber() + " -- "
                   + ev.m_styleNode.getNodeName() + ": ");

        ElemTextLiteral etl = (ElemTextLiteral) ev.m_styleNode;
        String chars = new String(etl.getChars(), 0, etl.getChars().length);

        m_pw.println("    " + chars.trim());
      }
      break;
    case Constants.ELEMNAME_TEMPLATE :
      if (m_traceTemplates || m_traceElements)
      {
        ElemTemplate et = (ElemTemplate) ev.m_styleNode;

        m_pw.print(et.getSystemId()+ " Line #" + et.getLineNumber() + ", " + "Column #"
                   + et.getColumnNumber() + ": " + et.getNodeName() + " ");

        if (null != et.getMatch())
        {
          m_pw.print("match='" + et.getMatch().getPatternString() + "' ");
        }

        if (null != et.getName())
        {
          m_pw.print("name='" + et.getName() + "' ");
        }

        m_pw.println();
      }
      break;
    default :
      if (m_traceElements)
      {
        m_pw.println(ev.m_styleNode.getSystemId()+ " Line #" + ev.m_styleNode.getLineNumber() + ", "
                     + "Column #" + ev.m_styleNode.getColumnNumber() + ": "
                     + ev.m_styleNode.getNodeName());
      }
    }
  }
  
  int m_indent = 0;
  
  /**
   * Print information about a TracerEvent.
   *
   * @param ev the trace event.
   */
  public void trace(TracerEvent ev)
  {
//  	m_traceElements = true;
//  	m_traceTemplates = true;
//  	
//  	for(int i = 0; i < m_indent; i++)
//  		m_pw.print(" ");
//    m_indent = m_indent+2;
//  	m_pw.print("trace: ");
	_trace(ev);
  }
  
  /**
   * Method that is called when the end of a trace event occurs.
   * The method is blocking.  It must return before processing continues.
   *
   * @param ev the trace event.
   */
  public void traceEnd(TracerEvent ev)
  {
//  	m_traceElements = true;
//  	m_traceTemplates = true;
//  	
//  	m_indent = m_indent-2;
//  	for(int i = 0; i < m_indent; i++)
//  		m_pw.print(" ");
//  	m_pw.print("etrac: ");
//	_trace(ev);
  }


  /**
   * Method that is called just after a select attribute has been evaluated.
   *
   * @param ev the generate event.
   *
   * @throws javax.xml.transform.TransformerException
   */
public void selected(SelectionEvent ev)
    throws javax.xml.transform.TransformerException {

    if (m_traceSelection) {
        ElemTemplateElement ete = (ElemTemplateElement) ev.m_styleNode;
        Node sourceNode = ev.m_sourceNode;

        SourceLocator locator = null;
        if (sourceNode instanceof DTMNodeProxy) {
            int nodeHandler = ((DTMNodeProxy) sourceNode).getDTMNodeNumber();
            locator =
                ((DTMNodeProxy) sourceNode).getDTM().getSourceLocatorFor(
                    nodeHandler);
        }

        if (locator != null)
            m_pw.println(
                "Selected source node '"
                    + sourceNode.getNodeName()
                    + "', at "
                    + locator);
        else
            m_pw.println(
                "Selected source node '" + sourceNode.getNodeName() + "'");

        if (ev.m_styleNode.getLineNumber() == 0) {

            // You may not have line numbers if the selection is occuring from a
            // default template.
            ElemTemplateElement parent =
                (ElemTemplateElement) ete.getParentElem();

            if (parent == ete.getStylesheetRoot().getDefaultRootRule()) {
                m_pw.print("(default root rule) ");
            } else if (
                parent == ete.getStylesheetRoot().getDefaultTextRule()) {
                m_pw.print("(default text rule) ");
            } else if (parent == ete.getStylesheetRoot().getDefaultRule()) {
                m_pw.print("(default rule) ");
            }

            m_pw.print(
                ete.getNodeName()
                    + ", "
                    + ev.m_attributeName
                    + "='"
                    + ev.m_xpath.getPatternString()
                    + "': ");
        } else {
            m_pw.print(
                ev.m_styleNode.getSystemId()
                    + " Line #"
                    + ev.m_styleNode.getLineNumber()
                    + ", "
                    + "Column #"
                    + ev.m_styleNode.getColumnNumber()
                    + ": "
                    + ete.getNodeName()
                    + ", "
                    + ev.m_attributeName
                    + "='"
                    + ev.m_xpath.getPatternString()
                    + "': ");
        }

        if (ev.m_selection.getType() == ev.m_selection.CLASS_NODESET) {
            m_pw.println();

            org.apache.xml.dtm.DTMIterator nl = ev.m_selection.iter();
            
            // The following lines are added to fix bug#16222.
            // The main cause is that the following loop change the state of iterator, which is shared
            // with the transformer. The fix is that we record the initial state before looping, then 
            // restore the state when we finish it, which is done in the following lines added.
            int currentPos = DTM.NULL;
            currentPos = nl.getCurrentPos();
            nl.setShouldCacheNodes(true); // This MUST be done before we clone the iterator!
            org.apache.xml.dtm.DTMIterator clone = null;
            // End of block
            
            try {
                clone = nl.cloneWithReset();
            } catch (CloneNotSupportedException cnse) {
                m_pw.println(
                    "     [Can't trace nodelist because it it threw a CloneNotSupportedException]");
                return;
            }
            int pos = clone.nextNode();

            if (DTM.NULL == pos) {
                m_pw.println("     [empty node list]");
            } else {
                while (DTM.NULL != pos) {
                    // m_pw.println("     " + ev.m_processor.getXPathContext().getDTM(pos).getNode(pos));
                    DTM dtm = ev.m_processor.getXPathContext().getDTM(pos);
                    m_pw.print("     ");
                    m_pw.print(Integer.toHexString(pos));
                    m_pw.print(": ");
                    m_pw.println(dtm.getNodeName(pos));
                    pos = clone.nextNode();
                }
            }
			
            // Restore the initial state of the iterator, part of fix for bug#16222.
            nl.runTo(-1);
            nl.setCurrentPos(currentPos);
        	// End of fix for bug#16222
			
        } else {
            m_pw.println(ev.m_selection.str());
        }
    }
}
  /**
   * Method that is called after an xsl:apply-templates or xsl:for-each 
   * selection occurs.
   *
   * @param ev the generate event.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void selectEnd(EndSelectionEvent ev) 
     throws javax.xml.transform.TransformerException
  {
  	// Nothing for right now.
  }


  /**
   * Print information about a Generate event.
   *
   * @param ev the trace event.
   */
  public void generated(GenerateEvent ev)
  {

    if (m_traceGeneration)
    {
      switch (ev.m_eventtype)
      {
      case SerializerTrace.EVENTTYPE_STARTDOCUMENT :
        m_pw.println("STARTDOCUMENT");
        break;
      case SerializerTrace.EVENTTYPE_ENDDOCUMENT :
        m_pw.println("ENDDOCUMENT");
        break;
      case SerializerTrace.EVENTTYPE_STARTELEMENT :
        m_pw.println("STARTELEMENT: " + ev.m_name);
        break;
      case SerializerTrace.EVENTTYPE_ENDELEMENT :
        m_pw.println("ENDELEMENT: " + ev.m_name);
        break;
      case SerializerTrace.EVENTTYPE_CHARACTERS :
      {
        String chars = new String(ev.m_characters, ev.m_start, ev.m_length);

        m_pw.println("CHARACTERS: " + chars);
      }
      break;
      case SerializerTrace.EVENTTYPE_CDATA :
      {
        String chars = new String(ev.m_characters, ev.m_start, ev.m_length);

        m_pw.println("CDATA: " + chars);
      }
      break;
      case SerializerTrace.EVENTTYPE_COMMENT :
        m_pw.println("COMMENT: " + ev.m_data);
        break;
      case SerializerTrace.EVENTTYPE_PI :
        m_pw.println("PI: " + ev.m_name + ", " + ev.m_data);
        break;
      case SerializerTrace.EVENTTYPE_ENTITYREF :
        m_pw.println("ENTITYREF: " + ev.m_name);
        break;
      case SerializerTrace.EVENTTYPE_IGNORABLEWHITESPACE :
        m_pw.println("IGNORABLEWHITESPACE");
        break;
      }
    }
  }

  /**
   * Print information about an extension event.
   *
   * @param ev the extension event to print information about
   */
  public void extension(ExtensionEvent ev) {
    if (m_traceExtension) {
      switch (ev.m_callType) {
        case ExtensionEvent.DEFAULT_CONSTRUCTOR:
          m_pw.println("EXTENSION: " + ((Class)ev.m_method).getName() + "#<init>");
          break;
        case ExtensionEvent.METHOD:
          m_pw.println("EXTENSION: " + ((Method)ev.m_method).getDeclaringClass().getName() + "#" + ((Method)ev.m_method).getName());
          break;
        case ExtensionEvent.CONSTRUCTOR:
          m_pw.println("EXTENSION: " + ((Constructor)ev.m_method).getDeclaringClass().getName() + "#<init>");
          break;
      }
    }
  }


  /**
   * Print information about an extension event.
   *
   * @param ev the extension event to print information about
   */
  public void extensionEnd(ExtensionEvent ev) {
    // do nothing
  }

}
