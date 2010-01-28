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
 * $Id: PipeDocument.java 468639 2006-10-28 06:52:33Z minchau $
 */
package org.apache.xalan.lib;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemLiteralResult;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xpath.XPathContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 */
// Imported Serializer classes
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;

/**
 * PipeDocument is a Xalan extension element to set stylesheet params and pipes an XML 
 * document through a series of 1 or more stylesheets.
 * PipeDocument is invoked from a stylesheet as the {@link #pipeDocument pipeDocument extension element}.
 * 
 * It is accessed by specifying a namespace URI as follows:
 * <pre>
 *    xmlns:pipe="http://xml.apache.org/xalan/PipeDocument"
 * </pre>
 *
 * @author Donald Leslie
 */
public class PipeDocument
{
/**
 * Extension element for piping an XML document through a series of 1 or more transformations.
 * 
 * <pre>Common usage pattern: A stylesheet transforms a listing of documents to be
 * transformed into a TOC. For each document in the listing calls the pipeDocument
 * extension element to pipe that document through a series of 1 or more stylesheets 
 * to the desired output document.
 * 
 * Syntax:
 * &lt;xsl:stylesheet version="1.0"
 *                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 *                xmlns:pipe="http://xml.apache.org/xalan/PipeDocument"
 *                extension-element-prefixes="pipe"&gt;
 * ...
 * &lt;pipe:pipeDocument   source="source.xml" target="target.xml"&gt;
 *   &lt;stylesheet href="ss1.xsl"&gt;
 *     &lt;param name="param1" value="value1"/&gt;
 *   &lt;/stylesheet&gt;
 *   &lt;stylesheet href="ss2.xsl"&gt;
 *     &lt;param name="param1" value="value1"/&gt;
 *     &lt;param name="param2" value="value2"/&gt;
 *   &lt;/stylesheet&gt;
 *   &lt;stylesheet href="ss1.xsl"/&gt;     
 * &lt;/pipe:pipeDocument&gt;
 * 
 * Notes:</pre>
 * <ul>
 *   <li>The base URI for the source attribute is the XML "listing" document.<li/>
 *   <li>The target attribute is taken as is (base is the current user directory).<li/>
 *   <li>The stylsheet containg the extension element is the base URI for the
 *   stylesheet hrefs.<li/>
 * </ul>
 */
  public void pipeDocument(XSLProcessorContext context, ElemExtensionCall elem)
	  throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException, FileNotFoundException	   
  {

      SAXTransformerFactory saxTFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
      
      // XML doc to transform.
      String source =  elem.getAttribute("source", 
                                         context.getContextNode(),
                                         context.getTransformer());
      TransformerImpl transImpl = context.getTransformer();

      //Base URI for input doc, so base for relative URI to XML doc to transform.
      String baseURLOfSource = transImpl.getBaseURLOfSource();
      // Absolute URI for XML doc to transform.
      String absSourceURL = SystemIDResolver.getAbsoluteURI(source, baseURLOfSource);      

      // Transformation target
      String target =  elem.getAttribute("target", 
                                         context.getContextNode(),
                                         context.getTransformer());
      
      XPathContext xctxt = context.getTransformer().getXPathContext();
      int xt = xctxt.getDTMHandleFromNode(context.getContextNode());
 
      // Get System Id for stylesheet; to be used to resolve URIs to other stylesheets.
      String sysId = elem.getSystemId();
      
      NodeList ssNodes = null;
      NodeList paramNodes = null;
      Node ssNode = null;
      Node paramNode = null;
      if (elem.hasChildNodes())
      {
        ssNodes = elem.getChildNodes();        
        // Vector to contain TransformerHandler for each stylesheet.
        Vector vTHandler = new Vector(ssNodes.getLength());
        
        // The child nodes of an extension element node are instances of
        // ElemLiteralResult, which requires does not fully support the standard
        // Node interface. Accordingly, some special handling is required (see below)
        // to get attribute values.
        for (int i = 0; i < ssNodes.getLength(); i++)
        {
          ssNode = ssNodes.item(i);
          if (ssNode.getNodeType() == Node.ELEMENT_NODE
              && ((Element)ssNode).getTagName().equals("stylesheet")
              && ssNode instanceof ElemLiteralResult)
          {
            AVT avt = ((ElemLiteralResult)ssNode).getLiteralResultAttribute("href");
            String href = avt.evaluate(xctxt,xt, elem);
            String absURI = SystemIDResolver.getAbsoluteURI(href, sysId);
            Templates tmpl = saxTFactory.newTemplates(new StreamSource(absURI));
            TransformerHandler tHandler = saxTFactory.newTransformerHandler(tmpl);
            Transformer trans = tHandler.getTransformer();
            
            // AddTransformerHandler to vector
            vTHandler.addElement(tHandler);

            paramNodes = ssNode.getChildNodes();
            for (int j = 0; j < paramNodes.getLength(); j++)
            {
              paramNode = paramNodes.item(j);
              if (paramNode.getNodeType() == Node.ELEMENT_NODE 
                  && ((Element)paramNode).getTagName().equals("param")
                  && paramNode instanceof ElemLiteralResult)
              {
                 avt = ((ElemLiteralResult)paramNode).getLiteralResultAttribute("name");
                 String pName = avt.evaluate(xctxt,xt, elem);
                 avt = ((ElemLiteralResult)paramNode).getLiteralResultAttribute("value");
                 String pValue = avt.evaluate(xctxt,xt, elem);
                 trans.setParameter(pName, pValue);
               } 
             }
           }
         }
         usePipe(vTHandler, absSourceURL, target);
       }
  }
  /**
   * Uses a Vector of TransformerHandlers to pipe XML input document through
   * a series of 1 or more transformations. Called by {@link #pipeDocument}.
   * 
   * @param vTHandler Vector of Transformation Handlers (1 per stylesheet).
   * @param source absolute URI to XML input
   * @param target absolute path to transformation output.
   */
  public void usePipe(Vector vTHandler, String source, String target)
          throws TransformerException, TransformerConfigurationException, 
                 FileNotFoundException, IOException, SAXException, SAXNotRecognizedException
  {
    XMLReader reader = XMLReaderFactory.createXMLReader();
    TransformerHandler tHFirst = (TransformerHandler)vTHandler.firstElement();
    reader.setContentHandler(tHFirst);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", tHFirst);
    for (int i = 1; i < vTHandler.size(); i++)
    {
      TransformerHandler tHFrom = (TransformerHandler)vTHandler.elementAt(i-1);
      TransformerHandler tHTo = (TransformerHandler)vTHandler.elementAt(i);
      tHFrom.setResult(new SAXResult(tHTo));      
    }
    TransformerHandler tHLast = (TransformerHandler)vTHandler.lastElement();
    Transformer trans = tHLast.getTransformer();
    Properties outputProps = trans.getOutputProperties();
    Serializer serializer = SerializerFactory.getSerializer(outputProps);
    
    FileOutputStream out = new FileOutputStream(target);
    try 
    {
      serializer.setOutputStream(out);
      tHLast.setResult(new SAXResult(serializer.asContentHandler()));
      reader.parse(source);
    }
    finally 
    {
      // Always clean up the FileOutputStream,
      // even if an exception was thrown in the try block
      if (out != null)
        out.close();
    }    
  }
}
