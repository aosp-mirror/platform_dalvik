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
 * $Id: ExtensionHandlerGeneral.java 469672 2006-10-31 21:56:19Z minchau $
 */
package org.apache.xalan.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.ref.DTMNodeList;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xpath.XPathProcessorException;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;

/**
 * Class handling an extension namespace for XPath. Provides functions
 * to test a function's existence and call a function
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @xsl.usage internal
 */
public class ExtensionHandlerGeneral extends ExtensionHandler
{

  /** script source to run (if any)      */
  private String m_scriptSrc;   

  /** URL of source of script (if any)         */
  private String m_scriptSrcURL;  

  /** functions of namespace        */
  private Hashtable m_functions = new Hashtable();  

  /** elements of namespace         */
  private Hashtable m_elements = new Hashtable();   

  // BSF objects used to invoke BSF by reflection.  Do not import the BSF classes
  // since we don't want a compile dependency on BSF.

  /** BSF manager used to run scripts */
  private Object m_engine;

  /** Engine call to invoke scripts */
  private Method m_engineCall = null;

  // static fields

  /** BSFManager package name */
  private static String BSF_MANAGER ;
  
  /** Default BSFManager name */
  private static final String DEFAULT_BSF_MANAGER = "org.apache.bsf.BSFManager";
  
  /** Property name to load the BSFManager class */
  private static final String propName = "org.apache.xalan.extensions.bsf.BSFManager";
  
  /** Integer Zero */
  private static final Integer ZEROINT = new Integer(0);

  static{
          BSF_MANAGER =  ObjectFactory.lookUpFactoryClassName(propName, null, null);
 
          if (BSF_MANAGER == null){
                  BSF_MANAGER = DEFAULT_BSF_MANAGER;               
          }          
  }

  /**
   * Construct a new extension namespace handler given all the information
   * needed.
   *
   * @param namespaceUri the extension namespace URI that I'm implementing
   * @param elemNames Vector of element names
   * @param funcNames    string containing list of functions of extension NS
   * @param scriptLang Scripting language of implementation
   * @param scriptSrcURL URL of source script
   * @param scriptSrc    the actual script code (if any)
   * @param systemId
   *
   * @throws TransformerException
   */
  public ExtensionHandlerGeneral(
          String namespaceUri, StringVector elemNames, StringVector funcNames, String scriptLang, String scriptSrcURL, String scriptSrc, String systemId)
            throws TransformerException
  {

    super(namespaceUri, scriptLang);

    if (elemNames != null)
    {
      Object junk = new Object();
      int n = elemNames.size();

      for (int i = 0; i < n; i++)
      {
        String tok = elemNames.elementAt(i);

        m_elements.put(tok, junk);  // just stick it in there basically
      }
    }

    if (funcNames != null)
    {
      Object junk = new Object();
      int n = funcNames.size();

      for (int i = 0; i < n; i++)
      {
        String tok = funcNames.elementAt(i);

        m_functions.put(tok, junk);  // just stick it in there basically
      }
    }

    m_scriptSrcURL = scriptSrcURL;
    m_scriptSrc = scriptSrc;

    if (m_scriptSrcURL != null)
    {
      URL url = null;
      try{
        url = new URL(m_scriptSrcURL);
      }
      catch (java.net.MalformedURLException mue)
      {
        int indexOfColon = m_scriptSrcURL.indexOf(':');
        int indexOfSlash = m_scriptSrcURL.indexOf('/');

        if ((indexOfColon != -1) && (indexOfSlash != -1)
            && (indexOfColon < indexOfSlash))
        {
          // The url is absolute.
          url = null;
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_COULD_NOT_FIND_EXTERN_SCRIPT, new Object[]{m_scriptSrcURL}), mue); //"src attribute not yet supported for "
          //+ scriptLang);
        }
        else
        {
          try{
            url = new URL(new URL(SystemIDResolver.getAbsoluteURI(systemId)), m_scriptSrcURL);          
          }        
          catch (java.net.MalformedURLException mue2)
          {
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_COULD_NOT_FIND_EXTERN_SCRIPT, new Object[]{m_scriptSrcURL}), mue2); //"src attribute not yet supported for "
          //+ scriptLang);
          }
        }
      }
      if (url != null)
      {
        try
        {
          URLConnection uc = url.openConnection();
          InputStream is = uc.getInputStream();
          byte []bArray = new byte[uc.getContentLength()];
          is.read(bArray);
          m_scriptSrc = new String(bArray);
          
        }
        catch (IOException ioe)
        {
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_COULD_NOT_FIND_EXTERN_SCRIPT, new Object[]{m_scriptSrcURL}), ioe); //"src attribute not yet supported for "
          //+ scriptLang);
        }
      }
      
    }

    Object manager = null;
    try
    {
      manager = ObjectFactory.newInstance(
        BSF_MANAGER, ObjectFactory.findClassLoader(), true);
    }
    catch (ObjectFactory.ConfigurationError e)
    {
      e.printStackTrace();
    }

    if (manager == null)
    {
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_INIT_BSFMGR, null)); //"Could not initialize BSF manager");
    }

    try
    {
      Method loadScriptingEngine = manager.getClass()
        .getMethod("loadScriptingEngine", new Class[]{ String.class });

      m_engine = loadScriptingEngine.invoke(manager,
        new Object[]{ scriptLang });

      Method engineExec = m_engine.getClass().getMethod("exec",
        new Class[]{ String.class, Integer.TYPE, Integer.TYPE, Object.class });

      // "Compile" the program
      engineExec.invoke(m_engine,
        new Object[]{ "XalanScript", ZEROINT, ZEROINT, m_scriptSrc });
    }
    catch (Exception e)
    {
      e.printStackTrace();

      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_CMPL_EXTENSN, null), e); //"Could not compile extension", e);
    }
  }

  /**
   * Tests whether a certain function name is known within this namespace.
   * @param function name of the function being tested
   * @return true if its known, false if not.
   */
  public boolean isFunctionAvailable(String function)
  {
    return (m_functions.get(function) != null);
  }

  /**
   * Tests whether a certain element name is known within this namespace.
   * @param element name of the element being tested
   * @return true if its known, false if not.
   */
  public boolean isElementAvailable(String element)
  {
    return (m_elements.get(element) != null);
  }

  /**
   * Process a call to a function.
   *
   * @param funcName Function name.
   * @param args     The arguments of the function call.
   * @param methodKey A key that uniquely identifies this class and method call.
   * @param exprContext The context in which this expression is being executed.
   *
   * @return the return value of the function evaluation.
   *
   * @throws TransformerException          if parsing trouble
   */
  public Object callFunction(
          String funcName, Vector args, Object methodKey, ExpressionContext exprContext)
            throws TransformerException
  {

    Object[] argArray;

    try
    {
      argArray = new Object[args.size()];

      for (int i = 0; i < argArray.length; i++)
      {
        Object o = args.get(i);

        argArray[i] = (o instanceof XObject) ? ((XObject) o).object() : o;
        o = argArray[i];
        if(null != o && o instanceof DTMIterator)
        {
          argArray[i] = new DTMNodeList((DTMIterator)o);
        }
      }

      if (m_engineCall == null) {
        m_engineCall = m_engine.getClass().getMethod("call",
          new Class[]{ Object.class, String.class, Object[].class });
      }

      return m_engineCall.invoke(m_engine,
        new Object[]{ null, funcName, argArray });
    }
    catch (Exception e)
    {
      e.printStackTrace();

      String msg = e.getMessage();

      if (null != msg)
      {
        if (msg.startsWith("Stopping after fatal error:"))
        {
          msg = msg.substring("Stopping after fatal error:".length());
        }

        // System.out.println("Call to extension function failed: "+msg);
        throw new TransformerException(e);
      }
      else
      {

        // Should probably make a TRaX Extension Exception.
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_CREATE_EXTENSN, new Object[]{funcName, e })); //"Could not create extension: " + funcName
                               //+ " because of: " + e);
      }
    }
  }

  /**
   * Process a call to an XPath extension function
   *
   * @param extFunction The XPath extension function
   * @param args The arguments of the function call.
   * @param exprContext The context in which this expression is being executed.
   * @return the return value of the function evaluation.
   * @throws TransformerException
   */
  public Object callFunction(FuncExtFunction extFunction,
                             Vector args,
                             ExpressionContext exprContext)
      throws TransformerException
  {
    return callFunction(extFunction.getFunctionName(), args, 
                        extFunction.getMethodKey(), exprContext);
  }

  /**
   * Process a call to this extension namespace via an element. As a side
   * effect, the results are sent to the TransformerImpl's result tree.
   *
   * @param localPart      Element name's local part.
   * @param element        The extension element being processed.
   * @param transformer      Handle to TransformerImpl.
   * @param stylesheetTree The compiled stylesheet tree.
   * @param methodKey A key that uniquely identifies this class and method call.
   *
   * @throws XSLProcessorException thrown if something goes wrong
   *            while running the extension handler.
   * @throws MalformedURLException if loading trouble
   * @throws FileNotFoundException if loading trouble
   * @throws IOException           if loading trouble
   * @throws TransformerException          if parsing trouble
   */
  public void processElement(
          String localPart, ElemTemplateElement element, TransformerImpl transformer, 
          Stylesheet stylesheetTree, Object methodKey)
            throws TransformerException, IOException
  {

    Object result = null;
    XSLProcessorContext xpc = new XSLProcessorContext(transformer, stylesheetTree);

    try
    {
      Vector argv = new Vector(2);

      argv.add(xpc);
      argv.add(element);

      result = callFunction(localPart, argv, methodKey,
                            transformer.getXPathContext().getExpressionContext());
    }
    catch (XPathProcessorException e)
    {

      // e.printStackTrace ();
      throw new TransformerException(e.getMessage(), e);
    }

    if (result != null)
    {
      xpc.outputToResultTree(stylesheetTree, result);
    }
  }
}
