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
 * $Id: ExtensionHandlerJavaPackage.java 469672 2006-10-31 21:56:19Z minchau $
 */
package org.apache.xalan.extensions;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.trace.ExtensionEvent;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;

/**
 * Represents an extension namespace for XPath that handles java packages
 * that may be fully or partially specified.
 * It is recommended that the class URI be of one of the following forms:
 * <pre>
 *   xalan://partial.class.name
 *   xalan://
 *   http://xml.apache.org/xalan/java (which is the same as xalan://)
 * </pre>
 * However, we do not enforce this.  If the class name contains a
 * a /, we only use the part to the right of the rightmost slash.
 * In addition, we ignore any "class:" prefix.
 * Provides functions to test a function's existence and call a function.
 * Also provides functions to test an element's existence and call an
 * element.
 *
 * @author <a href="mailto:garyp@firstech.com">Gary L Peskin</a>
 *
 * @xsl.usage internal
 */


public class ExtensionHandlerJavaPackage extends ExtensionHandlerJava
{

  /**
   * Construct a new extension namespace handler given all the information
   * needed. 
   * 
   * @param namespaceUri the extension namespace URI that I'm implementing
   * @param scriptLang   language of code implementing the extension
   * @param className    the beginning of the class name of the class.  This
   *                     should be followed by a dot (.)
   */
  public ExtensionHandlerJavaPackage(String namespaceUri,
                                     String scriptLang,
                                     String className)
  {
    super(namespaceUri, scriptLang, className);
  }


  /**
   * Tests whether a certain function name is known within this namespace.
   * Since this is for a package, we concatenate the package name used when
   * this handler was created and the function name specified in the argument.
   * There is
   * no information regarding the arguments to the function call or
   * whether the method implementing the function is a static method or
   * an instance method.
   * @param function name of the function being tested
   * @return true if its known, false if not.
   */

  public boolean isFunctionAvailable(String function) 
  {
    try
    {
      String fullName = m_className + function;
      int lastDot = fullName.lastIndexOf(".");
      if (lastDot >= 0)
      {
        Class myClass = getClassForName(fullName.substring(0, lastDot));
        Method[] methods = myClass.getMethods();
        int nMethods = methods.length;
        function = fullName.substring(lastDot + 1);
        for (int i = 0; i < nMethods; i++)
        {
          if (methods[i].getName().equals(function))
            return true;
        }
      }
    }
    catch (ClassNotFoundException cnfe) {}

    return false;
  }


  /**
   * Tests whether a certain element name is known within this namespace.
   * Looks for a method with the appropriate name and signature.
   * This method examines both static and instance methods.
   * @param element name of the element being tested
   * @return true if its known, false if not.
   */

  public boolean isElementAvailable(String element) 
  {
    try
    {
      String fullName = m_className + element;
      int lastDot = fullName.lastIndexOf(".");
      if (lastDot >= 0)
      {
        Class myClass = getClassForName(fullName.substring(0, lastDot));
        Method[] methods = myClass.getMethods();
        int nMethods = methods.length;
        element = fullName.substring(lastDot + 1);
        for (int i = 0; i < nMethods; i++)
        {
          if (methods[i].getName().equals(element))
          {
            Class[] paramTypes = methods[i].getParameterTypes();
            if ( (paramTypes.length == 2)
              && paramTypes[0].isAssignableFrom(
                                     org.apache.xalan.extensions.XSLProcessorContext.class)
              && paramTypes[1].isAssignableFrom(
                                       org.apache.xalan.templates.ElemExtensionCall.class) )
            {
              return true;
            }
          }
        }
      }
    }
    catch (ClassNotFoundException cnfe) {}

    return false;
  }


  /**
   * Process a call to a function in the package java namespace.
   * There are three possible types of calls:
   * <pre>
   *   Constructor:
   *     packagens:class.name.new(arg1, arg2, ...)
   *
   *   Static method:
   *     packagens:class.name.method(arg1, arg2, ...)
   *
   *   Instance method:
   *     packagens:method(obj, arg1, arg2, ...)
   * </pre>
   * We use the following rules to determine the type of call made:
   * <ol type="1">
   * <li>If the function name ends with a ".new", call the best constructor for
   *     class whose name is formed by concatenating the value specified on
   *     the namespace with the value specified in the function invocation
   *     before ".new".</li>
   * <li>If the function name contains a period, call the best static method "method"
   *     in the class whose name is formed by concatenating the value specified on
   *     the namespace with the value specified in the function invocation.</li>
   * <li>Otherwise, call the best instance method "method"
   *     in the class whose name is formed by concatenating the value specified on
   *     the namespace with the value specified in the function invocation.
   *     Note that a static method of the same
   *     name will <i>not</i> be called in the current implementation.  This
   *     module does not verify that the obj argument is a member of the
   *     package namespace.</li>
   * </ol>
   *
   * @param funcName Function name.
   * @param args     The arguments of the function call.
   * @param methodKey A key that uniquely identifies this class and method call.
   * @param exprContext The context in which this expression is being executed.
   * @return the return value of the function evaluation.
   *
   * @throws TransformerException          if parsing trouble
   */

  public Object callFunction (String funcName, 
                              Vector args, 
                              Object methodKey,
                              ExpressionContext exprContext)
    throws TransformerException 
  {

    String className;
    String methodName;
    Class  classObj;
    Object targetObject;
    int lastDot = funcName.lastIndexOf(".");
    Object[] methodArgs;
    Object[][] convertedArgs;
    Class[] paramTypes;

    try
    {
      TransformerImpl trans = (exprContext != null) ?
        (TransformerImpl)exprContext.getXPathContext().getOwnerObject() : null;
      if (funcName.endsWith(".new")) {                   // Handle constructor call

        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        
        Constructor c = (methodKey != null) ?
          (Constructor) getFromCache(methodKey, null, methodArgs) : null;
        
        if (c != null)
        {
          try
          {
            paramTypes = c.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, paramTypes, exprContext);
            return c.newInstance(convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
            // Must not have been the right one
          }
        }
        className = m_className + funcName.substring(0, lastDot);
        try
        {
          classObj = getClassForName(className);
        }
        catch (ClassNotFoundException e) 
        {
          throw new TransformerException(e);
        }
        c = MethodResolver.getConstructor(classObj, 
                                          methodArgs,
                                          convertedArgs,
                                          exprContext);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, c);
        
        if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(new ExtensionEvent(trans, c, convertedArgs[0]));            
            Object result;
            try {
                result = c.newInstance(convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(new ExtensionEvent(trans, c, convertedArgs[0]));
            }
            return result;
        } else
            return c.newInstance(convertedArgs[0]);
      }

      else if (-1 != lastDot) {                         // Handle static method call

        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Method m = (methodKey != null) ?
          (Method) getFromCache(methodKey, null, methodArgs) : null;
        
        if (m != null && !trans.getDebug())
        {
          try
          {
            paramTypes = m.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, paramTypes, exprContext);
            return m.invoke(null, convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
            // Must not have been the right one
          }
        }
        className = m_className + funcName.substring(0, lastDot);
        methodName = funcName.substring(lastDot + 1);
        try
        {
          classObj = getClassForName(className);
        }
        catch (ClassNotFoundException e) 
        {
          throw new TransformerException(e);
        }
        m = MethodResolver.getMethod(classObj,
                                     methodName,
                                     methodArgs, 
                                     convertedArgs,
                                     exprContext,
                                     MethodResolver.STATIC_ONLY);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, m);
        
        if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(m, null, convertedArgs[0]);            
            Object result;
            try {
                result = m.invoke(null, convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, null, convertedArgs[0]);
            }
            return result;
        }
        else
            return m.invoke(null, convertedArgs[0]);
      }

      else {                                            // Handle instance method call

        if (args.size() < 1)
        {
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_INSTANCE_MTHD_CALL_REQUIRES, new Object[]{funcName })); //"Instance method call to method " + funcName
                                    //+ " requires an Object instance as first argument");
        }
        targetObject = args.get(0);
        if (targetObject instanceof XObject)          // Next level down for XObjects
          targetObject = ((XObject) targetObject).object();
        methodArgs = new Object[args.size() - 1];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i+1);
        }
        Method m = (methodKey != null) ?
          (Method) getFromCache(methodKey, targetObject, methodArgs) : null;
        
        if (m != null)
        {
          try
          {
            paramTypes = m.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, paramTypes, exprContext);
            return m.invoke(targetObject, convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
            // Must not have been the right one
          }
        }
        classObj = targetObject.getClass();
        m = MethodResolver.getMethod(classObj,
                                     funcName,
                                     methodArgs, 
                                     convertedArgs,
                                     exprContext,
                                     MethodResolver.INSTANCE_ONLY);
        if (methodKey != null)
          putToCache(methodKey, targetObject, methodArgs, m);
        
        if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(m, targetObject, convertedArgs[0]);            
            Object result;
            try {
                result = m.invoke(targetObject, convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, targetObject, convertedArgs[0]);
            }
            return result;
        } else       
            return m.invoke(targetObject, convertedArgs[0]);
      }
    }
    catch (InvocationTargetException ite)
    {
      Throwable resultException = ite;
      Throwable targetException = ite.getTargetException();
 
      if (targetException instanceof TransformerException)
        throw ((TransformerException)targetException);
      else if (targetException != null)
        resultException = targetException;
            
      throw new TransformerException(resultException);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      throw new TransformerException(e);
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
   * For this namespace, only static element methods are currently supported.
   * If instance methods are needed, please let us know your requirements.
   * @param localPart      Element name's local part.
   * @param element        The extension element being processed.
   * @param transformer      Handle to TransformerImpl.
   * @param stylesheetTree The compiled stylesheet tree.
   * @param methodKey      A key that uniquely identifies this element call.
   * @throws IOException           if loading trouble
   * @throws TransformerException          if parsing trouble
   */

  public void processElement (String localPart,
                              ElemTemplateElement element,
                              TransformerImpl transformer,
                              Stylesheet stylesheetTree,
                              Object methodKey)
    throws TransformerException, IOException
  {
    Object result = null;
    Class classObj;

    Method m = (Method) getFromCache(methodKey, null, null);
    if (null == m)
    {
      try
      {
        String fullName = m_className + localPart;
        int lastDot = fullName.lastIndexOf(".");
        if (lastDot < 0)
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_ELEMENT_NAME, new Object[]{fullName })); //"Invalid element name specified " + fullName);
        try
        {
          classObj = getClassForName(fullName.substring(0, lastDot));
        }
        catch (ClassNotFoundException e) 
        {
          throw new TransformerException(e);
        }
        localPart = fullName.substring(lastDot + 1);
        m = MethodResolver.getElementMethod(classObj, localPart);
        if (!Modifier.isStatic(m.getModifiers()))
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ELEMENT_NAME_METHOD_STATIC, new Object[]{fullName })); //"Element name method must be static " + fullName);
      }
      catch (Exception e)
      {
        // e.printStackTrace ();
        throw new TransformerException (e);
      }
      putToCache(methodKey, null, null, m);
    }

    XSLProcessorContext xpc = new XSLProcessorContext(transformer, 
                                                      stylesheetTree);

    try
    {
      if (transformer.getDebug()) {
          transformer.getTraceManager().fireExtensionEvent(m, null, new Object[] {xpc, element});
        try {
            result = m.invoke(null, new Object[] {xpc, element});
        } catch (Exception e) {
            throw e;
        } finally {            
            transformer.getTraceManager().fireExtensionEndEvent(m, null, new Object[] {xpc, element});
        }
      } else
        result = m.invoke(null, new Object[] {xpc, element});
    }
    catch (InvocationTargetException ite)
    {
      Throwable resultException = ite;
      Throwable targetException = ite.getTargetException();
 
      if (targetException instanceof TransformerException)
        throw ((TransformerException)targetException);
      else if (targetException != null)
        resultException = targetException;
            
      throw new TransformerException(resultException);
    }
    catch (Exception e)
    {
      // e.printStackTrace ();
      throw new TransformerException (e);
    }

    if (result != null)
    {
      xpc.outputToResultTree (stylesheetTree, result);
    }
 
  }

}
